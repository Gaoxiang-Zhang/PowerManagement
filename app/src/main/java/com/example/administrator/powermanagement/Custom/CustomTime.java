package com.example.administrator.powermanagement.Custom;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.administrator.powermanagement.R;

/**
 * CustomTime: setting start and end time custom
 */
public class CustomTime extends DialogFragment{

    // two picker
    TimePicker start_picker = null;
    TimePicker end_picker = null;

    // old data as hh:mm String
    String old_start = null;
    String old_end = null;

    /**
     *
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // get parsed value
        old_start = getArguments().getString("start_time");
        old_end = getArguments().getString("end_time");

        // inflate view and set initial value
        View view = inflater.inflate(R.layout.fragment_custom_time, container);
        start_picker = (TimePicker) view.findViewById(R.id.start_timer);
        end_picker = (TimePicker)view.findViewById(R.id.end_timer);
        start_picker.setIs24HourView(true);
        end_picker.setIs24HourView(true);

        // set current hour and minute to time picker
        start_picker.setCurrentHour(stringTimeToInt(old_start)[0]);
        start_picker.setCurrentMinute(stringTimeToInt(old_start)[1]);
        end_picker.setCurrentHour(stringTimeToInt(old_end)[0]);
        end_picker.setCurrentMinute(stringTimeToInt(old_end)[1]);


        // set confirm and cancel button logic
        TextView text = (TextView)view.findViewById(R.id.confirm);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t1 = intToStringTime(start_picker.getCurrentHour(),start_picker.getCurrentMinute());
                String t2 = intToStringTime(end_picker.getCurrentHour(),end_picker.getCurrentMinute());
                mListener.onTimeComplete(new String[]{t1,t2});
                getDialog().dismiss();
            }
        });
        text = (TextView)view.findViewById(R.id.cancel);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    /**
     * intToStringTime: convert hour and minute to String hh:mm
     */
    private String intToStringTime(int hour, int minute){
        String t1 = (hour > 9) ? hour+"" : "0"+hour;
        String t2 = (minute > 9) ? minute+"" : "0"+minute;
        return t1+":"+t2;
    }

    /**
     * stringTimeToInt: convert hh:mm string to Integer[hour, minute]
     */
    private Integer[] stringTimeToInt(String time){
        Integer[] result = new Integer[2];
        result[0] = Integer.parseInt(time.substring(0,2));
        result[1] = Integer.parseInt(time.substring(3,5));
        return result;
    }

    /**
     * The following function is realized by CustomSettings to get value from dialog
     * OnTimeCompleteListener
     * mListener
     * onAttach
     */

    public static interface OnTimeCompleteListener {
        public abstract void onTimeComplete(String[] param);
    }

    private OnTimeCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnTimeCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
