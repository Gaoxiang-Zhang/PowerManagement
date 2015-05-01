package com.example.administrator.powermanagement.ShortcutDialog;

import android.app.DialogFragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;
import com.gc.materialdesign.views.Slider;

/**
 * BrightnessDialog: the dialog controls and listen to the brightness change
 */
public class BrightnessDialog extends DialogFragment {

    // Slider controls the value of brightness
    Slider brightnessSlider = null;
    // ContentObserver listens to the brightness change
    BrightnessObserver brightnessObserver = null;
    // Signal of brightness change
    final String BRIGHTNESS_STRING = android.provider.Settings.System.SCREEN_BRIGHTNESS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_brightness, container);

        // set content and listener for confirm button
        TextView text = (TextView)view.findViewById(R.id.confirm);
        text.setText(getResources().getString(R.string.OK));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        text = (TextView)view.findViewById(R.id.cancel);
        text.setVisibility(View.GONE);

        // get and initialize brightness slider
        brightnessSlider = (Slider)view.findViewById(R.id.brightnessSlider);
        initSlider(brightnessSlider);

        // set brightness content observer
        final Uri BRIGHTNESS_URL = Settings.System.getUriFor(BRIGHTNESS_STRING);
        brightnessObserver = new BrightnessObserver(new Handler());
        getActivity().getApplicationContext().getContentResolver()
                .registerContentObserver(BRIGHTNESS_URL,true,brightnessObserver);

        return view;
    }

    /**
     * getBrightnessValue: read and return brightness value (10~255)
     */
    private float getBrightnessValue(){
        float curBrightnessValue = 0;
        try{
            curBrightnessValue = android.provider.Settings.System.getInt(
                    getActivity().getContentResolver(),
                    BRIGHTNESS_STRING);
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return curBrightnessValue;
    }

    /**
     * initSlider: initialize the slider and set listener to it
     */
    private void initSlider(Slider slider){
        final int minBrightness = 10;
        final int maxBrightness = 255;
        // set range of slider
        brightnessSlider.setMax(maxBrightness - minBrightness);
        int screenBrightness = (int)getBrightnessValue();
        slider.setValue(screenBrightness);
        slider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int progress) {
                progress = progress + minBrightness;
                android.provider.Settings.System.putInt(getActivity().getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);
                WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
                layoutParams.screenBrightness = progress / (float)255;
                getActivity().getWindow().setAttributes(layoutParams);
            }
        });
    }

    /**
     * BrightnessObserver: get change of brightness in time and change the seekBar progress
     */
    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler h){
            super(h);
        }
        @Override
        public boolean deliverSelfNotifications(){
            return true;
        }
        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            brightnessSlider.setValue((int) getBrightnessValue());
        }
    }

    /**
     * onDestroy: unregister the content observer
     */
    @Override
    public void onDestroy(){
        getActivity().getContentResolver().unregisterContentObserver(brightnessObserver);
        super.onDestroy();

    }
}
