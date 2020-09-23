package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String buildingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //if we are recreating a previous saved state (the back button)
        if (savedInstanceState != null) {
            buildingName = savedInstanceState.getString("building_name");
            setTitle(buildingName);
        } else {
            Intent intent = getIntent();
            buildingName = intent.getStringExtra(BuildingSelectActivity.EXTRA_BUILDING);
            setTitle(buildingName);
        }

        TextView header = findViewById(R.id.textHeader);
        header.setText(getString(R.string.building_info_header, buildingName));
        TextView people = findViewById(R.id.textAmount);
        people.setText("25"); //should we call the db here, or cache is separately and send this as a string in the intent?

    }

    //This fixes a bug (that hasn't been fixed for 5 years...) where clicking the options back arrow (top left) on a activity with a parent won't properly save the state on onCreate()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (buildingName != null)
            savedInstanceState.putString("building_name", buildingName);
    }

    public void launchHistoryActivity(View v) {
        Log.d(LOG_TAG, "History button pressed. Launching HistoryActivity.");
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, buildingName);
        startActivity(intent);
    }

    public void launchGraphActivity(View v) {
        Log.d(LOG_TAG, "Graph button pressed. Launching GraphActivity.");
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra(BuildingSelectActivity.EXTRA_BUILDING, buildingName);
        startActivity(intent);
    }
}