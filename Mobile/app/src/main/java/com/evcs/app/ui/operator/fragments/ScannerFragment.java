package com.evcs.app.ui.operator.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.evcs.app.R;
import com.evcs.app.utils.SharedPreferencesManager;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.remote.ApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScannerFragment extends Fragment {
    
    private static final String TAG = "ScannerFragment";
    private static final int CAMERA_PERMISSION_REQUEST = 200;
    
    private LinearLayout scanButton;
    private SharedPreferencesManager prefsManager;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefsManager = new SharedPreferencesManager(requireContext());
        initializeViews(view);
        setupClickListeners();
    }
    
    private void initializeViews(View view) {
        scanButton = view.findViewById(R.id.scanButton);
    }
    
    private void setupClickListeners() {
        scanButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startQrScan();
            } else {
                requestCameraPermission();
            }
        });
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }
    
    private void startQrScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                processQrResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void processQrResult(String qrContent) {
        Log.d(TAG, "Processing QR result: " + qrContent);
        
        Map<String, String> qrRequest = new HashMap<>();
        qrRequest.put("Qr", qrContent);
        
        String token = prefsManager.getToken();
        if (token == null) {
            showError("Authentication error. Please login again.");
            return;
        }
        
        Call<Booking> call = ApiClient.getInstance().getApiService().scanQrCode(qrRequest);
        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    Booking booking = response.body();
                    showBookingDetails(booking);
                } else {
                    String error = "Failed to verify booking";
                    if (response.code() == 404) {
                        error = "Booking not found or invalid QR code";
                    } else if (response.code() == 400) {
                        error = "Invalid QR code format";
                    }
                    showError(error);
                }
            }
            
            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "QR verification failed", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showBookingDetails(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Booking Verified");
        
        // Build details without showing customer information
        String stationLine = "";
        if (booking.getStationName() != null && !booking.getStationName().isEmpty() && !booking.getStationName().equals("Unknown")) {
            stationLine = "Station: " + booking.getStationName() + "\n";
        } else if (booking.getStationId() != null && !booking.getStationId().isEmpty()) {
            String resolved = com.evcs.app.utils.StationNameResolver.getInstance().getStationName(booking.getStationId());
            if (resolved != null && !resolved.isEmpty() && !resolved.equals("Unknown")) {
                stationLine = "Station: " + resolved + "\n";
            }
        }

        String details = String.format(
                "Booking ID: %s\n%sStatus: %s\nTime: %s - %s",
                booking.getId(),
                stationLine,
                booking.getStatus(),
                booking.getStartTime(),
                booking.getEndTime()
        );
        
        builder.setMessage(details);
        builder.setPositiveButton("Confirm Arrival", (dialog, which) -> {
            confirmBookingArrival(booking.getId());
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }
    
    private void confirmBookingArrival(String bookingId) {
        Call<Booking> call = ApiClient.getInstance().getApiService().confirmArrival(bookingId);
        call.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Arrival confirmed successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to confirm arrival");
                }
            }
            
            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                if (getContext() == null) return;
                Log.e(TAG, "Confirm arrival failed", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrScan();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
            }
        }
    }
}