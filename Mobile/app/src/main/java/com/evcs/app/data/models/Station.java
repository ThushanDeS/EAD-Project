package com.evcs.app.data.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class Station {
    @NonNull
    @SerializedName("_id")
    private String id;
    
    private String name;
    private String type; // AC or DC
    private int slotCount;
    private boolean active;
    private double lat;
    private double lng;
    private String address;
    
    private OperatingHours hours;
    
    // Constructors
    public Station() {}
    
    public Station(String id, String name, String type, int slotCount, boolean active, 
                  double lat, double lng, String address, OperatingHours hours) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.slotCount = slotCount;
        this.active = active;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.hours = hours;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getSlotCount() { return slotCount; }
    public void setSlotCount(int slotCount) { this.slotCount = slotCount; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public OperatingHours getHours() { return hours; }
    public void setHours(OperatingHours hours) { this.hours = hours; }
    
    public static class OperatingHours {
        private String open;
        private String close;
        
        public OperatingHours() {}
        
        public OperatingHours(String open, String close) {
            this.open = open;
            this.close = close;
        }
        
        public String getOpen() { return open; }
        public void setOpen(String open) { this.open = open; }
        
        public String getClose() { return close; }
        public void setClose(String close) { this.close = close; }
    }
}