package com.evcs.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("email")
    private String email;
    
    @SerializedName("nic")
    private String nic;
    
    @SerializedName("password")
    private String password;
    
    public LoginRequest(String email, String nic, String password) {
        this.email = email;
        this.nic = nic;
        this.password = password;
    }
    
    // Constructor for email login
    public static LoginRequest forEmail(String email, String password) {
        return new LoginRequest(email, null, password);
    }
    
    // Constructor for NIC login
    public static LoginRequest forNic(String nic, String password) {
        return new LoginRequest(null, nic, password);
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}