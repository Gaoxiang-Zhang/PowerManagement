package com.example.administrator.powermanagement.Shortcut;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.powermanagement.R;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Application Activity: show running apps info
 */
public class ApplicationActivity extends ActionBarActivity {

    // item_names and item_icons contains the names and icons of the application
    ArrayList<String> item_names = null;
    ArrayList<Drawable> item_icons = null;
    ArrayList<String> item_values = null;

    // base list view and corresponding adapter
    ListView listView = null;
    AppListAdapter appListAdapter = null;

    // activity manager and package manager to get the information
    ActivityManager activityManager = null;
    PackageManager packageManager = null;

    // Toolbar
    Toolbar mToolbar = null;

    // the id of refresh button on action bar
    static final int REFRESH_ID = 0;

    // Floating button/menu: supported by clans, controls the quick toggle button
    FloatingActionMenu menu = null;
    FloatingActionButton button_all = null;
    FloatingActionButton button_user = null;
    FloatingActionButton button_system = null;
    int saved_state = 0;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        // initialize the array list
        item_names = new ArrayList<>();
        item_icons = new ArrayList<>();
        item_values = new ArrayList<>();

        // initialize the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(R.string.currentApps);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        // initialize managers
        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = getPackageManager();

        // get application list, initially retrieve all application (param = 0)
        generateList(saved_state);

        // initialize the listview
        listView = (ListView)findViewById(R.id.app_list);
        appListAdapter = new AppListAdapter(this,item_names,item_icons);
        listView.setAdapter(appListAdapter);

        initialFloatButton();
    }

    /**
     * initialFloatButon
     */
    private void initialFloatButton(){
        menu = (FloatingActionMenu)findViewById(R.id.menu_button);
        menu.setMenuButtonColorNormalResId(R.color.primary);
        menu.setMenuButtonColorPressedResId(R.color.primary_dark);
        button_all = (FloatingActionButton)findViewById(R.id.menu_all);
        button_user = (FloatingActionButton)findViewById(R.id.menu_user);
        button_system = (FloatingActionButton)findViewById(R.id.menu_system);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshAsyncTask task = new RefreshAsyncTask();
                switch (v.getId()){
                    case R.id.menu_all:
                        saved_state = 0;
                        menu.close(true);
                        break;
                    case R.id.menu_user:
                        saved_state = 1;
                        menu.close(true);
                        mToolbar.setTitle(R.string.user_apps);
                        break;
                    case R.id.menu_system:
                        saved_state = 2;
                        menu.close(true);
                        mToolbar.setTitle(R.string.system_apps);
                        break;
                }
                task.execute(saved_state);
            }
        };
        button_all.setOnClickListener(listener);
        button_user.setOnClickListener(listener);
        button_system.setOnClickListener(listener);
    }

    /**
     * generateList: generate the application list of current running
     */
    private void generateList(int type){
        final List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = activityManager.getRunningAppProcesses();
        // get basic info (name, pid and icon) of running apps
        for (int i = 0 ; i< runningTaskInfos.size() ; i++){
            try {
                ApplicationInfo info = packageManager.getApplicationInfo(runningTaskInfos.get(i).processName, 0);
                int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
                // type = 1 means getting user application
                if(type == 1){
                    // if it is not user application
                    if((mask & info.flags) != 0 ) {
                        continue;
                    }
                }
                // type = 1 means getting system application
                else if(type == 2){
                    // if it is user application
                    if((mask & info.flags) == 0 ) {
                        continue;
                    }
                }
                CharSequence c = info.loadLabel(packageManager);
                Drawable icon = info.loadIcon(packageManager);
                item_names.add(c.toString());
                item_icons.add(icon);
            }catch(PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }
        }
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
                task.execute(saved_state);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AppListAdapter: adapter for listview
     */
    public class AppListAdapter extends BaseAdapter {

        Context context;
        ArrayList<String> list_text;
        ArrayList<Drawable> list_icons;

        public AppListAdapter(Context arg0, ArrayList<String> arg1, ArrayList<Drawable> arg2){
            this.context = arg0;
            this.list_text = arg1;
            this.list_icons = arg2;
        }

        @Override
        public int getCount(){
            return list_text.size();
        }

        @Override
        public Object getItem(int position){
            return null;
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            View view;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                view = inflater.inflate(R.layout.listview_apps,null);
                TextView textView = (TextView)view.findViewById(R.id.list_text);
                ImageView imageView = (ImageView)view.findViewById(R.id.list_images);
                textView.setText(list_text.get(position));
                imageView.setImageDrawable(list_icons.get(position));
            }
            else{
                TextView textView = (TextView)convertView.findViewById(R.id.list_text);
                ImageView imageView = (ImageView)convertView.findViewById(R.id.list_images);
                textView.setText(list_text.get(position));
                imageView.setImageDrawable(list_icons.get(position));
                view = convertView;
            }
            return view;
        }
    }

    /**
     * refreshAsyncTask: the async task that controls the list view change
     */
    public class RefreshAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params){
            return params[0];
        }
        @Override
        protected void onPostExecute(Integer param){
            item_names.clear();
            item_icons.clear();
            generateList(param);
            appListAdapter.notifyDataSetChanged();
        }
    }

}
