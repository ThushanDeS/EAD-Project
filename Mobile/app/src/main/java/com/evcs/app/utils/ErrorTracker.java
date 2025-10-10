package com.evcs.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ErrorTracker {
    
    private static final String PREFS_NAME = "error_tracking";
    private static final String KEY_LAST_ERROR = "last_error";
    private static final String KEY_ERROR_COUNT = "error_count";
    
    public static void logError(Context context, String operation, String error) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        
        String errorLog = String.format("[%s] %s: %s", timestamp, operation, error);
        
        // Log to Android Log
        android.util.Log.e("ErrorTracker", errorLog);
        
        // Save to SharedPreferences
        int errorCount = prefs.getInt(KEY_ERROR_COUNT, 0) + 1;
        prefs.edit()
                .putString(KEY_LAST_ERROR, errorLog)
                .putInt(KEY_ERROR_COUNT, errorCount)
                .apply();
    }
    
    public static String getLastError(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LAST_ERROR, "No errors logged");
    }
    
    public static int getErrorCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ERROR_COUNT, 0);
    }
    
    public static void clearErrors(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}