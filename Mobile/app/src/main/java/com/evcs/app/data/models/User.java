package com.evcs.app.data.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class User {
    @NonNull
    @SerializedName("_id")
    private String id;
    
    private String role;
    private String email;
    private String nic;
    private String name;
    private String phone;
    private String status;
    private String token;
    
    // Constructors
    public User() {}
    
    public User(String id, String role, String email, String nic, String name, String phone, String status) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.nic = nic;
        this.name = name;
        this.phone = phone;
        this.status = status;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public boolean isEvOwner() {
        return "EvOwner".equals(role);
    }
    
    public boolean isStationOperator() {
        return "StationOperator".equals(role);
    }
}