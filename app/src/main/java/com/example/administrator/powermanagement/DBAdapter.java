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

    // key name of database
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

    // Database name, table name and version
    static final String DATABASE_NAME = "PowerDB";
    static final String DATABASE_TABLE = "patterns";
    static final int DATABASE_VERSION = 1;

    // SQL for creating table
    static final String DATABASE_CREATE =
            "create table "+ DATABASE_TABLE + " ( " +
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
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            // Drop database table
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
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
        return database.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * deletePattern: delete pattern from database
     */
    public boolean deletePattern(long rowId){
        return database.delete(DATABASE_TABLE,KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * retrieve all patterns in database
     */
    public Cursor getAllPatterns(){
        return database.query(DATABASE_TABLE,new String[]{KEY_ROWID,KEY_WEEK,KEY_HOUR,
        KEY_MIN,KEY_POSITION,KEY_APPS,KEY_USING_APPS,KEY_BATTERY,KEY_BRIGHTNESS,KEY_TIMEOUT,
        KEY_CHARGE,KEY_WIFI,KEY_GPRS,KEY_TOOTH,KEY_GPS,KEY_HOTSPOT,KEY_FLOW,KEY_MOBILE,KEY_USING}
                ,null,null,null,null,null);
    }

    /**
     * get pattern with given id
     */
    public Cursor getPattern(long rowID){
        Cursor myCursor = database.query(true,DATABASE_TABLE,new String[]{KEY_ROWID,KEY_WEEK,KEY_HOUR,
                KEY_MIN,KEY_POSITION,KEY_APPS,KEY_USING_APPS,KEY_BATTERY,KEY_BRIGHTNESS,KEY_TIMEOUT,
                KEY_CHARGE,KEY_WIFI,KEY_GPRS,KEY_TOOTH,KEY_GPS,KEY_HOTSPOT,KEY_FLOW,KEY_MOBILE,KEY_USING},
                KEY_ROWID + " = "+ rowID, null, null, null, null, null);
        if(myCursor != null){
            myCursor.moveToFirst();
        }
        return myCursor;
    }
}
