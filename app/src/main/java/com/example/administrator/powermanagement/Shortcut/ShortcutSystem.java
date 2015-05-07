package com.example.administrator.powermanagement.Shortcut;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.example.administrator.powermanagement.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Shortcut System: activity called from shortcut fragment and show the following infomation of system:
 * device model, system version, cpu version, cpu usage, battery capacity, memory usage,
 * internal storage and external storage
 */
public class ShortcutSystem extends ActionBarActivity {

    // toolbar above
    Toolbar mToolbar = null;

    // item_names contains the names and icons of the application
    String[] item_names = null;

    // the id of refresh button on action bar
    static final int REFRESH_ID = 0;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);

        // initialize the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(getResources().getStringArray(R.array.shortcuts)[13]);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        // initialize the view
        initValues();
    }


    /**
     * initValues: initialize the values of each icon_title_value bar and progress bar
     */
    private void initValues(){
        // initialize the array list
        item_names = getResources().getStringArray(R.array.system_info);

        // initialize the image
        ImageView imageView = (ImageView)findViewById(R.id.system_model).findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.device_info);
        imageView = (ImageView)findViewById(R.id.system_version).findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.android);
        imageView = (ImageView)findViewById(R.id.cpu_type).findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.cpu_info);
        imageView = (ImageView)findViewById(R.id.cpu_load).findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.cpu_usage);
        imageView = (ImageView)findViewById(R.id.battery_info).findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.battery_info);



        // initialize the title of the first kind of layout (icon_title_value)
        TextView textView = (TextView)findViewById(R.id.system_model).findViewById(R.id.tab_title);
        textView.setText(item_names[0]);
        textView = (TextView)findViewById(R.id.system_version).findViewById(R.id.tab_title);
        textView.setText(item_names[1]);
        textView = (TextView)findViewById(R.id.cpu_type).findViewById(R.id.tab_title);
        textView.setText(item_names[2]);
        textView = (TextView)findViewById(R.id.cpu_load).findViewById(R.id.tab_title);
        textView.setText(item_names[3]);
        textView = (TextView)findViewById(R.id.battery_info).findViewById(R.id.tab_title);
        textView.setText(item_names[4]);

        // initialize the title of the second kind of layout (progress)
        textView = (TextView)findViewById(R.id.memory_info).findViewById(R.id.progress_title);
        textView.setText(item_names[5]);
        textView = (TextView)findViewById(R.id.internal_info).findViewById(R.id.progress_title);
        textView.setText(item_names[6]);
        textView = (TextView)findViewById(R.id.sd_info).findViewById(R.id.progress_title);
        textView.setText(item_names[7]);

        // initialize the static value
        textView = (TextView)findViewById(R.id.system_model).findViewById(R.id.tab_value);
        textView.setText(getDeviceName());
        textView = (TextView)findViewById(R.id.system_version).findViewById(R.id.tab_value);
        textView.setText(getSystemEdition());
        textView = (TextView)findViewById(R.id.cpu_type).findViewById(R.id.tab_value);
        textView.setText(getCpuName());
        textView = (TextView)findViewById(R.id.battery_info).findViewById(R.id.tab_value);
        textView.setText(getBatteryCapacity());

        // the following function is dynamic and packed to used for refresh
        setCpuView();
        setMemoryView();
        setInternalView();
        setSDView();
    }

    /**
     * getDeviceName: get the manufacturer and model of mobile device
     */
    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return manufacturer + " " + model;
    }

    /**
     * getSystemEdition: get the system edition
     */
    private String getSystemEdition(){
        String number = Build.VERSION.RELEASE;
        int apiCode = Build.VERSION.SDK_INT;
        return number + " (API:" + apiCode + ")";
    }

    /**
     * getCpuName: get the cpu model by reading /proc/cpuinfo
     */
    public String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            return array[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getString(R.string.unknown);
    }

    /**
     * setCpuView: set view of cpu by calling getCpuLoad()
     */
    private void setCpuView(){
        TextView textView = (TextView)findViewById(R.id.cpu_load).findViewById(R.id.tab_value);
        textView.setText(getCpuLoad());
    }

    /**
     * getCpuLoad: get current cpu usage from /proc/stat
     */
    private String getCpuLoad() {
        // cpuInfo1 and cpuInfo2 stores the information getting from /proc/stat
        String[] cpuInfo1, cpuInfo2;
        // cpu usage = ( total1 + total2 - idle ) / ( total1 + total2 ) * 100
        long totalCpu1, totalCpu2, totalIdle;
        // read the cpu stat for the first time
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfo1 = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
            return getString(R.string.unknown);
        }
        // read the cpu stat for the second time
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfo2 = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
            return getString(R.string.unknown);
        }
        // calculate total1, total2 and idle time
        totalCpu1 = Long.parseLong(cpuInfo1[2]) + Long.parseLong(cpuInfo1[3]) + Long.parseLong(cpuInfo1[4]) +
                Long.parseLong(cpuInfo1[5]) + Long.parseLong(cpuInfo1[6]) + Long.parseLong(cpuInfo1[7]) +
                Long.parseLong(cpuInfo1[8]);
        totalCpu2 = Long.parseLong(cpuInfo2[2]) + Long.parseLong(cpuInfo2[3]) + Long.parseLong(cpuInfo2[4]) +
                Long.parseLong(cpuInfo2[6]) + Long.parseLong(cpuInfo2[5]) + Long.parseLong(cpuInfo2[7]) +
                Long.parseLong(cpuInfo2[8]);
        totalIdle = Long.parseLong(cpuInfo1[5]) + Long.parseLong(cpuInfo2[5]);
        // calculate result and format to 2 digits
        double result = ((double)totalCpu1 + totalCpu2 - totalIdle) / (totalCpu1 + totalCpu2) * 100;
        NumberFormat format = new DecimalFormat("#0.00");
        return format.format(result) + "%";
    }

    /**
     * getBatteryCapacity: get battery capacity with revoke method, return mAh
     */
    public String getBatteryCapacity() {
        Object mPowerProfile_;
        double batteryCapacity;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            return getString(R.string.unknown);
        }

        try {
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
        } catch (Exception e) {
            e.printStackTrace();
            return getString(R.string.unknown);
        }
        NumberFormat format = new DecimalFormat("#0.00");
        return format.format(batteryCapacity) + "mAh";
    }

    /**
     * setMemoryView: set memory progress view by calling getMemoryInfo
     */
    private void setMemoryView(){

        // get dynamic view
        TextView total = (TextView)findViewById(R.id.memory_info).findViewById(R.id.progress_total);
        TextView percent = (TextView)findViewById(R.id.memory_info).findViewById(R.id.progress_percent);
        TextView value = (TextView)findViewById(R.id.memory_info).findViewById(R.id.progress_real);
        RoundCornerProgressBar bar = (RoundCornerProgressBar)findViewById(R.id.memory_info).findViewById(R.id.progress_bar);

        // get result
        double[] result = getMemoryInfo();
        // cannot get enough information
        if(result == null){
            total.setText(getString(R.string.unknown));
            percent.setVisibility(View.INVISIBLE);
            value.setVisibility(View.INVISIBLE);
            return;
        }
        // get the information
        NumberFormat format = new DecimalFormat("#0.00");
        total.setText(format.format(result[0]) + "GB");
        value.setText(format.format(result[1]) + "GB");
        double rate = result[1] / result[0] * 100;
        percent.setText(format.format(rate) + "%");
        bar.setProgress((int)rate);
    }

    /**
     * getMemoryInfo: get total and used memory by reading /proc/meminfo and return double[]{total,used}
     */
    private double[] getMemoryInfo(){
        String totalMem, freeMem;
        final long KB2GB = 1024 * 1024;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"), 8192);
            // the first line is total, the second is free
            totalMem = reader.readLine();
            freeMem = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // replaceAll("\\D+","") eliminate all non-number character
        totalMem = totalMem.replaceAll("\\D+", "");
        freeMem = freeMem.replaceAll("\\D+","");
        double total = (double)Long.parseLong(totalMem) / KB2GB;
        double used = ((double)Long.parseLong(totalMem) - Long.parseLong(freeMem)) / KB2GB;
        return new double[]{total, used};
    }

    /**
     * setInternalView: set internal progress view by calling getInternalInfo
     */
    private void setInternalView(){

        // get dynamic view
        TextView total = (TextView)findViewById(R.id.internal_info).findViewById(R.id.progress_total);
        TextView percent = (TextView)findViewById(R.id.internal_info).findViewById(R.id.progress_percent);
        TextView value = (TextView)findViewById(R.id.internal_info).findViewById(R.id.progress_real);
        RoundCornerProgressBar bar = (RoundCornerProgressBar)findViewById(R.id.internal_info).findViewById(R.id.progress_bar);

        // get result
        double[] result = getInternalInfo();

        // cannot get enough information
        if(result == null){
            total.setText(getString(R.string.unknown));
            percent.setVisibility(View.INVISIBLE);
            value.setVisibility(View.INVISIBLE);
            return;
        }
        // set the result with given format
        NumberFormat format = new DecimalFormat("#0.0");
        total.setText(format.format(result[0]) + "MB");
        value.setText(format.format(result[1]) + "MB");
        double rate = result[1] / result[0] * 100;
        percent.setText(format.format(rate) + "%");
        bar.setProgress((int) rate);
    }


    /**
     * getInternalInfo: get internal storage total and used amount in MB format, return double[]{total, used}
     */
    private double[] getInternalInfo(){
        final long B2MB = 1024 * 1024;
        // get data directory: /data
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getAbsolutePath());
        long usedSize = (long)stat.getBlockSize() * ((long)stat.getBlockCount() - (long)stat.getAvailableBlocks());
        long totalSize = (long)stat.getBlockSize() * (long)stat.getBlockCount();
        double used = (double) usedSize / B2MB;
        double total = (double) totalSize / B2MB;
        return new double[]{total, used};
    }

    /**
     * setSdView: set the sd card progress view by calling getSgInfo
     */
    private void setSDView(){

        // get dynamic view
        TextView total = (TextView)findViewById(R.id.sd_info).findViewById(R.id.progress_total);
        TextView percent = (TextView)findViewById(R.id.sd_info).findViewById(R.id.progress_percent);
        TextView value = (TextView)findViewById(R.id.sd_info).findViewById(R.id.progress_real);
        RoundCornerProgressBar bar = (RoundCornerProgressBar)findViewById(R.id.sd_info).findViewById(R.id.progress_bar);

        // get result
        double[] result = getSdInfo();

        // cannot get enough information
        if(result == null){
            total.setText(getString(R.string.unknown));
            percent.setVisibility(View.INVISIBLE);
            value.setVisibility(View.INVISIBLE);
            return;
        }
        // get the information and set them
        NumberFormat format = new DecimalFormat("#0.00");
        total.setText(format.format(result[0]) + "GB");
        value.setText(format.format(result[1]) + "GB");
        double rate = result[1] / result[0] * 100;
        percent.setText(format.format(rate) + "%");
        bar.setProgress((int)rate);
    }

    /**
     * getSdInfo: get the total and use amount of sd card info in GB format
     */
    private double[] getSdInfo(){
        final long B2GB = 1024 * 1024 * 1024;
        File path = Environment.getExternalStorageDirectory();
        StatFs stat;
        // here is a trick because android regard internal storage as a as card
        // so when setting "internal primary" in device, the result would be same
        // as getDataDirectory, just as /storage/emulated/0
        if(!path.getAbsolutePath().equals("/storage/emulated/0")){
            stat = new StatFs(path.getAbsolutePath());
        }
        else{
            stat = new StatFs("/storage/sdcard1");
        }
        long usedSize = (long)stat.getBlockSize() * ((long)stat.getBlockCount() - (long)stat.getAvailableBlocks());
        long totalSize = (long)stat.getBlockSize() * (long)stat.getBlockCount();
        double used = (double) usedSize / B2GB;
        double total = (double) totalSize / B2GB;
        return new double[]{total, used};
    }

    /**
     * onCreateOptionsMenu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuItem refreshItem = menu.add(0, REFRESH_ID, REFRESH_ID, "Refresh");
        refreshItem.setIcon(R.drawable.refresh);
        refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /**
     * onOptionsItemSelected: Control the option menu in action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                return true;
            case REFRESH_ID:
                RefreshAsyncTask task = new RefreshAsyncTask();
                task.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * refreshAsyncTask: the async task that controls the list view change
     */
    public class RefreshAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params){
            return 0;
        }
        @Override
        protected void onPostExecute(Integer param){
            setCpuView();
            setMemoryView();
            setInternalView();
            setSDView();
        }
    }

}
