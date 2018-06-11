package com.travel.lib.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.travel.lib.TravelApp;

/**
 * Created by Administrator on 2017/1/20.
 * 继承RoundedBitmapDisplayer的圆角处理
 */
public class MyRoundBitmapDisplayer extends RoundedBitmapDisplayer{
    private static int type = 0;
    public MyRoundBitmapDisplayer(int cornerRadiusPixels, int type) {
        super(cornerRadiusPixels);
        this.type = type;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }

        imageAware.setImageDrawable(new MyRoundedDrawable(bitmap, cornerRadius, margin));
    }

    public static class MyRoundedDrawable extends Drawable {

        protected final float cornerRadius;
        protected final int margin;

        protected final RectF mRect = new RectF(),
                mBitmapRect;RectF mRects = new RectF();
        protected final BitmapShader bitmapShader;
        protected final BitmapShader bitmapShaders;
        protected final Paint paints;
        protected final Paint paint;
        private int mBitmapW = 0;
        private int mBitmapH = 0;

        public MyRoundedDrawable(Bitmap bitmap, int cornerRadius, int margin) {
            this.cornerRadius = cornerRadius;
            this.margin = margin;
            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapW = bitmap.getWidth();
            mBitmapH = bitmap.getHeight();
            mBitmapRect = new RectF (margin, margin, mBitmapW - margin, mBitmapH - margin);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
            paint.setFilterBitmap(true);
//            paint.setDither(true);
            bitmapShaders = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paints = new Paint();
            paints.setAntiAlias(true);
            paints.setShader(bitmapShaders);
            paints.setFilterBitmap(true);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            if(mBitmapH == 0 || mBitmapW == 0) return;
            int rectW = bounds.width();
            int rectH = bounds.height();
            if(rectW == 0 || rectH == 0) return;
            if(rectW<mBitmapW && rectH<mBitmapH){
                // 不变
            }else if (rectW>mBitmapW && rectH>mBitmapH){
                if(rectW*1.0/rectH <= mBitmapW*1.0/mBitmapH){
                    rectW = mBitmapW * rectH / mBitmapH;
                }else{
                    rectH = rectW * mBitmapH / mBitmapW;
                }
            }else if(rectW>mBitmapW && rectH<=mBitmapH){
                rectH = rectW * mBitmapH / mBitmapW;
            }else if(rectH>mBitmapH && rectW<=mBitmapW){
                rectW = mBitmapW * rectH / mBitmapH;
            }
            /*switch (type){
                case ImageDisplayTools.ROUND_LEFT:
                    mRect.set(margin, margin, rectW - margin + cornerRadius, rectH - margin);
                    break;
                case ImageDisplayTools.ROUND_TOP:
                    mRect.set(margin, margin, rectW - margin, rectH - margin + cornerRadius);
                    break;
                case ImageDisplayTools.ROUND_RIGHT:
                    mRect.set(margin - cornerRadius, margin, rectW - margin, rectH - margin);
                    break;
                case ImageDisplayTools.ROUND_BELOW:
                    mRect.set(margin, margin - cornerRadius, rectW - margin, rectH - margin);
                    break;
                default:
                    mRect.set(margin, margin, rectW - margin, rectH - margin);
            }*/
            mRect.set(margin, margin, rectW-margin, rectH-margin);
            Matrix shaderMatrix = new Matrix();
            shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.CENTER);
            bitmapShader.setLocalMatrix(shaderMatrix);
            switch (type){
                case ImageDisplayTools.ROUND_LEFT:
                    mRect.set(margin, margin, bounds.width() - margin + cornerRadius, bounds.height() - margin);
                    break;
                case ImageDisplayTools.ROUND_TOP:
                    mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin + cornerRadius);
                    break;
                case ImageDisplayTools.ROUND_RIGHT:
                    mRect.set(margin - cornerRadius, margin, bounds.width() - margin, bounds.height() - margin);
                    break;
                case ImageDisplayTools.ROUND_BELOW:
                    mRect.set(margin, margin - cornerRadius, bounds.width() - margin, bounds.height() - margin);
                    break;
                default:
                    mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);
            }
//            mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);
            Matrix shaderMatrixs = new Matrix();
            shaderMatrixs.setRectToRect(mRect, mRects, Matrix.ScaleToFit.FILL);
            bitmapShaders.setLocalMatrix(shaderMatrixs);

        }
        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);
            canvas.drawRoundRect(mRects, cornerRadius, cornerRadius, paints);

            // 方角显示的角
//            canvas.drawRect(0, 0, cornerRadius, cornerRadius, paint);
//            canvas.drawRect(mRect.right - cornerRadius, 0, mRect.right, cornerRadius, paint);
//            canvas.drawRect(0,  mRect.bottom - cornerRadius, cornerRadius, mRect.bottom, paint);
//            canvas.drawRect(mRect.right - cornerRadius, mRect.bottom - cornerRadius, mRect.right, mRect.bottom, paint);
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
