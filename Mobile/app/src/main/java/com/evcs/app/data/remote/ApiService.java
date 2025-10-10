package com.evcs.app.data.remote;

import com.evcs.app.data.models.Booking;
import com.evcs.app.data.models.Station;
import com.evcs.app.data.models.StationAvailability;
import com.evcs.app.data.models.User;
import com.evcs.app.data.remote.dto.CreateBookingRequest;
import com.evcs.app.data.remote.dto.LoginRequest;
import com.evcs.app.data.remote.dto.LoginResponse;
import com.evcs.app.data.remote.dto.RegisterRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    
    // Authentication endpoints
    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @POST("api/v1/auth/register/owner")
    Call<LoginResponse> registerOwner(@Body RegisterRequest request);
    
    // Station endpoints
    @GET("api/v1/stations")
    Call<List<Station>> getStations(@Query("active") Boolean active, @Query("type") String type);
    
    @GET("api/v1/stations/{id}/availability")
    Call<StationAvailability> getStationAvailability(@Path("id") String stationId, @Query("date") String date);
    
    // Booking endpoints
    @POST("api/v1/bookings")
    Call<Booking> createBooking(@Body CreateBookingRequest request, @retrofit2.http.Header("X-OwnerId") String ownerId);
    
    @GET("api/v1/bookings/mine")
    Call<List<Booking>> getMyBookings(@Query("status") String status, @Query("future") Boolean future);
    
    @PATCH("api/v1/bookings/{id}")
    Call<Booking> updateBooking(@Path("id") String bookingId, @Body Map<String, Object> updates);
    
    @POST("api/v1/bookings/{id}:cancel")
    Call<Booking> cancelBooking(@Path("id") String bookingId);

    @POST("api/v1/bookings/{id}/approve")
    Call<Booking> approveBooking(@Path("id") String bookingId);

    // Operator endpoints
    @POST("api/v1/operator/scan")
    Call<Booking> scanQrCode(@Body Map<String, String> qrRequest);
    
    @POST("api/v1/operator/bookings/{id}:confirm-arrival")
    Call<Booking> confirmArrival(@Path("id") String bookingId);
    
    @POST("api/v1/operator/bookings/{id}:finalize")
    Call<Booking> finalizeBooking(@Path("id") String bookingId, @Body Map<String, Object> finalizeData);
    
    // User profile endpoints
    @GET("api/v1/me")
    Call<User> getMyProfile();
    
    @PATCH("api/v1/me")
    Call<User> updateProfile(@Body Map<String, Object> updates);
    
    @POST("api/v1/me:deactivate")
    Call<Void> deactivateAccount();

    // Operator - get bookings for their station
    @GET("api/v1/stations/bookings")
    Call<List<Booking>> getStationBookings();
    // Health check
    @GET("api/v1/health")
    Call<Map<String, Object>> healthCheck();
}