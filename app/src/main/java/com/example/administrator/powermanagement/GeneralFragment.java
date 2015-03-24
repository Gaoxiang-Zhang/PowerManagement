package com.example.administrator.powermanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class GeneralFragment extends Fragment {

    /**
     *  variable declaration
     */
    // main gridView
    GridView gridView;
    // the image and text shown in general fragment
    // Due to the features in Adapter (deliver real argument), use ArrayList instead of Integer[]
    ArrayList<Integer>  gridImages;
    final Integer[] gridImagesOn={
            R.drawable.wifi,
            R.drawable.gprs,
            R.drawable.bluetooth,
            R.drawable.airplane,
            R.drawable.hotspot,
            R.drawable.gps,
            R.drawable.volume,
            R.drawable.action,
            R.drawable.clock
    };
    final Integer[] gridImagesOff={
            R.drawable.wifi_off,
            R.drawable.gprs_off,
            R.drawable.bluetooth_off,
            R.drawable.airplane_off,
            R.drawable.hotspot_off,
            R.drawable.gps_off,
            R.drawable.volume,
            R.drawable.action,
            R.drawable.clock
    };
    String[] imageText;
    // const variable definition
    final int WIFI_NUM=0,GPRS_NUM=1,TOOTH_NUM=2,PLANE_NUM=3,SPOT_NUM=4,GPS_NUM=5,SOUND_NUM=6,VIB_NUM=7,SCR_NUM=8,BRI_NUM=9;
    // Module Manager
    NetworkAdmin networkAdmin;
    BluetoothAdmin bluetoothAdmin;
    GPSAdmin gpsAdmin;
    // Adapter (ImageAdapter) that control the UI.
    ImageAdapter imageAdapter;
    // Broadcast receiver to get broadcast from GridService
    BroadcastReceiver mReceiver;
    IntentFilter intentFilter;
    // brightness seekBar, brightness content, ContentObserver
    SeekBar seekBar;
    final String BRIGHTNESS_STRING = android.provider.Settings.System.SCREEN_BRIGHTNESS;
    BrightnessObserver brightnessObserver = null;

    /**
     * onCreateView: Preparation
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View general = inflater.inflate(R.layout.fragment_general,container,false);

        // Initialize Admin
        networkAdmin = new NetworkAdmin(this.getActivity());
        bluetoothAdmin = new BluetoothAdmin();
        gpsAdmin = new GPSAdmin(this.getActivity());

        // Set initial values for gridImages
        imageText=getResources().getStringArray(R.array.general_items);
        gridImages = new ArrayList<>();
        setGridImages(getActivity().getIntent());

        // Set every content of the grid
        gridView = (GridView)general.findViewById(R.id.general_grid);
        imageAdapter = new ImageAdapter(this.getActivity(),imageText,gridImages);
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                switch(position){
                    case WIFI_NUM:
                        networkAdmin.toggleWiFi();
                        break;
                    case GPRS_NUM:
                        networkAdmin.toggleGPRS();
                        break;
                    case SPOT_NUM:
                        Intent spot = new Intent();
                        spot.setClassName("com.android.settings","com.android.settings.TetherSettings");
                        startActivity(spot);
                        break;
                    case PLANE_NUM:
                        Intent plane = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                        startActivity(plane);
                        break;
                    case GPS_NUM:
                        gpsAdmin.toggleGPS();
                        break;
                    case TOOTH_NUM:
                        bluetoothAdmin.toggleBluetooth();
                        break;
                    case SOUND_NUM:
                        Intent sound = new Intent(getActivity(),VolumeActivity.class);
                        startActivity(sound);
                        break;
                    case VIB_NUM:
                        Intent vibration = new Intent(getActivity(),InteractionActivity.class);
                        startActivity(vibration);
                        break;
                    case SCR_NUM:
                        Intent screen = new Intent(getActivity(),ScreenOffActivity.class);
                        startActivity(screen);
                        break;

                }
            }
        });

        // Start GridService
        Intent i = new Intent(this.getActivity(),GeneralBroadcastService.class);
        getActivity().startService(i);

        // Set and register broadcast receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.powermanagement.gridservice");
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
                //Log.d("Debug Info", "得到广播"+wifi_result+tooth_result);
                editItem edit= new editItem();
                //wifi,tooth,mobile data
                edit.execute(wifi_result,data_result,tooth_result,plane_result,hotspot_result,gps_result,-1,-1,-1,-1);

            }
        };
        getActivity().registerReceiver(mReceiver,intentFilter);

        // Set seekBar
        seekBar = (SeekBar)general.findViewById(R.id.light_bar);
        initBar(seekBar);
        // Set Brightness ContentObserver
        final Uri BRIGHTNESS_URL = Settings.System.getUriFor(BRIGHTNESS_STRING);
        brightnessObserver = new BrightnessObserver(new Handler());
        getActivity().getApplicationContext().getContentResolver()
                .registerContentObserver(BRIGHTNESS_URL,true,brightnessObserver);
        return general;
    }
    /**
     *   setGridImages: set initial values for ArrayList gridImages
     */
    private void setGridImages(Intent intent){
        for(int i = 0; i < imageText.length ; i++){
            gridImages.add(gridImagesOff[i]);
        }
        if(networkAdmin.isWifiConnected()){
            gridImages.set(WIFI_NUM,gridImagesOn[WIFI_NUM]);
        }
        if(networkAdmin.isMobileConnected()){
            gridImages.set(GPRS_NUM,gridImagesOn[GPRS_NUM]);
        }
        if(networkAdmin.isAirplaneModeOn()){
            gridImages.set(PLANE_NUM,gridImagesOn[PLANE_NUM]);
        }
        if(gpsAdmin.isGPSOn()){
            gridImages.set(GPS_NUM,gridImagesOn[GPS_NUM]);
        }
        if(bluetoothAdmin.checkBluetooth()==1){
            gridImages.set(TOOTH_NUM,gridImagesOn[TOOTH_NUM]);
        }
        if(networkAdmin.isHotspotConnected(intent)){
            gridImages.set(SPOT_NUM,gridImagesOn[SPOT_NUM]);
        }
    }
    /**
     *   ImageAdapter: Adapter controls UI of gridView
     */
    public class ImageAdapter extends BaseAdapter
    {
        private Context context;
        private final String[] imageText;
        private final ArrayList<Integer> imageContent;
        public ImageAdapter(Context c,String[] itemtext,ArrayList<Integer> imagecontent){
            context=c;
            this.imageContent = imagecontent;
            this.imageText = itemtext;
        }
        public int getCount(){
            return imageText.length;
        }
        public Object getItem(int position){
            return position;
        }
        public long getItemId(int position){
            return position;
        }
        // called in the beginning and notifyDataSetChanged
        public View getView(int position, View convertView,ViewGroup parent){
            View grid;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Log.d("Debug Info", position+"getView"+gridImages.get(position));
            if(convertView == null){
                grid = inflater.inflate(R.layout.grid_single,null);
                TextView textView = (TextView)grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
                imageView.setImageResource(imageContent.get(position));
                textView.setText(imageText[position]);
            }else{
                TextView textView = (TextView)convertView.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)convertView.findViewById(R.id.grid_image);
                imageView.setImageResource(imageContent.get(position));
                textView.setText(imageText[position]);
                grid = convertView;
            }
            return grid;
        }
        // areAllItemsEnabled: set that not all grids are able to be clicked
        @Override
        public boolean areAllItemsEnabled(){
            return false;
        }
        // isEnabled: set whether a grid can be clicked by its position
        @Override
        public boolean isEnabled(int position){
            switch (position){
                //case PLANE_NUM:
                case BRI_NUM:
                    return false;
                default:
                    return true;
            }
        }
    }

    /**
     *   editItem: Async task that handles gridImages change and notify imageAdapter
     */
    public class editItem extends AsyncTask<Integer,Integer,String>{
        @Override
        protected String doInBackground(Integer... params) {
            int i;
            for(i=0;i<gridImages.size();i++){
                if(params[i]==1){
                    gridImages.set(i,gridImagesOn[i]);
                }else if(params[i]==-1){
                    gridImages.set(i,gridImagesOff[i]);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            imageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * getBrightnessValue: read and return brightness value (10~255)
     */
    private float getBrightnessValue(){
        float curBrightnessValue = 0;
        try{
            curBrightnessValue = android.provider.Settings.System.getInt(
                    getActivity().getContentResolver(),
                    BRIGHTNESS_STRING);
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return curBrightnessValue;
    }
    /**
     * initialBar: initial the bar value and attach it to system brightness
     */
    public void initBar(SeekBar bar){
        final int minBrightness = 10;
        final int maxBrightness = 255;
        bar.setMax(maxBrightness - minBrightness);
        int screenBrightness = (int) getBrightnessValue();
        bar.setProgress(screenBrightness);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i + minBrightness;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                android.provider.Settings.System.putInt(getActivity().getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);
                WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
                layoutParams.screenBrightness = progress / (float)255;
                getActivity().getWindow().setAttributes(layoutParams);
            }
        });
    }
    /**
     * BrightnessObserver: get change of brightness in time and change the seekBar progress
     */
    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            seekBar.setProgress((int) getBrightnessValue());
        }
    }

    /**
     * onDestroy: unregister broadcast receiver
     */
    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        getActivity().getContentResolver().unregisterContentObserver(brightnessObserver);
        super.onDestroy();
    }
}
