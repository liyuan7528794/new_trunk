package com.travel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.lib.helper.PullToRefreshHelper;

/**
 * 重写了Touch的ListView
 * 处理自定义滑动事件
 * Created by ldkxingzhe on 2016/7/14.
 */
public class ListViewOverrideTouch extends PullToRefreshListView{
    @SuppressWarnings("unused")
    private static final String TAG = "ListViewOverrideTouch";

    public interface InterceptTouchListener{
        /** 拦截Touch事件,  */
        boolean onTouchEvent(int deltaY, boolean canScrollUp);
        /* 离开手势 */
        boolean onActionUp();
        /** 拦截手势 */
        boolean onInterceptTouchEvent(float deltaY, boolean isFirstPosition);
    }

    private InterceptTouchListener mListener;
    public void setInterceptTouchListener(InterceptTouchListener listener){
        mListener = listener;
    }


    private PullToRefreshHelper mPullToRefreshHelper;
    public ListViewOverrideTouch(Context context) {
        this(context, null);
    }

    public ListViewOverrideTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMode(Mode.PULL_FROM_END);
        mPullToRefreshHelper = new PullToRefreshHelper(this);
        mPullToRefreshHelper.initPullDownToRefreshView(null);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mIsMoving = false;
                mInitY = mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = ev.getRawY() - mInitY;
                boolean canScrollUp = getRefreshableView().canScrollVertically(-2);
                if(mListener != null && mListener.onInterceptTouchEvent(deltaY, !canScrollUp)){
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private float mLastY, mInitY;
    private boolean mIsMoving = false;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if(mListener != null){
            switch (action){
                case MotionEvent.ACTION_MOVE:
                    float currentY = ev.getRawY();
                    float deltaY = currentY - mLastY;
                    if(deltaY > 400){
                        mLastY = currentY;
                        break;
                    }
                    int intDeltaY = (int) deltaY;
                    mLastY = currentY + intDeltaY - deltaY;
                    if(!mListener.onTouchEvent(intDeltaY, canScrollVertically(-2))) {
                        super.onTouchEvent(ev);
                    }else{
                        mIsMoving = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if(!mIsMoving){
                        super.onTouchEvent(ev);
                    }else{
                        mListener.onActionUp();
                    }
                    mIsMoving = false;
                    break;
                case MotionEvent.ACTION_DOWN:
                    mLastY = ev.getRawY();
                    mIsMoving = false;
                default:
                    super.onTouchEvent(ev);
            }
        }else{
            super.onTouchEvent(ev);
        }
        return true;
    }
}
