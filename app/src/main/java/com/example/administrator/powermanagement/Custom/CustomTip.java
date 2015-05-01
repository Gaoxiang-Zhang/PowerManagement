package com.example.administrator.powermanagement.Custom;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;

/**
 * CustomTip:
 */
public class CustomTip extends Activity {


    String[] data;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tip);
        data = getIntent().getStringArrayExtra("data");

        initialInterface();
    }

    private void initialInterface(){

        // initial title of tip activity
        TextView text = (TextView)findViewById(R.id.tip_name).findViewById(R.id.custom_title);
        text.setText(getString(R.string.mode_name));
        text = (TextView)findViewById(R.id.tip_time).findViewById(R.id.custom_title);
        text.setText(getString(R.string.start_time));
        text = (TextView)findViewById(R.id.tip_brightness).findViewById(R.id.custom_title);
        text.setText(getString(R.string.brightness));
        text = (TextView)findViewById(R.id.tip_sound).findViewById(R.id.custom_title);
        text.setText(getString(R.string.sound));
        text = (TextView)findViewById(R.id.tip_wifi).findViewById(R.id.custom_title);
        text.setText(getString(R.string.wifi));
        text = (TextView)findViewById(R.id.tip_gprs).findViewById(R.id.custom_title);
        text.setText(getString(R.string.gprs));
        text = (TextView)findViewById(R.id.tip_tooth).findViewById(R.id.custom_title);
        text.setText(getString(R.string.bluetooth));

        // initial value of activity
        text = (TextView)findViewById(R.id.tip_name).findViewById(R.id.custom_value);
        text.setText(data[1]);
        text = (TextView)findViewById(R.id.tip_time).findViewById(R.id.custom_value);
        text.setText(data[2]+" ~ "+data[3]);
        text = (TextView)findViewById(R.id.tip_brightness).findViewById(R.id.custom_value);
        text.setText(data[4]);
        text = (TextView)findViewById(R.id.tip_sound).findViewById(R.id.custom_value);
        switch (data[5]){
            case "0":
                text.setText(getString(R.string.ordinary));
                break;
            case "1":
                text.setText(getString(R.string.vibrate));
                break;
            case "2":
                text.setText(getString(R.string.silent));
                break;
        }
        text = (TextView)findViewById(R.id.tip_wifi).findViewById(R.id.custom_value);
        text.setText(data[6] == "1" ? getString(R.string.open) : getString(R.string.close));
        text = (TextView)findViewById(R.id.tip_gprs).findViewById(R.id.custom_value);
        text.setText(data[7] == "1" ? getString(R.string.open) : getString(R.string.close));
        text = (TextView)findViewById(R.id.tip_tooth).findViewById(R.id.custom_value);
        text.setText(data[8] == "1" ? getString(R.string.open) : getString(R.string.close));

        text = (TextView)findViewById(R.id.confirm);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
