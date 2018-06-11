package com.travel.utils;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.travel.lib.utils.MLog;

/**
 * 有千叶窗效果的动画
 * Created by ldkxingzhe on 2016/6/22.
 */
public class BlindAnimationRunnable implements Runnable, Animation.AnimationListener {
    @SuppressWarnings("unused")
    private static final String TAG = "BlindAnimationRunnable";

    private View mAnimationView;
    private ViewGroup mParentView;
    private LinearLayout mAnimationLayout;
    private Interpolator mInterpolator;

    private static int mCounts = 10;

    public BlindAnimationRunnable(View animationView){
        mAnimationView = animationView;
        mInterpolator = new AccelerateDecelerateInterpolator();
        mParentView = (ViewGroup) mAnimationView.getParent();
    }

    @Override
    public void run() {
        mAnimationLayout = new LinearLayout(mAnimationView.getContext());
        mAnimationLayout.setLayoutParams(mAnimationView.getLayoutParams());
        mAnimationLayout.setClipChildren(false);
        mAnimationLayout.setOrientation(LinearLayout.VERTICAL);
        mAnimationView.setDrawingCacheEnabled(true);
        Bitmap animationViewBitmap = mAnimationView.getDrawingCache();
        int width = mAnimationView.getWidth();
        int height = mAnimationView.getHeight();

        float cellHeight = height * 1.0f / mCounts;
        for(int i = 0; i < mCounts; i++){
            ImageView imageView = new ImageView(mAnimationView.getContext());

            imageView.setImageBitmap(
                    Bitmap.createBitmap(animationViewBitmap,
                            0, (int)(cellHeight * i), width, (int)cellHeight));
            RotateXAnimation rotateX = new RotateXAnimation(width / 2, cellHeight / 2);
            rotateX.setInterpolator(mInterpolator);
            imageView.startAnimation(rotateX);
            if(i == mCounts - 1){
                rotateX.setAnimationListener(this);
            }
            mAnimationLayout.addView(imageView);
        }
        mParentView.removeView(mAnimationView);
        mParentView.addView(mAnimationLayout);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        MLog.v(TAG, "onAnimationEnd");
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        MLog.v(TAG, "onAnimationEnd");
        mParentView.removeView(mAnimationLayout);
        mParentView.addView(mAnimationView);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private static class RotateXAnimation extends Animation {
        private Camera mCamera;
        private float mCenterX, mCenterY;

        public RotateXAnimation(float centerX, float centerY){
            mCamera = new Camera();
            setFillAfter(false);
            setDuration(2000);
            mCenterX = centerX;
            mCenterY = centerY;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            Matrix matrix = t.getMatrix();
            mCamera.save();
            mCamera.rotateX(360 * interpolatedTime);
            mCamera.getMatrix(matrix);
            matrix.preTranslate(-mCenterX, -mCenterY);
            matrix.postTranslate(mCenterX, mCenterY);
            matrix.postScale(1.0f, 0.9f, mCenterX, mCenterY);
            mCamera.restore();
        }
    }
}
