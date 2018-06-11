package com.travel.test;

import android.app.Activity;
import android.os.Bundle;

import com.ctsmedia.hltravel.R;
import com.travel.layout.PageListWidget;

/**
 * Created by Administrator on 2017/4/20.
 */

public class TestActivity extends Activity {
    private PageListWidget pageListWidget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        pageListWidget = (PageListWidget) findViewById(R.id.page);
        TestAdapter adapter = new TestAdapter(this);
        pageListWidget.setAdapter(adapter);
    }
}
