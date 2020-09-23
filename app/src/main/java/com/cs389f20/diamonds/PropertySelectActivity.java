package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertySelectActivity extends AppCompatActivity {
    private static final String LOG_TAG =  PropertySelectActivity.class.getSimpleName();
    public static final String EXTRA_PROPERTY =   "com.cs389f20.diamonds.extra.PROPERTY";
    public static final int TEXT_REQUEST = 1;

    private HashMap<String, Property> properties = new HashMap<>();
    private List<Building> buildings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_select);
        getSupportActionBar().hide();


        //TODO: connect to database

        setupMaps(); //should we send the data via the intents? state might be destroyed otherwise. or get the data on each call, and dont cache it
        //TODO: if user comes back to this activity and there is an update to the db (a new property or building), when need to update the map / property object


    }

    private void setupMaps() {
        int propertiesCount = 5; //get from db
        int buildingsCount, detectors, currentPeople;
        String propertyName, buildingName;
        Property prop;
        for (int i = 0; i < propertiesCount; i++) {
            propertyName = "nameOfProperty [" + i + "]"; //get from db
            buildingsCount = i + 5; //get from db
            prop = new Property(propertyName, buildingsCount);
            properties.put(propertyName, prop);
            for (int j = 0; j < buildingsCount; j++) {
                buildingName = ""; //get from db
                detectors = 0; //get from db
                currentPeople = 0; //get from db
                buildings.add(new Building(buildingName, prop, detectors, currentPeople));
            }
        }
    }

    public void launchBuildingSelectActivity(View v)
    {
        Log.d(LOG_TAG, "Property selected. Launching BuildingSelectActivity.");
        Intent intent = new Intent(this, BuildingSelectActivity.class);
     //   String message = editTextMessage.getText().toString();
        String message = "Pace University"; //TODO: get selection from property activity (on click?) as the message
        //TODO: do we send an array with all the buildings (multiple arrays)? a serializable set of data?
        intent.putExtra(EXTRA_PROPERTY, message);

        startActivity(intent);
      //  startActivityForResult(intent, TEXT_REQUEST); //for returning data back to this activity
    }
}