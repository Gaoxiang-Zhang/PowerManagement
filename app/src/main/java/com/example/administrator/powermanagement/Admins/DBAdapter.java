package com.example.administrator.powermanagement.Admins;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

public final class DBAdapter {

    //The instance used by all classes in this project
    private static DBAdapter commonAdpater = null;

    // key name of the first database
    static final String KEY_ROWID = "_id";
    static final String KEY_WEEK = "week";
    static final String KEY_HOUR = "hour";
    static final String KEY_MIN = "minute";
    static final String KEY_LATITUDE = "latitude";
    static final String KEY_LONGITUDE = "longitude";
    static final String KEY_BATTERY = "battery";
    static final String KEY_CHARGE = "charge";
    static final String KEY_APPS = "applications";
    static final String KEY_BRIGHTNESS = "brightness";
    static final String KEY_TIMEOUT = "timeout";
    static final String KEY_SOUND = "sound";
    static final String KEY_FLOW = "flow";
    static final String KEY_MOBILE = "mobile";

    static final String TAG = "DBAdapter";

    // extra key names of the custom table
    static final String KEY_NAME = "name";
    static final String KEY_START = "start_time";
    static final String KEY_END = "end_time";
    static final String KEY_SCREEN = "screen";
    static final String KEY_WIFI = "wifi";
    static final String KEY_GPRS = "gprs";
    static final String KEY_TOOTH = "tooth";
    static final String KEY_TOGGLE = "toggle";

    // extra key names of the monitor table
    static final String KEY_USING = "isScreenOn";
    static final String KEY_MODIFY = "interaction";
    static final String KEY_PREDICTION = "prediction";
    static final String KEY_HEURISTIC = "heuristic";


    // Database name, table name and version
    static final String DATABASE_NAME = "PowerDB";
    static final String DATABASE_TABLE_PATTERN = "patterns";
    static final String DATABASE_TABLE_CUSTOM = "customs";
    static final String DATABASE_TABLE_MONITOR = "monitoring";
    static final int DATABASE_VERSION = 1;

    // SQL for creating table
    static final String DATABASE1_CREATE =
            "create table "+ DATABASE_TABLE_PATTERN + " ( " +
                    KEY_ROWID + " integer primary key autoincrement, "+
                    KEY_WEEK + " text not null, "+
                    KEY_HOUR + " text not null, "+
                    KEY_MIN + " text not null, "+
                    KEY_LATITUDE + " text not null, "+
                    KEY_LONGITUDE +" text not null, " +
                    KEY_BATTERY + " text not null, "+
                    KEY_CHARGE + " text not null, "+
                    KEY_APPS + " text not null, "+
                    KEY_BRIGHTNESS + " text not null, "+
                    KEY_TIMEOUT + " text not null, "+
                    KEY_SOUND + " text not null, "+
                    KEY_FLOW + " text not null, "+
                    KEY_MOBILE + " text not null "+ ");";
    static final String DATABASE2_CREATE = "create table " + DATABASE_TABLE_CUSTOM + " ( " +
            KEY_ROWID + " integer primary key autoincrement, " +
            KEY_NAME + " text not null, " +
            KEY_START + " text not null, "+
            KEY_END + " text not null, "+
            KEY_SCREEN + " text not null, " +
            KEY_SOUND + " text not null, "+
            KEY_WIFI + " text not null, "+
            KEY_GPRS + " text not null, " +
            KEY_TOOTH + " text not null, "+
            KEY_TOGGLE + " text not null " + ");";

    static final String DATABASE3_CREATE = "create table " + DATABASE_TABLE_MONITOR + " ( " +
            KEY_ROWID + " integer primary key autoincrement, " +
            KEY_WEEK + " text not null, "+
            KEY_HOUR + " text not null, "+
            KEY_MIN + " text not null, "+
            KEY_USING + " text not null, "+
            KEY_BRIGHTNESS + " text not null, "+
            KEY_TIMEOUT + " text not null, "+
            KEY_SOUND + " text not null, "+
            KEY_WIFI + " text not null, "+
            KEY_GPRS + " text not null, " +
            KEY_TOOTH + " text not null, "+
            KEY_MODIFY + " text not null, "+
            KEY_HEURISTIC + " text not null, " +
            KEY_PREDICTION + " text not null " + ");";


    // local variable
    final Context context;
    DatabaseHelper databaseHelper;
    SQLiteDatabase database;

    /**
     * DBAdapter: constructor to get context and database helper
     */
    public DBAdapter(Context ctx){
        this.context = ctx;
        databaseHelper = new DatabaseHelper(context);
    }

    /**
     * getInstance: get the static instance
     */
    public static DBAdapter getInstance(Context ctx){
        if(commonAdpater == null){
            commonAdpater = new DBAdapter(ctx);
        }
        return commonAdpater;
    }

