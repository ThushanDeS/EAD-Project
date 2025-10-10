package com.evcs.app.ui.operator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.evcs.app.R;
import com.evcs.app.databinding.ActivityOperatorMainBinding;
import com.evcs.app.ui.auth.LoginActivity;
import com.evcs.app.ui.operator.fragments.ScannerFragment;
import com.evcs.app.ui.operator.fragments.ActiveBookingsFragment;
import com.evcs.app.ui.operator.fragments.OperatorProfileFragment;
import com.evcs.app.utils.SharedPreferencesManager;
import com.google.android.material.navigation.NavigationBarView;

public class OperatorMainActivity extends AppCompatActivity {
    
    private ActivityOperatorMainBinding binding;
    private SharedPreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        preferencesManager = new SharedPreferencesManager(this);
        
        setupBottomNavigation();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new ScannerFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_scanner);
        }
    }
    
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                
                int itemId = item.getItemId();
                if (itemId == R.id.nav_scanner) {
                    fragment = new ScannerFragment();
                } else if (itemId == R.id.nav_active_bookings) {
                    fragment = new ActiveBookingsFragment();
                } else if (itemId == R.id.nav_operator_profile) {
                    fragment = new OperatorProfileFragment();
                }
                
                return loadFragment(fragment);
            }
        });
    }
    
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_operator, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        preferencesManager.logout();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}