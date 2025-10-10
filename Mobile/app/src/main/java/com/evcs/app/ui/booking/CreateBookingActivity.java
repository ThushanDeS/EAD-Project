package com.evcs.app.ui.booking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.models.Station;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.data.remote.dto.CreateBookingRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBookingActivity extends AppCompatActivity {
    
    public static final int RESULT_BOOKING_CREATED = 1001;
    
    private Spinner spinnerStations;
    private EditText editTextDate;
    private EditText editTextStartTime;
    private EditText editTextEndTime;
    private Spinner spinnerSlotNumber;
    private Button buttonCreateBooking;
    
    private List<Station> stations = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);
        
        initViews();
        loadStations();
        setupDateTimePickers();
        setupCreateButton();
    }
    
    private void initViews() {
        spinnerStations = findViewById(R.id.spinnerStations);
        editTextDate = findViewById(R.id.editTextDate);
        editTextStartTime = findViewById(R.id.editTextStartTime);
        editTextEndTime = findViewById(R.id.editTextEndTime);
        spinnerSlotNumber = findViewById(R.id.spinnerSlotNumber);
        buttonCreateBooking = findViewById(R.id.buttonCreateBooking);
        
        // Set default values
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        editTextDate.setText(dateFormat.format(selectedDate.getTime()));
        editTextStartTime.setText(timeFormat.format(startTime.getTime()));
        
        // Set end time 2 hours after start time
        endTime.setTime(startTime.getTime());
        endTime.add(Calendar.HOUR_OF_DAY, 2);
        editTextEndTime.setText(timeFormat.format(endTime.getTime()));
    }
    
    private void loadStations() {
        ApiClient.getInstance().getApiService()
                .getStations(true, null)
                .enqueue(new Callback<List<Station>>() {
                    @Override
                    public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            stations = response.body();
                            Log.d("CreateBooking", "Loaded " + stations.size() + " stations");
                            
                            // Sort stations alphabetically by name
                            stations.sort((s1, s2) -> {
                                if (s1.getName() == null && s2.getName() == null) return 0;
                                if (s1.getName() == null) return 1;
                                if (s2.getName() == null) return -1;
                                return s1.getName().compareToIgnoreCase(s2.getName());
                            });
                            
                            List<String> stationNames = new ArrayList<>();
                            for (Station station : stations) {
                                // Create display name with station name, type, and address (if available)
                                String displayName = station.getName();
                                
                                // Add type (AC/DC) if available
                                if (station.getType() != null && !station.getType().isEmpty()) {
                                    displayName += " (" + station.getType() + ")";
                                }
                                
                                // Add address if available and not null/empty
                                String address = station.getAddress();
                                if (address != null && !address.trim().isEmpty() && !address.equals("null")) {
                                    displayName += " - " + address;
                                }
                                
                                stationNames.add(displayName);
                            }
                            
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                CreateBookingActivity.this,
                                R.layout.spinner_item,
                                stationNames
                            );
                            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                            spinnerStations.setAdapter(adapter);
                        } else {
                            Log.e("CreateBooking", "Failed to load stations: " + response.code());
                            Toast.makeText(CreateBookingActivity.this, "Failed to load stations", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Station>> call, Throwable t) {
                        Log.e("CreateBooking", "Error loading stations", t);
                        Toast.makeText(CreateBookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void setupDateTimePickers() {
        editTextDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    editTextDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        editTextStartTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startTime.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    editTextStartTime.setText(timeFormat.format(startTime.getTime()));
                    
                    // Auto-update end time to 2 hours later
                    endTime.setTime(startTime.getTime());
                    endTime.add(Calendar.HOUR_OF_DAY, 2);
                    editTextEndTime.setText(timeFormat.format(endTime.getTime()));
                },
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });
        
        editTextEndTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endTime.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    editTextEndTime.setText(timeFormat.format(endTime.getTime()));
                },
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });
    }
    
    private void setupCreateButton() {
        buttonCreateBooking.setOnClickListener(v -> createBooking());
        
        // Add listener to station spinner to update slot number hint
        spinnerStations.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateSlotNumberHint(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void updateSlotNumberHint(int selectedStationIndex) {
        if (selectedStationIndex >= 0 && selectedStationIndex < stations.size()) {
            Station selectedStation = stations.get(selectedStationIndex);
            int slotCount = selectedStation.getSlotCount();
            
            // Create slot number options
            List<String> slotNumbers = new ArrayList<>();
            slotNumbers.add("Select slot number");
            for (int i = 1; i <= slotCount; i++) {
                slotNumbers.add("Slot " + i);
            }
            
            // Setup slot number spinner
            ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, slotNumbers);
            slotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSlotNumber.setAdapter(slotAdapter);
            
        } else {
            // Set empty adapter when no station is selected
            List<String> emptySlots = new ArrayList<>();
            emptySlots.add("Select slot number");
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, emptySlots);
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSlotNumber.setAdapter(emptyAdapter);
        }
    }
    
    private void createBooking() {
        if (stations.isEmpty()) {
            Toast.makeText(this, "Please wait for stations to load", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int selectedStationIndex = spinnerStations.getSelectedItemPosition();
        if (selectedStationIndex < 0 || selectedStationIndex >= stations.size()) {
            Toast.makeText(this, "Please select a station", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int selectedSlotIndex = spinnerSlotNumber.getSelectedItemPosition();
        if (selectedSlotIndex <= 0) {
            Toast.makeText(this, "Please select a slot number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Station selectedStation = stations.get(selectedStationIndex);
        
        // Convert spinner position to actual slot number (position 0 is "Select slot number", position 1 is "Slot 1", etc.)
        int slotNumber = selectedSlotIndex;
            
            // Get current user ID for the X-OwnerId header
            com.evcs.app.utils.SharedPreferencesManager prefsManager = 
                new com.evcs.app.utils.SharedPreferencesManager(this);
            com.evcs.app.data.models.User currentUser = prefsManager.getCurrentUser();
            
            if (currentUser == null || currentUser.getId() == null) {
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Combine date and time
            Calendar startDateTime = Calendar.getInstance();
            startDateTime.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
            startDateTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
            startDateTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
            startDateTime.set(Calendar.SECOND, 0);
            startDateTime.set(Calendar.MILLISECOND, 0);
            
            Calendar endDateTime = Calendar.getInstance();
            endDateTime.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
            endDateTime.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
            endDateTime.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
            endDateTime.set(Calendar.SECOND, 0);
            endDateTime.set(Calendar.MILLISECOND, 0);
            
            // Validate booking constraints
            Calendar now = Calendar.getInstance();
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.DAY_OF_YEAR, 7); // 7 days from now
            
            Calendar minBookingTime = Calendar.getInstance();
            minBookingTime.add(Calendar.HOUR_OF_DAY, 12); // 12 hours from now
            
            // Check if booking is too far in the future (more than 7 days)
            if (startDateTime.after(maxDate)) {
                Toast.makeText(this, "Bookings can only be made up to 7 days in advance", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Check if booking is too close (less than 12 hours)
            if (startDateTime.before(minBookingTime)) {
                Toast.makeText(this, "Bookings must be made at least 12 hours in advance", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Check if start time is before current time
            if (startDateTime.before(now)) {
                Toast.makeText(this, "Cannot book time in the past", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if end time is after start time
            if (endDateTime.before(startDateTime) || endDateTime.equals(startDateTime)) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check booking duration (minimum 30 minutes, maximum 8 hours)
            long durationMillis = endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis();
            long durationMinutes = durationMillis / (1000 * 60);
            
            if (durationMinutes < 30) {
                Toast.makeText(this, "Minimum booking duration is 30 minutes", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (durationMinutes > 480) { // 8 hours
                Toast.makeText(this, "Maximum booking duration is 8 hours", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Format dates for API - use ISO 8601 datetime format
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            
            CreateBookingRequest request = new CreateBookingRequest(
                selectedStation.getId(),
                isoFormat.format(startDateTime.getTime()),
                isoFormat.format(endDateTime.getTime()),
                slotNumber
            );
            
            // Debug logging using utility
            com.evcs.app.utils.BookingDebugUtils.logBookingRequest(request, currentUser.getId());
            
            buttonCreateBooking.setEnabled(false);
            
            ApiClient.getInstance().getApiService()
                    .createBooking(request, currentUser.getId())
                    .enqueue(new Callback<Booking>() {
                        @Override
                        public void onResponse(Call<Booking> call, Response<Booking> response) {
                            buttonCreateBooking.setEnabled(true);
                            
                            Log.d("CreateBooking", "Response code: " + response.code());
                            
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(CreateBookingActivity.this, "Booking created successfully!", Toast.LENGTH_SHORT).show();
                                
                                // Set result to indicate successful booking creation
                                setResult(RESULT_BOOKING_CREATED);
                                finish();
                            } else {
                                String errorMessage = "Failed to create booking";
                                
                                // Enhanced error handling
                                if (response.code() == 400) {
                                    errorMessage = "Invalid booking data. Please check your inputs.";
                                } else if (response.code() == 401) {
                                    errorMessage = "Authentication required. Please login again.";
                                } else if (response.code() == 403) {
                                    errorMessage = "You don't have permission to create bookings.";
                                } else if (response.code() == 409) {
                                    errorMessage = "Time slot is already booked. Please choose a different time.";
                                } else if (response.code() == 422) {
                                    errorMessage = "Invalid booking time. Check date and time constraints.";
                                } else {
                                    errorMessage += " (Error: " + response.code() + ")";
                                }
                                
                                Log.e("CreateBooking", "Booking creation failed: " + response.code() + " - " + errorMessage);
                                
                                // Try to get error details from response body
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBody = response.errorBody().string();
                                        Log.e("CreateBooking", "Error body: " + errorBody);
                                    }
                                } catch (Exception e) {
                                    Log.e("CreateBooking", "Error reading error body", e);
                                }
                                
                                Toast.makeText(CreateBookingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<Booking> call, Throwable t) {
                            buttonCreateBooking.setEnabled(true);
                            Log.e("CreateBooking", "Error creating booking", t);
                            
                            String errorMessage = "Network error: " + t.getMessage();
                            if (t instanceof java.net.ConnectException) {
                                errorMessage = "Cannot connect to server. Please check your internet connection.";
                            } else if (t instanceof java.net.SocketTimeoutException) {
                                errorMessage = "Request timeout. Please try again.";
                            }
                            
                            Toast.makeText(CreateBookingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
    }
}