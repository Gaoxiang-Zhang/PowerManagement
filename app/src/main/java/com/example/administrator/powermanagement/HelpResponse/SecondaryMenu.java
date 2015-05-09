package com.example.administrator.powermanagement.HelpResponse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.powermanagement.R;

/**
 * SecondaryMenu: menu holding response and help entry
 */
public class SecondaryMenu extends ActionBarActivity {

    Context context = null;

    Toolbar mToolbar = null;

    LinearLayout responseLayout = null;
    LinearLayout helpLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_response);

        context = this;

        // initialize the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(getResources().getStringArray(R.array.user_interface)[2]);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        // get the button and set the value and action to them
        responseLayout = (LinearLayout)findViewById(R.id.response);
        helpLayout = (LinearLayout)findViewById(R.id.help);

        TextView textView = (TextView)responseLayout.findViewById(R.id.tab_title);
        textView.setText(R.string.response);
        textView = (TextView)responseLayout.findViewById(R.id.tab_value);
        textView.setVisibility(View.INVISIBLE);
        textView = (TextView)helpLayout.findViewById(R.id.tab_title);
        textView.setText(R.string.help);
        textView = (TextView)helpLayout.findViewById(R.id.tab_value);
        textView.setVisibility(View.INVISIBLE);
        ImageView imageView = (ImageView)responseLayout.findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.tag);
        imageView = (ImageView)helpLayout.findViewById(R.id.tab_icon);
        imageView.setImageResource(R.drawable.tag);

        responseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResponseActivity.class);
                startActivity(intent);
            }
        });
        helpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
