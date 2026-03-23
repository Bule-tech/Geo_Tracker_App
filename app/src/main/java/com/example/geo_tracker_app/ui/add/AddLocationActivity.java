package com.example.geo_tracker_app.ui.add;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Location;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import com.example.geo_tracker_app.MainActivity;
import com.example.geo_tracker_app.ui.list.LocationListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.geo_tracker_app.R;
import com.example.geo_tracker_app.data.db.AppDatabase;
import com.example.geo_tracker_app.model.SavedLocation;
import com.example.geo_tracker_app.receiver.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.example.geo_tracker_app.utils.GeofenceHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Screen for adding a new saved location manually
 * or using the current GPS position.
 */
public class AddLocationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 3001;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST = 3002;

    private EditText etLocationName, etLatitude, etLongitude, etRadius;
    private Button btnUseCurrentLocation, btnSaveLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofencingClient geofencingClient;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    /**
     * Initializes the activity, sets up UI components and services.
     * @param savedInstanceState If the activity is being re-initialized then this Bundle contains the saved data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        etLocationName = findViewById(R.id.etLocationName);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        btnSaveLocation = findViewById(R.id.btnSaveLocation);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        btnUseCurrentLocation.setOnClickListener(v -> fetchCurrentLocation());
        btnSaveLocation.setOnClickListener(v -> saveLocation());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_add);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(AddLocationActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(AddLocationActivity.this, LocationListActivity.class));
                return true;
            }

            return false;
        });
    }

    /**
     * Checks for location permissions before fetching the current device position.
     */
    private void fetchCurrentLocation() {
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
     * Retrieves the last known location of the device and populates the coordinates.
     */
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationProviderClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken()
                )
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fillLocationFields(location);
                    } else {
                        fetchLastKnownLocationFallback();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                    fetchLastKnownLocationFallback();
                });
    }

    private void fetchLastKnownLocationFallback() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fillLocationFields(location);
                    } else {
                        Toast.makeText(
                                this,
                                "Current location unavailable. Enable GPS and try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show());
    }

    private void fillLocationFields(Location location) {
        etLatitude.setText(String.valueOf(location.getLatitude()));
        etLongitude.setText(String.valueOf(location.getLongitude()));
    }

    /**
     * Validates input fields and saves the new location to the database.
     * Also attempts to register a geofence for the new location.
     */
    private void saveLocation() {
        String name = etLocationName.getText().toString().trim();
        String latText = etLatitude.getText().toString().trim();
        String lngText = etLongitude.getText().toString().trim();
        String radiusText = etRadius.getText().toString().trim();

        if (name.isEmpty()) {
            etLocationName.setError("Enter location name");
            etLocationName.requestFocus();
            return;
        }

        if (latText.isEmpty()) {
            etLatitude.setError("Enter latitude");
            etLatitude.requestFocus();
            return;
        }

        if (lngText.isEmpty()) {
            etLongitude.setError("Enter longitude");
            etLongitude.requestFocus();
            return;
        }

        if (radiusText.isEmpty()) {
            etRadius.setError("Enter radius");
            etRadius.requestFocus();
            return;
        }

        try {
            double latitude = Double.parseDouble(latText);
            double longitude = Double.parseDouble(lngText);
            float radius = Float.parseFloat(radiusText);

            if (latitude < -90 || latitude > 90) {
                etLatitude.setError("Latitude must be between -90 and 90");
                etLatitude.requestFocus();
                return;
            }

            if (longitude < -180 || longitude > 180) {
                etLongitude.setError("Longitude must be between -180 and 180");
                etLongitude.requestFocus();
                return;
            }

            if (radius <= 0) {
                etRadius.setError("Radius must be greater than 0");
                etRadius.requestFocus();
                return;
            }

            SavedLocation savedLocation = new SavedLocation(name, latitude, longitude, radius);

            executorService.execute(() -> {
                appDatabase.savedLocationDao().insert(savedLocation);

                runOnUiThread(() -> {
                    GeofenceHelper.addGeofence(this, savedLocation, new GeofenceHelper.GeofenceResultCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AddLocationActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(AddLocationActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });

                    Toast.makeText(this, "Location saved successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Registers a geofence for the specified location.
     * @param location The SavedLocation for which to add a geofence.
     */
    private void addGeofence(SavedLocation location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        BACKGROUND_LOCATION_PERMISSION_REQUEST
                );
                Toast.makeText(this, "Grant background location for geofencing", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required for geofence", Toast.LENGTH_SHORT).show();
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(location.getName())
                .setCircularRegion(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getRadius()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Geofence added for " + location.getName(), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add geofence: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Creates a PendingIntent that will be triggered when a geofence transition occurs.
     * @return The PendingIntent for GeofenceBroadcastReceiver.
     */
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        return PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
    }

    /**
     * Clears all input fields in the activity.
     */
    private void clearFields() {
        etLocationName.setText("");
        etLatitude.setText("");
        etLongitude.setText("");
        etRadius.setText("");
    }

    /**
     * Shuts down the executor service when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    /**
     * Callback for the result from requesting permissions.
     * @param requestCode The request code.
     * @param permissions The requested permissions.
     * @param grantResults The grant results.
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

        if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Background location granted. Save location again to add geofence.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Background location denied. Geofencing may not work properly.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
