package com.example.administrator.powermanagement.Admins;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Created by Administrator on 15/3/10.
 */
public class GPSAdmin {
    private Context context;
    private LocationManager manager;
    public GPSAdmin(Context context){
        this.context = context;
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    public boolean isGPSOn(){
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }
    public void toggleGPS(){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }
}
