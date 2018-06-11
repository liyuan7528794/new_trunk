package com.travel.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;

/**
 * 单张图片的循环(头尾相连)的Drawable
 * 暂时没有写图片真实长度小于view的情况, 如有需要请根据需求在draw中自己绘制
 * Created by ldkxingzhe on 2016/7/7.
 */
public class OnePictureLooperDrawable extends Drawable{
    @SuppressWarnings("unused")
    private static final String TAG = "OnePictureLooperDrawable";

    private Bitmap mBitmap;
    private int mResourceId = -1;
    private Resources mResources;
    private int mStartX;
    private int mBitmapVisibleWidth;
    private Paint mBitmapPaint;
    private Paint mBoundRectPaint;

    private Rect mRect;
    private Rect mTmpSrcRect;
    private Rect mTmpDesRect;
    private Path mTmpPath;

    private PorterDuffXfermode mMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);


    /** 原图大小跟绘制大小之比 */
    private float mBVR; // bitmap_height / real_height
    // 圆角大小, 左上, 左下, 右上, 右下
    private int mLeftUpRadius, mLeftDownRadius, mRightUpRadius, mRightDownRadius;

    private Handler mHandler;

    public OnePictureLooperDrawable(){
        mTmpSrcRect = new Rect();
        mTmpDesRect = new Rect();
        mBitmapPaint = new Paint();
        mBoundRectPaint = new Paint();
        mBoundRectPaint.setStyle(Paint.Style.FILL);
        mBoundRectPaint.setXfermode(mMode);

        mTmpPath = new Path();
        mHandler = new Handler();
    }

    public void start(){
        mHandler.post(mTimerRunnable);
    }

    public void stop(){
        mHandler.removeCallbacks(mTimerRunnable);
    }


    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mTimerRunnable);
            mStartX -= 10;
            invalidateSelf();
            mHandler.postDelayed(mTimerRunnable, 50);
        }
    };

    /**
     * 设置圆角
     */
    public void setRadius(int leftUpRadius, int rightUpRadius, int rightDownRadius, int leftDownRadius){
        mLeftUpRadius = leftUpRadius;
        mLeftDownRadius = leftDownRadius;
        mRightUpRadius = rightUpRadius;
        mRightDownRadius = rightDownRadius;
        initRoundBoundCircle();
    }

    /**
     * 设置bitmap,
     * must called after setBounds
     */
    public void setBitmap(Bitmap bitmap){
        mBitmap = bitmap;
        calculateArgs();
    }

    private void calculateArgs() {
        mRect = getBounds();
        if(mBitmap == null || mRect.width() == 0 || mRect.height() == 0) return;
        mBVR = mBitmap.getHeight()  * 1.0f / mRect.height();
        mBitmapVisibleWidth = (int) (mBitmap.getWidth() / mBVR);
        mStartX = mBitmapVisibleWidth - mRect.width();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        calculateArgs();
        mTmpPath.reset();
        initRoundBoundCircle();
        if(mResources != null && mResourceId != -1){
            dealBitmapResource(mResources, mResourceId);
        }
    }

    private void initRoundBoundCircle() {
        if(mRect == null) return;
        RectF rectF = new RectF();
        rectF.set(mRect);
        mTmpPath.addRoundRect(rectF, new float[]{mLeftUpRadius, mLeftUpRadius, mRightUpRadius, mRightUpRadius,
                mRightDownRadius, mRightDownRadius, mLeftDownRadius, mLeftDownRadius},
                Path.Direction.CW);
    }

    /**
     * 设置图片的资源Id
     * must called after setBounds
     * @note 如果图片非常长, 很大, 可以考虑使用分段读取的方法
     * */
    public void setBitmapResource(Resources resource,  int resourceId){
        if(getBounds().height() == 0){
            mResourceId = resourceId;
            mResources = resource;
            return;
        }
        dealBitmapResource(resource, resourceId);
    }

    private void dealBitmapResource(Resources resource, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeResource(resource, resourceId, options);
        int bitmapHeight = options.outHeight;
        int heightSampleSize = bitmapHeight / getBounds().height();
        options.inSampleSize = heightSampleSize <= 1 ? 1 : heightSampleSize;
        options.inJustDecodeBounds = false;
        setBitmap(BitmapFactory.decodeResource(resource, resourceId, options));
        mResources = null;
        mResourceId = -1;
    }


    @Override
    public void draw(Canvas canvas) {
        if(mBitmap == null) return;
        if(mStartX < -mBitmapVisibleWidth) mStartX += mBitmapVisibleWidth;
        int startX = mStartX >= 0 ? mStartX % mBitmapVisibleWidth : (mStartX + mBitmapVisibleWidth) % mBitmapVisibleWidth;
        int endX = startX + getBounds().width();
        canvas.saveLayer(mRect.left, mRect.top, mRect.right, mRect.bottom, null, Canvas.ALL_SAVE_FLAG);
        if(endX <= mBitmapVisibleWidth){
            mTmpDesRect.set(getBounds());
            mTmpSrcRect.set((int) (startX * mBVR), 0, (int) (endX * mBVR), mBitmap.getHeight());
            canvas.drawBitmap(mBitmap, mTmpSrcRect, mTmpDesRect, mBitmapPaint);
        }else{
            int screenMid = mBitmapVisibleWidth - startX;
            mTmpDesRect.set(0, 0, screenMid, mRect.bottom);
            mTmpSrcRect.set((int) (startX * mBVR), 0, mBitmap.getWidth(), mBitmap.getHeight());
            canvas.drawBitmap(mBitmap, mTmpSrcRect, mTmpDesRect, mBitmapPaint);
            mTmpDesRect.set(screenMid, 0, mRect.right, mRect.bottom);
            mTmpSrcRect.set(0, 0, (int)((mRect.right - screenMid)*mBVR), mBitmap.getHeight());
            canvas.drawBitmap(mBitmap, mTmpSrcRect, mTmpDesRect, mBitmapPaint);
        }
        canvas.drawPath(mTmpPath, mBoundRectPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
