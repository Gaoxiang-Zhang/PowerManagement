package com.example.administrator.powermanagement.Custom;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.administrator.powermanagement.BluetoothAdmin;
import com.example.administrator.powermanagement.MainActivity;
import com.example.administrator.powermanagement.NetworkAdmin;
import com.example.administrator.powermanagement.R;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * CustomService: main service for custom mode
 */
public class CustomService extends Service {

    // context of service
    Context context;

    // debug tag
    String TAG = "custom_info";

    // all contents sync with CustomActivity, and have all contents of customs
    ArrayList<String[]> all_contents;
    // task_ids stores the task id, and used for start_intents and stop_intents
    // start and stop intents are for unregister on pending list
    ArrayList<Integer> task_ids;
    ArrayList<Intent> start_intents;
    ArrayList<Intent> stop_intents;

    // ordinary sync with CustomActivity, and stores the ordinary settings
    String[] ordinary;

    // static custom database
    CustomDatabase database;

    // data receiver and filter, receive data change broadcast from custom activity
    BroadcastReceiver dataReceiver = null;
    IntentFilter dataFilter = null;

    // task receiver and filter, receive alarm manager send (1) notification (2) notification press
    // event (confirm, cancel and detail)
    BroadcastReceiver taskReceiver = null;
    IntentFilter taskFilter = null;

    // timeout receiver and filter, receive timeout of notification; timeoutIntent is for sending
    // broadcast to this receiver
    BroadcastReceiver timeoutReceiver = null;
    IntentFilter timeoutFilter = null;
    Intent timeoutIntent = null;

    // alarm manager manages the timed task
    AlarmManager alarmManager = null;

    // notification manager manages the notification
    NotificationManager notificationManager = null;

    // shared preference to initially load ordinary info
    SharedPreferences ordinaryPrefs = null;

    // current scheduled task id (>0 for task, =0 for idle state)
    int currentId = (1<<31);

