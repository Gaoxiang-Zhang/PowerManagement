package com.example.administrator.powermanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OptionsFragment extends Fragment {

    TextView profitStatus = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        final View options = inflater.inflate(R.layout.fragment_main,container,false);
        return options;
    }
}
