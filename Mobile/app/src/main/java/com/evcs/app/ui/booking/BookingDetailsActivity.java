package com.evcs.app.ui.booking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.utils.QRCodeUtils;
import com.evcs.app.utils.QRDebugUtils;
import com.evcs.app.utils.StationNameResolver;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailsActivity extends AppCompatActivity {
    private static final String TAG = "BookingDetailsActivity";
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_BOOKING_OBJECT = "booking_object";
    
    // Result codes
    public static final int RESULT_BOOKING_CANCELLED = 1001;
    public static final int RESULT_BOOKING_UPDATED = 1002;
    
    // UI Components
    private TextView textViewBookingId;
    private TextView textViewStationName;
    private TextView textViewSlotNumber;
    private TextView textViewStatus;
    private TextView textViewStartTime;
    private TextView textViewEndTime;
    private TextView textViewCreatedAt;
    private TextView textViewQRStatus;
    private ImageView imageViewQRCode;
    private View cardViewQRCode;
    private Button buttonCancelBooking;
    
    // Data
    private Booking currentBooking;
    private StationNameResolver stationNameResolver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        
        initializeViews();
        initializeData();
        loadBookingDetails();
    }
    
    private void initializeViews() {
        // Text views
        textViewBookingId = findViewById(R.id.textViewBookingId);
        textViewStationName = findViewById(R.id.textViewStationName);
        textViewSlotNumber = findViewById(R.id.textViewSlotNumber);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewStartTime = findViewById(R.id.textViewStartTime);
        textViewEndTime = findViewById(R.id.textViewEndTime);
        textViewCreatedAt = findViewById(R.id.textViewCreatedAt);
        textViewQRStatus = findViewById(R.id.textViewQRStatus);
        
        // Image view
        imageViewQRCode = findViewById(R.id.imageViewQRCode);
        
        // QR Code container
        cardViewQRCode = findViewById(R.id.cardViewQRCode);
        
        // Buttons
        buttonCancelBooking = findViewById(R.id.buttonCancelBooking);

        // Set click listeners
        if (buttonCancelBooking != null) {
            buttonCancelBooking.setOnClickListener(v -> cancelBooking());
        }
        
        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Booking Details");
        }
    }
    
    private void initializeData() {
        stationNameResolver = StationNameResolver.getInstance();
    }
    
    private void loadBookingDetails() {
        Log.d(TAG, "=== LOAD BOOKING DETAILS DEBUG ===");
        
        // Get booking data from intent
        currentBooking = (Booking) getIntent().getSerializableExtra(EXTRA_BOOKING_OBJECT);
        String bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        
        Log.d(TAG, "Current booking from intent: " + (currentBooking != null ? "NOT NULL" : "NULL"));
        Log.d(TAG, "Booking ID from intent: " + (bookingId != null ? bookingId : "NULL"));
        
        if (currentBooking != null) {
            Log.d(TAG, "Booking object details:");
            Log.d(TAG, "  - ID: " + currentBooking.getId());
            Log.d(TAG, "  - Status: " + currentBooking.getStatus());
            Log.d(TAG, "  - QrCode: " + (currentBooking.getQrCode() != null ? "NOT NULL (length=" + currentBooking.getQrCode().length() + ")" : "NULL"));
            Log.d(TAG, "  - QrImageBase64: " + (currentBooking.getQrImageBase64() != null ? "NOT NULL (length=" + currentBooking.getQrImageBase64().length() + ")" : "NULL"));
            Log.d(TAG, "  - QrImageContentType: " + currentBooking.getQrImageContentType());
        }
        
        // Prioritize fresh API data to ensure we have QR codes
        if (bookingId != null) {
            Log.d(TAG, "Booking ID provided, fetching fresh data from API: " + bookingId);
            loadBookingFromAPI(bookingId);
        } else if (currentBooking != null) {
            Log.d(TAG, "Using passed booking object (may not have QR data)");
            
            // Check if the booking has QR data
            if (currentBooking.getQrImageBase64() == null && currentBooking.getQrCode() == null) {
                Log.w(TAG, "Booking object missing QR data, should fetch from API: " + currentBooking.getId());
                // TODO: Fetch fresh data using currentBooking.getId()
            }
            
            displayBookingDetails(currentBooking);
        } else {
            Log.e(TAG, "No booking data provided");
            Toast.makeText(this, "Error: No booking data available", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        Log.d(TAG, "=== END LOAD BOOKING DETAILS DEBUG ===");
    }
    
    private void loadBookingFromAPI(String bookingId) {
        Log.d(TAG, "Loading booking from API: " + bookingId);
        textViewQRStatus.setText("Loading booking details...");
        
        // Always fetch fresh data to ensure we have QR codes
        Log.d(TAG, "Fetching fresh booking data from API to get QR codes");
        
        // TODO: Implement actual API call here
        // For now, we need to implement the API service call
        // This will ensure we get the latest booking data with QR codes
        
        Toast.makeText(this, "Loading fresh data from API...", Toast.LENGTH_SHORT).show();
        
        // Note: This should be implemented to call:
        // GET /api/v1/bookings/{bookingId} 
        // which returns the complete booking with QR data
    }
    
    private void displayBookingDetails(Booking booking) {
        Log.d(TAG, "=== DISPLAY BOOKING DETAILS DEBUG ===");
        Log.d(TAG, "Displaying booking details for: " + booking.getId());
        
        // Log all booking fields to see what we received
        Log.d(TAG, "Complete booking object debug:");
        Log.d(TAG, "  - ID: " + booking.getId());
        Log.d(TAG, "  - StationId: " + booking.getStationId());
        Log.d(TAG, "  - OwnerId: " + booking.getOwnerId());
        Log.d(TAG, "  - Status: " + booking.getStatus());
        Log.d(TAG, "  - SlotNumber: " + booking.getSlotNumber());
        Log.d(TAG, "  - StartTime: " + booking.getStartTime());
        Log.d(TAG, "  - EndTime: " + booking.getEndTime());
        Log.d(TAG, "  - CreatedBy: " + booking.getCreatedBy());
        Log.d(TAG, "  - CreatedAt: " + booking.getCreatedAt());
        Log.d(TAG, "  - QrCode: " + (booking.getQrCode() != null ? "NOT NULL (length=" + booking.getQrCode().length() + ")" : "NULL"));
        Log.d(TAG, "  - QrImageBase64: " + (booking.getQrImageBase64() != null ? "NOT NULL (length=" + booking.getQrImageBase64().length() + ")" : "NULL"));
        Log.d(TAG, "  - QrImageContentType: " + booking.getQrImageContentType());
        
        if (booking.getQrCode() != null && booking.getQrCode().length() > 10) {
            Log.d(TAG, "  - QrCode first 50 chars: " + booking.getQrCode().substring(0, Math.min(50, booking.getQrCode().length())));
        }
        
        if (booking.getQrImageBase64() != null && booking.getQrImageBase64().length() > 10) {
            Log.d(TAG, "  - QrImageBase64 first 50 chars: " + booking.getQrImageBase64().substring(0, Math.min(50, booking.getQrImageBase64().length())));
        }
        
        // Display basic information
        textViewBookingId.setText("Booking ID: " + booking.getId());
        textViewSlotNumber.setText("Slot " + booking.getSlotNumber());
        
        // Display status with color
        displayStatus(booking.getStatus());
        
        // Display booking times using local time (+5:30 hours) but apply reductions
        // as requested: start = start - 5 hours, end = end - 5.5 hours, createdAt = createdAt - 5.5 hours.
        // These adjustments are done before the existing local formatting.
        if (booking.getStartTime() != null) {
            long reduceStart = 5L * 60L * 60L * 1000L; // 5 hours in ms
            long adjustedStart = booking.getStartTime().getTime() - reduceStart;
            textViewStartTime.setText(com.evcs.app.utils.DateTimeUtils.formatDateTimeForLocalDisplaySimple(new java.util.Date(adjustedStart)));
        }

        if (booking.getEndTime() != null) {
            long reduceEnd = (5L * 60L + 30L) * 60L * 1000L; // 5.5 hours in ms
            long adjustedEnd = booking.getEndTime().getTime() - reduceEnd;
            textViewEndTime.setText(com.evcs.app.utils.DateTimeUtils.formatDateTimeForLocalDisplaySimple(new java.util.Date(adjustedEnd)));
        }

        if (booking.getCreatedAt() != null) {
            long reduceCreated = (5L * 60L + 30L) * 60L * 1000L; // 5.5 hours in ms
            long adjustedCreated = booking.getCreatedAt().getTime() - reduceCreated;
            textViewCreatedAt.setText(com.evcs.app.utils.DateTimeUtils.formatDateTimeForLocalDisplaySimple(new java.util.Date(adjustedCreated)));
        }
        
        // Load station name
        loadStationName(booking.getStationId());
        
        // Display QR code
        displayQRCode(booking);
        
        // Show/hide cancel button based on booking status
        updateCancelButtonVisibility(booking);
    }
    
    private void displayStatus(String status) {
        if (status == null) status = "unknown";
        
        textViewStatus.setText(status.toUpperCase());
        
        // Set status color
        int statusColor;
        switch (status.toLowerCase()) {
            case "pending":
                statusColor = ContextCompat.getColor(this, R.color.status_pending);
                break;
            case "approved":
                statusColor = ContextCompat.getColor(this, R.color.status_approved);
                break;
            case "in_progress":
                statusColor = ContextCompat.getColor(this, R.color.status_in_progress);
                break;
            case "completed":
                statusColor = ContextCompat.getColor(this, R.color.status_completed);
                break;
            case "cancelled":
                statusColor = ContextCompat.getColor(this, R.color.status_cancelled);
                break;
            default:
                statusColor = ContextCompat.getColor(this, R.color.text_secondary);
                break;
        }
        
        textViewStatus.setTextColor(statusColor);
    }
    
    private void loadStationName(String stationId) {
        if (stationId == null) {
            textViewStationName.setText("Unknown Station");
            return;
        }
        
        Log.d(TAG, "Loading station name for: " + stationId);
        textViewStationName.setText("Loading...");
        
        String stationName = stationNameResolver.getStationName(stationId);
        textViewStationName.setText(stationName);
        
        Log.d(TAG, "Station name resolved: " + stationName);
    }
    
    private void displayQRCode(Booking booking) {
        Log.d(TAG, "=== QR CODE DEBUG ===");
        
        if (booking == null) {
            Log.w(TAG, "Booking is null");
            textViewQRStatus.setText("No booking data");
            imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
            cardViewQRCode.setVisibility(View.GONE);
            return;
        }
        
        String status = booking.getStatus();
        Log.d(TAG, "Booking status: " + status);
        
        // Handle different booking statuses
        if (status == null || status.isEmpty()) {
            textViewQRStatus.setText("Unknown booking status");
            imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
            cardViewQRCode.setVisibility(View.GONE);
            return;
        }
        
        switch (status.toLowerCase()) {
            case "pending":
                textViewQRStatus.setText(getString(R.string.qr_status_pending));
                imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
                cardViewQRCode.setVisibility(View.GONE);
                Log.d(TAG, "Booking is pending - QR code container hidden");
                return;
                
            case "cancelled":
                textViewQRStatus.setText(getString(R.string.qr_status_cancelled));
                imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
                cardViewQRCode.setVisibility(View.GONE);
                Log.d(TAG, "Booking is cancelled - QR code container hidden");
                return;
                
            case "completed":
                textViewQRStatus.setText(getString(R.string.qr_status_completed));
                imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
                cardViewQRCode.setVisibility(View.GONE);
                Log.d(TAG, "Booking is completed - QR code container hidden");
                return;
                
            case "approved":
            case "in_progress":
                // Show QR code for approved and in-progress bookings
                cardViewQRCode.setVisibility(View.VISIBLE);
                Log.d(TAG, "Booking is " + status + " - showing QR code container");
                break;
                
            default:
                textViewQRStatus.setText(getString(R.string.qr_status_unknown_status, status));
                imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
                cardViewQRCode.setVisibility(View.GONE);
                Log.d(TAG, "Unknown status: " + status + " - QR code container hidden");
                return;
        }
        
        // Only reach here for approved/in_progress bookings
        String qrImageBase64 = booking.getQrImageBase64();
        String qrCodeData = booking.getQrCode();
        
        Log.d(TAG, "QR image base64 available: " + (qrImageBase64 != null && !qrImageBase64.isEmpty()));
        Log.d(TAG, "QR code data available: " + (qrCodeData != null && !qrCodeData.isEmpty()));
        
        if (qrImageBase64 != null && !qrImageBase64.isEmpty()) {
            Log.d(TAG, "Using QR image from backend (base64)");
            Log.d(TAG, "QR image base64 length: " + qrImageBase64.length());
            
            textViewQRStatus.setText(getString(R.string.qr_status_loading));
            
            // Use the pre-generated image from backend
            new Thread(() -> {
                Log.d(TAG, "Background thread started for base64 image decoding");
                Bitmap qrCodeBitmap = QRCodeUtils.createBitmapFromBase64(qrImageBase64);
                
                runOnUiThread(() -> {
                    if (qrCodeBitmap != null) {
                        Log.d(TAG, "QR code loaded successfully from base64");
                        Log.d(TAG, "Bitmap dimensions: " + qrCodeBitmap.getWidth() + "x" + qrCodeBitmap.getHeight());
                        imageViewQRCode.setImageBitmap(qrCodeBitmap);
                        textViewQRStatus.setText(getString(R.string.qr_status_ready));
                        Log.d(TAG, "QR code displayed successfully");
                    } else {
                        Log.e(TAG, "Failed to decode base64 image, falling back to text generation");
                        generateQRFromText(qrCodeData);
                    }
                });
            }).start();
            
        } else if (qrCodeData != null && !qrCodeData.isEmpty()) {
            Log.d(TAG, "No base64 image available, generating QR from text");
            generateQRFromText(qrCodeData);
            
        } else {
            Log.w(TAG, "No QR data available for approved booking");
            textViewQRStatus.setText(getString(R.string.qr_status_not_available));
            imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
        }
        
        Log.d(TAG, "=== END QR DEBUG ===");
    }
    
    private void generateQRFromText(String qrCodeData) {
        Log.d(TAG, "Generating QR code from text data");
        Log.d(TAG, "QR code data: " + (qrCodeData != null ? qrCodeData : "NULL"));
        Log.d(TAG, "QR code data length: " + (qrCodeData != null ? qrCodeData.length() : 0));
        
        // Run detailed analysis
        QRDebugUtils.analyzeQRData(qrCodeData);
        
        if (qrCodeData != null && qrCodeData.length() > 0) {
            Log.d(TAG, "First 50 chars: " + qrCodeData.substring(0, Math.min(50, qrCodeData.length())));
        }
        
        if (!QRCodeUtils.isValidQRData(qrCodeData)) {
            Log.w(TAG, "QR code data is invalid");
            textViewQRStatus.setText("No QR code available");
            imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
            return;
        }
        
        textViewQRStatus.setText("Generating QR code...");
        Log.d(TAG, "Starting QR code generation...");
        
        // Generate QR code in background thread
        new Thread(() -> {
            Log.d(TAG, "Background thread started for QR generation");
            Bitmap qrCodeBitmap = QRCodeUtils.generateQRCode(qrCodeData, 400);
            
            runOnUiThread(() -> {
                if (qrCodeBitmap != null) {
                    Log.d(TAG, "QR bitmap generated successfully, setting to ImageView");
                    Log.d(TAG, "Bitmap dimensions: " + qrCodeBitmap.getWidth() + "x" + qrCodeBitmap.getHeight());
                    imageViewQRCode.setImageBitmap(qrCodeBitmap);
                    textViewQRStatus.setText("QR Code ready");
                    Log.d(TAG, "QR code displayed successfully");
                } else {
                    Log.e(TAG, "QR bitmap is null, showing placeholder");
                    imageViewQRCode.setImageResource(R.drawable.qr_code_placeholder);
                    textViewQRStatus.setText("Failed to generate QR code");
                }
            });
        }).start();
        
        // Show decoded QR data for debugging
        String decodedText = QRCodeUtils.getDecodedQRText(qrCodeData);
        Log.d(TAG, "QR Code decoded text: " + decodedText);
    }
    
    private void updateCancelButtonVisibility(Booking booking) {
        if (booking != null && booking.canBeCancelled()) {
            buttonCancelBooking.setVisibility(View.VISIBLE);
        } else {
            buttonCancelBooking.setVisibility(View.GONE);
        }
    }
    
    // Refresh QR functionality removed (UI button was removed). If needed, reintroduce later.
    
    private void cancelBooking() {
        if (currentBooking == null) {
            Toast.makeText(this, getString(R.string.cancel_booking_no_booking), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!currentBooking.canBeCancelled()) {
            Toast.makeText(this, getString(R.string.cancel_booking_cannot_cancel), Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Cancelling booking: " + currentBooking.getId());
        
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.cancel_booking_title))
                .setMessage(getString(R.string.cancel_booking_message))
                .setPositiveButton(getString(R.string.cancel_booking_confirm), (dialog, which) -> {
                    performCancellation();
                })
                .setNegativeButton(getString(R.string.cancel_booking_no), (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    private void performCancellation() {
        Log.d(TAG, "=== BOOKING CANCELLATION START ===");
        Log.d(TAG, "Cancelling booking ID: " + currentBooking.getId());
        
        // Disable cancel button to prevent multiple clicks
        buttonCancelBooking.setEnabled(false);
        buttonCancelBooking.setText(getString(R.string.cancel_booking_processing));
        
        ApiClient.getInstance().getApiService()
                .cancelBooking(currentBooking.getId())
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(Call<Booking> call, Response<Booking> response) {
                        Log.d(TAG, "=== CANCEL API RESPONSE ===");
                        Log.d(TAG, "Response successful: " + response.isSuccessful());
                        Log.d(TAG, "Response code: " + response.code());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            Booking cancelledBooking = response.body();
                            Log.d(TAG, "Booking cancelled successfully");
                            Log.d(TAG, "New status: " + cancelledBooking.getStatus());
                            
                            runOnUiThread(() -> {
                                // Update current booking
                                currentBooking = cancelledBooking;
                                
                                // Update UI to reflect cancellation
                                displayBookingDetails(currentBooking);
                                
                                // Set result to notify parent that booking was cancelled
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(EXTRA_BOOKING_OBJECT, currentBooking);
                                setResult(RESULT_BOOKING_CANCELLED, resultIntent);
                                
                                // Show success message
                                Toast.makeText(BookingDetailsActivity.this, 
                                    getString(R.string.cancel_booking_success), 
                                    Toast.LENGTH_LONG).show();
                                
                                Log.d(TAG, "UI updated after cancellation");
                                
                                // Optionally finish activity after a delay
                                new android.os.Handler().postDelayed(() -> {
                                    finish();
                                }, 2000); // 2 second delay to show the success message
                            });
                            
                        } else {
                            Log.e(TAG, "Failed to cancel booking: " + response.code());
                            Log.e(TAG, "Response message: " + response.message());
                            
                            runOnUiThread(() -> {
                                // Re-enable cancel button
                                buttonCancelBooking.setEnabled(true);
                                buttonCancelBooking.setText(getString(R.string.button_cancel_booking));
                                
                                // Show error message
                                String errorMessage = getString(R.string.cancel_booking_error);
                                if (response.code() == 409) {
                                    errorMessage = getString(R.string.cancel_booking_cutoff_error);
                                } else if (response.code() == 404) {
                                    errorMessage = getString(R.string.cancel_booking_not_found);
                                }
                                
                                Toast.makeText(BookingDetailsActivity.this, 
                                    errorMessage, 
                                    Toast.LENGTH_LONG).show();
                            });
                        }
                        
                        Log.d(TAG, "=== END CANCEL API RESPONSE ===");
                    }
                    
                    @Override
                    public void onFailure(Call<Booking> call, Throwable t) {
                        Log.e(TAG, "=== CANCEL API FAILURE ===");
                        Log.e(TAG, "API call failed", t);
                        Log.e(TAG, "Error message: " + t.getMessage());
                        
                        runOnUiThread(() -> {
                            // Re-enable cancel button
                            buttonCancelBooking.setEnabled(true);
                            buttonCancelBooking.setText(getString(R.string.button_cancel_booking));
                            
                            // Show error message
                            Toast.makeText(BookingDetailsActivity.this, 
                                getString(R.string.msg_network_error), 
                                Toast.LENGTH_LONG).show();
                        });
                        
                        Log.d(TAG, "=== END CANCEL API FAILURE ===");
                    }
                });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    /**
     * Helper method to start this activity with booking object
     */
    public static void startWithBooking(android.content.Context context, Booking booking) {
        Intent intent = new Intent(context, BookingDetailsActivity.class);
        intent.putExtra(EXTRA_BOOKING_OBJECT, booking);
        context.startActivity(intent);
    }
    
    /**
     * Helper method to start this activity with booking object for result
     */
    public static void startWithBookingForResult(androidx.fragment.app.Fragment fragment, Booking booking, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), BookingDetailsActivity.class);
        intent.putExtra(EXTRA_BOOKING_OBJECT, booking);
        fragment.startActivityForResult(intent, requestCode);
    }
    
    /**
     * Helper method to start this activity with booking object for result (from Activity)
     */
    public static void startWithBookingForResult(android.app.Activity activity, Booking booking, int requestCode) {
        Intent intent = new Intent(activity, BookingDetailsActivity.class);
        intent.putExtra(EXTRA_BOOKING_OBJECT, booking);
        activity.startActivityForResult(intent, requestCode);
    }
    
    /**
     * Helper method to start this activity with booking ID
     */
    public static void startWithBookingId(android.content.Context context, String bookingId) {
        Intent intent = new Intent(context, BookingDetailsActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, bookingId);
        context.startActivity(intent);
    }
}