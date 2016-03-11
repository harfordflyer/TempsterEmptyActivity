package com.example.dhammond1.tempsteremptyactivity;

/**
 * Created by dhammond1 on 3/10/2016.
 */
public class PID {

    /*working variables*/
    long lastTime;
    public double Input, Output, Setpoint;
    double ITerm, lastInput;
    double kp, ki, kd;
    long SampleTime = 1000; //1 sec
    double outMin, outMax;
    boolean inAuto = false;
    static int MANUAL = 0;
    static int AUTOMATIC = 1;



    public void Compute()
    {
        if(!inAuto) return;
        long now = System.currentTimeMillis();
        long timeChange = (now - lastTime);
        if(timeChange>=SampleTime)
        {
      /*Compute all the working error variables*/
            double error = Setpoint - Input;
            ITerm+= (ki * error);
            if(ITerm> outMax)
                ITerm= outMax;
            else if(ITerm< outMin)
                ITerm= outMin;
            double dInput = (Input - lastInput);

      /*Compute PID Output*/
            Output = kp * error + ITerm- kd * dInput;
            if(Output> outMax)
                Output = outMax;
            else if(Output < outMin)
                Output = outMin;

      /*Remember some variables for next time*/
            lastInput = Input;
            lastTime = now;
        }
    }

    public void SetTunings(double Kp, double Ki, double Kd)
    {
        double SampleTimeInSec = ((double)SampleTime)/1000;
        kp = Kp;
        ki = Ki * SampleTimeInSec;
        kd = Kd / SampleTimeInSec;
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

        if(Output > outMax) Output = outMax;
        else if(Output < outMin) Output = outMin;

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
        ITerm = Output;
        if(ITerm> outMax) ITerm= outMax;
        else if(ITerm< outMin) ITerm= outMin;
    }
}
