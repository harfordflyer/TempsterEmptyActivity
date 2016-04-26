package com.example.dhammond1.tempsteremptyactivity;

import java.io.Serializable;

/**
 * Created by dhammond1 on 3/9/2016.
 */
public class TemperatureEntry implements Serializable {
    //private vars
    int _id;

    String _date;
    String _time;
    String _pitTemp;
    String _meatTemp;

    public TemperatureEntry()
    { }

    public TemperatureEntry(String date, String time, String pitTemp, String meatTemp)
    {
        this._date = date;
        this._time = time;
        this._pitTemp = pitTemp;
        this._meatTemp = meatTemp;
    }

    public TemperatureEntry(int id, String date, String time, String pitTemp, String meatTemp)
    {
        this._id = id;
        this._date = date;
        this._time = time;
        this._pitTemp = pitTemp;
        this._meatTemp = meatTemp;
    }


    public int getID(){ return this._id; }

    public void setID(int id){ this._id = id; }

    public String getDate()  { return this._date; }

    public void setDate(String date) { this._date = date; }

    public String getTime()
    {
        return this._time;
    }

    public void setTime(String time)
    {
        this._time = time;
    }

    public String getPitTemp() { return this._pitTemp; }

    public void setPitTemp(String pitTemp)
    {
        this._pitTemp = pitTemp;
    }

    public String getMeatTemp()
    {
        return this._meatTemp;
    }

    public void setMeatTemp(String meatTemp)
    {
        this._meatTemp = meatTemp;
    }
}
