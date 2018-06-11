package com.travel.lib.fragment_interface;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.TextView;

import com.travel.lib.R;

/**
 * Activity和Fragment的通信
 * Created by Administrator on 2017/5/16.
 */

public class ComunicationActivity extends FragmentActivity{
    private Functions functions;
    private TextView activityReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunication);
        activityReceiver = (TextView) findViewById(R.id.activityReceiver);
    }

    public void bindFunction(String tag){
        initFunctions();
        FragmentManager fm = getSupportFragmentManager();
        BlankFragment blankFragment = (BlankFragment) fm.findFragmentByTag(tag);
        blankFragment.setFunctions(functions);
    }

    private void initFunctions() {
        functions = new Functions();
        functions.addFunction(new Functions.FunctionParamAndResult(BlankFragment.FUNC_PR) {
            @Override
            public Object function(Object o) {
                activityReceiver.setText("Fragment说：" + (int)o);
                return (int)o;
            }
        });
    }
}
