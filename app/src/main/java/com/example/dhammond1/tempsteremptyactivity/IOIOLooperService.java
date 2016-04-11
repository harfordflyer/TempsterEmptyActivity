package com.example.dhammond1.tempsteremptyactivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

import ioio.lib.util.android.IOIOService;

import java.io.InterruptedIOException;
import java.util.Timer;
import java.util.TimerTask;



//this was here originally
public class IOIOLooperService extends IOIOService {
    private static final String CONFIG_NAME = "AppConfig";

    int sampleTime;
    int minTemp;
    int setPoint;
    int kp, ki, kd;
    int initialWait = 15000;
    int pin45 = 45;
    int pin46 = 46;
    int pin40 = 40;
    int pin39 = 39;
    private BaseIOIOLooper baselooper;
    private Looper m_looper;
    private PID pid;
    private boolean pidLoopRunning;
    private double[] pidOutput;
    private boolean STARTUP = true;

    public static float pitVoltage = 0;
    public static float meatVoltage = 0;

    //new
    @Override
    protected synchronized IOIOLooper createIOIOLooper(){
        if ( m_looper == null )
            m_looper = new Looper();

        pidLoopRunning = false;
        return m_looper;
    }

    //new
    private class Looper extends BaseIOIOLooper implements PIDSource, PIDOutput
    {
        public DigitalOutput led_;
        public PwmOutput pwmOutput;
        public AnalogInput pitInput;
        public AnalogInput meatInput;
        public boolean isPIDRunning;
        public boolean isOutputOpen;
        int[] temperatureValues = new int[2];

        //Initialize Timer Stuff
        Timer tempTimer = new Timer();
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

        Timer ledTimer = new Timer();
        TimerTask ledLoop = new TimerTask(){
            @Override
            public void run(){
                for (int i = 0; i < 3; i++) {
                    try {
                        led_.write(true);
                        Thread.sleep(750);
                        led_.write(false);
                    }
                    catch(ConnectionLostException e)
                    {

                    }
                    catch(InterruptedException ie)
                    {

                    }
                }

                            }


        };

        protected void StopTimerTasks()
        {
            controlLoop.cancel();
            ledLoop.cancel();
            pid.Stop();
        }

        //IOIO Setup Method
        @Override
        protected void setup() throws ConnectionLostException,
                InterruptedException {
            //Setup IOIO Pins
            led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);

            try
            {
                pwmOutput = ioio_.openPwmOutput(14,10);
            }
            catch(Exception e)
            {
                if(e instanceof IllegalArgumentException)
                {
                    Log.d("Pin open","IllegalArgumentException... pin probably open");
                }
                else
                {
                    throw new RuntimeException("Digital output pin failed");
                }
            }
            pwmOutput.setDutyCycle(0.0f);
            isOutputOpen = false;

            //Increase thread priority
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            if(pidLoopRunning)
            {
                pid.Stop();
            }
            //Set timer to execute 'controlLoop' after a xxms delay every xxms
            Log.d("CONTROL LOOP TIMER:", "Scheduling control loop Timer");
            tempTimer.schedule(controlLoop, initialWait, sampleTime);

            StartPIDTask();

            Log.d("LED TIMER:", "Scheduling LED Timer");
            //timer for flahsing leds
            ledTimer.schedule(ledLoop, initialWait, initialWait);
        }

        public void StartPIDTask()
        {
            //setup the PID
            pid = new PID(this, this);
            //since array values are passed by value, we will use one here to get the
            //output from the PID algorithm
            pidOutput = new double[1];
            //get values from intent
            pid.SetControllerDirection(pid.DIRECT);
            pid.SetTunings(kp, ki, kd);
            pid.SetOutputLimits(0, 255);
            pid.SetSampleTime(sampleTime);

            pid.enable();
            pid.SetMode(pid.AUTOMATIC);
            pid.SetContext(getApplicationContext());
            //starting with a 5 second compute loop... this could be a var in shared preferences
            pid.StartPID(sampleTime);
            pid.Setpoint = setPoint; //what are we trying to get to
            pid.Initialize();

           /* //Set timer to execute 'controlLoop' after a xxms delay every xxms
            Log.d("CONTROL LOOP TIMER:", "Scheduling control loop Timer");
            t.schedule(controlLoop, initialWait, sampleTime);*/
            isPIDRunning = true;
        }

