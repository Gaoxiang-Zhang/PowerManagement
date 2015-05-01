package com.example.administrator.powermanagement.ShortcutDialog;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;
import com.gc.materialdesign.views.Slider;

/**
 * VolumeDialog: generate from shortcut fragment and controls the volume change
 */
public class VolumeDialog extends DialogFragment {

    // The sliders
    Slider ringSlider = null;
    Slider alarmSlider = null;
    Slider mediaSlider = null;
    Slider voiceSlider = null;

    // AudioManager controls the stat and change of the audio
    AudioManager audioManager = null;

    // IntentFilter filters the intent of broadcast of volume change
    IntentFilter intentFilter = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Initial the audioManager and IntentFilter
        audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

        View view = inflater.inflate(R.layout.fragment_volume, container);

        // set text for each line
        TextView text = (TextView) view.findViewById(R.id.ring).findViewById(R.id.volumeText);
        text.setText(getResources().getString(R.string.ring));
        text = (TextView) view.findViewById(R.id.alarm).findViewById(R.id.volumeText);
        text.setText(getResources().getString(R.string.alarm));
        text = (TextView) view.findViewById(R.id.media).findViewById(R.id.volumeText);
        text.setText(getResources().getString(R.string.media));
        text = (TextView) view.findViewById(R.id.voice).findViewById(R.id.volumeText);
        text.setText(getResources().getString(R.string.voice));
        text = (TextView)view.findViewById(R.id.confirm);
        text.setText(getResources().getString(R.string.OK));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // set icon for each line
        ImageView icon = (ImageView) view.findViewById(R.id.ring).findViewById(R.id.volumeIcon);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ring));
        icon = (ImageView) view.findViewById(R.id.alarm).findViewById(R.id.volumeIcon);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.alarm));
        icon = (ImageView) view.findViewById(R.id.media).findViewById(R.id.volumeIcon);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.media));
        icon = (ImageView) view.findViewById(R.id.voice).findViewById(R.id.volumeIcon);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.voice));

        // get slider for each line
        ringSlider = (Slider)view.findViewById(R.id.ring).findViewById(R.id.volumeSlider);
        alarmSlider = (Slider)view.findViewById(R.id.alarm).findViewById(R.id.volumeSlider);
        mediaSlider = (Slider)view.findViewById(R.id.media).findViewById(R.id.volumeSlider);
        voiceSlider = (Slider)view.findViewById(R.id.voice).findViewById(R.id.volumeSlider);

        // initialize slider
        initSlider(ringSlider,AudioManager.STREAM_RING);
        initSlider(alarmSlider,AudioManager.STREAM_ALARM);
        initSlider(mediaSlider,AudioManager.STREAM_MUSIC);
        initSlider(voiceSlider,AudioManager.STREAM_VOICE_CALL);

        getActivity().registerReceiver(mReceiver,intentFilter);

        return view;
    }

    /**
     * initSlider: initialize and set controller for each slider
     */
    private void initSlider(Slider slider,final int stream){
        int volume = audioManager.getStreamVolume(stream);
        slider.setMax(audioManager.getStreamMaxVolume(stream));
        slider.setValue(volume);
        slider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int i) {
                audioManager.setStreamVolume(stream,i,AudioManager.FLAG_PLAY_SOUND);
            }
        });
    }

    /**
     * mReceiver: get the broadcast of volume change
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int volume = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
            int type = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_TYPE");
            switch(type){
                case AudioManager.STREAM_RING:
                    ringSlider.setValue(volume);
                    break;
                case AudioManager.STREAM_ALARM:
                    alarmSlider.setValue(volume);
                    break;
                case AudioManager.STREAM_MUSIC:
                    mediaSlider.setValue(volume);
                    break;
                case AudioManager.STREAM_VOICE_CALL:
                    voiceSlider.setValue(volume);
                    break;
            }
        }
    };

    /**
     * onDestroy: unregister mReceiver
     */
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }
}
