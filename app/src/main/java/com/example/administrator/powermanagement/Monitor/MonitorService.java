package com.example.administrator.powermanagement.Monitor;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.example.administrator.powermanagement.BluetoothAdmin;
import com.example.administrator.powermanagement.DBAdapter;
import com.example.administrator.powermanagement.GPSAdmin;
import com.example.administrator.powermanagement.LocationAdmin;
import com.example.administrator.powermanagement.NetworkAdmin;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/**
 * MonitorService for monitoring and writing to database
 */
public class MonitorService extends Service {

    DBAdapter dbAdapter = null;
    BroadcastReceiver receiver = null;
    IntentFilter intentFilter = null;

    NetworkAdmin networkAdmin = null;
    GPSAdmin gpsAdmin = null;
    BluetoothAdmin bluetoothAdmin = null;

    Intent thisIntent = null;
    Intent timeService = null;

    String address = null;
    LocationAdmin locationAdmin = null;

    BroadcastReceiver locationReceiver = null;
    IntentFilter locationFilter = null;

    final static long KBYTE = 1024L;
    private long RxBytes = 0;
    private long TxBytes = 0;
    private long MobileRx = 0;
    private long MobileTx = 0;

    private PowerManager powerManager = null;

    private int old_second = 60;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        thisIntent = intent;
        return 0;
    }

    @Override
    public void onCreate(){

        // Initialize the database adapter
        dbAdapter = DBAdapter.getInstance(this);

        // Initialize the hardware manager
        networkAdmin = new NetworkAdmin(this);
        gpsAdmin = new GPSAdmin(this);
        bluetoothAdmin = new BluetoothAdmin();
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);

        // Initialize location admin and set receiver
        locationAdmin = new LocationAdmin(getApplicationContext());
        locationFilter = new IntentFilter();
        locationFilter.addAction("com.example.administrator.powermanagement.location");
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                address = intent.getStringExtra("address");
            }
        };
        registerReceiver(locationReceiver,locationFilter);

        // receiver for getting timer task
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.powermanagement.timer");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                locationAdmin.startLocationService();
                readSystemValue();
            }
        };
        registerReceiver(receiver, intentFilter);

        startTimeService();

        Log.d("monitor_info","start service");
    }

    /**
     * startTimeService: start timer
     */
    private void startTimeService(){
        timeService = new Intent(this,TimerService.class);
        this.startService(timeService);
    }


    /**
     * readSystemValue: get system values for monitoring and store them into database
     */
    private void readSystemValue(){

        // Monitoring params
        int week, hour, minute, second;
        String location;
        String all_apps, running_apps;
        int battery_level, battery_status;
        int brightness, screenoff;
        boolean wifi, tooth, gps, hotspot, gprs;
        long all_flow, mobile_flow;
        boolean isUsing;

        try{
            dbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
        }

        // Get time info
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        week = calendar.get(Calendar.DAY_OF_WEEK);
        second = calendar.get(Calendar.SECOND);

        // Get position info
        location = address;

        // Get application info
        all_apps = getAllApps();
        running_apps = getRunningApps();

        // Get battery status and level
        battery_level = (int)getBatteryLevel();
        battery_status = getBatteryStatus();

        // Get current Brightness and timeout value
        brightness = getBrightnessValue();
        screenoff = getScreenOffTime();

        // get hardware status
        wifi = networkAdmin.isWifiConnected();
        tooth = (bluetoothAdmin.checkBluetooth() == 1);
        gprs = networkAdmin.isMobileConnected();
        hotspot = networkAdmin.isHotspotConnected(thisIntent);
        gps = gpsAdmin.isGPSOn();

        // get network flow
        all_flow = getAllFlow();
        mobile_flow = getMobileFlow();

        // get using info
        isUsing = powerManager.isScreenOn();

        // check if data is adequate
        boolean isFit = checkData(second,location);

        // Write the data to database
        Log.d("monitor_info","get all info "+second+"/"+location);
        if(isFit){
            dbAdapter.insertPattern(week,hour,minute,location,all_apps,running_apps,battery_level,
                    brightness,screenoff,battery_status,wifi,gprs,tooth,gps,hotspot,all_flow,
                    mobile_flow,isUsing);
            Log.d("monitor_info", minute + "/" + all_apps + "/" + battery_status + "/" + brightness + "/" +
                    screenoff + "/" + all_flow + ":" + second+"..."+location);
            Log.d("monitor_info","write data to database");
        }
        dbAdapter.close();
    }

    /**
     * getAllApps: get all apps with "|" as a seperator
     */
    private String getAllApps(){
        String result = null;
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = getPackageManager();
        final List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = activityManager.getRunningAppProcesses();
        for (int i = 0 ; i< runningTaskInfos.size() ; i++){
            try {
                CharSequence c = packageManager.getApplicationLabel(packageManager.getApplicationInfo(
                        runningTaskInfos.get(i).processName, PackageManager.GET_META_DATA ));
                if(c!=null) {
                    result = result + '|' + c.toString();
                }
            } catch (PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * getRunningApps: get current running apps with "|" as a seperator
     */
    private String getRunningApps(){
        String result = null;
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = getPackageManager();
        final List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = activityManager.getRunningAppProcesses();
        for (int i = 0 ; i< runningTaskInfos.size() ; i++){
            try {
                int importance = runningTaskInfos.get(i).importance;
                if ( importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                        importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE ||
                        importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE){
                    CharSequence c = packageManager.getApplicationLabel(packageManager.getApplicationInfo(
                            runningTaskInfos.get(i).processName, PackageManager.GET_META_DATA ));
                    if( c!=null ){
                        result = result + '|' + c.toString();
                    }
                }
            } catch (PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * getBatteryLevel: get the current battery level
     */
    private float getBatteryLevel(){
        Intent batteryIntent = registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1){
            return 50.0f;
        }
        return ((float)level / (float)scale) * 100.0f;

    }

    /**
     * getBatteryStatus: get battery charging status
     * return: 0: not charging; 1: usb; 2: ac; 3:others
     */
    private int getBatteryStatus(){
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int type = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if ( status == -1 || type == -1){
            return -1;
        }
        boolean isCharging = ( status == BatteryManager.BATTERY_STATUS_CHARGING );
        boolean usbCharge = ( type == BatteryManager.BATTERY_PLUGGED_USB );
        boolean acCharge = ( type == BatteryManager.BATTERY_PLUGGED_AC );

        // If use usb charging
        if(usbCharge){
            return 1;
        }
        // If use AC charging
        else if(acCharge){
            return 2;
        }
        // If not charging
        if(!isCharging){
            return 0;
        }
        // If use other charging type
        return 3;
    }

    /**
     * getBrightnessValue: read and return brightness value (10~255)
     */
    private int getBrightnessValue(){
        float curBrightnessValue = 0;
        final String BRIGHTNESS_STRING = Settings.System.SCREEN_BRIGHTNESS;
        try{
            curBrightnessValue = Settings.System.getInt(
                    getContentResolver(), BRIGHTNESS_STRING);
        }catch (Settings.SettingNotFoundException e){
            Log.d("monitor_info","cannot get brightness");
            e.printStackTrace();
        }
        return (int)curBrightnessValue;
    }

    /**
     * getScreenOffTime: get current screen off time
     */
    private int getScreenOffTime(){
        int time = 0;
        try{
            time = Settings.System.getInt(getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return time;
    }

    /**
     * getAllFlow: get all network flow in the last interval
     */
    private long getAllFlow(){
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        long totalTxBytes = TrafficStats.getTotalTxBytes();
        long usedRxBytes = totalRxBytes - RxBytes;
        long usedTxBytes = totalTxBytes - TxBytes;
        RxBytes = totalRxBytes;
        TxBytes = totalTxBytes;
        return (usedRxBytes + usedTxBytes) / KBYTE;
    }

    /**
     * getMobileFlow: get mobile network flow in the last interval
     */
    private long getMobileFlow(){
        long totalMobileRxBytes = TrafficStats.getMobileRxBytes();
        long totalMobileTxBytes = TrafficStats.getMobileTxBytes();
        long usedRxMobileBytes = totalMobileRxBytes - MobileRx;
        long usedTxMobileBytes = totalMobileTxBytes - MobileTx;
        MobileRx = totalMobileRxBytes;
        MobileTx = totalMobileTxBytes;
        return (usedRxMobileBytes + usedTxMobileBytes) / KBYTE;
    }

    /**
     * checkData: check if data is fit for tracking
     */
    private boolean checkData(int second, String pos){
        // If the monitor read system data for the first time
        if(old_second == 60){
            Log.d("monitor_info","block 1");
            old_second = second;
            return false;
        }
        // If cannot get location data
        if(pos == null){
            Log.d("monitor_info","block 2");
            old_second = second;
            return false;
        }
        // If the timer task is not precise
        if(Math.abs(second - old_second) > 10){
            Log.d("monitor_info","block 3");
            old_second = second;
            return false;
        }
        old_second = second;
        return true;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("monitor_info", "stop service");
        stopService(timeService);
        unregisterReceiver(receiver);
        unregisterReceiver(locationReceiver);
    }

}
