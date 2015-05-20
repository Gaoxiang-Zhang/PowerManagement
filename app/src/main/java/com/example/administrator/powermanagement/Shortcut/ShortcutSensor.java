package com.example.administrator.powermanagement.Shortcut;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 15/5/20.
 */
public class ShortcutSensor extends ActionBarActivity {

    // Toolbar
    Toolbar mToolbar = null;

    //
    ListView listView = null;
    SensorListAdapter adapter = null;

    SensorManager sensorManager = null;

    ArrayList<String[]> sensorInfo = null;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // initialize the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(getResources().getStringArray(R.array.shortcuts)[14]);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        getSensorList();

        listView = (ListView)findViewById(R.id.sensor_list);
        adapter = new SensorListAdapter(this,sensorInfo);
        listView.setAdapter(adapter);
    }

    private void getSensorList(){
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorInfo = new ArrayList<>();

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for(Sensor item : sensors){
            String[] stringType = getResources().getStringArray(R.array.sensors_list);
            String[] sensor;
            if(item.getType() > stringType.length){
                sensor = new String[]{item.getName(), getString(R.string.unknown)};
            }
            else{
                sensor = new String[]{item.getName(),stringType[item.getType()]};
            }
            sensorInfo.add(sensor);
        }
    }

    /**
     * AppListAdapter: adapter for listview
     */
    public class SensorListAdapter extends BaseAdapter {

        Context context;
        ArrayList<String[]> list_text;
        ArrayList<Drawable> list_icons;

        public SensorListAdapter(Context arg0, ArrayList<String[]> arg1){
            this.context = arg0;
            this.list_text = arg1;
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
                view = inflater.inflate(R.layout.layout_text_up_down,null);
                TextView textView = (TextView)view.findViewById(R.id.updown_title);
                textView.setText(list_text.get(position)[0]);
                textView = (TextView)view.findViewById(R.id.updown_value);
                textView.setText(list_text.get(position)[1]);
            }
            else{
                TextView textView = (TextView)convertView.findViewById(R.id.updown_title);
                textView.setText(list_text.get(position)[0]);
                textView = (TextView)convertView.findViewById(R.id.updown_value);
                textView.setText(list_text.get(position)[1]);
                view = convertView;
            }
            return view;
        }
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

}
