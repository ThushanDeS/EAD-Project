package com.evcs.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.evcs.app.R;
import com.evcs.app.data.models.User;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.data.remote.dto.LoginResponse;
import com.evcs.app.data.remote.dto.RegisterRequest;
import com.evcs.app.databinding.ActivityRegisterBinding;
import com.evcs.app.ui.owner.OwnerMainActivity;
import com.evcs.app.utils.SharedPreferencesManager;
import com.evcs.app.utils.NetworkUtils;
import com.evcs.app.config.ApiConfig;
import com.evcs.app.utils.DebugUtils;
import com.evcs.app.utils.ErrorTracker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    
    private ActivityRegisterBinding binding;
    private SharedPreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        preferencesManager = new SharedPreferencesManager(this);
        setupViews();
    }
    
    private void setupViews() {
        binding.buttonRegister.setOnClickListener(v -> performRegistration());
        binding.textViewLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
        
        // Debug: Long click to show API config
        binding.buttonRegister.setOnLongClickListener(v -> {
            DebugUtils.showApiConfigInfo(this);
            return true;
        });
    }
    
    private void performRegistration() {
        String nic = binding.editTextNic.getText().toString().trim();
        String name = binding.editTextName.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString();
        
        // Clear previous errors
        binding.editTextNic.setError(null);
        binding.editTextName.setError(null);
        binding.editTextPhone.setError(null);
        binding.editTextEmail.setError(null);
        binding.editTextPassword.setError(null);
        binding.editTextConfirmPassword.setError(null);
        
        // Validate inputs
        boolean isValid = true;
        
        if (TextUtils.isEmpty(nic)) {
            binding.editTextNic.setError("NIC is required");
            isValid = false;
        } else if (nic.length() < ApiConfig.Validation.MIN_NIC_LENGTH) {
            binding.editTextNic.setError("NIC must be at least " + ApiConfig.Validation.MIN_NIC_LENGTH + " characters");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(name)) {
            binding.editTextName.setError("Name is required");
            isValid = false;
        } else if (name.length() < ApiConfig.Validation.MIN_NAME_LENGTH) {
            binding.editTextName.setError("Name must be at least " + ApiConfig.Validation.MIN_NAME_LENGTH + " characters");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(phone)) {
            binding.editTextPhone.setError("Phone number is required");
            isValid = false;
        } else if (!phone.matches(ApiConfig.Validation.PHONE_REGEX)) {
            binding.editTextPhone.setError("Please enter a valid phone number");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(email)) {
            binding.editTextEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Please enter a valid email address");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < ApiConfig.Validation.MIN_PASSWORD_LENGTH) {
            binding.editTextPassword.setError("Password must be at least " + ApiConfig.Validation.MIN_PASSWORD_LENGTH + " characters");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }
        
        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please check your network settings.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(nic, name, phone, password, email);
        
        // Debug logging
        DebugUtils.logRegistrationAttempt(email, nic);
        android.util.Log.d("RegisterActivity", "Attempting registration with email: " + email + ", nic: " + nic);
        
        // Make API call
        ApiClient.getInstance().getApiService().registerOwner(registerRequest)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);
                        
                        DebugUtils.logApiResponse(response.code(), "register/owner");
                        android.util.Log.d("RegisterActivity", "Registration response code: " + response.code());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            User user = loginResponse.getUser();
                            String token = loginResponse.getToken();
                            
                            android.util.Log.d("RegisterActivity", "Registration successful for user: " + user.getName());
                            
                            // Save user data and token
                            user.setToken(token);
                            preferencesManager.saveUser(user);
                            preferencesManager.saveToken(token);
                            preferencesManager.saveLoginCredentials(email, nic);
                            
                            // Update API client token
                            ApiClient.getInstance().updateToken(token);
                            
                            Toast.makeText(RegisterActivity.this, "Registration successful! Welcome " + user.getName(), Toast.LENGTH_LONG).show();
                            
                            // Navigate to main activity
                            Intent intent = new Intent(RegisterActivity.this, OwnerMainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            
                        } else {
                            String errorMessage = "Registration failed";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    android.util.Log.e("RegisterActivity", "Error body: " + errorBody);
                                    
                                    // Parse common error messages
                                    if (errorBody.contains("email") && errorBody.contains("exists")) {
                                        errorMessage = "Email already exists";
                                    } else if (errorBody.contains("nic") && errorBody.contains("exists")) {
                                        errorMessage = "NIC already exists";
                                    } else if (response.code() == 409) {
                                        errorMessage = "User with this NIC or email already exists";
                                    } else if (response.code() == 400) {
                                        errorMessage = "Invalid input data";
                                    } else if (response.code() == 500) {
                                        errorMessage = "Server error. Please try again later";
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("RegisterActivity", "Error parsing error response", e);
                                ErrorTracker.logError(RegisterActivity.this, "REGISTRATION_PARSE_ERROR", e.getMessage());
                            }
                            
                            ErrorTracker.logError(RegisterActivity.this, "REGISTRATION_API_ERROR", 
                                "Code: " + response.code() + " - " + errorMessage);
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        setLoading(false);
                        android.util.Log.e("RegisterActivity", "Registration network error", t);
                        
                        String errorMessage;
                        if (t instanceof java.net.ConnectException) {
                            errorMessage = "Cannot connect to server. Please check your internet connection and try again.";
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMessage = "Connection timeout. Please try again.";
                        } else if (t instanceof java.net.UnknownHostException) {
                            errorMessage = "Server not found. Please check your internet connection.";
                        } else {
                            errorMessage = "Registration failed: " + t.getMessage();
                        }
                        
                        ErrorTracker.logError(RegisterActivity.this, "REGISTRATION_NETWORK_ERROR", 
                            t.getClass().getSimpleName() + ": " + t.getMessage());
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonRegister.setEnabled(!loading);
        binding.editTextNic.setEnabled(!loading);
        binding.editTextName.setEnabled(!loading);
        binding.editTextPhone.setEnabled(!loading);
        binding.editTextEmail.setEnabled(!loading);
        binding.editTextPassword.setEnabled(!loading);
        binding.editTextConfirmPassword.setEnabled(!loading);
    }
}