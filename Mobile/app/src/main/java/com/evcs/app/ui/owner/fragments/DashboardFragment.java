package com.evcs.app.ui.owner.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import com.evcs.app.R;
import com.evcs.app.data.models.Booking;
import com.evcs.app.data.models.User;
import com.evcs.app.data.remote.ApiClient;
import com.evcs.app.databinding.FragmentDashboardBinding;
import com.evcs.app.ui.booking.CreateBookingActivity;
import com.evcs.app.utils.SharedPreferencesManager;
import com.evcs.app.utils.JsonDebugHelper;
import com.evcs.app.ui.adapters.BookingAdapter;
import com.evcs.app.ui.owner.fragments.StationsFragment;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Rect;
import android.content.res.Resources;

public class DashboardFragment extends Fragment {
    
    private static final int REQUEST_CREATE_BOOKING = 1001;
    
    private FragmentDashboardBinding binding;
    private SharedPreferencesManager preferencesManager;
    private int loadingTasksCount = 0;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout manually first
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        // Then create binding from the inflated view
        binding = FragmentDashboardBinding.bind(view);
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preferencesManager = new SharedPreferencesManager(requireContext());
        setupViews();
        loadDashboardData();
    }
    
    private void setupViews() {
        User currentUser = preferencesManager.getCurrentUser();
        if (currentUser != null) {
            // The welcome TextView was removed as part of the PulseCharge rebrand.
            // Keep the user info available here for future usage or logging.
            // Log.d("DashboardFragment", "Welcome user: " + currentUser.getName());
        }
        
        binding.buttonNewBooking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateBookingActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_BOOKING);
        });

        // Find Stations button navigates to StationsFragment (same as bottom nav)
        if (binding.buttonFindStations != null) {
            binding.buttonFindStations.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    StationsFragment fragment = new StationsFragment();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
        
        // Setup refresh listener
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDashboardData();
        });
        
        // Configure refresh colors for dark theme
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.secondary_color,
            R.color.status_approved,
            R.color.success
        );
        
    // Setup click listeners for status containers (new style)
    binding.containerPending.setOnClickListener(v -> navigateToPendingBookings());
    binding.containerApproved.setOnClickListener(v -> navigateToApprovedBookings());
    binding.containerCompleted.setOnClickListener(v -> navigateToCompletedBookings());
    }
    
    private void loadDashboardData() {
        if (binding != null) {
            // Show refresh indicator when loading
            binding.swipeRefreshLayout.setRefreshing(true);
        }
        
        // Reset loading tasks counter - we have 2 main API calls (booking stats has 2 sub-calls + recent bookings = 3 total)
        loadingTasksCount = 3;
        
        // Set a timeout fallback to ensure refresh indicator doesn't get stuck
        timeoutHandler.postDelayed(() -> {
            if (binding != null) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }, 30000); // 30 second timeout
        
        // Load booking statistics
        loadBookingStats();
        
        // Load recent bookings
        loadRecentBookings();
    }
    
    private void loadBookingStats() {
        User currentUser = preferencesManager.getCurrentUser();
        if (currentUser == null) return;
        
        // Get bookings count for pending and approved
        ApiClient.getInstance().getApiService()
                .getMyBookings("pending,approved", null)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> bookings = response.body();
                            Log.d("DashboardFragment", "Pending/Approved bookings count: " + bookings.size());
                            
                            int pendingCount = 0;
                            int approvedCount = 0;
                            
                            for (Booking booking : bookings) {
                                Log.d("DashboardFragment", "Booking status: " + booking.getStatus());
                                if (booking.isPending()) {
                                    pendingCount++;
                                } else if (booking.isApproved()) {
                                    approvedCount++;
                                }
                            }
                            
                            Log.d("DashboardFragment", "Pending: " + pendingCount + ", Approved: " + approvedCount);
                            
                            if (binding != null) {
                                binding.textViewPendingCount.setText(String.valueOf(pendingCount));
                                binding.textViewApprovedCount.setText(String.valueOf(approvedCount));
                            }
                        } else {
                            Log.e("DashboardFragment", "Failed to get bookings: " + response.code());
                        }
                        onTaskCompleted();
                    }
                    
                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e("DashboardFragment", "Failed to load booking stats", t);
                        onTaskCompleted();
                        // Handle error silently for stats - don't access binding here
                    }
                });
        
        // Get completed bookings count separately
        ApiClient.getInstance().getApiService()
                .getMyBookings("completed", null)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> completedBookings = response.body();
                            Log.d("DashboardFragment", "Completed bookings count: " + completedBookings.size());
                            
                            if (binding != null) {
                                binding.textViewCompletedCount.setText(String.valueOf(completedBookings.size()));
                            }
                        } else {
                            Log.e("DashboardFragment", "Failed to get completed bookings: " + response.code());
                        }
                        onTaskCompleted();
                    }
                    
                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e("DashboardFragment", "Failed to load completed booking stats", t);
                        onTaskCompleted();
                        // Handle error silently for stats - don't access binding here
                    }
                });
    }
    
    private void loadRecentBookings() {
        ApiClient.getInstance().getApiService()
                .getMyBookings(null, true) // Future bookings
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        if (response.isSuccessful()) {
                            Log.d("DashboardFragment", "Response successful, body is null: " + (response.body() == null));
                            
                            if (response.body() != null && binding != null) {
                                List<Booking> recentBookings = response.body();
                                Log.d("DashboardFragment", "Received " + recentBookings.size() + " bookings");
                                
                                if (recentBookings.isEmpty()) {
                                    binding.textViewNoBookings.setVisibility(View.VISIBLE);
                                    binding.recyclerViewRecentBookings.setVisibility(View.GONE);
                                } else {
                                    binding.textViewNoBookings.setVisibility(View.GONE);
                                    binding.recyclerViewRecentBookings.setVisibility(View.VISIBLE);
                                    
                                    // Take only first 5 bookings for dashboard
                                    if (recentBookings.size() > 5) {
                                        recentBookings = recentBookings.subList(0, 5);
                                    }
                                    
                                    // Create final variable for lambda access
                                    final List<Booking> finalRecentBookings = recentBookings;
                                    
                                    // Resolve station names before displaying
                                    com.evcs.app.utils.StationNameResolver.getInstance()
                                            .resolveStationNames(finalRecentBookings, new com.evcs.app.utils.StationNameResolver.StationResolverCallback() {
                                                @Override
                                                public void onStationsResolved(List<Booking> bookingsWithStationNames) {
                                                    if (getActivity() != null && binding != null) {
                                                        getActivity().runOnUiThread(() -> {
                                                            // Setup recycler view with bookings
                                                            BookingAdapter adapter = new BookingAdapter(bookingsWithStationNames, false);
                                                            binding.recyclerViewRecentBookings.setAdapter(adapter);
                                                            GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
                                                            binding.recyclerViewRecentBookings.setLayoutManager(glm);
                                                            // Add spacing (in dp)
                                                            int spacingDp = 8;
                                                            int spacingPx = Math.round(spacingDp * Resources.getSystem().getDisplayMetrics().density);
                                                            // Remove any previous decorations to avoid duplicates
                                                            while (binding.recyclerViewRecentBookings.getItemDecorationCount() > 0) {
                                                                binding.recyclerViewRecentBookings.removeItemDecorationAt(0);
                                                            }
                                                            binding.recyclerViewRecentBookings.addItemDecoration(new GridSpacingItemDecoration(2, spacingPx, true));
                                                        });
                                                    }
                                                }
                                                
                                                @Override
                                                public void onError(String error) {
                                                    Log.w("DashboardFragment", "Station name resolution error: " + error);
                                                    // Still show bookings even if station names couldn't be resolved
                                                    if (getActivity() != null && binding != null) {
                                                        getActivity().runOnUiThread(() -> {
                                                            BookingAdapter adapter = new BookingAdapter(finalRecentBookings, false);
                                                            binding.recyclerViewRecentBookings.setAdapter(adapter);
                                                            GridLayoutManager glm2 = new GridLayoutManager(getContext(), 2);
                                                            binding.recyclerViewRecentBookings.setLayoutManager(glm2);
                                                            int spacingDp2 = 8;
                                                            int spacingPx2 = Math.round(spacingDp2 * Resources.getSystem().getDisplayMetrics().density);
                                                            while (binding.recyclerViewRecentBookings.getItemDecorationCount() > 0) {
                                                                binding.recyclerViewRecentBookings.removeItemDecorationAt(0);
                                                            }
                                                            binding.recyclerViewRecentBookings.addItemDecoration(new GridSpacingItemDecoration(2, spacingPx2, true));
                                                        });
                                                    }
                                                }
                                            });
                                }
                            }
                        } else {
                            Log.e("DashboardFragment", "Response not successful or body is null");
                        }
                        onTaskCompleted();
                    }
                    
                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        onTaskCompleted();
                        Log.e("DashboardFragment", "Failed to load bookings", t);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load bookings: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void navigateToPendingBookings() {
        if (getParentFragmentManager() != null) {
            PendingBookingsFragment fragment = new PendingBookingsFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void navigateToApprovedBookings() {
        if (getParentFragmentManager() != null) {
            ApprovedBookingsFragment fragment = new ApprovedBookingsFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void navigateToCompletedBookings() {
        if (getParentFragmentManager() != null) {
            CompletedBookingsFragment fragment = new CompletedBookingsFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void onTaskCompleted() {
        loadingTasksCount--;
        if (loadingTasksCount <= 0 && binding != null) {
            timeoutHandler.removeCallbacksAndMessages(null); // Cancel timeout
            binding.swipeRefreshLayout.setRefreshing(false);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CREATE_BOOKING) {
            if (resultCode == CreateBookingActivity.RESULT_BOOKING_CREATED) {
                Log.d("DashboardFragment", "Booking created successfully, refreshing dashboard data");
                // Refresh the dashboard to show updated booking counts and lists
                loadDashboardData();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeoutHandler.removeCallbacksAndMessages(null); // Clean up any pending timeouts
        binding = null;
    }

    // Simple Grid spacing decoration
    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}