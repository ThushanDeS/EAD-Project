package com.evcs.app.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.evcs.app.R;
import com.evcs.app.databinding.ActivityOwnerMainBinding;
import com.evcs.app.ui.auth.LoginActivity;
import com.evcs.app.ui.owner.fragments.DashboardFragment;
import com.evcs.app.ui.owner.fragments.BookingsFragment;
import com.evcs.app.ui.owner.fragments.StationsFragment;
import com.evcs.app.ui.owner.fragments.ProfileFragment;
import com.evcs.app.ui.profile.ProfileActivity;
import com.evcs.app.utils.SharedPreferencesManager;
import com.google.android.material.navigation.NavigationBarView;

public class OwnerMainActivity extends AppCompatActivity {
    
    private ActivityOwnerMainBinding binding;
    private SharedPreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOwnerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        preferencesManager = new SharedPreferencesManager(this);
        
        setupBottomNavigation();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }
    
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (itemId == R.id.nav_stations) {
                    fragment = new StationsFragment();
                } else if (itemId == R.id.nav_bookings) {
                    fragment = new BookingsFragment();
                } else if (itemId == R.id.nav_profile) {
                    fragment = new ProfileFragment();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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