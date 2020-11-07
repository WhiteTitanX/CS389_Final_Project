package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
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

    //TODO: fix landscape mode on BuildingActivity (or lock to portrait)
    //TODO: add scrollable for BuildingSelectActivity activity?

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DBManager(this);

        //if we are recreating a previous saved state (the back button on BuildingSelectActivity)
        if (savedInstanceState != null) {
            try {
                properties = (HashMap<String, Property>) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
            } catch (ClassCastException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error: Can't get properties map from cache!");
                finish();
            }

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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
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

    public void storeData(JSONObject stringResponse, JSONArray arrayResponse) {
        Log.d(LOG_TAG, "CONNECTED TO DATABASE! :) Updating locally stored data for " + ((stringResponse != null) ? "current count" : "past count"));
        Iterator<String> keys = null;
        //Get a list of all buildings
        if (stringResponse != null)
            keys = stringResponse.keys();
        else {
            try {
                for (int i = 0; i < arrayResponse.length(); i++)
                    if (arrayResponse.getJSONObject(i).getJSONObject("data").length() != 0) {
                        keys = arrayResponse.getJSONObject(1).getJSONObject("data").keys();
                        break;
                    }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Keys fetch: " + Objects.requireNonNull(e.getMessage()));
                return;
            }
        }

        String prop, building;
        int currentPeople;
        int[] pastPeople;
        String[] times = null;
        prop = "Pace"; //It will be difficult to implement the prefix system. currently only buildings can be stored in database
        assert keys != null;
        while (keys.hasNext()) {
            building = keys.next();
            currentPeople = -1;
            pastPeople = null;
            try {
                if (stringResponse != null)
                    currentPeople = stringResponse.getInt(building);
                else {
                    if (times == null)
                        times = new String[arrayResponse.length()];
                    pastPeople = new int[arrayResponse.length()];
                    for (int i = 0; i < arrayResponse.length(); i++) {
                        times[i] = arrayResponse.getJSONObject(i).getString("timestamp");
                        if (arrayResponse.getJSONObject(i).getJSONObject("data").length() == 0 || !arrayResponse.getJSONObject(i).getJSONObject("data").has(building))
                            pastPeople[i] = -1;
                        else
                            pastPeople[i] = arrayResponse.getJSONObject(i).getJSONObject("data").getInt(building);
                    }

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Value fetch: " + Objects.requireNonNull(e.getMessage()));
                Toast.makeText(getApplicationContext(), "Note: an error occurred trying to get data from database", Toast.LENGTH_LONG).show();
            }
            if (!properties.containsKey(prop))
                addProperty(prop);
            Property p = properties.get(prop);
            if (p != null)
                addOrUpdateBuilding(p, building, currentPeople, pastPeople, times);
        }

        if (findViewById(R.id.loadBar).getVisibility() == View.VISIBLE) {
            findViewById(R.id.loadBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.propertySelectHeader).setVisibility(View.VISIBLE);
        }
        if (!properties.values().iterator().hasNext())
            findViewById(R.id.textNoProperties).setVisibility(View.VISIBLE);
        else {
            if (findViewById(R.id.textNoProperties).getVisibility() == View.VISIBLE)
                findViewById(R.id.textNoProperties).setVisibility(View.INVISIBLE);
            DrawButtons.drawButtons(properties.values().iterator(), (RelativeLayout) findViewById(R.id.propertySelectLayout));
        }

    }

    private void addOrUpdateBuilding(Property prop, String building, int current, int[] past, String[] times) {
        if (!prop.updateBuilding(building, current, past, times)) //already exists
            prop.addBuilding(new Building(building, prop, 1, current, past, times));
    }

    private void addProperty(String name) {
        Property prop = new Property(name);
        properties.put(name, prop);
    }

    public void launchBuildingSelectActivity(String propertyName) {
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
