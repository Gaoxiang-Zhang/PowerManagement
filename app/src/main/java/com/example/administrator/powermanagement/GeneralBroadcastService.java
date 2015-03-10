package com.example.administrator.powermanagement;

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
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 15/3/5.
 */
public class GeneralBroadcastService extends Service {
    final String GPS_CHANGE_ACTION = "android.location.PROVIDERS_CHANGED";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String tag="Debug Info";
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent broadcast_intent = new Intent();
            //Initial value is 0
            int wifiState,bluetoothState;
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
                    Log.e(tag, "数据连接");
                    broadcast_intent.putExtra("data_state", 1);
                }else{
                    Log.e(tag, "无数据连接");
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

            //send broadcast
            broadcast_intent.setAction("com.example.administrator.powermanagement.gridservice");
            sendBroadcast(broadcast_intent);
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mFilter.addAction(GPS_CHANGE_ACTION);
        registerReceiver(mReceiver, mFilter);
    }
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
