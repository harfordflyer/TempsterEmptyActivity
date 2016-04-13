package com.example.dhammond1.tempsteremptyactivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.media.RingtoneManager;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {

    boolean isServiceRunning;
    public static TextView tv_PitText;
    public static TextView tv_MeatText;
    public static EditText ed_targetTemp;
    DatabaseHandler dbHandler;
    ScheduledExecutorService databaseReadTask;
    Future<?> scheduleFuture;
    Chronometer chrono;
    long mLastStopTime = 0;
    long stoppedMilliseconds = 0;
    String setTime = "00:00";
    private static final String CONFIG_NAME = "AppConfig";
    private boolean saveGraphData;
    public static Calendar calendar;
    private SharedPreferences prefs;
    private boolean initialStart = true;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;

    public static void addDataBaseEntry(TemperatureEntry entry, DatabaseHandler handler)
    {
        handler.addEntry(entry);
    }


    public void StopService()
    {

    }


    @Override
    protected void onResume()
    {
        super.onResume();

        //if(!initialStart)
       // {
        //   RestoreChronoTime();
       // }
       // else
       // {
       //     initialStart = false;
       // }


    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SaveChronoTime();

    }


    protected void onSave()
    {

    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }


    protected void onSavedInstanceState()
    {
        SaveChronoTime();
    }

    protected void onRestoreInstanceState()
    {
        RestoreChronoTime();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saveGraphData = true;
        isServiceRunning = false;
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
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


        //do i need to do something here on first start... I will need to set all defaults on first start
        prefs = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        SetPreferences(prefs);

        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivityForResult(intent, 1);
                SaveChronoTime();


            }
        });

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                MainActivity.this.startActivity(intent);
                SaveChronoTime();

            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE).edit();
                String s = ed_targetTemp.getText().toString();
                editor.putString("targetPitTemp", ed_targetTemp.getText().toString());
                editor.apply();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning) {
                    if (mLastStopTime == 0) {
                        chrono.setBase(SystemClock.elapsedRealtime());
                    } else {
                        long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
                        chrono.setBase(chrono.getBase() + intervalOnPause);
                        // chrono.setFormat("00:00:00");
                        // chrono.setText("01:00:00");
                    }
                    StartIOIOServiceLoop(prefs);
                    isServiceRunning = true;
                    chrono.start();

                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chrono.stop();

                mLastStopTime = SystemClock.elapsedRealtime();
                StopIOIOService();

            }


        });

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter("results"));

        //set the clock to the correct time
        chrono.setText(setTime);
    }

    private void SaveChronoTime()
    {
        stoppedMilliseconds = 0;
        long base = chrono.getBase();
        long elapse = SystemClock.elapsedRealtime();
        mLastStopTime = SystemClock.elapsedRealtime();
        String chronoText = chrono.getText().toString();
        //get the time shown on the clock
        String array[] = chronoText.split(":");
        if (array.length == 2) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                    + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
                    + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }

        chrono.stop();
    }

    private void RestoreChronoTime()
    {
        long timeOnResume = SystemClock.elapsedRealtime();
        long intervalOnPause = (SystemClock.elapsedRealtime() +  mLastStopTime);
        long timeToShow = intervalOnPause + stoppedMilliseconds;
        long second = (timeToShow / 1000) % 60;
        long minute = (timeToShow / (1000 * 60)) % 60;
        long hour = (timeToShow / (1000 * 60 * 60)) % 24;

        //set time needs to go in shared prefs
        //along with the state of the running app
        //String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, timeToShow);
        setTime = String.format("%02d:%02d:%02d:%d", hour, minute, second, timeToShow);
        //chrono.setText(time);

        //chrono.start();
    }

    private  void StartIOIOServiceLoop(SharedPreferences prefs)
    {
        //IOIO service
        Intent intent = new Intent(MainActivity.this, IOIOLooperService.class);
        String sample = prefs.getString("sampleTime",null);
        String minTemp = prefs.getString("minTemp",null);
        String kp = prefs.getString("kp", null);
        String ki = prefs.getString("ki", null);
        String kd = prefs.getString("kd", null);
        String targetTemp = null;
        targetTemp = prefs.getString("targetPitTemp", null);

        if(Objects.equals(targetTemp, null))
        {
            targetTemp = ed_targetTemp.getText().toString();
        }

        intent.putExtra("sampleTime", sample );
        intent.putExtra("minTemp", minTemp);
        intent.putExtra("kp", kp);
        intent.putExtra("ki", ki);
        intent.putExtra("kd", kd);
        intent.putExtra("targetPitTemp",targetTemp);
        startService(intent);
    }

    private  void StopIOIOService()
    {
        Intent intent = new Intent(MainActivity.this, IOIOLooperService.class);
        stopService(intent);

    }

    //need to put in handlers for onDestroy and onResume
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int[] values = intent.getIntArrayExtra("temps");
            tv_MeatText.setText(String.valueOf(values[0]));
            tv_PitText.setText(String.valueOf(values[1]));
            calendar = Calendar.getInstance();
            DatabaseHandler DBHandler = dbHandler.getInstance(getApplicationContext());
            String date = DatabaseHandler.GetDateTime.GetDate(calendar);
            String time = DatabaseHandler.GetDateTime.GetTime(calendar);
            String pit = Integer.toString(values[0]);
            String meat = Integer.toString(values[1]);
            TemperatureEntry sampleEntry = new TemperatureEntry(date,time,pit,meat);
            Log.d("pit temp: ", sampleEntry.getPitTemp());
            Log.d("meat temp: ", sampleEntry.getMeatTemp());
            Log.d("date: ", DatabaseHandler.GetDateTime.GetDate(calendar));
            Log.d("time: ", DatabaseHandler.GetDateTime.GetTime(calendar));

            if(saveGraphData) {
                addDataBaseEntry(sampleEntry, DatabaseHandler.getInstance(getApplicationContext()));
            }

            CheckForTempNotification(pit);

        }
    };

    private void CheckForTempNotification(String pit)
    {
        boolean b_notify = false;
        String minTemp = prefs.getString("minTemp", null);
        String maxTemp = prefs.getString("maxTemp", null);
        int targetTemp = Integer.parseInt(pit);
        if(targetTemp < Integer.parseInt(minTemp))
        {
            b_notify = true;
        }
        else if(targetTemp < Integer.parseInt(maxTemp))
        {
            b_notify = true;
        }

        if(b_notify)
        {

            try {

                mediaPlayer.setVolume((float) (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) / 7.0),
                        (float) (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) / 7.0));
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
        }
    }


    //gets the value of save graph from the config activiyt
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            switch (resultCode)
            {
                case 1:
                {
                    saveGraphData = data.getBooleanExtra("saveGraphData", true);
                    break;
                }
                case 2:
                {
                    //stop and start the service with the new PID values
                    if(isServiceRunning)
                    {
                        StopIOIOService();
                        StartIOIOServiceLoop(prefs);
                    }
                    break;
                }

            }
            /*if(resultCode == 1){
                saveGraphData = data.getBooleanExtra("saveGraphData", true);

            }*/
        }
    }

    private void SetPreferences(SharedPreferences prefs)
    {
        String restoredText = prefs.getString("targetPitTemp", null);
        if(restoredText == null)
        {
            ed_targetTemp.setText("250", TextView.BufferType.EDITABLE);
        }
        else
        {
            ed_targetTemp.setText(restoredText, TextView.BufferType.EDITABLE);
        }
        //put some default values in the prefs so if the user doesn't visit the preferences
        //before starting the timer, the program has some values to use.

        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE).edit();
        restoredText = prefs.getString("minTemp", "230");
        editor.putString("minTemp",restoredText);
        restoredText = prefs.getString("maxTemp", "260");
        editor.putString("maxTemp", restoredText);
        restoredText = prefs.getString("fanSpeed", "100");
        editor.putString("fanSpeed", restoredText);
        restoredText = prefs.getString("kp", "5");
        editor.putString("kp", restoredText);
        restoredText = prefs.getString("ki", "0");
        editor.putString("ki", restoredText);
        restoredText = prefs.getString("kd", "0");
        editor.putString("ki", restoredText);
        restoredText = prefs.getString("sampleTime", "5");
        editor.putString("sampleTime", restoredText);
        boolean save = prefs.getBoolean("saveGraph", true);
        editor.putBoolean("saveGraph", save);

        editor.apply();
    }
}
