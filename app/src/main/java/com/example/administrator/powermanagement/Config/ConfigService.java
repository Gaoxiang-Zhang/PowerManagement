package com.example.administrator.powermanagement.Config;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.example.administrator.powermanagement.Admins.BluetoothAdmin;
import com.example.administrator.powermanagement.Admins.DBAdapter;
import com.example.administrator.powermanagement.Admins.LocationAdmin;
import com.example.administrator.powermanagement.Admins.NetworkAdmin;
import com.example.administrator.powermanagement.MainActivity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // network information
    private long RxBytes = 0;
    private long TxBytes = 0;
    private long MobileRx = 0;
    private long MobileTx = 0;
    // the constant to calculate data usage from B to KB
    final static long KBYTE = 1024L;
    // miute of a day
    final int MINUTE_OF_A_DAY = 60 * 24;
    // minimal value to judge if there exists data transform
    final static long MIN_NETWORK_FLOW = 50;
    final static long MIN_MOBILE_FLOW = 50;

    // tag to distinguish pattern or monitor to database
    final static int PATTERN_TAG = 0;
    final static int MONITOR_TAG = 1;

    // threshold that hold the similar of application
    final static double SIMILAR_THRESHOLD = 0.60;


    // monitoring information
    int userInteractionTimes = 0;
    int heuristicTimes = 0;
    int predictionTimes = 0;

    // judge if this the first time for users to use in this screen on period
    boolean flagFirst = true;

    // judge if this the first time for users to use this app
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;
    String KEY_COMPRESS = "KEY_COMPRESS", KEY_HEURISTIC = "KEY_HEURISTIC", KEY_PREDICTION = "KEY_PREDICTION",
    KEY_PATTERNS = "KEY_PATTERNS";


    /**
     * onCreate
     */
    public void onCreate() {
        context = this;
        Log.d(DEBUG_TAG, "start config service");
        // initial the admin
        initAdmin();
        // initial the receiver
        initReceiver();
        // initial the user data used for evaluation
        userData = new ArrayList<>();
        // if the user open the software for the first time, set with heuristic
        // in order not to get incorrect information
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
        if(result.length()!=0) {
            return result.substring(1);
        }else{
            return result;

        }
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
        Log.d(DEBUG_TAG, "get user data with" + apps);
        return new String[]{time[0]+"", time[1]+"", time[2]+"", latitude+"", longitude+"",
            battery_level+"", battery_state+"", apps, brightness+"", sleep+"", sound+"", flow+"", mobile+""};
    }

    /**
     * timerWithScreenOn: just store current data into user data array list
     */
    private void timerWithScreenOn(){
        Log.d(DEBUG_TAG, "entering timer function with screen on "+flagFirst);
        // neglect usage for the first time because of not accurate network flow
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
        // get current data
        String[] current = getUserData();
        Log.d(DEBUG_TAG, "entering timer function with screen off");
        // calculate similarity to existing pattern in database
        String[] similar = calculateSimilarity(current);
        // similiar != null means that there is a pattern fit current situation
        if(similar != null){
            setWithData(similar, current);
        }
        else{
            setHeuristic(current);
        }
    }

    /**
     * screenOffAction: actions behind the screen off event
     */
    private void screenOffAction(){
        Log.d(DEBUG_TAG, "entering screen off function");
        // set flagFirst to true for next user data collection
        flagFirst = true;
        // get the user data for the last time in this interaction period
        // if userData = null, means that this usage is less than 1 minute, so ignore this usage
        if(userData.size() != 0){
            // category the user data collected in this period and store into the pattern database
            generateCentriod();
            // clear temporary data
            userData.clear();
        }
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
        // write the times of heuristic method
        int times = getFromPreference(KEY_HEURISTIC);
        writeToPreference(KEY_HEURISTIC, times + 1);

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
    private void setWithData(String[] newData, String[] nowData){
        // write the times of pattern method
        int times = getFromPreference(KEY_PREDICTION);
        writeToPreference(KEY_PREDICTION, times+1);

        int wifi_state = 0, gprs_state = 0, tooth_state = 0;
        int brightness = Integer.parseInt(newData[8]);
        int screen = Integer.parseInt(newData[9]);
        int sound = Integer.parseInt(newData[10]);
        int all_flow = Integer.parseInt(newData[11]);
        int mobile_flow = Integer.parseInt(newData[12]);
        int now_all = Integer.parseInt(nowData[11]);
        int now_mobile = Integer.parseInt(nowData[12]);
        setBrightness(brightness);
        setScreenOffTime(screen);
        setVolume(sound);
        if(all_flow - mobile_flow >= MIN_NETWORK_FLOW || audioManager.isMusicActive() || now_all - now_mobile >= MIN_NETWORK_FLOW){
            wifi_state = 1;
        }
        if(mobile_flow >= MIN_MOBILE_FLOW || audioManager.isMusicActive() || now_mobile >= MIN_MOBILE_FLOW){
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

        ArrayList<String[]> result = new ArrayList<>();

        // the number of context that has been categorized
        int flag = 0, start = 0, end = 0;

        while(flag < userData.size()){
            // got the last one
            if(end == userData.size() - 1){
                Log.d(DEBUG_TAG,"add the last centroid");
                result.add(userData.get(end));
                break;
            }
            // if start and end+1 is not sililar
            if(posSimilarity(userData.get(start),userData.get(end+1)) > 100 ||
                    timeSimilarity(userData.get(start),userData.get(end+1)) > 30 ||
                    appSimilarity(userData.get(start),userData.get(end+1)) < SIMILAR_THRESHOLD){
                // if the end exists
                if(result.indexOf(userData.get(end))>=0){
                    Log.d(DEBUG_TAG,"the latter centroid is not similar with all formers");
                    start++;
                    end++;
                }
                // if the end not exists
                else {
                    Log.d(DEBUG_TAG,"the "+ end +" centroid is not similar with the start "+start);
                    result.add(userData.get(end));
                    flag++;
                    start = end + 1;
                }
            }
            else{
                Log.d(DEBUG_TAG,"the "+ end +" centroid is similar with the start "+start);
                end++;
                flag++;
            }
        }
        // write the times of deleted patterns
        int times = getFromPreference(KEY_COMPRESS);
        writeToPreference(KEY_COMPRESS, times + userData.size() - result.size());
        storeCentroid(result);
    }

    /**
     * storeCentroid: store the centers in pattern database
     */
    private void storeCentroid(ArrayList<String[]> data){
        for(int i = 0; i < data.size(); i++ ){
            String[] newPattern = data.get(i);
            writeToDatabase(newPattern, PATTERN_TAG);
        }
        // write the times of patterns in database
        int times = getFromPreference(KEY_PATTERNS);
        writeToPreference(KEY_PATTERNS, times + data.size());
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

        // all of the pattern
        ArrayList<String[]> patterns = new ArrayList<>();

        try{
            dbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        Cursor c = dbAdapter.getAllPatterns();

        // get all patterns from list
        if(c.moveToFirst()){
            do{
                String[] pattern = new String[c.getColumnCount()];
                for(int i = 0; i < c.getColumnCount(); i++){
                    pattern[i] = c.getString(i);
                }
                patterns.add(pattern);
            }while (c.moveToNext());
        }
        Log.d(DEBUG_TAG, "get " + patterns.size() + " patterns from database");
        dbAdapter.close();
        return getSimilarity(data, patterns);
    }

    private String[] getSimilarity(String[] data, ArrayList<String[]> patterns){

        String[] selected = null;
        double similarity = 0;
        int battery = 100;

        for(int i = 0 ; i< patterns.size() ; i++){
            String[] pattern  = patterns.get(i);
            if(posSimilarity(data, pattern) > 100 || timeSimilarity(data, pattern) > 30){
                continue;
            }
            int battery1 = Integer.parseInt(pattern[5]), battery2 = Integer.parseInt(data[5]);
            double value = appSimilarity(data, pattern);
            if(value > similarity || value == similarity && Math.abs(battery1 - battery2) <= battery){
                selected = pattern;
                battery = Math.abs(battery1 - battery2);
            }
        }
        if(selected == null){
            Log.d(DEBUG_TAG, "no pattern found");
        }
        else{
            Log.d(DEBUG_TAG,"get the similarity of time "+selected[0]+":"+selected[1]);
        }
        return selected;

    }

    /**
     * posSimilarity: calculate the distance between 2 packed data
     */
    private double posSimilarity(String[] data, String[] pattern){
        // get latitude and longitude
        double lat1 = Double.parseDouble(data[3]),
                lat2 = Double.parseDouble(pattern[3]),
                lon1 = Double.parseDouble(data[4]),
                lon2 = Double.parseDouble(pattern[4]);
        lat1 = lat1 * 10000;
        lat2 = lat2 * 10000;
        lon1 = lon1 * 10000;
        lon2 = lon2 * 10000;
        Log.d(DEBUG_TAG, lat1+"," + lat2 + "," + lon1 + "," + lon2);
        return Math.sqrt(Math.pow(lat1 - lat2, 2.0) + Math.pow(lon1 - lon2, 2.0));
    }

    /**
     * timeSimilarity: filter the time beyond 30min
     */
    private int timeSimilarity(String[] data, String[] pattern){
        int hour1 = Integer.parseInt(pattern[1]), hour2 = Integer.parseInt(data[1]),
                minute1 = Integer.parseInt(pattern[2]), minute2 = Integer.parseInt(data[2]);
        Log.d(DEBUG_TAG, "enter time similarity");
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
        Log.d(DEBUG_TAG, "time is "+time_interval);
        return Math.min(time_interval, MINUTE_OF_A_DAY - time_interval);
    }

    /**
     * appSimilarity: calculate
     */
    private double appSimilarity(String[] data, String[] pattern){

        Set<String> set1 = new HashSet<>(), set2 = new HashSet<>();
        String[] app1 = pattern[7].split(";"), app2 = data[7].split(";");
        for(int i = 0 ; i < app1.length ; i++){
            set1.add(app1[i]);
        }
        for(int i = 0 ; i < app2.length ; i++){
            set2.add(app2[i]);
        }

        Set<String> nSet = new HashSet<>(set1),uSet = new HashSet<>(set1);
        // intersaction
        nSet.retainAll(set2);
        // union
        uSet.addAll(set2);

        if(uSet.size() == 0){
            return 0;
        }
        Log.d(DEBUG_TAG, nSet.size()+"/"+uSet.size());
        return (double)nSet.size() / uSet.size();
    }


    private int getFromPreference(String key){
        return sharedPreferences.getInt(key, 0);
    }

    private void writeToPreference(String key, int new_value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key,new_value);
        editor.apply();
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        Notification notification = new Notification();
        startForeground(0, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(DEBUG_TAG, "stop config service");
        //stopService(timerIntent);
        unregisterReceiver(locationReceiver);
        unregisterReceiver(eventReceiver);
    }
}
