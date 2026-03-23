package com.example.geo_tracker_app.ui.list;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geo_tracker_app.R;
import com.example.geo_tracker_app.data.db.AppDatabase;
import com.example.geo_tracker_app.model.SavedLocation;
import com.example.geo_tracker_app.utils.GeofenceHelper;
import com.example.geo_tracker_app.MainActivity;
import com.example.geo_tracker_app.ui.add.AddLocationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Displays all saved locations and allows navigation,
 * deletion, and geofence re-registration.
 */
public class LocationListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewLocations;
    private LocationAdapter adapter;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST = 4001;

    private SavedLocation pendingGeofenceLocation;

    /**
     * Initializes the activity, sets up the RecyclerView and bottom navigation.
     * @param savedInstanceState If the activity is being re-initialized then this Bundle contains the saved data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        recyclerViewLocations = findViewById(R.id.recyclerViewLocations);
        recyclerViewLocations.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LocationAdapter(
                new ArrayList<>(),
                this::deleteLocation,
                this::navigateToLocation,
                this::registerGeofenceAgain
        );

        recyclerViewLocations.setAdapter(adapter);

        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_list);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(LocationListActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(LocationListActivity.this, AddLocationActivity.class));
                return true;
            } else if (id == R.id.nav_list) {
                return true;
            }

            return false;
        });
    }

    /**
     * Refreshes the list of locations whenever the activity comes to the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadLocations();
    }

    /**
     * Loads all saved locations from the database and updates the UI.
     */
    private void loadLocations() {
        executorService.execute(() -> {
            List<SavedLocation> locations = appDatabase.savedLocationDao().getAllLocations();
            runOnUiThread(() -> adapter.setLocationList(locations));
        });
    }

    /**
     * Deletes a specific location from the database.
     * @param location The SavedLocation object to delete.
     */
    private void deleteLocation(SavedLocation location) {
        executorService.execute(() -> {
            appDatabase.savedLocationDao().delete(location);
            runOnUiThread(() -> {
                Toast.makeText(this, "Location deleted", Toast.LENGTH_SHORT).show();
                loadLocations();
            });
        });
    }

    /**
     * Opens Google Maps or a browser to navigate to the specified location.
     * @param location The SavedLocation object containing coordinates.
     */
    private void navigateToLocation(SavedLocation location) {
        String uri = "google.navigation:q=" + location.getLatitude() + "," + location.getLongitude();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            String browserUri = "https://www.google.com/maps/dir/?api=1&destination="
                    + location.getLatitude() + "," + location.getLongitude();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

    /**
     * Re-registers a geofence for an existing saved location.
     * @param location The SavedLocation object for which to add a geofence.
     */
    private void registerGeofenceAgain(SavedLocation location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                pendingGeofenceLocation = location;

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        BACKGROUND_LOCATION_PERMISSION_REQUEST
                );

                Toast.makeText(
                        this,
                        "Please allow background location to enable geofencing.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
        }

        GeofenceHelper.addGeofence(this, location, new GeofenceHelper.GeofenceResultCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(LocationListActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(LocationListActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shuts down the executor service when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Background location granted", Toast.LENGTH_SHORT).show();

                if (pendingGeofenceLocation != null) {
                    GeofenceHelper.addGeofence(this, pendingGeofenceLocation, new GeofenceHelper.GeofenceResultCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(LocationListActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(LocationListActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Toast.makeText(
                        this,
                        "Background location denied. Geofencing will not work in the background.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
