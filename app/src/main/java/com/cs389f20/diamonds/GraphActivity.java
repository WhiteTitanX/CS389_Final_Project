package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class GraphActivity extends AppCompatActivity {

    private TextView textFlip;
    private Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        building = (Building) intent.getSerializableExtra(BuildingSelectActivity.EXTRA_BUILDING);
        assert building != null;
        setTitle(getString((R.string.graph_title), building.name));

        textFlip = findViewById(R.id.textFlip);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createGraph(View v) {
        PastCount[] data = building.getPastArray(60);
        if (data == null) //You can turn this into an error screen instead of just a toast, or just keep it like this
        {
            Log.e(HistoryActivity.class.getSimpleName(), "There is no past data for " + building.name);
            Toast.makeText(getApplicationContext(), "Error: No past data for " + building.name, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int people = data[0].getPeople();
        Date date = data[0].getDate();
        String dateStr = data[0].getDate().toString();

        textFlip.setVisibility(View.VISIBLE);
    }
}