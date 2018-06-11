package com.travel.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.travel.lib.utils.MLog;


/**
 * 仿照Gallery中的ImageViewTouchBase,
 * 功能实现可能没有对方的多
 * Created by ke on 16-5-14.
 */
public class ImageViewTouchBase extends ImageView{
    @SuppressWarnings("unused")
    private static final String TAG = "ImageViewTouchBase";

    private Bitmap mBitmap;
    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mDisplayMatrix = new Matrix();
    private final float[] mTmpMatrixValues = new float[9];
    private final RectF mBitmapRect = new RectF();

    private int mWidth, mHeight;

    public ImageViewTouchBase(Context context) {
        this(context, null);
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top;
        MLog.v(TAG,"onLayout, and left=%d, top=%d, right=%d, bottom=%d.", left, top, right, bottom);
        if(mBitmap != null){
            initBaseMatrix();
            displayImage();
        }
    }

    public void zoom(float scale, float centerX, float centerY){
        float oldScale = getScale();
        float deltaScale = scale / oldScale;
        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        center(true, true);
    }

    private void displayImage() {
        mBitmapRect.set(0, 0, mWidth, mHeight);
        Matrix viewMatrix = getViewMatrix();
        setImageMatrix(viewMatrix);
        viewMatrix.mapRect(mBitmapRect);
        onDisplayImage(mBitmapRect);
    }

    protected void onDisplayImage(RectF bitmapRectF){
        MLog.v(TAG, "onDisplayImage %s.", bitmapRectF.toShortString());
    }

    public void panBy(float dx, float dy){
        mSuppMatrix.postTranslate(dx, dy);
        displayImage();
    }

    public void reset(){
        mSuppMatrix.reset();
        displayImage();
    }

    protected void center(boolean horizontal, boolean vertical){
        if(mBitmap == null) return;
        Matrix m = getViewMatrix();
        RectF rect = new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        m.mapRect(rect);

        float width = rect.width();
        float height = rect.height();

        float deltaX = 0, deltaY = 0;
        if(vertical){
            int viewHeight = getHeight();
            if(height < viewHeight){
                deltaY = (viewHeight - height)/2 - rect.top;
            }else if(rect.top > 0){
                deltaY = -rect.top;
            }else if(rect.bottom < viewHeight){
                deltaY = viewHeight - rect.bottom;
            }
        }

        if(horizontal){
            int viewWidth = getWidth();
            if(width < viewWidth){
                deltaX = (viewWidth - width)/ 2 - rect.left;
            }else if(rect.left > 0){
                deltaX = -rect.left;
            }else  if(rect.right < viewWidth){
                deltaX = viewWidth - rect.right;
            }
        }
        MLog.v(TAG, "center, and dx = %f, dy = %f", deltaX, deltaY);
        panBy(deltaX, deltaY);
    }

    private Matrix getViewMatrix() {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }


    protected float getScale(){
        return getValue(mSuppMatrix, Matrix.MSCALE_X);
    }

    private float getValue(@NonNull  Matrix matrix, int whichValue){
        matrix.getValues(mTmpMatrixValues);
        return mTmpMatrixValues[whichValue];
    }

    private void initBaseMatrix() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float bitmapWidth = mBitmap.getWidth();
        float bitmapHeight = mBitmap.getHeight();

        mBaseMatrix.reset();

        float widthScale = Math.min(viewWidth / bitmapWidth, 3.0f);
        float heightScale = Math.min(viewHeight / bitmapHeight, 3.0f);
        float scale = Math.min(widthScale, heightScale);
        MLog.v(TAG, "widthScale is %f, heightScale is %f, scale is %f", widthScale, heightScale, scale);
        mBaseMatrix.postScale(scale, scale);
        mBaseMatrix.postTranslate(
                (viewWidth - bitmapWidth * scale)/2f,
                (viewHeight - bitmapHeight * scale)/2f
        );
    }

    public void recycleBitmap(){
        if(mBitmap != null){
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if(mBitmap != null){
            mBitmap.recycle();
        }
        mBitmap = bm;
    }
}
