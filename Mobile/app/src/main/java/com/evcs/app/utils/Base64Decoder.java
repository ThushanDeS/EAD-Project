package com.evcs.app.utils;

import android.util.Base64;
import android.util.Log;

/**
 * Manual test to decode the backend QR code and see what it contains
 */
public class Base64Decoder {
    private static final String TAG = "Base64Decoder";
    
    public static void main(String[] args) {
        // Your backend's QR code
        String backendQR = "Ym9va2luZzo2OGRiYjNkNjczNmU4ZmZhMjQwNzE1ZDM6NjhkNjNhYWUzNzViNDE5YmYyZmYxOTRlOjIwMjUtMTAtMDFUMTk6MDA6MDAuMDAwMDAwMFo=";
        
        System.out.println("Original base64: " + backendQR);
        System.out.println("Length: " + backendQR.length());
        
        try {
            // Decode using standard Java Base64 (for testing outside Android)
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(backendQR);
            String decodedText = new String(decodedBytes);
            
            System.out.println("Decoded text: " + decodedText);
            System.out.println("Decoded length: " + decodedText.length());
            
            // This should output something like:
            // "booking:68dbb3d6736e8ffa240715d3:68d63aae375b419bf2ff194e:2025-10-01T19:00:00.000000Z"
            
        } catch (Exception e) {
            System.out.println("Error decoding: " + e.getMessage());
        }
    }
    
    /**
     * Android version for testing in the app
     */
    public static void testAndroidDecode() {
        String backendQR = "Ym9va2luZzo2OGRiYjNkNjczNmU4ZmZhMjQwNzE1ZDM6NjhkNjNhYWUzNzViNDE5YmYyZmYxOTRlOjIwMjUtMTAtMDFUMTk6MDA6MDAuMDAwMDAwMFo=";
        
        Log.d(TAG, "Original base64: " + backendQR);
        Log.d(TAG, "Length: " + backendQR.length());
        
        try {
            byte[] decodedBytes = Base64.decode(backendQR, Base64.DEFAULT);
            String decodedText = new String(decodedBytes);
            
            Log.d(TAG, "Decoded text: " + decodedText);
            Log.d(TAG, "Decoded length: " + decodedText.length());
            
        } catch (Exception e) {
            Log.e(TAG, "Error decoding: " + e.getMessage());
        }
    }
}