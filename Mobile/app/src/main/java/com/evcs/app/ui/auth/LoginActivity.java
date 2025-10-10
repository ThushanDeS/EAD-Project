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
import com.evcs.app.data.remote.dto.LoginRequest;
import com.evcs.app.data.remote.dto.LoginResponse;
import com.evcs.app.databinding.ActivityLoginBinding;
import com.evcs.app.ui.operator.OperatorMainActivity;
import com.evcs.app.ui.owner.OwnerMainActivity;
import com.evcs.app.utils.SharedPreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private ActivityLoginBinding binding;
    private SharedPreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        preferencesManager = new SharedPreferencesManager(this);
        
        // Check if user is already logged in
        if (preferencesManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        
        setupViews();
    }
    
    private void setupViews() {
        // Load last entered credentials (try email first, then NIC)
        String lastEmail = preferencesManager.getLastEmail();
        String lastNic = preferencesManager.getLastNic();

        // Set the last used credential (prefer email if both exist)
        if (!TextUtils.isEmpty(lastEmail)) {
            binding.inputCredential.setText(lastEmail);
        } else if (!TextUtils.isEmpty(lastNic)) {
            binding.inputCredential.setText(lastNic);
        }
        
        binding.btnSignIn.setOnClickListener(v -> performLogin());
        binding.linkSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Debug: Long click on login button to test API connection
        binding.btnSignIn.setOnLongClickListener(v -> {
            Intent intent = new Intent(this, com.evcs.app.ui.debug.ApiTestActivity.class);
            startActivity(intent);
            return true;
        });
    }
    
    private void performLogin() {
    String emailOrNic = binding.inputCredential.getText().toString().trim();
    String password = binding.inputSecret.getText().toString();
        
        // Validate inputs
        if (TextUtils.isEmpty(emailOrNic)) {
            binding.inputCredential.setError("Email or NIC is required");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.inputSecret.setError("Password is required");
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Determine if input is email or NIC
        final String email;
        final String nic;
        
        if (isValidEmail(emailOrNic)) {
            email = emailOrNic;
            nic = null;
        } else {
            email = null;
            nic = emailOrNic;
        }
        
        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, nic, password);
        
        // Make API call
        ApiClient.getInstance().getApiService().login(loginRequest)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            User user = loginResponse.getUser();
                            String token = loginResponse.getToken();
                            
                            // Save user data and token
                            user.setToken(token);
                            preferencesManager.saveUser(user);
                            preferencesManager.saveToken(token);
                            
                            // Save the credential that was used for login
                            if (email != null) {
                                preferencesManager.saveLoginCredentials(email, null);
                            } else {
                                preferencesManager.saveLoginCredentials(null, nic);
                            }
                            
                            // Update API client token
                            ApiClient.getInstance().updateToken(token);
                            
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                            
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSignIn.setEnabled(!loading);
        binding.inputCredential.setEnabled(!loading);
        binding.inputSecret.setEnabled(!loading);
    }
    
    /**
     * Check if the input string is a valid email format
     */
    private boolean isValidEmail(String input) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }
    
    private void navigateToMainActivity() {
        User currentUser = preferencesManager.getCurrentUser();
        if (currentUser != null) {
            Intent intent;
            if (currentUser.isEvOwner()) {
                intent = new Intent(this, OwnerMainActivity.class);
            } else if (currentUser.isStationOperator()) {
                intent = new Intent(this, OperatorMainActivity.class);
            } else {
                Toast.makeText(this, "Unsupported user role", Toast.LENGTH_SHORT).show();
                return;
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}