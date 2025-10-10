package com.evcs.app.utils;

import android.content.Context;
import android.util.Log;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.models.Station;
import com.evcs.app.data.remote.ApiClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationNameResolver {
    private static final String TAG = "StationNameResolver";
    private static StationNameResolver instance;
    private final Map<String, Station> stationCache = new HashMap<>();
    private boolean isLoading = false;
    
    public static StationNameResolver getInstance() {
        if (instance == null) {
            instance = new StationNameResolver();
        }
        return instance;
    }
    
    public interface StationResolverCallback {
        void onStationsResolved(List<Booking> bookingsWithStationNames);
        void onError(String error);
    }
    
    /**
     * Resolve station names for a list of bookings
     */
    public void resolveStationNames(List<Booking> bookings, StationResolverCallback callback) {
        if (bookings == null || bookings.isEmpty()) {
            callback.onStationsResolved(bookings);
            return;
        }
        
        // Check if we need to load stations
        boolean needsLoading = false;
        for (Booking booking : bookings) {
            if (booking.getStationId() != null && !stationCache.containsKey(booking.getStationId())) {
                needsLoading = true;
                break;
            }
        }
        
        if (!needsLoading) {
            // All stations are already cached, populate names immediately
            populateStationNames(bookings);
            callback.onStationsResolved(bookings);
            return;
        }
        
        // Load stations if not already loading
        if (!isLoading) {
            loadAllStations(new StationLoadCallback() {
                @Override
                public void onStationsLoaded() {
                    populateStationNames(bookings);
                    callback.onStationsResolved(bookings);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to load stations: " + error);
                    // Still populate with what we have cached
                    populateStationNames(bookings);
                    callback.onStationsResolved(bookings);
                }
            });
        } else {
            // Already loading, just populate with cached data
            populateStationNames(bookings);
            callback.onStationsResolved(bookings);
        }
    }
    
    /**
     * Get station name by ID from cache
     */
    public String getStationName(String stationId) {
        if (stationId == null) {
            Log.d(TAG, "getStationName called with null stationId");
            return "Unknown Station";
        }
        
        Station station = stationCache.get(stationId);
        if (station != null) {
            Log.d(TAG, "Found station name for ID " + stationId + ": " + station.getName());
            return station.getName();
        }
        
        Log.w(TAG, "Station not found in cache for ID: " + stationId + ". Cache size: " + stationCache.size());
        Log.d(TAG, "Available station IDs in cache: " + stationCache.keySet());
        
        return "Station " + stationId.substring(Math.max(0, stationId.length() - 8)); // Show last 8 chars
    }
    
    /**
     * Get station address by ID from cache
     */
    public String getStationAddress(String stationId) {
        if (stationId == null) return "";
        
        Station station = stationCache.get(stationId);
        if (station != null) {
            return station.getAddress();
        }
        
        return "";
    }
    
    private interface StationLoadCallback {
        void onStationsLoaded();
        void onError(String error);
    }
    
    private void loadAllStations(StationLoadCallback callback) {
        if (isLoading) return;
        
        isLoading = true;
        Log.d(TAG, "Loading all stations...");
        
        ApiClient.getInstance().getApiService()
                .getStations(true, null)
                .enqueue(new Callback<List<Station>>() {
                    @Override
                    public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                        isLoading = false;
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<Station> stations = response.body();
                            Log.d(TAG, "Loaded " + stations.size() + " stations");
                            
                            // Cache all stations
                            for (Station station : stations) {
                                if (station.getId() != null) {
                                    Log.d(TAG, "Caching station: " + station.getId() + " -> " + station.getName());
                                    stationCache.put(station.getId(), station);
                                }
                            }
                            
                            Log.d(TAG, "Station cache now contains " + stationCache.size() + " stations");
                            callback.onStationsLoaded();
                        } else {
                            Log.e(TAG, "Failed to load stations: " + response.code());
                            callback.onError("HTTP " + response.code());
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Station>> call, Throwable t) {
                        isLoading = false;
                        Log.e(TAG, "Error loading stations", t);
                        callback.onError(t.getMessage());
                    }
                });
    }
    
    private void populateStationNames(List<Booking> bookings) {
        Log.d(TAG, "Populating station names for " + bookings.size() + " bookings");
        
        for (Booking booking : bookings) {
            if (booking.getStationId() != null) {
                Log.d(TAG, "Processing booking with stationId: " + booking.getStationId());
                Station station = stationCache.get(booking.getStationId());
                if (station != null) {
                    Log.d(TAG, "Setting station name: " + station.getName() + " for booking " + booking.getId());
                    booking.setStationName(station.getName());
                    booking.setStationAddress(station.getAddress());
                } else {
                    // Fallback to a shortened ID
                    String shortId = booking.getStationId();
                    if (shortId.length() > 8) {
                        shortId = "..." + shortId.substring(shortId.length() - 8);
                    }
                    Log.w(TAG, "Station not found for ID: " + booking.getStationId() + ", using fallback: Station " + shortId);
                    booking.setStationName("Station " + shortId);
                    booking.setStationAddress("");
                }
            } else {
                Log.w(TAG, "Booking has null stationId: " + booking.getId());
            }
        }
    }
    
    /**
     * Clear the station cache (useful for testing or when data might be stale)
     */
    public void clearCache() {
        stationCache.clear();
    }
    
    /**
     * Preload stations for better performance
     */
    public void preloadStations() {
        if (stationCache.isEmpty() && !isLoading) {
            Log.d(TAG, "Preloading stations...");
            loadAllStations(new StationLoadCallback() {
                @Override
                public void onStationsLoaded() {
                    Log.d(TAG, "Stations preloaded successfully");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to preload stations: " + error);
                }
            });
        }
    }
    
    /**
     * Test method to verify station name resolution
     */
    public void testStationResolution(String testStationId) {
        Log.d(TAG, "=== Testing Station Resolution ===");
        Log.d(TAG, "Test Station ID: " + testStationId);
        Log.d(TAG, "Cache size: " + stationCache.size());
        Log.d(TAG, "Resolved name: " + getStationName(testStationId));
        Log.d(TAG, "=== End Test ===");
    }
}