package com.travel.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.travel.lib.R;

/**
 * Created by Administrator on 2017/9/27.
 */

public class MaskImageView extends ImageView {

    private int mShadeColor = 0x00ffffff;
    private ColorMatrix mColorMatrix = new ColorMatrix(); // 颜色矩阵
    private ColorFilter mColorFilter;

    public MaskImageView(Context context) {
        this(context, null);
    }

    public MaskImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MaskImageView);
        mShadeColor = ta.getColor(R.styleable.MaskImageView_mask_color, mShadeColor);
        float r = Color.alpha(mShadeColor) / 255f;
        r=r-(1 - r)*0.15f;
        float rr = (1 - r)*1.15f;
        setColorMatrix(new float[]{
                rr, 0, 0, 0, Color.red(mShadeColor) * r,
                0, rr, 0, 0, Color.green(mShadeColor) * r,
                0, 0, rr, 0, Color.blue(mShadeColor) * r,
                0, 0, 0, 1, 0,
        });

        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mShadeColor);
    }

    public void setShadeColor(int shadeColor){
        this.mShadeColor = shadeColor;
        float r = Color.alpha(mShadeColor)/255f;
        r = r - (1 - r) * 0.15f;
        float rr = (1 - r) * 1.15f;
        setColorMatrix(new float[]{
                rr, 0, 0, 0, Color.red(mShadeColor) * r,
                0, rr, 0, 0, Color.green(mShadeColor) * r,
                0, 0, rr, 0, Color.blue(mShadeColor) * r,
                0, 0, 0, 1, 0,
        });
        invalidate();
    }
    private void setColorMatrix(float[] matrix){
        mColorMatrix.set(matrix);
        mColorFilter = new ColorMatrixColorFilter(mColorMatrix);
    }
}
