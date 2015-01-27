package com.example.administrator.powermanagement;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GeneralFragment extends Fragment {
    // the image and text shown in general fragment
    Integer[] imageIDs={
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px,
            R.drawable.cloud_128px
    };
    String[] imageText;
    // onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View general = inflater.inflate(R.layout.general_fragment,container,false);
        imageText=getResources().getStringArray(R.array.general_items);
        GridView gridView = (GridView)general.findViewById(R.id.general_grid);
        gridView.setAdapter(new ImageAdapter(this.getActivity(),imageText,imageIDs));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(general.getContext(), "pic" + (position + 1) + "selected", Toast.LENGTH_SHORT).show();
            }
        });
        return general;
    }

    public class ImageAdapter extends BaseAdapter
    {
        private Context context;
        private final String[] imageText;
        private final Integer[] imageId;
        public ImageAdapter(Context c,String[] itemtext,Integer[] itempic){
            context=c;
            this.imageId=itempic;
            this.imageText=itemtext;
        }
        public int getCount(){
            return imageIDs.length;
        }
        public Object getItem(int position){
            return position;
        }
        public long getItemId(int position){
            return position;
        }
        public View getView(int position, View convertView,ViewGroup parent){
            View grid;
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                grid = new View(context);
                grid = inflater.inflate(R.layout.grid_single,null);
                TextView textView = (TextView)grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
                textView.setText(imageText[position]);
                imageView.setImageResource(imageId[position]);
            }else{
                grid = convertView;
            }
            return grid;
        }
    }
}
