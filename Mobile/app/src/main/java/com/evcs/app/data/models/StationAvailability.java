package com.evcs.app.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StationAvailability {
    @SerializedName("stationId")
    private String stationId;
    
    @SerializedName("date")
    private String date;
    
    @SerializedName("blocks")
    private List<TimeBlock> blocks;
    
    // Constructors
    public StationAvailability() {}
    
    public StationAvailability(String stationId, String date, List<TimeBlock> blocks) {
        this.stationId = stationId;
        this.date = date;
        this.blocks = blocks;
    }
    
    // Getters and Setters
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public List<TimeBlock> getBlocks() { return blocks; }
    public void setBlocks(List<TimeBlock> blocks) { this.blocks = blocks; }
    
    public static class TimeBlock {
        @SerializedName("start")
        private String start;
        
        @SerializedName("end")
        private String end;
        
        @SerializedName("free")
        private int free;
        
        @SerializedName("total")
        private int total;
        
        @SerializedName("freeSlots")
        private List<Integer> freeSlots;
        
        // Constructors
        public TimeBlock() {}
        
        public TimeBlock(String start, String end, int free, int total, List<Integer> freeSlots) {
            this.start = start;
            this.end = end;
            this.free = free;
            this.total = total;
            this.freeSlots = freeSlots;
        }
        
        // Getters and Setters
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
        
        public int getFree() { return free; }
        public void setFree(int free) { this.free = free; }
        
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public List<Integer> getFreeSlots() { return freeSlots; }
        public void setFreeSlots(List<Integer> freeSlots) { this.freeSlots = freeSlots; }
        
        public boolean hasAvailableSlots() {
            return free > 0 && freeSlots != null && !freeSlots.isEmpty();
        }
    }
}