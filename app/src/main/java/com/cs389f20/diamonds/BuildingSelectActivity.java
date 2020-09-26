package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class BuildingSelectActivity extends AppCompatActivity {
    private static final String LOG_TAG = BuildingSelectActivity.class.getSimpleName(), SERIALIZABLE_KEY = "property";
    public static final String EXTRA_BUILDING = "com.cs389f20.diamonds.extra.BUILDING";
    public static final int TEXT_REQUEST = 1;
    private Property property;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_select);

        //if we are recreating a previous saved state (the back button on MainActivity)
        if (savedInstanceState != null) {
            property = (Property) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
        } else {
            Intent intent = getIntent();
            property = (Property) intent.getSerializableExtra(PropertySelectActivity.EXTRA_PROPERTY);
        }
        if (property == null) {
            Log.e(LOG_TAG, "Property is null in onCreate");
            Toast.makeText(getApplicationContext(), "Error: Cannot get property data", Toast.LENGTH_LONG).show();
            return;
        }
        setTitle(property.name);

        List<Building> buildings = property.getBuildings();
        //TODO: create a button (with contentDescription of building name) for each building in buildings list

        Log.d(LOG_TAG, "-------");
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (property != null)
            savedInstanceState.putSerializable(SERIALIZABLE_KEY, property);
    }

    //If we use the top left arrow or the bottom left arrow, we want to save the state
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchMainActivity(View v) {
        if (!(v instanceof ImageButton)) {
            Log.e(LOG_TAG, "Trying to launch MainActivity when no ImageButton on BuildingSelectActivity was clicked");
            Toast.makeText(getApplicationContext(), "Error: Can't open building info (MainActivity) without clicking a button", Toast.LENGTH_LONG).show();
            return;
        }
        ImageButton button;
        button = (ImageButton) v;
        if (button.getContentDescription() == null || button.getContentDescription().toString().isEmpty()) {
            Log.e(LOG_TAG, "Trying to launch MainActivity when ContentDescription of ImageButton (id: " + button.getId() + ") on BuildingSelectActivity is null or blank");
            Toast.makeText(getApplicationContext(), "Error: No building assigned to that button", Toast.LENGTH_LONG).show();
            return;
        }
        String buildingName = button.getContentDescription().toString();
        Building building = property.getBuilding(buildingName);
        if(building == null) {
            Log.e(LOG_TAG, "Trying to launch MainActivity when buildingName of " + buildingName + " isn't part of buildings list in property " + property.name);
            Toast.makeText(getApplicationContext(), "Error: Cannot find that building in that property", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(LOG_TAG, "Building (name: " + buildingName + ") selected. Launching MainActivity.");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_BUILDING, building);

        startActivity(intent);
    }
}