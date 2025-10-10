package com.evcs.app.utils;

import android.app.Activity;
import android.util.Log;
import com.evcs.app.data.models.Booking;
import com.evcs.app.ui.adapters.BookingAdapter;
import java.util.List;

public class BookingUtils {
    private static final String TAG = "BookingUtils";
    
    /**
     * Resolve station names for bookings and update adapter
     */
    public static void resolveStationNamesAndUpdateAdapter(List<Booking> bookings, 
                                                          BookingAdapter adapter, 
                                                          Activity activity) {
        if (bookings == null || adapter == null || activity == null) {
            return;
        }
        
        StationNameResolver.getInstance()
                .resolveStationNames(bookings, new StationNameResolver.StationResolverCallback() {
                    @Override
                    public void onStationsResolved(List<Booking> bookingsWithStationNames) {
                        activity.runOnUiThread(() -> {
                            adapter.updateBookings(bookingsWithStationNames);
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "Station name resolution error: " + error);
                        // Still update adapter with original bookings
                        activity.runOnUiThread(() -> {
                            adapter.updateBookings(bookings);
                        });
                    }
                });
    }
    
    /**
     * Resolve station names for bookings with custom callback
     */
    public static void resolveStationNamesWithCallback(List<Booking> bookings,
                                                      Activity activity,
                                                      BookingStationResolvedCallback callback) {
        if (bookings == null || activity == null || callback == null) {
            return;
        }
        
        StationNameResolver.getInstance()
                .resolveStationNames(bookings, new StationNameResolver.StationResolverCallback() {
                    @Override
                    public void onStationsResolved(List<Booking> bookingsWithStationNames) {
                        activity.runOnUiThread(() -> {
                            callback.onStationsResolved(bookingsWithStationNames);
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "Station name resolution error: " + error);
                        activity.runOnUiThread(() -> {
                            callback.onError(error, bookings);
                        });
                    }
                });
    }
    
    public interface BookingStationResolvedCallback {
        void onStationsResolved(List<Booking> bookingsWithStationNames);
        void onError(String error, List<Booking> originalBookings);
    }
}