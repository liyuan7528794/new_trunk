package com.travel.lib.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Created by Administrator on 2017/2/6.
 * 梯形圆角处理
 */

public class LadderRoundBitmapDisplayer implements BitmapDisplayer {

    protected final int cornerRadius;
    protected final int margin;
    protected final int leftTopX;
    protected final int rightTopX;
    protected final int leftBellowX;
    protected final int rightBellowX;
    public LadderRoundBitmapDisplayer(int leftX,int rightX, int leftBellowX, int rightBellowX,int cornerRadiusPixels) {
        this(leftX, rightX, leftBellowX, rightBellowX, cornerRadiusPixels, 0);
    }

    public LadderRoundBitmapDisplayer(int leftX,int rightX, int leftBellowX, int rightBellowX, int cornerRadiusPixels, int marginPixels) {
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
        this.leftTopX = leftX;
        this.rightTopX = rightX;
        this.leftBellowX = leftBellowX;
        this.rightBellowX = rightBellowX;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }

        imageAware.setImageDrawable(new RoundedDrawable(bitmap, leftTopX, rightTopX, leftBellowX, rightBellowX, cornerRadius, margin));
    }

    public static class RoundedDrawable extends Drawable {

        protected final float cornerRadius;
        protected final int margin;

        protected final RectF mRect = new RectF(),
                mBitmapRect;
        protected final BitmapShader bitmapShader;
        protected final Paint paint;
        protected final int leftTX;
        protected final int rightTX;
        protected final int leftBX;
        protected final int rightBX;
        public RoundedDrawable(Bitmap bitmap, int leftX,int rightX, int leftBellowX, int rightBellowX, int cornerRadius, int margin) {
            this.cornerRadius = cornerRadius;
            this.margin = margin;
            this.leftTX = leftX;
            this.rightTX = rightX;
            this.leftBX = leftBellowX;
            this.rightBX = rightBellowX;
            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapRect = new RectF (margin, margin, bitmap.getWidth() - margin, bitmap.getHeight() - margin);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
            paint.setFilterBitmap(true);
            paint.setDither(true);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);

            // Resize the original bitmap to fit the new bound
            Matrix shaderMatrix = new Matrix();
            shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
            bitmapShader.setLocalMatrix(shaderMatrix);

        }

        @Override
        public void draw(Canvas canvas) {
            Path path = new Path();
            path.reset();
            path.moveTo(leftTX ==-1 ? 0 : leftTX, 0); //左顶点 也即起始点
            path.lineTo(rightTX ==-1 ? mRect.width() : rightTX, 0); //右顶点
            path.lineTo(rightBX ==-1 ? mRect.width() : rightBX, mRect.height()); //右底部
            path.lineTo(leftBX ==-1 ? 0 : leftBX, mRect.height()); // 左底部
            path.lineTo(leftTX ==-1 ? 0 : leftTX, 0); //左顶点
            path.lineTo(rightTX ==-1 ? mRect.width() : rightTX, 0); //右顶点
            paint.setStyle(Paint.Style.FILL);
            paint.setPathEffect(new CornerPathEffect(cornerRadius)); // 拐角处平滑半圆
            canvas.drawPath(path, paint);

//            canvas.clipPath(path);
//            canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }
    }

}
