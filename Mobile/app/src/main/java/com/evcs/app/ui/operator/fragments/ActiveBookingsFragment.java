package com.evcs.app.ui.operator.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evcs.app.R;
import com.evcs.app.utils.SharedPreferencesManager;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.ui.adapters.OperatorBookingAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveBookingsFragment extends Fragment implements OperatorBookingAdapter.OnOperatorBookingActionListener {

    private static final String TAG = "ActiveBookingsFragment";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OperatorBookingAdapter adapter;
    private SharedPreferencesManager prefsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPreferencesManager(requireContext());
        initializeViews(view);
        setupRecyclerView();
        loadStationBookings();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadStationBookings);
    }

    private void setupRecyclerView() {
        adapter = new OperatorBookingAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }



    private void loadStationBookings() {
        swipeRefreshLayout.setRefreshing(true);

        String token = prefsManager.getToken();
        if (token == null) {
            showError("Authentication error. Please login again.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        Call<List<Booking>> call = ApiClient.getInstance().getApiService().getStationBookings();

        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    Log.d(TAG, "Loaded " + bookings.size() + " station bookings");
                    adapter.updateBookings(bookings);
                } else {
                    Log.e(TAG, "Failed to load bookings: " + response.code());
                    showError("Failed to load station bookings");
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                if (getContext() == null) return;

                Log.e(TAG, "Error loading bookings", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onCancelBooking(Booking booking) {
        Call<Booking> call = ApiClient.getInstance().getApiService().cancelBooking(booking.getId());
        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (getContext() == null) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                    loadStationBookings();
                } else {
                    showError("Failed to cancel booking");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "Error cancelling booking", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onFinalizeBooking(Booking booking) {
        Map<String, Object> finalizeData = new HashMap<>();
        finalizeData.put("endTime", System.currentTimeMillis());
        finalizeData.put("energyDelivered", 0);

        Call<Booking> call = ApiClient.getInstance().getApiService().finalizeBooking(booking.getId(), finalizeData);
        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (getContext() == null) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Booking finalized", Toast.LENGTH_SHORT).show();
                    loadStationBookings();
                } else {
                    showError("Failed to finalize booking");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "Error finalizing booking", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // UPDATE: Implemented the new onApproveBooking method
    @Override
    public void onApproveBooking(Booking booking) { // <-- ADDED METHOD
        Call<Booking> call = ApiClient.getInstance().getApiService().approveBooking(booking.getId());
        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (getContext() == null) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Booking approved!", Toast.LENGTH_SHORT).show();
                    loadStationBookings(); // Refresh the list
                } else {
                    Log.e(TAG, "Failed to approve booking: " + response.code());
                    showError("Failed to approve booking");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "Error approving booking", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onViewDetails(Booking booking) {
        // Create custom dialog with improved UI
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        
        // Inflate custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_booking_details, null);
        builder.setView(dialogView);
        
        // Find views in the custom layout
        TextView bookingIdText = dialogView.findViewById(R.id.dialogBookingId);
        TextView customerNameText = dialogView.findViewById(R.id.dialogCustomerName);
        TextView stationNameText = dialogView.findViewById(R.id.dialogStationName);
        TextView statusText = dialogView.findViewById(R.id.dialogStatus);
        TextView startTimeText = dialogView.findViewById(R.id.dialogStartTime);
        TextView endTimeText = dialogView.findViewById(R.id.dialogEndTime);
        Button closeButton = dialogView.findViewById(R.id.dialogCloseButton);
        
        // Set booking data
        bookingIdText.setText(booking.getId() != null ? booking.getId() : "N/A");
        
        // Hide customer field as requested
        customerNameText.setVisibility(View.GONE);
        
        // Use StationNameResolver to get the station name from cache
        String stationName = "N/A";
        if (booking.getStationName() != null && !booking.getStationName().isEmpty()) {
            stationName = booking.getStationName();
        } else if (booking.getStationId() != null) {
            stationName = com.evcs.app.utils.StationNameResolver.getInstance().getStationName(booking.getStationId());
        }
        stationNameText.setText(stationName);
        
        // Set status with proper styling
        String status = booking.getStatus() != null ? booking.getStatus() : "unknown";
        statusText.setText(status.toUpperCase());
        
        // Apply status-specific background using runtime lookup and ensure white text
        String drawableName;
        switch (status.toLowerCase()) {
            case "pending":
                drawableName = "status_pending_bg";
                break;
            case "approved":
            case "confirmed":
                drawableName = "status_approved_bg";
                break;
            case "arrived":
            case "in_progress":
                drawableName = "status_in_progress_bg";
                break;
            case "completed":
                drawableName = "status_completed_bg";
                break;
            case "cancelled":
                drawableName = "status_cancelled_bg";
                break;
            default:
                drawableName = "status_pending_bg";
                break;
        }

        int bgRes = requireContext().getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
        if (bgRes != 0) {
            statusText.setBackgroundResource(bgRes);
        } else {
            statusText.setBackgroundColor(requireContext().getResources().getColor(android.R.color.darker_gray));
        }
        // Ensure the status text is white on colored backgrounds
        statusText.setTextColor(android.graphics.Color.WHITE);
        
        // Format and set times with timezone conversion (+5.5 hours for Sri Lanka)
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT+05:30' yyyy", Locale.getDefault());
        
        if (booking.getStartTime() != null) {
            long utcTime = booking.getStartTime().getTime();
            long sriLankaTime = utcTime + (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // +5.5 hours
            Date localStartTime = new Date(sriLankaTime);
            startTimeText.setText(timeFormat.format(localStartTime));
        } else {
            startTimeText.setText("N/A");
        }
        
        if (booking.getEndTime() != null) {
            long utcTime = booking.getEndTime().getTime();
            long sriLankaTime = utcTime + (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // +5.5 hours
            Date localEndTime = new Date(sriLankaTime);
            endTimeText.setText(timeFormat.format(localEndTime));
        } else {
            endTimeText.setText("N/A");
        }
        
        // Create and show dialog
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Set close button click listener
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStationBookings();
    }
}