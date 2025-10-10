package com.evcs.app.ui.owner.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evcs.app.R;
import com.evcs.app.data.models.Station;
import com.evcs.app.data.remote.ApiClient; // Import ApiClient
import com.evcs.app.ui.adapters.OwnerStationAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call; // Import Retrofit classes
import retrofit2.Callback;
import retrofit2.Response;

public class StationsFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final String TAG = "StationsFragment";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SQLiteDatabase db;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private RecyclerView recyclerViewStations;
    private OwnerStationAdapter stationAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stations_combined, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Setup RecyclerView ---
        recyclerViewStations = view.findViewById(R.id.recyclerViewStations);
        stationAdapter = new OwnerStationAdapter();
        recyclerViewStations.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewStations.setAdapter(stationAdapter);

        db = requireActivity().openOrCreateDatabase("EVStationDB", requireActivity().MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS stations(" +
                "id TEXT PRIMARY KEY, name TEXT, type TEXT, slots INTEGER, " +
                "lat REAL, lng REAL, address TEXT, openHours TEXT, closeHours TEXT)");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        LinearLayout bottomSheetLayout = view.findViewById(R.id.bottom_sheet_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        FloatingActionButton fabMyLocation = view.findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> checkLocationPermissionAndShowUser());

        // Call the new Retrofit method
        loadStationsFromApi();
    }

    // Uses Retrofit to fetch stations
    private void loadStationsFromApi() {
        Log.d(TAG, "Fetching stations from API using Retrofit...");
        ApiClient.getInstance().getApiService().getStations(true, null)
                .enqueue(new Callback<List<Station>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Station>> call, @NonNull Response<List<Station>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Station> stations = response.body();
                            Log.d(TAG, "Successfully fetched " + stations.size() + " stations.");

                            // Save the fetched data to the local database in a background thread
                            new Thread(() -> saveStationsToDatabase(stations)).start();
                        } else {
                            Log.e(TAG, "Failed to fetch stations. Code: " + response.code());
                            Toast.makeText(getContext(), "Failed to load stations.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Station>> call, @NonNull Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        Toast.makeText(getContext(), "Network error. Check connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Handles saving data to the database
    private void saveStationsToDatabase(List<Station> stations) {
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM stations");
            for (Station station : stations) {
                ContentValues values = new ContentValues();
                values.put("id", station.getId());
                values.put("name", station.getName());
                values.put("type", station.getType());
                values.put("slots", station.getSlotCount());
                values.put("lat", station.getLat());
                values.put("lng", station.getLng());
                values.put("address", station.getAddress());
                if (station.getHours() != null) {
                    values.put("openHours", station.getHours().getOpen());
                    values.put("closeHours", station.getHours().getClose());
                }
                db.insert("stations", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermissionAndShowUser();
    }

    private void checkLocationPermissionAndShowUser() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));
                    showNearbyStations(location);
                } else {
                    Toast.makeText(getContext(), "Could not get current location.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showNearbyStations(Location userLocation) {
        final List<Station> nearbyStations = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM stations", null);
        if (cursor.moveToFirst()) {
            do {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng"));

                Location stationLocation = new Location("");
                stationLocation.setLatitude(lat);
                stationLocation.setLongitude(lng);
                float distanceInMeters = userLocation.distanceTo(stationLocation);

                if (distanceInMeters < 10000) { // 10km radius
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(name)
                            .snippet("Type: " + type));

                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    int slots = cursor.getInt(cursor.getColumnIndexOrThrow("slots"));
                    Station station = new Station();
                    station.setName(name);
                    station.setAddress(address);
                    station.setSlotCount(slots);
                    station.setType(type);
                    nearbyStations.add(station);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // The RecyclerView must be updated on the UI thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> stationAdapter.setStations(nearbyStations));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndShowUser();
            } else {
                Toast.makeText(getContext(), "Location permission is required.", Toast.LENGTH_LONG).show();
            }
        }
    }
}