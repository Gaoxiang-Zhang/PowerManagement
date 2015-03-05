package com.example.administrator.powermanagement;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Administrator on 15/3/5.
 */
public class GridService extends Service {
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String tag="Debug Info";
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d(tag, "Wifi未连接");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.d(tag, "Wifi正在断开");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(tag, "Wifi已连接");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.d(tag, "Wifi正在连接");
                        break;
                }
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())){
                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (bluetoothState){
                    case BluetoothAdapter.STATE_ON:
                        Log.d(tag,"蓝牙打开");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(tag,"蓝牙关闭");
                        break;
                }

            }
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
