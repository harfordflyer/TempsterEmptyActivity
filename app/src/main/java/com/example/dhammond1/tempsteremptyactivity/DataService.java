package com.example.dhammond1.tempsteremptyactivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by dhammond1 on 3/9/2016.
 */
public class DataService extends Service implements Serializable {//private ReadTempTimerTask readTempTimerTask = new ReadTempTimerTask();
    Timer myTimer;
    Context ctx;
    private final IBinder mBinder = new MyBinder();
    public TemperatureEntry sampleEntry = new TemperatureEntry(null, null, null, null);
    DatabaseHandler dbHandler;
    public static Calendar c = Calendar.getInstance();

    @Override
    public void onCreate()
    {
        ctx = this.getApplicationContext();
        Log.d("on create", "service created");
    }


    public int onStartCommand(Intent intent, int flags, int startId){
        //to do ---- something useful
        //for now, write a method that will return a random value of pit and meat temp
        //in a TemperatureEntry class
        String[] temps = intent.getStringArrayExtra("temps");
        sampleEntry.setPitTemp(temps[0]);
        sampleEntry.setMeatTemp(temps[1]);
        Log.d("on start started", "service started?");
        Log.d("intents: ", temps[0]);
        Log.d("intents: ", temps[1]);
        //put a timer here
        ScheduledExecutorService getCurrentTemps = Executors.newScheduledThreadPool(5);

        getCurrentTemps.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                DatabaseHandler DBHandler = dbHandler.getInstance(ctx);
                boolean exists = DBHandler.DoesDatabaseExist(getApplicationContext(),"temperatureEntry.db");
                Log.d("DBHandler: ", Boolean.toString(exists));
                ReadTemperatures(sampleEntry, DBHandler);
                TemperatureEntry entry = DBHandler.getLastEntry();
               /* Bundle bundle = new Bundle();
                bundle.putSerializable("entry", entry);
                Message message = new Message();
                message.setData(bundle);
                runnableCallback.sendMessage(message);*/
               /* Log.d("creating intent", "Intent for broadcast");
                Intent intent = new Intent(getBaseContext(), FragmentMain.class);
                intent.putExtra("results", sampleEntry);

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);*/
            }
        },5000, 5000, TimeUnit.MILLISECONDS);

        // Runnable r = new Runnable() {
        //     @Override
        //     public void run() {
        //         DatabaseHandler DBHandler = dbHandler.getInstance(getApplicationContext());
        //         boolean exists = DBHandler.DoesDatabaseExist(getApplicationContext(),"temperatureEntry.db");
        //         Log.d("DBHandler: ", Boolean.toString(exists));
        //         ReadTemperatures(sampleEntry, DBHandler);
        //     }
        // };

        // Thread t = new Thread(r);
        // t.start();
        //android.os.Debug.waitForDebugger();
        //ReadTemperatures(sampleEntry);
        // MainActivity.dbHandler.addEntry(sampleEntry);
        return Service.START_STICKY;
    }



    public IBinder onBind(Intent intent){
        //to do for communication return IBinder implementation
        return null;
    }

    public class MyBinder extends Binder {
        DataService getService(){
            return DataService.this;
        }
    }

    public TemperatureEntry GetTemperatureEntry()
    {
        return sampleEntry;
    }


    public static void ReadTemperatures(TemperatureEntry sample, DatabaseHandler db)
    {
        Random rand = new Random();

        int pit = rand.nextInt(240) + 20;
        rand = new Random();
        int meat = rand.nextInt(220) + 60;
        sample.setPitTemp(Integer.toString(pit));
        sample.setMeatTemp(Integer.toString(meat));
        Log.d("pit temp: ", sample.getPitTemp());
        Log.d("meat temp: ", sample.getMeatTemp());
        Log.d("date: ", DatabaseHandler.GetDateTime.GetDate(c));
        Log.d("time: ", DatabaseHandler.GetDateTime.GetTime(c));
        db.addEntry(new TemperatureEntry(DatabaseHandler.GetDateTime.GetDate(c),
                DatabaseHandler.GetDateTime.GetTime(c), sample.getPitTemp(), sample.getMeatTemp()));


    }


}
