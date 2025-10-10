package com.evcs.app.data.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Booking implements Serializable {
    @NonNull
    @SerializedName("_id")
    private String id;
    
    // Handle both string and object stationId from API
    private String stationId;
    private String ownerId;
    private int slotNumber;
    private Date startTime;
    private Date endTime;
    private String status; // pending, approved, in_progress, completed, cancelled
    private String qrCode;
    @SerializedName("qrImageBase64")
    private String qrImageBase64;
    @SerializedName("qrImageContentType")
    private String qrImageContentType;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;
    private String finalizedBy;
    private Date finalizedAt;
    private Double energyKWh;
    private String notes;
    
    // For local display
    private String stationName;
    private String stationAddress;
    private String ownerName;
    private String ownerNic;
    
    // Constructors
    public Booking() {}
    
    public Booking(String id, String stationId, String ownerId, int slotNumber, 
                  Date startTime, Date endTime, String status, String qrCode) {
        this.id = id;
        this.stationId = stationId;
        this.ownerId = ownerId;
        this.slotNumber = slotNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.qrCode = qrCode;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public int getSlotNumber() { return slotNumber; }
    public void setSlotNumber(int slotNumber) { this.slotNumber = slotNumber; }
    
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public String getQrImageBase64() { return qrImageBase64; }
    public void setQrImageBase64(String qrImageBase64) { this.qrImageBase64 = qrImageBase64; }
    
    public String getQrImageContentType() { return qrImageContentType; }
    public void setQrImageContentType(String qrImageContentType) { this.qrImageContentType = qrImageContentType; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getFinalizedBy() { return finalizedBy; }
    public void setFinalizedBy(String finalizedBy) { this.finalizedBy = finalizedBy; }
    
    public Date getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(Date finalizedAt) { this.finalizedAt = finalizedAt; }
    
    public Double getEnergyKWh() { return energyKWh; }
    public void setEnergyKWh(Double energyKWh) { this.energyKWh = energyKWh; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    
    public String getStationAddress() { return stationAddress; }
    public void setStationAddress(String stationAddress) { this.stationAddress = stationAddress; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getOwnerNic() { return ownerNic; }
    public void setOwnerNic(String ownerNic) { this.ownerNic = ownerNic; }
    
    // Helper methods
    public boolean isPending() { return "pending".equals(status); }
    public boolean isApproved() { return "approved".equals(status); }
    public boolean isInProgress() { return "in_progress".equals(status); }
    public boolean isCompleted() { return "completed".equals(status); }
    public boolean isCancelled() { return "cancelled".equals(status); }
    
    public boolean canBeCancelled() {
        return isPending() || isApproved();
    }
    
    public boolean isActive() {
        return isPending() || isApproved() || isInProgress();
    }
}