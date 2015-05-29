package com.example.administrator.powermanagement.Config;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.powermanagement.Admins.DBAdapter;
import com.example.administrator.powermanagement.MainActivity;
import com.example.administrator.powermanagement.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Created by Administrator on 15/5/13.
 */
public class DebugAdmin {

    Context context = null;
    DBAdapter adapter = null;

    SharedPreferences sharedPreferences = null;
    String KEY_COMPRESS = "KEY_COMPRESS", KEY_HEURISTIC = "KEY_HEURISTIC", KEY_PREDICTION = "KEY_PREDICTION",
            KEY_PATTERNS = "KEY_PATTERNS";

    final static int PATTERN_TAG = 0;
    final static int MONITOR_TAG = 1;

    public DebugAdmin(Context ctx){
        context = ctx;
        adapter = DBAdapter.getInstance(context);
        sharedPreferences = context.getSharedPreferences(MainActivity.PREF_NAME, 0);
    }

    public void outputToFile(int type){

        String filename = "";

        // get the file name
        Calendar calendar = Calendar.getInstance();
        int filename0 = calendar.get(Calendar.DAY_OF_MONTH);
        int filename1 = calendar.get(Calendar.HOUR_OF_DAY);
        int filename2 = calendar.get(Calendar.MINUTE);
        if(type == PATTERN_TAG){
            filename = filename0 + "_" + filename1 + "_" + filename2 +".dat";
        }
        else if (type == MONITOR_TAG){
            filename = filename0 + "_" + filename1 + "_" + filename2 +".txt";
        }
        // write to file
        try{
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/" + context.getString(R.string.app_name));
            if(!directory.exists()){
                directory.mkdirs();
            }
            File file = new File(directory, filename);
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write("Heuristic times: "+sharedPreferences.getInt(KEY_HEURISTIC,0)+"\n");
            osw.write("Prediction times: "+sharedPreferences.getInt(KEY_PREDICTION,0)+"\n");
            osw.write("Patterns times: "+sharedPreferences.getInt(KEY_PATTERNS,0)+"\n");
            osw.write("Compress times: "+sharedPreferences.getInt(KEY_COMPRESS,0)+"\n");
            try{
                adapter.open();
            } catch (SQLException e){
                e.printStackTrace();
                return;
            }
            Cursor c;
            if(type == PATTERN_TAG){
                c = adapter.getAllPatterns();
            }
            else{
                c = adapter.getAllMonitor();
            }
            if(c.moveToFirst()){
                do{
                    osw.write(displayData(c));
                    osw.flush();
                } while (c.moveToNext());
            }
            adapter.close();
            osw.close();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        Toast.makeText(context, "successfully write to "+filename, Toast.LENGTH_LONG).show();
    }

    private String displayData(Cursor c){
        String result = "";
        for (int i = 0; i < c.getColumnCount() - 1; i++){
            result += c.getString(i) + "\t";
        }
        result += c.getString(c.getColumnCount() - 1) + "\n";
        return result;
    }
}
