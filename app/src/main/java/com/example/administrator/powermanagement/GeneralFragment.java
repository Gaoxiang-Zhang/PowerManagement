package com.example.administrator.powermanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
    String[] imageText;
    // const variable definition
    final int WIFI_NUM=0,GPRS_NUM=1,PLANE_NUM=2,GPS_NUM=4,TOOTH_NUM=5,SOUND_NUM=6;
    // Module Manager
    NetworkAdmin networkAdmin;
    BluetoothAdmin bluetoothAdmin;
    GPSAdmin gpsAdmin;
    // Adapter (ImageAdapter) that control the UI.
    ImageAdapter imageAdapter;
    // Broadcast receiver to get broadcast from GridService
    BroadcastReceiver mReceiver;
    IntentFilter intentFilter;
    /**
     * onCreateView: Preparation
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View general = inflater.inflate(R.layout.general_fragment,container,false);

        // Initialize Admin
        networkAdmin = new NetworkAdmin(this.getActivity());
        bluetoothAdmin = new BluetoothAdmin();
        gpsAdmin = new GPSAdmin(this.getActivity());

        // Set initial values for gridImages
        gridImages = new ArrayList<>();
        setGridImages();

        // Set every name of the grid
        imageText=getResources().getStringArray(R.array.general_items);
        gridView = (GridView)general.findViewById(R.id.general_grid);
        imageAdapter = new ImageAdapter(this.getActivity(),imageText,gridImages);
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                switch(position){
                    case GPS_NUM:
                        gpsAdmin.toggleGPS();
                        break;
                    case SOUND_NUM:
                        Intent i = new Intent(getActivity(),VolumeActivity.class);
                        startActivity(i);
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
                int wifi_result,tooth_result,data_result,plane_result,gps_result;
                wifi_result = intent.getIntExtra("wifi_state",0);
                data_result = intent.getIntExtra("data_state",0);
                plane_result = intent.getIntExtra("plane_state",0);
                gps_result = intent.getIntExtra("gps_state",0);
                tooth_result = intent.getIntExtra("tooth_state",0);
                //Log.d("Debug Info", "得到广播"+wifi_result+tooth_result);
                editItem edit= new editItem();
                //wifi,tooth,mobile data
                edit.execute(wifi_result,data_result,plane_result,-1,gps_result,tooth_result,-1,-1,-1);

            }
        };
        getActivity().registerReceiver(mReceiver,intentFilter);

        return general;
    }
    /**
     *   setGridImages: set initial values for ArrayList gridImages
     */
    private void setGridImages(){
        for(int i = 0; i < 9 ; i++){
            gridImages.add(R.drawable.denied_128px);
        }
        if(networkAdmin.isWifiConnected()){
            gridImages.set(WIFI_NUM,R.drawable.cloud_128px);
        }
        if(networkAdmin.isMobileConnected()){
            gridImages.set(GPRS_NUM,R.drawable.cloud_128px);
        }
        if(networkAdmin.isAirplaneModeOn()){
            gridImages.set(PLANE_NUM,R.drawable.cloud_128px);
        }
        if(gpsAdmin.isGPSOn()){
            gridImages.set(GPS_NUM,R.drawable.cloud_128px);
        }
        if(bluetoothAdmin.checkBluetooth()==1){
            gridImages.set(TOOTH_NUM,R.drawable.cloud_128px);
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
                    gridImages.set(i,R.drawable.cloud_128px);
                }else if(params[i]==-1){
                    gridImages.set(i,R.drawable.denied_128px);
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
     * onDestroy: unregister broadcast receiver
     */
    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
