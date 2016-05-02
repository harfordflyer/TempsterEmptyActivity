package com.example.dhammond1.tempsteremptyactivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class SessionTimerService extends Service {

    Timer countUpTimer;
    boolean m_timerRunning;
    long base,count;
    protected SessionTimerClass m_TimerClass;
    static long ONE_SECOND = 1000;
    Timer sessionTimer;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String CONFIG_NAME = "AppConfig";

    public SessionTimerService() {
    }

    @Override
    public void onCreate()
    {
        if(m_TimerClass == null)
        {
            m_TimerClass = new SessionTimerClass();
            sessionTimer = new Timer();

        }
        m_timerRunning = false;
        prefs = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        editor = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE).edit();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (intent != null && intent.getAction() != null && intent.getAction().equals("stop"))
        {
            // User clicked the notification. Need to stop the service.
                nm.cancel(0);
                stopSelf();
        }
        else
        {
            try {
                ResumeTime(intent.getStringExtra("sessionTimerText"));
            }
            catch(Exception e)
            {
               //swallow this one
            }

            Notification notification = null;
            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                    //.setContentIntent(intent)
                    // .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Session Timer service");
            notification = builder.build();
        }
        return START_STICKY;


    };


    @Override
    public void onDestroy()
    {
        sessionTimer.cancel();
    }


    private void ResumeTime(String timerText)
    {
        long prefsTime = prefs.getLong("baseTime",SystemClock.elapsedRealtime());
        if(timerText.equals("00:00"))
        {
            //first time through
            base = SystemClock.elapsedRealtime();
            editor.putLong("baseTime", base);
        }
        else
        {

            long elapsedTime = SystemClock.elapsedRealtime() - prefs.getLong("baseTime",0);

            String time = m_TimerClass.formatString(elapsedTime);
            m_TimerClass.SendBroadCast(time);
            //reset the counter
            count += elapsedTime /1000;
        }
        sessionTimer.schedule(new SessionTimerClass(),ONE_SECOND,ONE_SECOND);
    }

    public class SessionTimerClass extends TimerTask
    {

        public void run()
        {
            sessionLoop();
            m_timerRunning = true;
        }



        public void sessionLoop()
        {
            try
            {
                Log.d("COUNT","Counting ONE second");
                //calculate the time
                count++;
                String time = formatString(count);
                //send the broadcast
                SendBroadCast(time);

            }
            catch(Exception e)
            {
                Log.d("TIMERTASK","Timer task error on Sleep");
            }
        }

        protected void SendBroadCast(String time)
        {
            Intent intent = new Intent("time");
            intent.putExtra("timeString", time);
            sendBroadcast(intent);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        //http://stackoverflow.com/questions/5387371/how-to-convert-minutes-to-hours-and-minutes-hhmm-in-java
        public String formatString(long seconds)
        {
            long milliseconds = seconds * 1000;
            if(TimeUnit.MILLISECONDS.toHours(milliseconds) != 0) {
                return String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(milliseconds),
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(milliseconds)),
                        TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
            }
            else
            {
                return String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(milliseconds)),
                        TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
            }
        }
    }

    public void Stop(){sessionTimer.cancel();}




    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
