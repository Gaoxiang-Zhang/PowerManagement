package com.example.administrator.powermanagement.Custom;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.powermanagement.R;
import com.gc.materialdesign.views.CheckBox;

import org.w3c.dom.Text;

/**
 * CustomSound: setting sound custom
 */
public class CustomSound extends DialogFragment {

    // old value of sound, 0 represents ring, 1 represents vibrate, 2 represents silent
    int old_value;
    // radio button for single choice
    RadioButton buttonOrdinary = null;
    RadioButton buttonVibrate = null;
    RadioButton buttonSilent = null;

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_custom_sound, container);

        // get parameter
        old_value = getArguments().getInt("sound");

        // get radio button
        buttonOrdinary = (RadioButton)view.findViewById(R.id.sound_ordinary);
        buttonVibrate = (RadioButton)view.findViewById(R.id.sound_vibrate);
        buttonSilent = (RadioButton)view.findViewById(R.id.sound_silent);
        switch (old_value){
            case 0:
                buttonOrdinary.setChecked(true);
                break;
            case 1:
                buttonVibrate.setChecked(true);
                break;
            case 2:
                buttonSilent.setChecked(true);
                break;
        }

        // Set logic for confirm and cancel
        TextView text = (TextView)view.findViewById(R.id.confirm);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSoundComplete(getValue());
                getDialog().dismiss();
            }
        });
        text = (TextView)view.findViewById(R.id.cancel);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    /**
     * get value based on single choice
     */
    private int getValue(){
        if(buttonOrdinary.isChecked()){
            return 0;
        }
        else if(buttonVibrate.isChecked()){
            return 1;
        }
        return 2;
    }

    /**
     * The following function is realized by CustomSettings to get value from dialog
     * OnSoundCompleteListener
     * mListener
     * onAttach
     */

    public static interface OnSoundCompleteListener {
        public abstract void onSoundComplete(int param);
    }

    private OnSoundCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnSoundCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
