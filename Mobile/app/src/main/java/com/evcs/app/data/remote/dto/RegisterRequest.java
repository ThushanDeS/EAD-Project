package com.evcs.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("nic")
    private String nic;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("email")
    private String email;
    
    public RegisterRequest(String nic, String name, String phone, String password, String email) {
        this.nic = nic;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.email = email;
    }
    
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}