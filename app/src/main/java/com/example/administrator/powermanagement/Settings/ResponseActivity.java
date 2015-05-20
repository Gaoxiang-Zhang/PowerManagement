package com.example.administrator.powermanagement.Settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.powermanagement.R;

/**
 * ResponseActivity: user response interface
 */
public class ResponseActivity extends ActionBarActivity {

    // base context of activity
    Context context = null;

    // toolbar
    Toolbar mToolbar = null;

    // text view and edit text inside the interface
    TextView textFunc = null, textUi = null, textError = null, textOther = null, comfirm = null;
    EditText editContent = null;
    EditText editContact = null;

    // selected color and unselected color for response tags
    int selectedColor, unselectedColor;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        // set color of selected and unselected
        selectedColor = getResources().getColor(R.color.primary_dark);
        unselectedColor = getResources().getColor(R.color.secondary_text);

        context = this;

        // initialize the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text));
        mToolbar.setTitle(getString(R.string.response));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);

        initInterface();

    }

    /**
     * initInterface: initialize the interface by getting view and set onclick listener
     */
    private void initInterface(){
        textFunc = (TextView)findViewById(R.id.response_function);
        textUi = (TextView)findViewById(R.id.response_ui);
        textError = (TextView)findViewById(R.id.response_error);
        textOther = (TextView)findViewById(R.id.response_other);
        editContent = (EditText)findViewById(R.id.response_content);
        editContact = (EditText)findViewById(R.id.response_contact);
        comfirm = (TextView)findViewById(R.id.confirm);

        comfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResponseLetter();
                finish();
            }
        });
        textFunc.setOnClickListener(textListener);
        textError.setOnClickListener(textListener);
        textUi.setOnClickListener(textListener);
        textOther.setOnClickListener(textListener);
    }

    /**
     * sendResponseLetter: send response to developer
     */
    private void sendResponseLetter(){
        String content = editContent.getText().toString();
        String contact = editContact.getText().toString();
        Toast.makeText(this,"Need to be implemented", Toast.LENGTH_SHORT).show();
        //TODO: Implement function for sending information
    }

    // textListener: listener for tags to change color
    private View.OnClickListener textListener = new View.OnClickListener() {
        public void onClick(View v) {
            TextView textView = (TextView)v;
            if(textView.getCurrentTextColor() == selectedColor){
                textView.setTextColor(unselectedColor);
            }
            else{
                textView.setTextColor(selectedColor);
            }
        }
    };

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
