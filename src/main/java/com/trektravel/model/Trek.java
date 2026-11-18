package com.trektravel.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model representing a trek listed by an Organizer.
 */
public class Trek implements Serializable {
    private static final long serialVersionUID = 1L;

    private int trekId;
    private int organizerId;
    private String name;
    private String state;
    private String region;
    private String baseVillage;
    private String difficulty; // 'EASY', 'MODERATE', 'DIFFICULT'
    private int durationDays;
    private int maxAltitudeM;
    private BigDecimal trekDistanceKm;
    private String bestSeason;
    private String description;
    private String highlights;
    private String inclusions;
    private String status; // 'PENDING', 'APPROVED', 'REJECTED'
    private Timestamp createdAt;

    // Joined / aggregation fields for UI cards
    private String organizerName;
    private BigDecimal minPrice;
    private int maxDiscountPercent;
    private int openBatchesCount;

    public Trek() {}

    public int getTrekId() { return trekId; }
    public void setTrekId(int trekId) { this.trekId = trekId; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getBaseVillage() { return baseVillage; }
    public void setBaseVillage(String baseVillage) { this.baseVillage = baseVillage; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getMaxAltitudeM() { return maxAltitudeM; }
    public void setMaxAltitudeM(int maxAltitudeM) { this.maxAltitudeM = maxAltitudeM; }

    public BigDecimal getTrekDistanceKm() { return trekDistanceKm; }
    public void setTrekDistanceKm(BigDecimal trekDistanceKm) { this.trekDistanceKm = trekDistanceKm; }

    public String getBestSeason() { return bestSeason; }
    public void setBestSeason(String bestSeason) { this.bestSeason = bestSeason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String highlights) { this.highlights = highlights; }

    public String getInclusions() { return inclusions; }
    public void setInclusions(String inclusions) { this.inclusions = inclusions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public int getMaxDiscountPercent() { return maxDiscountPercent; }
    public void setMaxDiscountPercent(int maxDiscountPercent) { this.maxDiscountPercent = maxDiscountPercent; }

    public int getOpenBatchesCount() { return openBatchesCount; }
    public void setOpenBatchesCount(int openBatchesCount) { this.openBatchesCount = openBatchesCount; }

    public boolean isApproved() { return "APPROVED".equalsIgnoreCase(status); }
    public boolean isPending() { return "PENDING".equalsIgnoreCase(status); }
    public boolean isRejected() { return "REJECTED".equalsIgnoreCase(status); }
}
