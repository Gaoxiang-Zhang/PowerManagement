package com.example.administrator.powermanagement;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Administrator on 15/1/28.
 */
public class WifiAdmin {
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    public WifiAdmin(Context context){
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }
    public void openWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }
    public void closeWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }
    }
    public int checkWifi(){
        int result=wifiManager.getWifiState();
        if(result == WifiManager.WIFI_STATE_DISABLED){
            return 0;
        }
        else if(result == WifiManager.WIFI_STATE_ENABLED ){
            return 1;
        }
        return -1;
    }
}
