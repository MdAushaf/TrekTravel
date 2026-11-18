package com.trektravel.dao;

import com.trektravel.model.Booking;
import com.trektravel.util.DBUtil;
import com.trektravel.util.PriceUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for bookings table.
 * Handles slot locking, price snapshotting, and atomic transactions.
 */
public class BookingDAO {

    public static class BookingResult {
        private final boolean success;
        private final int bookingId;
        private final String message;

        public BookingResult(boolean success, int bookingId, String message) {
            this.success = success;
            this.bookingId = bookingId;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public int getBookingId() { return bookingId; }
        public String getMessage() { return message; }
    }

    /**
     * High-level booking method matching reference pattern.
     * Throws BookingException with a safe, user-facing error message on failure.
     */
    public int book(int userId, int batchId, int numParticipants) throws SQLException, BookingException {
        BookingResult res = createBookingInTransaction(userId, batchId, numParticipants);
        if (!res.isSuccess()) {
            throw new BookingException(res.getMessage());
        }
        return res.getBookingId();
    }

    /**
     * Executes booking creation in a strict atomic transaction with row locking (SELECT ... FOR UPDATE).
     */
    public BookingResult createBookingInTransaction(int userId, int batchId, int numParticipants) {
        if (numParticipants < 1 || numParticipants > 10) {
            return new BookingResult(false, -1, "Number of participants must be between 1 and 10.");
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check trekker profile exists & get verification status
            String profileSql = "SELECT verification_status FROM trekker_profiles WHERE user_id = ?";
            String verificationStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(profileSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        verificationStatus = rs.getString("verification_status");
                    } else {
                        conn.rollback();
                        return new BookingResult(false, -1, "Trekker profile not found. Please complete your emergency contact details before booking.");
                    }
                }
            }

            // 2. Lock batch row and fetch trek status
            String batchSql = "SELECT b.batch_id, b.trek_id, b.start_date, b.price_per_person, "
                            + "b.verified_discount_percent, b.total_slots, b.status, t.status AS trek_status "
                            + "FROM batches b "
                            + "JOIN treks t ON b.trek_id = t.trek_id "
                            + "WHERE b.batch_id = ? FOR UPDATE";

            LocalDate startDate;
            BigDecimal pricePerPerson;
            int verifiedDiscountPercent;
            int totalSlots;
            String batchStatus;
            String trekStatus;

            try (PreparedStatement ps = conn.prepareStatement(batchSql)) {
                ps.setInt(1, batchId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new BookingResult(false, -1, "Selected batch was not found.");
                    }
                    Date sd = rs.getDate("start_date");
                    startDate = (sd != null) ? sd.toLocalDate() : null;
                    pricePerPerson = rs.getBigDecimal("price_per_person");
                    verifiedDiscountPercent = rs.getInt("verified_discount_percent");
                    totalSlots = rs.getInt("total_slots");
                    batchStatus = rs.getString("status");
                    trekStatus = rs.getString("trek_status");
                }
            }

            // Validations
            if (!"APPROVED".equalsIgnoreCase(trekStatus)) {
                conn.rollback();
                return new BookingResult(false, -1, "This trek is currently not approved for public booking.");
            }

            if (!"OPEN".equalsIgnoreCase(batchStatus)) {
                conn.rollback();
                return new BookingResult(false, -1, "This departure batch is closed for booking.");
            }

            if (startDate == null || startDate.isBefore(LocalDate.now())) {
                conn.rollback();
                return new BookingResult(false, -1, "Cannot book a departure batch that has already departed.");
            }

