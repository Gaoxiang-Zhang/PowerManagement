package com.example.administrator.powermanagement;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 15/4/2.
 */
public class ApplicationActivity extends ActionBarActivity {

    ArrayList<String> item_names = null;
    ArrayList<Drawable> item_icons = null;
    ListView listView = null;
    ActivityManager activityManager = null;
    PackageManager packageManager = null;
    Toolbar mToolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        item_names = new ArrayList<>();
        item_icons = new ArrayList<>();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(R.string.currentApps);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = getPackageManager();
        final List<ActivityManager.RunningAppProcessInfo> runningTaskInfos = activityManager.getRunningAppProcesses();
        for (int i = 0 ; i< runningTaskInfos.size() ; i++){
            try {
                CharSequence c = packageManager.getApplicationLabel(packageManager.getApplicationInfo(
                        runningTaskInfos.get(i).processName, PackageManager.GET_META_DATA
                ));
                Drawable icon = packageManager.getApplicationIcon(packageManager.getApplicationInfo(
                        runningTaskInfos.get(i).processName, PackageManager.GET_META_DATA));
                if (runningTaskInfos.get(i).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    item_names.add(c.toString()+"/");
                    Log.d("Toast", c.toString() + "/");
                }
                else if(runningTaskInfos.get(i).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
                    item_names.add(c.toString()+"\\");
                    Log.d("Toast", c.toString() + "\\");
                }
                else{
                    item_names.add(c.toString());
                    Log.d("Toast", c.toString());
                }
                item_icons.add(icon);
            }catch(PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }
        }
        listView = (ListView)findViewById(R.id.app_list);

        AppListAdapter appListAdapter = new AppListAdapter(this,item_names,item_icons);
        listView.setAdapter(appListAdapter);
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
        }
        return super.onOptionsItemSelected(item);
    }

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
}
