package com.example.administrator.powermanagement.Config;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.powermanagement.Admins.BluetoothAdmin;
import com.example.administrator.powermanagement.Admins.DBAdapter;
import com.example.administrator.powermanagement.Admins.LocationAdmin;
import com.example.administrator.powermanagement.Admins.NetworkAdmin;
import com.example.administrator.powermanagement.MainActivity;
import com.example.administrator.powermanagement.R;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ConfigService for monitoring and writing to database
 */
public class ConfigService extends Service {

    // base context
    Context context = null;

    // admin for database, bluetooth, network, power(screen state) and audio
    DBAdapter dbAdapter = null;
    BluetoothAdmin bluetoothAdmin = null;
    NetworkAdmin networkAdmin = null;
    PowerManager powerManager = null;
    AudioManager audioManager = null;

    // location manager and result
    LocationAdmin locationAdmin = null;
    BroadcastReceiver locationReceiver = null;
    IntentFilter locationFilter = null;
    double latitude = 0, longitude = 0;

    //eventReceiver to receive screen event and timer event
    BroadcastReceiver eventReceiver = null;
    IntentFilter eventFilter = null;

    // intent holding timer service
    Intent timerIntent = null;

    // user data with screen on
    ArrayList<String[]> userData;

    // DEBUG_TAG for debug
    final String DEBUG_TAG = "config info";
    final static long KBYTE = 1024L;
    final static int PATTERN_TAG = 0;
    final static int MONITOR_TAG = 1;
    final static double SIMILAR_THRESHOLD = 0.50;
    final static double SAME_THRESHOLD = 0.80;
    final static long MIN_NETWORK_FLOW = 100;
    final static long MIN_MOBILE_FLOW = 100;
    private final static double DEF_PI = 3.14159265359; // PI
    private final static double DEF_2PI= 6.28318530712; // 2*PI
    private final static double DEF_PI180= 0.01745329252; // PI/180.0
    private final static double DEF_R =6370693.5; // radius of earth


    // network information

    private long RxBytes = 0;
    private long TxBytes = 0;
    private long MobileRx = 0;
    private long MobileTx = 0;

    // monitoring information
    int userInteractionTimes = 0;
    int heuristicTimes = 0;
    int predictionTimes = 0;

    boolean flagFirst = true;

    //
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;


    public void onCreate() {
        context = this;
        Log.d(DEBUG_TAG, "start config service");
        initAdmin();
        initReceiver();
        userData = new ArrayList<>();
        sharedPreferences = getSharedPreferences(MainActivity.PREF_NAME, 0);
        int firstOfConfig = sharedPreferences.getInt("firstOfConfig", 0);
        // first time of execution
        if(firstOfConfig == 0){
            Log.d(DEBUG_TAG, "First time of config");
            setHeuristic(null);
            editor = sharedPreferences.edit();
            editor.putInt("firstOfConfig", 1);
            editor.apply();
        }
        startTimerService();
    }

