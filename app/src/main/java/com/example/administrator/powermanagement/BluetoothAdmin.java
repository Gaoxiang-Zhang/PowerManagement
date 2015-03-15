package com.example.administrator.powermanagement;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * Created by Administrator on 15/1/28.
 */
public class BluetoothAdmin {
    // Bluetooth Adapter manages the bluetooth
    private BluetoothAdapter bluetoothAdapter;

    /**
     * BluetoothAdmin: initialize the bluetoothAdapter
     */
    public BluetoothAdmin(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * checkBluetooth: check the state of bluetooth
     * @ return: on->1, off->0, others->-1
     */
    public int checkBluetooth(){
        int result = bluetoothAdapter.getState();
        if(result == BluetoothAdapter.STATE_ON)
            return 1;
        else if (result == BluetoothAdapter.STATE_OFF)
            return 0;
        return -1;
    }

    /**
     * toggleBluetooth: switch the status of bluetooth (External port)
     * toggleToothTask: switch the status of bluetooth in another thread (Main implements)
     */
    public void toggleBluetooth(){
        toggleToothTask task = new toggleToothTask();
        task.execute();
    }
    public class toggleToothTask extends AsyncTask<Void,Void,Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean enable = ( checkBluetooth() == 1 );
            return enable;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                bluetoothAdapter.enable();
            }
            else{
                bluetoothAdapter.disable();
            }
        }
    }
}
