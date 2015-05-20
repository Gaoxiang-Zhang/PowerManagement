package com.example.administrator.powermanagement.Custom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.powermanagement.Admins.DBAdapter;
import com.example.administrator.powermanagement.MainActivity;
import com.example.administrator.powermanagement.R;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * CustomActivity: activity holds the custom listView
 */
public class CustomActivity extends ActionBarActivity {

    // this activity
    Context context = null;
    // toolbar
    Toolbar mToolbar = null;
    // database stores custom settings
    DBAdapter database;
    // custom_values holds the local custom settings
    static ArrayList<String[]> custom_values;
    // ordinary holds the pre-defined ordinary settings
    String[] ordinary = null;
    // main list view and adapter
    ListView listView = null;
    CustomListAdapter listAdapter = null;
    // shared preferences that holds the ordinary settings
    SharedPreferences ordinaryPrefs = null;
    private static String KEY_BRIGHTNESS = "brightness";
    private static String KEY_SOUND = "volume";
    private static String KEY_WIFI = "wifi";
    private static String KEY_GPRS = "gprs";
    private static String KEY_TOOTH = "tooth";

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        context = this;

        // Set Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(R.string.customMode);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // get database
        database = DBAdapter.getInstance(this);

        // initial ordinary usage data
        initialOrdinary();

