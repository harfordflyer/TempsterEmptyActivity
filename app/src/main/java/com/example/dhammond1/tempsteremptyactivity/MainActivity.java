package com.example.dhammond1.tempsteremptyactivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
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
import android.widget.Toast;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;


public class MainActivity extends AppCompatActivity {

    boolean isServiceRunning;
    public static TextView tv_PitText;
    public static TextView tv_MeatText;
    public static EditText ed_targetTemp;
    public static TextView tv_Timer;
    DatabaseHandler dbHandler;
    ScheduledExecutorService databaseReadTask;


    boolean initialStart = true;
    boolean notificationOn = true;
    String setTime = "00:00";
    private static final String CONFIG_NAME = "AppConfig";
    private boolean saveGraphData;
    public static Calendar calendar;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    AlertDialog.Builder dlgAlert;
    Timer globalnotificationTimer;
    TimerTask notificationLoop;


    public static void addDataBaseEntry(TemperatureEntry entry, DatabaseHandler handler)
    {
        handler.addEntry(entry);
    }


    //onResume method call used to make sure the chronometer starts correctly
    @Override
    protected void onResume()
    {
        Log.d("ON RESUME", "onResume called");
        super.onResume();
        boolean start = prefs.getBoolean("initialStart",true);
        if(start)
        {
            Log.d("RESUME_INITSTART", setTime);
            editor.putBoolean("initialStart",false);
            editor.apply();
        }
        /*else
        {
            Log.d("RESUME_INITSTART", setTime);
            editor.putBoolean("initialStart",false);
            editor.apply();
        }*/
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

        Button config = (Button)findViewById(R.id.configactivity);
        Button graph = (Button)findViewById(R.id.chartactivity);
        Button start = (Button)findViewById(R.id.chronStart);
        Button stop = (Button)findViewById(R.id.chronStop);
        Button set = (Button)findViewById(R.id.btn_setPit);
        final Button notifyStop = (Button)findViewById(R.id.notifyStop);
        tv_PitText = (TextView)findViewById(R.id.tx_tempPit);
        tv_MeatText = (TextView)findViewById(R.id.tx_tempMeat);
        ed_targetTemp = (EditText)findViewById(R.id.ed_targetPit);
        tv_Timer = (TextView)findViewById(R.id.timerView);

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

            }
        });

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                MainActivity.this.startActivity(intent);


            }
        });

        notifyStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SetNotificationState();
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE).edit();
                String s = ed_targetTemp.getText().toString();
                editor.putString("targetPitTemp", ed_targetTemp.getText().toString());
                editor.apply();
                ResetConfigurationData();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning) {

                    StartIOIOServiceLoop(prefs);
                    Intent intent = new Intent(MainActivity.this, SessionTimerService.class);
                    intent.putExtra("sessionTimerText", tv_Timer.getText());
                    startService(intent);
                    isServiceRunning = true;
                    editor.putBoolean("isServiceRunning", true);
                    editor.apply();


                }


            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowStopMessageBox();

            }


        });


        IntentFilter filter = new IntentFilter("results");
        filter.addAction("time");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        SetNotificationState();

    }

    private void SetNotificationState()
    {
        boolean notify = prefs.getBoolean("notificationsOn",true);
        String text = null;

        if(notify)
        {
            //notifications are turned on - user has elected to turn them off
            editor.putBoolean("notificationsOn", false);
            StopNotificationTimer();
            text = "Notification Off";
        }
        else
        {
            StartNotificationLoopTimer();
            editor.putBoolean("notificationsOn", true);
            text = "Notification On";
        }
        Button notifyStop = (Button)findViewById(R.id.notifyStop);
        notifyStop.setText(text);
        editor.apply();
    }

    private AlertDialog.Builder ShowStopMessageBox()
    {
        dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Selecting Ok will end the session.  Do you wish to continue?");
        dlgAlert.setTitle("Stop Tempster");
        dlgAlert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                        StopAllServices();

                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        return dlgAlert;
    }


    protected void StopAllServices()
    {
        StopIOIOService();
        stopService(new Intent(MainActivity.this, SessionTimerService.class));

        tv_Timer.setText(setTime);
        tv_PitText.setText("N/T");
        tv_MeatText.setText("N/T");
        editor.putBoolean("isServiceRunning", false);
        editor.apply();
    }

    protected void StartNotificationLoopTimer()
    {
        globalnotificationTimer = new Timer();
        notificationLoop = new TimerTask() {
            @Override
            public void run() {
                CheckForTempNotification();
            }
        };
        globalnotificationTimer.schedule(notificationLoop,0, 60000);

    }


    protected void StopNotificationTimer()
    {
        if(globalnotificationTimer != null) {
            globalnotificationTimer.cancel();
        }
    }

    protected void onSavedInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Log.d("ON SAVE", "OnSavedInstanceState");
        savePrefsToSharedPreferences();

    }


    protected void onRestoreInstanceState(Bundle savedInstance)
    {
        super.onRestoreInstanceState(savedInstance);
        Log.d("ON RESTORE", "OnRestoreSavedInstanceState");

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


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("ACTION",action);
            switch (action)
            {
                case "results":
                {
                    int[] values = intent.getIntArrayExtra("temps");
                    tv_MeatText.setText(String.valueOf(values[0]));
                    tv_PitText.setText(String.valueOf(values[1]));
                    Log.d("BROADCAST_MEAT", String.valueOf(values[0]));
                    Log.d("BROADCAST_PIT",String.valueOf(values[1]));
                    if(values[0] < 0 && values[1] < 0)
                    {
                        Toast.makeText(getApplicationContext(),"Make sure probes are plugged in correctly",Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (saveGraphData)
                        {
                            FormatAndSaveTemperatureResponse(values);
                        }
                    }
                    break;
                }
                case "time":
                {
                    String time = intent.getStringExtra("timeString");
                    tv_Timer.setText(time);
                    Log.d("BROADCAST_TIME", time);
                    break;
                }
            }

        }
    };

    private void FormatAndSaveTemperatureResponse(int[] values)
    {
        DatabaseHandler DBHandler = dbHandler.getInstance(getApplicationContext());
        calendar = Calendar.getInstance();
        String date = DatabaseHandler.GetDateTime.GetDate(calendar);
        String time = DatabaseHandler.GetDateTime.GetTime(calendar);
        String pit = Integer.toString(values[0]);
        String meat = Integer.toString(values[1]);
        TemperatureEntry sampleEntry = new TemperatureEntry(date,time,pit,meat);
        Log.d("pit temp: ", sampleEntry.getPitTemp());
        Log.d("meat temp: ", sampleEntry.getMeatTemp());
        Log.d("date: ", DatabaseHandler.GetDateTime.GetDate(calendar));
        Log.d("time: ", DatabaseHandler.GetDateTime.GetTime(calendar));

        addDataBaseEntry(sampleEntry, DatabaseHandler.getInstance(getApplicationContext()));
    }

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
                    ResetConfigurationData();

                    break;
                }

            }

        }
    }


    public void ResetConfigurationData()
    {
        //stop and start the service with the new PID values
        if(isServiceRunning)
        {
            StopIOIOService();
            StartIOIOServiceLoop(prefs);
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
        saveBoolean = prefs.getBoolean("notificationOn", notificationOn);
        editor.putBoolean("notificationOn",saveBoolean);

        editor.apply();
    }
}