            // 3. Count currently confirmed bookings for this batch (locked)
            String sumSql = "SELECT COALESCE(SUM(num_participants), 0) FROM bookings WHERE batch_id = ? AND status = 'CONFIRMED' FOR UPDATE";
            int bookedSlots = 0;
            try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
                ps.setInt(1, batchId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        bookedSlots = rs.getInt(1);
                    }
                }
            }

            int slotsLeft = totalSlots - bookedSlots;
            if (numParticipants > slotsLeft) {
                conn.rollback();
                return new BookingResult(false, -1, "Only " + Math.max(slotsLeft, 0) + " slot(s) remaining for this departure batch.");
            }

            // 4. Calculate final price snapshot
            // Verified discount applies ONLY if trekker has an APPROVED profile in DB
            int discountPercent = 0;
            if ("APPROVED".equalsIgnoreCase(verificationStatus) && verifiedDiscountPercent > 0) {
                discountPercent = verifiedDiscountPercent;
            }

            BigDecimal totalAmount = PriceUtil.calculateTotal(pricePerPerson, discountPercent, numParticipants);

            // 5. Insert booking row
            String insertSql = "INSERT INTO bookings (user_id, batch_id, num_participants, price_per_person, "
                             + "discount_percent, total_amount, status) VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')";
            int generatedId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, batchId);
                ps.setInt(3, numParticipants);
                ps.setBigDecimal(4, pricePerPerson);
                ps.setInt(5, discountPercent);
                ps.setBigDecimal(6, totalAmount);

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1);
                        }
                    }
                }
            }

            if (generatedId > 0) {
                conn.commit(); // Transaction committed successfully
                String msg = "Booking confirmed successfully!";
                if (discountPercent > 0) {
                    msg += " Verified Trekker discount of " + discountPercent + "% applied.";
                }
                return new BookingResult(true, generatedId, msg);
            } else {
                conn.rollback();
                return new BookingResult(false, -1, "Failed to create booking record.");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return new BookingResult(false, -1, "Database transaction error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public Booking findById(int bookingId) {
        String sql = baseBookingSelect() + " WHERE bk.booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = baseBookingSelect() + " WHERE bk.user_id = ? ORDER BY bk.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Organizer participant list: strictly fetches bookings for this organizer's treks only.
     */
    public List<Booking> getBookingsByOrganizerId(int organizerId) {
        List<Booking> list = new ArrayList<>();
        String sql = baseBookingSelect() + " WHERE t.organizer_id = ? ORDER BY bk.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getAllBookingsForAdmin() {
        List<Booking> list = new ArrayList<>();
        String sql = baseBookingSelect() + " ORDER BY bk.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cancels a booking. Trekkers can cancel up to 2 days before departure. Admins and Organizers can cancel anytime.
     */
    public boolean cancelBooking(int bookingId, int requesterUserId, boolean isAdminOrOrganizer) {
        Booking booking = findById(bookingId);
        if (booking == null || !booking.isConfirmed()) {
            return false;
        }

        // If trekker is cancelling, verify ownership and 2-day rule
        if (!isAdminOrOrganizer) {
            if (booking.getUserId() != requesterUserId) {
                return false;
            }
            if (!booking.canBeCancelled()) {
                return false;
            }
        } else if (!isAdminOrOrganizer && booking.getOrganizerId() != requesterUserId) {
            // Organizer must own the trek
            return false;
        }

        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countConfirmedBookings() {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) FROM bookings WHERE status = 'CONFIRMED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal rev = rs.getBigDecimal(1);
                return (rev != null) ? rev : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private String baseBookingSelect() {
        return "SELECT bk.booking_id, bk.user_id, bk.batch_id, bk.num_participants, bk.price_per_person, "
             + "bk.discount_percent, bk.total_amount, bk.status, bk.created_at, "
             + "b.start_date, "
             + "t.trek_id, t.organizer_id, t.name AS trek_name, t.state AS trek_state, t.region AS trek_region, "
             + "t.difficulty AS trek_difficulty, t.duration_days, "
             + "u.name AS user_name, u.email AS user_email, u.phone AS user_phone, "
             + "p.emergency_contact_name, p.emergency_contact_phone "
             + "FROM bookings bk "
             + "JOIN batches b ON bk.batch_id = b.batch_id "
             + "JOIN treks t ON b.trek_id = t.trek_id "
             + "JOIN users u ON bk.user_id = u.id "
             + "LEFT JOIN trekker_profiles p ON bk.user_id = p.user_id";
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking bk = new Booking();
        bk.setBookingId(rs.getInt("booking_id"));
        bk.setUserId(rs.getInt("user_id"));
        bk.setBatchId(rs.getInt("batch_id"));
        bk.setNumParticipants(rs.getInt("num_participants"));
        bk.setPricePerPerson(rs.getBigDecimal("price_per_person"));
        bk.setDiscountPercent(rs.getInt("discount_percent"));
        bk.setTotalAmount(rs.getBigDecimal("total_amount"));
        bk.setStatus(rs.getString("status"));
        bk.setCreatedAt(rs.getTimestamp("created_at"));

        Date sd = rs.getDate("start_date");
        if (sd != null) {
            bk.setStartDate(sd.toLocalDate());
        }

        bk.setOrganizerId(rs.getInt("organizer_id"));
        bk.setTrekName(rs.getString("trek_name"));
        bk.setState(rs.getString("trek_state"));
        bk.setRegion(rs.getString("trek_region"));
        bk.setDifficulty(rs.getString("trek_difficulty"));
        bk.setDurationDays(rs.getInt("duration_days"));

        bk.setUserName(rs.getString("user_name"));
        bk.setUserEmail(rs.getString("user_email"));
        bk.setUserPhone(rs.getString("user_phone"));
        bk.setEmergencyContactName(rs.getString("emergency_contact_name"));
        bk.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));

        return bk;
    }
}
