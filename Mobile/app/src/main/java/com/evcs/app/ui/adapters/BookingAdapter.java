package com.evcs.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookings;
    private boolean showActions;
    private OnBookingClickListener listener;
    
    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }
    
    public BookingAdapter(List<Booking> bookings, boolean showActions) {
        this.bookings = bookings;
        this.showActions = showActions;
    }
    
    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }
    
    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }
    
    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }
    
    class BookingViewHolder extends RecyclerView.ViewHolder {
    TextView textViewStatus;
    TextView textViewSlot;
    TextView textViewStationName;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewSlot = itemView.findViewById(R.id.textViewSlot);
            textViewStationName = itemView.findViewById(R.id.textViewStationName);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(bookings.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Booking booking) {
            // Set station name (moved to meta row). Keep a simple fallback.
            String stationNameDisplay = "Unknown";
            if (booking.getStationName() != null && !booking.getStationName().isEmpty()) {
                stationNameDisplay = booking.getStationName();
            } else if (booking.getStationId() != null) {
                String resolved = com.evcs.app.utils.StationNameResolver.getInstance().getStationName(booking.getStationId());
                if (resolved != null && !resolved.isEmpty()) {
                    stationNameDisplay = resolved;
                }
            }
                textViewStationName.setText(stationNameDisplay);
                textViewStatus.setText(booking.getStatus().toUpperCase());
                textViewSlot.setText("Slot: " + booking.getSlotNumber());

                // Format date time - Manual timezone conversion: Add 5.5 hours to UTC time
                if (booking.getStartTime() != null) {
                    long utcTime = booking.getStartTime().getTime();
                    long sriLankaTimeOffset = (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // 5.5 hours in milliseconds
                    long localTime = utcTime + sriLankaTimeOffset;
                    Date localDate = new Date(localTime);

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    TextView tvDate = itemView.findViewById(R.id.textViewDateTime);
                    if (tvDate != null) {
                        tvDate.setText(sdf.format(localDate));
                    }
                }
            
            // Set status background drawable and ensure text is white
            String status = booking.getStatus() != null ? booking.getStatus().toLowerCase() : "";
            // Resolve drawable name by status and lookup resource id at runtime.
            String drawableName;
            switch (status) {
                case "approved":
                    drawableName = "status_approved_bg";
                    break;
                case "pending":
                    drawableName = "status_pending_bg";
                    break;
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
                textViewStatus.setBackgroundResource(bgRes);
            } else {
                // Fallback: simple gray background if drawable not found for any reason
                textViewStatus.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            }

            textViewStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
        }
    }
}