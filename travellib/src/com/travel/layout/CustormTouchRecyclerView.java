package com.travel.layout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2018/3/30.
 */

public class CustormTouchRecyclerView extends RecyclerView {
    public CustormTouchRecyclerView(Context context) {
        this(context, null);
    }
    public CustormTouchRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CustormTouchRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
}
