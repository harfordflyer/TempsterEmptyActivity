package com.example.dhammond1.tempsteremptyactivity;

/**
 * Created by dhammond1 on 3/9/2016.
 */
public class ConfigEntity {
    int _id;
    String _configDate;
    String _configTargetPit;
    String _configMinPit;
    String _configMaxPit;
    String _configFan;
    String _configKp;
    String _configKi;
    String _configKd;
    String _configSampleTime;

    public ConfigEntity()
    {}

    public ConfigEntity(String date, String targetPit, String minPit, String maxPit, String fan, String kp, String ki, String kd, String sample)
    {
        _configDate = date;
        _configTargetPit = targetPit;
        _configMinPit = minPit;
        _configMaxPit = maxPit;
        _configFan = fan;
        _configKp = kp;
        _configKi = ki;
        _configKd = kd;
        _configSampleTime = sample;
    }

    public ConfigEntity(int id, String date, String targetPit, String minPit, String maxPit, String fan, String kp, String ki, String kd, String sample)
    {
        _id = id;
        _configTargetPit = targetPit;
        _configMinPit = minPit;
        _configMaxPit = maxPit;
        _configFan = fan;
        _configKp = kp;
        _configKi = ki;
        _configKd = kd;
        _configSampleTime = sample;
    }

    public int getID(){ return this._id; }
    public void setID(int id){ this._id = id; }

    public String getDate(){return this._configDate;}
    public void setDate(String date){this._configDate = date;}

    public String getTartetPitTemp(){return this._configTargetPit;}
    public void setTargetPitTemp(String target){this._configTargetPit = target;}

    public String getMinPitTemp(){return this._configMinPit;}
    public void setMinPitTemp(String minTemp){this._configMinPit = minTemp;}

    public String getMaxPitTemp(){return this._configMaxPit;}
    public void setMaxPitTemp(String maxTemp){this._configMaxPit = maxTemp;}

    public String getFan() {return this._configFan;}
    public void setFan(String fan){this._configFan = fan;}

    public String getKP() {return this._configKp;}
    public void setKP(String kp) {this._configKp = kp;}

    public void setKI(String ki){this._configKi = ki;}
    public String getKI() {return this._configKi;}

    public String getKD() {return this._configKd;}
    public void setKD(String kd) {this._configKd = kd;}

    public String getSampleTime() {return this._configSampleTime;}
    public void setSampleTime(String sampleTime) {this._configSampleTime = sampleTime; }
}