        protected synchronized void controlLoop() throws ConnectionLostException,
                InterruptedException {

            Log.d("CONTROL_LOOP_SAMP_TIME",String.valueOf(sampleTime));
            //Lets take some temps

            pitInput = ioio_.openAnalogInput(pin45);
            pitVoltage = GetVoltagesFromAnalogProbe(pitInput);

            //Log.d("avg voltage on pit ", String.valueOf(pitVoltage));
            pitInput.close();

            meatInput = ioio_.openAnalogInput(pin46);
            meatVoltage = GetVoltagesFromAnalogProbe(meatInput);

          //  Log.d("avg voltage on meat ", String.valueOf(meatVoltage));
            meatInput.close();

            //convert the voltages to real temps
            //int[] values = new int[2];
            float meatTemp = computeTemperature(meatVoltage);
            temperatureValues[0] = ConvetKelvin2Farenheight(meatTemp);

            float pitTemp = computeTemperature(pitVoltage);
            temperatureValues[1] = ConvetKelvin2Farenheight(pitTemp);


            //pid.Compute();


            //send the results back to the main acitivity
            Intent intent = new Intent("results");
            intent.putExtra("temps", temperatureValues);
            sendBroadcast(intent);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        }

        public double pidGet()
        {
            double returnVal = 0;
            returnVal = (double)temperatureValues[1];
            String val = "returning" + String.valueOf((double)returnVal) + "to PID";
            Log.d("PIDGET", val);
            return returnVal;
        }

        @Override
        public void pidWrite(double pwm)
        {
            //heres where we set the fan blowing algorithm
            float dutyCycle = (float)pwm / 255.0f;
            //set the duty cycle on pin 14
            try
            {
                String val = "PIDWRITE " + String.valueOf((double)dutyCycle);
                Log.d("pidWrite",val);
                if(pwmOutput != null)
                {
                    pwmOutput.setDutyCycle(dutyCycle);
                }
            }
            catch(ConnectionLostException e)
            {
                Log.d("Connection","Connection Lost Exception");
            }
        }
    }


    private  float GetVoltagesFromAnalogProbe(AnalogInput input) throws ConnectionLostException, InterruptedException
    {
        double averageVolts = 0;
        double totalVolts = 0;
        int samples = 0;


        input.setBuffer(256);
        samples = input.available();

        for (int i = 0; i < 256; i++) {
            totalVolts += input.getVoltageBuffered();
        }

        averageVolts = totalVolts / (double)256;
        return (float)averageVolts;
    }


    public float computeTemperature( double measuredVoltage )
    {
        //https://sourceforge.net/p/udssupervisor/code/ci/master/tree/src/main/java/org/deeg/uds/SteinhartHart.java

        double A, B, C, voltageReference;
        int    Resistance = 22200;
        voltageReference = 3.3;
        A = 2.3067434E-4;
        B = 2.3696596E-4;
        C = 1.2636414E-7;

        double diff = voltageReference - measuredVoltage;
        double measuredR = measuredVoltage * Resistance;
        double resistence = measuredR / diff;

        double logResistence = Math.log( resistence );
        double t = A +
                B * logResistence +
                C * Math.pow( logResistence, 3.0 );

        double T = 1.0 / t;

        double f_double = ((T- 273.25) * 9.0) / 5.0 + 32.0;
        int f_temp = (int) f_double;
        //Log.d("f_temp", Integer.toString(f_temp));
        return (float)T;
    }

    public int ConvertKelvin2Celcius(float temp)
    {
        float k = temp - (float)273.25;
        return (int)k;
    }

    public int ConvetKelvin2Farenheight(float temp)
    {
        float k = temp - (float)273.25;
        double f = (k * 9.0)/5.0 + 32.0;
        return (int)f;
    }


    @Override
    public void onDestroy()
    {
        //kill the timer tasks
        m_looper.StopTimerTasks();
        Log.d("onDestroy","being called");
        super.onDestroy();
        Log.d("onDestroy", "being called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String sample = intent.getStringExtra("sampleTime");
        sampleTime = Integer.parseInt(sample) * 1000;
        String minT = intent.getStringExtra("minTemp");
        minTemp = Integer.parseInt(minT);
        String k = intent.getStringExtra("kp");
        kp = Integer.parseInt(k);
        k = intent.getStringExtra("ki");
        ki = Integer.parseInt(k);
        k = intent.getStringExtra("kd");
        kd = Integer.parseInt(k);
        String target = intent.getStringExtra("targetPitTemp");
        setPoint = Integer.parseInt(target);

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


}

