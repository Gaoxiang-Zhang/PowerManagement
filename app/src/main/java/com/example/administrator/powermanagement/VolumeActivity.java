package com.example.administrator.powermanagement;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

/**
 * Volume Activity: The Activity shows the volume interface
 */
public class VolumeActivity extends Activity {
    // SeekBar of 4 volume respects
    ImageView system = null;
    SeekBar ring = null;
    SeekBar media = null;
    SeekBar alarm = null;
    SeekBar voice = null;
    // AudioManager controls the stat and change of the audio
    AudioManager audioManager = null;
    //
    IntentFilter intentFilter;
    //
    int savedVolume = 0;

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        // set view defined in activity_volume.xml
        setContentView(R.layout.activity_volume);
        // set the width of this dialog activity
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels *0.90);
        getWindow().setLayout(screenWidth, ActionBar.LayoutParams.WRAP_CONTENT);
        // initialize the AudioManager
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

        system = (ImageView)findViewById(R.id.imgRing);
        alarm = (SeekBar)findViewById(R.id.alarm_bar);
        media = (SeekBar)findViewById(R.id.music_bar);
        ring = (SeekBar)findViewById(R.id.ring_bar);
        voice = (SeekBar)findViewById(R.id.voice_bar);

        //voice=0, system=1, ring=2, music=3, alarm=4
        initBar(alarm, AudioManager.STREAM_ALARM);
        initBar(media,AudioManager.STREAM_MUSIC);
        initBar(ring,AudioManager.STREAM_RING);
        initBar(voice,AudioManager.STREAM_VOICE_CALL);

        system.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                if(volume == 0){
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    system.setImageResource(R.drawable.volume_normal);
                    ring.setProgress(savedVolume);
                }
                else{
                    savedVolume = volume;
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    system.setImageResource(R.drawable.volume_vibrate);
                    ring.setProgress(0);
                }
            }
        });

        registerReceiver(mReceiver,intentFilter);
    }

    /**
     * initBar: initial and set controllers in seek bar
     */
    private void initBar(SeekBar bar, final int stream){
        int volume = audioManager.getStreamVolume(stream);
        bar.setMax(audioManager.getStreamMaxVolume(stream));
        bar.setProgress(volume);
        if(stream == AudioManager.STREAM_RING && volume == 0){
            int mode = audioManager.getRingerMode();
            if(mode == AudioManager.RINGER_MODE_VIBRATE){
                system.setImageResource(R.drawable.volume_vibrate);
            }
            else if(mode == AudioManager.RINGER_MODE_SILENT){
                system.setImageResource(R.drawable.volume_silent);
            }
        }
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                audioManager.setStreamVolume(stream,progress,AudioManager.FLAG_PLAY_SOUND);
            }
            public void onStartTrackingTouch(SeekBar seekBar){

            }
            public void onStopTrackingTouch(SeekBar seekBar){

            }
        });
    }
    /**
     *
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent){
            int old_volume = (Integer)intent.getExtras().get("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE");
            int volume = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
            int type = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_TYPE");
            switch(type){
                case AudioManager.STREAM_VOICE_CALL:
                    voice.setProgress(volume);
                    break;
                case AudioManager.STREAM_MUSIC:
                    media.setProgress(volume);
                    break;
                case AudioManager.STREAM_ALARM:
                    alarm.setProgress(volume);
                    break;
                case AudioManager.STREAM_RING:
                    handlerRingChanged(old_volume,volume);
                    break;
            }
        }
    };
    /**
     *
     */
    public void handlerRingChanged(int old_value, int new_value){
        int mode = audioManager.getRingerMode();
        if(old_value > 0 && new_value > 0){
            ring.setProgress(new_value);
        }
        else if(old_value > 0 && new_value == 0){
            system.setImageResource(R.drawable.volume_vibrate);
            ring.setProgress(new_value);
        }
        else if(old_value == 0 && new_value > 0){
            system.setImageResource(R.drawable.volume_normal);
            ring.setProgress(new_value);
        }
        else if(old_value == 0 && new_value == 0){
            if(mode == AudioManager.RINGER_MODE_SILENT){
                system.setImageResource(R.drawable.volume_silent);
            }
            else if(mode == AudioManager.RINGER_MODE_VIBRATE){
                system.setImageResource(R.drawable.volume_vibrate);
            }
        }
        return;
    }
    /**
     *
     */
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
