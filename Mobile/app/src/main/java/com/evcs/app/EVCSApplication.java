package com.evcs.app;

import android.app.Application;
import com.evcs.app.data.local.SQLiteDatabase;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.utils.SharedPreferencesManager;

public class EVCSApplication extends Application {
    
    private static EVCSApplication instance;
    private SQLiteDatabase database;
    private SharedPreferencesManager preferencesManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize SQLite database
        database = SQLiteDatabase.getInstance(this);
        
        // Initialize SharedPreferences manager
        preferencesManager = new SharedPreferencesManager(this);
        
        // Initialize API client
        ApiClient.initialize(this);
        
        // Preload stations for better booking display performance
        // This will happen in background and improve user experience
        com.evcs.app.utils.StationNameResolver.getInstance().preloadStations();
    }
    
    public static EVCSApplication getInstance() {
        return instance;
    }
    
    public SQLiteDatabase getDatabase() {
        return database;
    }
    
    public SharedPreferencesManager getPreferencesManager() {
        return preferencesManager;
    }
}