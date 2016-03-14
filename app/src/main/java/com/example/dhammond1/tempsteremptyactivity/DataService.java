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


/**
 * Created by dhammond1 on 3/9/2016.
 */
public class DataService extends Service implements Serializable {//private ReadTempTimerTask readTempTimerTask = new ReadTempTimerTask();
    Timer myTimer;
    Context ctx;
    private final IBinder mBinder = new MyBinder();
    public TemperatureEntry sampleEntry = new TemperatureEntry(null, null, null, null);
    DatabaseHandler dbHandler;
   // public static Calendar c = Calendar.getInstance();
    private boolean _saveGraphData;

    public  static int pitTemp, meatTemp;


    @Override
    public void onCreate()
    {
        ctx = this.getApplicationContext();
        _saveGraphData = true;
        Log.d("on create", "service created");
        pitTemp = 0;
        meatTemp = 0;

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


    public static void ReadTemperatures(/*TemperatureEntry sample, DatabaseHandler db*/)
    {
        Random rand = new Random();

        pitTemp = rand.nextInt(240) + 20;
        rand = new Random();
        meatTemp = rand.nextInt(220) + 60;

    }


}
