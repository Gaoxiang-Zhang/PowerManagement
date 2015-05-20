package com.example.administrator.powermanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.powermanagement.Admins.DBAdapter;
import com.example.administrator.powermanagement.Config.DebugAdmin;
import com.example.administrator.powermanagement.Custom.CustomActivity;
import com.example.administrator.powermanagement.Custom.CustomService;
import com.example.administrator.powermanagement.Config.ConfigService;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Calendar;

public class OptionsFragment extends Fragment {

    Context context = null;

    // ArcProgress: supported by lzyzsd, controls the profit meter of this page
    ArcProgress arcProgress = null;

    // Floating button/menu: supported by clans, controls the quick toggle button
    FloatingActionMenu menu = null;
    FloatingActionButton button_auto = null;
    FloatingActionButton button_manual = null;
    FloatingActionButton button_off = null;

    // saving state = 0: auto 1: manually 2: disabled
    int saving_state = 0;

    // savingBefore/All: the energy saving yesterday / in sum
    long savingBefore = 0;
    long savingAll = 0;

    //showing battery level and remaining time and the control receiver get broadcast from General
    TextView batteryLevel = null;
    TextView remainTime = null;
    BroadcastReceiver batteryReceiver = null;

    // database stores the monitoring info and custom info
    DBAdapter dbAdapter = null;

    // monitor service
    Intent monitorService = null;
    Intent customService = null;

    // isAutoService on = true means that monitor service is working
    boolean isAutoServiceOn = false;
    boolean isCustomServiceOn = false;

    SharedPreferences sharedMode = null;
    static final String KEY_MODE = "mode";

    DebugAdmin debugAdmin = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        final View options = inflater.inflate(R.layout.fragment_main,container,false);

        context = getActivity();

        // get and set the initial value of arc progress
        arcProgress = (ArcProgress)options.findViewById(R.id.main_progress);
        arcProgress.setProgress(10);
        arcProgress.setBottomText(getString(R.string.profitYesterday));

        // set text in this page
        TextView text = (TextView)options.findViewById(R.id.menu_settings).findViewById(R.id.menu_title);
        text.setText(getString(R.string.detailOption));
        text = (TextView)options.findViewById(R.id.menu_custom).findViewById(R.id.menu_title);
        text.setText(getString(R.string.customMode));
        text = (TextView)options.findViewById(R.id.menu_ranking).findViewById(R.id.menu_title);
        text.setText(getString(R.string.debug));
        text = (TextView)options.findViewById(R.id.menu_battery).findViewById(R.id.menutabs_item);
        text.setText(getString(R.string.battery));
        text = (TextView)options.findViewById(R.id.menu_profit).findViewById(R.id.menutabs_item);
        text.setText(getString(R.string.profitTotal));
        text = (TextView)options.findViewById(R.id.menu_life).findViewById(R.id.menutabs_item);
        text.setText(getString(R.string.usingTime));
        batteryLevel = (TextView)options.findViewById(R.id.menu_battery).findViewById(R.id.menutabs_value);
        remainTime = (TextView)options.findViewById(R.id.menu_life).findViewById(R.id.menutabs_value);


        ImageView image = (ImageView)options.findViewById(R.id.menu_settings).findViewById(R.id.menu_icon);
        image.setImageDrawable(getResources().getDrawable(R.drawable.settings));
        image = (ImageView)options.findViewById(R.id.menu_custom).findViewById(R.id.menu_icon);
        image.setImageDrawable(getResources().getDrawable(R.drawable.custom));
        image = (ImageView)options.findViewById(R.id.menu_ranking).findViewById(R.id.menu_icon);
        image.setImageDrawable(getResources().getDrawable(R.drawable.ranking));

        // get the floating button menu and buttons
        menu = (FloatingActionMenu)options.findViewById(R.id.menu_button);
        setMenuColor(saving_state);
        button_auto = (FloatingActionButton)options.findViewById(R.id.menu_auto);
        button_manual = (FloatingActionButton)options.findViewById(R.id.menu_manu);
        button_off = (FloatingActionButton)options.findViewById(R.id.menu_disabled);

        // set click listener to floating buttons
        button_auto.setOnClickListener(clickListener);
        button_manual.setOnClickListener(clickListener);
        button_off.setOnClickListener(clickListener);

        // initialize battery broadcast receiver
        initBatteryReceiver();

        // initialize database
        dbAdapter = DBAdapter.getInstance(getActivity());

        // get monitor service, if the service is not working and the state is 0, start it
        monitorService = new Intent(getActivity(),ConfigService.class);
        customService = new Intent(getActivity(), CustomService.class);

        // get shared preference in shared preference
        sharedMode = getActivity().getSharedPreferences(MainActivity.PREF_NAME, 0);
        saving_state = sharedMode.getInt(KEY_MODE, 2);
        setMenuColor(saving_state);


        if(saving_state == 0 && !isAutoServiceOn){
            isAutoServiceOn = true;
            getActivity().startService(monitorService);
        }
        else if(saving_state == 1 && !isCustomServiceOn){
            isCustomServiceOn = true;
            getActivity().startService(customService);
        }

        debugAdmin = new DebugAdmin(context);

