package com.travel.video.tools;

import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.travel.lib.utils.OSUtil;

/**
 * Created by Administrator on 2016/12/20.
 */

public class WindowmoveTouchListener implements View.OnTouchListener {
    private final String TAG = "WindowmoveTouchListener";
    private int lastX;
    private int lastY;
    private View view;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private SmallWindowMoveTouchListener.LeftUpPointChangedListener mListener;
    public WindowmoveTouchListener(View view, WindowManager windowManager) {
        this.view = view;
        this.windowManager = windowManager;
        windowParams = (WindowManager.LayoutParams) view.getLayoutParams();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
            // 获取当前点的xy位置
            int currentX = windowParams.x;
            int currentY = windowParams.y;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx =(int)event.getRawX() - lastX;
                    int dy =(int)event.getRawY() - lastY;
                    lastX = (int)event.getRawX();
                    lastY = (int)event.getRawY();
                    int x = currentX + dx;
                    int y = currentY + dy;
                    windowParams.x = x;
                    windowParams.y = y;
                    v.setLayoutParams(windowParams);
                    windowManager.updateViewLayout(v, windowParams);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
    }
}
