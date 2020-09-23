package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingSelectActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_BUILDING = "com.cs389f20.diamonds.extra.BUILDING";
    public static final int TEXT_REQUEST = 1;
    private String propertyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_select);
        //if we are recreating a previous saved state (the back button on mainactivity)
        if (savedInstanceState != null) {
            String _propertyName = savedInstanceState.getString("property_name");
            setTitle(_propertyName);
            propertyName = _propertyName;
        } else {
            Intent intent = getIntent();
            propertyName = intent.getStringExtra(PropertySelectActivity.EXTRA_PROPERTY);
            setTitle(propertyName);
        }
        //TODO: get all buildings listed under propertyName (from db), and display it
        // or: already store all buildings (and properties) on first connect to db (when app first launches), then just display them now from a map?


        Log.d(LOG_TAG, "-------");
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (propertyName != null)
            savedInstanceState.putString("property_name", propertyName);
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
        Log.d(LOG_TAG, "Building (name: " + button.getContentDescription().toString() + ") selected. Launching MainActivity.");
        Intent intent = new Intent(this, MainActivity.class);
        String message = button.getContentDescription().toString();
        intent.putExtra(EXTRA_BUILDING, message);
        startActivity(intent);
    }
}