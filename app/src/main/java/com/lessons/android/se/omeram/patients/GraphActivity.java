package com.lessons.android.se.omeram.patients;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Date;

//This class display the patient tests grades graph on each date
public class GraphActivity extends AppCompatActivity {

    private ArrayList<String> years = new ArrayList<>();
    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> grades = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Graph View");
        setSupportActionBar(toolbar);

        GraphView graph = (GraphView) findViewById(R.id.graph);

        //Receive the data to show on the graph
        years = getIntent().getStringArrayListExtra(Constants.COLUMN_YEAR);
        months = getIntent().getStringArrayListExtra(Constants.COLUMN_MONTH);
        days = getIntent().getStringArrayListExtra(Constants.COLUMN_DAY);
        grades = getIntent().getStringArrayListExtra(Constants.COLUMN_GRADE);

        DataPoint[] dataPoints = new DataPoint[years.size()];

        for (int i = 0; i < years.size(); i++) {
            DataPoint dataPoint = new DataPoint(new Date(
                    Integer.parseInt(years.get(i)),
                    Integer.parseInt(months.get(i)),
                    Integer.parseInt(days.get(i))),
                    Double.parseDouble(grades.get(i)));
            dataPoints[i] = dataPoint;
        }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(dataPoints);

        graph.setBackgroundColor(R.color.success_stroke_color);
        graph.addSeries(series);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

        // set manual x bounds to have nice steps
        //graph.getViewport().setMinX(dataPoints[0].getX().getTime());
        //graph.getViewport().setMaxX(dataPoints[dataPoints.length].getX().getTime());
        //graph.getViewport().setXAxisBoundsManual(true);

        // styling
        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });

        //series.setSpacing(50);

        // draw values on top
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.RED);
        series.setValuesOnTopSize(50);

    }

}
