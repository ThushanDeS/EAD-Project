package com.evcs.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.evcs.app.data.remote.ApiClient;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {
    
    private static final String TAG = "NetworkUtils";
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
    }
    
    public static void testApiConnection(Context context, ApiConnectionCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onResult(false, "No internet connection available");
            return;
        }
        
        OkHttpClient client = new OkHttpClient();
        
        // Test the health endpoint first
        String baseUrl = "http://10.0.2.2:5000/"; // Use the same URL as ApiClient
        Request request = new Request.Builder()
                .url(baseUrl + "api/v1/health")
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API connection test failed", e);
                String errorMsg = "Cannot connect to server: " + e.getMessage();
                callback.onResult(false, errorMsg);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "API connection test successful");
                    callback.onResult(true, "API server is reachable");
                } else {
                    Log.w(TAG, "API returned error code: " + response.code());
                    callback.onResult(false, "API server returned error: " + response.code());
                }
            }
        });
    }
    
    public interface ApiConnectionCallback {
        void onResult(boolean success, String message);
    }
}