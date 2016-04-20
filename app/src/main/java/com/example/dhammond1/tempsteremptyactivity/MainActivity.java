package com.example.dhammond1.tempsteremptyactivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;


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
    boolean initialStart = true;
    boolean notificationTaskDone = true;
    String setTime = "00:00";
    private static final String CONFIG_NAME = "AppConfig";
    private boolean saveGraphData;
    public static Calendar calendar;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;

    Timer globalnotificationTimer;
    TimerTask notificationLoop; /*//*= new TimerTask() {
       /* @Override
        public void run() {
            CheckForTempNotification();
        }
    };*//*
*/

    public static void addDataBaseEntry(TemperatureEntry entry, DatabaseHandler handler)
    {
        handler.addEntry(entry);
    }



    @Override
    protected void onResume()
    {
        Log.d("ON RESUME", "onResume called");
        super.onResume();
        boolean start = prefs.getBoolean("initialStart",true);
        if(!start)
        {
            String time = chrono.getText().toString();
            Log.d("TIMEONRESUME", time);

            chrono.start();
        }
        else
        {
            Log.d("RESUME_INITSTART", setTime);
            //chrono.setText(setTime);
            editor.putBoolean("initialStart",false);
            editor.apply();
        }


    }

    @Override
    protected void onPause()
    {
        Log.d("ON PAUSE", "onPause called");
        super.onPause();
        Bundle bundle = new Bundle();
        onSavedInstanceState(bundle);

    }


    @Override
    protected void onStop()
    {
        Log.d("ON STOP", "onStop called");
        super.onStop();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ON CREATE", "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saveGraphData = true;
        //isServiceRunning = false;
        Button config = (Button)findViewById(R.id.configactivity);
        Button graph = (Button)findViewById(R.id.chartactivity);
        Button start = (Button)findViewById(R.id.chronStart);
        Button stop = (Button)findViewById(R.id.chronStop);
        Button set = (Button)findViewById(R.id.btn_setPit);
        final Button notifyStop = (Button)findViewById(R.id.notifyStop);
        tv_PitText = (TextView)findViewById(R.id.tx_tempPit);
        tv_MeatText = (TextView)findViewById(R.id.tx_tempMeat);
        ed_targetTemp = (EditText)findViewById(R.id.ed_targetPit);
        chrono = (Chronometer)findViewById(R.id.chronometer);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


        //do i need to do something here on first start... I will need to set all defaults on first start
        prefs = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);


        savePrefsToSharedPreferences();
        if(!prefs.getBoolean("isServiceRunning",false))
        {
            //then we are at the initial start or really just to set the clock to 00:00
            editor.putBoolean("initialStart",true);
            editor.apply();
        }

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

        notifyStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String text = null;
                if(notifyStop.getText().toString().equals("Notification Stop"))
                {

                    globalnotificationTimer.cancel();

                    text = "Notification Start";

                }
                else
                {
                    Timer timer = StartNotificationLoopTimer();
                    globalnotificationTimer = timer;
                    timer.schedule(notificationLoop, 0, 60000);
                    text = "Notification Stop";
                }
                notifyStop.setText(text);

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

                    StartIOIOServiceLoop(prefs);
                    isServiceRunning = true;
                    editor.putBoolean("isServiceRunning", true);
                    editor.apply();


                } else {
                    RestoreChronoTime();
                }
                if (initialStart) {
                    chrono.setText(setTime);
                    Log.d("CHRONOTIME", chrono.getText().toString());
                }
                chrono.setBase(SystemClock.elapsedRealtime());
                chrono.start();

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chrono.stop();
                StopIOIOService();
                mLastStopTime = 0;
                isServiceRunning = false;
                editor.putLong("lastStopTime", mLastStopTime);
                editor.putBoolean("isServiceRunning", isServiceRunning);
                editor.apply();
                SaveChronoTime();
            }


        });

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter("results"));

        globalnotificationTimer = StartNotificationLoopTimer();
        globalnotificationTimer.schedule(notificationLoop,0, 60000);
    }


    protected Timer StartNotificationLoopTimer()
    {
        Timer notificationTimer = new Timer();
        notificationLoop = new TimerTask() {
            @Override
            public void run() {
                CheckForTempNotification();
            }
        };
        return notificationTimer;

    }


    protected void onSavedInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Log.d("ON SAVE", "OnSavedInstanceState");
        savePrefsToSharedPreferences();
        SaveChronoTime();
    }


    protected void onRestoreInstanceState(Bundle savedInstance)
    {
        super.onRestoreInstanceState(savedInstance);
        Log.d("ON RESTORE","OnRestoreSavedInstanceState");
        RestoreChronoTime();
    }

    private void SaveChronoTime()
    {
        stoppedMilliseconds = 0;
        //long base = chrono.getBase();
       // long elapse = SystemClock.elapsedRealtime();
        mLastStopTime = SystemClock.elapsedRealtime();
        String chronoText = chrono.getText().toString();
        //put the text in the prefs
        //setTime = chronoText;
        //editor.putString("chronoTime", chronoText);
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

        editor.putLong("stoppedMilliseconds", stoppedMilliseconds);
        Log.d("MILLISECONDS", String.valueOf(stoppedMilliseconds));
        editor.putLong("lastStopTime", mLastStopTime);
        Log.d("LASTSTOPTIME", String.valueOf(mLastStopTime));
        editor.apply();

    }

    private void RestoreChronoTime()
    {
        stoppedMilliseconds = prefs.getLong("stoppedMilliseconds", 0L);
        mLastStopTime = prefs.getLong("lastStopTime", 0L);
        long seconds = SystemClock.elapsedRealtime() -  mLastStopTime;
        long timetodisplay = seconds + stoppedMilliseconds;
        Log.d("TIMETODISPLAY", String.valueOf(timetodisplay));
        Log.d("RESTORE_millis", String.valueOf(stoppedMilliseconds));
        if(stoppedMilliseconds != 0) {
            long second = (timetodisplay / 1000) % 60;
            long minute = (timetodisplay / (1000 * 60)) % 60;
            long hour = (timetodisplay / (1000 * 60 * 60)) % 24;

            //set time needs to go in shared prefs
            //along with the state of the running app

            if(hour != 0L)
            {
                setTime = String.format("%02d:%02d:%02d", hour, minute, second);
            }
            else
            {
                setTime = String.format("%02d:%02d", minute, second);
            }
            editor.putString("chronoTime", setTime);
            editor.apply();
        }
        else
        {
            editor.putString("chronoTime", setTime);
            editor.apply();
        }
        Log.d("SETTINGTIME", setTime);
        chrono.setText(setTime);
        chrono.setBase(SystemClock.elapsedRealtime() - timetodisplay);

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

        String max = prefs.getString("maxTemp", null);
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

        intent.putExtra("maxTemp", max);
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


        }
    };

    private void CheckForTempNotification()
    {
        String pit = tv_PitText.getText().toString();
        boolean b_notify = false;
        String minTemp = prefs.getString("minTemp", null);
        String maxTemp = prefs.getString("maxTemp", null);
        int targetTemp = 0;
        try {
            targetTemp = Integer.parseInt(pit);
        }
        catch(Exception e)
        {
            return;
        }

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


    private void savePrefsToSharedPreferences()
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

        editor = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE).edit();
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
        editor.putString("kd", restoredText);
        restoredText = prefs.getString("sampleTime", "5");
        editor.putString("sampleTime", restoredText);

        boolean saveBoolean = prefs.getBoolean("saveGraph", true);
        editor.putBoolean("saveGraph", saveBoolean);
        saveBoolean = prefs.getBoolean("isRunning", isServiceRunning);
        editor.putBoolean("isRunning", saveBoolean);
        saveBoolean = prefs.getBoolean("initialStart", true);
        editor.putBoolean("initialStart", saveBoolean);

        long restoredLong = prefs.getLong("stoppedMilliseconds", stoppedMilliseconds);
        editor.putLong("stoppedMilliseconds", restoredLong);
        restoredLong = prefs.getLong("lastStopTime", mLastStopTime);
        editor.putLong("lastStopTime", restoredLong);
        editor.apply();
    }
}
