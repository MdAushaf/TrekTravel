package com.trektravel.model;

import com.trektravel.util.PriceUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model representing a dated departure batch for a trek.
 */
public class Batch implements Serializable {
    private static final long serialVersionUID = 1L;

    private int batchId;
    private int trekId;
    private LocalDate startDate;
    private BigDecimal pricePerPerson;
    private int verifiedDiscountPercent;
    private int totalSlots;
    private String status; // 'OPEN', 'CLOSED'

    // Computed / joined fields
    private int durationDays;
    private String trekName;
    private String difficulty;
    private String state;
    private String region;
    private int bookedSlots;

    public Batch() {}

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public int getTrekId() { return trekId; }
    public void setTrekId(int trekId) { this.trekId = trekId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public BigDecimal getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(BigDecimal pricePerPerson) { this.pricePerPerson = pricePerPerson; }

    public int getVerifiedDiscountPercent() { return verifiedDiscountPercent; }
    public void setVerifiedDiscountPercent(int verifiedDiscountPercent) { this.verifiedDiscountPercent = verifiedDiscountPercent; }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getTrekName() { return trekName; }
    public void setTrekName(String trekName) { this.trekName = trekName; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public int getBookedSlots() { return bookedSlots; }
    public void setBookedSlots(int bookedSlots) { this.bookedSlots = bookedSlots; }

    /**
     * End date is dynamically calculated: startDate + durationDays - 1.
     */
    public LocalDate getEndDate() {
        if (startDate == null) return null;
        int days = durationDays > 0 ? durationDays : 1;
        return startDate.plusDays(days - 1);
    }

    public int getSlotsLeft() {
        int left = totalSlots - bookedSlots;
        return Math.max(left, 0);
    }

    public BigDecimal getDiscountedPrice() {
        return PriceUtil.calculateDiscountedPricePerPerson(pricePerPerson, verifiedDiscountPercent);
    }

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status) && (startDate == null || !startDate.isBefore(LocalDate.now()));
    }
}
