package com.cs389f20.diamonds;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

/*
Displays the building info screen
 */
public class BuildingActivity extends AppCompatActivity {
    private static final String LOG_TAG = BuildingActivity.class.getSimpleName(), SERIALIZABLE_KEY = "building";
    private Building building;
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

        //Dropdown spinner for notifications
        Spinner dropdown = findViewById(R.id.spinnerNotify);
        final String[] items = new String[]{"zero", "below 50%", "above 50%", "maximum"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(dropdown(items));
        //TODO: If a notification is active, set default on spinner to that level
    }

    private void createNotification(int percent) //0 = zero, 49 = below 50%, 51 = above 50%, 100 = (above) max
    {
        //If a notification already exists, replace it

        int max = building.maxOccupancy;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
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
        intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, building);
        startActivity(intent);
    }

    public void launchGraphActivity(View v) {
        Log.d(LOG_TAG, "Graph button pressed. Launching GraphActivity.");
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, building);
        startActivity(intent);
    }

    private void displayNameAndAmount() {
        displayName();
        displayAmount();
        displayCapacity();
    }

    private void displayName() {
        TextView header = findViewById(R.id.textHeader);
        header.setText(getString(R.string.building_info_header, building.name));
    }

    private void displayAmount() {
        TextView people = findViewById(R.id.textAmount);
        people.setText(getString(R.string.people_amount, building.currentNumberOfPeople));
    }

    private void displayCapacity() {
        TextView capacity = findViewById(R.id.textCapacity);
        if (building.currentNumberOfPeople <= building.maxOccupancy / 2) {
            capacity.setBackgroundColor(Color.GREEN);
            if (building.currentNumberOfPeople == 0)
                capacity.setText(getString(R.string.capacity_text, "at zero"));
            else
                capacity.setText(getString(R.string.capacity_text, "below 50%"));
        } else if (building.currentNumberOfPeople < building.maxOccupancy) {
            capacity.setBackgroundColor(Color.rgb(212, 175, 55));
            capacity.setText(getString(R.string.capacity_text, "above 50%"));
        } else {
            capacity.setBackgroundColor(Color.RED);
            capacity.setText(getString(R.string.capacity_text, "at or above maximum"));
        }
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
        displayCapacity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lastUpdateTask != null)
            handler.removeCallbacks(lastUpdateTask);
    }

    private AdapterView.OnItemSelectedListener dropdown(final String[] items) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null)
                    if (item.toString().equals(items[0]))
                        createNotification(0);
                    else if (item.toString().equals(items[1]))
                        createNotification(49);
                    else if (item.toString().equals(items[2]))
                        createNotification(51);
                    else
                        createNotification(100);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
    }
}