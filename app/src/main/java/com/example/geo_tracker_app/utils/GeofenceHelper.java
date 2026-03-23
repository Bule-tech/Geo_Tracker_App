package com.example.geo_tracker_app.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.geo_tracker_app.model.SavedLocation;
import com.example.geo_tracker_app.receiver.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Utility class for creating and registering geofences.
 */
public class GeofenceHelper {

    /**
     * Interface for geofence registration result callbacks.
     */
    public interface GeofenceResultCallback {
        /**
         * Called when geofence registration is successful.
         * @param message Success message.
         */
        void onSuccess(String message);

        /**
         * Called when geofence registration fails.
         * @param message Failure message.
         */
        void onFailure(String message);
    }

    /**
     * Adds a geofence for the specified location.
     * Checks for necessary permissions and handles the registration with GeofencingClient.
     * @param context The application context.
     * @param location The SavedLocation object.
     * @param callback The callback to handle success or failure.
     */
    public static void addGeofence(Context context,
                                   SavedLocation location,
                                   GeofenceResultCallback callback) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onFailure("Fine location permission not granted");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                callback.onFailure("Background location permission not granted");
                return;
            }
        }

        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);

        Geofence geofence = new Geofence.Builder()
                .setRequestId(location.getName())
                .setCircularRegion(location.getLatitude(), location.getLongitude(), location.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(unused -> callback.onSuccess("Geofence added for " + location.getName()))
                .addOnFailureListener(e -> callback.onFailure("Failed to add geofence: " + e.getMessage()));
    }
}
