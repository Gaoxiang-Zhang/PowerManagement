package com.example.administrator.powermanagement;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.SeekBar;

/**
 * Volume Activity: The Activity shows the volume interface
 */
public class VolumeActivity extends Activity {
    // SeekBar of 5 volume respects
    SeekBar alarm = null;
    SeekBar music = null;
    SeekBar ring = null;
    SeekBar system = null;
    SeekBar voice = null;
    // AudioManager controls the stat and change of the audio
    AudioManager audioManager = null;

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

        alarm = (SeekBar)findViewById(R.id.alarm_bar);
        music = (SeekBar)findViewById(R.id.music_bar);
        ring = (SeekBar)findViewById(R.id.ring_bar);
        system = (SeekBar)findViewById(R.id.system_bar);
        voice = (SeekBar)findViewById(R.id.voice_bar);

        initBar(alarm,AudioManager.STREAM_ALARM);
        initBar(music,AudioManager.STREAM_MUSIC);
        initBar(ring,AudioManager.STREAM_RING);
        initBar(system,AudioManager.STREAM_SYSTEM);
        initBar(voice,AudioManager.STREAM_VOICE_CALL);
    }

    /**
     * initBar: initial and set controllers in seek bar
     */
    private void initBar(SeekBar bar, final int stream){
        bar.setMax(audioManager.getStreamMaxVolume(stream));
        bar.setProgress(audioManager.getStreamVolume(stream));
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
}
