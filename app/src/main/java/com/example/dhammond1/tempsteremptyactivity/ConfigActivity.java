package com.example.dhammond1.tempsteremptyactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Objects;

public class ConfigActivity extends AppCompatActivity {

    public String s_date;

    private EditText ed_currentKp;
    private EditText ed_currentKi;
    private EditText ed_currentKd;

    //private EditText ed_targetPit;
    private EditText ed_minTemp;
    private EditText ed_maxTemp;
    private EditText ed_fanSpeed;
    private EditText ed_sampleTime;

    private CheckBox chk_saveGraph;

    private static final String CONFIG_NAME = "AppConfig";
    private boolean b_saveGraphData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        s_date = DatabaseHandler.GetDateTime.GetDate(Calendar.getInstance());

        ed_minTemp = (EditText)findViewById(R.id.ed_minPit);
        ed_maxTemp = (EditText)findViewById(R.id.ed_maxPit);
        ed_fanSpeed = (EditText)findViewById(R.id.ed_fanSpeed);
        ed_currentKp = (EditText)findViewById(R.id.ed_Kp);
        ed_currentKi = (EditText)findViewById(R.id.ed_Ki);
        ed_currentKd = (EditText)findViewById(R.id.ed_Kd);
        ed_sampleTime = (EditText)findViewById(R.id.ed_SampleTime);
        chk_saveGraph = (CheckBox)findViewById(R.id.chk_saveGraph);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        RestoreConfigurations();

        chk_saveGraph.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                b_saveGraphData = chk_saveGraph.isChecked();
            }
        });

    }

    @Override
    public void onBackPressed(){
        if(SaveConfigurations())
        {
            Intent intent = new Intent();
            intent.putExtra("pidChanged", true);
            setResult(2, intent);
           // finish();
        }

        //I don't have to do this as the data is saved in the SharedPreferences
        //But I'm leaving it in for example purposes
        //intent.putExtra("saveGraphData", b_saveGraphData);
        //setResult(1, intent);



        finish();
    };


    public void RestoreConfigurations()
    {
        SharedPreferences config = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);

        String restoredText = config.getString("minTemp", null);
        SetTextBox(ed_minTemp, restoredText, "230");

        restoredText = config.getString("maxTemp", null);
        SetTextBox(ed_maxTemp, restoredText, "260");

        restoredText = config.getString("fanSpeed",null);
        SetTextBox(ed_fanSpeed, restoredText, "100");

        restoredText = config.getString("kp", null);
        SetTextBox(ed_currentKp, restoredText, "5");

        restoredText = config.getString("ki", null);
        SetTextBox(ed_currentKi, restoredText, "1");

        restoredText = config.getString("kd", null);
        SetTextBox(ed_currentKd, restoredText, "1");

        restoredText = config.getString("sampleTime", null);
        SetTextBox(ed_sampleTime, restoredText, "5");

        Boolean restoredBool = config.getBoolean("saveGraph", true);
        if(!restoredBool) {
            chk_saveGraph.toggle();
        }

    }

    public void SetTextBox(TextView view, String text, String defaultText)
    {
        if(text == null)
        {
            view.setText(defaultText, TextView.BufferType.EDITABLE);
        }
        else
        {
            view.setText(text, TextView.BufferType.EDITABLE);
        }
    }

    public boolean SaveConfigurations()
    {
        boolean itemsChanged = false;
        boolean pidItemsChanged = false;
        SharedPreferences config = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE).edit();
        //editor.putString("targetPit", ed_targetPit.getText().toString());
        if(!Objects.equals(config.getString("minTemp", null), ed_minTemp.getText().toString()))
        {
            editor.putString("minTemp", ed_minTemp.getText().toString());
            itemsChanged = true;
        }

        if(!Objects.equals(config.getString("maxTemp", null),ed_maxTemp.getText().toString()))
        {
            editor.putString("maxTemp", ed_maxTemp.getText().toString());
            itemsChanged = true;
        }

        if(!Objects.equals(config.getString("fanSpeed",null),ed_fanSpeed.getText().toString()))
        {
            editor.putString("fanSpeed", ed_fanSpeed.getText().toString());
            itemsChanged = true;
        }


        //PID values are going to be treated differently if they change so I
        //can change the values on the fly
        if(!Objects.equals(config.getString("kp",null), ed_currentKp.getText().toString()))
        {
            editor.putString("kp", ed_currentKp.getText().toString());
            itemsChanged = true;
            pidItemsChanged = true;
        }

        if(!Objects.equals(config.getString("ki",null), ed_currentKi.getText().toString()))
        {
            editor.putString("ki", ed_currentKi.getText().toString());
            itemsChanged = true;
            pidItemsChanged = true;
        }

        if(!Objects.equals(config.getString("kd",null), ed_currentKd.getText().toString()))
        {
            editor.putString("kd", ed_currentKd.getText().toString());
            itemsChanged = true;
            pidItemsChanged = true;
        }

        if(!Objects.equals(config.getString("sampleTime",null), ed_sampleTime.getText().toString()))
        {
            editor.putString("sampleTime", ed_sampleTime.getText().toString());
            itemsChanged = true;
        }

        if(!Objects.equals(config.getBoolean("saveGraph", true), chk_saveGraph.isChecked()))
        {
            editor.putBoolean("saveGraph", chk_saveGraph.isChecked());
            itemsChanged = true;
        }

        if(itemsChanged){
            editor.apply();
        }

        return pidItemsChanged;
    }


}
