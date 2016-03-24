package com.example.dhammond1.tempsteremptyactivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;


import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

import ioio.lib.util.android.IOIOService;

import java.util.Timer;
import java.util.TimerTask;


public class IOIOLooperService extends IOIOService {
    private static final String CONFIG_NAME = "AppConfig";
   // SharedPreferences config = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
   // String restoredText = config.getString("sampleTime", null);
    int sampleTime;
    int initialWait = 10000;

    public static float pitVoltage = 0;
    public static float meatVoltage = 0;

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper() {


            private DigitalOutput led_;

            //Initialize Timer Stuff
            Timer t = new Timer();
            TimerTask controlLoop = new TimerTask(){

                @Override
                public void run() {
                    try {
                        controlLoop();
                    } catch (ConnectionLostException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            };

            //IOIO Setup Method
            @Override
            protected void setup() throws ConnectionLostException,
                    InterruptedException {
                //Setup IOIO Pins
                led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);

                //Increase thread priority
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                //Set timer to execute 'controlLoop' after a xxms delay every xxms
                t.schedule(controlLoop, initialWait, sampleTime);
            }

            //Control Loop Method
            protected synchronized void controlLoop() throws ConnectionLostException,
                    InterruptedException {
                led_.write(false);
                Thread.sleep(2000);
                led_.write(true);

                pitVoltage = pitVoltage + 1;
                meatVoltage = meatVoltage + 2;
                float[] values = new float[2];
                values[0] = pitVoltage;
                values[1] = meatVoltage;

                Intent intent = new Intent("results");
                intent.putExtra("temps", values);
                sendBroadcast(intent);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            }


            /*private DigitalOutput led_;

            @Override
            protected void setup() throws ConnectionLostException,
                    InterruptedException {
                led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
            }

            @Override
            public void loop() throws ConnectionLostException,
                    InterruptedException {
                led_.write(false);
                Thread.sleep(500);
                led_.write(true);
                Thread.sleep(500);
            }*/
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String sample = intent.getStringExtra("sampleTime");
        sampleTime = Integer.parseInt(sample) * 1000;

        super.onStartCommand(intent, flags, startId);



        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (intent != null && intent.getAction() != null
                && intent.getAction().equals("stop")) {
            // User clicked the notification. Need to stop the service.

            nm.cancel(0);
            stopSelf();
        } else {
            // Service starting. Create a notification.

/*Notification notification = new Notification(
					R.drawable.ic_launcher, "IOIO service running",
					System.currentTimeMillis());
			notification
					.setLatestEventInfo(this, "IOIO Service", "Click to stop",
							PendingIntent.getService(this, 0, new Intent(
									"stop", null, this, this.getClass()), 0));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);*/

            Notification notification = null;
            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                    //.setContentIntent(intent)
                   // .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("IOIO service");
            notification = builder.build();
        }
        return START_REDELIVER_INTENT;
    //}

    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


/** indicates how to behave if the service is killed *//*

    int mStartMode;

    */
/** interface for clients that bind *//*

    IBinder mBinder;

    */
/** indicates whether onRebind should be used *//*

    boolean mAllowRebind;

    */
/** Called when the service is being created. *//*

    @Override
    public void onCreate() {

    }

   */
/* *//*
*/
/** The service is starting, due to a call to startService() *//*
*/
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;
    }*//*



    */
/** Called when all clients have unbound with unbindService() *//*

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    */
/** Called when a client is binding to the service with bindService()*//*

    @Override
    public void onRebind(Intent intent) {

    }

    */
/** Called when The service is no longer used and is being destroyed *//*

    @Override
    public void onDestroy() {

    }*/
}

