package com.travel.layout;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.lib.utils.OSUtil;

/**
 * Created by wyp on 2017/10/27.
 * 头部图片下拉放大，上滑覆盖
 */

public class HeadZoomRecyclerView extends SwipeRefreshRecyclerView{


    public HeadZoomRecyclerView(Context context) {
        this(context, null);
    }

    public HeadZoomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //    用于记录下拉位置
    private float y = 0f;
    //    zoomView原本的宽高
    private int zoomViewWidth = 0;
    private int zoomViewHeight = 0;

    //    是否正在放大
    private boolean mScaling = false;

    //    放大的view，默认为第一个子view
    private View zoomView;
    private ViewGroup zoomContain;
    private ViewGroup containLayout;

    public void setZoomContain(ViewGroup zoomView) {
        this.zoomContain = zoomView;
        if(zoomView != null && zoomView.getChildCount() > 0){
            this.zoomView = zoomView.getChildAt(0);
        }
    }

    public ViewGroup getContainLayout() {
        return containLayout;
    }

    public void setContainLayout(ViewGroup containLayout) {
        this.containLayout = containLayout;
    }

    //    滑动放大系数，系数越大，滑动时放大程度越大
    private float mScaleRatio = 0.4f;

    public void setmScaleRatio(float mScaleRatio) {
        this.mScaleRatio = mScaleRatio;
    }

    //    最大的放大倍数
    private float mScaleTimes = 2f;

    public void setmScaleTimes(int mScaleTimes) {
        this.mScaleTimes = mScaleTimes;
    }

    //    回弹时间系数，系数越小，回弹越快
    private float mReplyRatio = 0.5f;

    public void setmReplyRatio(float mReplyRatio) {
        this.mReplyRatio = mReplyRatio;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //        不可过度滚动，否则上移后下拉会出现部分空白的情况
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private float x1 = 0f;
    private float y1 = 0f;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (zoomViewWidth <= 0 || zoomViewHeight <= 0) {
            zoomViewWidth = zoomView.getMeasuredWidth();
            zoomViewHeight = zoomView.getMeasuredHeight();
            zoomContain.setMinimumHeight(zoomViewHeight);

        }
        if (zoomView == null || zoomViewWidth <= 0 || zoomViewHeight <= 0) {
            return super.onInterceptTouchEvent(ev);
        }

        boolean isScrollDown = ViewCompat.canScrollVertically(getScrollView(), -1);// 能否向下滚动
        if(isScrollDown){
            // 能向下滚动
            zoomContain.setAlpha(0);
            /*if(containLayout != null && containLayout.getChildCount() == 0){
                if(zoomView != null && zoomView.getParent() != null){
                    ((ViewGroup)zoomView.getParent()).removeView(zoomView);
                }
                containLayout.addView(zoomView);
            }*/
        }else{
            zoomContain.setAlpha(1);
            /*if(zoomContain != null && zoomContain.getChildCount() == 0){
                if(zoomView != null && zoomView.getParent() != null){
                    ((ViewGroup)zoomView.getParent()).removeView(zoomView);
                }
                zoomContain.addView(zoomView);
            }*/
        }
        boolean isUp = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = ev.getX();
                y1 = ev.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(ev.getY()-y1) <=  Math.abs(ev.getX() - x1)){// 如果是横向
                    return super.onInterceptTouchEvent(ev);
                }
                if(ev.getY() - y1 < 0){
                    isUp = true;
                }else {
                    isUp = false;
                }
                x1 = ev.getX();
                y1 = ev.getY();

                if (!mScaling) {
                    if (!isScrollDown) {
                        y = ev.getY();//滑动到顶部时，记录位置
                    } else {
                        break;
                    }
                }
                if ((ev.getY() - y) < 0 || isScrollDown)
                    return false;//若往下滑动
                if(zoomView.getMeasuredWidth() == zoomViewWidth && isUp){
                    return false;
                }
                return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (zoomViewWidth <= 0 || zoomViewHeight <= 0) {
            zoomViewWidth = zoomView.getMeasuredWidth();
            zoomViewHeight = zoomView.getMeasuredHeight();
        }
        if (zoomView == null || zoomViewWidth <= 0 || zoomViewHeight <= 0) {
            return super.onTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = ev.getX();
                y1 = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(ev.getY()-y1) <=  Math.abs(ev.getX() - x1)){// 如果是横向
                    return super.onTouchEvent(ev);
                }
                x1 = ev.getX();
                y1 = ev.getY();

                boolean isScrollDown = ViewCompat.canScrollVertically(getScrollView(), -1);// 能否向下滚动
                if (!mScaling) {
                    if (!isScrollDown) {
                        y = ev.getY();//滑动到顶部时，记录位置
                    } else {
                        break;
                    }
                }
                int distance = (int) ((ev.getY() - y) * mScaleRatio);
                /*if (distance <= 0) {
                    zoomView.setAlpha(0);
                } else {
                    zoomView.setAlpha(1);
                }*/
                if (distance < 0 || isScrollDown)
                    return false;//若往下滑动
                mScaling = true;
                setZoom(distance);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mScaling = false;
                replyView();
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 放大view
     */
    private void setZoom(float s) {
        float scaleTimes = (float) ((zoomViewWidth + s) / (zoomViewWidth * 1.0));
        //        如超过最大放大倍数，直接返回
        if (scaleTimes > mScaleTimes)
            return;

        ViewGroup.LayoutParams layoutParams = zoomView.getLayoutParams();
        layoutParams.width = (int) (zoomViewWidth + s);
        layoutParams.height = (int) (zoomViewHeight * ((zoomViewWidth + s) / zoomViewWidth));
        //        设置控件水平居中
        ((MarginLayoutParams) layoutParams).setMargins(-(layoutParams.width - zoomViewWidth) / 2, 0, 0, 0);
        zoomView.setLayoutParams(layoutParams);
    }

    /**
     * 回弹
     */
    private void replyView() {
        final float distance = zoomView.getMeasuredWidth() - zoomViewWidth;
        if(distance > OSUtil.dp2px(getContext(), 30)){
            onRefreshListener.onRefresh();
        }
        // 设置动画
        ValueAnimator anim = ObjectAnimator.ofFloat(distance, 0.0F).setDuration((long) (distance * mReplyRatio));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setZoom((Float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    private OnRefreshListener onRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onScrollListener) {
        this.onRefreshListener = onScrollListener;
    }

    /**
     * 滑动监听
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
}
