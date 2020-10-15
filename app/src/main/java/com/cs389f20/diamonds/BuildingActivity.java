package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/*
Displays the building info screen
 */
public class BuildingActivity extends AppCompatActivity {
    private static final String LOG_TAG = BuildingActivity.class.getSimpleName(), SERIALIZABLE_KEY = "building";
    private Building building;
    private boolean isRunning;
    private Handler handler;
    private Runnable lastUpdateTask;

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

        //Display info (from cache)
        displayNameAndAmount();

        //Set recurring task to update "last updated" every minute and update count if a database refresh happens
        handler = new Handler(Looper.getMainLooper());
        lastUpdateTask = new Runnable() {
            @Override
            public void run() {
                updateAmountAndLastUpdated();
                handler.postDelayed(this, TimeUnit.MINUTES.toMillis(1));
            }
        };
        handler.post(lastUpdateTask);
        //Note: if we just launched the app, we will update the building again in updateAAndLU even though we don't need to.
        //this really isn't a big problem, just worth noting
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

    private void displayNameAndAmount() {
        displayName();
        displayAmount();
    }

    private void displayName() {
        TextView header = findViewById(R.id.textHeader);
        header.setText(getString(R.string.building_info_header, building.name));
    }

    private void displayAmount() {
        TextView people = findViewById(R.id.textAmount);
        people.setText(getString(R.string.people_amount, building.currentNumberOfPeople));
    }

    public void updateAmountAndLastUpdated() {
        Log.d(LOG_TAG, "Updating last updated for " + building.name);
        TextView lastUpdated = findViewById(R.id.textlastUpdated);
        MainActivity ma = MainActivity.getInstance();
        long time = TimeUnit.MILLISECONDS.toMinutes(ma.getDatabase().getLastUpdated());
        if (time < 1) //just refreshed. get the updated count
        {
            Log.d(LOG_TAG, "Updating amount (count) for " + building.name);
            building = ma.getBuilding(building);
            if (building == null)
                finish(); //the building was removed from the database
            displayAmount();
            lastUpdated.setText(getString(R.string.last_updated_now));
        } else {
            String unit = (time == 1) ? "minute" : "minutes";
            lastUpdated.setText(getString(R.string.last_updated_time, time, unit));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(lastUpdateTask);
    }
}