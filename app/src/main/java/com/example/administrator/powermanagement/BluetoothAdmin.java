package com.example.administrator.powermanagement;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 15/1/28.
 */
public class BluetoothAdmin {
    private BluetoothAdapter bluetoothAdapter;
    public BluetoothAdmin(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    }
    public Intent openBluetooth(){
        if(!bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            return intent;
        }
        return null;
    }
    public void closeBluetooth(){
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
        }
    }
    public int checkBluetooth(){
        int result = bluetoothAdapter.getState();
        if(result == BluetoothAdapter.STATE_ON)
            return 1;
        else if (result == BluetoothAdapter.STATE_OFF)
            return 0;
        return -1;
    }
}
