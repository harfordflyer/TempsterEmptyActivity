package com.example.dhammond1.tempsteremptyactivity;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import junit.framework.TestCase;
/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */



public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testPID() {

        PID pid = new PID();
        pid.enable();
        pid.Input = 15;
        pid.Setpoint = 250;
        pid.SetMode(pid.AUTOMATIC);
        pid.SetTunings(10, 0, 1);
        pid.SetOutputLimits(0, 255);
        pid.SetSampleTime(1000);
        pid.SetControllerDirection(pid.REVERSE);
        pid.Initialize();

        while(true) {
            pid.Compute();
            double out = pid.Output;
            Log.d("PID_OUTPUT", String.valueOf(out));
            pid.Input = pid.Input - 1;
        }
    }

}