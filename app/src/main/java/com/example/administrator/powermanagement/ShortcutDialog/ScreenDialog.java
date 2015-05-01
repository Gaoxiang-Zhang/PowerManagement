package com.example.administrator.powermanagement.ShortcutDialog;


import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.administrator.powermanagement.R;

/**
 * ScreenDialog: controls the procedure of performance of sleep time
 * Due to using external library, this class is not extended from DialogFragment
 */
public class ScreenDialog{

    private Context context = null;
    // Content Observer for sleep time
    public ScreenObserver screenObserver = null;
    // Main dialog for representing
    private MaterialDialog materialDialog = null;

    /**
     * ScreenDialog: constructor, initialize the local value
     * @param context
     */
    public ScreenDialog(final Context context){
        this.context = context;
        screenObserver = new ScreenObserver(new Handler());
        int time = getScreenOffTime();
        // initialize material dialog with single choice radio button
        // Follow the instruction on https://github.com/afollestad/material-dialogs
        materialDialog = new MaterialDialog.Builder(context)
                .title(R.string.screenOff)
                .items(R.array.offTime)
                .itemsCallbackSingleChoice(timeToPos(time), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        setScreenOffTime(posToTime(i));
                        if(i!=-1){
                            context.getContentResolver().unregisterContentObserver(screenObserver);
                        }
                        return true;
                    }
                })
                .positiveText(R.string.OK)
                .build();
        // Set and register content observer
        final Uri SCREEN_URL = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT);
        screenObserver = new ScreenObserver(new Handler());
        context.getApplicationContext().getContentResolver().registerContentObserver(SCREEN_URL,true,screenObserver);
    }

    /**
     * execute: execute function for external calling
    */
    public void execute(){
        materialDialog.show();
    }

    /**
     * getScreenOffTime: return current screen off time
     */
    private int getScreenOffTime(){
        int screenTime = 0;
        try{
            screenTime = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        }catch ( Exception e ){
            e.printStackTrace();
        }
        return screenTime;
    }

    /**
     * setScreenOffTime: set current screen off time to a given value
     */
    private void setScreenOffTime(int param) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * timeToPos: switch sleep time(ms) to position
     */
    private int timeToPos(int value){
        switch (value){
            case 15000:
                return 0;
            case 30000:
                return 1;
            case 60000:
                return 2;
            case 120000:
                return 3;
            case 300000:
                return 4;
            case 600000:
                return 5;
            case 1800000:
                return 6;
        }
        return -1;
    }

    /**
     * posToTime: switch position to sleep time(ms)
     */
    private int posToTime(int pos){
        switch(pos){
            case 0:
                return 15000;
            case 1:
                return 30000;
            case 2:
                return 60000;
            case 3:
                return 120000;
            case 4:
                return 300000;
            case 5:
                return 600000;
            case 6:
                return 1800000;
        }
        return -1;
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
            materialDialog.setSelectedIndex(timeToPos(getScreenOffTime()));
        }
    }


}
