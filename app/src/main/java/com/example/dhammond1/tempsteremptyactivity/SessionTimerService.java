package com.example.dhammond1.tempsteremptyactivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class SessionTimerService extends Service {

    Timer countUpTimer;
    boolean m_timerRunning;
    long count;
    protected SessionTimerClass m_TimerClass;
    static long ONE_SECOND = 1000;
    Timer sessionTimer;

    public SessionTimerService() {
    }

    @Override
    public void onCreate()
    {
        if(m_TimerClass == null)
        {
            m_TimerClass = new SessionTimerClass();
            sessionTimer = new Timer();
            sessionTimer.schedule(new SessionTimerClass(),ONE_SECOND,ONE_SECOND);
        }
        m_timerRunning = false;
        count = 0;
    }

    @Override
    public void onDestroy()
    {
        sessionTimer.cancel();
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
               // Thread.sleep(1000L);
                Log.d("COUNT","Counting ONE second");
                //calculate the time
                count++;
                String time = formatString(count);
                //send the broadcast
                Intent intent = new Intent("time");
                intent.putExtra("timeString", time);
                sendBroadcast(intent);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            }
            catch(Exception e)
            {
                Log.d("TIMERTASK","Timer task error on Sleep");
            }
        }
        //http://stackoverflow.com/questions/5387371/how-to-convert-minutes-to-hours-and-minutes-hhmm-in-java
        protected String formatString(long seconds)
        {
            long milliseconds = seconds * 1000;
            if(TimeUnit.MICROSECONDS.toHours(milliseconds) != 0) {
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
    public int onStartCommand(Intent intent, int flags, int startId) {


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
                    .setContentTitle("Session Timer service");
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
}
