package com.tencent.qcloud.suixinbo.views.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tencent.av.opengl.ui.GLRootView;

/**
 * GLRootView的子类， 提供了OnTouch事件
 * Created by ldkxingzhe on 2016/10/17.
 */
public class GLRootViewWithTouch extends GLRootView {
    @SuppressWarnings("unused")
    private static final String TAG = "GLRootViewWithTouch";

    private OnTouchListener mTouchListener;

    public GLRootViewWithTouch(Context context) {
        super(context);
    }

    public GLRootViewWithTouch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mTouchListener = l;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        mTouchListener.onTouch(this, motionEvent);
        return true;
    }
}
