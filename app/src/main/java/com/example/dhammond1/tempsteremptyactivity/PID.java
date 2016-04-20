package com.example.dhammond1.tempsteremptyactivity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dhammond1 on 3/10/2016.
 */
public class PID {

    /*working variables*/
    Context m_context;
    long lastTime;
    public double Input, pwmOutput, Setpoint;
    double ITerm, lastInput;
    double kp, ki, kd;
    long SampleTime = 1000; //1 sec
    double outMin, outMax;
    boolean inAuto = false;
    public static int MANUAL = 0;
    public static int AUTOMATIC = 1;
    private int controllDirection;
    java.util.Timer m_pid_controlLoop;
    int m_period;
    boolean m_enabled;
    int DIRECT = 0;
    int REVERSE = 1;
    int initalWait = 60000;
    PIDSource pidSource;
    PIDOutput pidOutput;

    public PID(PIDSource source, PIDOutput output)
    {
        pidSource = source;
        pidOutput = output;
        controllDirection = DIRECT;
    }

    public void Stop()
    {
        m_pid_controlLoop.cancel();
    }

    public void StartPID(int period)
    {
        Log.d("PID_LOOP_SAMPLE",String.valueOf(period));
        m_pid_controlLoop = new java.util.Timer();
        m_period = period;
        //wait 60 seconds before we start trying to adjust the temperature
        m_pid_controlLoop.schedule(new PIDTask(this), initalWait, (long) (m_period ));

    }



    public void free() {
        m_pid_controlLoop.cancel();
        m_pid_controlLoop = null;
    }

    private class PIDTask extends TimerTask {

        private PID m_controller;

        public PIDTask(PID controller) {
            if (controller == null) {
                throw new NullPointerException("Given PIDController was null");
            }
            m_controller = controller;
        }

        public void run() {
            //Log.d("StartingCompute","RunningFromTask");
            m_controller.Compute();
        }
    }

    public synchronized void enable() {
        m_enabled = true;
    }


    public synchronized void disable() {

        m_enabled = false;
    }

    /**
     * Return true if PIDController is enabled.
     */
    public synchronized boolean isEnable() {
        return m_enabled;
    }


    public void Compute()
    {
        Log.d("PID_LOOP_SAMPLE",String.valueOf(m_period));
        if(!inAuto)
        {
            return;
        }
        Input = pidSource.pidGet();
        long now = System.currentTimeMillis();
        long timeChange = (now - lastTime);
        if(m_enabled) {
            synchronized (this) {
                if (timeChange >= SampleTime) {
      /*Compute all the working error variables*/
                    double error = Setpoint - Input;
                    if(error < 0)
                    {
                        pwmOutput = 0;
                        pidOutput.pidWrite(pwmOutput);
                        return;
                    }
                    if(controllDirection == DIRECT)
                    {
                        error = Math.abs(error);
                    }

                    ITerm += (ki * error);
                    if (ITerm > outMax)
                        ITerm = outMax;
                    else if (ITerm < outMin)
                        ITerm = outMin;
                    double dInput = (Input - lastInput);

                    /*Compute PID Output*/
                    Log.d("setpoint:", String.valueOf((double)Setpoint));
                    Log.d("input:", String.valueOf((double)Input));
                    Log.d("kp", String.valueOf((double)kp));
                    Log.d("error:", String.valueOf((double)error));
                    Log.d("ITerm:", String.valueOf((double)ITerm));
                    Log.d("lastInput:", String.valueOf((double)lastInput));
                    Log.d("dInput:", String.valueOf((double)dInput));
                    pwmOutput = kp * error + ITerm - kd * dInput;

                    if (pwmOutput > outMax)
                        pwmOutput = outMax;

                    else if (pwmOutput < outMin)
                        pwmOutput = outMin;

                    Log.d("PIDOutput", String.valueOf(pwmOutput));
                    /*Remember some variables for next time*/
                    lastInput = Input;
                    lastTime = now;
                    pidOutput.pidWrite(pwmOutput);
                    /*//send the results back to the main acitivity
                    Intent intent = new Intent("results");
                    intent.putExtra("output", Output);
                    m_context.sendBroadcast(intent);
                    LocalBroadcastManager.getInstance(m_context.getApplicationContext()).sendBroadcast(intent);*/

                   // Log.d("InputPIDClass", String.valueOf(Input));
                   // Log.d("OutputPIDClass", String.valueOf(Output));
                }
            }
        }
    }

    public void SetContext(Context context)
    {
        m_context = context;
    }

    public void SetTunings(double Kp, double Ki, double Kd)
    {
        double SampleTimeInSec = ((double)SampleTime)/1000;
        kp = Kp;
        ki = Ki * SampleTimeInSec;
        kd = Kd / SampleTimeInSec;

        if(controllDirection == REVERSE)
        {
            kp = -kp;
            ki = -ki;
            kd = -kd;
            Log.d("kp sign is; ", String.valueOf((double)kp));
            /*kp = (0 - kp);
            ki = (0 - ki);
            kd = (0 - kd);*/
        }
    }

    public void SetSampleTime(int NewSampleTime)
    {
        if (NewSampleTime > 0)
        {
            double ratio  = (double)NewSampleTime
                    / (double)SampleTime;
            ki *= ratio;
            kd /= ratio;
            SampleTime = NewSampleTime;
        }
    }

    public void SetOutputLimits(double Min, double Max)
    {
        if(Min > Max) return;
        outMin = Min;
        outMax = Max;

        if(pwmOutput > outMax) pwmOutput = outMax;
        else if(pwmOutput < outMin) pwmOutput = outMin;

        if(ITerm> outMax) ITerm= outMax;
        else if(ITerm< outMin) ITerm= outMin;
    }

    public void SetMode(int Mode)
    {
        boolean newAuto = (Mode == AUTOMATIC);
        if(newAuto && !inAuto)
        {  /*we just went from manual to auto*/
            Initialize();
        }
        inAuto = newAuto;
    }

    public void Initialize()
    {
        lastInput = Input;
        ITerm = pwmOutput;
        if(ITerm > outMax) {
            ITerm = outMax;
        }
        else if(ITerm < outMin){
            ITerm= outMin;
        }
    }

    public void SetControllerDirection(int direction)
    {
        controllDirection = direction;
    }
}
