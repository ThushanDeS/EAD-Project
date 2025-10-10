package com.evcs.app.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.evcs.app.data.models.Booking;
import java.util.ArrayList;
import java.util.List;

public class StationNameTestUtils {
    private static final String TAG = "StationNameTest";
    
    /**
     * Test station name resolution with a sample booking
     */
    public static void testStationNameResolution(Context context, String testStationId) {
        Log.d(TAG, "=== Testing Station Name Resolution ===");
        Log.d(TAG, "Test Station ID: " + testStationId);
        
        // Test direct resolution
        StationNameResolver resolver = StationNameResolver.getInstance();
        String stationName = resolver.getStationName(testStationId);
        
        Log.d(TAG, "Direct resolution result: " + stationName);
        Toast.makeText(context, "Station ID: " + testStationId + "\nResolved to: " + stationName, Toast.LENGTH_LONG).show();
        
        // Test with a sample booking
        Booking testBooking = new Booking();
        testBooking.setId("test-booking-123");
        testBooking.setStationId(testStationId);
        testBooking.setSlotNumber(1);
        testBooking.setStatus("pending");
        
        List<Booking> testBookings = new ArrayList<>();
        testBookings.add(testBooking);
        
        resolver.resolveStationNames(testBookings, new StationNameResolver.StationResolverCallback() {
            @Override
            public void onStationsResolved(List<Booking> bookingsWithStationNames) {
                if (!bookingsWithStationNames.isEmpty()) {
                    Booking resolvedBooking = bookingsWithStationNames.get(0);
                    String resolvedName = resolvedBooking.getStationName();
                    Log.d(TAG, "Booking resolution result: " + resolvedName);
                    Toast.makeText(context, "Booking resolved station name: " + resolvedName, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Station resolution error: " + error);
                Toast.makeText(context, "Resolution error: " + error, Toast.LENGTH_LONG).show();
            }
        });
        
        Log.d(TAG, "=== End Test ===");
    }
    
    /**
     * Test with the Fort Station ID from your example
     */
    public static void testFortStation(Context context) {
        testStationNameResolution(context, "68d63aae375b419bf2ff194e");
    }
    
    /**
     * Show cache status
     */
    public static void showCacheStatus(Context context) {
        StationNameResolver resolver = StationNameResolver.getInstance();
        
        // This will trigger debug logging
        resolver.testStationResolution("68d63aae375b419bf2ff194e");
        
        Toast.makeText(context, "Check logcat for cache status details", Toast.LENGTH_SHORT).show();
    }
}