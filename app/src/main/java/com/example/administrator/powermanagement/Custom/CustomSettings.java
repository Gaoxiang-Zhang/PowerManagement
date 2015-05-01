package com.example.administrator.powermanagement.Custom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;

/**
 * Created by Administrator on 15/4/25.
 */
public class CustomSettings extends ActionBarActivity implements
        CustomName.OnNameCompleteListener, CustomTime.OnTimeCompleteListener,
        CustomBrightness.OnScreenCompleteListener, CustomSound.OnSoundCompleteListener,
        CustomNetwork.OnNetworkCompleteListener{

    Toolbar mToolbar = null;

    // Parsed Value
    int id;
    int toggle, brightness, sound, wifi, tooth, gprs;
    String name = null;
    String start_time = null;
    String end_time = null;

    // Button
    SwitchCompat item_switch = null;
    LinearLayout item_name = null;
    LinearLayout item_time = null;
    LinearLayout item_screen = null;
    LinearLayout item_sound = null;
    LinearLayout item_network = null;
    TextView item_delete = null;
    TextView item_confirm = null;


    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_settings);

        // Set up toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(R.string.mode_setting);
        setSupportActionBar(mToolbar);

        // get parsed data
        getParseData();

        // initialize interface based on parsed data
        initialInterface();

        // if the data is original, call for special interface
        if(id == -1){
            ordinaryInterface();
        }

    }

    /**
     * getParseData: get parsed data from CustomActivity and unpack to single data
     */
    private void getParseData(){
        String[] value = getIntent().getStringArrayExtra("value");
        id = Integer.parseInt(value[0]);
        if(id < -1){
            finish();
        }
        name = value[1];
        start_time = value[2];
        end_time = value[3];
        brightness = Integer.parseInt(value[4]);
        sound = Integer.parseInt(value[5]);
        wifi = Integer.parseInt(value[6]);
        gprs = Integer.parseInt(value[7]);
        tooth = Integer.parseInt(value[8]);
        toggle = Integer.parseInt(value[9]);
    }

    /**
     * packageData: package single data to String array
     */
    private String[] packageData(){
        String[] result = new String[10];
        result[0] = id+"";
        result[1] = name;
        result[2] = start_time;
        result[3] = end_time;
        result[4] = brightness+"";
        result[5] = sound+"";
        result[6] = wifi+"";
        result[7] = gprs+"";
        result[8] = tooth+"";
        result[9] = toggle+"";
        return result;
    }


    /**
     * initialInterface: initialize the button and set the text
     */
    private void initialInterface(){
        // get each button
        item_switch = (SwitchCompat)findViewById(R.id.custom_settings_switch);
        item_name = (LinearLayout)findViewById(R.id.custom_settings_name);
        item_time = (LinearLayout)findViewById(R.id.custom_settings_time);
        item_screen = (LinearLayout)findViewById(R.id.custom_settings_screen);
        item_sound = (LinearLayout)findViewById(R.id.custom_settings_sound);
        item_network = (LinearLayout)findViewById(R.id.custom_settings_connect);
        item_delete = (TextView)findViewById(R.id.custom_settings_delete);
        item_confirm = (TextView)findViewById(R.id.custom_settings_confirm);

        // set listener for each button
        item_switch.setOnCheckedChangeListener(checkListener);
        item_name.setOnClickListener(clickListener);
        item_time.setOnClickListener(clickListener);
        item_screen.setOnClickListener(clickListener);
        item_sound.setOnClickListener(clickListener);
        item_network.setOnClickListener(clickListener);
        item_delete.setOnClickListener(clickListener);
        item_confirm.setOnClickListener(clickListener);


        // set unchanged titles
        item_switch.setChecked(toggle == 1);
        TextView text = (TextView)findViewById(R.id.custom_settings_name).findViewById(R.id.custom_title);
        text.setText(getString(R.string.mode_name));
        text = (TextView)findViewById(R.id.custom_settings_time).findViewById(R.id.custom_title);
        text.setText(getString(R.string.start_time));
        text = (TextView)findViewById(R.id.custom_settings_screen).findViewById(R.id.custom_title);
        text.setText(getString(R.string.brightness));
        text = (TextView)findViewById(R.id.custom_settings_sound).findViewById(R.id.custom_title);
        text.setText(getString(R.string.sound));
        text = (TextView)findViewById(R.id.custom_settings_connect).findViewById(R.id.custom_title);
        text.setText(getString(R.string.connPref));

        //set variable values
        text = (TextView)findViewById(R.id.custom_settings_name).findViewById(R.id.custom_value);
        text.setText(name);
        text = (TextView)findViewById(R.id.custom_settings_time).findViewById(R.id.custom_value);
        text.setText(start_time+" ~ "+end_time);
        text = (TextView)findViewById(R.id.custom_settings_screen).findViewById(R.id.custom_value);
        text.setText(brightness+"");
        text = (TextView)findViewById(R.id.custom_settings_sound).findViewById(R.id.custom_value);
        switch (sound){
            case 0:
                text.setText(getString(R.string.ordinary));
                break;
            case 1:
                text.setText(getString(R.string.vibrate));
                break;
            case 2:
                text.setText(getString(R.string.silent));
                break;
        }
    }

    /**
     * ordinaryInterface: set interface for ordinary setting
     */
    private void ordinaryInterface(){

        // set switch to gone
        LinearLayout layout = (LinearLayout)findViewById(R.id.custom_settings_bar);
        layout.setVisibility(View.GONE);

        // set name tab not clickable
        item_name.setClickable(false);

        // set time tab gone
        item_time.setVisibility(View.GONE);

        // set delete button gone
        item_delete.setVisibility(View.GONE);

    }


    // Listener definition
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            switch (v.getId()){
                case R.id.custom_settings_name:
                    CustomName nameDialog = new CustomName();
                    // Set parsed value
                    args.putString("name",name);
                    nameDialog.setArguments(args);
                    // start Dialog
                    nameDialog.setCancelable(false);
                    nameDialog.show(getFragmentManager(),"custom_name");
                    break;
                case R.id.custom_settings_time:
                    CustomTime timeDialog = new CustomTime();
                    // Set parsed value
                    args.putString("end_time",end_time);
                    args.putString("start_time",start_time);
                    timeDialog.setArguments(args);
                    timeDialog.setCancelable(false);
                    timeDialog.show(getFragmentManager(),"custom_start");
                    break;
                case R.id.custom_settings_screen:
                    CustomBrightness brightnessDialog = new CustomBrightness();
                    // Set parsed value
                    args.putInt("brightness", brightness);
                    brightnessDialog.setArguments(args);
                    brightnessDialog.setCancelable(false);
                    brightnessDialog.show(getFragmentManager(),"custom_brightness");
                    break;
                case R.id.custom_settings_sound:
                    CustomSound soundDialog = new CustomSound();
                    // set parsed value
                    args.putInt("sound",sound);
                    soundDialog.setArguments(args);
                    soundDialog.setCancelable(false);
                    soundDialog.show(getFragmentManager(),"custom_sound");
                    break;
                case R.id.custom_settings_connect:
                    CustomNetwork networkDialog = new CustomNetwork();
                    // set parsed value
                    args.putInt("wifi",wifi);
                    args.putInt("gprs",gprs);
                    args.putInt("tooth", tooth);
                    networkDialog.setArguments(args);
                    networkDialog.setCancelable(false);
                    networkDialog.show(getFragmentManager(),"custom_network");
                    break;
                case R.id.custom_settings_confirm:
                    Intent confirmIntent = new Intent();
                    confirmIntent.putExtra("data",packageData());
                    confirmIntent.putExtra("delete",-1);
                    setResult(0, confirmIntent);
                    finish();
                    break;
                case R.id.custom_settings_delete:
                    Intent dataIntent = new Intent();
                    dataIntent.putExtra("delete",id);
                    setResult(0,dataIntent);
                    finish();
                    break;
            }
        }
    };

    // checkListener for toggle of custom
    private CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if(isChecked){
                toggle = 1;
            }
            else {
                toggle = 0;
            }
        }
    };

    /**
     * onNameComplete: receiver to get value from CustomName dialog
     * @param param: new name to be set
     */
    public void onNameComplete(String param){
        name = param;
        TextView text = (TextView)findViewById(R.id.custom_settings_name).findViewById(R.id.custom_value);
        text.setText(param);
    }

    /**
     * onTimeComplete: receiver to get value from CustomTime dialog
     * @param param: new start_time and end_time to be set
     */
    public void onTimeComplete(String[] param){
        start_time = param[0];
        end_time = param[1];
        TextView text = (TextView)findViewById(R.id.custom_settings_time).findViewById(R.id.custom_value);
        text.setText(start_time+" ~ "+end_time);
    }

    /**
     * onScreenComplete: receiver to get value from CustomBrightness dialog
     * @param param: new brightness to be set
     */
    public void onScreenComplete(int param){
        brightness = param;
        TextView text = (TextView)findViewById(R.id.custom_settings_screen).findViewById(R.id.custom_value);
        text.setText(param+"");
    }

    /**
     * onSoundComplete: receiver to get value from CustomSound dialog
     * @param param: new sound to be set
     */
    public void onSoundComplete(int param){
        sound = param;
        TextView text = (TextView)findViewById(R.id.custom_settings_sound).findViewById(R.id.custom_value);
        switch (sound){
            case 0:
                text.setText(getString(R.string.ordinary));
                break;
            case 1:
                text.setText(getString(R.string.vibrate));
                break;
            case 2:
                text.setText(getString(R.string.silent));
                break;
        }
    }

    /**
     * onNetworkComplete: receiver to get value from CustomNetwork dialog
     * @param param: new network features to be set
     */
    public void onNetworkComplete(int[] param){
        wifi = param[0];
        gprs = param[1];
        tooth = param[2];
    }
}
