package com.example.dhammond1.tempsteremptyactivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dhammond1 on 3/9/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    Context _context;

    private static DatabaseHandler instance;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "temperatureEntry.db";

    // Contacts table name
    public static final String TABLE_ENTRIES = "entries";

    public static final String TABLE_CONFIG = "config";

    // Contacts Table Columns names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_PIT_TEMP = "pitTemp";
    public static final String COLUMN_MEAT_TEMP = "meatTemp";

    public static final String CONFIG_ID = "id";
    public static final String CONFIG_DATE = "date";
    public static final String CONFIG_TARGET_TEMP = "config_target_temp";
    public static final String CONFIG_MIN_TEMP = "config_min_temp";
    public static final String CONFIG_MAX_TEMP = "config_max_temp";
    public static final String CONFIG_FAN_SPEED = "config_fan";

    public static final String CONFIG_KP = "config_kp";
    public static final String CONFIG_KI = "config_ki";
    public static final String CONFIG_KD = "config_kd";
    public static final String CONFIG_SAMPLE_TIME = "config_sample_Time";


    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
    }

    public static synchronized DatabaseHandler getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new DatabaseHandler(context.getApplicationContext(),null, null, DATABASE_VERSION );
        }
        return instance;
    }



    public String DeleteEntriesTable()
    {
        return "DROP TABLE IF EXISTS " + TABLE_ENTRIES;
    }

    public String DeleteConfigTable()
    {
        return "DROP TABLE IF EXISTS" + TABLE_CONFIG;
    }

    public String CreateTableString()
    {
        final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_ENTRIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_PIT_TEMP + " TEXT,"
                + COLUMN_MEAT_TEMP + " TEXT" + ")";
        return SQL_CREATE_ENTRIES;
    }

    public String CreateConfigTableString()
    {
        final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_CONFIG + "("
                + CONFIG_ID + " INTEGER PRIMARY KEY,"
                + CONFIG_DATE + " TEXT,"
                + CONFIG_TARGET_TEMP  + " TEXT,"
                + CONFIG_MIN_TEMP  + " TEXT,"
                + CONFIG_MAX_TEMP  + " TEXT,"
                + CONFIG_FAN_SPEED  + " TEXT,"
                + CONFIG_KP  + " TEXT,"
                + CONFIG_KI   + " TEXT,"
                + CONFIG_KD   + " TEXT,"
                + CONFIG_SAMPLE_TIME   + " TEXT" + ")";
        return SQL_CREATE_ENTRIES;
    }

    public boolean DoesDatabaseExist(Context context, String dbName)
    {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //start with a clean db for now
        if(DoesDatabaseExist(_context, TABLE_ENTRIES))
        {
            db.execSQL(DeleteEntriesTable());
        }

        if(DoesDatabaseExist(_context, TABLE_CONFIG))
        {
            db.execSQL(DeleteConfigTable());
        }

        db.execSQL(CreateTableString());
        db.execSQL(CreateConfigTableString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(DeleteEntriesTable());
        db.execSQL(DeleteConfigTable());

        // Create tables again
        onCreate(db);
    }

    public void addEntry(TemperatureEntry entry)
    {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        }
        catch(Exception e)
        {
            Log.d("database error", e.getMessage());
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, entry.getDate());
        values.put(COLUMN_TIME, entry.getTime());
        values.put(COLUMN_PIT_TEMP, entry.getPitTemp());
        values.put(COLUMN_MEAT_TEMP, entry.getMeatTemp());

        db.insert(TABLE_ENTRIES, null, values);
        db.close();
    }

    public void addConfigEntry(ConfigEntity entry)
    {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        }
        catch(Exception e){
            Log.d("database error", e.getMessage());
        }
        ContentValues value = new ContentValues();
        value.put(CONFIG_DATE, entry.getDate());
        value.put(CONFIG_TARGET_TEMP, entry.getTartetPitTemp());
        value.put(CONFIG_MIN_TEMP, entry.getMinPitTemp());
        value.put(CONFIG_MAX_TEMP, entry.getMaxPitTemp());
        value.put(CONFIG_FAN_SPEED, entry.getFan());

        value.put(CONFIG_KP, entry.getKP());
        value.put(CONFIG_KI, entry.getKI());
        value.put(CONFIG_KD, entry.getKD());
        value.put(CONFIG_SAMPLE_TIME, entry.getSampleTime());

        try {
            db.insert(TABLE_CONFIG, null, value);
        }
        catch (Exception ex){
            Log.d("Save config", ex.getMessage());
        }
    }
       /* public TemperatureEntry getEntry(int id)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_TEMPSTER, new String[]{
                            KEY_ID, KEY_DATE, KEY_PIT_TEMP, KEY_MEAT_TEMP}, KEY_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);
            if(cursor != null)
                cursor.moveToFirst();

            TemperatureEntry entry = new TemperatureEntry(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3));

            cursor.close();
            return entry;

        }*/

    public TemperatureEntry getLastEntry()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ENTRIES, null);
        cursor.moveToLast();
        TemperatureEntry entry = new TemperatureEntry();
        entry.setID(Integer.parseInt(cursor.getString(0)));
        entry.setDate(cursor.getString(1));
        entry.setTime(cursor.getString(2));
        entry.setPitTemp(cursor.getString(3));
        entry.setMeatTemp(cursor.getString(4));
        cursor.close();
        return entry;
    }



    public List<TemperatureEntry> getEntriesByDate(String date)
    {
        List<TemperatureEntry> listEntry = new ArrayList<TemperatureEntry>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ENTRIES  + " WHERE " + COLUMN_DATE + " = \"" + date + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor cursor = db.rawQuery(selectQuery, new String[]{date});
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
        }
        catch(Exception ex)
        {
            Log.d("GetEntriesError", ex.getMessage());
        }
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TemperatureEntry entry = new TemperatureEntry();
                entry.setID(Integer.parseInt(cursor.getString(0)));
                entry.setDate(cursor.getString(1));
                entry.setTime(cursor.getString(2));
                entry.setPitTemp(cursor.getString(3));
                entry.setMeatTemp(cursor.getString(4));
                // Adding contact to list
                listEntry.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return listEntry;
    }



    public final static class GetDateTime
    {
        public static String GetDate(Calendar calendar)
        {
            int Month = calendar.get(Calendar.MONTH) + 1;
            int Day = calendar.get(Calendar.DAY_OF_MONTH);
            int Year = calendar.get(Calendar.YEAR);

            String date = String.format("%02d/%02d/%04d", Month,Day,Year);
            return  date;
        }

        public static String GetTime(Calendar calendar)
        {
            calendar.setTime(calendar.getTime());

            String time = String.format("%1$tl %1$tM %1$tS %1$tp", calendar);
            return time;
        }
    }
}
