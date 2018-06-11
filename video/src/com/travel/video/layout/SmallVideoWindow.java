package com.travel.video.layout;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.travel.app.TravelApp;
import com.travel.lib.utils.MLog;
import com.travel.video.tools.WindowmoveTouchListener;

/**
 * 弹窗辅助类 * * @ClassName SmallVideoWindow * *
 */
public class SmallVideoWindow {
    private static final String LOG_TAG = "SmallVideoWindow";
    private static SmallVideoWindow instance = null;
    private View mView = null;
    private RelativeLayout videoContain = null;
    private WindowManager mWindowManager = null;
    private Context mContext = null;
    public Boolean isShown = false;
    private WindowUtilsListener listener;

    public SmallVideoWindow(Context mContext) {
        this.mContext = mContext;
    }

    public static SmallVideoWindow getInstance(){
        if(instance == null)
            instance = new SmallVideoWindow(TravelApp.appContext);
        return instance;
    }

    public void setZoomListener(WindowUtilsListener listener){
        this.listener = listener;
    }

    public interface WindowUtilsListener{
        public void onClose(View view);
        public void onZoom(View view);
    }

    /**
     * 显示弹出框
     */
    public void showPopupWindow() {
        if (isShown) {
            Log.i(LOG_TAG, "return already show PopupWindow");
            return;
        }
        isShown = true;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setView();
        initVideoViewListener();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        int type = 0;
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //解决Android 7.1.1起不能再用Toast的问题（先解决crash）
            if(Build.VERSION.SDK_INT > 24){
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }else{
                type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 不设置flag的话，home页的划屏会有问题
//        flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
//        flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,弹出的View收不到Back键的事件,
        flags = LayoutParams.FLAG_NOT_FOCUSABLE;

        params.type = type;
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;

        params.width = 300;
        params.height = 400;
        mWindowManager.addView(mView, params);
        Log.i(LOG_TAG, "showPopupWindow");
    }

    private void setView() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.small_video_window, null);
        videoContain = (RelativeLayout) mView.findViewById(R.id.video_contain);
        ImageView close = (ImageView) mView.findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeVideoView();
                listener.onClose(videoView);
                hidePopupWindow();
            }
        });

        // 点击back键可消除
        mView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        hidePopupWindow();
                        return true;
                    default:
                        return false;
                }
            }
        });

        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                    click();
                return true;
            }
        });
    }

    private void click(){
        Log.i(LOG_TAG, "onClick");

    }

    private GestureDetector mGestureDetector;
    private WindowmoveTouchListener mWindowmoveTouchListener;
    private void initVideoViewListener() {
        mView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(LOG_TAG, "onTouch");
                int x = (int) event.getX();
                int y = (int) event.getY();
                Rect rect = new Rect();
                videoContain.getGlobalVisibleRect(rect);
                if (!rect.contains(x, y)) {
//                    SmallVideoWindow.hidePopupWindow();
                    return true;
                }
                if(mWindowmoveTouchListener == null){
                    mWindowmoveTouchListener = new WindowmoveTouchListener(mView, mWindowManager);
                }
                MLog.v(LOG_TAG, "move onTouch");
                mWindowmoveTouchListener.onTouch(v, event);
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    public boolean isShow(){
        return isShown;
    }

    private View videoView = null;
    /**
     * 添加view
     */
    public void addVideoView(View view) {
        videoView = view;
        if(videoContain == null) {
            hidePopupWindow();
            listener.onZoom(videoView);
            return;
        }
        videoContain.addView(view,new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    /**
     * 删除view
     */
    private void removeVideoView(){
        if(videoContain != null && videoContain.getChildCount() > 0){
            videoContain.removeView(videoView);
            listener.onZoom(videoView);
            videoView = null;
        }
    }

    /**
     * 隐藏弹出框
     */
    public void hidePopupWindow() {
        if (isShown && null != mView) {
            Log.i(LOG_TAG, "hidePopupWindow");
            removeVideoView();
            mWindowManager.removeView(mView);
            isShown = false;
            listener = null;
            instance = null;
        }
    }

}