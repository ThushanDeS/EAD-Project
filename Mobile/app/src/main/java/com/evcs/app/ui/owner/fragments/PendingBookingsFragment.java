package com.evcs.app.ui.owner.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.databinding.FragmentBookingListBinding;
import com.evcs.app.ui.adapters.BookingAdapter;
import com.evcs.app.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingBookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {
    
    private static final String TAG = "PendingBookingsFragment";
    private static final int REQUEST_BOOKING_DETAILS = 1001;
    
    private FragmentBookingListBinding binding;
    private BookingAdapter adapter;
    private SharedPreferencesManager preferencesManager;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferencesManager = new SharedPreferencesManager(requireContext());
        setupViews();
        loadPendingBookings();
    }
    
    private void setupViews() {
    binding.titleText.setText("Pending Bookings");
        
        // Setup RecyclerView
        adapter = new BookingAdapter(new ArrayList<>(), true);
        adapter.setOnBookingClickListener(this);
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewBookings.setAdapter(adapter);
        
        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadPendingBookings);
    }
    
    private void loadPendingBookings() {
        binding.swipeRefreshLayout.setRefreshing(true);
        
        ApiClient.getInstance().getApiService()
                .getMyBookings("pending", null)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        Log.d(TAG, "=== API RESPONSE RECEIVED ===");
                        Log.d(TAG, "Response successful: " + response.isSuccessful());
                        Log.d(TAG, "Response code: " + response.code());
                        Log.d(TAG, "Response body not null: " + (response.body() != null));
                        
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> pendingBookings = response.body();
                            Log.d(TAG, "=== API RESPONSE DEBUG ===");
                            Log.d(TAG, "Loaded " + pendingBookings.size() + " pending bookings");
                            
                            // Log details of each booking from API
                            for (int i = 0; i < Math.min(pendingBookings.size(), 3); i++) {
                                Booking booking = pendingBookings.get(i);
                                Log.d(TAG, "Booking " + (i+1) + " from API:");
                                Log.d(TAG, "  - ID: " + booking.getId());
                                Log.d(TAG, "  - Status: " + booking.getStatus());
                                Log.d(TAG, "  - QrCode: " + (booking.getQrCode() != null ? "NOT NULL (length=" + booking.getQrCode().length() + ")" : "NULL"));
                                Log.d(TAG, "  - QrImageBase64: " + (booking.getQrImageBase64() != null ? "NOT NULL (length=" + booking.getQrImageBase64().length() + ")" : "NULL"));
                                Log.d(TAG, "  - QrImageContentType: " + booking.getQrImageContentType());
                            }
                            Log.d(TAG, "=== END API RESPONSE DEBUG ===");
                            
                            if (pendingBookings.isEmpty()) {
                                showEmptyState();
                            } else {
                                // Resolve station names before showing bookings
                                com.evcs.app.utils.BookingUtils.resolveStationNamesWithCallback(
                                    pendingBookings, 
                                    getActivity(),
                                    new com.evcs.app.utils.BookingUtils.BookingStationResolvedCallback() {
                                        @Override
                                        public void onStationsResolved(List<Booking> bookingsWithStationNames) {
                                            showBookings(bookingsWithStationNames);
                                        }
                                        
                                        @Override
                                        public void onError(String error, List<Booking> originalBookings) {
                                            // Show bookings anyway, even without station names
                                            showBookings(originalBookings);
                                        }
                                    }
                                );
                            }
                        } else {
                            Log.e(TAG, "=== API RESPONSE ERROR ===");
                            Log.e(TAG, "Failed to load pending bookings: " + response.code());
                            Log.e(TAG, "Response message: " + response.message());
                            Log.e(TAG, "Response body is null: " + (response.body() == null));
                            Log.e(TAG, "=== END API RESPONSE ERROR ===");
                            showError("Failed to load pending bookings");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e(TAG, "=== API CALL FAILURE ===");
                        Log.e(TAG, "API call failed", t);
                        Log.e(TAG, "Error message: " + t.getMessage());
                        Log.e(TAG, "=== END API CALL FAILURE ===");
                        
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                        Log.e(TAG, "Error loading pending bookings", t);
                        showError("Network error: " + t.getMessage());
                    }
                });
    }
    
    private void showBookings(List<Booking> bookings) {
        if (binding != null) {
            binding.recyclerViewBookings.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
            adapter.updateBookings(bookings);
        }
    }
    
    private void showEmptyState() {
        if (binding != null) {
            binding.recyclerViewBookings.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.emptyStateText.setText("No pending bookings found");
            binding.emptyStateSubtext.setText("Your pending reservations will appear here");
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadPendingBookings();
    }
    
    @Override
    public void onBookingClick(Booking booking) {
        Log.d(TAG, "=== PENDING BOOKING CLICK DEBUG ===");
        Log.d(TAG, "Booking clicked: " + booking.getId());
        
        // Log the booking data before passing it
        Log.d(TAG, "Booking data being passed to details:");
        Log.d(TAG, "  - ID: " + booking.getId());
        Log.d(TAG, "  - Status: " + booking.getStatus());
        Log.d(TAG, "  - QrCode: " + (booking.getQrCode() != null ? "NOT NULL (length=" + booking.getQrCode().length() + ")" : "NULL"));
        Log.d(TAG, "  - QrImageBase64: " + (booking.getQrImageBase64() != null ? "NOT NULL (length=" + booking.getQrImageBase64().length() + ")" : "NULL"));
        Log.d(TAG, "  - QrImageContentType: " + booking.getQrImageContentType());
        
        if (booking.getQrCode() != null && booking.getQrCode().length() > 0) {
            Log.d(TAG, "  - QrCode preview: " + booking.getQrCode().substring(0, Math.min(30, booking.getQrCode().length())) + "...");
        }
        
        if (booking.getQrImageBase64() != null && booking.getQrImageBase64().length() > 0) {
            Log.d(TAG, "  - QrImageBase64 preview: " + booking.getQrImageBase64().substring(0, Math.min(30, booking.getQrImageBase64().length())) + "...");
        }
        
        Log.d(TAG, "Calling startWithBookingForResult...");
        
        // Navigate to booking details activity with result callback
        com.evcs.app.ui.booking.BookingDetailsActivity.startWithBookingForResult(this, booking, REQUEST_BOOKING_DETAILS);
        
        Log.d(TAG, "=== END PENDING BOOKING CLICK DEBUG ===");
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "=== ACTIVITY RESULT RECEIVED ===");
        Log.d(TAG, "Request code: " + requestCode);
        Log.d(TAG, "Result code: " + resultCode);
        
        if (requestCode == REQUEST_BOOKING_DETAILS) {
            if (resultCode == com.evcs.app.ui.booking.BookingDetailsActivity.RESULT_BOOKING_CANCELLED) {
                Log.d(TAG, "Booking was cancelled, refreshing list");
                // Refresh the pending bookings list
                loadPendingBookings();
                
                // Show a toast message
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Booking list updated", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == com.evcs.app.ui.booking.BookingDetailsActivity.RESULT_BOOKING_UPDATED) {
                Log.d(TAG, "Booking was updated, refreshing list");
                // Refresh the pending bookings list
                loadPendingBookings();
            }
        }
        
        Log.d(TAG, "=== END ACTIVITY RESULT ===");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}