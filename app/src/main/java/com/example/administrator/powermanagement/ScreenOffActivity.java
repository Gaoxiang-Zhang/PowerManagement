package com.example.administrator.powermanagement;

import android.app.ActionBar;
import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * ScreenOffActivity: Activity contains screen time controller
 */
public class ScreenOffActivity extends Activity {

    // RadioGroup and ContentObserver
    RadioGroup radioGroup = null;
    ScreenObserver screenObserver = null;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_screen);

        // set the width of this dialog activity
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels *0.90);
        getWindow().setLayout(screenWidth, ActionBar.LayoutParams.WRAP_CONTENT);

        //set the initial value of radio group
        radioGroup = (RadioGroup)findViewById(R.id.screenGroup);
        setRadioGroup();

        //set onChangeListener of RadioGroup
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb1 = (RadioButton) findViewById(R.id.screenBtn1);
                RadioButton rb2 = (RadioButton) findViewById(R.id.screenBtn2);
                RadioButton rb3 = (RadioButton) findViewById(R.id.screenBtn3);
                RadioButton rb4 = (RadioButton) findViewById(R.id.screenBtn4);
                RadioButton rb5 = (RadioButton) findViewById(R.id.screenBtn5);
                RadioButton rb6 = (RadioButton) findViewById(R.id.screenBtn6);
                RadioButton rb7 = (RadioButton) findViewById(R.id.screenBtn7);
                if (rb1.isChecked()) {
                    setScreenOffTime(15000);
                } else if (rb2.isChecked()) {
                    setScreenOffTime(30000);
                } else if (rb3.isChecked()) {
                    setScreenOffTime(60000);
                } else if (rb4.isChecked()) {
                    setScreenOffTime(120000);
                } else if (rb5.isChecked()) {
                    setScreenOffTime(300000);
                } else if (rb6.isChecked()) {
                    setScreenOffTime(600000);
                } else if (rb7.isChecked()) {
                    setScreenOffTime(1800000);
                }
            }
        });

        // set ContentObserver to handle the sync problem
        final Uri SCREEN_URL = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT);
        screenObserver = new ScreenObserver(new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(SCREEN_URL,true,screenObserver);
    }

    /**
     * setRadioGroup: set which button is checked based on current value
     */
    private void setRadioGroup(){
        int value = getScreenOffTime();
        switch (value){
            case 15000:
                radioGroup.check(R.id.screenBtn1);
                break;
            case 30000:
                radioGroup.check(R.id.screenBtn2);
                break;
            case 60000:
                radioGroup.check(R.id.screenBtn3);
                break;
            case 120000:
                radioGroup.check(R.id.screenBtn4);
                break;
            case 300000:
                radioGroup.check(R.id.screenBtn5);
                break;
            case 600000:
                radioGroup.check(R.id.screenBtn6);
                break;
            case 1800000:
                radioGroup.check(R.id.screenBtn7);
                break;
        }
    }

    /**
     * getScreenOffTime: return current screen off time
     */
    private int getScreenOffTime(){
        int screenTime = 0;
        try{
            screenTime = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT);
        }catch ( Exception e ){
            e.printStackTrace();
        }
        return screenTime;
    }

    /**
     * setScreenOffTime: set current screen off time to a given value
     */
    private void setScreenOffTime(int param){
        try{
            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,param);
        }catch ( Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ScreenObserver: listen to content change
     */
    private class ScreenObserver extends ContentObserver {
        public ScreenObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            setRadioGroup();
        }
    }

    /**
     * onDestroy: unregister the screenObserver
     */
    @Override
    public void onDestroy(){
        getContentResolver().unregisterContentObserver(screenObserver);
        super.onDestroy();
    }

}
