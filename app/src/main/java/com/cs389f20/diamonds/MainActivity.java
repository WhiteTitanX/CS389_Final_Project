package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName(), SERIALIZABLE_KEY = "properties";
    public static final String EXTRA_PROPERTY = "com.cs389f20.diamonds.extra.PROPERTY";
    public static final int TEXT_REQUEST = 1;

    private HashMap<String, Property> properties;
    private DBManager db;
    private static MainActivity ma;

    public MainActivity()
    {
        ma = this;
    }

    //TODO: Loading Screen (?)
    //TODO: Can't connect to internet screen/error message (already in onError in DBManager?)
    //TODO: cache DatabaseManager (like props map? ) ?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        db = new DBManager(this);

        //if we are recreating a previous saved state (the back button on BuildingSelectActivity)
        if (savedInstanceState != null) {
            properties = (HashMap) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
            //TODO: see if the database has been updated. should we store JSONObject in savedInstance, then test to see if new one is equal to old?

        } else {
            properties = new HashMap<>();
            db.connectToDatabase();
        }
     //   DrawButtons.drawButtons(properties.values().iterator(), (RelativeLayout) findViewById(R.id.propertySelectLayout));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (properties != null)
            savedInstanceState.putSerializable(SERIALIZABLE_KEY, properties);
    }



    public void storeData(JSONObject response) {
        Log.d(LOG_TAG, "CONNECTED TO DATABASE! :)");

        //connect to db and get all the prefixes (pace.miller)
        Iterator<String> keys = response.keys();
        String key, prop, building;
        int currentPeople;
        while (keys.hasNext()) {
            key = keys.next();
      //      prop = key.substring(0, key.indexOf("."));
      //     building = key.substring(key.indexOf("."));
            currentPeople = -1;

            //DEBUG <<<<<<<<<<<<<<>>>>>>>>>>>>>>>>
            prop = "Pace";
            building = key;
            try { currentPeople = response.getInt(key);} catch (JSONException e) { e.printStackTrace(); }

            if (!properties.containsKey(prop))
                addProperty(prop);
            Property p = properties.get(prop);
            if (p != null)
                addBuilding(p, building, 1, ((currentPeople != -1) ? currentPeople : 0));
        }


        DrawButtons.drawButtons(properties.values().iterator(), (RelativeLayout) findViewById(R.id.propertySelectLayout));
        Log.d(LOG_TAG, "Done connecting. drawing buttons. buildings in pro size is " + properties.values().size());
    }

    private void addBuilding(Property prop, String building, int detectors, int current) {
        if (!prop.updateBuilding(building, current)) //already exists
            prop.addBuilding(new Building(building, prop, detectors, current));
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
        Log.d(LOG_TAG, "Property (name: " + propertyName + ") selected. Launching BuildingSelectActivity.");
        Intent intent = new Intent(this, BuildingSelectActivity.class);
        intent.putExtra(EXTRA_PROPERTY, property);

        startActivity(intent);
        //  startActivityForResult(intent, TEXT_REQUEST); //for returning data back to this activity
    }

    public DBManager getDatabase()
    {
        return db;
    }

    public static MainActivity getInstance()
    {
        return ma;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db.getQueue() != null) {
            db.getQueue().cancelAll(this);
        }
    }
}
