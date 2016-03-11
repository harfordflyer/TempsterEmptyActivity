package com.example.dhammond1.tempsteremptyactivity;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testPID() {
        PID pid = new PID();
        pid.SetTunings(5, 3, 3);
        pid.SetMode(1);
        pid.SetOutputLimits(240, 265);
        while(true) {
            pid.Compute();
            double out = pid.Output;
            pid.Input = out - 1;
        }
    }
}