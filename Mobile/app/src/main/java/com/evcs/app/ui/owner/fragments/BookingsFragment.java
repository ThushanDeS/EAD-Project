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
import androidx.recyclerview.widget.RecyclerView;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.ui.adapters.BookingAdapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {
    
    private RecyclerView recyclerViewBookings;
    private BookingAdapter bookingAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadBookings();
    }
    
    private void loadBookings() {
        ApiClient.getInstance().getApiService()
                .getMyBookings(null, null) // Get all bookings
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> bookings = response.body();
                            Log.d("BookingsFragment", "Loaded " + bookings.size() + " bookings");
                            
                            // Resolve station names for better display
                            com.evcs.app.utils.StationNameResolver.getInstance()
                                    .resolveStationNames(bookings, new com.evcs.app.utils.StationNameResolver.StationResolverCallback() {
                                        @Override
                                        public void onStationsResolved(List<Booking> bookingsWithStationNames) {
                                            // Update UI on main thread
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    if (bookingAdapter == null) {
                                                        bookingAdapter = new BookingAdapter(bookingsWithStationNames, true);
                                                        bookingAdapter.setOnBookingClickListener(BookingsFragment.this);
                                                        recyclerViewBookings.setAdapter(bookingAdapter);
                                                    } else {
                                                        bookingAdapter.updateBookings(bookingsWithStationNames);
                                                    }
                                                });
                                            }
                                        }
                                        
                                        @Override
                                        public void onError(String error) {
                                            Log.w("BookingsFragment", "Station name resolution error: " + error);
                                            // Still show bookings even if station names couldn't be resolved
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    if (bookingAdapter == null) {
                                                        bookingAdapter = new BookingAdapter(bookings, true);
                                                        bookingAdapter.setOnBookingClickListener(BookingsFragment.this);
                                                        recyclerViewBookings.setAdapter(bookingAdapter);
                                                    } else {
                                                        bookingAdapter.updateBookings(bookings);
                                                    }
                                                });
                                            }
                                        }
                                    });
                        } else {
                            Log.e("BookingsFragment", "Failed to load bookings: " + response.code());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e("BookingsFragment", "Error loading bookings", t);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    @Override
    public void onBookingClick(Booking booking) {
        Log.d("BookingsFragment", "Booking clicked: " + booking.getId());
        
        // Navigate to booking details activity
        com.evcs.app.ui.booking.BookingDetailsActivity.startWithBooking(getContext(), booking);
    }
}