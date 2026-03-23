package com.example.geo_tracker_app.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.geo_tracker_app.data.dao.SavedLocationDao;
import com.example.geo_tracker_app.model.SavedLocation;

/**
 * Main database class for the application, using Room persistence library.
 * Defines the database configuration and serves as the main access point for the persisted data.
 */
@Database(entities = {SavedLocation.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Provides access to the SavedLocationDao.
     * @return The Data Access Object for saved locations.
     */
    public abstract SavedLocationDao savedLocationDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * Gets the singleton instance of the AppDatabase.
     * @param context The application context.
     * @return The AppDatabase instance.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "geo_tracker_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
