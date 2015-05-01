package com.example.administrator.powermanagement.Custom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created by Administrator on 15/4/27.
 */
public class CustomDatabase {

    // The instance used by all classes in Custom module
    private static CustomDatabase commonAdapter = null;

    // id and value pair in database
    static final String KEY_ROWID = "_id";
    static final String KEY_NAME = "name";
    static final String KEY_START = "start_time";
    static final String KEY_END = "end_time";
    static final String KEY_SCREEN = "screen";
    static final String KEY_SOUND = "sound";
    static final String KEY_WIFI = "wifi";
    static final String KEY_GPRS = "gprs";
    static final String KEY_TOOTH = "tooth";
    static final String KEY_TOGGLE = "toggle";
    static final int DATABASE_VERSION = 1;

    // database, table name
    static final String DATABASE_NAME = "PowerDB";
    static final String DATABASE_TABLE = "CUSTOM_TABLE";

    // create phase
    static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " ( " +
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
     * CustomDatabase: constructor to get context and set database helper
     */
    public CustomDatabase(Context ctx){
        this.context = ctx;
        databaseHelper = new DatabaseHelper(ctx);
    }

    /**
     * getInstance: get the static instance
     */
    public static CustomDatabase getInstance(Context ctx){
        if(commonAdapter == null){
            commonAdapter = new CustomDatabase(ctx);
        }
        return commonAdapter;
    }

    /**
     * DatabaseHelper: a helper class to manage database creation and version management
     */
    private static class DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * open(): create and open a database that will be used for reading and writing
     */
    public CustomDatabase open() throws SQLException{
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
     * insertCustom: insert a new custom into the database
     */
    public int insertPattern(String[] params){
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
        return (int)database.insert(DATABASE_TABLE, null, contentValues);
    }

    /**
     * getAllCustom: retrieve all customs in database
     */
    public Cursor getAllCustoms(){
        return database.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_START, KEY_END,
                KEY_SCREEN, KEY_SOUND, KEY_WIFI, KEY_GPRS, KEY_TOOTH, KEY_TOGGLE}, null, null, null, null, null);
    }

    /**
     * getCustom: retrieve custom with given id
     */
    public Cursor getCustom(long rowID){
        Cursor myCursor = database.query(true, DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_START, KEY_END,
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
        return database.delete(DATABASE_TABLE, KEY_ROWID + " = " + rowID, null) > 0;
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
        return database.update(DATABASE_TABLE, contentValues, KEY_ROWID + "=" + params[0], null) > 0;
    }
}
