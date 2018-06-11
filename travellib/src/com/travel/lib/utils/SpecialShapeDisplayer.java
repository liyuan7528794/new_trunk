package com.travel.lib.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Created by ldkxingzhe on 2017/2/6.
 */
public class SpecialShapeDisplayer implements BitmapDisplayer{
    @SuppressWarnings("unused")
    private static final String TAG = "SpecialShapeDisplayer";

    private Bitmap mMaskBitmap;

    public SpecialShapeDisplayer(@NonNull Bitmap maskBitmap){
        mMaskBitmap = maskBitmap;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }

        imageAware.setImageDrawable(new SpecialShapeDrawable(bitmap, mMaskBitmap));
    }

    public static class SpecialShapeDrawable extends Drawable{

        private Paint mPaint = new Paint();
        private PorterDuffXfermode mPorterDuffXfermode;
        private Bitmap mBitmap;
        private Bitmap mShapeBitmap;
        public SpecialShapeDrawable(Bitmap bitmap, Bitmap maskBitmap) {
            mBitmap = bitmap;
            mShapeBitmap = maskBitmap;
            mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
            mPaint.setAntiAlias(true);
        }


        @Override
        public void draw(Canvas canvas) {
            Rect rect = getBounds();
            canvas.saveLayer(rect.left, rect.top, rect.right, rect.bottom, mPaint, Canvas.ALL_SAVE_FLAG);
            canvas.drawBitmap(mBitmap, null, rect, mPaint);
            mPaint.setXfermode(mPorterDuffXfermode);
            canvas.drawBitmap(mShapeBitmap, null, rect, mPaint);
            mPaint.setXfermode(null);
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
