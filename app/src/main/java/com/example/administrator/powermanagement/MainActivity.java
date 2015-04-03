package com.example.administrator.powermanagement;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;


/**
 * MainActivity: Controls the layout of main page which hold toolbar, tab host and fragments
 */
public class MainActivity extends ActionBarActivity implements OnTabChangeListener, OnPageChangeListener {

    MyPageAdapter pageAdapter;
    private ViewPager mViewPager;
    private TabHost mTabHost;
    private Toolbar mToolbar;

    /**
     * onCreate()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initial tool bar and cover original action bar (actually not existed)
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setNavigationIcon(R.drawable.ic_launcher);
        setSupportActionBar(mToolbar);

        //get ViewPager that holds the animation of fragment swipe
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        //initialTabHost
        initialiseTabHost();

        //get fragments of each fragment
        List<Fragment> fragments = getFragments();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(MainActivity.this);
    }

    /**
     * onCreateOptionsMenu: Create the option menu in action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * AddTab: Add a TabSpec to TabHost
     */
    private static void AddTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec) {
        tabSpec.setContent(new MyTabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    /**
     * onTabChanged: Manages the Tab changes, synchronizing it with Pages
     */
    public void onTabChanged(String tag) {
        int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);
    }

    /**
     * onPageScrolled: Manages the Page changes, synchronizing it with Tabs
     */
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        int pos = this.mViewPager.getCurrentItem();
        this.mTabHost.setCurrentTab(pos);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageSelected(int arg0) {
    }

    /**
     * getFragments: add new fragment to fragment List
     */
    private List<Fragment> getFragments(){
        List<Fragment> fList = new ArrayList<>();

        fList.add(new ShortcutFragment());
        fList.add(new OptionsFragment());
        fList.add(new ConsumerFragment());

        return fList;
    }

    /**
     * initialiseTabHost: add tabs to tab host and customize the background of tabs
     */
    private void initialiseTabHost() {

        // Get and setup the tab host
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        // Get names for each tab
        String[] tab_names = getResources().getStringArray(R.array.tab_names);

        //Add tabs
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(tab_names[0]).setIndicator(tab_names[0]));
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(tab_names[1]).setIndicator(tab_names[1]));
        MainActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(tab_names[2]).setIndicator(tab_names[2]));

        // Use custom strips behind tabs
        mTabHost.getTabWidget().setStripEnabled(false);

        // For each tab
        for (int i = 0 ; i <mTabHost.getTabWidget().getChildCount();i++){
            View v = mTabHost.getTabWidget().getChildTabViewAt(i);
            TextView tv = (TextView)mTabHost.getTabWidget().getChildTabViewAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColorStateList(R.color.selector_tabs));
            v.setBackgroundResource(R.drawable.tabwidget);
        }
        // Set this activity as listener (implements OnTabChangeListener)
        mTabHost.setOnTabChangedListener(this);
    }

    /**
     * MyPageAdapter: adapter for pages logic
     */
    class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList;

        public MyPageAdapter(FragmentManager fm, List<Fragment> fragmentList){
            super(fm);
            this.fragmentList = fragmentList;
        }

        // Get each page
        @Override
        public Fragment getItem(int arg0) {
            return (fragmentList == null || fragmentList.size() == 0) ? null : fragmentList.get(arg0);
        }

        // Get page num
        @Override
        public int getCount() {
            return fragmentList == null ? 0 : fragmentList.size();
        }
    }

    /**
     * MyTabFactory: makes the content of a tab when it is selected,
     * used in AddTab method.
     */
    public static class MyTabFactory implements TabHost.TabContentFactory {

        private final Context mContext;

        public MyTabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
}