package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
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
        setTitle(getString((R.string.history_title), building.name));

        scrollView = findViewById(R.id.scrollHistory);

        Spinner dropdown = findViewById(R.id.spinnerFilter);
        String[] items = new String[]{"5 minutes", "15 minutes", "30 minutes", "hour"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    Toast.makeText(MainActivity.getInstance().getApplicationContext(), item.toString(),
                            Toast.LENGTH_SHORT).show();

                    displayLog();
                }
                Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Selected",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void displayLog() {
        //add items from building's array to the scroll view
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