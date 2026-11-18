package com.trektravel.dao;

import com.trektravel.model.Batch;
import com.trektravel.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for batches table.
 */
public class BatchDAO {

    public Batch findById(int batchId) {
        String sql = baseBatchSelect() + " WHERE b.batch_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBatch(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Batch> getBatchesByTrekId(int trekId) {
        List<Batch> list = new ArrayList<>();
        String sql = baseBatchSelect() + " WHERE b.trek_id = ? ORDER BY b.start_date ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trekId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBatch(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Batch> getFutureOpenBatchesByTrekId(int trekId) {
        List<Batch> list = new ArrayList<>();
        String sql = baseBatchSelect()
                   + " WHERE b.trek_id = ? AND b.status = 'OPEN' AND b.start_date >= CURDATE() "
                   + " ORDER BY b.start_date ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trekId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBatch(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int createBatch(Batch b) {
        if (b.getStartDate() == null || b.getStartDate().isBefore(LocalDate.now())) {
            return -1; // Cannot create batch in the past
        }
        String sql = "INSERT INTO batches (trek_id, start_date, price_per_person, verified_discount_percent, "
                   + "total_slots, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getTrekId());
            ps.setDate(2, Date.valueOf(b.getStartDate()));
            ps.setBigDecimal(3, b.getPricePerPerson());
            ps.setInt(4, b.getVerifiedDiscountPercent());
            ps.setInt(5, b.getTotalSlots());
            ps.setString(6, b.getStatus() != null ? b.getStatus() : "OPEN");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateBatchStatus(int batchId, String status) {
        String sql = "UPDATE batches SET status = ? WHERE batch_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, batchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasConfirmedBookings(int batchId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE batch_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBatch(int batchId) {
        if (hasConfirmedBookings(batchId)) {
            return false;
        }
        String sql = "DELETE FROM batches WHERE batch_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifyBatchOwnership(int batchId, int organizerId) {
        String sql = "SELECT COUNT(*) FROM batches b JOIN treks t ON b.trek_id = t.trek_id WHERE b.batch_id = ? AND t.organizer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            ps.setInt(2, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String baseBatchSelect() {
        return "SELECT b.batch_id, b.trek_id, b.start_date, b.price_per_person, b.verified_discount_percent, "
             + "b.total_slots, b.status, "
             + "t.name AS trek_name, t.duration_days, t.difficulty, t.state, t.region, "
             + "COALESCE((SELECT SUM(bk.num_participants) FROM bookings bk WHERE bk.batch_id = b.batch_id AND bk.status = 'CONFIRMED'), 0) AS booked_slots "
             + "FROM batches b "
             + "JOIN treks t ON b.trek_id = t.trek_id";
    }

    private Batch mapBatch(ResultSet rs) throws SQLException {
        Batch b = new Batch();
        b.setBatchId(rs.getInt("batch_id"));
        b.setTrekId(rs.getInt("trek_id"));

        Date sd = rs.getDate("start_date");
        if (sd != null) {
            b.setStartDate(sd.toLocalDate());
        }

        b.setPricePerPerson(rs.getBigDecimal("price_per_person"));
        b.setVerifiedDiscountPercent(rs.getInt("verified_discount_percent"));
        b.setTotalSlots(rs.getInt("total_slots"));
        b.setStatus(rs.getString("status"));

        b.setTrekName(rs.getString("trek_name"));
        b.setDurationDays(rs.getInt("duration_days"));
        b.setDifficulty(rs.getString("difficulty"));
        b.setState(rs.getString("state"));
        b.setRegion(rs.getString("region"));
        b.setBookedSlots(rs.getInt("booked_slots"));
        return b;
    }
}
