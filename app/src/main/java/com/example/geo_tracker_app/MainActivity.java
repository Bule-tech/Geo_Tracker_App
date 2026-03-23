package com.example.geo_tracker_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.location.Location;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import com.example.geo_tracker_app.ui.add.AddLocationActivity;
import com.example.geo_tracker_app.ui.list.LocationListActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.example.geo_tracker_app.utils.NotificationHelper;
import com.example.geo_tracker_app.data.db.AppDatabase;
import com.example.geo_tracker_app.model.SavedLocation;
import com.example.geo_tracker_app.utils.GeofenceHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main screen of Geo_Tracker_app.
 * Handles current location display, notification setup,
 * geofence restoration, and navigation to other app screens.
 */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private Button btnAddLocation, btnViewLocations, btnCurrentLocation;
    private TextView tvCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    /**
     * Initializes the activity, UI components, and services.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);

        btnAddLocation = findViewById(R.id.btnAddLocation);
        btnViewLocations = findViewById(R.id.btnViewLocations);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(MainActivity.this, AddLocationActivity.class));
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(MainActivity.this, LocationListActivity.class));
                return true;
            }

            return false;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        restoreAllGeofences();
        btnAddLocation.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddLocationActivity.class)));

        btnViewLocations.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LocationListActivity.class)));

        btnCurrentLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());

        requestNotificationPermission();

    }

    /**
     * Restores all saved geofences from the database upon app startup.
     */
    private void restoreAllGeofences() {
        executorService.execute(() -> {
            List<SavedLocation> locations = appDatabase.savedLocationDao().getAllLocations();

            runOnUiThread(() -> {
                for (SavedLocation location : locations) {
                    GeofenceHelper.addGeofence(this, location, new GeofenceHelper.GeofenceResultCallback() {
                        @Override
                        public void onSuccess(String message) {
                        }

                        @Override
                        public void onFailure(String message) {
                        }
                    });
                }
            });
        });
    }

    /**
     * Checks for location permissions and fetches current location if granted.
     */
    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }

    /**
     * Shuts down the executor service when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Fetches the last known location of the device.
     */
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken()
                )
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateLocationText(location);
                    } else {
                        fetchLastKnownLocationAsFallback();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Failed to get current location: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    fetchLastKnownLocationAsFallback();
                });
    }

    private void fetchLastKnownLocationAsFallback() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateLocationText(location);
                    } else {
                        tvCurrentLocation.setText(
                                "Current location unavailable. Turn on GPS and try again."
                        );
                    }
                })
                .addOnFailureListener(e ->
                        tvCurrentLocation.setText(
                                "Unable to get location. Please check GPS settings."
                        ));
    }

    private void updateLocationText(Location location) {
        String locationText = "Latitude: " + location.getLatitude()
                + "\nLongitude: " + location.getLongitude();
        tvCurrentLocation.setText(locationText);
    }

    /**
     * Requests notification permission for devices running Android 13 or higher.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        2001
                );
            }
        }
    }

    /**
     * Callback for the result from requesting permissions.
     * @param requestCode The request code passed in {@link #requestPermissions}.
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
