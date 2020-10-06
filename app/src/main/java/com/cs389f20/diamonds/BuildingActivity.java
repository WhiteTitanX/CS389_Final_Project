package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
/*
Displays the building info screen
 */
public class BuildingActivity extends AppCompatActivity {
    private static final String LOG_TAG = BuildingActivity.class.getSimpleName(), SERIALIZABLE_KEY = "building";
    private Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);

        //if we are recreating a previous saved state (the back button)
        if (savedInstanceState != null) {
            building = (Building) savedInstanceState.getSerializable(SERIALIZABLE_KEY);
        } else {
            Intent intent = getIntent();
            building = (Building) intent.getSerializableExtra(BuildingSelectActivity.EXTRA_BUILDING);
        }
        if (building == null) {
            Log.e(LOG_TAG, "Building is null in onCreate");
            Toast.makeText(getApplicationContext(), "Error: Cannot get building data", Toast.LENGTH_LONG).show();
            return;
        }
        setTitle(building.name);

        //Update/display info
        TextView header = findViewById(R.id.textHeader);
        header.setText(getString(R.string.building_info_header, building.name));
        TextView people = findViewById(R.id.textAmount);
        people.setText(getString(R.string.amount_initial_value, building.currentNumberOfPeople));
        //TODO: should we updated info from db, instead of using the cache?

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (building != null)
            savedInstanceState.putSerializable(SERIALIZABLE_KEY, building);
    }

    //For when we are clicking the options back arrow (top left) on a activity with a parent, as it won't properly save the state on onCreate() w/o this
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchHistoryActivity(View v) {
        Log.d(LOG_TAG, "History button pressed. Launching HistoryActivity.");
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, building.name);
        startActivity(intent);
    }

    public void launchGraphActivity(View v) {
        Log.d(LOG_TAG, "Graph button pressed. Launching GraphActivity.");
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, building.name);
        startActivity(intent);
    }

    public void refreshCount(View v) {
        //for debug purposes. will scrap when we get it auto updating
        Toast.makeText(getApplicationContext(), "Updated to latest count", Toast.LENGTH_SHORT).show();

    }
}