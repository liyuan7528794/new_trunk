package com.travel.localfile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;

/**
 * 类似一种示波器的视图
 * fixme 考虑使用SurfaceView
 * Created by ldkxingzhe on 2016/6/30.
 */
public class VisualizerView extends View{
    @SuppressWarnings("unused")
    private static final String TAG = "VisualizerView";
    private Context context;

    private Paint mLinePaint;

    private int mNumWaves = 20;
    private Path mTmpPath;
    private float mCurrentAmplitude;
    private float mCurrentPhase;

    public VisualizerView(Context context) {
        this(context, null);
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        mLinePaint = new Paint();
        mLinePaint.setColor(R.color.black_3);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(OSUtil.dp2px(context,2));
        mTmpPath = new Path();
    }

    public void updateAmplitude(float amplitude){
        if(amplitude < 0 || amplitude > 1)
            throw new IllegalArgumentException("amplitude is out of range");
        mCurrentAmplitude = amplitude;
        mCurrentPhase += 1.0;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int halfHeight = getHeight() / 2;
        int halfWidth = getWidth() / 2;
        int maxAmplitude = halfHeight;
//        canvas.drawLine(0, halfHeight, getWidth(), halfHeight, mLinePaint);
        for(int i = 0; i < 1; i++){ //for(int i = 0; i < mNumWaves; i++){
            float progress = 1- i * 1.0f / mNumWaves;
            float normedAmplitude = Math.abs(1.5f * progress - 0.5f) * mCurrentAmplitude;
            float multiplier = Math.min(1.0f, progress / 3.0f * 2.0f + 1.0f / 3.0f);
            mLinePaint.setAlpha((int) (255 * progress));
            mTmpPath.reset();
            for(int x = 0; x < getWidth(); x++){
                float scaling = -(float)Math.pow(1.0f / halfWidth * (x - halfWidth), 2.0f) + 1;
                int y = (int) (scaling * maxAmplitude * normedAmplitude
                        * Math.sin(2 * Math.PI * (x * 1.0f / getWidth()) + mCurrentPhase)) + halfHeight;
                if(x == 0){
                    mTmpPath.moveTo(x, y);
                }
                mTmpPath.lineTo(x, y);
            }
            canvas.drawPath(mTmpPath, mLinePaint);
        }
    }
}
