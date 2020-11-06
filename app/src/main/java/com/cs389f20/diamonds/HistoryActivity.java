package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    private Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent intent = getIntent();
        building = (Building) intent.getSerializableExtra(BuildingSelectActivity.EXTRA_BUILDING);
        if (building == null) {
            Toast.makeText(MainActivity.getInstance().getApplicationContext(), "ERROR: No building", Toast.LENGTH_SHORT).show();
            return;
        }
        setTitle(getString((R.string.history_title), building.name));

        Spinner dropdown = findViewById(R.id.spinnerFilter);
        final String[] items = new String[]{"15 minutes", "30 minutes", "hour"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    if (item.toString().equals(items[1]))
                        displayLog(30);
                    else if (item.toString().equals(items[2]))
                        displayLog(60);
                    else
                        displayLog(15);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        displayAverage();
    }

    private void displayLog(int minutes) {
        PastCount[] data = building.getPastArray(minutes);
        if (data == null) //TODO: maybe turn this into TextView on the history activity, instead of just a toast?
        {
            Log.e(HistoryActivity.class.getSimpleName(), "There is no past data for " + building.name);
            Toast.makeText(getApplicationContext(), "Error: No past data for " + building.name, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView textView = findViewById(R.id.logText);
        textView.setMovementMethod(new ScrollingMovementMethod());
        String text = "";
        for (PastCount datum : data) {
            text += datum.getDate() + " " + datum.getPeople() + "\n";
        }
        textView.setText(text);
    }

    private void displayAverage() {
        PastCount[] data = building.getPastArray(15);
        if (data == null)
            return;
        int total = 0;
        for (PastCount datum : data) {
            total += datum.getPeople();
        }
        TextView textView = findViewById(R.id.averageText);
        textView.setText(getString((R.string.history_average), total / data.length));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}