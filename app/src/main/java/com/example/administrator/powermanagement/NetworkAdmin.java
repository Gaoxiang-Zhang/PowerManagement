package com.example.administrator.powermanagement;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 15/3/9.
 * NetworkAdmin is the class that control the state of WiFi, GPRS and airplane mode
 */
public class NetworkAdmin {
    private Context context;
    private ConnectivityManager connectivityManager;
    public NetworkAdmin(Context context){
        this.context = context;
        connectivityManager = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    /**
     * Judge whether the network is connected
     */
    public boolean isNetworkConnected(){
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null){
            return networkInfo.isConnected();
        }
        return false;
    }
    /**
     * Judge whether wifi is available
     */
    public boolean isWifiConnected(){
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mWifi != null){
            return mWifi.isConnected();
        }
        return false;
    }
    /**
     * Judge whether mobile data is available
     */
    public boolean isMobileConnected(){
        NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(mMobile != null){
            return mMobile.isConnected();
        }
        return false;
    }
    /**
     * GPRS network switch
     */
    public void toggleGprs(boolean isEnable) throws Exception{
        Class<?> cmClass = connectivityManager.getClass();
        Class<?>[] argClasses = new Class[1];
        argClasses[0] = boolean.class;
        Method method = cmClass.getMethod("setMobileDataEnabled",argClasses);
        method.invoke(connectivityManager,isEnable);
    }
    /**
     * WiFi network switch
     */
    public boolean toggleWiFi(boolean enabled){
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wm.setWifiEnabled(enabled);
    }
    /**
     * Judge whether the phone is in airplane mode
     */
    public boolean isAirplaneModeOn(){
        int modeIdx = Settings.System.getInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON,0);
        boolean isEnabled = (modeIdx == 1);
        return isEnabled;
    }
    /**
     * Airplane switch
     */
    public void toggleAirplaneMode(boolean setAirPlane){
        Settings.System.putInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON,setAirPlane?1:0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state",setAirPlane);
        context.sendBroadcast(intent);
    }
}
