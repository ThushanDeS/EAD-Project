package com.evcs.app.ui.debug;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.ui.booking.BookingDetailsActivity;
import com.evcs.app.utils.QRDebugUtils;
import java.util.Date;

public class QRTestActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_test);
        
        Button buttonTestQR = findViewById(R.id.buttonTestQR);
        Button buttonDebugQR = findViewById(R.id.buttonDebugQR);
        
        buttonTestQR.setOnClickListener(v -> testQRCode());
        buttonDebugQR.setOnClickListener(v -> debugQRGeneration());
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("QR Code Test");
        }
    }
    
    private void testQRCode() {
        Log.d("QRTest", "=== Starting QR Test ===");
        
        // Create a sample booking with QR code (using the example from user)
        Booking testBooking = new Booking();
        testBooking.setId("68dbb3d6736e8ffa240715d3");
        testBooking.setStationId("68d63aae375b419bf2ff194e"); // Fort Station
        testBooking.setSlotNumber(7);
        testBooking.setStatus("pending");
        
        // Test the base64 QR code from backend
        String base64QRCode = "Ym9va2luZzo2OGRiYjNkNjczNmU4ZmZhMjQwNzE1ZDM6NjhkNjNhYWUzNzViNDE5YmYyZmYxOTRlOjIwMjUtMTAtMDFUMTk6MDA6MDAuMDAwMDAwMFo=";
        testBooking.setQrCode(base64QRCode);
        
        Log.d("QRTest", "Test booking created with QR code: " + base64QRCode);
        Log.d("QRTest", "QR code length: " + base64QRCode.length());
        
        // Set dates for testing
        Date now = new Date();
        testBooking.setCreatedAt(now);
        testBooking.setStartTime(new Date(now.getTime() + 3600000)); // 1 hour from now
        testBooking.setEndTime(new Date(now.getTime() + 5400000)); // 1.5 hours from now
        
        Toast.makeText(this, "Opening booking details with sample QR code", Toast.LENGTH_SHORT).show();
        
        // Open booking details
        BookingDetailsActivity.startWithBooking(this, testBooking);
    }
    
    private void debugQRGeneration() {
        Log.d("QRTest", "Running QR generation debug tests...");
        Toast.makeText(this, "Running QR debug tests - check logcat", Toast.LENGTH_LONG).show();
        
        // Run comprehensive QR tests
        QRDebugUtils.testQRGeneration();
        
        // Also test the decoded text directly
        String decodedBookingData = "booking:68dbb3d6736e8ffa240715d3:68d63aae375b419bf2ff194e:2025-10-01T19:00:00.000000Z";
        
        // Create a test booking with the decoded text instead of base64
        Booking directBooking = new Booking();
        directBooking.setId("68dbb3d6736e8ffa240715d3");
        directBooking.setStationId("68d63aae375b419bf2ff194e");
        directBooking.setSlotNumber(7);
        directBooking.setStatus("pending");
        directBooking.setQrCode(decodedBookingData); // Use decoded text directly
        
        Date now = new Date();
        directBooking.setCreatedAt(now);
        directBooking.setStartTime(new Date(now.getTime() + 3600000));
        directBooking.setEndTime(new Date(now.getTime() + 5400000));
        
        Log.d("QRTest", "Testing with decoded text: " + decodedBookingData);
        
        Toast.makeText(this, "Debug tests completed - check logcat for results", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    public static void start(Context context) {
        Intent intent = new Intent(context, QRTestActivity.class);
        context.startActivity(intent);
    }
}