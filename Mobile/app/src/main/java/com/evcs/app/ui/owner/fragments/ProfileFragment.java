package com.evcs.app.ui.owner.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.evcs.app.R;
import com.evcs.app.data.models.User;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.data.remote.ApiService;
import com.evcs.app.ui.auth.LoginActivity;
import com.evcs.app.utils.SharedPreferencesManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    
    private static final String TAG = "ProfileFragment";
    
    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextNic;
    private Button buttonUpdateProfile;
    private Button buttonDeactivateAccount;
    private Button buttonLogout;
    private View progressBar;
    
    private SharedPreferencesManager preferencesManager;
    private ApiService apiService;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
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
        buttonDeactivateAccount = view.findViewById(R.id.buttonDeactivateAccount);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        
        // Try to find progress bar, create a simple one if not found
        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar == null) {
            // If no progress bar in layout, we'll show loading via button text
        }
    }
    
    private void setupClickListeners() {
        if (buttonUpdateProfile != null) {
            buttonUpdateProfile.setOnClickListener(v -> updateProfile());
        }
        
        if (buttonDeactivateAccount != null) {
            buttonDeactivateAccount.setOnClickListener(v -> showDeactivateConfirmation());
        }
        
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> logout());
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
        Log.d(TAG, "Loading user profile from server");
        
        Call<User> call = apiService.getMyProfile();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoadingState(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Profile loaded successfully from server");
                    User serverUser = response.body();
                    
                    // Update local storage with latest data
                    preferencesManager.saveUser(serverUser);
                    
                    // Update UI with latest data from server
                    populateFields(serverUser);
                    
                    Toast.makeText(getContext(), "Profile data refreshed", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to load profile from server: " + response.code());
                    
                    String errorMessage;
                    if (response.code() == 401) {
                        errorMessage = "Session expired. Please login again.";
                    } else if (response.code() == 404) {
                        errorMessage = "User profile not found.";
                    } else {
                        errorMessage = "Failed to load profile from server.";
                    }
                    
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    
                    // If no local data either, show empty state
                    if (getCurrentUser() == null) {
                        clearFields();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoadingState(false);
                Log.e(TAG, "Error loading profile from server: " + t.getMessage());
                
                // If server call fails, check if we have local data
                if (getCurrentUser() == null) {
                    Toast.makeText(getContext(), "Cannot load profile. Check your internet connection.", Toast.LENGTH_LONG).show();
                    clearFields();
                } else {
                    Toast.makeText(getContext(), "Using cached profile data. Check internet connection.", Toast.LENGTH_SHORT).show();
                }
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
        
        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("email", email);
        updates.put("nic", nic);
        
        Log.d(TAG, "Updating profile: name=" + name + ", phone=" + phone + ", email=" + email + ", nic=" + nic);
        
        Call<User> call = apiService.updateProfile(updates);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Profile updated successfully");
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Update stored user data
                    User updatedUser = response.body();
                    preferencesManager.saveUser(updatedUser);
                    
                    // Refresh UI
                    populateFields(updatedUser);
                } else {
                    Log.e(TAG, "Failed to update profile: " + response.code());
                    String errorMessage = "Failed to update profile";
                    if (response.code() == 409) {
                        errorMessage = "Email or NIC already exists";
                    } else if (response.code() == 400) {
                        errorMessage = "Invalid input data";
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Error updating profile: " + t.getMessage());
                Toast.makeText(getContext(), "Error updating profile: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showDeactivateConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? This action cannot be undone and you will be logged out immediately.")
            .setPositiveButton("Deactivate", (dialog, which) -> deactivateAccount())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deactivateAccount() {
        Log.d(TAG, "Deactivating account");
        
        Call<Void> call = apiService.deactivateAccount();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Account deactivated successfully");
                    Toast.makeText(getContext(), "Account deactivated successfully", Toast.LENGTH_SHORT).show();
                    logout(); // Log out the user immediately
                } else {
                    Log.e(TAG, "Failed to deactivate account: " + response.code());
                    Toast.makeText(getContext(), "Failed to deactivate account", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error deactivating account: " + t.getMessage());
                Toast.makeText(getContext(), "Error deactivating account: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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