package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    private ScrollView scrollView;
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

        scrollView = findViewById(R.id.scrollHistory);

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
    }

    private void displayLog(int minutes) {
        //add items from building's array to the scroll view
        PastCount[] data = building.getPastArray(minutes);
        if (data == null) //TODO: maybe turn this into TextView on the history activity, instead of just a toast?
        {
            Log.e(HistoryActivity.class.getSimpleName(), "There is no past data for " + building.name);
            Toast.makeText(getApplicationContext(), "Error: No past data for " + building.name, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsText.setMargins(0, 15, 0, 0);
        paramsText.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        paramsText.addRule(RelativeLayout.TEXT_ALIGNMENT_CENTER, RelativeLayout.TRUE);

        TextView textView;
        int lastID = R.id.averageText;

        for (PastCount datum : data) {
         /*   textView = new TextView(this);
            textView.setText(building.pastNumberOfPeople[i]);
            paramsText.addRule(RelativeLayout.BELOW, lastID);
            lastID = textView.getId(); */
            Log.d(HistoryActivity.class.getName(), datum.getDate() + ": " + datum.getPeople());
        }
        Log.d(HistoryActivity.class.getSimpleName(), "Data length is : " + data.length); //15m should be 96, 30m should be 48, 1h should be 24
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