package com.evcs.app.ui.debug;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.models.User;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.data.remote.dto.CreateBookingRequest;
import com.evcs.app.utils.SharedPreferencesManager;
import com.evcs.app.utils.BookingDebugUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingTestUtils {
    private static final String TAG = "BookingTest";
    
    /**
     * Test booking creation with sample data
     */
    public static void testBookingCreation(Context context, String testStationId) {
        SharedPreferencesManager prefsManager = new SharedPreferencesManager(context);
        User currentUser = prefsManager.getCurrentUser();
        
        if (currentUser == null || currentUser.getId() == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Testing booking creation...");
        Log.d(TAG, "User ID: " + currentUser.getId());
        Log.d(TAG, "Station ID: " + testStationId);
        
        // Create a test booking for tomorrow
        CreateBookingRequest testRequest = BookingDebugUtils.createTestBookingRequest(testStationId);
        BookingDebugUtils.logBookingRequest(testRequest, currentUser.getId());
        
        ApiClient.getInstance().getApiService()
                .createBooking(testRequest, currentUser.getId())
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(Call<Booking> call, Response<Booking> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "✅ Test booking created successfully!");
                            Log.d(TAG, "Booking ID: " + response.body().getId());
                            Toast.makeText(context, "Test booking created successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "❌ Test booking failed: " + response.code());
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error body: " + errorBody);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                            Toast.makeText(context, "Test booking failed: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Booking> call, Throwable t) {
                        Log.e(TAG, "❌ Test booking network error", t);
                        Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Test API connectivity
     */
    public static void testApiConnectivity(Context context) {
        com.evcs.app.utils.NetworkUtils.testApiConnection(context, new com.evcs.app.utils.NetworkUtils.ApiConnectionCallback() {
            @Override
            public void onResult(boolean success, String message) {
                Log.d(TAG, "API connectivity test: " + (success ? "✅ SUCCESS" : "❌ FAILED"));
                Log.d(TAG, "Message: " + message);
                Toast.makeText(context, 
                    (success ? "✅ API Connected: " : "❌ API Failed: ") + message, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}