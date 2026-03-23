package com.example.geo_tracker_app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.geo_tracker_app.utils.NotificationHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Receives geofence transition events and displays notifications
 * when the user enters a saved geofence area.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * Processes geofence transition events and triggers notifications.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error code: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String locationName = "Saved Location";

            if (triggeringGeofences != null && !triggeringGeofences.isEmpty()) {
                locationName = triggeringGeofences.get(0).getRequestId();
            }

            NotificationHelper.showNotification(
                    context,
                    "Geofence Alert",
                    "You have entered: " + locationName
            );
        }
    }
}