        // set listView initial data
        initialCustomList();

    }

    /**
     * onCreateOptionsMenu: set add button on action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // add button with "add" icon
        MenuItem menuItem = menu.add(0, 0, 0, "Add new Custom");
        menuItem.setIcon(R.drawable.fab_add);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
            // case back button
            case android.R.id.home:
                finish();
                return true;
            // case add button
            case 0:
                // parse default value to detailed custom settings
                Intent intent = new Intent(context, CustomSettings.class);
                String[] str = new String[]{getString(R.string.defined_mode),"00:00","00:00","10",
                        "1","0","0","0","0"};
                // append this item to list (local, service and database)
                String[] param = appendCustomList(str);
                intent.putExtra("value",param);
                startActivityForResult(intent, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * initialOrdinary: get values from preference and initial the interface
     */
    private void initialOrdinary(){

        // get shared preference
        ordinaryPrefs = getSharedPreferences(MainActivity.PREF_NAME, 0);
        int brightness = ordinaryPrefs.getInt(KEY_BRIGHTNESS, 10);
        int sound = ordinaryPrefs.getInt(KEY_SOUND, 1);
        int wifi = ordinaryPrefs.getInt(KEY_WIFI, 1);
        int gprs = ordinaryPrefs.getInt(KEY_GPRS, 1);
        int tooth = ordinaryPrefs.getInt(KEY_TOOTH, 0);

        // store the preference into ordinary
        ordinary = new String[]{-1+"", getString(R.string.ordinary_mode), "", "", brightness+"", sound+"",
                wifi+"", gprs+"", tooth+"", "0"};

        // initialize the interface
        LinearLayout layout = (LinearLayout)findViewById(R.id.ordinary);
        TextView text = (TextView)layout.findViewById(R.id.custom_title);
        text.setText(getString(R.string.ordinary_mode));
        text = (TextView)layout.findViewById(R.id.custom_value);
        text.setText(getString(R.string.ordinary_explanation));
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomSettings.class);
                intent.putExtra("value",ordinary);
                startActivityForResult(intent,0);
            }
        });
    }

    /**
     * initialCustomList: initialize the custom_values and list view that holds the values
     */
    private void initialCustomList(){
        // set initial array list that holds data
        custom_values = new ArrayList<>();

        // get all data from database
        getAllDataFromDatabase();

        // initial list view
        listView = (ListView)findViewById(R.id.custom_list);
        listAdapter = new CustomListAdapter(this,custom_values);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] str = custom_values.get(position);
                Intent intent = new Intent(context, CustomSettings.class);
                intent.putExtra("value", str);
                startActivityForResult(intent, 0);
            }
        });
    }

    /**
     * getAllDataFromDatabase: get custom data from database
     */
    private void getAllDataFromDatabase(){

        // get data from database and save them into array list
        try{
            database.open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        Cursor c = database.getAllCustoms();
        if(c.moveToFirst()){
            do{
                // add the database data to local custom_values
                custom_values.add(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3),
                        c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9)});
            }while (c.moveToNext());
        }
        database.close();

        // if the database do not have data, insert a default one into it
        if(custom_values.size() == 0){
            appendCustomList(new String[]{getString(R.string.sleep_mode),"23:00","08:00",
                    "10","0","0","0","0","0"});
        }
    }


    /**
     * appendCustomList: insert the params (without id) into database,
     * and return array list inserted params (with id got from database)
     * also update custom_value and service data
     */
    public String[] appendCustomList(String[] params){

        // insert the data into database
        try{
            database.open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        int id = database.insertCustom(params);
        database.close();

        // insert the data into activity list
        String[] str = new String[]{id+"", params[0], params[1], params[2], params[3], params[4],
        params[5], params[6], params[7], params[8]};
        custom_values.add(str);

        // broadcast the data change message to CustomService (If exists)
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.example.administrator.powermanagement.Custom.data");
        broadcastIntent.putExtra("type", "add");
        broadcastIntent.putExtra("content", str);
        sendBroadcast(broadcastIntent);

        // refresh the listview
        RefreshAsyncTask refreshAsyncTask;
        refreshAsyncTask = new RefreshAsyncTask();
        refreshAsyncTask.execute();

        return str;
    }

    /**
     * deleteCustomList: delete the given id data in array list and database
     */
    public void deleteCustomList(int id){

        // find the data with given id
        for(int i = 0; i < custom_values.size(); i++){
            String[] tmp = custom_values.get(i);
            if(Integer.parseInt(tmp[0]) == id){

                // remove the data from activity list
                custom_values.remove(tmp);

                // remove the data from database
                try{
                    database.open();
                }catch (SQLException e){
                    e.printStackTrace();
                }
                database.deleteCustom(id);
                database.close();

                // broadcast the data change message to CustomService (If exists)
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("com.example.administrator.powermanagement.Custom.data");
                broadcastIntent.putExtra("type","delete");
                broadcastIntent.putExtra("pos",i);
                sendBroadcast(broadcastIntent);

                break;
            }
        }
    }

    /**
     * modifyCustomList: modify the given params data in array list and database
     */
    public void modifyCustomList(String[] params){
        // get id for modified item
        int id = Integer.parseInt(params[0]);
        for(int i = 0; i < custom_values.size(); i++){
            String[] tmp = custom_values.get(i);
            if(Integer.parseInt(tmp[0]) == id){

                // update data in activity list
                custom_values.set(i, params);

                // update data in database
                try{
                    database.open();
                }catch (SQLException e){
                    e.printStackTrace();
                }
                database.updateCustom(params);
                database.close();

                // broadcast the data change message to CustomService (If exists)
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("com.example.administrator.powermanagement.Custom.data");
                broadcastIntent.putExtra("type", "modify");
                broadcastIntent.putExtra("pos",i);
                broadcastIntent.putExtra("content", custom_values.get(i));
                sendBroadcast(broadcastIntent);

                break;
            }
        }
    }

    /**
     * modifyOrdinary: modify local ordinary array and store in preference
     */
    private void modifyOrdinary(String[] params){
        // set ordinary with new value
        ordinary = params;

        // store shared preference
        SharedPreferences.Editor editor = ordinaryPrefs.edit();
        editor.putInt(KEY_BRIGHTNESS, Integer.parseInt(params[4]));
        editor.putInt(KEY_SOUND, Integer.parseInt(params[5]));
        editor.putInt(KEY_WIFI, Integer.parseInt(params[6]));
        editor.putInt(KEY_GPRS, Integer.parseInt(params[7]));
        editor.putInt(KEY_TOOTH, Integer.parseInt(params[8]));
        editor.apply();

        // notify Custom Service the change
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.example.administrator.powermanagement.Custom.data");
        broadcastIntent.putExtra("type", "ordinary");
        broadcastIntent.putExtra("content", params);
        sendBroadcast(broadcastIntent);
    }

    /**
     * CustomListAdapter: adapter for list view in this activity
     */
    private class CustomListAdapter extends BaseAdapter{

        private ArrayList<String[]> customContent;
        private Context context;

        public CustomListAdapter(Context ctx, ArrayList<String[]> list){
            this.customContent = list;
            this.context = ctx;
        }
        @Override
        public int getCount(){
            return customContent.size();
        }
        @Override
        public String[] getItem(int position){
            return customContent.get(position);
        }
        @Override
        public long getItemId(int position){
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent){
            View view;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                view = inflater.inflate(R.layout.listview_custom, null);
                String[] str = getItem(position);
                TextView text = (TextView)view.findViewById(R.id.custom_title);
                text.setText(str[1]);
                text = (TextView)view.findViewById(R.id.custom_value);
                text.setText(str[2]+" ~ "+str[3]);
            }
            else{
                String[] str = getItem(position);
                TextView text = (TextView)convertView.findViewById(R.id.custom_title);
                text.setText(str[1]);
                text = (TextView)convertView.findViewById(R.id.custom_value);
                text.setText(str[2]+" ~ "+str[3]);
                view = convertView;
            }
            return view;
        }
    }

    /**
     * refreshAsyncTask: the async task that controls the list view change
     */
    public class RefreshAsyncTask extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params){
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            listAdapter.notifyDataSetChanged();
        }
    }


    /**
     * onActivityResult: receive data from custom settings(startActivityForResult)
     * This function is called before onResume
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // If return null, means this methods is called somewhere
        if (data != null) {
            int flag = data.getIntExtra("delete", -1);
            // flag = -1 means that the modification of data (ordinary or custom)
            if (flag == -1) {
                String[] value = data.getStringArrayExtra("data");
                int id = Integer.parseInt(value[0]);
                // id != -1 means the modification of custom data
                if(id != -1){
                    modifyCustomList(value);
                }
                // id == -1 means the modification of ordinary data
                else{
                    modifyOrdinary(value);
                }
            }
            // flag != -1 means the deletion of data
            else {
                deleteCustomList(flag);
            }
        }
    }

    /**
     * onResume: refresh interface
     */
    @Override
    public void onResume(){
        super.onResume();
        RefreshAsyncTask refreshAsyncTask;
        refreshAsyncTask = new RefreshAsyncTask();
        refreshAsyncTask.execute();
    }
}
