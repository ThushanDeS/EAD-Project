package com.evcs.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.HashMap;
import java.util.Map;

public class QRCodeUtils {
    private static final String TAG = "QRCodeUtils";
    private static final int DEFAULT_QR_SIZE = 400;
    
    /**
     * Create bitmap from base64 encoded image
     */
    public static Bitmap createBitmapFromBase64(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            Log.e(TAG, "Base64 image data is null or empty");
            return null;
        }
        
        try {
            Log.d(TAG, "Decoding base64 image, length: " + base64Image.length());
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Log.d(TAG, "Decoded bytes length: " + decodedBytes.length);
            
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                Log.d(TAG, "Successfully created bitmap from base64, dimensions: " + 
                      bitmap.getWidth() + "x" + bitmap.getHeight());
            } else {
                Log.e(TAG, "Failed to decode bitmap from base64 bytes");
            }
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding base64 image", e);
            return null;
        }
    }
    
    /**
     * Generate QR code bitmap from base64 string or plain text
     */
    public static Bitmap generateQRCode(String qrData, int size) {
        if (qrData == null || qrData.isEmpty()) {
            Log.e(TAG, "QR data is null or empty");
            return null;
        }
        
        Log.d(TAG, "Starting QR generation for data: " + qrData.substring(0, Math.min(50, qrData.length())) + "...");
        Log.d(TAG, "QR data length: " + qrData.length());
        
        try {
            // Try to decode if it's base64, otherwise use as plain text
            String qrText = qrData;
            if (isBase64(qrData)) {
                try {
                    byte[] decodedBytes = Base64.decode(qrData, Base64.DEFAULT);
                    qrText = new String(decodedBytes);
                    Log.d(TAG, "Decoded base64 QR data: " + qrText);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to decode base64, using original data", e);
                    qrText = qrData;
                }
            } else {
                Log.d(TAG, "Data is not base64, using as plain text");
            }
            
            QRCodeWriter writer = new QRCodeWriter();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            Log.d(TAG, "Encoding QR with text: " + qrText + " and size: " + size);
            BitMatrix bitMatrix = writer.encode(qrText, BarcodeFormat.QR_CODE, size, size, hints);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Log.d(TAG, "BitMatrix created with dimensions: " + width + "x" + height);
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            Log.d(TAG, "QR Code generated successfully for data: " + qrText.substring(0, Math.min(50, qrText.length())) + "...");
            return bitmap;
            
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            return null;
        }
    }
    
    /**
     * Generate QR code with default size
     */
    public static Bitmap generateQRCode(String qrData) {
        return generateQRCode(qrData, DEFAULT_QR_SIZE);
    }
    
    /**
     * Check if string is valid base64
     */
    private static boolean isBase64(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        
        try {
            Base64.decode(str, Base64.DEFAULT);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Validate QR code data
     */
    public static boolean isValidQRData(String qrData) {
        return qrData != null && !qrData.trim().isEmpty();
    }
    
    /**
     * Get decoded QR text for display purposes
     */
    public static String getDecodedQRText(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            return "No QR data available";
        }
        
        if (isBase64(qrData)) {
            try {
                byte[] decodedBytes = Base64.decode(qrData, Base64.DEFAULT);
                return new String(decodedBytes);
            } catch (Exception e) {
                Log.w(TAG, "Failed to decode base64 for display", e);
                return qrData;
            }
        }
        
        return qrData;
    }
}