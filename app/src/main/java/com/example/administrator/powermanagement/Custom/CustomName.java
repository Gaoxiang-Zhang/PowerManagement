package com.example.administrator.powermanagement.Custom;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;

/**
 * Created by Administrator on 15/4/26.
 */
public class CustomName extends DialogFragment{

    String oldName = null;
    EditText editText = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rid of title in dialog
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // inflate view and set initial value
        View view = inflater.inflate(R.layout.fragment_custom_name, container);
        editText = (EditText)view.findViewById(R.id.name);
        oldName = getArguments().getString("name");
        editText.setText(oldName);


        // set confirm and cancel button logic
        TextView text = (TextView)view.findViewById(R.id.confirm);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newText = editText.getText().toString();
                mListener.onNameComplete(newText);
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

    public static interface OnNameCompleteListener {
        public abstract void onNameComplete(String param);
    }

    private OnNameCompleteListener mListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.mListener = (OnNameCompleteListener)activity;
        }catch (final ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
}
