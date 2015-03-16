package com.example.administrator.powermanagement;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

/**
 * Created by Administrator on 15/3/16.
 */
public class VibrationActivity extends Activity {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_vibration);

        // set the width of this dialog activity
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels *0.90);
        getWindow().setLayout(screenWidth, ActionBar.LayoutParams.WRAP_CONTENT);
    }
}
