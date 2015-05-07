package com.example.administrator.powermanagement.Shortcut;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.example.administrator.powermanagement.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * ShortcutService: receive multiple broadcast and send it to general fragment
 */
public class ShortcutService extends Service {
    // String for change action of gps and hotspot
    final String GPS_CHANGE_ACTION = "android.location.PROVIDERS_CHANGED";
    final String HOT_CHANGE_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    // arraylist that holds key-value pairs of time stamp and battery level
    ArrayList<Integer[]> batteryPair;

    // core receiver to get multiple event and to pack as a single broadcast
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String tag="Debug Info";
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent broadcast_intent = new Intent();
            //Initial value is 0
            int wifiState,bluetoothState;
            // WIFI_AP STATE
            final int HOTSPOT_OPEN = 13, HOTSPOT_CLOSE = 11;
            // If get broadcast from WiFi
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d(tag, "Wifi未连接");
                        broadcast_intent.putExtra("wifi_state",-1);
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.d(tag, "Wifi正在断开");
                        broadcast_intent.putExtra("wifi_state",0);
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(tag, "Wifi已连接");
                        broadcast_intent.putExtra("wifi_state",1);
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.d(tag, "Wifi正在连接");
                        broadcast_intent.putExtra("wifi_state",0);
                        break;
                    default:
                        broadcast_intent.putExtra("wifi_state",0);
                }
            }
            // If get broadcast from mobile data
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mMobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if(NetworkInfo.State.CONNECTED==mMobile.getState()){
                    Log.d(tag, "数据连接");
                    broadcast_intent.putExtra("data_state", 1);
                }else{
                    Log.d(tag, "无数据连接");
                    broadcast_intent.putExtra("data_state", -1);
                }
            }
            // if get broadcast from airplane mode
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())){
                int modeIdx = Settings.System.getInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON,0);
                boolean isEnabled = (modeIdx == 1);
                if(isEnabled) {
                    Log.d(tag, "飞行模式on");
                    broadcast_intent.putExtra("plane_state",1);
                }else{
                    Log.d(tag, "飞行模式off");
                    broadcast_intent.putExtra("plane_state",-1);
                }
            }
            // If get broadcast from GPS module
            if (intent.getAction().matches(GPS_CHANGE_ACTION)){
                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if(statusOfGPS){
                    Log.d(tag,"GPS on");
                    broadcast_intent.putExtra("gps_state",1);
                }else{
                    Log.d(tag,"GPS off");
                    broadcast_intent.putExtra("gps_state",-1);
                }
            }
            // If get broadcast from Bluetooth
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())){
                bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (bluetoothState){
                    case BluetoothAdapter.STATE_ON:
                        Log.d(tag,"蓝牙打开");
                        broadcast_intent.putExtra("tooth_state",1);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(tag,"蓝牙关闭");
                        broadcast_intent.putExtra("tooth_state",-1);
                        break;
                    default:
                        broadcast_intent.putExtra("tooth_state",0);
                }
            }

            // If get broadcast from hotspot module
            if (intent.getAction().matches(HOT_CHANGE_ACTION)){
                int state = intent.getIntExtra("wifi_state",0);
                switch (state){
                    case HOTSPOT_CLOSE:
                        broadcast_intent.putExtra("hotspot_state",-1);
                        break;
                    case HOTSPOT_OPEN:
                        broadcast_intent.putExtra("hotspot_state",1);
                        break;
                    default:
                        broadcast_intent.putExtra("hotspot_state",0);
                }
            }
            // If get broadcast from battery change
            if (intent.getAction().matches(Intent.ACTION_BATTERY_CHANGED)){
                Intent batteryIntent = new Intent();
                batteryIntent.setAction("com.example.administrator.powermanagement.battery");

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                // calculate battery level and bond to intent
                level = (int)(level/(float)scale*100);
                batteryIntent.putExtra("battery_level",level+"%");
                // if the phone is charging
                String result;
                if(status == BatteryManager.BATTERY_STATUS_CHARGING){
                    switch (plug){
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            result = getString(R.string.acCharging);
                            break;
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            result = getString(R.string.usbCharging);
                            break;
                        case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                            result = getString(R.string.wirelessCharging);
                            break;
                        default:
                            result = getString(R.string.defaultCharging);
                            break;
                    }
                    batteryPair.clear();
                }
                else {
                    result = calculateRemainTime(level);
                }
                batteryIntent.putExtra("remain_time",result);
                sendBroadcast(batteryIntent);
            }
            //send broadcast
            broadcast_intent.setAction("com.example.administrator.powermanagement.shortcutservice");
            sendBroadcast(broadcast_intent);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * onCreate: mainly define filter and tight it to receiver
     */
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        // filter the event to be trapped
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mFilter.addAction(GPS_CHANGE_ACTION);
        mFilter.addAction(HOT_CHANGE_ACTION);
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, mFilter);

        batteryPair = new ArrayList<>();
    }


    private String calculateRemainTime(int level){
        Calendar calendar = Calendar.getInstance();
        int minutes = (int)((double)calendar.getTimeInMillis() / ( 1000 * 60 ));
        String str;
        // if the array list is empty, or the battery level has never shown
        if(batteryPair.size() == 0 || level != batteryPair.get(batteryPair.size()-1)[1]){
            Integer[] newData = new Integer[]{level, minutes};
            batteryPair.add(newData);
        }
        // if this is the first monitoring point
        if(batteryPair.size() < 2){
            str = getString(R.string.not_calculated);
        }
        // because the weka is not precise when there are only 2 points, so manually calculate the function
        else if(batteryPair.size() == 2){
            int result = (int)doEstimation(minutes);
            if(result > 0){
                // convert to hh:mm format
                int newHour = result / 60;
                int newMinute = result % 60;
                str = newHour + getString(R.string.hour) +
                        newMinute + getString(R.string.minute);
            }
            else{
                str = getString(R.string.not_calculated);
            }
        }
        // else the model has been built
        else{
            int result = (int)doLinearRegression(minutes);
            if(result > 0){
                // convert to hh:mm format
                int newHour = result / 60;
                int newMinute = result % 60;
                str = newHour + getString(R.string.hour) +
                        newMinute + getString(R.string.minute);
            }
            else{
                str = getString(R.string.not_calculated);
            }
        }
        return str;
    }

    /**
     * doEstimation
     */
    private double doEstimation(int currentMinutes){
        // x1: level 1; y1: time 1; x2: level 2; y2: time 2
        int x1 = batteryPair.get(0)[0], y1 = batteryPair.get(0)[1];
        int x2 = batteryPair.get(1)[0], y2 = batteryPair.get(1)[1];
        double k = ((double)y1 - y2) / (x1 - x2);
        double b = (double)y1 - k * x1;
        return b - currentMinutes;
    }

    /**
     * doLinearRegression
     */
    private double doLinearRegression(int currentMinutes){
        // attribute used for weka data format
        ArrayList<Attribute> atts = new ArrayList<Attribute>();
        // list for generate weka data
        List<Instance> instances = new ArrayList<Instance>();
        // dimension = 2: level and time (currentTimeInMinutes)
        int numDimensions = 2;
        // num of instances
        int numInstances = batteryPair.size() + 1;
        // the result
        double minutes = currentMinutes;

        // iterate the dimension
        for( int dim = 0; dim < numDimensions; dim++){
            // set name and num of attribute
            Attribute current = new Attribute("Attribute" + dim, dim);
            if(dim == 0) {
                // initialize the rows
                for (int obj = 0; obj < numInstances; obj++) {
                    instances.add(new SparseInstance(numDimensions));
                }
                instances.get(numInstances - 1).setValue(current, 0.1);
            }
            // initial the value of each known row in the same dimension (column)
            for(int obj = 0; obj < numInstances - 1; obj++) {
                instances.get(obj).setValue(current, batteryPair.get(obj)[dim]);
            }
            // add current instance to attrivute list
            atts.add(current);
        }
        // instances are used for building model of linear regression
        Instances newDataset = new Instances("Dataset", atts, instances.size());
        for(Instance inst : instances){
            newDataset.add(inst);
        }
        // set the task class as the second attribute
        newDataset.setClassIndex(newDataset.numAttributes() - 1);

        LinearRegression regression = new LinearRegression();
        try {
            // initialize regression and build the model
            regression.buildClassifier(newDataset);
            System.out.println(regression);
            // get the last line as test set
            Instance instance = newDataset.lastInstance();
            // get the result
            minutes = regression.classifyInstance(instance);
        } catch (Exception e){
            e.printStackTrace();
        }
        // calculated minutes minus current time
        minutes = minutes - currentMinutes;
        return minutes;
    }

    /**
     * onDestroy: unregister the receiver
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
