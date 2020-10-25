package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GraphActivity extends AppCompatActivity {

    private TextView textFlip;
    private Building building;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        building = (Building) intent.getSerializableExtra(BuildingSelectActivity.EXTRA_BUILDING);
        setTitle(getString((R.string.graph_title), building.name));

        textFlip = findViewById(R.id.textFlip);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createGraph(View v)
    {


       textFlip.setVisibility(View.VISIBLE);
    }
}