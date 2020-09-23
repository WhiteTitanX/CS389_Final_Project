package com.cs389f20.diamonds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String buildingName = intent.getStringExtra(BuildingSelectActivity.EXTRA_BUILDING);
        setTitle(buildingName);
        TextView header = findViewById(R.id.textHeader);
        header.setText(getString(R.string.building_info_header, buildingName));
        TextView people = findViewById(R.id.textAmount);
        people.setText("25"); //should we call the db here, or cache is separately and send this as a string in the intent?

    }

    //This fixes a bug (that hasn't been fixed for 5 years...) where clicking the back arrow (top left) on a activity with a parent won't properly save the state on onCreate()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}