package com.cs389f20.diamonds;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphActivity extends AppCompatActivity {

    private Building building;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        // used for timestamp formatting
        final SimpleDateFormat adf = new SimpleDateFormat("hh:mm a");
        Intent intent = getIntent();
        building = (Building) intent.getSerializableExtra(BuildingSelectActivity.EXTRA_BUILDING);
        assert building != null;
        setTitle(getString((R.string.graph_title), building.name));

        PastCount[] data = building.getPastArray(60);
        if (data == null) //TODO Make error screen?
        {
            Log.e(HistoryActivity.class.getSimpleName(), "There is no past data for " + building.name);
            Toast.makeText(getApplicationContext(), "Error: No past data for " + building.name, Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        GraphView graph = findViewById(R.id.graph);
        // set date label formatter for x axis format (hh:mm am/pm)
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public java.lang.String formatLabel(double value, boolean isValueX) {
                if(isValueX)
                {
                    return adf.format(new Date((long)value));
                }
                else{
                    return super.formatLabel(value, isValueX);
                }

            }
        });
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);//fix the number of default x axis labels 
        DataPoint[] points = new DataPoint[24];
        double y_max = 100;//placeholder
        for(int i = 0; i < points.length; i++){
            points[i] = new DataPoint(data[i].getDate(),data[i].getPeople());
            if(data[i].getPeople() > y_max){
                y_max = data[i].getPeople();
            }
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);


        // set manual x bounds
        graph.getViewport().setMinX(data[0].getDate().getTime());
        graph.getViewport().setMaxX(data[23].getDate().getTime());
        graph.getViewport().setXAxisBoundsManual(true);
        //set manual y bounds
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(y_max + 10);
        graph.getViewport().setYAxisBoundsManual(true);
        //prevent GraphView from rounding x and y values
        graph.getGridLabelRenderer().setHumanRounding(false);
        //enable pinch and zoom for graph NOTE: Please try on actual phone
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        //TODO "tap listener on data" so that each point can be clicked on to show data

        //TODO "Style options"
        graph.addSeries(series);



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
