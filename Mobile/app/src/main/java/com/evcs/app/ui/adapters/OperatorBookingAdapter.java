package com.evcs.app.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.evcs.app.R;
import com.evcs.app.data.models.Booking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OperatorBookingAdapter extends RecyclerView.Adapter<OperatorBookingAdapter.ViewHolder> {

    private List<Booking> bookings;
    private List<Booking> bookingsFiltered; // For search functionality
    private OnOperatorBookingActionListener listener;

    // UPDATE: Added onApproveBooking to the interface
    public interface OnOperatorBookingActionListener {
        void onCancelBooking(Booking booking);
        void onFinalizeBooking(Booking booking);
        void onViewDetails(Booking booking);
        void onApproveBooking(Booking booking); // <-- ADDED
    }

    public OperatorBookingAdapter(List<Booking> bookings, OnOperatorBookingActionListener listener) {
        this.bookings = bookings;
        this.bookingsFiltered = new ArrayList<>(bookings); // Initialize filtered list
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operator_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingsFiltered.get(position); // Use filtered list
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookingsFiltered.size(); // Use filtered list size
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        this.bookingsFiltered = new ArrayList<>(newBookings); // Reset filtered list
        notifyDataSetChanged();
    }

    // Method to filter bookings by owner name
    public void filter(String query) {
        bookingsFiltered.clear();
        
        if (query == null || query.trim().isEmpty()) {
            // If query is empty, show all bookings
            bookingsFiltered.addAll(bookings);
        } else {
            // Filter bookings based on owner name
            String queryLower = query.toLowerCase().trim();
            for (Booking booking : bookings) {
                boolean matches = false;
                
                // Search in owner name
                if (booking.getOwnerName() != null && 
                    booking.getOwnerName().toLowerCase().contains(queryLower)) {
                    matches = true;
                }
                
                // Search in owner NIC as fallback
                if (!matches && booking.getOwnerNic() != null && 
                    booking.getOwnerNic().toLowerCase().contains(queryLower)) {
                    matches = true;
                }
                
                // Search in booking ID as additional option
                if (!matches && booking.getId() != null && 
                    booking.getId().toLowerCase().contains(queryLower)) {
                    matches = true;
                }
                
                if (matches) {
                    bookingsFiltered.add(booking);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView customerNameText, stationNameText, statusText, timeText;
        // UPDATE: Removed bookingIdText, Added approveButton
        private Button cancelButton, finalizeButton, viewDetailsButton, approveButton; // <-- EDITED

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            stationNameText = itemView.findViewById(R.id.stationNameText);
            statusText = itemView.findViewById(R.id.statusText);
            timeText = itemView.findViewById(R.id.timeText);

            // Reuse confirmArrivalButton as cancel button
            cancelButton = itemView.findViewById(R.id.confirmArrivalButton);
            finalizeButton = itemView.findViewById(R.id.finalizeButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            // UPDATE: Find the new approve button by its ID
            approveButton = itemView.findViewById(R.id.approveButton); // <-- ADDED
        }

        public void bind(Booking booking, OnOperatorBookingActionListener listener) {
            // Debug logging
            android.util.Log.d("OperatorBookingAdapter", "Binding booking " + booking.getId());
            android.util.Log.d("OperatorBookingAdapter", "  - OwnerName: " + booking.getOwnerName());
            android.util.Log.d("OperatorBookingAdapter", "  - OwnerNic: " + booking.getOwnerNic());
            android.util.Log.d("OperatorBookingAdapter", "  - OwnerId: " + booking.getOwnerId());
            
            // Display owner name if available, otherwise show owner ID
            // Hide customer display as requested
            customerNameText.setVisibility(View.GONE);
            
            // Use StationNameResolver for better station name display
            String stationDisplay = "N/A";
            if (booking.getStationName() != null && !booking.getStationName().isEmpty()) {
                stationDisplay = booking.getStationName();
            } else if (booking.getStationId() != null) {
                stationDisplay = com.evcs.app.utils.StationNameResolver.getInstance().getStationName(booking.getStationId());
            }
            stationNameText.setText("Station: " + stationDisplay);
            
            // Format start and end times properly - Manual timezone conversion: Add 5.5 hours to UTC time
            SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault());
            
            String startTimeStr = "N/A";
            String endTimeStr = "N/A";
            
            if (booking.getStartTime() != null) {
                long utcTime = booking.getStartTime().getTime();
                long sriLankaTimeOffset = (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // 5.5 hours in milliseconds
                long localTime = utcTime + sriLankaTimeOffset;
                Date localDate = new Date(localTime);
                startTimeStr = timeFormat.format(localDate);
            }
            
            if (booking.getEndTime() != null) {
                long utcTime = booking.getEndTime().getTime();
                long sriLankaTimeOffset = (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // 5.5 hours in milliseconds
                long localTime = utcTime + sriLankaTimeOffset;
                Date localDate = new Date(localTime);
                endTimeStr = timeFormat.format(localDate);
            }
            
            timeText.setText(startTimeStr + " - " + endTimeStr);
            
            statusText.setText(booking.getStatus().toUpperCase());
            
            // Set status background and text color based on status using runtime lookup
            String status = booking.getStatus().toLowerCase();
            String drawableName;
            switch (status) {
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

            int bgRes = itemView.getContext().getResources().getIdentifier(
                    drawableName, "drawable", itemView.getContext().getPackageName());
            if (bgRes != 0) {
                statusText.setBackgroundResource(bgRes);
            } else {
                statusText.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            }
            statusText.setTextColor(Color.WHITE);

            // UPDATE: Added visibility logic for the approve button
            if (status.equals("pending")) { // <-- ADDED BLOCK
                approveButton.setVisibility(View.VISIBLE);
            } else {
                approveButton.setVisibility(View.GONE);
            }

            // Cancel button visible for all
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText("Cancel");

            // Finalize button visible only for certain statuses
            if (status.equals("arrived") || status.equals("in_progress")) {
                finalizeButton.setVisibility(View.VISIBLE);
            } else {
                finalizeButton.setVisibility(View.GONE);
            }

            // Click listeners
            // UPDATE: Added click listener for the approve button
            approveButton.setOnClickListener(v -> { // <-- ADDED BLOCK
                if (listener != null) {
                    listener.onApproveBooking(booking);
                }
            });

            cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking);
                }
            });

            finalizeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFinalizeBooking(booking);
                }
            });

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(booking);
                }
            });
        }
    }
}