package com.travel.video.tools;

import com.travel.lib.utils.OSUtil;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class SmallWindowMoveTouchListener implements OnTouchListener{
	private int lastX;  
    private int lastY;
    private View view;

    public interface LeftUpPointChangedListener{
        void onLeftUpChanged(int left, int top);
    }
    private LeftUpPointChangedListener mListener;
    public SmallWindowMoveTouchListener(View view) {
		this.view = view;
	}
    public SmallWindowMoveTouchListener(View view, LeftUpPointChangedListener listener){
        this(view);
        mListener = listener;
    }
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action=event.getAction();  
        switch(action){
        case MotionEvent.ACTION_DOWN:  
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            break;
        case MotionEvent.ACTION_MOVE:
            int dx =(int)event.getRawX() - lastX;
            int dy =(int)event.getRawY() - lastY; 
          
            int left = v.getLeft() + dx;
            int top = v.getTop() + dy;
            int right = v.getRight() + dx;
            int bottom = v.getBottom() + dy;
            if(left < 0){
                left = 0;
                right = left + v.getWidth();
            }
            if(right > OSUtil.getScreenWidth()){
                right = OSUtil.getScreenWidth();
                left = right - v.getWidth();  
            }
            if(top < 0){
                top = 0;  
                bottom = top + v.getHeight();  
            }
            if(bottom > OSUtil.getScreenHeight()){
                bottom = OSUtil.getScreenHeight();
                top = bottom - v.getHeight();
            }
            v.layout(left, top, right, bottom);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if(params instanceof RelativeLayout.LayoutParams){
                ((RelativeLayout.LayoutParams)params).setMargins(left, top, 0, 0);
            }else if(params instanceof FrameLayout.LayoutParams){
                ((FrameLayout.LayoutParams)params).setMargins(left, top, 0, 0);
            }
            if(mListener != null){
                mListener.onLeftUpChanged(left, top);
            }
    		view.setLayoutParams(params);
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            break;
        case MotionEvent.ACTION_UP:
            break;
        }
        return false;
    }
	
}