package com.example.administrator.powermanagement;

import android.app.ApplicationErrorReport;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Administrator on 15/5/4.
 */
public class RankingActivity extends ActionBarActivity {

    ApplicationErrorReport.BatteryInfo info = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);
    }
}