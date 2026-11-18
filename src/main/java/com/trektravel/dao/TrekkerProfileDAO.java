package com.trektravel.dao;

import com.trektravel.model.TrekkerProfile;
import com.trektravel.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for trekker_profiles table.
 */
public class TrekkerProfileDAO {

    public TrekkerProfile findByUserId(int userId) {
        String sql = baseProfileSelect() + " WHERE p.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TrekkerProfile findById(int profileId) {
        String sql = baseProfileSelect() + " WHERE p.profile_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveOrUpdate(TrekkerProfile profile) {
        TrekkerProfile existing = findByUserId(profile.getUserId());
        if (existing == null) {
            return insertProfile(profile);
        } else {
            profile.setProfileId(existing.getProfileId());
            return updateProfile(profile);
        }
    }

    private boolean insertProfile(TrekkerProfile p) {
        String sql = "INSERT INTO trekker_profiles (user_id, date_of_birth, city, emergency_contact_name, "
                   + "emergency_contact_phone, fitness_level, medical_notes, id_proof_file, verification_status, "
                   + "submitted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId());
            ps.setDate(2, Date.valueOf(p.getDateOfBirth()));
            ps.setString(3, p.getCity());
            ps.setString(4, p.getEmergencyContactName());
            ps.setString(5, p.getEmergencyContactPhone());
            ps.setString(6, p.getFitnessLevel());
            ps.setString(7, p.getMedicalNotes());
            ps.setString(8, p.getIdProofFile());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setProfileId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateProfile(TrekkerProfile p) {
        String sql = "UPDATE trekker_profiles SET date_of_birth = ?, city = ?, emergency_contact_name = ?, "
                   + "emergency_contact_phone = ?, fitness_level = ?, medical_notes = ?, "
                   + (p.getIdProofFile() != null ? "id_proof_file = ?, " : "")
                   + "verification_status = 'PENDING', admin_remarks = NULL, submitted_at = NOW(), verified_at = NULL "
                   + "WHERE profile_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(p.getDateOfBirth()));
            ps.setString(idx++, p.getCity());
            ps.setString(idx++, p.getEmergencyContactName());
            ps.setString(idx++, p.getEmergencyContactPhone());
            ps.setString(idx++, p.getFitnessLevel());
            ps.setString(idx++, p.getMedicalNotes());
            if (p.getIdProofFile() != null) {
                ps.setString(idx++, p.getIdProofFile());
            }
            ps.setInt(idx, p.getProfileId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateVerificationStatus(int profileId, String status, String remarks) {
        String sql = "UPDATE trekker_profiles SET verification_status = ?, admin_remarks = ?, "
                   + "verified_at = (CASE WHEN ? = 'APPROVED' THEN NOW() ELSE NULL END) "
                   + "WHERE profile_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setString(3, status);
            ps.setInt(4, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<TrekkerProfile> getAllProfiles(String statusFilter) {
        List<TrekkerProfile> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseProfileSelect());
        boolean hasFilter = statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter);
        if (hasFilter) {
            sql.append(" WHERE p.verification_status = ?");
        }
        sql.append(" ORDER BY p.submitted_at DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasFilter) {
                ps.setString(1, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProfile(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countPendingProfiles() {
        String sql = "SELECT COUNT(*) FROM trekker_profiles WHERE verification_status = 'PENDING'";
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

    private String baseProfileSelect() {
        return "SELECT p.profile_id, p.user_id, p.date_of_birth, p.city, p.emergency_contact_name, "
             + "p.emergency_contact_phone, p.fitness_level, p.medical_notes, p.id_proof_file, "
             + "p.verification_status, p.admin_remarks, p.submitted_at, p.verified_at, "
             + "u.name AS user_name, u.email AS user_email, u.phone AS user_phone "
             + "FROM trekker_profiles p "
             + "JOIN users u ON p.user_id = u.id";
    }

    private TrekkerProfile mapProfile(ResultSet rs) throws SQLException {
        TrekkerProfile p = new TrekkerProfile();
        p.setProfileId(rs.getInt("profile_id"));
        p.setUserId(rs.getInt("user_id"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            p.setDateOfBirth(dob.toLocalDate());
        }

        p.setCity(rs.getString("city"));
        p.setEmergencyContactName(rs.getString("emergency_contact_name"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        p.setFitnessLevel(rs.getString("fitness_level"));
        p.setMedicalNotes(rs.getString("medical_notes"));
        p.setIdProofFile(rs.getString("id_proof_file"));
        p.setVerificationStatus(rs.getString("verification_status"));
        p.setAdminRemarks(rs.getString("admin_remarks"));
        p.setSubmittedAt(rs.getTimestamp("submitted_at"));
        p.setVerifiedAt(rs.getTimestamp("verified_at"));

        p.setUserName(rs.getString("user_name"));
        p.setUserEmail(rs.getString("user_email"));
        p.setUserPhone(rs.getString("user_phone"));
        return p;
    }
}
