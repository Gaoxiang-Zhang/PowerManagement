package com.example.administrator.powermanagement;

import android.app.ActionBar;
import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * InterActionActivity: Controls dial pad sound, touch sound, screen lock sound and touch vibrate
 */
public class InteractionActivity extends Activity {

    // 4 Switchs
    Switch dialTone = null;
    Switch touchSound = null;
    Switch lockSound = null;
    Switch touchVib = null;

    // 4 corresponding flags
    final int DIAL_TONE = 1;
    final int TOUCH_SOUND = 2;
    final int LOCK_SOUND = 3;
    final int TOUCH_VIB = 4;

    // 4 corresponding ContentObservers
    DialToneObserver dialToneObserver;
    TouchSoundObserver touchSoundObserver;
    LockSoundObserver lockSoundObserver;
    TouchVibObserver touchVibObserver;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_interaction);

        // set the width of this dialog activity
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels *0.90);
        getWindow().setLayout(screenWidth, ActionBar.LayoutParams.WRAP_CONTENT);

        /** DialTone Settings **/
        // get every switch
        dialTone = (Switch)findViewById(R.id.DialTone);
        // set initial state
        dialTone.setChecked(getVibState(DIAL_TONE));
        // set on change listener
        dialTone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setVibState(DIAL_TONE,b);
            }
        });
        // register content observer
        final Uri DIAL_TONE_URL = Settings.System.getUriFor(Settings.System.DTMF_TONE_WHEN_DIALING);
        dialToneObserver = new DialToneObserver(new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(DIAL_TONE_URL,true,dialToneObserver);

        /** TouchSound Settings **/
        touchSound = (Switch)findViewById(R.id.touchSound);
        touchSound.setChecked(getVibState(TOUCH_SOUND));
        touchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setVibState(TOUCH_SOUND,b);
            }
        });
        final Uri TOUCH_SOUND_URL = Settings.System.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED);
        touchSoundObserver = new TouchSoundObserver(new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(TOUCH_SOUND_URL,true,touchSoundObserver);

        /** LockSound Settings **/
        lockSound = (Switch)findViewById(R.id.lockSound);
        lockSound.setChecked(getVibState(LOCK_SOUND));
        lockSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setVibState(LOCK_SOUND, b);
            }
        });
        final Uri LOCK_SOUND_URL = Settings.System.getUriFor("lockscreen_sounds_enabled");
        lockSoundObserver = new LockSoundObserver(new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(LOCK_SOUND_URL,true,lockSoundObserver);

        /** TouchVibration Settings **/
        touchVib = (Switch)findViewById(R.id.touchVib);
        touchVib.setChecked(getVibState(TOUCH_VIB));
        touchVib.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setVibState(TOUCH_VIB,b);
            }
        });
        final Uri TOUCH_VIB_URL = Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED);
        touchVibObserver = new TouchVibObserver(new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(TOUCH_VIB_URL,true,touchVibObserver);
    }

    /**
     * getVibState: get the state
     * @param flag: judge which data to be read
     */
    private boolean getVibState(int flag){
        boolean state = false;
        try{
            switch(flag){
                case DIAL_TONE:
                    state = ( Settings.System.getInt(getContentResolver(),Settings.System.DTMF_TONE_WHEN_DIALING,1) != 0 );
                    break;
                case TOUCH_SOUND:
                    state = ( Settings.System.getInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,1) != 0 );
                    break;
                case LOCK_SOUND:
                    state = ( Settings.System.getInt(getContentResolver(),"lockscreen_sounds_enabled",1) != 0 );
                    break;
                case TOUCH_VIB:
                    state = ( Settings.System.getInt(getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,1) != 0 );
                    break;
            }
        }catch ( Exception e ){
            e.printStackTrace();
        }
        return state;
    }

    /**
     * setVibState: set the state
     * @param flag: judge which data to be read
     * @param param: value to be set
     */
    private void setVibState(int flag, boolean param){
        int value = param ? 1 : 0;
        try{
            switch(flag){
                case DIAL_TONE:
                    Settings.System.putInt(getContentResolver(),Settings.System.DTMF_TONE_WHEN_DIALING,value);
                    break;
                case TOUCH_SOUND:
                    Settings.System.putInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,value);
                    break;
                case LOCK_SOUND:
                    Settings.System.putInt(getContentResolver(),"lockscreen_sounds_enabled",value);
                    break;
                case TOUCH_VIB:
                    Settings.System.putInt(getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,value);
                    break;
            }
        }catch ( Exception e ){
            e.printStackTrace();
        }
    }


    /**
     * ContentObserver definition
     */
    private class DialToneObserver extends ContentObserver {
        public DialToneObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            dialTone.setChecked(getVibState(DIAL_TONE));
        }
    }
    private class TouchSoundObserver extends ContentObserver{
        public TouchSoundObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            touchSound.setChecked(getVibState(TOUCH_SOUND));
        }
    }
    private class LockSoundObserver extends ContentObserver{
        public LockSoundObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            lockSound.setChecked(getVibState(LOCK_SOUND));
        }
    }
    private class TouchVibObserver extends ContentObserver{
        public TouchVibObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            lockSound.setChecked(getVibState(LOCK_SOUND));
        }
    }

    /**
     * onDestroy: unregister ContentObservers
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        getContentResolver().unregisterContentObserver(dialToneObserver);
        getContentResolver().unregisterContentObserver(touchSoundObserver);
        getContentResolver().unregisterContentObserver(lockSoundObserver);
        getContentResolver().unregisterContentObserver(touchVibObserver);
    }
}
