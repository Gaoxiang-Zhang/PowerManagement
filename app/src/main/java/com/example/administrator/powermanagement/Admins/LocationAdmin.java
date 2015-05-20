package com.example.administrator.powermanagement.Admins;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by Administrator on 15/4/8.
 */
public class LocationAdmin {

    // Context
    private Context context = null;
    // Location Admin supported by baidu
    private LocationClient locationClient = null;
    // Broadcast to send location changed
    Intent broadcastIntent = null;

    /**
     * LocationAdmin: start location service
     */
    public LocationAdmin(final Context ctx) {
        this.context = ctx;
        broadcastIntent = new Intent();
        broadcastIntent.setAction("com.example.administrator.powermanagement.location");
        locationClient = new LocationClient(ctx);
        initLocationClient();
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null) {
                    return;
                }
                broadcastIntent.putExtra("latitude", bdLocation.getLatitude());
                broadcastIntent.putExtra("longitude", bdLocation.getLongitude());
                context.sendBroadcast(broadcastIntent);
                stopLocationService();
            }
        });
        startLocationService();
    }

    /**
     * initLocationClient: set parameters for location client option
     */
    private void initLocationClient(){
        LocationClientOption option = new LocationClientOption();
        // allowing open GPS
        option.setOpenGps(true);
        // set coordinate type
        option.setCoorType("bd09ll");
        // set location priority
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // set request interval
        option.setScanSpan(20000);
        // set needing address
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }

    public void startLocationService(){
        locationClient.start();
        locationClient.requestLocation();
    }

    public void stopLocationService(){
        locationClient.stop();
    }

}
