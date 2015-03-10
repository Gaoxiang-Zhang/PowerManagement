package com.example.administrator.powermanagement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /*
        Variable definition:
        viewPager, actionbar: realize switched tabs
        mAdapter: object of TabsPagerAdapter, used as logic of tabs
        tab_names: names of tabs
     */
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private String[] tab_names;
    static final int SOUND_DIALOG=0;
    private String[] item_names;
    private Integer[] item_pics;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a defined fragment in TabsPageAdapter.
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mAdapter);

        //When swiping between different sections, select the corresponding tab.
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }
        });
        //set name for each tab
        tab_names = getResources().getStringArray(R.array.tab_names);
        for (String tab_name : tab_names) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
        // Set initial values for sound dialog
        item_names = getResources().getStringArray(R.array.sound_items);
        item_pics = new Integer[item_names.length];
        for(int i = 0; i < item_names.length; i++){
            item_pics[i] = R.drawable.cloud_128px;
        }

    }
    //Set menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.help_info:
                Toast.makeText(this,"help",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this,AppPreferenceActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public Dialog onCreateDialog(int id, Bundle state){
        switch (SOUND_DIALOG){
            case SOUND_DIALOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sound);
                List<Map<String,Object>> listItems = new ArrayList<>();
                for(int i=0; i< item_names.length; i++){
                    Map<String, Object> listItem = new HashMap<String, Object>();
                    listItem.put("sound_images", item_pics[i]);
                    listItem.put("sound_names", item_names[i]);
                    listItems.add(listItem);
                }
                SimpleAdapter simpleAdapter = new SimpleAdapter(
                        this,listItems,R.layout.sound_list,
                        new String[]{ "sound_images", "sound_names" }
                        ,new int[]{R.id.sound_list_pic , R.id.sound_list_text});
                builder.setAdapter(simpleAdapter,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which)
                    {
                        Toast.makeText(getBaseContext(),"Hello"+which,Toast.LENGTH_SHORT);
                    }
                });
                return builder.create();
        }
        return null;
    }
}