        // this function is temporarily used for debug
        LinearLayout ranking = (LinearLayout)options.findViewById(R.id.menu_ranking);
        LinearLayout custom = (LinearLayout)options.findViewById(R.id.menu_custom);
        ranking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugAdmin.outputToFile(0);
                debugAdmin.outputToFile(1);
            }
        });
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CustomActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return options;
    }

    /**
     * setMenuColor: set menu color based on different status
     * @param id: 0 for auto, 1 for manually, 2 for disable
     */
    private void setMenuColor(int id){
        switch (id){
            case 0:
                menu.setMenuButtonColorNormalResId(R.color.mode_auto);
                menu.setMenuButtonColorPressedResId(R.color.mode_auto_pressed);
                break;
            case 1:
                menu.setMenuButtonColorNormalResId(R.color.mode_manu);
                menu.setMenuButtonColorPressedResId(R.color.mode_manu_pressed);
                break;
            case 2:
                menu.setMenuButtonColorNormalResId(R.color.mode_disabled);
                menu.setMenuButtonColorPressedResId(R.color.mode_disabled_pressed);
                break;
        }
    }

    // clickListener: listener for fabs
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menu_auto:
                    saving_state = 0;
                    menu.close(true);
                    setMenuColor(saving_state);
                    if(!isAutoServiceOn){
                        isAutoServiceOn = true;
                        getActivity().startService(monitorService);
                    }
                    if(isCustomServiceOn){
                        isCustomServiceOn = false;
                        getActivity().stopService(customService);
                    }
                    break;
                case R.id.menu_manu:
                    saving_state = 1;
                    menu.close(true);
                    setMenuColor(saving_state);
                    if(isAutoServiceOn){
                        isAutoServiceOn = false;
                        getActivity().stopService(monitorService);
                    }
                    if(!isCustomServiceOn){
                        isCustomServiceOn = true;
                        getActivity().startService(customService);
                    }
                    break;
                case R.id.menu_disabled:
                    saving_state = 2;
                    menu.close(true);
                    setMenuColor(saving_state);
                    if(isAutoServiceOn){
                        isAutoServiceOn = false;
                        getActivity().stopService(monitorService);
                    }
                    if(isCustomServiceOn){
                        isCustomServiceOn = false;
                        getActivity().stopService(customService);
                    }
                    break;
            }
        }
    };

    /**
     * initBatteryReceiver: set battery receiver receive battery status data
     */
    private void initBatteryReceiver(){
        // set battery receiver receive battery status data
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                changeUITask task = new changeUITask();
                task.execute(intent.getStringExtra("battery_level"), intent.getStringExtra("remain_time"));
            }
        };
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction("com.example.administrator.powermanagement.battery");
        getActivity().registerReceiver(batteryReceiver,batteryFilter);
    }

    /**
     * changeUITask:
     */
    private class changeUITask extends AsyncTask<String, Integer, Integer>{

        String level = "", life = "";

        @Override
        protected Integer doInBackground(String... params){
            level = params[0];
            life = params[1];
            return 0;
        }
        @Override
        protected void onPostExecute(Integer result){
            batteryLevel.setText(level);
            remainTime.setText(life);
        }
    }


    /**
     * outputPattern: write the data in database into file
     */
    private void outputPattern(){

        // get the file name
        Calendar calendar = Calendar.getInstance();
        int filename0 = calendar.get(Calendar.DAY_OF_MONTH);
        int filename1 = calendar.get(Calendar.HOUR_OF_DAY);
        int filename2 = calendar.get(Calendar.MINUTE);
        String filename = filename0+"_"+filename1+"_"+filename2+".dat";

        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath()+"/"+getString(R.string.app_name));
            if(!directory.exists()){
                directory.mkdirs();
            }
            File file = new File(directory,filename);
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            try{
                dbAdapter.open();
            }catch (SQLException e){
                e.printStackTrace();
            }
            Cursor c = dbAdapter.getAllPatterns();
            if(c.moveToFirst()){
                do{
                    osw.write(displayPattern(c));
                    osw.flush();
                }while (c.moveToNext());
            }
            dbAdapter.close();
            osw.close();
            Toast.makeText(getActivity(),"Writing to file "+filename+" done",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private String displayPattern(Cursor c){
        String id = c.getString(0);
        String week = c.getString(1);
        String hour = c.getString(2);
        String min = c.getString(3);
        String pos = c.getString(4);
        String app = c.getString(5);
        String used = c.getString(6);
        String battery = c.getString(7);
        String brightness = c.getString(8);
        String timeout = c.getString(9);
        String charge = c.getString(10);
        String wifi = c.getString(11);
        String gprs = c.getString(12);
        String tooth = c.getString(13);
        String gps = c.getString(14);
        String hotspot = c.getString(15);
        String flow = c.getString(16);
        String mobile = c.getString(17);
        String using = c.getString(18);
        String data = id+"\t"+week+"\t"+hour+"\t"+min+"\t"+pos+"\t"+app+"\t"+used+"\t"+
                battery+"\t"+brightness+"\t"+timeout+"\t"+charge+"\t"+wifi+"\t"+gprs+"\t"+
                tooth+"\t"+gps+"\t"+hotspot+"\t"+flow+"\t"+mobile+"\t"+using+"\n";
        return data;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(batteryReceiver);
        // write the shared preference in storage
        SharedPreferences.Editor editor = sharedMode.edit();
        editor.putInt(KEY_MODE,saving_state);
        editor.apply();
    }
}
