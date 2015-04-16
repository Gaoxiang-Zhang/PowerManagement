package com.example.administrator.powermanagement;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.lzyzsd.circleprogress.ArcProgress;

public class OptionsFragment extends Fragment {

    // ArcProgress: supported by lzyzsd, controls the profit meter of this page
    ArcProgress arcProgress = null;

    // Floating button/menu: supported by clans, controls the quick toggle button
    FloatingActionMenu menu = null;
    FloatingActionButton button_auto = null;
    FloatingActionButton button_manual = null;
    FloatingActionButton button_off = null;

    // saving state = 0: auto 1: manually 2: disabled
    int saving_state = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        final View options = inflater.inflate(R.layout.fragment_main,container,false);

        // get and set the initial value of arc progress
        arcProgress = (ArcProgress)options.findViewById(R.id.main_progress);
        arcProgress.setProgress(10);
        arcProgress.setBottomText(getString(R.string.profitYesterday));

        // set text in this page
        TextView text = (TextView)options.findViewById(R.id.menu_settings).findViewById(R.id.menu_title);
        text.setText(getString(R.string.detailOption));
        text = (TextView)options.findViewById(R.id.menu_custom).findViewById(R.id.menu_title);
        text.setText(getString(R.string.customMode));
        text = (TextView)options.findViewById(R.id.menu_ranking).findViewById(R.id.menu_title);
        text.setText(getString(R.string.ranking));
        text = (TextView)options.findViewById(R.id.menu_battery).findViewById(R.id.menutabs_item);
        text.setText(getString(R.string.battery));
        text = (TextView)options.findViewById(R.id.menu_profit).findViewById(R.id.menutabs_item);
        text.setText(getString(R.string.profitTotal));
        text = (TextView)options.findViewById(R.id.menu_life).findViewById(R.id.menutabs_item);
        text.setText(getString(R.string.usingTime));

        // get the floating button menu and buttons
        menu = (FloatingActionMenu)options.findViewById(R.id.menu_button);
        setMenuColor(saving_state);
        button_auto = (FloatingActionButton)options.findViewById(R.id.menu_auto);
        button_manual = (FloatingActionButton)options.findViewById(R.id.menu_manu);
        button_off = (FloatingActionButton)options.findViewById(R.id.menu_disabled);

        // set click listener to floating buttons
        button_auto.setOnClickListener(clickListener);
        button_manual.setOnClickListener(clickListener);
        button_off.setOnClickListener(clickListener);



        return options;
    }

    /**
     * setMenuColor: set menu color based on different status
     * @param id: 0 for auto, 1 for manually, 2 for disable
     */
    private void setMenuColor(int id){
        switch (id){
            case 0:
                menu.setMenuButtonColorNormalResId(R.color.mode_auto);
                menu.setMenuButtonColorPressedResId(R.color.mode_auto_pressed);
                break;
            case 1:
                menu.setMenuButtonColorNormalResId(R.color.mode_manu);
                menu.setMenuButtonColorPressedResId(R.color.mode_manu_pressed);
                break;
            case 2:
                menu.setMenuButtonColorNormalResId(R.color.mode_disabled);
                menu.setMenuButtonColorPressedResId(R.color.mode_disabled_pressed);
                break;
        }
    }

    // clickListener: listener for fabs
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menu_auto:
                    Toast.makeText(getActivity(),"1",Toast.LENGTH_SHORT).show();
                    saving_state = 0;
                    menu.close(true);
                    setMenuColor(saving_state);
                    break;
                case R.id.menu_manu:
                    Toast.makeText(getActivity(),"2",Toast.LENGTH_SHORT).show();
                    saving_state = 1;
                    menu.close(true);
                    setMenuColor(saving_state);
                    break;
                case R.id.menu_disabled:
                    Toast.makeText(getActivity(),"3",Toast.LENGTH_SHORT).show();
                    saving_state = 2;
                    menu.close(true);
                    setMenuColor(saving_state);
                    break;
            }
        }
    };

}
