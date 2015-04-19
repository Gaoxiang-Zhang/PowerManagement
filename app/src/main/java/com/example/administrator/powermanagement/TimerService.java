package com.example.administrator.powermanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Timer Service for periodical monitoring
 */
public class TimerService extends Service {

    AlarmManager alarmManager;
    Intent timeIntent;
    PendingIntent pendingIntent;

    @Override
    public void onCreate(){
        super.onCreate();


        // time interval in ms, here is 1 min
        long time_interval = 1000 * 60;

        // Initialize alarm manager for periodical broadcast
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        timeIntent = new Intent();
        timeIntent.setAction("com.example.administrator.powermanagement.timer");

        // Retrieve a pending intent that will perform a broadcast, like calling Context.sendBroadcast;
        // params: Context context, request code, intent, flags
        pendingIntent = PendingIntent.getBroadcast(this,0,timeIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule a repeating alarm
        // params [0]: ELAPSED_REALTIME(_WAKEUP): time is counted since boot; RTC(_WAKEUP): absolute time
        // params [1]: if elapsed, SystemClock.elapsedRealtime, else if RTC, System.currentTimeMills
        // params [2]: time interval in ms
        // params [3]: pending intent
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis()+time_interval,time_interval,pendingIntent);

    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        // cancel the alarm
        alarmManager.cancel(pendingIntent);
    }
}
