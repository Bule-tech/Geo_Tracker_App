package com.example.geo_tracker_app;

import static org.junit.Assert.assertEquals;

import com.example.geo_tracker_app.model.SavedLocation;

import org.junit.Test;

/**
 * Entity class representing a saved GPS location.
 * Stores the location name, coordinates, and geofence radius.
 */

public class SavedLocationTest {

    @Test
    public void savedLocation_storesCorrectValues() {
        SavedLocation location = new SavedLocation("Office", -17.81, 31.04, 120f);

        assertEquals("Office", location.getName());
        assertEquals(-17.81, location.getLatitude(), 0.001);
        assertEquals(31.04, location.getLongitude(), 0.001);
        assertEquals(120f, location.getRadius(), 0.001f);
    }
}