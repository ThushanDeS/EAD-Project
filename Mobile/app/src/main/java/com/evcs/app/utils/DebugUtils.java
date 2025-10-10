package com.evcs.app.utils;

import android.content.Context;
import android.widget.Toast;
import com.evcs.app.config.ApiConfig;

public class DebugUtils {
    
    public static void showApiConfigInfo(Context context) {
        String info = "Current API Configuration:\n\n" +
                     "Base URL: " + ApiConfig.Endpoints.CURRENT_BASE_URL + "\n" +
                     "Connect Timeout: " + ApiConfig.Timeouts.CONNECT_TIMEOUT_SECONDS + "s\n" +
                     "Read Timeout: " + ApiConfig.Timeouts.READ_TIMEOUT_SECONDS + "s\n\n" +
                     "Available URLs:\n" +
                     "Emulator: " + ApiConfig.Endpoints.EMULATOR_BASE_URL + "\n" +
                     "Device: " + ApiConfig.Endpoints.DEVICE_BASE_URL + "\n" +
                     "Production: " + ApiConfig.Endpoints.PRODUCTION_BASE_URL;
        
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
        android.util.Log.i("DebugUtils", info);
    }
    
    public static void logRegistrationAttempt(String email, String nic) {
        android.util.Log.d("DebugUtils", 
            String.format("Registration attempt - Email: %s, NIC: %s, API URL: %s", 
                email, nic, ApiConfig.Endpoints.CURRENT_BASE_URL));
    }
    
    public static void logApiResponse(int responseCode, String endpoint) {
        String status = responseCode >= 200 && responseCode < 300 ? "SUCCESS" : "ERROR";
        android.util.Log.d("DebugUtils", 
            String.format("API Response - %s: %d for %s", status, responseCode, endpoint));
    }
}