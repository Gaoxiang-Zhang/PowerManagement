package com.example.administrator.powermanagement.Admins;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;

import java.lang.reflect.InvocationTargetException;
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

    final int HOTSPOT_OPEN = 13;

    /**
     * Judge whether wifi is available
     */
    public boolean isWifiAvailable(){
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mWifi != null){
            return mWifi.isAvailable();
        }
        return false;
    }
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
    public boolean isMobileAvailable(){
        NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(mMobile != null){
            return mMobile.isAvailable();
        }
        return false;
    }
    public boolean isMobileConnected(){
        NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(mMobile != null){
            return mMobile.isConnected();
        }
        return false;
    }
    /**
     * Judge whether wifi ap is available
     */
    public boolean isHotspotConnected(Intent intent){
        int state = intent.getIntExtra("wifi_state",0);
        return ( state == HOTSPOT_OPEN );
    }
    /**
     * GPRS network switch
     */
    public void toggleGPRS(Boolean value){
        toggleGPRSTask task = new toggleGPRSTask();
        task.execute(value);
    }
    public class toggleGPRSTask extends AsyncTask<Boolean,Void,Boolean>
    {
        @Override
        protected Boolean doInBackground(Boolean... params) {
            return params[0];
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Class<?> cmClass = connectivityManager.getClass();
            Class<?>[] argClasses = new Class[1];
            argClasses[0] = boolean.class;
            Method method = null;
            try{
                method = cmClass.getMethod("setMobileDataEnabled",argClasses);
            } catch (NoSuchMethodException e){
                e.printStackTrace();
            }
            if(method != null){
                try{
                    method.invoke(connectivityManager,result);
                } catch (IllegalAccessException | InvocationTargetException e){
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * WiFi network switch
     */
    public void toggleWiFi(boolean value){
        toggleWiFiTask task = new toggleWiFiTask();
        task.execute(value);
        //WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        //wm.setWifiEnabled(!isWifiAvailable());
    }
    public class toggleWiFiTask extends AsyncTask<Boolean,Void,Boolean>
    {
        WifiManager wm;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            return params[0];
        }
        @Override
        protected void onPostExecute(Boolean result) {
            wm.setWifiEnabled(result);
        }
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
     * Airplane switch: in Android > 4.2, this function needs rooted system, so it needs further discussion
     */
    public void toggleAirplaneMode(){
        //togglePlaneTask task = new togglePlaneTask();
        //task.execute();
    }
    public class togglePlaneTask extends AsyncTask<Void,Void,Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean enabled = isAirplaneModeOn();
            return !enabled;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            //Settings.System.putInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON,result?1:0);
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,result?1:0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state",result);
            context.sendBroadcast(intent);
        }
    }
}
