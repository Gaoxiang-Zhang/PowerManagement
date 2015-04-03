package com.example.administrator.powermanagement;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by Administrator on 15/4/3.
 */
public class EffectDialog extends DialogFragment {

    SwitchCompat dialTone = null;
    SwitchCompat touchSound = null;
    SwitchCompat lockSound = null;
    SwitchCompat touchVib = null;

    final int DIAL_TONE = 0;
    final int TOUCH_SOUND = 1;
    final int LOCK_SOUND = 2;
    final int TOUCH_VIB = 3;

    DialToneObserver dialToneObserver = null;
    TouchSoundObserver touchSoundObserver = null;
    LockSoundObserver lockSoundObserver = null;
    TouchVibObserver touchVibObserver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_effect, container);

        // set text for each components
        TextView text = (TextView)view.findViewById(R.id.dialTone).findViewById(R.id.effect_text);
        text.setText(getResources().getString(R.string.dialTone));
        text = (TextView)view.findViewById(R.id.touchSound).findViewById(R.id.effect_text);
        text.setText(getResources().getString(R.string.touchSound));
        text = (TextView)view.findViewById(R.id.lockSound).findViewById(R.id.effect_text);
        text.setText(getResources().getString(R.string.lockSound));
        text = (TextView)view.findViewById(R.id.touchVib).findViewById(R.id.effect_text);
        text.setText(getResources().getString(R.string.touchVib));
        text = (TextView)view.findViewById(R.id.confirm);
        text.setText(getResources().getString(R.string.OK));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // get the switch in each component
        dialTone = (SwitchCompat)view.findViewById(R.id.dialTone).findViewById(R.id.effect_switch);
        touchSound = (SwitchCompat)view.findViewById(R.id.touchSound).findViewById(R.id.effect_switch);
        lockSound = (SwitchCompat)view.findViewById(R.id.lockSound).findViewById(R.id.effect_switch);
        touchVib = (SwitchCompat)view.findViewById(R.id.touchVib).findViewById(R.id.effect_switch);

        // set initial state
        dialTone.setChecked(getState(DIAL_TONE));
        touchSound.setChecked(getState(TOUCH_SOUND));
        lockSound.setChecked(getState(LOCK_SOUND));
        touchVib.setChecked(getState(TOUCH_VIB));

        // set on change listener
        dialTone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setState(DIAL_TONE, b);
            }
        });
        touchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setState(TOUCH_SOUND, b);
            }
        });
        lockSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setState(LOCK_SOUND, b);
            }
        });
        touchVib.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setState(TOUCH_VIB, b);
            }
        });

        //register the content observer
        final Uri TOUCH_SOUND_URL = Settings.System.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED);
        final Uri LOCK_SOUND_URL = Settings.System.getUriFor("lockscreen_sounds_enabled");
        final Uri DIAL_TONE_URL = Settings.System.getUriFor(Settings.System.DTMF_TONE_WHEN_DIALING);
        final Uri TOUCH_VIB_URL = Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED);

        dialToneObserver = new DialToneObserver(new Handler());
        getActivity().getApplicationContext().getContentResolver().
                registerContentObserver(DIAL_TONE_URL,true,dialToneObserver);
        touchSoundObserver = new TouchSoundObserver(new Handler());
        getActivity().getApplicationContext().getContentResolver().
                registerContentObserver(TOUCH_SOUND_URL,true,touchSoundObserver);
        lockSoundObserver = new LockSoundObserver(new Handler());
        getActivity().getApplicationContext().getContentResolver().
                registerContentObserver(LOCK_SOUND_URL,true,lockSoundObserver);
        touchVibObserver = new TouchVibObserver(new Handler());
        getActivity().getApplicationContext().getContentResolver().
                registerContentObserver(TOUCH_VIB_URL, true, touchVibObserver);
        return view;
    }

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
    }

    /**
     * getState: get the state
     * @param flag: judge which data to be read
     */
    private boolean getState(int flag){
        boolean state = false;
        try{
            switch(flag){
                case DIAL_TONE:
                    state = ( Settings.System.getInt(getActivity().getContentResolver(),
                            Settings.System.DTMF_TONE_WHEN_DIALING,1) != 0 );
                    break;
                case TOUCH_SOUND:
                    state = ( Settings.System.getInt(getActivity().getContentResolver(),
                            Settings.System.SOUND_EFFECTS_ENABLED,1) != 0 );
                    break;
                case LOCK_SOUND:
                    state = ( Settings.System.getInt(getActivity().getContentResolver()
                            ,"lockscreen_sounds_enabled",1) != 0 );
                    break;
                case TOUCH_VIB:
                    state = ( Settings.System.getInt(getActivity().getContentResolver()
                            ,Settings.System.HAPTIC_FEEDBACK_ENABLED,1) != 0 );
                    break;
            }
        }catch ( Exception e ){
            e.printStackTrace();
        }
        return state;
    }

    /**
     * setState: set the state
     * @param flag: judge which data to be read
     * @param param: value to be set
     */
    private void setState(int flag, boolean param){
        int value = param ? 1 : 0;
        try{
            switch(flag){
                case DIAL_TONE:
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.DTMF_TONE_WHEN_DIALING,value);
                    break;
                case TOUCH_SOUND:
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SOUND_EFFECTS_ENABLED,value);
                    break;
                case LOCK_SOUND:
                    Settings.System.putInt(getActivity().getContentResolver(),
                            "lockscreen_sounds_enabled",value);
                    break;
                case TOUCH_VIB:
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.HAPTIC_FEEDBACK_ENABLED,value);
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
            dialTone.setChecked(getState(DIAL_TONE));
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
            touchSound.setChecked(getState(TOUCH_SOUND));
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
            lockSound.setChecked(getState(LOCK_SOUND));
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
            touchVib.setChecked(getState(TOUCH_VIB));
        }
    }

    /**
     * onDestroy: unregister ContentObservers
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().getContentResolver().unregisterContentObserver(dialToneObserver);
        getActivity().getContentResolver().unregisterContentObserver(touchSoundObserver);
        getActivity().getContentResolver().unregisterContentObserver(lockSoundObserver);
        getActivity().getContentResolver().unregisterContentObserver(touchVibObserver);
    }
}
