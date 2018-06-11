package com.travel.communication.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

/**
 * 按压显示进度条的Button
 * Created by ldkxingzhe on 2017/1/16.
 */
public class PressProgressButton extends View {
    @SuppressWarnings("unused")
    private static final String TAG = "PressProgressButton";

    private int mMaxProgress = 100;
    private int mMaxWidth;
    private int mProgressWidth;
    private int mNormalWidth;
    private int mCurrentWidthNormal;
    private int mCurrentWidthBorder;
    private int mCurrentProgress;
    private boolean mIsShowProgress = true;
    private ValueAnimator mCurrentAnimator;
    private ValueAnimator mCurrentAnimatorBorder;

    private Paint mNormalCirclePaint, mBorderCirclePaint;
    private Paint mProgressPaint;

    private float mCenterX, mCenterY;
    private RectF mCuRectF = new RectF();

    public PressProgressButton(Context context) {
        this(context, null);
    }

    public PressProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PressProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mProgressWidth = OSUtil.dp2px(context, 3);
        mNormalCirclePaint = new Paint();
        mBorderCirclePaint = new Paint();
        mProgressPaint = new Paint();
        mNormalCirclePaint.setColor(Color.WHITE);
        mBorderCirclePaint.setColor(ContextCompat.getColor(context,R.color.white_alpha75));
        mProgressPaint.setColor(Color.GREEN);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mNormalCirclePaint.setAntiAlias(true);
        mProgressPaint.setAntiAlias(true);
        mBorderCirclePaint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                startAnimator(mCurrentWidthNormal, OSUtil.dp2px(getContext(), 48));//mMaxWidth - 2 * mProgressWidth
                startAnimatorBorder(mCurrentWidthBorder, mMaxWidth - 2 * mProgressWidth);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startAnimator(mCurrentWidthNormal, mNormalWidth - OSUtil.dp2px(getContext(), 16));
                startAnimatorBorder(mCurrentWidthBorder, mNormalWidth);
                break;
        }
        return true;
    }

    public void setProgress(int progress){
        mCurrentProgress = progress;
        postInvalidate();
    }

    public void setMaxProgress(int maxProgress){
        mMaxProgress = maxProgress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaxWidth = getMeasuredWidth();
        mNormalWidth = OSUtil.dp2px(getContext(), 76);
        mCurrentWidthNormal = OSUtil.dp2px(getContext(), 60);
        mCurrentWidthBorder = mCurrentWidthNormal + OSUtil.dp2px(getContext(), 16);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenterX = (right - left)/2f;
        mCenterY = (bottom - top)/2f;
        float progressMiddle = mProgressWidth/2f;
        mCuRectF.set(
                progressMiddle,
                progressMiddle,
                right - left - progressMiddle,
                bottom - top - progressMiddle);
//        mCurrentWidthNormal = (int) mCenterX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MLog.v(TAG, "onDraw, and mCurrentWidthNormal is %d.", mCurrentWidthNormal);
        float radius = mCurrentWidthNormal / 2f;
        canvas.drawCircle(mCenterX, mCenterY, radius, mNormalCirclePaint);

        canvas.drawCircle(mCenterX, mCenterY, mCurrentWidthBorder / 2f, mBorderCirclePaint);

        float swapAngle = 360 * mCurrentProgress / mMaxProgress;
        canvas.drawArc(mCuRectF, -90, swapAngle, false, mProgressPaint);
    }

    private void startAnimator(float current, float end){
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) mCurrentAnimator.cancel();
        mCurrentAnimator = ValueAnimator.ofFloat(current, end);
        mCurrentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mCurrentWidthNormal = (int) value;
//                invalidate();
                MLog.v(TAG, "onAnimationUpdate and mCurrentWidthNormal is %d.", mCurrentWidthNormal);
            }
        });
        mCurrentAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsShowProgress = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsShowProgress = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCurrentAnimator.setDuration(500);
        mCurrentAnimator.start();
    }

    private void startAnimatorBorder(float current, float end){
        if (mCurrentAnimatorBorder != null && mCurrentAnimatorBorder.isRunning()) mCurrentAnimatorBorder.cancel();
        mCurrentAnimatorBorder = ValueAnimator.ofFloat(current, end);
        mCurrentAnimatorBorder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mCurrentWidthBorder = (int) value;
                invalidate();
                MLog.v(TAG, "onAnimationUpdate and mCurrentWidthNormal is %d.", mCurrentWidthNormal);
            }
        });
        mCurrentAnimatorBorder.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsShowProgress = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsShowProgress = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCurrentAnimatorBorder.setDuration(500);
        mCurrentAnimatorBorder.start();
    }
}
