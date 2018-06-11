package com.travel.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.travel.lib.R;

/**
 * Created by wyp on 2017/11/23.
 */

public class ArcView extends View {
    private Paint mPaint, mPaint1;
    private PointF mStartPoint, mEndPoint, mControlPoint;
    private int mWidth;
    private int mHeight;
    private Path mPath = new Path();
    private int mColor;// 圆弧的颜色
    private boolean isTop;// 圆弧是否朝上
    private int mLineColor;// 圆弧两边的线的颜色
    private float mLineWidth;// 圆弧两边的线的宽度

    public ArcView(Context context) {
        super(context);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcView, defStyleAttr, 0);
        mColor = typedArray.getColor(R.styleable.ArcView_arcColor, 0);
        isTop = typedArray.getBoolean(R.styleable.ArcView_arcTop, true);
        mLineColor = typedArray.getColor(R.styleable.ArcView_arcLineColor, 0);
        mLineWidth = typedArray.getDimension(R.styleable.ArcView_arcLineWidth, 1);
        typedArray.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint1 = new Paint();
        mStartPoint = new PointF(0, 0);
        mEndPoint = new PointF(0, 0);
        mControlPoint = new PointF(0, 0);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        mPath.reset();
        if (isTop) {
            mStartPoint.x = 0;
            mStartPoint.y = mHeight;
            mPath.moveTo(mStartPoint.x, mStartPoint.y);

            mEndPoint.x = mWidth;
            mEndPoint.y = mHeight;

            mControlPoint.x = mWidth / 2;
            mControlPoint.y = -mHeight;
            mPath.quadTo(mControlPoint.x, mControlPoint.y, mEndPoint.x, mEndPoint.y);
        } else {
            mStartPoint.x = 0;
            mStartPoint.y = 0;
            mPath.moveTo(mStartPoint.x, mStartPoint.y);

            mEndPoint.x = mWidth;
            mEndPoint.y = 0;

            mControlPoint.x = mWidth / 2;
            mControlPoint.y = 2 * mHeight;
            mPath.rQuadTo(mControlPoint.x, mControlPoint.y, mControlPoint.x * 2, 0);
            mPath.lineTo(mWidth, mHeight);
            mPath.lineTo(0, mHeight);
        }
        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(mPath, mPaint);
        if (mLineColor != 0) {
            mPaint1.setColor(mLineColor);
            mPaint1.setStrokeWidth(mLineWidth);
            mPaint1.setStyle(Paint.Style.STROKE);
            canvas.drawLine(1, 0, 1, mHeight, mPaint1);
            Path path = new Path();
            path.moveTo(mWidth - 1, 0);
            canvas.drawLine(mWidth - 1, 0, mWidth - 1, mHeight, mPaint1);
        }
    }
}
