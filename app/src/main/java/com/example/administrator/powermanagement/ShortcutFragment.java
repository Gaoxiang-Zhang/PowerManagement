package com.example.administrator.powermanagement;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.administrator.powermanagement.Admins.BluetoothAdmin;
import com.example.administrator.powermanagement.Admins.GPSAdmin;
import com.example.administrator.powermanagement.Admins.NetworkAdmin;
import com.example.administrator.powermanagement.Shortcut.ShortcutApplication;
import com.example.administrator.powermanagement.Shortcut.BrightnessDialog;
import com.example.administrator.powermanagement.Shortcut.EffectDialog;
import com.example.administrator.powermanagement.Shortcut.ShortcutSensor;
import com.example.administrator.powermanagement.Shortcut.ShortcutService;
import com.example.administrator.powermanagement.Shortcut.ShortcutSystem;
import com.example.administrator.powermanagement.Shortcut.ShortcutAdapter;
import com.example.administrator.powermanagement.Shortcut.ScreenDialog;
import com.example.administrator.powermanagement.Shortcut.VolumeDialog;

import java.util.ArrayList;

/**
 * ShortcutFragment: the first fragment shows the information of system related to power management
 */
public class ShortcutFragment extends Fragment {

    // Layout variable
    ListView listView = null;
    Context context = null;
    Integer item_num;
    ShortcutAdapter shortcutAdapter;

    // Definitions for parameters to Adapter, Array means parsing Object
    String[] item_titles;
    String[] item_headers;
    ArrayList<String> item_results;
    ArrayList<Boolean> item_status;
    //int[] item_icons;
    final int[] item_icons={
            R.drawable.wifi,
            R.drawable.gprs,
            R.drawable.bluetooth,
            R.drawable.airplane,
            R.drawable.hotspot,
            R.drawable.gps,
            R.drawable.flow,
            R.drawable.brightness,
            R.drawable.volume,
            R.drawable.sleep,
            R.drawable.interaction,
            R.drawable.applications,
            R.drawable.sync,
            R.drawable.system,
            R.drawable.sensors
    };

    // Function Admin
    NetworkAdmin networkAdmin = null;
    GPSAdmin gpsAdmin = null;
    BluetoothAdmin bluetoothAdmin = null;

    // Filters for broadcast receiver
    IntentFilter intentFilter = null;
    BroadcastReceiver mReceiver;

    // 0 = Wifi, 1 = GPRS, 2 = Bluetooth, 3 = Airplane, 4 = Hotspot, 5 = GPS, 6 = Network Flow,
    // 7 = Brightness, 8 = Volume, 9 = Sleep Time, 10 = Interaction Time11 = Latest App Usage,
    // 12 = Sync, 13 = system info 14 = sensor list
    final static int WIFI_NUM=0, GPRS_NUM=1, TOOTH_NUM=2, PLANE_NUM=3,  HOTSPOT_NUM=4, GPS_NUM=5, FLOW_NUM=6,
            LIGHT_NUM=7, VOLUME_NUM=8, SLEEP_NUM=9, ACTION_NUM=10, APP_NUM=11, SYNC_NUM =12, CPU_NUM=13, SENSOR_NUM=14;
    // SWITCH_NUM: The starting items who have SwitchCompat
    final static int SWITCH_NUM = 6;

    int old_wifi = 2, old_tooth = 2, old_data = 2, old_plane = 2, old_gps = 2, old_hot = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View consumer = inflater.inflate(R.layout.fragment_shortcut,container,false);

        context = getActivity();
        listView = (ListView)consumer.findViewById(R.id.shortcut_list);

        // initial admins
        gpsAdmin = new GPSAdmin(context);
        networkAdmin = new NetworkAdmin(context);
        bluetoothAdmin = new BluetoothAdmin();

        // set values for adapter parameters
        item_titles = getResources().getStringArray(R.array.shortcuts);
        item_num = item_titles.length;
        item_status = new ArrayList<>();
        item_headers = new String[item_num];
        item_results = new ArrayList<>();
        initialList(getActivity().getIntent());