    /**
     * initialize the admin used in config service
     */
    private void initAdmin(){
        // Initialize the database adapter
        dbAdapter = DBAdapter.getInstance(this);
        bluetoothAdmin = new BluetoothAdmin();
        networkAdmin = new NetworkAdmin(context);
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        // initialize location admin and receiver with intent filter to get longitude and latitude
        locationAdmin = new LocationAdmin(getApplicationContext());
        locationFilter = new IntentFilter();
        locationFilter.addAction("com.example.administrator.powermanagement.location");
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                latitude = intent.getDoubleExtra("latitude", 0.0);
                longitude = intent.getDoubleExtra("longitude", 0.0);
            }
        };
        registerReceiver(locationReceiver, locationFilter);
    }

    /**
     * initReceiver: initialize the receiver of timer task and screen event
     */
    private void initReceiver(){

        final String SCREEN_ON_ACTION = "android.intent.action.SCREEN_ON";
        final String SCREEN_OFF_ACTION = "android.intent.action.SCREEN_OFF";
        final String TIMER_ACTION = "com.example.administrator.powermanagement.timer";

        eventFilter = new IntentFilter();
        eventFilter.addAction(TIMER_ACTION);
        eventFilter.addAction(SCREEN_ON_ACTION);
        eventFilter.addAction(SCREEN_OFF_ACTION);
        eventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().matches(SCREEN_ON_ACTION)){
                    Log.d(DEBUG_TAG, "receive screen on");
                }
                else if(intent.getAction().matches(SCREEN_OFF_ACTION)){
                    Log.d(DEBUG_TAG, "receive screen off");
                    screenOffAction();
                }
                else if(intent.getAction().matches(TIMER_ACTION)){
                    Log.d(DEBUG_TAG, "receive timer");
                    if(powerManager.isScreenOn()){
                        timerWithScreenOn();
                    }
                    else{
                        timerWithScreenOff();
                    }
                    storeMonitoring();
                }
            }
        };
        registerReceiver(eventReceiver, eventFilter);
    }

    /**
     * startTimeService: start timer service for timed monitoring
     */
    private void startTimerService(){
        timerIntent = new Intent(this, TimerService.class);
        startService(timerIntent);
    }

    /**
     * getTime: get current week, hour, minute and second in int array
     */
    private int[] getTime(){
        Calendar calendar = Calendar.getInstance();
        return new int[]{calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)};
    }

    /**
     * getBatteryLevel: get current battery level
     */
    private int getBatteryLevel(){
        Intent batteryIntent = registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1){
            return 50;
        }
        return (int)(((float)level / (float)scale) * 100.0);
    }

    /**
     * getBatteryStatus: get battery charging status
     * return: 0: not charging; 1: usb; 2: ac; 3:others
     */
    private int getBatteryStatus(){
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int type = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if ( status == -1 || type == -1){
            return -1;
        }
        boolean isCharging = ( status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL);
        boolean usbCharge = ( type == BatteryManager.BATTERY_PLUGGED_USB );
        boolean acCharge = ( type == BatteryManager.BATTERY_PLUGGED_AC );

        // If use usb charging
        if(usbCharge){
            return 1;
        }
        // If use AC charging
        else if(acCharge){
            return 2;
        }
        // If not charging
        if(!isCharging){
            return 0;
        }
        // If use other charging type
        return 3;
    }

    /**
     * getRunningApps: get current running apps with "|" as a seperator
     */
    private String getRunningApps(){
        String result = "";
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = getPackageManager();
        final List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = activityManager.getRunningAppProcesses();
        for (int i = 0 ; i< runningTaskInfos.size() ; i++){
            try {
                ApplicationInfo info = packageManager.getApplicationInfo(runningTaskInfos.get(i).processName, 0);
                int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
                // if it is not user application
                if((mask & info.flags) != 0 ) {
                    continue;
                }
                int importance = runningTaskInfos.get(i).importance;
                if ( importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                        importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE ||
                        importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE){
                    CharSequence c = packageManager.getApplicationLabel(packageManager.getApplicationInfo(
                            runningTaskInfos.get(i).processName, PackageManager.GET_META_DATA ));
                    if( c!=null ){
                        result = result + ';' + c.toString();
                    }
                }
            } catch (PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * getBrightnessValue: read and return brightness value (10~255)
     */
    private int getBrightnessValue(){
        float curBrightnessValue = 0;
        final String BRIGHTNESS_STRING = Settings.System.SCREEN_BRIGHTNESS;
        try{
            curBrightnessValue = Settings.System.getInt(
                    getContentResolver(), BRIGHTNESS_STRING);
        }catch (Settings.SettingNotFoundException e){
            Log.d("monitor_info","cannot get brightness");
            e.printStackTrace();
        }
        return (int)curBrightnessValue;
    }

    /**
     * getScreenOffTime: get current screen off time
     */
    private int getScreenOffTime(){
        int time = 0;
        try{
            time = Settings.System.getInt(getApplicationContext().getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return time;
    }

    /**
     * getSoundType: get type of sound
     */
    private int getSoundType(){
        return audioManager.getRingerMode();
    }

    /**
     * setBrightness: set system brightness
     */
    private void setBrightness(int brightness){
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    /**
     * setScreenOffTime: set current screen off time to a given value
     */
    private void setScreenOffTime(int param) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * setVolume set sound type with given parameter: 0 for ring, 1 for vibrate, 2 for silent
     */
    private void setVolume(int type){
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(type);
    }

    /**
     * setNetwork set wifi, bluetooth and gprs state
     */
    private void setNetwork(int wifi_state, int gprs_state, int bluetooth_state){
        NetworkAdmin networkAdmin = new NetworkAdmin(context);
        BluetoothAdmin bluetoothAdmin = new BluetoothAdmin();
        networkAdmin.toggleWiFi(wifi_state == 1);
        networkAdmin.toggleGPRS(gprs_state == 1);
        bluetoothAdmin.toggleBluetooth(bluetooth_state == 1);
    }

    /**
     * getAllFlow: get all network flow in the last interval
     */
    private long getAllFlow(){
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        long totalTxBytes = TrafficStats.getTotalTxBytes();
        long usedRxBytes = totalRxBytes - RxBytes;
        long usedTxBytes = totalTxBytes - TxBytes;
        RxBytes = totalRxBytes;
        TxBytes = totalTxBytes;
        return (usedRxBytes + usedTxBytes) / KBYTE;
    }

    /**
     * getMobileFlow: get mobile network flow in the last interval
     */
    private long getMobileFlow(){
        long totalMobileRxBytes = TrafficStats.getMobileRxBytes();
        long totalMobileTxBytes = TrafficStats.getMobileTxBytes();
        long usedRxMobileBytes = totalMobileRxBytes - MobileRx;
        long usedTxMobileBytes = totalMobileTxBytes - MobileTx;
        MobileRx = totalMobileRxBytes;
        MobileTx = totalMobileTxBytes;
        return (usedRxMobileBytes + usedTxMobileBytes) / KBYTE;
    }

    /**
     * getUserData: get current data, return with pattern format
     */
    private String[] getUserData(){
        int[] time = getTime();
        int battery_level = getBatteryLevel();
        int battery_state = getBatteryStatus();
        String apps = getRunningApps();
        int brightness = getBrightnessValue();
        int sound = getSoundType();
        int sleep = getScreenOffTime();
        long flow = getAllFlow();
        long mobile = getMobileFlow();
        return new String[]{time[0]+"", time[1]+"", time[2]+"", latitude+"", longitude+"",
            battery_level+"", battery_state+"", apps, brightness+"", sleep+"", sound+"", flow+"", mobile+""};
    }

    /**
     * timerWithScreenOn: just store current data into user data array list
     */
    private void timerWithScreenOn(){
        Log.d(DEBUG_TAG, "entering timer function with screen on");
        // neglect usage within an interval of timers (namely in 1 minute)
        if(flagFirst){
            flagFirst = false;
            return;
        }
        userData.add(getUserData());
    }

    /**
     * timerWithScreenOff: judge the similarity of pattern and current situation
     */
    private void timerWithScreenOff(){
        String[] current = getUserData();
        Log.d(DEBUG_TAG, "entering timer function with screen off");
        String[] similar = calculateSimilarity(current);
        // similiar != null means that there is a pattern fit current situation
        if(similar != null){
            setWithData(similar);
        }
        else{
            setHeuristic(current);
        }
        setHeuristic(current);
    }

    /**
     * screenOffAction: actions behind the screen off event
     */
    private void screenOffAction(){
        Log.d(DEBUG_TAG, "entering screen off function");
        // set flagFirst to true for next user data collection
        flagFirst = true;
        // get the user data for the last time in this interaction period
        userData.add(getUserData());
        // category the user data collected in this period and store into the pattern database
        generateCentriod();
        // clear temporary data
        userData.clear();
    }

    /**
     * setHeuristic: config the system heuristically if there is no reliable pattern for config,
     * or for the first time when system start
     */
    private void setHeuristic(String[] data){
        int wifi_state = 0, gprs_state = 0, tooth_state = 0;
        long all_flow, mobile_flow;
        if(data != null){
            all_flow = Long.parseLong(data[11]);
            mobile_flow = Long.parseLong(data[12]);
        }
        else {
            mobile_flow = MIN_MOBILE_FLOW;
            all_flow = MIN_NETWORK_FLOW;
        }

        heuristicTimes++;
        // set brightness and screen off time with lowest value, and keep sound as usual
        setBrightness(10);
        setScreenOffTime(30000);
        if(networkAdmin.isWifiConnected() && ( (all_flow - mobile_flow) >= MIN_NETWORK_FLOW || audioManager.isMusicActive())) {
            wifi_state = 1;
        }
        if(networkAdmin.isMobileConnected() && (mobile_flow >= MIN_MOBILE_FLOW)) {
            gprs_state = 1;
        }

        setNetwork(wifi_state, gprs_state, tooth_state);
        Log.d(DEBUG_TAG, "config system with heuristic wifi=" + wifi_state + " gprs=" + gprs_state);
    }
    /**
     * setWithData: config the system with given data
     */
    private void setWithData(String[] newData){
        predictionTimes++;
        int wifi_state = 0, gprs_state = 0, tooth_state = 0;
        int brightness = Integer.parseInt(newData[8]);
        int screen = Integer.parseInt(newData[9]);
        int sound = Integer.parseInt(newData[10]);
        int all_flow = Integer.parseInt(newData[11]);
        int mobile_flow = Integer.parseInt(newData[12]);
        setBrightness(brightness);
        setScreenOffTime(screen);
        setVolume(sound);
        if(networkAdmin.isWifiAvailable() && (all_flow - mobile_flow >= MIN_NETWORK_FLOW || audioManager.isMusicActive())){
            wifi_state = 1;
        }
        if(networkAdmin.isMobileAvailable() && (mobile_flow >= MIN_MOBILE_FLOW)){
            gprs_state = 1;
        }
        setNetwork(wifi_state, gprs_state, tooth_state);
        Log.d(DEBUG_TAG, "setWithData with brightness=" + brightness + " screen=" + screen + " sound=" + sound +
                " wifi=" + wifi_state + " gprs=" + gprs_state);
    }

    /**
     * generateCentroid: category the collected user data and find the center point
     */
    private void generateCentriod(){
        //ArrayList<String[]> result = new ArrayList<>();
        //TODO: generate centroids in result
        storeCentroid(userData);
    }

    /**
     * storeCentroid: store the centers in pattern database
     */
    private void storeCentroid(ArrayList<String[]> data){
        for(int i = 0; i < data.size(); i++ ){
            String[] newPattern = data.get(i);
            writeToDatabase(newPattern, PATTERN_TAG);
        }
    }

    /**
     * storeMonitoring: store monitoring data for evaluation
     */
    private void storeMonitoring(){
        int[] time = getTime();
        boolean screen = powerManager.isScreenOn();
        int brightness = getBrightnessValue();
        int sleep = getScreenOffTime();
        int sound = getSoundType();
        boolean wifi = networkAdmin.isWifiAvailable();
        boolean gprs = networkAdmin.isMobileAvailable();
        boolean tooth = (bluetoothAdmin.checkBluetooth() == 1);
        String[] parseData = new String[]{time[0]+"", time[1]+"", time[2]+"", screen+"", brightness+"",
            sleep+"", sound+"", wifi+"", gprs+"", tooth+"", userInteractionTimes+"", heuristicTimes+"",
            predictionTimes+""};
        userInteractionTimes = 0;
        heuristicTimes = 0;
        predictionTimes = 0;
        writeToDatabase(parseData, MONITOR_TAG);
    }

    /**
     * writeToDatabase: write data to database with type pattern or monitor
     */
    private boolean writeToDatabase(String[] data, int type){
        long id = -1;
        try{
            dbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        if(type == PATTERN_TAG){
            id = dbAdapter.insertPattern(data);
        }
        else if(type == MONITOR_TAG){
            id = dbAdapter.insertMonitor(data);
        }
        dbAdapter.close();
        Log.d(DEBUG_TAG, "successfully write " + type + " info to database with id " + id);
        return true;
    }

    /**
     * calculateSimilarity: calculate the similarity of data and patterns in database, find
     * the minimal pattern with similarity > threshold, return null if no such pattern exists.
     */
    private String[] calculateSimilarity(String[] data){

        String[] minimal = null;
        double similar = 1.0;

        try{
            dbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        Cursor c = dbAdapter.getAllPatterns();
        String[] pattern = new String[c.getColumnCount()];

        if(c.moveToFirst()){
            do{
                for(int i = 0; i < c.getColumnCount(); i++){
                    pattern[i] = c.getString(i);
                }
                double result = getSimilarity(data, pattern);
                // <= means the latter data (latest happened) will have more importance
                if(result <= similar){
                    similar = result;
                    minimal = pattern;
                }
            }while (c.moveToNext());
        }
        dbAdapter.close();
        if(similar >= SIMILAR_THRESHOLD){
            return minimal;
        }
        return null;
    }

    /**
     * getSimilarity: get the similarity of two packed data
     */
    private double getSimilarity(String[] params1, String[] params2){

        // get data and repack them to similarity calculation functions
        double lat1 = Double.parseDouble(params1[3]),
                lat2 = Double.parseDouble(params2[3]),
                lon1 = Double.parseDouble(params1[4]),
                lon2 = Double.parseDouble(params2[4]);
        int week1 = Integer.parseInt(params1[0]),
                week2 = Integer.parseInt(params2[0]),
                hour1 = Integer.parseInt(params1[1]),
                hour2 = Integer.parseInt(params2[1]),
                min1 = Integer.parseInt(params1[2]),
                min2 = Integer.parseInt(params2[2]);
        String[] app1 = params1[7].split(";");
        String[] app2 = params2[7].split(";");

        // calculate time, space and application similarity
        double pos = posSimiliarity(lat1, lon1, lat2, lon2);
        double time = timeSimilarity(week1, hour1, min1, week2, hour2, min2);
        double app = appSimiliarity(app1, app2);

        Log.d(DEBUG_TAG, "calculate similarity with pos="+pos+" time="+time+" app="+app);

        // the result is weighted linear function
        return 0.4 * pos + 0.4 * time + 0.2 * app;
    }

    /**
     * posSimilarity: calculate 2 <latitude, longitude> pairs similarity
     * the similarity is 1 if the distance < 100m (coarse prediction)
     * otherwise the similarity is 0.1
     */
    private double posSimiliarity(double lat1, double lon1, double lat2, double lon2){

        lat1 = lat1 * 10000;
        lat2 = lat2 * 10000;
        lon1 = lon1 * 10000;
        lon2 = lon2 * 10000;

        double distance = Math.sqrt(Math.pow(lat1 - lat2, 2.0) + Math.pow(lon1 - lon2, 2.0));
        if(distance < 100){
            return 1.0;
        }
        return 0.1;
    }

    /**
     * timeSimilarity: calculate 2 <week, hour, minute> pairs similarity
     */
    private double timeSimilarity(int week1, int hour1, int minute1, int week2, int hour2, int minute2){

        // the similarity
        double similar = 0;
        // miute of a day
        int MINUTE_OF_A_DAY = 60 * 24;
        //calcullate the time interval between 2 time
        int time_interval = 0;
        if(hour1 > hour2){
            time_interval = ( hour1 - hour2 ) * 60 + ( minute1 - minute2 );
        }
        else if( hour1 == hour2 ){
            time_interval =  ( minute1 - minute2 ) > 0 ? ( minute1 - minute2 ) : ( minute2 - minute1 );
        }
        else{
            time_interval = ( hour2 - hour1 ) * 60 + ( minute1 - minute2 );
        }
        time_interval = Math.min(time_interval, MINUTE_OF_A_DAY - time_interval);

        // with in an hour
        if(time_interval <= 60){
            similar = 0.9;
        }
        // between an hour and 5 hour
        else if(time_interval >60 && time_interval < 300){
            similar = 0 - (time_interval / 300) + 1.1;
        }
        else{
            similar = 0.1;
        }
        if(week1 == week2){
            similar += 0.1;
        }
        return similar;
    }

    /**
     * appSimilarity: calculate 2 lists similarity
     */
    private double appSimiliarity(String[] list1, String[] list2) {

        int same = 0, different = 0;

        // find the same and different pair of 2 list
        for(int i = 0 ; i < list1.length ; i++){
            for(int j = 0; j < list2.length ; j++){
                if( list1[i].length() != 0 && list2[j].length() != 0){
                    if(list1[i].equals(list2[j])){
                        same++;
                    }
                    else{
                        different++;
                    }
                }
            }
        }
        // no available data in lists
        if(different == 0 && same == 0){
            return 1.0;
        }
        else{
            return same / Math.sqrt(different + same);
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy(){
        Log.d(DEBUG_TAG, "stop config service");
        super.onDestroy();
        unregisterReceiver(locationReceiver);
        unregisterReceiver(eventReceiver);
        stopService(timerIntent);
    }
}
