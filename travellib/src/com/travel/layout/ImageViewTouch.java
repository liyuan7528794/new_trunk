package com.travel.layout;

import com.travel.lib.utils.MLog;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


/**
 * Created by ke on 16-5-15.
 */
public class ImageViewTouch extends ImageViewTouchBase{
    @SuppressWarnings("unused")
    private static final String TAG = "ImageViewTouch";

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private boolean mIsScaling = false;
    private static final float mMaxScale = 3;

    private ImageViewTouchListener mListener;
    public interface ImageViewTouchListener{
        void onSingleTap(ImageViewTouch imageViewTouch);
    }

    public void setListener(ImageViewTouchListener listener){
        mListener = listener;
    }

    public ImageViewTouch(Context context) {
        this(context, null);
    }

    public ImageViewTouch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTouch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setBackgroundColor(Color.parseColor("#B8000000"));
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                MLog.v(TAG, "onSingleTapConfirmed");
                if(mListener != null) mListener.onSingleTap(ImageViewTouch.this);
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                MLog.v(TAG, "onDoubleTap");
                int centerX = (int) e.getX();
                int centerY = (int) e.getY();
                float scale = getScale();
                if(scale == mMaxScale){
                    reset();
                }else{
                    zoom(mMaxScale, centerX, centerY);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                MLog.v(TAG, "onScroll, and dx = %f, dy = %f", -distanceX, -distanceY);
                panBy(-distanceX, -distanceY);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                MLog.v(TAG, "onDown");
                return true;
            }

        });

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                MLog.v(TAG, "onScale");
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                float preSpan = detector.getPreviousSpan();
                float currentSpan = detector.getCurrentSpan();
                float scale = currentSpan / preSpan * getScale();
                scale = Math.min(mMaxScale, scale);
                MLog.v(TAG, "onScale, and focusX is %f, focusY is %f, scale is %f", focusX, focusY, scale);
                zoom(scale, focusX, focusY);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                MLog.v(TAG, "onScaleBegin");
                mIsScaling = true;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                MLog.v(TAG, "onScaleEnd");
                mIsScaling = false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            MLog.v(TAG, "onTouchEvent, and isScroll, up action");
            float scale = getScale();
            if(scale < 1){
                reset();
            }else{
                center(true, true);
            }
        }
        mScaleGestureDetector.onTouchEvent(event);
        if(!mIsScaling){
            mGestureDetector.onTouchEvent(event);
        }
        return true;
    }
}
