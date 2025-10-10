package com.evcs.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateBookingRequest {
    @SerializedName("stationId")
    private String stationId;
    
    @SerializedName("date")
    private String date;
    
    @SerializedName("startTime")
    private String startTime;
    
    @SerializedName("endTime")
    private String endTime;
    
    @SerializedName("slotNumber")
    private int slotNumber;
    
    public CreateBookingRequest(String stationId, String startTime, String endTime, int slotNumber) {
        this.stationId = stationId;
        this.date = startTime.substring(0, 10); // Extract date from startTime (YYYY-MM-DD)
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotNumber = slotNumber;
    }
    
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public int getSlotNumber() { return slotNumber; }
    public void setSlotNumber(int slotNumber) { this.slotNumber = slotNumber; }
}