package com.evcs.app.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

public class QRDebugUtils {
    private static final String TAG = "QRDebugUtils";
    
    /**
     * Test QR code generation with various scenarios
     */
    public static void testQRGeneration() {
        Log.d(TAG, "=== QR DEBUG TEST START ===");
        
        // Test 1: Simple text
        String simpleText = "Hello World";
        Bitmap simpleBitmap = QRCodeUtils.generateQRCode(simpleText, 300);
        Log.d(TAG, "Simple text QR result: " + (simpleBitmap != null ? "SUCCESS" : "FAILED"));
        
        // Test 2: Your backend's base64 QR code
        String backendQR = "Ym9va2luZzo2OGRiYjNkNjczNmU4ZmZhMjQwNzE1ZDM6NjhkNjNhYWUzNzViNDE5YmYyZmYxOTRlOjIwMjUtMTAtMDFUMTk6MDA6MDAuMDAwMDAwMFo=";
        
        // Decode the base64 to see what it contains
        try {
            byte[] decodedBytes = Base64.decode(backendQR, Base64.DEFAULT);
            String decodedText = new String(decodedBytes);
            Log.d(TAG, "Backend QR decoded text: " + decodedText);
            
            // Test generating QR from decoded text
            Bitmap decodedBitmap = QRCodeUtils.generateQRCode(decodedText, 300);
            Log.d(TAG, "Decoded text QR result: " + (decodedBitmap != null ? "SUCCESS" : "FAILED"));
            
            // Test generating QR from original base64
            Bitmap base64Bitmap = QRCodeUtils.generateQRCode(backendQR, 300);
            Log.d(TAG, "Base64 QR result: " + (base64Bitmap != null ? "SUCCESS" : "FAILED"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error decoding base64", e);
        }
        
        // Test 3: Booking URL format (what the decoded text should look like)
        String bookingUrl = "booking:68dbb3d6736e8ffa240715d3:68d63aae375b419bf2ff194e:2025-10-01T19:00:00.000000Z";
        Bitmap urlBitmap = QRCodeUtils.generateQRCode(bookingUrl, 300);
        Log.d(TAG, "Booking URL QR result: " + (urlBitmap != null ? "SUCCESS" : "FAILED"));
        
        Log.d(TAG, "=== QR DEBUG TEST END ===");
    }
    
    /**
     * Analyze base64 QR code data
     */
    public static void analyzeQRData(String qrData) {
        Log.d(TAG, "=== QR DATA ANALYSIS ===");
        Log.d(TAG, "Original data: " + qrData);
        Log.d(TAG, "Data length: " + (qrData != null ? qrData.length() : 0));
        
        if (qrData == null || qrData.isEmpty()) {
            Log.w(TAG, "QR data is null or empty!");
            return;
        }
        
        // Check if it's base64
        try {
            byte[] decodedBytes = Base64.decode(qrData, Base64.DEFAULT);
            String decodedText = new String(decodedBytes);
            Log.d(TAG, "Is valid base64: YES");
            Log.d(TAG, "Decoded text: " + decodedText);
            Log.d(TAG, "Decoded length: " + decodedText.length());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Is valid base64: NO");
            Log.d(TAG, "Using as plain text: " + qrData);
        }
        
        Log.d(TAG, "=== END ANALYSIS ===");
    }
}