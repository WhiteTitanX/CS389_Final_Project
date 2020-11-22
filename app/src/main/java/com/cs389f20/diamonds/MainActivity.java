package com.cs389f20.diamonds;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
    public static final String LOG_TAG = MainActivity.class.getSimpleName(), SERIALIZABLE_KEY = "properties",
            EXTRA_PROPERTY = "com.cs389f20.diamonds.extra.PROPERTY", NOTIFICATION_CHANNEL = "ENTRY_TRACK_CHANNEL";
    public static final int REFRESH_INTERVAL = 5;

    private HashMap<String, Property> properties;
    private DBManager db;
    private static MainActivity ma;
    private Handler handler;
    private Runnable dbUpdater;

    public MainActivity() {
        ma = this;
    }

    //TODO: get max capacity from db

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

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
            if (db == null)
                db = new DBManager(this);

        } else {
            new OccupancyAlertManager();
            db = new DBManager(this);
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
            createNotificationChannel();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CHANNEL_ENTRYTRACK";
            String description = "Notifications for EntryTrack";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; don't change any notification behaviours or importance afterwards
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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
        if (dbUpdater != null)
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
        int currentPeople, maxPeople, notificationIndex = 0;
        int[] pastPeople;
        String[] times = null;
        prop = "Pace"; //It will be difficult to implement the prefix system. currently only buildings can be stored in database
        assert keys != null;
        while (keys.hasNext()) {
            building = keys.next();
            currentPeople = -1;
            maxPeople = 250;
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
                addOrUpdateBuilding(p, building, currentPeople, maxPeople, notificationIndex++, pastPeople, times);
        }

        if (findViewById(R.id.loadBar).getVisibility() == View.VISIBLE) {
            findViewById(R.id.loadBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.propertySelectHeader).setVisibility(View.VISIBLE);
        }
        if (!properties.values().iterator().hasNext()) {
            findViewById(R.id.textNoProperties).setVisibility(View.VISIBLE);
            findViewById(R.id.propertyScrollLayout).setVisibility(View.INVISIBLE);
        } else {
            if (findViewById(R.id.textNoProperties).getVisibility() == View.VISIBLE) {
                findViewById(R.id.textNoProperties).setVisibility(View.INVISIBLE);
                findViewById(R.id.propertyScrollLayout).setVisibility(View.VISIBLE);
            }
            if (arrayResponse == null)
                DrawButtons.drawButtons(properties.values().iterator(), (LinearLayout) findViewById(R.id.propertyScrollLayout));
        }

    }

    private void addOrUpdateBuilding(Property prop, String building, int current, int max, int notifyID, int[] past, String[] times) {
        if (!prop.updateBuilding(building, current, past, times)) //already exists
            prop.addBuilding(new Building(building, prop, 1, current, max, notifyID, past, times));
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
