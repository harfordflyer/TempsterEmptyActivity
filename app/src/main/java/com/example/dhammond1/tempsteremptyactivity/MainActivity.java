package com.example.dhammond1.tempsteremptyactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static TextView tv_PitText;
    public static TextView tv_MeatText;
    public static EditText ed_targetTemp;
    DatabaseHandler dbHandler;
    ScheduledExecutorService databaseReadTask;
    Future<?> scheduleFuture;
    Chronometer chrono;
    long mLastStopTime = 0;
    private static final String CONFIG_NAME = "AppConfig";

    public void StartService()
    {
        scheduleFuture = databaseReadTask.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                DatabaseHandler DBHandler = dbHandler.getInstance(getApplicationContext());
                TemperatureEntry sampleEntry = new TemperatureEntry(null, null, null, null);

                DataService.ReadTemperatures(sampleEntry, DBHandler);

                TemperatureEntry entry = DBHandler.getLastEntry();

                Bundle bundle = new Bundle();
                bundle.putSerializable("entry", entry);
                Message message = new Message();
                message.setData(bundle);
                runnableCallback.sendMessage(message);

            }
        }, 10000, 10000, TimeUnit.MILLISECONDS);

    }

    public void StopService()
    {
        databaseReadTask.shutdown();
    }

    public static Handler runnableCallback = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            TemperatureEntry entry = (TemperatureEntry)bundle.getSerializable("entry");
            tv_PitText.setText(entry.getPitTemp());

            tv_MeatText.setText(entry.getMeatTemp());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button config = (Button)findViewById(R.id.configactivity);
        Button graph = (Button)findViewById(R.id.chartactivity);
        Button start = (Button)findViewById(R.id.chronStart);
        Button stop = (Button)findViewById(R.id.chronStop);
        Button set = (Button)findViewById(R.id.btn_setPit);
        tv_PitText = (TextView)findViewById(R.id.tx_tempPit);
        tv_MeatText = (TextView)findViewById(R.id.tx_tempMeat);
        ed_targetTemp = (EditText)findViewById(R.id.ed_targetPit);
        chrono = (Chronometer)findViewById(R.id.chronometer);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        String restoredText = prefs.getString("targetPitTemp", null);
        if(restoredText == null)
        {
            ed_targetTemp.setText("250", TextView.BufferType.EDITABLE);
        }
        else
        {
            ed_targetTemp.setText(restoredText, TextView.BufferType.EDITABLE);
        }

        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        graph.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(CONFIG_NAME,Context.MODE_PRIVATE).edit();
                String s = ed_targetTemp.getText().toString();
                editor.putString("targetPitTemp", ed_targetTemp.getText().toString());
                editor.apply();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastStopTime == 0) {
                    chrono.setBase(SystemClock.elapsedRealtime());
                } else {
                    long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
                    chrono.setBase(chrono.getBase() + intervalOnPause);
                }
                chrono.start();
                //start sampling data
                Intent i = new Intent(MainActivity.this, DataService.class);
                i.putExtra("temps", new String[]{"1000", "2000"});

                databaseReadTask = Executors.newScheduledThreadPool(5);
                StartService();

            }
        });

        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                chrono.stop();

                mLastStopTime = SystemClock.elapsedRealtime();
                StopService();
                //To Do add StopService
            }
        });
    }
}
