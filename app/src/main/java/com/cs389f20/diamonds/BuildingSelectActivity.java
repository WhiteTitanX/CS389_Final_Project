package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingSelectActivity extends AppCompatActivity {
    private static final String LOG_TAG =  MainActivity.class.getSimpleName();
    public static final String EXTRA_BUILDING =   "com.cs389f20.diamonds.extra.BUILDING";
    public static final int TEXT_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_select);

        Intent intent = getIntent();
        String propertyName = intent.getStringExtra(PropertySelectActivity.EXTRA_PROPERTY);


        //TODO: get all buildings listed under propertyName (from db), and display it
        // or: already store all buildings (and properties) on first connect to db (when app first launches), then just display them now from a map?

    }

    public void launchMainActivity(View v)
    {
        Log.d(LOG_TAG, "Building selected. Launching MainActivity.");
        Intent intent = new Intent(this, MainActivity.class);
        //   String message = editTextMessage.getText().toString();
        String message = ""; //TODO: get selection from building activity (on click?) as the message
        intent.putExtra(EXTRA_BUILDING, message);
        startActivity(intent);
        //  startActivityForResult(intent, TEXT_REQUEST); //for returning data back to this activity
    }
}