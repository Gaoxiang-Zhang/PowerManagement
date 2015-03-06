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
