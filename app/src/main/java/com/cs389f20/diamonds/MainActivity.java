package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName(), SERIALIZABLE_KEY = "properties";
    public static final String EXTRA_PROPERTY = "com.cs389f20.diamonds.extra.PROPERTY";
    public static final int REFRESH_INTERVAL = 5;

    private HashMap<String, Property> properties;
    private DBManager db;
    private static MainActivity ma;
    private Handler handler;
    private Runnable dbUpdater;

    public MainActivity() {
        ma = this;
    }

    //TODO: store an array of ints in Building for last 24 hours. each value is either 10 or 5 minutes apart.
    //TODO: connection for 24hours

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        db = new DBManager(this);

        //if we are recreating a previous saved state (the back button on BuildingSelectActivity)
        if (savedInstanceState != null) {
            properties = (HashMap) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
            findViewById(R.id.loadBar).setVisibility(View.INVISIBLE);
            //need to update property selection buttons (not a priority as only one property right now)
        } else {
            properties = new HashMap<>();
            findViewById(R.id.propertySelectHeader).setVisibility(View.INVISIBLE);
            findViewById(R.id.loadBar).setVisibility(View.VISIBLE);
            //Connect to the database
            db.connectToDatabase();
            //Sets up the recurring task of getting updated values from the database every REFRESH_INTERVAL. Only called on first time starting the app.
            handler = new Handler(Looper.getMainLooper());
            dbUpdater = new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "Connecting to database and updating data");
                    db.connectToDatabase();
                    handler.postDelayed(this, TimeUnit.MINUTES.toMillis(REFRESH_INTERVAL));
                }
            };
            handler.postDelayed(dbUpdater, TimeUnit.MINUTES.toMillis(REFRESH_INTERVAL));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (properties != null)
            savedInstanceState.putSerializable(SERIALIZABLE_KEY, properties);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db.getQueue() != null)
            db.getQueue().cancelAll(this);
        handler.removeCallbacks(dbUpdater);
        db.destroyDBHandler();
    }

    public void storeData(JSONObject response) {
        Log.d(LOG_TAG, "CONNECTED TO DATABASE! :) Updating locally stored data");
        if (findViewById(R.id.loadBar).getVisibility() == View.VISIBLE) {
            findViewById(R.id.loadBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.propertySelectHeader).setVisibility(View.VISIBLE);
        }
        //connect to db and get all the prefixes (pace.miller)
        Iterator<String> keys = response.keys();
        String key, prop, building;
        int currentPeople;
        prop = "Pace"; //It will be difficult to implement the prefix system below. currently only buildings can be stored in database
        while (keys.hasNext()) {
            key = keys.next();
            //      prop = key.substring(0, key.indexOf("."));
            //     building = key.substring(key.indexOf("."));
            currentPeople = -1;
            building = key;
            try {
                currentPeople = response.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!properties.containsKey(prop))
                addProperty(prop);
            Property p = properties.get(prop);
            if (p != null)
                addOrUpdateBuilding(p, building, 1, ((currentPeople != -1) ? currentPeople : 0));
        }
        //   Log.d(LOG_TAG, "Update complete.");
        DrawButtons.drawButtons(properties.values().iterator(), (RelativeLayout) findViewById(R.id.propertySelectLayout));
    }

    private void addOrUpdateBuilding(Property prop, String building, int detectors, int current) {
        if (!prop.updateBuilding(building, current)) //already exists
            prop.addBuilding(new Building(building, prop, detectors, current));
        else
            prop.updateBuilding(building, current);
    }

    private void addProperty(String name) {
        Property prop = new Property(name);
        properties.put(name, prop);
    }

    public void launchBuildingSelectActivity(View v, String propertyName) {
        //   String propertyName = "Pace";
        // and use getContentDescription().toString() as propertyName
        Property property = properties.get(propertyName);
        if (property == null) {
            Log.e(LOG_TAG, "Trying to launch BuildingSelectActivity when propertyName of " + propertyName + " isn't part of properties map.");
            Toast.makeText(getApplicationContext(), "Error: Cannot find that property", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(LOG_TAG, "Property " + propertyName + " selected. Launching BuildingSelectActivity.");
        Intent intent = new Intent(this, BuildingSelectActivity.class);
        intent.putExtra(EXTRA_PROPERTY, property);

        startActivity(intent);
    }

    public DBManager getDatabase() {
        return db;
    }

    public static MainActivity getInstance() {
        return ma;
    }

    //returns the (updated) Building for a building in all properties
    public Building getBuilding(Building b) {
        for (Property p : properties.values()) {
            if (p.contains(b.name))
                return p.getBuilding(b.name);
        }
        return null;
    }

    public Property getProperty(Property p) {
        return properties.get(p.name);
    }
}
