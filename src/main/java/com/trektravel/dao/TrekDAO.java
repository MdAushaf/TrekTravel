package com.trektravel.dao;

import com.trektravel.model.Trek;
import com.trektravel.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for treks table.
 */
public class TrekDAO {

    public Trek findById(int trekId) {
        String sql = baseTrekSelect() + " WHERE t.trek_id = ?" + baseTrekGroupBy();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trekId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrek(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Trek findApprovedById(int trekId) {
        String sql = baseTrekSelect() + " WHERE t.trek_id = ? AND t.status = 'APPROVED'" + baseTrekGroupBy();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trekId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrek(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Searches approved treks with optional filters: state/region, difficulty, departure month (1-12), maxPrice.
     * Only returns treks having at least one OPEN future batch.
     */
    public List<Trek> searchTreks(String stateOrRegion, String difficulty, Integer month, BigDecimal maxPrice) {
        List<Trek> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, ")
           .append("t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, ")
           .append("t.description, t.highlights, t.inclusions, t.status, t.created_at, ")
           .append("u.name AS organizer_name, ")
           .append("MIN(b.price_per_person) AS min_price, ")
           .append("MAX(b.verified_discount_percent) AS max_discount_percent, ")
           .append("COUNT(b.batch_id) AS open_batches_count ")
           .append("FROM treks t ")
           .append("JOIN users u ON t.organizer_id = u.id ")
           .append("LEFT JOIN batches b ON t.trek_id = b.trek_id AND b.status = 'OPEN' AND b.start_date >= CURDATE() ")
           .append("WHERE t.status = 'APPROVED' ");

        List<Object> params = new ArrayList<>();

        if (stateOrRegion != null && !stateOrRegion.trim().isEmpty()) {
            sql.append("AND (LOWER(t.state) LIKE ? OR LOWER(t.region) LIKE ? OR LOWER(t.name) LIKE ?) ");
            String term = "%" + stateOrRegion.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (difficulty != null && !difficulty.trim().isEmpty() && !"ALL".equalsIgnoreCase(difficulty)) {
            sql.append("AND t.difficulty = ? ");
            params.add(difficulty.trim().toUpperCase());
        }

        if (month != null && month >= 1 && month <= 12) {
            sql.append("AND MONTH(b.start_date) = ? ");
            params.add(month);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND b.price_per_person <= ? ");
            params.add(maxPrice);
        }

        sql.append("GROUP BY t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, ")
           .append("t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, ")
           .append("t.description, t.highlights, t.inclusions, t.status, t.created_at, u.name ")
           .append("ORDER BY min_price ASC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrek(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Trek> getFeaturedTreks(int limit) {
        List<Trek> list = new ArrayList<>();
        String sql = "SELECT t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, "
                   + "t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, "
                   + "t.description, t.highlights, t.inclusions, t.status, t.created_at, "
                   + "u.name AS organizer_name, "
                   + "MIN(b.price_per_person) AS min_price, "
                   + "MAX(b.verified_discount_percent) AS max_discount_percent, "
                   + "COUNT(b.batch_id) AS open_batches_count "
                   + "FROM treks t "
                   + "JOIN users u ON t.organizer_id = u.id "
                   + "LEFT JOIN batches b ON t.trek_id = b.trek_id AND b.status = 'OPEN' AND b.start_date >= CURDATE() "
                   + "WHERE t.status = 'APPROVED' "
                   + "GROUP BY t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, "
                   + "t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, "
                   + "t.description, t.highlights, t.inclusions, t.status, t.created_at, u.name "
                   + "HAVING open_batches_count > 0 "
                   + "ORDER BY t.created_at DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrek(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Trek> getTreksByOrganizer(int organizerId) {
        List<Trek> list = new ArrayList<>();
        String sql = "SELECT t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, "
                   + "t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, "
                   + "t.description, t.highlights, t.inclusions, t.status, t.created_at, "
                   + "u.name AS organizer_name, "
                   + "MIN(b.price_per_person) AS min_price, "
                   + "MAX(b.verified_discount_percent) AS max_discount_percent, "
                   + "COUNT(b.batch_id) AS open_batches_count "
                   + "FROM treks t "
                   + "JOIN users u ON t.organizer_id = u.id "
                   + "LEFT JOIN batches b ON t.trek_id = b.trek_id "
                   + "WHERE t.organizer_id = ? "
                   + "GROUP BY t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, "
                   + "t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, "
                   + "t.description, t.highlights, t.inclusions, t.status, t.created_at, u.name "
                   + "ORDER BY t.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrek(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int createTrek(Trek t) {
        String sql = "INSERT INTO treks (organizer_id, name, state, region, base_village, difficulty, "
                   + "duration_days, max_altitude_m, trek_distance_km, best_season, description, "
                   + "highlights, inclusions, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getOrganizerId());
            ps.setString(2, t.getName());
            ps.setString(3, t.getState());
            ps.setString(4, t.getRegion());
            ps.setString(5, t.getBaseVillage());
            ps.setString(6, t.getDifficulty());
            ps.setInt(7, t.getDurationDays());
            ps.setInt(8, t.getMaxAltitudeM());
            ps.setBigDecimal(9, t.getTrekDistanceKm());
            ps.setString(10, t.getBestSeason());
            ps.setString(11, t.getDescription());
            ps.setString(12, t.getHighlights());
            ps.setString(13, t.getInclusions());

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

    /**
     * Editing a trek resets status to PENDING per specification §4.3.
     */
    public boolean updateTrek(Trek t) {
        String sql = "UPDATE treks SET name = ?, state = ?, region = ?, base_village = ?, "
                   + "difficulty = ?, duration_days = ?, max_altitude_m = ?, trek_distance_km = ?, "
                   + "best_season = ?, description = ?, highlights = ?, inclusions = ?, "
                   + "status = 'PENDING' "
                   + "WHERE trek_id = ? AND organizer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getState());
            ps.setString(3, t.getRegion());
            ps.setString(4, t.getBaseVillage());
            ps.setString(5, t.getDifficulty());
            ps.setInt(6, t.getDurationDays());
            ps.setInt(7, t.getMaxAltitudeM());
            ps.setBigDecimal(8, t.getTrekDistanceKm());
            ps.setString(9, t.getBestSeason());
            ps.setString(10, t.getDescription());
            ps.setString(11, t.getHighlights());
            ps.setString(12, t.getInclusions());
            ps.setInt(13, t.getTrekId());
            ps.setInt(14, t.getOrganizerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasBookings(int trekId) {
        String sql = "SELECT COUNT(*) FROM bookings b JOIN batches ba ON b.batch_id = ba.batch_id WHERE ba.trek_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trekId);
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

    public boolean deleteTrek(int trekId, int organizerId) {
        if (hasBookings(trekId)) {
            return false;
        }
        String sql = "DELETE FROM treks WHERE trek_id = ? AND organizer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trekId);
            ps.setInt(2, organizerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Trek> getAllTreksForAdmin(String statusFilter) {
        List<Trek> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, ")
           .append("t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, ")
           .append("t.description, t.highlights, t.inclusions, t.status, t.created_at, ")
           .append("u.name AS organizer_name, ")
           .append("MIN(b.price_per_person) AS min_price, ")
           .append("MAX(b.verified_discount_percent) AS max_discount_percent, ")
           .append("COUNT(b.batch_id) AS open_batches_count ")
           .append("FROM treks t ")
           .append("JOIN users u ON t.organizer_id = u.id ")
           .append("LEFT JOIN batches b ON t.trek_id = b.trek_id ");

        boolean hasFilter = statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter);
        if (hasFilter) {
            sql.append("WHERE t.status = ? ");
        }

        sql.append("GROUP BY t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, ")
           .append("t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, ")
           .append("t.description, t.highlights, t.inclusions, t.status, t.created_at, u.name ")
           .append("ORDER BY t.created_at DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasFilter) {
                ps.setString(1, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrek(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateTrekStatus(int trekId, String status) {
        String sql = "UPDATE treks SET status = ? WHERE trek_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, trekId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countPendingTreks() {
        String sql = "SELECT COUNT(*) FROM treks WHERE status = 'PENDING'";
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

    private String baseTrekSelect() {
        return "SELECT t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, "
             + "t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, "
             + "t.description, t.highlights, t.inclusions, t.status, t.created_at, "
             + "u.name AS organizer_name, "
             + "MIN(b.price_per_person) AS min_price, "
             + "MAX(b.verified_discount_percent) AS max_discount_percent, "
             + "COUNT(b.batch_id) AS open_batches_count "
             + "FROM treks t "
             + "JOIN users u ON t.organizer_id = u.id "
             + "LEFT JOIN batches b ON t.trek_id = b.trek_id ";
    }

    private String baseTrekGroupBy() {
        return " GROUP BY t.trek_id, t.organizer_id, t.name, t.state, t.region, t.base_village, "
             + "t.difficulty, t.duration_days, t.max_altitude_m, t.trek_distance_km, t.best_season, "
             + "t.description, t.highlights, t.inclusions, t.status, t.created_at, u.name";
    }

    private Trek mapTrek(ResultSet rs) throws SQLException {
        Trek t = new Trek();
        t.setTrekId(rs.getInt("trek_id"));
        t.setOrganizerId(rs.getInt("organizer_id"));
        t.setName(rs.getString("name"));
        t.setState(rs.getString("state"));
        t.setRegion(rs.getString("region"));
        t.setBaseVillage(rs.getString("base_village"));
        t.setDifficulty(rs.getString("difficulty"));
        t.setDurationDays(rs.getInt("duration_days"));
        t.setMaxAltitudeM(rs.getInt("max_altitude_m"));
        t.setTrekDistanceKm(rs.getBigDecimal("trek_distance_km"));
        t.setBestSeason(rs.getString("best_season"));
        t.setDescription(rs.getString("description"));
        t.setHighlights(rs.getString("highlights"));
        t.setInclusions(rs.getString("inclusions"));
        t.setStatus(rs.getString("status"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setOrganizerName(rs.getString("organizer_name"));

        BigDecimal minP = rs.getBigDecimal("min_price");
        t.setMinPrice(minP != null ? minP : BigDecimal.ZERO);
        t.setMaxDiscountPercent(rs.getInt("max_discount_percent"));
        t.setOpenBatchesCount(rs.getInt("open_batches_count"));
        return t;
    }
}
