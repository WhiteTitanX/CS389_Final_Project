package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PropertySelectActivity extends AppCompatActivity {
    private static final String LOG_TAG = PropertySelectActivity.class.getSimpleName(), SERIALIZABLE_KEY = "properties";
    public static final String EXTRA_PROPERTY = "com.cs389f20.diamonds.extra.PROPERTY";
    public static final int TEXT_REQUEST = 1;

    private HashMap<String, Property> properties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_select);
        getSupportActionBar().hide();

        //if we are recreating a previous saved state (the back button on BuildingSelectActivity)
        if (savedInstanceState != null) {
            properties = (HashMap) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
            //TODO: see if the database has been updated. if so, connect to database and call setupPropertyMap();

        } else {
            properties = new HashMap<>();
            Log.d(LOG_TAG, "Creating new properties HashMap");
            //TODO: connect to database
            setupPropertyMap();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (properties != null)
            savedInstanceState.putSerializable(SERIALIZABLE_KEY, properties);
    }

    private void setupPropertyMap() {
        int propertiesCount = 5; //get from db
        int buildingsCount, detectors, currentPeople;
        String propertyName, buildingName;
        Property property;
        List<Building> buildings;
        for (int i = 0; i < propertiesCount; i++) {
            propertyName = "nameOfProperty [" + i + "]"; //get from db
            buildingsCount = i + 5; //get from db
            property = new Property(propertyName, buildingsCount);
            buildings = new ArrayList<>();
            for (int j = 0; j < buildingsCount; j++) {
                buildingName = ""; //get from db

                //debug
                if(j == 1)
                    buildingName = "Miller";

                detectors = 0; //get from db
                currentPeople = 0; //get from db
                buildings.add(new Building(buildingName, property, detectors, currentPeople));
            }
            property.addBuildings(buildings);
            properties.put(propertyName, property);
        }
    }

    public void launchBuildingSelectActivity(View v) {
        String propertyName = "nameOfProperty [0]"; //TODO: get selection from this activity (from ImageButton, dropdown?)
        // and use getContentDescription().toString() as propertyName
        Property property = properties.get(propertyName);
        if (property == null) {
            Log.e(LOG_TAG, "Trying to launch BuildingSelectActivity when propertyName of " + propertyName + " isn't part of properties map.");
            Toast.makeText(getApplicationContext(), "Error: Cannot find that property", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(LOG_TAG, "Property (name: " + propertyName + ") selected. Launching BuildingSelectActivity.");
        Intent intent = new Intent(this, BuildingSelectActivity.class);
        intent.putExtra(EXTRA_PROPERTY, property);

        startActivity(intent);
        //  startActivityForResult(intent, TEXT_REQUEST); //for returning data back to this activity
    }
}