package com.cs389f20.diamonds;

import android.app.PendingIntent;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.TimeUnit;

/*
Displays the building info screen
 */
public class BuildingActivity extends AppCompatActivity {
    private static final String LOG_TAG = BuildingActivity.class.getSimpleName(), SERIALIZABLE_KEY = "building";
    private Building building;
    private Handler handler;
    private Runnable lastUpdateTask;
    private NotificationCompat.Builder builder;

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
        displayInfo();

        //Set recurring task to update "last updated" every minute and update count if a database refresh happens
        handler = new Handler(Looper.getMainLooper());
        lastUpdateTask = new Runnable() {
            @Override
            public void run() {
                update();
                handler.postDelayed(this, TimeUnit.MINUTES.toMillis(1));
            }
        };
        handler.post(lastUpdateTask);

        //Dropdown spinner for notifications
        Spinner dropdown = findViewById(R.id.spinnerNotify);
        final String[] items = new String[]{"", "zero", "below 50%", "above 50%", "maximum"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(dropdown(items));
        //if a notification is active, set default of dropdown to that level
        if (building.isNotificationActive) {
            String text = building.notificationType;
            if (text.contains("zero"))
                dropdown.setSelection(1);
            else if (text.contains("below"))
                dropdown.setSelection(2);
            else if (text.contains("above"))
                dropdown.setSelection(3);
            else if (text.contains("maximum"))
                dropdown.setSelection(4);
        }
    }

    private void createNotification(int percent) //0 = zero, 49 = below 50%, 51 = above 50%, 100 = (above) max
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        String msg;
        if (percent == -1) {
            notificationManager.cancel(building.notificationID);
            building.isNotificationActive = false;
            building.notificationType = "";
            return;
        } else if (percent == 0)
            msg = "zero";
        else if (percent == 49)
            msg = "below 50%";
        else if (percent == 51)
            msg = "above 50%";
        else
            msg = "at or above maximum";
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder = new NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.default_building)
                .setContentTitle(building.name + " Capacity Alert")
                .setContentText("The building has reached " + msg + " capacity")
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        building.isNotificationActive = true;
        building.notificationType = msg;
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

    private void displayInfo() {
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

    //Updates amount of people, when the number was last updated, and checks if we need to send a notification
    private void update() {
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
        checkNotification();
    }

    private void checkNotification() {
        //get notification for this building. if doesn't exist, return.
        //if the notification requirements is met, call notify.
        if (!building.isNotificationActive || builder == null)
            return;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        String text = building.notificationType;
        if ((text.contains("zero") && building.currentNumberOfPeople == 0) ||
                (text.contains("below") && building.currentNumberOfPeople <= building.maxOccupancy / 2) ||
                (text.contains("above") && building.currentNumberOfPeople > building.maxOccupancy / 2) ||
                (text.contains("maximum") && building.currentNumberOfPeople >= building.maxOccupancy)
        )
            notificationManager.notify(building.notificationID, builder.build());
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
                    if (item.toString().equals(items[1]))
                        createNotification(0);
                    else if (item.toString().equals(items[2]))
                        createNotification(49);
                    else if (item.toString().equals(items[3]))
                        createNotification(51);
                    else if (item.toString().equals(items[4]))
                        createNotification(100);
                    else
                        createNotification(-1); //remove
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
    }
}