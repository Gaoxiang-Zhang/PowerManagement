package com.example.administrator.powermanagement.Custom;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;
import com.gc.materialdesign.views.Slider;

/**
 * CustomBrightness: setting brightness dialog
 */
public class CustomBrightness extends DialogFragment {

    // Slider controls the value of brightness
    Slider brightnessSlider = null;

    // get old brightness settings
    int old_brightness;

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_brightness, container);
        brightnessSlider = (Slider)view.findViewById(R.id.brightnessSlider);

        // get parameter of old settings
        old_brightness = getArguments().getInt("brightness");

        // initial slider
        initialSlider();

        // set logic for confirm and cancel buttons
        TextView text = (TextView)view.findViewById(R.id.confirm);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = brightnessSlider.getValue();
                mListener.onScreenComplete(value);
                getDialog().dismiss();
            }
        });
        text = (TextView)view.findViewById(R.id.cancel);
        text.setVisibility(View.VISIBLE);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    /**
     * initialSlider: initialize brightness slider
     */
    private void initialSlider(){
        brightnessSlider.setMax(255);
        brightnessSlider.setMin(10);
        brightnessSlider.setValue(old_brightness);
    }

    /**
     * The following function is realized by CustomSettings to get value from dialog
     * OnScreenCompleteListener
     * mListener
     * onAttach
     */
    public static interface OnScreenCompleteListener {
        public abstract void onScreenComplete(int param);
    }

    private OnScreenCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnScreenCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
