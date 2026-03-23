package com.example.geo_tracker_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.geo_tracker_app.data.dao.SavedLocationDao;
import com.example.geo_tracker_app.data.db.AppDatabase;
import com.example.geo_tracker_app.model.SavedLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

/**
 * DAO for inserting, deleting, and retrieving saved locations.
 */

@RunWith(AndroidJUnit4.class)
public class SavedLocationDaoTest {

    private AppDatabase db;
    private SavedLocationDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.savedLocationDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertLocation_andReadAll() {
        SavedLocation location = new SavedLocation("Home", -17.8252, 31.0335, 200f);
        dao.insert(location);

        List<SavedLocation> locations = dao.getAllLocations();

        assertEquals(1, locations.size());
        assertEquals("Home", locations.get(0).getName());
    }

    @Test
    public void deleteLocation_removesItem() {
        SavedLocation location = new SavedLocation("School", -17.8, 31.0, 150f);
        dao.insert(location);

        List<SavedLocation> beforeDelete = dao.getAllLocations();
        assertTrue(beforeDelete.size() > 0);

        dao.delete(beforeDelete.get(0));

        List<SavedLocation> afterDelete = dao.getAllLocations();
        assertEquals(0, afterDelete.size());
    }
}