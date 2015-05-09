package com.example.administrator.powermanagement;

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
    static final String KEY_POSITION = "positions";
    static final String KEY_APPS = "applications";
    static final String KEY_USING_APPS = "using_apps";
    static final String KEY_BATTERY = "battery";
    static final String KEY_BRIGHTNESS = "brightness";
    static final String KEY_TIMEOUT = "timeout";
    static final String KEY_CHARGE = "charge";
    static final String KEY_WIFI = "wifi";
    static final String KEY_GPRS = "gprs";
    static final String KEY_TOOTH = "tooth";
    static final String KEY_GPS = "gps";
    static final String KEY_HOTSPOT = "hotspot";
    static final String KEY_FLOW = "flow";
    static final String KEY_MOBILE = "mobile";
    static final String KEY_USING = "interaction";
    static final String TAG = "DBAdapter";

    // extra key name of the custom database
    static final String KEY_NAME = "name";
    static final String KEY_START = "start_time";
    static final String KEY_END = "end_time";
    static final String KEY_SCREEN = "screen";
    static final String KEY_SOUND = "sound";
    static final String KEY_TOGGLE = "toggle";

    // Database name, table name and version
    static final String DATABASE_NAME = "PowerDB";
    static final String DATABASE_TABLE_PATTERN = "patterns";
    static final String DATABASE_TABLE_CUSTOM = "customs";
    static final int DATABASE_VERSION = 1;

    // SQL for creating table
    static final String DATABASE1_CREATE =
            "create table "+ DATABASE_TABLE_PATTERN + " ( " +
                    KEY_ROWID + " integer primary key autoincrement, "+
                    KEY_WEEK + " text not null, "+
                    KEY_HOUR + " text not null, "+
                    KEY_MIN + " text not null, "+
                    KEY_POSITION + " text not null, "+
                    KEY_APPS + " text not null, "+
                    KEY_USING_APPS + " text not null, "+
                    KEY_BATTERY + " text not null, "+
                    KEY_BRIGHTNESS + " text not null, "+
                    KEY_TIMEOUT + " text not null, "+
                    KEY_CHARGE + " text not null, "+
                    KEY_WIFI + " text not null, "+
                    KEY_GPRS + " text not null, "+
                    KEY_TOOTH + " text not null, "+
                    KEY_GPS + " text not null, "+
                    KEY_HOTSPOT + " text not null, "+
                    KEY_FLOW + " text not null, "+
                    KEY_MOBILE + " text not null, "+
                    KEY_USING +" text not null" +");";
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            // Drop database table
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE_PATTERN);
            db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE_CUSTOM);
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
    public long insertPattern(int week, int hour, int minute, String position, String applications,
                              String using_apps, int battery, int brightness, int timeout,
                              int charge, boolean wifi, boolean gprs, boolean tooth,
                              boolean gps, boolean hotspot, long flow, long mobile, boolean using) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_WEEK, week);
        initialValues.put(KEY_HOUR, hour);
        initialValues.put(KEY_MIN, minute);
        initialValues.put(KEY_POSITION, position);
        initialValues.put(KEY_APPS, applications);
        initialValues.put(KEY_USING_APPS, using_apps);
        initialValues.put(KEY_BATTERY, battery);
        initialValues.put(KEY_BRIGHTNESS, brightness);
        initialValues.put(KEY_TIMEOUT, timeout);
        initialValues.put(KEY_CHARGE, charge);
        initialValues.put(KEY_WIFI, wifi);
        initialValues.put(KEY_GPRS, gprs);
        initialValues.put(KEY_TOOTH, tooth);
        initialValues.put(KEY_GPS, gps);
        initialValues.put(KEY_HOTSPOT, hotspot);
        initialValues.put(KEY_FLOW, flow);
        initialValues.put(KEY_MOBILE, mobile);
        initialValues.put(KEY_USING, using);
        return database.insert(DATABASE_TABLE_PATTERN, null, initialValues);
    }

    /**
     * deletePattern: delete pattern from database
     */
    public boolean deletePattern(long rowId){
        return database.delete(DATABASE_TABLE_PATTERN,KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * retrieve all patterns in database
     */
    public Cursor getAllPatterns(){
        return database.query(DATABASE_TABLE_PATTERN,new String[]{KEY_ROWID,KEY_WEEK,KEY_HOUR,
        KEY_MIN,KEY_POSITION,KEY_APPS,KEY_USING_APPS,KEY_BATTERY,KEY_BRIGHTNESS,KEY_TIMEOUT,
        KEY_CHARGE,KEY_WIFI,KEY_GPRS,KEY_TOOTH,KEY_GPS,KEY_HOTSPOT,KEY_FLOW,KEY_MOBILE,KEY_USING}
                ,null,null,null,null,null);
    }

    /**
     * get pattern with given id
     */
    public Cursor getPattern(long rowID){
        Cursor myCursor = database.query(true, DATABASE_TABLE_PATTERN,new String[]{KEY_ROWID,KEY_WEEK,KEY_HOUR,
                KEY_MIN,KEY_POSITION,KEY_APPS,KEY_USING_APPS,KEY_BATTERY,KEY_BRIGHTNESS,KEY_TIMEOUT,
                KEY_CHARGE,KEY_WIFI,KEY_GPRS,KEY_TOOTH,KEY_GPS,KEY_HOTSPOT,KEY_FLOW,KEY_MOBILE,KEY_USING},
                KEY_ROWID + " = "+ rowID, null, null, null, null, null);
        if(myCursor != null){
            myCursor.moveToFirst();
        }
        return myCursor;
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
     * getCustom: retrieve custom with given id
     */
    public Cursor getCustom(long rowID){
        Cursor myCursor = database.query(true, DATABASE_TABLE_CUSTOM, new String[]{KEY_ROWID, KEY_NAME, KEY_START, KEY_END,
                        KEY_SCREEN, KEY_SOUND, KEY_WIFI, KEY_GPRS, KEY_TOOTH, KEY_TOGGLE}, KEY_ROWID + " = " + rowID,
                null, null, null, null, null);
        if(myCursor != null){
            myCursor.moveToFirst();
        }
        return myCursor;
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
}
