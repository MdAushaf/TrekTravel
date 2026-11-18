package com.trektravel.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Model representing personal details, fitness profile, and Govt-ID verification for a Trekker.
 */
public class TrekkerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private int profileId;
    private int userId;
    private LocalDate dateOfBirth;
    private String city;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String fitnessLevel; // 'BEGINNER', 'INTERMEDIATE', 'EXPERIENCED'
    private String medicalNotes;
    private String idProofFile;
    private String verificationStatus; // 'PENDING', 'APPROVED', 'REJECTED'
    private String adminRemarks;
    private Timestamp submittedAt;
    private Timestamp verifiedAt;

    // Joined User fields for UI display
    private String userName;
    private String userEmail;
    private String userPhone;

    public TrekkerProfile() {}

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getIdProofFile() { return idProofFile; }
    public void setIdProofFile(String idProofFile) { this.idProofFile = idProofFile; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public Timestamp getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Timestamp verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public boolean isApproved() { return "APPROVED".equalsIgnoreCase(verificationStatus); }
    public boolean isPending() { return "PENDING".equalsIgnoreCase(verificationStatus); }
    public boolean isRejected() { return "REJECTED".equalsIgnoreCase(verificationStatus); }
}