    /**
     * DatabaseHelper: a helper class to manage database creation and version management
     */
    private static class DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper(Context context){
            // args: Context, String name, SQLiteDatabase.CursorFactory, int version
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            // Create database table with given create SQL
            db.execSQL(DATABASE1_CREATE);
            db.execSQL(DATABASE2_CREATE);
            db.execSQL(DATABASE3_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            // Drop database table
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE_PATTERN);
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE_CUSTOM);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MONITOR);
            onCreate(db);
        }
    }


    /**
     * open(): create and open a database that will used for reading and writing
     */
    public DBAdapter open() throws SQLException{
        database = databaseHelper.getWritableDatabase();
        return this;
    }

    /**
     * close(): close the database
     */
    public void close(){
        databaseHelper.close();
    }

    /**
     * insertPattern: insert a new pattern into the database
     */
    public long insertPattern(String[] params){
        ContentValues initValues = new ContentValues();
        initValues.put(KEY_WEEK, params[0]);
        initValues.put(KEY_HOUR, params[1]);
        initValues.put(KEY_MIN, params[2]);
        initValues.put(KEY_LATITUDE, params[3]);
        initValues.put(KEY_LONGITUDE, params[4]);
        initValues.put(KEY_BATTERY, params[5]);
        initValues.put(KEY_CHARGE, params[6]);
        initValues.put(KEY_APPS, params[7]);
        initValues.put(KEY_BRIGHTNESS, params[8]);
        initValues.put(KEY_TIMEOUT, params[9]);
        initValues.put(KEY_SOUND, params[10]);
        initValues.put(KEY_FLOW, params[11]);
        initValues.put(KEY_MOBILE, params[12]);
        return database.insert(DATABASE_TABLE_PATTERN, null, initValues);
    }

    /**
     * retrieve all patterns in database
     */
    public Cursor getAllPatterns(){
        return database.query(DATABASE_TABLE_PATTERN, new String[]{KEY_WEEK, KEY_HOUR, KEY_MIN, KEY_LATITUDE,
                KEY_LONGITUDE, KEY_BATTERY, KEY_CHARGE, KEY_APPS, KEY_BRIGHTNESS, KEY_TIMEOUT, KEY_SOUND,
                KEY_FLOW, KEY_MOBILE },null, null, null, null, null);
    }


    public int insertCustom(String[] params){
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, params[0]);
        contentValues.put(KEY_START, params[1]);
        contentValues.put(KEY_END, params[2]);
        contentValues.put(KEY_SCREEN, params[3]);
        contentValues.put(KEY_SOUND, params[4]);
        contentValues.put(KEY_WIFI, params[5]);
        contentValues.put(KEY_GPRS, params[6]);
        contentValues.put(KEY_TOOTH, params[7]);
        contentValues.put(KEY_TOGGLE, params[8]);
        return (int)database.insert(DATABASE_TABLE_CUSTOM, null, contentValues);
    }

    /**
     * getAllCustom: retrieve all customs in database
     */
    public Cursor getAllCustoms(){
        return database.query(DATABASE_TABLE_CUSTOM, new String[]{KEY_ROWID, KEY_NAME, KEY_START, KEY_END,
                KEY_SCREEN, KEY_SOUND, KEY_WIFI, KEY_GPRS, KEY_TOOTH, KEY_TOGGLE}, null, null, null, null, null);
    }

    /**
     * deleteCustom: delete custom from database with given id
     */
    public boolean deleteCustom(long rowID){
        return database.delete(DATABASE_TABLE_CUSTOM, KEY_ROWID + " = " + rowID, null) > 0;
    }

    /**
     * updateCustom: update custom with given id and data
     */
    public boolean updateCustom(String[] params){
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, params[1]);
        contentValues.put(KEY_START, params[2]);
        contentValues.put(KEY_END, params[3]);
        contentValues.put(KEY_SCREEN, params[4]);
        contentValues.put(KEY_SOUND, params[5]);
        contentValues.put(KEY_WIFI, params[6]);
        contentValues.put(KEY_GPRS, params[7]);
        contentValues.put(KEY_TOOTH, params[8]);
        contentValues.put(KEY_TOGGLE, params[9]);
        return database.update(DATABASE_TABLE_CUSTOM, contentValues, KEY_ROWID + "=" + params[0], null) > 0;
    }

    public int insertMonitor(String[] params){
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_WEEK, params[0]);
        contentValues.put(KEY_HOUR, params[1]);
        contentValues.put(KEY_MIN, params[2]);
        contentValues.put(KEY_USING, params[3]);
        contentValues.put(KEY_BRIGHTNESS, params[4]);
        contentValues.put(KEY_TIMEOUT, params[5]);
        contentValues.put(KEY_SOUND, params[6]);
        contentValues.put(KEY_WIFI, params[7]);
        contentValues.put(KEY_GPRS, params[8]);
        contentValues.put(KEY_TOOTH, params[9]);
        contentValues.put(KEY_MODIFY, params[10]);
        contentValues.put(KEY_HEURISTIC, params[11]);
        contentValues.put(KEY_PREDICTION, params[12]);
        return (int)database.insert(DATABASE_TABLE_MONITOR, null, contentValues);
    }

    /**
     * getAllCustom: retrieve all customs in database
     */
    public Cursor getAllMonitor(){
        return database.query(DATABASE_TABLE_MONITOR, new String[]{KEY_WEEK, KEY_HOUR, KEY_MIN, KEY_USING,
                KEY_BRIGHTNESS, KEY_TIMEOUT, KEY_SOUND, KEY_WIFI, KEY_GPRS, KEY_TOOTH, KEY_MODIFY, KEY_HEURISTIC,
                KEY_PREDICTION}, null, null, null, null, null);
    }
}
