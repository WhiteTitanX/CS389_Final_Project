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

    //TODO: fix image being wrong size

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        findViewById(R.id.propertySelectHeader).setVisibility(View.INVISIBLE);
        findViewById(R.id.loadBar).setVisibility(View.VISIBLE);

        //if we are recreating a previous saved state (the back button on BuildingSelectActivity)
        if (savedInstanceState != null) {
            try {
                properties = (HashMap<String, Property>) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
            } catch (ClassCastException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error: Can't get properties map from cache!");
                finish();
            }

            if (db == null)
                db = new DBManager(this);
        } else {
            new OccupancyAlertManager();
            db = new DBManager(this);
            properties = new HashMap<>();
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

    private void showButtons() {
        Log.d(LOG_TAG, "Done connecting to database. Displaying buttons");
        Runnable buttons = new Runnable() {
            @Override
            public void run() {
                //Update UI (buttons)
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
                    DrawButtons.drawButtons(properties.values().iterator(), (LinearLayout) findViewById(R.id.propertyScrollLayout));
                }
            }
        };
        handler.post(buttons);
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

    public void storeData(final JSONObject stringResponse, final JSONArray arrayResponse, final DBManager.DataType type) {
        Log.d(LOG_TAG, "Updating or creating locally stored data for " + type);
        Runnable main = new Runnable() {
            @Override
            public void run() {
                try {
                    if (type == DBManager.DataType.INFO)
                        storeInfo(stringResponse);
                    else if (type == DBManager.DataType.CURRENT)
                        storeCurrent(stringResponse);
                    else if (type == DBManager.DataType.PAST) {
                        storePast(arrayResponse);
                        showButtons();
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Value fetch: " + Objects.requireNonNull(e.getMessage()));
                    Toast.makeText(getApplicationContext(), "Note: an error occurred trying to get data from database", Toast.LENGTH_LONG).show();
                }
            }
        };
        handler.post(main);
    }

    private void storeInfo(JSONObject response) throws JSONException {
        String property, building, image, id;
        int capacity, notificationIndex = 0;
        property = response.getString("site");
        Property p = addProperty(property);
        for (Iterator<String> it = response.getJSONObject("locations").keys(); it.hasNext(); ) {
            id = it.next();
            building = response.getJSONObject("locations").getString(id);
            capacity = response.getJSONObject("capacities").getInt(id);
            image = response.getJSONObject("images").getString(id);
            addOrUpdateBuilding(p, id, building, -1, capacity, notificationIndex++, null, null, image);
        }
    }

    private void storeCurrent(JSONObject response) throws JSONException {
        Iterator<String> keys = response.keys();
        int currentPeople;
        Building b;
        while (keys.hasNext()) {
            String id = keys.next();
            b = getBuilding(id);
            if (b == null)
                continue;
            currentPeople = response.getInt(id);
            addOrUpdateBuilding(b.property, b.id, b.name, currentPeople, b.maxOccupancy, b.notificationID, null, null, b.image_url);
        }
    }

    private void storePast(JSONArray response) throws JSONException {
        Iterator<String> keys = null;
        for (int i = 0; i < response.length(); i++)
            if (response.getJSONObject(i).getJSONObject("data").length() != 0) {
                keys = response.getJSONObject(1).getJSONObject("data").keys();
                break;
            }
        if (keys == null)
            return;
        int[] pastPeople;
        String[] times = null;
        Building b;
        while (keys.hasNext()) {
            String id = keys.next();
            b = getBuilding(id);
            if (b == null)
                continue;
            if (times == null)
                times = new String[response.length()];
            pastPeople = new int[response.length()];
            for (int i = 0; i < response.length(); i++) {
                times[i] = response.getJSONObject(i).getString("timestamp");
                if (response.getJSONObject(i).getJSONObject("data").length() == 0 || !response.getJSONObject(i).getJSONObject("data").has(b.id))
                    pastPeople[i] = -1;
                else
                    pastPeople[i] = response.getJSONObject(i).getJSONObject("data").getInt(b.id);
            }
            addOrUpdateBuilding(b.property, b.id, b.name, -1, b.maxOccupancy, b.notificationID, pastPeople, times, b.image_url);
        }
    }

    private void addOrUpdateBuilding(Property prop, String id, String building, int current, int max, int notifyID, int[] past, String[] times, String image) {
        if (!prop.updateBuilding(id, current, past, times)) //already exists
            prop.addBuilding(new Building(id, building, prop, 1, current, max, notifyID, past, times, image));
    }

    private Property addProperty(String name) {
        Property prop = new Property(name);
        properties.put(name, prop);
        return prop;
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
            if (p.contains(b.id))
                return p.getBuilding(b.name);
        }
        return null;
    }

    private Building getBuilding(String id) {
        for (Property p : properties.values()) {
            if (p.contains(id))
                return p.getBuildingFromID(id);
        }
        return null;
    }

    public Property getProperty(Property p) {
        return properties.get(p.name);
    }
}
