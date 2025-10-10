package com.evcs.app.ui.debug;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.evcs.app.R;
import com.evcs.app.config.ApiConfig;
import com.evcs.app.utils.NetworkUtils;

public class ApiTestActivity extends AppCompatActivity {
    
    private TextView textViewResult;
    private Button buttonTestApi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create simple layout
        setContentView(createLayout());
        
        initViews();
        setupClickListeners();
    }
    
    private int createLayout() {
        // For simplicity, we'll use a basic layout
        // In a real app, you would create a proper XML layout file
        return android.R.layout.activity_list_item;
    }
    
    private void initViews() {
        // Create views programmatically for testing
        textViewResult = new TextView(this);
        textViewResult.setText("API Test Results will appear here...\n\nCurrent API URL: " + ApiConfig.Endpoints.CURRENT_BASE_URL);
        textViewResult.setPadding(16, 16, 16, 16);
        
        buttonTestApi = new Button(this);
        buttonTestApi.setText("Test API Connection");
        
        // Add to a simple container
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.addView(textViewResult);
        container.addView(buttonTestApi);
        
        setContentView(container);
    }
    
    private void setupClickListeners() {
        buttonTestApi.setOnClickListener(v -> testApiConnection());
    }
    
    private void testApiConnection() {
        textViewResult.setText("Testing API connection...\n\nURL: " + ApiConfig.Endpoints.CURRENT_BASE_URL);
        
        // Check network first
        if (!NetworkUtils.isNetworkAvailable(this)) {
            textViewResult.append("\n❌ No internet connection");
            return;
        }
        
        textViewResult.append("\n✅ Internet connection available");
        
        // Test API connection
        NetworkUtils.testApiConnection(this, new NetworkUtils.ApiConnectionCallback() {
            @Override
            public void onResult(boolean success, String message) {
                runOnUiThread(() -> {
                    if (success) {
                        textViewResult.append("\n✅ API Connection: " + message);
                        Toast.makeText(ApiTestActivity.this, "API is reachable!", Toast.LENGTH_SHORT).show();
                    } else {
                        textViewResult.append("\n❌ API Connection Failed: " + message);
                        Toast.makeText(ApiTestActivity.this, "API connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}