    /**
     * onCreate
     */
    @Override
    public void onCreate(){

        context = this;
        Log.d(TAG,"custom service start");

        // initialize list
        task_ids = new ArrayList<>();
        all_contents = new ArrayList<>();
        start_intents = new ArrayList<>();
        stop_intents = new ArrayList<>();
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // get ordinary from shared preference
        getSharedOrdinary();

        // get database
        database = CustomDatabase.getInstance(this);

        // Firstly, get task from database
        getAllDataFromDatabase();

        // set receiver for notifying data change
        dataFilter = new IntentFilter();
        dataFilter.addAction("com.example.administrator.powermanagement.Custom.data");
        dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                switch (type){
                    // add new data
                    case "add":
                        String[] data = intent.getStringArrayExtra("content");
                        addContent(data);
                        break;
                    // delete data with given position at arrayList ( not id )
                    case "delete":
                        int pos = intent.getIntExtra("pos",-1);
                        deleteContent(pos);
                        break;
                    // modify data with content and given position
                    case "modify":
                        String[] value = intent.getStringArrayExtra("content");
                        int loc = intent.getIntExtra("pos", -1);
                        modifyContent(loc, value);
                        break;
                    // modify ordinary data
                    case "ordinary":
                        ordinary = intent.getStringArrayExtra("content");
                        taskNotice(ordinary);
                        break;
                }
            }
        };
        registerReceiver(dataReceiver, dataFilter);

        // receiver for receiving time task
        taskFilter = new IntentFilter();
        taskFilter.addAction("com.example.administrator.powermanagement.Custom.task");
        taskReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra("type", -1);
                String[] info = intent.getStringArrayExtra("data");
                int id = Integer.parseInt(info[0]);
                // receive the task from alarm manager
                if(type == -1) {
                    Log.d(TAG, "receive timer task with id" + info[0]);
                    taskNotice(info);
                }
                //manually confirm task
                else if (type == 1){
                    Log.d(TAG, "receive confirm with id" + info[0]);
                    // cancel timeout task
                    alarmManager.cancel(PendingIntent.getBroadcast(context,
                            id, timeoutIntent, PendingIntent.FLAG_ONE_SHOT));
                    // remove notification
                    stopNotification(Integer.parseInt(info[0]));
                    autoConfigSystem(info);
                }
                //manually cancel task
                else if (type == 2){
                    Log.d(TAG, "receive cancel with id" + info[0]);
                    // cancel timeout task
                    alarmManager.cancel(PendingIntent.getBroadcast(context,
                            id, timeoutIntent, PendingIntent.FLAG_ONE_SHOT));
                    // remove notification
                    stopNotification(Integer.parseInt(info[0]));
                }
            }
        };
        registerReceiver(taskReceiver, taskFilter);

        // receiver for notification timeout
        timeoutFilter = new IntentFilter();
        timeoutFilter.addAction("com.example.administrator.powermanagement.Custom.timeout");
        timeoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String[] data = intent.getStringArrayExtra("data");
                Log.d(TAG,"receive timeout with id" + data[0]);
                stopNotification(Integer.parseInt(data[0]));
                autoConfigSystem(data);
            }
        };
        registerReceiver(timeoutReceiver,timeoutFilter);
    }

    /**
     * getSharedOrdinary: get ordinary from shared preference
     */
    private void getSharedOrdinary(){

        String KEY_BRIGHTNESS = "brightness";
        String KEY_SOUND = "volume";
        String KEY_WIFI = "wifi";
        String KEY_GPRS = "gprs";
        String KEY_TOOTH = "tooth";

        // get shared preference
        ordinaryPrefs = getSharedPreferences(MainActivity.PREF_NAME, 0);
        int brightness = ordinaryPrefs.getInt(KEY_BRIGHTNESS, 10);
        int sound = ordinaryPrefs.getInt(KEY_SOUND, 1);
        int wifi = ordinaryPrefs.getInt(KEY_WIFI, 1);
        int gprs = ordinaryPrefs.getInt(KEY_GPRS, 1);
        int tooth = ordinaryPrefs.getInt(KEY_TOOTH, 0);

        ordinary = new String[]{-1+"", getString(R.string.ordinary_mode), "", "", brightness+"", sound+"",
                wifi+"", gprs+"", tooth+"", "0"};
    }

    /**
     * getAllDataFromDatabase: get custom data from database
     */
    private void getAllDataFromDatabase() {

        // get data from database and save them into arrayList all_contents,
        // if the toggle is true, append it to arrayList task ids
        try {
            database.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Cursor c = database.getAllCustoms();
        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "add a new item with id " + c.getString(0));
                all_contents.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3),
                        c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9)});
                if (c.getString(9).equals("1")) {
                    addTask(Integer.parseInt(c.getString(0)));
                }
            } while (c.moveToNext());
        }
        database.close();
    }

    /**
     * addContent: add content to all content
     */
    private void addContent(String[] param){
        // add content to all contents
        Log.d(TAG, "add a new item with id " + param[0]);
        all_contents.add(param);
        int id = Integer.parseInt(param[0]);
        int toggle = Integer.parseInt(param[param.length - 1]);
        // if toggle == 1 (need to activate) and haven't being activated
        if(toggle == 1 && !isInTask(id)){
            addTask(id);
        }
        // if toggle == 0
        if(toggle == 0 && isInTask(id)){
            removeTask(id);
        }
    }

    /**
     * deleteContent: delete content with given position (not id)
     */
    private void deleteContent(int pos){
        if(pos == -1){
            return;
        }
        // get id of the task
        int id = Integer.parseInt(all_contents.get(pos)[0]);
        Log.d(TAG, "delete a new item with position " + pos + " and id " + id);
        all_contents.remove(pos);
        // if the content is in task
        if(isInTask(id)){
            removeTask(id);
        }
    }

    /**
     * modifyContent: modify content
     */
    private void modifyContent(int pos, String[] param){
        Log.d(TAG, "modify a new item with id " + param[0]);
        all_contents.set(pos, param);
        // get id and toggle of new content
        int id = Integer.parseInt(param[0]);
        int toggle = Integer.parseInt(param[param.length-1]);
        // switch off to on
        if(toggle == 1 && !isInTask(id)){
            addTask(id);
        }
        // switch on to off
        if(toggle == 0 && isInTask(id)){
            removeTask(id);
        }
        // reset time
        if(toggle == 1 && isInTask(id)){
            removeTask(id);
            addTask(id);
        }
    }

    /**
     * addTask: add task to pending intent and schedule on alram manager
     */
    private void addTask(int id){
        // if the id exists
        for(int i = 0 ; i < task_ids.size(); i++){
            if(task_ids.get(i) == id){
                return;
            }
        }
        Log.d(TAG, "add task with id " + id);
        // add the task to list
        task_ids.add(id);
        for(int i=0 ; i < all_contents.size(); i++){
            // find the content of task
            if(Integer.parseInt(all_contents.get(i)[0]) == id){

                // set the start target time calendar
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTimeInMillis(System.currentTimeMillis());
                startCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(all_contents.get(i)[2].substring(0, 2)));
                startCalendar.set(Calendar.MINUTE, Integer.parseInt(all_contents.get(i)[2].substring(3, 5)));

                // set the stop target time calendar
                Calendar stopCalendar = Calendar.getInstance();
                stopCalendar.setTimeInMillis(System.currentTimeMillis());
                stopCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(all_contents.get(i)[3].substring(0, 2)));
                stopCalendar.set(Calendar.MINUTE, Integer.parseInt(all_contents.get(i)[3].substring(3, 5)));

                // set the start broadcast intent
                Intent startIntent = new Intent();
                startIntent.putExtra("data", all_contents.get(i));
                startIntent.setAction("com.example.administrator.powermanagement.Custom.task");
                start_intents.add(startIntent);

                // set the end broadcast intent
                Intent stopIntent = new Intent();
                // this is for special condition
                String[] data = new String[ordinary.length];
                System.arraycopy(ordinary,0,data,0,ordinary.length);
                data[0] = -id + "";
                stopIntent.putExtra("data",data);
                stopIntent.setAction("com.example.administrator.powermanagement.Custom.task");
                stop_intents.add(stopIntent);

                // set the start alarm manager
                PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, id, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(startCalendar.getTimeInMillis() <= System.currentTimeMillis()){
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startCalendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY,
                            AlarmManager.INTERVAL_DAY, startPendingIntent);
                }
                else{
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startCalendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, startPendingIntent);
                }

                // set the stop alarm manager
                PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0-id, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(stopCalendar.getTimeInMillis() <= System.currentTimeMillis()){
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, stopCalendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY,
                            AlarmManager.INTERVAL_DAY, stopPendingIntent);
                }
                else{
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, stopCalendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, stopPendingIntent);
                }
            }
        }
    }

    /**
     * removeTask: remove task with given id
     */
    private void removeTask(int id){
        Log.d(TAG, "remove task with id " + id);

        Intent startIntent = null;
        Intent stopIntent = null;

        // remove task id from pending list and get startIntent and stopIntent
        for(int i = 0; i < task_ids.size(); i++){
            if(task_ids.get(i) == id){
                task_ids.remove(i);
                startIntent = start_intents.get(i);
                stopIntent = stop_intents.get(i);
                start_intents.remove(i);
                stop_intents.remove(i);
                break;
            }
        }

        for(int i=0 ; i < all_contents.size(); i++) {
            // find the content of tasks and cancel them
            if (Integer.parseInt(all_contents.get(i)[0]) == id) {
                alarmManager.cancel(PendingIntent.getBroadcast(this, id, startIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                alarmManager.cancel(PendingIntent.getBroadcast(this, 0-id, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
    }

    /**
     * isInTask: loop the task id array list to find if the given id is in
     */
    private boolean isInTask(int id){
        for( int i = 0; i < task_ids.size(); i++){
            if(task_ids.get(i) == id){
                return true;
            }
        }
        return false;
    }

    /**
     * taskNotice: after get broadcast from alarm manager
     */
    private void taskNotice(String[] params){

        int id = Integer.parseInt(params[0]);
        // start a new config
        if ( id > 0 ){
            Log.d(TAG,"start a new config");
            currentId = id;
            startNotification(params);

            // set timeout timer for 30 seconds
            long timeout = 1000 * 30;
            timeoutIntent = new Intent();
            timeoutIntent.putExtra("data",params);
            timeoutIntent.setAction("com.example.administrator.powermanagement.Custom.timeout");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, timeoutIntent, PendingIntent.FLAG_ONE_SHOT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, pendingIntent);

        }
        // end id match start, start ordinary
        else if( id == 0 - currentId ){
            Log.d(TAG,"return to ordinary");
            currentId = 0;
            autoConfigSystem(params);
        }
        // else end is between another time interval
        else{
            Log.d(TAG,"neglect ordinary");
        }
    }


    /**
     * startNotification: start a new notification with given id and name
     */
    private void startNotification(String[] params){

        int id = Integer.parseInt(params[0]);
        String name = params[1];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // set basic information for notification builder
        builder.setTicker(getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.example1);

        // set sound for notification
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        // set view for notification
        Notification notification = builder.build();
        RemoteViews contentView = new RemoteViews(getPackageName(),R.layout.layout_notifications);
        contentView.setTextViewText(R.id.notification_content, name + " " + getString(R.string.notification_tip));

        // set confirm button
        Intent confirmIntent = new Intent();
        confirmIntent.setAction("com.example.administrator.powermanagement.Custom.task");
        confirmIntent.putExtra("type",1);
        confirmIntent.putExtra("data",params);
        contentView.setOnClickPendingIntent(R.id.notification_ok, PendingIntent.getBroadcast(this, id, confirmIntent, PendingIntent.FLAG_ONE_SHOT));

        // set cancel button
        Intent cancelIntent = new Intent();
        cancelIntent.setAction("com.example.administrator.powermanagement.Custom.task");
        cancelIntent.putExtra("type", 2);
        cancelIntent.putExtra("data", params);
        contentView.setOnClickPendingIntent(R.id.notification_cancel, PendingIntent.getBroadcast(this, 0 - id, cancelIntent, PendingIntent.FLAG_ONE_SHOT));

        // set detail button
        Intent detailIntent = new Intent(this, CustomTip.class);
        detailIntent.putExtra("data",params);
        contentView.setOnClickPendingIntent(R.id.notification_detail, PendingIntent.getActivity(this, 1000, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        notification.contentView = contentView;
        notificationManager.notify(id, notification);
    }

    /**
     * stopNotificationL stop notification with given id
     */
    private void stopNotification(int id){
        notificationManager.cancel(id);
    }

    /**
     * autoConfigSystem: config system parameters with given packaged data
     */
    private void autoConfigSystem(String[] params){
        int id = Integer.parseInt(params[0]);
        int brightness = Integer.parseInt(params[4]);
        int sound = Integer.parseInt(params[5]);
        int wifi = Integer.parseInt(params[6]);
        int gprs = Integer.parseInt(params[7]);
        int tooth = Integer.parseInt(params[8]);
        if(id < 0 ){
            Log.d(TAG, "config system back to ordinary");
        }
        else{
            Log.d(TAG, "config system to "+id);
        }
        setBrightness(brightness);
        setVolume(sound);
        setNetwork(wifi, gprs, tooth);
    }

    /**
     * setBrightness: set system brightness
     */
    private void setBrightness(int brightness){
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    /**
     * setVolume set sound type with given parameter: 0 for ring, 1 for vibrate, 2 for silent
     */
    private void setVolume(int type){
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        switch(type){
            case 0:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
            case 1:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
            case 2:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
        }
    }

    /**
     * setNetwork set wifi, bluetooth and gprs state
     */
    private void setNetwork(int wifi_state, int gprs_state, int bluetooth_state){
        NetworkAdmin networkAdmin = new NetworkAdmin(context);
        BluetoothAdmin bluetoothAdmin = new BluetoothAdmin();
        int current_wifi = networkAdmin.isWifiConnected() ? 1 : 0;
        int current_gprs = networkAdmin.isMobileConnected() ? 1 : 0;
        int current_tooth = bluetoothAdmin.checkBluetooth();
        if ( wifi_state != current_wifi ){
            networkAdmin.toggleWiFi();
        }
        if ( gprs_state != current_gprs ) {
            networkAdmin.toggleGPRS();
        }
        if ( current_tooth != bluetooth_state ){
            bluetoothAdmin.toggleBluetooth();
        }
    }


    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    /**
     * onDestroy: unregister the receiver
     */
    @Override
    public void onDestroy(){
        Log.d(TAG, "service destroy");
        unregisterReceiver(dataReceiver);
        unregisterReceiver(taskReceiver);
        unregisterReceiver(timeoutReceiver);
        super.onDestroy();
    }
}
