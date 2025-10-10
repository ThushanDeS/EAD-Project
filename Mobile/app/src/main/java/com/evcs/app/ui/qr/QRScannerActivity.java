package com.evcs.app.ui.qr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.databinding.ActivityQrScannerBinding;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRScannerActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private ActivityQrScannerBinding binding;
    private DecoratedBarcodeView barcodeView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupToolbar();
        checkCameraPermission();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Scan QR Code");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            initializeScanner();
        } else {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    CAMERA_PERMISSION_REQUEST);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeScanner();
            } else {
                Toast.makeText(this, getString(R.string.permission_camera_required), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void initializeScanner() {
        barcodeView = binding.barcodeScanner;
        barcodeView.decodeContinuous(barcodeCallback);
        barcodeView.resume();
    }
    
    private final BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result != null && result.getText() != null) {
                String qrContent = result.getText();
                processQRCode(qrContent);
            }
        }
    };

    private void processQRCode(String qrCode) {
        // Pause scanner while processing
        barcodeView.pause();

        // Trim to avoid leading/trailing whitespace or newlines
        qrCode = qrCode.trim();

        // 🔹 Debug: log the scanned QR string
        Log.d("QRScanner", "Scanned QR raw: " + qrCode);

        // The QR code format is "booking:booking_id:station_id:timestamp"
        String[] parts = qrCode.split(":");

        // 🔹 Debug: log parts count and values
        Log.d("QRScanner", "Parts count: " + parts.length);
        for (int i = 0; i < parts.length; i++) {
            Log.d("QRScanner", "Part[" + i + "] = " + parts[i]);
        }

        // Validate expected format (4 parts)
        if (parts.length == 4 && parts[0].equalsIgnoreCase("booking")) {
            String bookingId = parts[1];
            String stationId = parts[2];
            String timestamp = parts[3];

            // 🔹 Debug: log extracted values
            Log.d("QRScanner", "BookingId: " + bookingId);
            Log.d("QRScanner", "StationId: " + stationId);
            Log.d("QRScanner", "Timestamp: " + timestamp);

            // Build request body with all needed data
            Map<String, String> qrRequest = new HashMap<>();
            qrRequest.put("booking_id", bookingId);
            qrRequest.put("station_id", stationId);
            qrRequest.put("timestamp", timestamp);

            // 🔹 Debug: log the request map
            Log.d("QRScanner", "Request map: " + qrRequest.toString());

            ApiClient.getInstance().getApiService().scanQrCode(qrRequest)
                    .enqueue(new Callback<Booking>() {
                        @Override
                        public void onResponse(Call<Booking> call, Response<Booking> response) {
                            Log.d("QRScanner", "API response code: " + response.code());
                            if (response.isSuccessful() && response.body() != null) {
                                Booking booking = response.body();
                                Log.d("QRScanner", "Booking object: " + booking.toString());

                                // Return booking details to calling activity
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("booking_id", booking.getId());
                                resultIntent.putExtra("station_id", booking.getStationId());
                                resultIntent.putExtra("slot_number", booking.getSlotNumber());
                                resultIntent.putExtra("status", booking.getStatus());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Log.w("QRScanner", "Invalid QR code or booking not found");
                                Toast.makeText(QRScannerActivity.this,
                                        "Invalid QR code or booking not found",
                                        Toast.LENGTH_SHORT).show();
                                barcodeView.resume();
                            }
                        }

                        @Override
                        public void onFailure(Call<Booking> call, Throwable t) {
                            Log.e("QRScanner", "API call failed", t);
                            Toast.makeText(QRScannerActivity.this,
                                    "Failed to process QR code: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            barcodeView.resume();
                        }
                    });

        } else {
            Log.w("QRScanner", "Invalid QR format");
            Toast.makeText(this, "Invalid QR format im chalana", Toast.LENGTH_SHORT).show();
            barcodeView.resume();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}