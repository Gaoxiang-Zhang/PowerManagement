package com.example.administrator.powermanagement;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.powermanagement.HelpResponse.SecondaryMenu;

import java.util.ArrayList;

public class UserFragment extends Fragment {

    ListView listView = null;
    UserListAdapter listAdapter = null;

    String[] list_title = null;
    TypedArray list_icon = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        listView = (ListView)view.findViewById(R.id.user_list);
        initListView();


        return view;
    }

    /**
     *
     */
    private void initListView(){
        list_title = getResources().getStringArray(R.array.user_interface);
        list_icon = getResources().obtainTypedArray(R.array.user_icons);
        listAdapter = new UserListAdapter(getActivity(),list_title,list_icon);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        Intent resHelp = new Intent(getActivity(), SecondaryMenu.class);
                        startActivity(resHelp);
                        break;
                    case 3:
                        break;
                }
            }
        });
    }

    /**
     * CustomListAdapter: adapter for list view in this activity
     */
    private class UserListAdapter extends BaseAdapter {

        private String[] title;
        private TypedArray icon;
        private Context context;

        public UserListAdapter(Context arg1, String[] arg2, TypedArray arg3){
            this.context = arg1;
            this.title = arg2;
            this.icon = arg3;
        }
        @Override
        public int getCount(){
            return list_title.length;
        }
        @Override
        public Object getItem(int position){
            return null;
        }
        @Override
        public long getItemId(int position){
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent){
            View view;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                view = inflater.inflate(R.layout.layout_icon_title_value, null);
                ImageView imageView = (ImageView)view.findViewById(R.id.tab_icon);
                imageView.setImageDrawable(icon.getDrawable(position));
                TextView textView = (TextView)view.findViewById(R.id.tab_title);
                textView.setText(title[position]);
                textView = (TextView)view.findViewById(R.id.tab_value);
                textView.setVisibility(View.INVISIBLE);
            }
            else{
                ImageView imageView = (ImageView)convertView.findViewById(R.id.tab_icon);
                imageView.setImageDrawable(icon.getDrawable(position));
                TextView textView = (TextView)convertView.findViewById(R.id.tab_title);
                textView.setText(title[position]);
                textView = (TextView)convertView.findViewById(R.id.tab_value);
                textView.setVisibility(View.INVISIBLE);
                view = convertView;
            }
            return view;
        }
    }
}
