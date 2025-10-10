package com.evcs.app.ui.owner.fragments;

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

public class ActiveBookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {
    
    private static final String TAG = "ActiveBookingsFragment";
    
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
        loadActiveBookings();
    }
    
    private void setupViews() {
    binding.titleText.setText("Active Bookings");
        
        // Setup RecyclerView
        adapter = new BookingAdapter(new ArrayList<>(), true);
        adapter.setOnBookingClickListener(this);
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewBookings.setAdapter(adapter);
        
        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadActiveBookings);
    }
    
    private void loadActiveBookings() {
        binding.swipeRefreshLayout.setRefreshing(true);
        
        ApiClient.getInstance().getApiService()
                .getMyBookings("in_progress,arrived", null)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> activeBookings = response.body();
                            Log.d(TAG, "Loaded " + activeBookings.size() + " active bookings");
                            
                            if (activeBookings.isEmpty()) {
                                showEmptyState();
                            } else {
                                showBookings(activeBookings);
                            }
                        } else {
                            Log.e(TAG, "Failed to load active bookings: " + response.code());
                            showError("Failed to load active bookings");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        if (binding != null) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                        Log.e(TAG, "Error loading active bookings", t);
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
            binding.emptyStateText.setText("No active bookings found");
            binding.emptyStateSubtext.setText("Your active charging sessions will appear here");
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
        loadActiveBookings();
    }
    
    @Override
    public void onBookingClick(Booking booking) {
        Log.d(TAG, "Booking clicked: " + booking.getId());
        
        // Navigate to booking details activity
        com.evcs.app.ui.booking.BookingDetailsActivity.startWithBooking(getContext(), booking);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}