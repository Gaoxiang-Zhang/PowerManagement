package com.example.administrator.powermanagement;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;


public class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int index){
        switch(index) {
            case 0:
                return new GeneralFragment();
            case 1:
                return new OptionsFragment();
            case 2:
                return new ConsumerFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 3;
    }
}
