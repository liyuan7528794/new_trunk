package com.travel.layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import com.travel.lib.R;
import com.travel.lib.utils.MLog;

/**
 * Created by Administrator on 2016/11/21.
 */
public class BaseBellowPopupWindow extends PopupWindow {
    private static final String TAG = "BaseBellowPopupWindow";

    protected View mRootView;
    private Context context;
    private static boolean isInit = false;

    public BaseBellowPopupWindow(Context context){
        this.context = context;
        if(isInit){
            isInit = false;
            return;
        }
        isInit = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    isInit = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void SetContentView(View contentView) {
        super.setContentView(contentView);
        this.mRootView = contentView;
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                MLog.v(TAG, "onGlobalLayout");
                int rootViewHeight = mRootView.getHeight();
                TranslateAnimation animation = new TranslateAnimation(0, 0, rootViewHeight, 0);
                animation.setDuration(200);
                mRootView.startAnimation(animation);
                mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        darkenBackground(0.73f);
    }

    @Override
    public void dismiss() {
        Animation dismissAnimation = new TranslateAnimation(0, 0, 0, mRootView.getHeight());
        dismissAnimation.setDuration(200);
        dismissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BaseBellowPopupWindow.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(dismissAnimation);
        darkenBackground(1f);
    }

    public void show(){
        showAtLocation(((Activity)context).getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * 改变背景颜色
     */
    private void darkenBackground(float bgcolor){
        Window window = ((Activity)context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = bgcolor;

        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(lp);

    }
}