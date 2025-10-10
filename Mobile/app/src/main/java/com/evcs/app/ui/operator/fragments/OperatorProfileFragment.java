package com.evcs.app.ui.operator.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.evcs.app.R;
import com.evcs.app.data.models.User;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.data.remote.ApiService;
import com.evcs.app.ui.auth.LoginActivity;
import com.evcs.app.utils.SharedPreferencesManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorProfileFragment extends Fragment {
    
    private static final String TAG = "OperatorProfile";
    
    // UI Components
    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextNic;
    private Button buttonUpdateProfile;
    private ProgressBar progressBar;
    
    // Data & Services
    private SharedPreferencesManager preferencesManager;
    private ApiService apiService;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operator_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferencesManager = new SharedPreferencesManager(requireContext());
        
        // Initialize ApiClient if needed and get the service
        ApiClient.initialize(requireContext());
        apiService = ApiClient.getInstance().getApiService();
        
        initViews(view);
        loadUserProfile();
        setupClickListeners();
    }
    
    private void initViews(View view) {
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextName = view.findViewById(R.id.editTextName);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextNic = view.findViewById(R.id.editTextNic);
        buttonUpdateProfile = view.findViewById(R.id.buttonUpdateProfile);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        if (buttonUpdateProfile != null) {
            buttonUpdateProfile.setOnClickListener(v -> updateProfile());
        }
    }
    
    private void loadUserProfile() {
        Log.d(TAG, "Loading user profile");
        
        // Show loading state
        showLoadingState(true);
        
        // First, try to load from local storage for immediate display
        User currentUser = preferencesManager.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Populating fields with cached user data");
            populateFields(currentUser);
        } else {
            // Clear fields if no local data
            clearFields();
        }
        
        // Always fetch fresh data from server
        loadUserProfileFromServer();
    }
    
    private void loadUserProfileFromServer() {
        // Use the correct API endpoint that doesn't require userId
        apiService.getMyProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoadingState(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "Successfully loaded user profile from server");
                    
                    // Update local storage with fresh data
                    preferencesManager.saveUser(user);
                    
                    // Update UI with fresh data
                    populateFields(user);
                } else {
                    Log.e(TAG, "Failed to load user profile: " + response.code());
                    // Keep existing data displayed, just show error
                    Toast.makeText(getContext(), "Failed to refresh profile data", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoadingState(false);
                Log.e(TAG, "Error loading user profile: " + t.getMessage());
                // Keep existing data displayed, just show error
                Toast.makeText(getContext(), "Error refreshing profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private User getCurrentUser() {
        return preferencesManager.getCurrentUser();
    }
    
    private void showLoadingState(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        // Disable/enable form during loading
        setFormEnabled(!isLoading);
        
        if (buttonUpdateProfile != null) {
            if (isLoading) {
                buttonUpdateProfile.setText("Loading...");
                buttonUpdateProfile.setEnabled(false);
            } else {
                buttonUpdateProfile.setText("Update Profile");
                buttonUpdateProfile.setEnabled(true);
            }
        }
    }
    
    private void setFormEnabled(boolean enabled) {
        if (editTextEmail != null) editTextEmail.setEnabled(enabled);
        if (editTextName != null) editTextName.setEnabled(enabled);
        if (editTextPhone != null) editTextPhone.setEnabled(enabled);
        if (editTextNic != null) editTextNic.setEnabled(enabled);
    }
    
    private void clearFields() {
        Log.d(TAG, "Clearing profile fields");
        if (editTextEmail != null) editTextEmail.setText("");
        if (editTextName != null) editTextName.setText("");
        if (editTextPhone != null) editTextPhone.setText("");
        if (editTextNic != null) editTextNic.setText("");
    }
    
    private void populateFields(User user) {
        if (user == null) {
            Log.w(TAG, "Cannot populate fields: user is null");
            clearFields();
            return;
        }
        
        Log.d(TAG, "Populating fields for user: " + user.getName() + " (" + user.getEmail() + ")");
        
        if (editTextEmail != null) {
            editTextEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        }
        if (editTextName != null) {
            editTextName.setText(user.getName() != null ? user.getName() : "");
        }
        if (editTextPhone != null) {
            editTextPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        }
        if (editTextNic != null) {
            editTextNic.setText(user.getNic() != null ? user.getNic() : "");
        }
    }
    
    /**
     * Public method to refresh profile data from server
     * Can be called from other components or on swipe refresh
     */
    public void refreshProfile() {
        Log.d(TAG, "Refreshing profile data");
        loadUserProfile();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile data when user returns to this fragment
        Log.d(TAG, "Fragment resumed, refreshing profile");
        refreshProfile();
    }
    
    private void updateProfile() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String nic = editTextNic.getText().toString().trim();
        
        // Validate required fields
        boolean hasError = false;
        
        if (name.isEmpty()) {
            editTextName.setError("Name is required");
            if (!hasError) editTextName.requestFocus();
            hasError = true;
        }
        
        if (phone.isEmpty()) {
            editTextPhone.setError("Phone number is required");
            if (!hasError) editTextPhone.requestFocus();
            hasError = true;
        }
        
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            if (!hasError) editTextEmail.requestFocus();
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address");
            if (!hasError) editTextEmail.requestFocus();
            hasError = true;
        }
        
        if (nic.isEmpty()) {
            editTextNic.setError("NIC is required");
            if (!hasError) editTextNic.requestFocus();
            hasError = true;
        }
        
        if (hasError) {
            return;
        }
        
        // Show loading state
        showLoadingState(true);
        
        // Get current user to extract ID
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            showLoadingState(false);
            Toast.makeText(getContext(), "No user session found. Please login again.", Toast.LENGTH_LONG).show();
            logout();
            return;
        }
        
        // Prepare update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("phone", phone);
        updateData.put("email", email);
        updateData.put("nic", nic);
        
        // Make API call to update profile
        apiService.updateProfile(updateData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoadingState(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    Log.d(TAG, "Profile updated successfully");
                    
                    // Update local storage with updated user data
                    preferencesManager.saveUser(updatedUser);
                    
                    // Show success message
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Optionally refresh the UI with updated data
                    populateFields(updatedUser);
                } else {
                    Log.e(TAG, "Failed to update profile: " + response.code());
                    Toast.makeText(getContext(), "Failed to update profile. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoadingState(false);
                Log.e(TAG, "Error updating profile: " + t.getMessage());
                Toast.makeText(getContext(), "Error updating profile: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    

    
    private void logout() {
        Log.d(TAG, "Logging out user");
        preferencesManager.logout();
        
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}