        // Set Adapter and OnClickListener for ListView
        shortcutAdapter = new ShortcutAdapter(context,item_titles,item_icons,item_headers,
                item_results,item_status,networkAdmin,bluetoothAdmin);
        listView.setAdapter(shortcutAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position){
                    case WIFI_NUM:
                        Intent wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(wifi);
                        break;
                    case GPRS_NUM:
                        Intent gprs = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                        startActivity(gprs);
                        break;
                    case TOOTH_NUM:
                        Intent bluetooth = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(bluetooth);
                        break;
                    case PLANE_NUM:
                        Intent plane = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                        startActivity(plane);
                        break;
                    case HOTSPOT_NUM:
                        Intent spot = new Intent();
                        spot.setClassName("com.android.settings","com.android.settings.TetherSettings");
                        startActivity(spot);
                        break;
                    case GPS_NUM:
                        gpsAdmin.toggleGPS();
                        break;
                    case FLOW_NUM:
                        Intent flow = new Intent();
                        flow.setComponent(new ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings$DataUsageSummaryActivity"
                        ));
                        startActivity(flow);
                        break;
                    case LIGHT_NUM:
                        BrightnessDialog brightnessDialog = new BrightnessDialog();
                        brightnessDialog.show(getActivity().getFragmentManager(), "tag");
                        break;
                    case VOLUME_NUM:
                        VolumeDialog volumeDialog = new VolumeDialog();
                        volumeDialog.setCancelable(false);
                        volumeDialog.show(getActivity().getFragmentManager(), "tag");
                        break;
                    case SLEEP_NUM:
                        ScreenDialog screenDialog = new ScreenDialog(getActivity());
                        screenDialog.execute();
                        break;
                    case ACTION_NUM:
                        EffectDialog effectDialog = new EffectDialog();
                        effectDialog.setCancelable(false);
                        effectDialog.show(getActivity().getFragmentManager(),"tag");
                        break;
                    case APP_NUM:
                        Intent app = new Intent(getActivity(),ShortcutApplication.class);
                        startActivity(app);
                        break;
                    case SYNC_NUM:
                        Intent usage = new Intent(Settings.ACTION_SYNC_SETTINGS);
                        startActivity(usage);
                        break;
                    case CPU_NUM:
                        Intent cpu = new Intent(getActivity(), ShortcutSystem.class);
                        startActivity(cpu);
                        break;
                    case SENSOR_NUM:
                        Intent sensor = new Intent(getActivity(), ShortcutSensor.class);
                        startActivity(sensor);
                        break;
                }
            }
        });

        // Start GridService
        Intent i = new Intent(this.getActivity(),ShortcutService.class);
        getActivity().startService(i);

        // Set and register broadcast receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.powermanagement.shortcutservice");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                int wifi_result,tooth_result,data_result,plane_result,gps_result,hotspot_result;
                wifi_result = intent.getIntExtra("wifi_state",0);
                data_result = intent.getIntExtra("data_state",0);
                plane_result = intent.getIntExtra("plane_state",0);
                gps_result = intent.getIntExtra("gps_state",0);
                tooth_result = intent.getIntExtra("tooth_state",0);
                hotspot_result = intent.getIntExtra("hotspot_state",0);
                editListItem edit= new editListItem();
                //wifi,tooth,mobile data
                Log.d("debug info", "receive signal "+wifi_result);
                edit.execute(wifi_result,data_result,tooth_result,plane_result,hotspot_result,gps_result);

            }
        };
        getActivity().registerReceiver(mReceiver,intentFilter);

        return consumer;
    }

    /**
     * Initial values in ListView
     * @param intent
     */
    private void initialList(Intent intent){
        for(int i = 0; i < item_num; i++){
            item_status.add(false);
            item_headers[i] = "";
            item_results.add("");
        }
        item_headers[WIFI_NUM] = getResources().getString(R.string.network);
        item_headers[LIGHT_NUM] = getResources().getString(R.string.usage);
        if(networkAdmin.isWifiConnected()){
            item_status.set(WIFI_NUM , true);
        }
        if(networkAdmin.isMobileAvailable()){
            item_status.set(GPRS_NUM , true);
        }
        if(networkAdmin.isAirplaneModeOn()){
            item_status.set(PLANE_NUM , true);
        }
        if(gpsAdmin.isGPSOn()){
            item_status.set(GPS_NUM , true);
        }
        if(bluetoothAdmin.checkBluetooth()==1){
            item_status.set(TOOTH_NUM , true);
        }
        if(networkAdmin.isHotspotConnected(intent)){
            item_status.set(HOTSPOT_NUM , true);
        }
    }

    /**
     * editListItem: async task for changing UI
     */
    public class editListItem extends AsyncTask<Integer,Integer,Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int i;
            Boolean change = false;
            for(i=0;i<SWITCH_NUM;i++){
                if(params[i]==1){
                    if(!item_status.get(i)){
                        item_status.set(i,true);
                        change = true;
                    }
                }else if(params[i]==-1){
                    if(item_status.get(i)){
                        item_status.set(i,false);
                        change = true;
                    }
                }
            }
            return change;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                shortcutAdapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * onDestroy: unregister broadcast receiver
     */
    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }



}
