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
import com.jjoe64.graphview.series.Series;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphActivity extends AppCompatActivity {

    private Building building;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Intent intent = getIntent();
        building = (Building) intent.getSerializableExtra(BuildingSelectActivity.EXTRA_BUILDING);
        assert building != null;
        setTitle(getString((R.string.graph_title), building.name));
        //displays for 15 minute intervals change to 60 for hour,30 for ever half hour
        displaygraph(15);
    }

    private void displaygraph(int minutes) {
        final SimpleDateFormat adf = new SimpleDateFormat("hh:mm a");
        PastCount[] data = building.getPastArray(minutes);

        if (data == null) //TODO Make error screen?
        {
            Log.e(HistoryActivity.class.getSimpleName(), "There is no past data for " + building.name);
            Toast.makeText(getApplicationContext(), "Error: No past data for " + building.name, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        System.out.println(data.length);
        GraphView graph = findViewById(R.id.graph);
        // set date label formatter for x axis format (hh:mm am/pm)
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public java.lang.String formatLabel(double value, boolean isValueX) {
                if(isValueX) { return adf.format(new Date((long)value)); }
                else{ return super.formatLabel(value, isValueX);}
            }
        });
        //create datapoints for line
        DataPoint[] points = new DataPoint[data.length];
        double y_max = 0;
        for(int i = 0; i < points.length; i++){
            points[i] = new DataPoint(data[i].getDate(),data[i].getPeople());
            if(data[i].getPeople() > y_max){
                y_max = data[i].getPeople();
            }
        }

        //create line with datapoints above
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
        //Setup graph options
        graph.setTitle("Past 24 hours");
        // set manual x bounds on graph
        graph.getViewport().setMinX(data[0].getDate().getTime());
        graph.getViewport().setMaxX(data[23].getDate().getTime());
        graph.getViewport().setXAxisBoundsManual(true);
        //set manual y bounds on graph
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(y_max);
        graph.getViewport().setYAxisBoundsManual(true);
        //prevent GraphView from rounding x but allow rounding of y values
        graph.getGridLabelRenderer().setHumanRounding(false,true );
        //fix the number of default x axis labels
        graph.getGridLabelRenderer().setNumHorizontalLabels(24);
        //If labels are 45 degrees they fit
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(45);
        graph.getGridLabelRenderer().setPadding(175);
        //Allow zooming and scrolling on graph
        graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true); I am still messing around with this
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);

        //Datapoints can be taped to show Capacity at each time ie "105 people at 08:03 AM"
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(GraphActivity.this, Math.round(dataPoint.getY()) + " people at " +  adf.format(dataPoint.getX()), Toast.LENGTH_SHORT).show();
            }
        });

        //Style options for graph and line series
        series.setDrawDataPoints(true);//Show data points on line series
        series.setDrawBackground(true);//shade in under line series
        graph.addSeries(series);//draw line on graph
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
