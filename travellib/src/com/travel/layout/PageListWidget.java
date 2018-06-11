package com.travel.layout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.travel.bean.NotifyBean;
import com.travel.lib.utils.TravelUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/20.
 */

public class PageListWidget extends FrameLayout {
    private static final String TAG = "PageListWidget";
    private Context mContext;

    private int mWidth = 360;
    private int mHeight = 600;
    private int mCornerX = 0; // 拖拽点对应的页脚
    private int mCornerY = 0;
    private Path mPath0;
    private Path mPath1;

    PointF mTouch = new PointF(); // 拖拽点
    PointF mBezierStart1 = new PointF(); // 贝塞尔曲线起始点
    PointF mBezierControl1 = new PointF(); // 贝塞尔曲线控制点
    PointF mBeziervertex1 = new PointF(); // 贝塞尔曲线顶点
    PointF mBezierEnd1 = new PointF(); // 贝塞尔曲线结束点

    PointF mBezierStart2 = new PointF(); // 另一条贝塞尔曲线
    PointF mBezierControl2 = new PointF();
    PointF mBeziervertex2 = new PointF();
    PointF mBezierEnd2 = new PointF();

    float mMiddleX;
    float mMiddleY;
    float mDegrees;
    float mTouchToCornerDis;
    ColorMatrixColorFilter mColorMatrixFilter;
    Matrix mMatrix;
    float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};

    boolean mIsRTandLB; // 是否属于右上左下
    float mMaxLength = (float) Math.hypot(mWidth, mHeight);
    int[] mBackShadowColors;
    int[] mFrontShadowColors;
    GradientDrawable mBackShadowDrawableLR;
    GradientDrawable mBackShadowDrawableRL;
    GradientDrawable mFolderShadowDrawableLR;
    GradientDrawable mFolderShadowDrawableRL;

    GradientDrawable mFrontShadowDrawableHBT;
    GradientDrawable mFrontShadowDrawableHTB;
    GradientDrawable mFrontShadowDrawableVLR;
    GradientDrawable mFrontShadowDrawableVRL;

    Paint mPaint;

    Scroller mScroller;
    private boolean isAnimated = false;
    private View currentView = null;
    private View nextView = null;
    private View nextViewTranscript = null;

    private BaseAdapter mAdapter = null;
    private int currentPosition = -1;
    private int itemCount = 0;

    private boolean isOutCanScrolled;// 外边是否可滑动 true:不显示跟书有关的提示语， 反之：显示

    public PageListWidget(Context context) {
        this(context, null);
    }

    public PageListWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageListWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initView();
    }

    private ArrayList<NotifyBean> list;

    public void setList(ArrayList<NotifyBean> list) {
        this.list = list;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    private void initView() {
        mPath0 = new Path();
        mPath1 = new Path();

        createDrawable();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        ColorMatrix cm = new ColorMatrix();
        float array[] = {0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0,
                0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0};
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        mMatrix = new Matrix();
        mScroller = new Scroller(getContext());

        mTouch.x = 0.01f; // 不让x,y为0,否则在点计算时会有问题
        mTouch.y = 0.01f;

        setOnTouchListener(new FingerTouchListener());
    }

    /**
     * 创建阴影的GradientDrawable
     */
    private void createDrawable() {
        int[] color = {0x333333, 0xb0333333};
        mFolderShadowDrawableRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, color);
        mFolderShadowDrawableRL
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFolderShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, color);
        mFolderShadowDrawableLR
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowColors = new int[]{0x00FFC108, 0x00C108};
        mBackShadowDrawableRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
        mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowColors = new int[]{0x00FF2942, 0x002942};
        mFrontShadowDrawableVLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
        mFrontShadowDrawableVLR
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mFrontShadowDrawableVRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
        mFrontShadowDrawableVRL
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHTB = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
        mFrontShadowDrawableHTB
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHBT = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
        mFrontShadowDrawableHBT
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        calcPoints();
        super.dispatchDraw(canvas);
        if (itemCount > 1) {
            drawCurrentPageShadow(canvas);
            drawCurrentBackArea(canvas, nextViewTranscript);
        }
    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child.equals(currentView)) {
            drawCurrentPageArea(canvas, child, mPath0);
        } else {
            drawNextPageAreaAndShadow(canvas, child);
        }
        return true;
    }

    /**
     * Author : hmg25 Version: 1.0 Description : 计算拖拽点对应的拖拽脚
     */
    public void calcCornerXY(float x, float y) {
        if (x <= mWidth / 2)
            mCornerX = 0;
        else
            mCornerX = mWidth;
        if (y <= mHeight / 2)
            mCornerY = 0;
        else
            mCornerY = mHeight;
        if ((mCornerX == 0 && mCornerY == mHeight)
                || (mCornerX == mWidth && mCornerY == 0))
            mIsRTandLB = true;
        else
            mIsRTandLB = false;
    }

    /**
     * Author : hmg25 Version: 1.0 Description : 求解直线P1P2和直线P3P4的交点坐标
     */
    public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        PointF CrossP = new PointF();
        // 二元函数通式： y=ax+b
        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }

    private void calcPoints() {
        mMiddleX = (mTouch.x + mCornerX) / 2;
        mMiddleY = (mTouch.y + mCornerY) / 2;
        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
        mBezierControl1.y = mCornerY;
        mBezierControl2.x = mCornerX;
        mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);


        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x)
                / 3;
        mBezierStart1.y = mCornerY;

        //这里做限制,使得手指滑动过程中视图不能翻过中间线
        if (!isAnimated) {
            if (mCornerX == 0 && mBezierStart1.x > mWidth / 2) {            //向右滑动时
                float f1 = Math.abs(mCornerX - mTouch.x);
                float f2 = mWidth / 2 * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);                //这里将BStart1.x按比例缩小到mWidth后
                //重新计算等效的mTouch.x的值
                float f3 = Math.abs(mCornerX - mTouch.x)
                        * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);

                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                        * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                        * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

                mBezierStart1.x = mBezierControl1.x
                        - (mCornerX - mBezierControl1.x) / 2;
            }
            if (mCornerX == mWidth && mBezierStart1.x < mWidth / 2) {                //向左滑动时
                mBezierStart1.x = mWidth - mBezierStart1.x;
                float f1 = Math.abs(mCornerX - mTouch.x);
                float f2 = mWidth / 2 * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);
                float f3 = Math.abs(mCornerX - mTouch.x)
                        * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);
                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                        * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                        * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
                mBezierStart1.x = mBezierControl1.x
                        - (mCornerX - mBezierControl1.x) / 2;
            }
        }

        mBezierStart2.x = mCornerX;
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y)
                / 2;

        mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX),
                (mTouch.y - mCornerY));

        mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1,
                mBezierStart2);
        mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1,
                mBezierStart2);

		/*
         * mBeziervertex1.x 推导
		 * ((mBezierStart1.x+mBezierEnd1.x)/2+mBezierControl1.x)/2 化简等价于
		 * (mBezierStart1.x+ 2*mBezierControl1.x+mBezierEnd1.x) / 4
		 */
        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
    }

    private void drawCurrentPageArea(Canvas canvas, View child, Path path) {
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
                mBezierEnd1.y);
        mPath0.lineTo(mTouch.x, mTouch.y);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
                mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();

        canvas.save();
        canvas.clipPath(path, Region.Op.XOR);
        child.draw(canvas);
        canvas.restore();
    }

    private void drawNextPageAreaAndShadow(Canvas canvas, View child) {
        mPath1.reset();
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.lineTo(mCornerX, mCornerY);
        mPath1.close();

        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
                - mCornerX, mBezierControl2.y - mCornerY));
        int leftx;
        int rightx;
        GradientDrawable mBackShadowDrawable;
        if (mIsRTandLB) {
            leftx = (int) (mBezierStart1.x);
            rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
            mBackShadowDrawable = mBackShadowDrawableLR;
        } else {
            leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
            rightx = (int) mBezierStart1.x;
            mBackShadowDrawable = mBackShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        child.draw(canvas);
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx,
                (int) (mMaxLength + mBezierStart1.y));
        mBackShadowDrawable.draw(canvas);
        canvas.restore();
    }

    public void setScreen(int w, int h) {
        mWidth = w;
        mHeight = h;
    }


    /**
     * 绘制翻起页的阴影
     */
    private void drawCurrentPageShadow(Canvas canvas) {
        double degree;
        if (mIsRTandLB) {
            degree = Math.PI
                    / 4
                    - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x
                    - mBezierControl1.x);
        } else {
            degree = Math.PI
                    / 4
                    - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x
                    - mBezierControl1.x);
        }
        // 翻起页阴影顶点与touch点的距离
        double d1 = (float) 25 * 1.414 * Math.cos(degree);
        double d2 = (float) 25 * 1.414 * Math.sin(degree);
        float x = (float) (mTouch.x + d1);
        float y;
        if (mIsRTandLB) {
            y = (float) (mTouch.y + d2);
        } else {
            y = (float) (mTouch.y - d2);
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.close();
        float rotateDegrees;
        canvas.save();

        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        int leftx;
        int rightx;
        GradientDrawable mCurrentPageShadow;
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl1.x);
            rightx = (int) mBezierControl1.x + 25;
            mCurrentPageShadow = mFrontShadowDrawableVLR;
        } else {
            leftx = (int) (mBezierControl1.x - 25);
            rightx = (int) mBezierControl1.x + 1;
            mCurrentPageShadow = mFrontShadowDrawableVRL;
        }

        rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x
                - mBezierControl1.x, mBezierControl1.y - mTouch.y));
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
        mCurrentPageShadow.setBounds(leftx,
                (int) (mBezierControl1.y - mMaxLength), rightx,
                (int) (mBezierControl1.y));
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.close();
        canvas.save();
        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl2.y);
            rightx = (int) (mBezierControl2.y + 25);
            mCurrentPageShadow = mFrontShadowDrawableHTB;
        } else {
            leftx = (int) (mBezierControl2.y - 25);
            rightx = (int) (mBezierControl2.y + 1);
            mCurrentPageShadow = mFrontShadowDrawableHBT;
        }
        rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y
                - mTouch.y, mBezierControl2.x - mTouch.x));
        canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
        float temp;
        if (mBezierControl2.y < 0)
            temp = mBezierControl2.y - mHeight;
        else
            temp = mBezierControl2.y;

        int hmg = (int) Math.hypot(mBezierControl2.x, temp);
        if (hmg > mMaxLength)
            mCurrentPageShadow
                    .setBounds((int) (mBezierControl2.x - 25) - hmg, leftx,
                            (int) (mBezierControl2.x + mMaxLength) - hmg,
                            rightx);
        else
            mCurrentPageShadow.setBounds(
                    (int) (mBezierControl2.x - mMaxLength), leftx,
                    (int) (mBezierControl2.x), rightx);

        mCurrentPageShadow.draw(canvas);
        canvas.restore();
    }

    /**
     * Author : hmg25 Version: 1.0 Description : 绘制翻起页背面
     */
    private void drawCurrentBackArea(Canvas canvas, View view) {
        int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
        float f1 = Math.abs(i - mBezierControl1.x);
        int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
        float f2 = Math.abs(i1 - mBezierControl2.y);
        float f3 = Math.min(f1, f2);
        mPath1.reset();
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath1.close();
        GradientDrawable mFolderShadowDrawable;
        int left;
        int right;
        if (mIsRTandLB) {
            left = (int) (mBezierStart1.x - 1);
            right = (int) (mBezierStart1.x + f3 + 1);
            mFolderShadowDrawable = mFolderShadowDrawableLR;
        } else {
            left = (int) (mBezierStart1.x - f3 - 1);
            right = (int) (mBezierStart1.x + 1);
            mFolderShadowDrawable = mFolderShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);

        //mPaint.setColorFilter(mColorMatrixFilter);

        float rotateDegrees = (float) Math.toDegrees(Math.PI / 2 + Math.atan2(mBezierControl2.y
                - mTouch.y, mBezierControl2.x - mTouch.x));

        if (mCornerY == 0) {
            rotateDegrees -= 180;
        }
        mMatrix.reset();
        mMatrix.setPolyToPoly(new float[]{Math.abs(mWidth - mCornerX), mCornerY}, 0, new float[]{mTouch.x, mTouch.y}, 0, 1);
        mMatrix.postRotate(rotateDegrees, mTouch.x, mTouch.y);
        canvas.save();
        canvas.concat(mMatrix);
        view.draw(canvas);
        canvas.restore();
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right,
                (int) (mBezierStart1.y + mMaxLength));
        mFolderShadowDrawable.draw(canvas);
        canvas.restore();
    }

    public void computeScroll() {
        super.computeScroll();

        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            mTouch.x = x;
            mTouch.y = y;
            postInvalidate();
        }
        if (isAnimated && mScroller.isFinished()) {                //这里是用来化最后一帧的，用来消除阴影
            isAnimated = false;
            if (DragToRight()) {
                if (currentPosition > 0)
                    currentPosition -= 1;
            } else if(DragToLeft()){
                if (currentPosition < itemCount - 1)
                    currentPosition += 1;
            }
            Log.e(TAG, "currentPosition in computeScroll->" + currentPosition);
            currentView = mAdapter.getView(currentPosition, currentView, null);

            mTouch.x = 0.01f;
            mTouch.y = 0.01f;
            mCornerX = 0;
            mCornerY = 0;
            postInvalidate();
        }
    }


    private void startAnimation(int delayMillis) {
        int dx, dy;
        // dx 水平方向滑动的距离，负值会使滚动向左滚动
        // dy 垂直方向滑动的距离，负值会使滚动向上滚动

        if (mCornerX > 0) {
            dx = (int) (-mTouch.x + 1);
        } else {
            dx = (int) (mWidth - mTouch.x - 1);
        }
        if (mCornerY > 0) {
            dy = (int) (mHeight - mTouch.y - 1);
        } else {
            dy = (int) (1 - mTouch.y); // 防止mTouch.y最终变为0
        }
        mScroller.startScroll((int) mTouch.x, (int) mTouch.y, dx, dy,
                delayMillis);

    }


    public void abortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    public boolean canDragOver() {
        if (mTouchToCornerDis > mWidth / 10)
            return true;
        return false;
    }

    /**
     * Author : hmg25 Version: 1.0 Description : 是否从左边翻向右边
     */
    public boolean DragToRight() {
        if(xMove <= yMove){
            return false;
        }
        if (mCornerX > 0)
            return false;
        return true;
    }
    public boolean DragToLeft() {
        if(xMove <= yMove){
            return false;
        }
        if (mCornerX > 0)
            return true;
        return false;
    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        itemCount = mAdapter.getCount();
        currentView = null;
        nextView = null;
        nextViewTranscript = null;
        removeAllViews();
        if (itemCount != 0) {
            currentPosition = 0;
            currentView = mAdapter.getView(currentPosition, null, null);
            addView(currentView);
            if (itemCount > 1) {
                nextView = mAdapter.getView(currentPosition + 1, null, null);
                nextViewTranscript = mAdapter.getView(currentPosition + 1, null, null);
                addView(nextView);
                addView(nextViewTranscript);
            }

        } else {
            currentPosition = -1;
        }
        mTouch.x = 0.01f;
        mTouch.y = 0.01f;
        mCornerX = 0;
        mCornerY = 0;
        postInvalidate();

    }

    float xDown, xMove;
    float yDown, yMove;

    GestureDetector.OnGestureListener listener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.i(TAG, "onDown:" + e.getAction());
            xDown = e.getRawX();
            yDown = e.getRawY();
            xMove = 0;
            yMove = 0;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.i(TAG, "onShowPress:" + e.getAction());
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Intent intent = new Intent();
            intent.setAction("com.travel.shop.activity.GoodsInfoActivity");
            intent.putExtra("storyId", list.get(currentPosition).getId());
            mContext.startActivity(intent);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i(TAG, "onScroll:" + e2.getAction());
            xMove = Math.abs(xDown - e2.getRawX());
            yMove = Math.abs(yDown - e2.getRawY());
            Log.d(TAG, "xMove: " + xMove + "yMove: " + yMove);
                abortAnimation();
                calcCornerXY(e2.getX(), e2.getY());
                if (DragToRight()) {
                    if (currentPosition <= 0) {
                        return false;
                    }
                    nextView = mAdapter.getView(currentPosition - 1, nextView, null);
                    nextViewTranscript = mAdapter.getView(currentPosition - 1, nextViewTranscript, null);
                } else if(DragToLeft()){
                    if (currentPosition >= itemCount - 1) {
                        return false;
                    }
                    nextView = mAdapter.getView(currentPosition + 1, nextView, null);
                    nextViewTranscript = mAdapter.getView(currentPosition + 1, nextViewTranscript, null);
                }

            if (xMove > yMove) {
                if (outScrollListener != null) {
                    outScrollListener.isScrolled(false);
                    isOutCanScrolled = false;
                }
                mTouch.x = e2.getX();
                mTouch.y = e2.getY();
                PageListWidget.this.postInvalidate();
                isAnimated = false;
            } else if (outScrollListener != null) {
                outScrollListener.isScrolled(true);
                isOutCanScrolled = true;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return true;
        }
    };
    GestureDetector gestureDetector = new GestureDetector(mContext, listener);

    private class FingerTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == PageListWidget.this && mAdapter != null) {
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (outScrollListener != null) {
                        outScrollListener.isScrolled(true);
                        isOutCanScrolled = true;
                    }
                    Log.i(TAG, "onTouch:" + event.getAction());
                    if (canDragOver()) {
                        isAnimated = true;
                        startAnimation(100);
                    } else {
                        mTouch.x = mCornerX - 0.09f;
                        mTouch.y = mCornerY - 0.09f;
                    }

                    PageListWidget.this.postInvalidate();

                    if (DragToRight()) {
                        if (currentPosition == 0 && isOutCanScrolled) {
                            TravelUtil.showToast("已经是第一页了");
                        }
                    } else if(DragToLeft()){
                        if (currentPosition == itemCount - 1 && isOutCanScrolled) {
                            TravelUtil.showToast("已经是最后一页了");
                        }
                    }
                }
            }
            return true;
        }
    }

    public interface OutScroll {
        void isScrolled(boolean isScrolled);
    }

    OutScroll outScrollListener;

    public void setOutScroll(OutScroll outScrollListener) {
        this.outScrollListener = outScrollListener;
    }
}
