package com.example.geo_tracker_app.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.geo_tracker_app.model.SavedLocation;

import java.util.List;

/**
 * Data Access Object (DAO) for the SavedLocation entity.
 * Provides methods for database operations on saved locations.
 */
@Dao
public interface SavedLocationDao {

    /**
     * Inserts a new location into the database.
     * @param location The SavedLocation object to insert.
     */
    @Insert
    void insert(SavedLocation location);

    /**
     * Deletes a location from the database.
     * @param location The SavedLocation object to delete.
     */
    @Delete
    void delete(SavedLocation location);

    /**
     * Retrieves all saved locations from the database, ordered by ID in descending order.
     * @return A list of all saved locations.
     */
    @Query("SELECT * FROM saved_locations ORDER BY id DESC")
    List<SavedLocation> getAllLocations();
}
