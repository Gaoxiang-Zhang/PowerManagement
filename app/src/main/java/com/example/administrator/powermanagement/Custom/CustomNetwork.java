package com.example.administrator.powermanagement.Custom;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;

/**
 * CustomNetwork: setting network dialog
 */
public class CustomNetwork extends DialogFragment {

    // 3 switch
    SwitchCompat wifiSwitch = null;
    SwitchCompat gprsSwitch = null;
    SwitchCompat toothSwitch = null;


    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_custom_network, container);

        // get arguments
        int wifi_value = getArguments().getInt("wifi");
        int gprs_value = getArguments().getInt("gprs");
        int tooth_value = getArguments().getInt("tooth");

        // set interface text
        TextView text = (TextView)view.findViewById(R.id.wifi).findViewById(R.id.switch_text);
        text.setText(getString(R.string.wifi));
        text = (TextView)view.findViewById(R.id.gprs).findViewById(R.id.switch_text);
        text.setText(getString(R.string.gprs));
        text = (TextView)view.findViewById(R.id.tooth).findViewById(R.id.switch_text);
        text.setText(getString(R.string.bluetooth));

        // set interface switch compat value
        wifiSwitch = (SwitchCompat)view.findViewById(R.id.wifi).findViewById(R.id.switch_switch);
        wifiSwitch.setChecked(wifi_value==1);
        gprsSwitch = (SwitchCompat)view.findViewById(R.id.gprs).findViewById(R.id.switch_switch);
        gprsSwitch.setChecked(gprs_value==1);
        toothSwitch = (SwitchCompat)view.findViewById(R.id.tooth).findViewById(R.id.switch_switch);
        toothSwitch.setChecked(tooth_value==1);

        // Set logic for confirm and cancel
        text = (TextView)view.findViewById(R.id.confirm);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] params = new int[3];
                params[0] = wifiSwitch.isChecked() ? 1 : 0;
                params[1] = gprsSwitch.isChecked() ? 1 : 0;
                params[2] = toothSwitch.isChecked() ? 1 : 0;
                mListener.onNetworkComplete(params);
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
     * The following function is realized by CustomSettings to get value from dialog
     * OnNetworkCompleteListener
     * mListener
     * onAttach
     */

    public static interface OnNetworkCompleteListener {
        public abstract void onNetworkComplete(int[] params);
    }

    private OnNetworkCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnNetworkCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
