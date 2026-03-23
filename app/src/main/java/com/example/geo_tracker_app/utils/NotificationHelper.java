package com.example.geo_tracker_app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.geo_tracker_app.MainActivity;
import com.example.geo_tracker_app.R;

/**
 * Utility class for creating notification channels
 * and displaying geofence alerts.
 */
public class NotificationHelper {

    /**
     * ID for the notification channel used by this app.
     */
    public static final String CHANNEL_ID = "geo_tracker_channel";

    /**
     * User-visible name for the notification channel.
     */
    public static final String CHANNEL_NAME = "Geo Tracker Alerts";

    /**
     * Creates a notification channel for Android O and above.
     * @param context The application context.
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for geofence entry alerts");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Displays a notification to the user.
     * @param context The application context.
     * @param title The title of the notification.
     * @param message The body text of the notification.
     */
    public static void showNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
