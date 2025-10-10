package com.evcs.app.data.remote;

import android.content.Context;
import com.evcs.app.data.models.Booking;
import com.evcs.app.utils.SharedPreferencesManager;
import com.evcs.app.config.ApiConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    
    // Base URL from configuration
    private static final String BASE_URL = ApiConfig.Endpoints.CURRENT_BASE_URL;
    
    private static ApiClient instance;
    private ApiService apiService;
    private SharedPreferencesManager preferencesManager;
    
    private ApiClient(Context context) {
        preferencesManager = new SharedPreferencesManager(context);
        
        // Create Gson with custom deserializers
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, jsonContext) -> {
                    String dateString = json.getAsString();
                    
                    // Try different date formats that the API might return
                    String[] patterns = {
                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        "yyyy-MM-dd'T'HH:mm:ssZ",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                    };
                    
                    for (String pattern : patterns) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            return sdf.parse(dateString);
                        } catch (ParseException ignored) {
                            // Try next pattern
                        }
                    }
                    
                    throw new JsonParseException("Unable to parse date: " + dateString);
                })
                .registerTypeAdapter(Booking.class, new JsonDeserializer<Booking>() {
                    @Override
                    public Booking deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext jsonContext) {
                        JsonObject jsonObject = json.getAsJsonObject();
                        Booking booking = new Booking();
                        
                        try {
                            // Handle _id
                            if (jsonObject.has("_id")) {
                                booking.setId(jsonObject.get("_id").getAsString());
                            }
                            
                            // Handle stationId - could be string or object
                            if (jsonObject.has("stationId")) {
                                JsonElement stationIdElement = jsonObject.get("stationId");
                                if (stationIdElement.isJsonPrimitive()) {
                                    booking.setStationId(stationIdElement.getAsString());
                                } else if (stationIdElement.isJsonObject()) {
                                    // Convert complex object to a simple string
                                    JsonObject stationObj = stationIdElement.getAsJsonObject();
                                    String stationIdStr = "station_" + System.currentTimeMillis(); // fallback
                                    if (stationObj.has("timestamp") && stationObj.has("increment")) {
                                        stationIdStr = stationObj.get("timestamp").getAsString() + "_" + stationObj.get("increment").getAsString();
                                    }
                                    booking.setStationId(stationIdStr);
                                }
                            }
                            
                            // Handle other simple fields
                            if (jsonObject.has("ownerId") && !jsonObject.get("ownerId").isJsonNull()) {
                                booking.setOwnerId(jsonObject.get("ownerId").getAsString());
                            }
                            if (jsonObject.has("slotNumber")) {
                                booking.setSlotNumber(jsonObject.get("slotNumber").getAsInt());
                            }
                            if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
                                booking.setStatus(jsonObject.get("status").getAsString());
                            }
                            if (jsonObject.has("qrCode") && !jsonObject.get("qrCode").isJsonNull()) {
                                booking.setQrCode(jsonObject.get("qrCode").getAsString());
                            }
                            if (jsonObject.has("qrImageBase64") && !jsonObject.get("qrImageBase64").isJsonNull()) {
                                booking.setQrImageBase64(jsonObject.get("qrImageBase64").getAsString());
                            }
                            if (jsonObject.has("qrImageContentType") && !jsonObject.get("qrImageContentType").isJsonNull()) {
                                booking.setQrImageContentType(jsonObject.get("qrImageContentType").getAsString());
                            }
                            
                            // Parse owner fields
                            if (jsonObject.has("ownerName") && !jsonObject.get("ownerName").isJsonNull()) {
                                booking.setOwnerName(jsonObject.get("ownerName").getAsString());
                            }
                            if (jsonObject.has("ownerNic") && !jsonObject.get("ownerNic").isJsonNull()) {
                                booking.setOwnerNic(jsonObject.get("ownerNic").getAsString());
                            }
                            
                            // Add debugging for owner fields
                            android.util.Log.d("ApiClient", "Deserializing booking " + booking.getId() + ":");
                            android.util.Log.d("ApiClient", "  - OwnerName from JSON: " + (jsonObject.has("ownerName") && !jsonObject.get("ownerName").isJsonNull() ? jsonObject.get("ownerName").getAsString() : "NULL or NOT FOUND"));
                            android.util.Log.d("ApiClient", "  - OwnerNic from JSON: " + (jsonObject.has("ownerNic") && !jsonObject.get("ownerNic").isJsonNull() ? jsonObject.get("ownerNic").getAsString() : "NULL or NOT FOUND"));
                            android.util.Log.d("ApiClient", "  - OwnerName in Booking: " + booking.getOwnerName());
                            android.util.Log.d("ApiClient", "  - OwnerNic in Booking: " + booking.getOwnerNic());
                            android.util.Log.d("ApiClient", "  - QrImageBase64: " + (jsonObject.has("qrImageBase64") && !jsonObject.get("qrImageBase64").isJsonNull() ? "NOT NULL" : "NULL"));
                            
                            // Handle dates
                            if (jsonObject.has("startTime")) {
                                booking.setStartTime(jsonContext.deserialize(jsonObject.get("startTime"), Date.class));
                            }
                            if (jsonObject.has("endTime")) {
                                booking.setEndTime(jsonContext.deserialize(jsonObject.get("endTime"), Date.class));
                            }
                            if (jsonObject.has("createdAt")) {
                                booking.setCreatedAt(jsonContext.deserialize(jsonObject.get("createdAt"), Date.class));
                            }
                            
                            // Handle owner details
                            if (jsonObject.has("ownerName")) {
                                booking.setOwnerName(jsonObject.get("ownerName").getAsString());
                            }
                            if (jsonObject.has("ownerNic")) {
                                booking.setOwnerNic(jsonObject.get("ownerNic").getAsString());
                            }
                            
                        } catch (Exception e) {
                            android.util.Log.e("ApiClient", "Error deserializing booking", e);
                        }
                        
                        return booking;
                    }
                })
                .setLenient() // Make Gson more forgiving with malformed JSON
                .create();
        
        // Create logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Create auth interceptor
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                String token = preferencesManager.getToken();
                
                if (token != null && !token.isEmpty()) {
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token);
                    
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
                
                return chain.proceed(original);
            }
        };
        
        // Create debug interceptor for API responses
        Interceptor debugInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);
                
                // Log the response for bookings endpoints
                if (request.url().toString().contains("bookings")) {
                    android.util.Log.d("ApiClient", "Request URL: " + request.url());
                    android.util.Log.d("ApiClient", "Response Code: " + response.code());
                    
                    // Read response body for logging (we need to peek to avoid consuming)
                    try {
                        String responseBody = response.peekBody(2048).string();
                        android.util.Log.d("ApiClient", "Response Body (first 2048 chars): " + responseBody);
                    } catch (IOException e) {
                        android.util.Log.e("ApiClient", "Error reading response body", e);
                    }
                }
                
                return response;
            }
        };

        // Create OkHttp client
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(debugInterceptor)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(ApiConfig.Timeouts.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.Timeouts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.Timeouts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        
        apiService = retrofit.create(ApiService.class);
    }
    
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
    }
    
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    public ApiService getApiService() {
        return apiService;
    }
    
    public void updateToken(String token) {
        preferencesManager.saveToken(token);
    }
    
    public void clearToken() {
        preferencesManager.clearToken();
    }
}