package com.travel.layout;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.travel.lib.R;
import com.travel.lib.TravelApp;

/**
 * Created by Administrator on 2017/6/1.
 */

public class WebVideoPopWindow {
    private static final String LOG_TAG = "SmallVideoWindow";
    private static WebVideoPopWindow instance = null;
    private View mView = null;
    private RelativeLayout videoContain = null;
    private WindowManager mWindowManager = null;
    private Context mContext = null;
    public Boolean isShown = false;
    private WindowUtilsListener listener;

    public WebVideoPopWindow(Context mContext) {
        this.mContext = mContext;
    }

    public static WebVideoPopWindow getInstance(){
        if(instance == null)
            instance = new WebVideoPopWindow(TravelApp.appContext);
        return instance;
    }

    public void setZoomListener(WindowUtilsListener listener){
        this.listener = listener;
    }

    public interface WindowUtilsListener{
        void onClose(View view);
    }

    /**
     * 显示弹出框
     */
    public void showPopupWindow() {
        if (isShown) {
            return;
        }
        isShown = true;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setView();
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
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        params.type = type;
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowManager.addView(mView, params);
        Log.i(LOG_TAG, "showPopupWindow");
    }

    private void setView() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.webvideo_popwindow, null);
        videoContain = (RelativeLayout) mView.findViewById(R.id.video_contain);
        ImageView close = (ImageView) mView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClose(videoView);
            }
        });

        // 点击back键可消除
        mView.setFocusable(true);
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        removeVideoView();
                        listener.onClose(videoView);
                        return true;
                    default:
                        return false;
                }
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
            listener.onClose(videoView);
            return;
        }
        videoContain.addView(view,new RelativeLayout
                .LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    // 返回方法
    public void backClick(){
        if(listener != null)
            listener.onClose(videoView);
    }

    /**
     * 删除view
     */
    private void removeVideoView(){
        if(videoContain != null && videoContain.getChildCount() > 0){
            videoContain.removeView(videoView);
            listener.onClose(videoView);
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
