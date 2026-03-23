package com.example.geo_tracker_app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a location saved by the user with geofencing details.
 * This class is a Room entity corresponding to the "saved_locations" table.
 */
@Entity(tableName = "saved_locations")
public class SavedLocation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private double latitude;
    private double longitude;
    private float radius;

    /**
     * Constructs a new SavedLocation.
     * @param name Name of the location.
     * @param latitude Latitude coordinate.
     * @param longitude Longitude coordinate.
     * @param radius Geofence radius in meters.
     */
    public SavedLocation(String name, double latitude, double longitude, float radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    /**
     * Gets the unique ID of the saved location.
     * @return The ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique ID of the saved location.
     * @param id The ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the saved location.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the latitude of the saved location.
     * @return The latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude of the saved location.
     * @return The longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Gets the radius of the geofence for this location.
     * @return The radius in meters.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the name of the saved location.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the latitude of the saved location.
     * @param latitude The latitude to set.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets the longitude of the saved location.
     * @param longitude The longitude to set.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the radius of the geofence for this location.
     * @param radius The radius to set in meters.
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }
}
