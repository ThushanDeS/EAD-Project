package com.evcs.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.evcs.app.data.models.User;
import com.google.gson.Gson;

public class SharedPreferencesManager {
    
    private static final String PREF_NAME = "evcs_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private final SharedPreferences preferences;
    private final Gson gson;
    
    public SharedPreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }
    
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }
    
    public void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }
    
    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        preferences.edit()
                .putString(KEY_USER, userJson)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }
    
    public User getCurrentUser() {
        String userJson = preferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }
    
    // Convenience method alias
    public User getUser() {
        return getCurrentUser();
    }
    
    public void clearUser() {
        preferences.edit()
                .remove(KEY_USER)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }
    
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null;
    }
    
    public void logout() {
        preferences.edit().clear().apply();
    }
    
    public void saveLoginCredentials(String email, String nic) {
        preferences.edit()
                .putString("last_email", email)
                .putString("last_nic", nic)
                .apply();
    }
    
    public String getLastEmail() {
        return preferences.getString("last_email", "");
    }
    
    public String getLastNic() {
        return preferences.getString("last_nic", "");
    }
}