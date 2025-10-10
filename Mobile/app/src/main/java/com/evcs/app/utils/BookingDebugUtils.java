package com.evcs.app.utils;

import android.util.Log;
import com.evcs.app.data.remote.dto.CreateBookingRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingDebugUtils {
    private static final String TAG = "BookingDebug";
    
    /**
     * Log detailed booking request information for debugging
     */
    public static void logBookingRequest(CreateBookingRequest request, String ownerId) {
        Log.d(TAG, "=== Booking Request Debug ===");
        Log.d(TAG, "Owner ID: " + ownerId);
        Log.d(TAG, "Station ID: " + request.getStationId());
        Log.d(TAG, "Date: " + request.getDate());
        Log.d(TAG, "Start Time: " + request.getStartTime());
        Log.d(TAG, "End Time: " + request.getEndTime());
        Log.d(TAG, "Slot Number: " + request.getSlotNumber());
        
        // Log as JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonRequest = gson.toJson(request);
        Log.d(TAG, "JSON Request Body: " + jsonRequest);
        
        Log.d(TAG, "=== End Debug ===");
    }
    
    /**
     * Generate a sample booking request for testing
     */
    public static CreateBookingRequest createTestBookingRequest(String stationId) {
        // Create a booking for tomorrow, 2 hours from now
        long tomorrow = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours from now
        long startTime = tomorrow + (2 * 60 * 60 * 1000); // 2 hours after tomorrow starts
        long endTime = startTime + (2 * 60 * 60 * 1000); // 2 hours duration
        
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        
        String startTimeStr = isoFormat.format(new Date(startTime));
        String endTimeStr = isoFormat.format(new Date(endTime));
        
        return new CreateBookingRequest(stationId, startTimeStr, endTimeStr, 1);
    }
    
    /**
     * Validate if the booking times meet API requirements
     */
    public static String validateBookingTimes(long startTimeMillis, long endTimeMillis) {
        long now = System.currentTimeMillis();
        long twelveHoursFromNow = now + (12 * 60 * 60 * 1000);
        long sevenDaysFromNow = now + (7 * 24 * 60 * 60 * 1000);
        
        if (startTimeMillis < now) {
            return "Start time cannot be in the past";
        }
        
        if (startTimeMillis < twelveHoursFromNow) {
            return "Bookings must be made at least 12 hours in advance";
        }
        
        if (startTimeMillis > sevenDaysFromNow) {
            return "Bookings can only be made up to 7 days in advance";
        }
        
        if (endTimeMillis <= startTimeMillis) {
            return "End time must be after start time";
        }
        
        long durationMillis = endTimeMillis - startTimeMillis;
        long durationMinutes = durationMillis / (1000 * 60);
        
        if (durationMinutes < 30) {
            return "Minimum booking duration is 30 minutes";
        }
        
        if (durationMinutes > 480) { // 8 hours
            return "Maximum booking duration is 8 hours";
        }
        
        return "OK";
    }
}