package com.example.dhammond1.tempsteremptyactivity;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph1);

        SetDataPoints();
    }

    DatabaseHandler handler;



    public void SetDataPoints() throws NullPointerException {
        GraphView graph = (GraphView)findViewById(R.id.graph);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        DatabaseHandler dbHandler = handler.getInstance(getApplicationContext());
        TemperatureEntry entry;

        try
        {
            entry = dbHandler.getLastEntry();
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Could not get last entry in database: No entry for the session.", Toast.LENGTH_LONG).show();
            return;
        }

        String date = entry.getDate();

        List<TemperatureEntry> entries = dbHandler.getEntriesByDate(date);

        double count = 0.0;
        LineGraphSeries<DataPoint> pitSeries = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> meatSeries = new LineGraphSeries<DataPoint>();
        int meatCol = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);

        int cccP = pitSeries.getColor();
        int cccM = meatSeries.getColor();

        meatSeries.setColor(Color.MAGENTA);
        pitSeries.setColor(Color.GREEN);
        cccM = meatSeries.getColor();
        cccP = pitSeries.getColor();

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        pitSeries.setCustomPaint(paint);

        ArrayList<DataPoint> pitDataList = new ArrayList<DataPoint>();
        ArrayList<DataPoint> meatDataList = new ArrayList<DataPoint>();
        for(TemperatureEntry e : entries)
        {

            DataPoint pit = new DataPoint(count, Double.parseDouble(e.getPitTemp()));
            DataPoint meat = new DataPoint(count, Double.parseDouble(e.getMeatTemp()));
            pitDataList.add(pit);
            meatDataList.add(meat);
            count++;
        }

        DataPoint[] pitDataPoints = new DataPoint[pitDataList.size()];
        DataPoint[] meatDataPoints = new DataPoint[meatDataList.size()];

        meatDataPoints = meatDataList.toArray(meatDataPoints);
        meatSeries = new LineGraphSeries<DataPoint>(meatDataPoints);
        graph.addSeries(meatSeries);

        pitDataPoints  = pitDataList.toArray(pitDataPoints);
        pitSeries = new LineGraphSeries<DataPoint>(pitDataPoints);
        graph.addSeries(pitSeries);
    }

}
