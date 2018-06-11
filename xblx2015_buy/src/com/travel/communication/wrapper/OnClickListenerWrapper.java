package com.travel.communication.wrapper;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ldkxingzhe on 2016/9/8.
 */
public class OnClickListenerWrapper implements View.OnTouchListener{
    @SuppressWarnings("unused")
    private static final String TAG = "OnClickListenerWrapper";
    private View.OnClickListener mOnClickListener;
    private View mView;
    private float mRawX, mRawY;
    private Listener mListener;
    private GestureDetector mGestureDetector;
    private boolean mIsConsumeAllEvent = true;

    public interface Listener{
        void onClick(View view, float rawX, float rawY);
        void onLongClick(View view, float rawX, float rawY);
    }

    public OnClickListenerWrapper(Listener listener){
        this(listener, true);
    }

    public OnClickListenerWrapper(Listener listener, boolean consumeAllEvent){
        mListener = listener;
        mIsConsumeAllEvent = consumeAllEvent;
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                mListener.onClick(mView, mRawX, mRawY);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                mListener.onLongClick(mView, mRawX, mRawY);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mView = v;
        mRawX = event.getRawX();
        mRawY = event.getRawY();
        mGestureDetector.onTouchEvent(event);
        return mIsConsumeAllEvent;
    }
}
