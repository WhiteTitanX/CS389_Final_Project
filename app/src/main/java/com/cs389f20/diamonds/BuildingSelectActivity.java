package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class BuildingSelectActivity extends AppCompatActivity {
    private static final String LOG_TAG = BuildingSelectActivity.class.getSimpleName(), SERIALIZABLE_KEY = "property";
    public static final String EXTRA_BUILDING = "com.cs389f20.diamonds.extra.BUILDING";
    public static final int TEXT_REQUEST = 1;
    private Property property;
    private static BuildingSelectActivity bsa;

    public BuildingSelectActivity() {
        bsa = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_select);

        //if we are recreating a previous saved state (the back button on MainActivity)
        if (savedInstanceState != null) {
            property = (Property) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
            //Test if an update in the database has happened
            MainActivity ma = MainActivity.getInstance();
            long time = TimeUnit.MILLISECONDS.toMinutes(ma.getDatabase().getLastUpdated());
            if (time < 1) //just refreshed. get the updated count
            {
                Log.d(LOG_TAG, "Updating list of buildings for selection for " + property.name);
                property = ma.getProperty(property);
                if (property == null)
                    finish(); //the building was removed from the database
            }
        } else {
            Intent intent = getIntent();
            property = (Property) intent.getSerializableExtra(MainActivity.EXTRA_PROPERTY);
        }
        if (property == null) {
            Log.e(LOG_TAG, "Property is null in onCreate");
            Toast.makeText(getApplicationContext(), "Error: Cannot get property data", Toast.LENGTH_LONG).show();
            return;
        }
        setTitle(property.name);

        updateButtons();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (property != null)
            savedInstanceState.putSerializable(SERIALIZABLE_KEY, property);
    }

    //If we use the top left arrow or the bottom left arrow, we want to save the state
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchBuildingActivity(View v, String buildingName) {
        if (!(v instanceof ImageButton)) {
            Log.e(LOG_TAG, "Trying to launch MainActivity when no ImageButton on BuildingSelectActivity was clicked");
            Toast.makeText(getApplicationContext(), "Error: Can't open building info (MainActivity) without clicking a button", Toast.LENGTH_LONG).show();
            return;
        }
        Building building = property.getBuilding(buildingName);
        if (building == null) {
            Log.e(LOG_TAG, "Trying to launch MainActivity when buildingName of " + buildingName + " isn't part of buildings list in property " + property.name);
            Toast.makeText(getApplicationContext(), "Error: Cannot find that building in that property", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(LOG_TAG, "Building (name: " + buildingName + ") selected. Launching " + BuildingActivity.class.getSimpleName() + ".");
        Intent intent = new Intent(this, BuildingActivity.class);
        intent.putExtra(EXTRA_BUILDING, building);

        startActivity(intent);
    }

    public static BuildingSelectActivity getInstance() {
        return bsa;
    }

    public void updateButtons() {
        DrawButtons.drawButtons(property.getBuildings().iterator(), (RelativeLayout) findViewById(R.id.buildingSelectLayout));
    }
}