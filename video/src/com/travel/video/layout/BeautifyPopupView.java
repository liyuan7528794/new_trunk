package com.travel.video.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.MLog;

/**
 * Created by ldkxingzhe on 2016/9/23.
 */
public class BeautifyPopupView extends PopupWindow implements SeekBar.OnSeekBarChangeListener {
    @SuppressWarnings("unused")
    private static final String TAG = "BeautifyPopupView";

    private View mRootView;
    private SeekBar mSeekBarBeautify;
    private SeekBar mSeekBarWhiteBalance;
    private Listener mListener;

    public interface Listener{
        void onProgressChanged(boolean isBeautify, int value);
    }

    public BeautifyPopupView(Context context, Listener listener){
        mListener = listener;
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_beauty_settings, null);
        mSeekBarWhiteBalance = (SeekBar) mRootView.findViewById(R.id.sb_white_balance);
        mSeekBarBeautify = (SeekBar) mRootView.findViewById(R.id.sb_beautify);

        setContentView(mRootView);
        mSeekBarBeautify.setOnSeekBarChangeListener(this);
        mSeekBarWhiteBalance.setOnSeekBarChangeListener(this);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                MLog.v(TAG, "onGlobalLayout");
                int rootViewHeight = mRootView.getHeight();
                TranslateAnimation animation = new TranslateAnimation(0, 0, rootViewHeight, 0);
                animation.setDuration(500);
                mRootView.startAnimation(animation);
                mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void dismiss() {
        Animation dismissAnimation = new TranslateAnimation(0, 0, 0, mRootView.getHeight());
        dismissAnimation.setDuration(500);
        dismissAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BeautifyPopupView.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(dismissAnimation);
    }

    public void show(View parent, int beautyProgress, int whiteBalanceProgress){
        mSeekBarBeautify.setProgress(beautyProgress);
        mSeekBarWhiteBalance.setProgress(whiteBalanceProgress);
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(mListener != null && fromUser){
            mListener.onProgressChanged(seekBar == mSeekBarBeautify, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
