package com.travel.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.travel.lib.R;

/**
 * Created by Administrator on 2017/9/11.
 */

public class CurveRoundedImageView extends ImageView {
    public static final String TAG = "CurveRoundedImageView";

    private int mResource = 0;

    private static final ImageView.ScaleType[] sScaleTypeArray = {
            ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };

    // Set default scale type to FIT_CENTER, which is default scale type of
    // original ImageView.
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

    private int orientations = 0;
    private float mOffset = 0.0f;

    private Drawable mDrawable;
    private Bitmap mBitmap;
    private RectF mBounds = new RectF();
//    private float[] mRadii = new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };

    public CurveRoundedImageView(Context context) {
        super(context);
    }

    public CurveRoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveRoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CurveRoundedImageView, defStyle, 0);

        final int index = a.getInt(R.styleable.CurveRoundedImageView_android_scaleType, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }

        orientations = a.getInteger(R.styleable.CurveRoundedImageView_orientation_curve, 0);
        mOffset = a.getDimensionPixelSize(R.styleable.CurveRoundedImageView_offset_distance, 0);

        a.recycle();

        updateDrawable();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public ImageView.ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void setScaleType(ImageView.ScaleType scaleType) {
        super.setScaleType(scaleType);
        mScaleType = scaleType;
        updateDrawable();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mResource = 0;
        mDrawable = CurveRoundedCornerDrawable.fromDrawable(drawable, getResources());
        super.setImageDrawable(mDrawable);
        updateDrawable();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mBitmap = bm;
        mResource = 0;
        mDrawable = CurveRoundedCornerDrawable.fromBitmap(bm, getResources());
        super.setImageDrawable(mDrawable);
        updateDrawable();
    }

    @Override
    public void setImageResource(int resId) {
        if (mResource != resId) {
            mResource = resId;
            mDrawable = resolveResource();
            super.setImageDrawable(mDrawable);
            updateDrawable();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dra(canvas);
    }

    public void dra(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        Matrix mMatrix = new Matrix();
        float scale = 1;
        float scaleX = w/ mBitmap.getWidth();
        float scaleY = h / mBitmap.getHeight();
        scale = scaleX > scaleY ? scaleX : scaleY;
        mMatrix.setScale(scale, scale);

        BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapShader.setLocalMatrix(mMatrix);

        Paint mBitmapPaint= new Paint();
        mBitmapPaint.setShader(bitmapShader);
        mBitmapPaint.setAntiAlias(true); // 去锯齿

        Path mPath = new Path();
        if(mOffset < 0){
            pathConfigIn(mPath, mOffset);
        } else {
            pathConfigOut(mPath, mOffset);
        }

        mPath.close();

        canvas.drawPath(mPath,mBitmapPaint);
        mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); //取交集
        mDrawable.setBounds(0, 0, (int)w, (int)h);
        canvas.saveLayer(mBounds, mBitmapPaint, Canvas.ALL_SAVE_FLAG);
        mDrawable.draw(canvas);
        mBitmapPaint.setXfermode(null);
        canvas.restore();
    }

    private void pathConfigOut(Path mPath, float offset) {
        int mBitmapWidth = getWidth();
        int mBitmapHeight = getHeight();
        switch (orientations){
            case 0:
                mPath.moveTo(offset, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.lineTo(offset, mBitmapHeight);
                mPath.cubicTo(offset, mBitmapHeight, 0, mBitmapHeight/2, offset, 0);
//                    mPath.lineTo(0, 0);
                break;
            case 1:
                mPath.moveTo(0, offset);
                mPath.cubicTo(0, offset, mBitmapWidth/2, 0, mBitmapWidth, offset);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.lineTo(0, mBitmapHeight);
                mPath.lineTo(0, offset);
                break;
            case 2:
                mPath.moveTo(0, 0);
                mPath.lineTo(mBitmapWidth-offset, 0);
                mPath.cubicTo(mBitmapWidth-offset, 0, mBitmapWidth, mBitmapHeight/2, mBitmapWidth-offset, mBitmapHeight);
                mPath.lineTo(0, mBitmapHeight);
                mPath.lineTo(0, 0);
                break;
            case 3:
                mPath.moveTo(0, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight-offset);
                mPath.cubicTo(mBitmapWidth, mBitmapHeight-offset, mBitmapWidth/2, mBitmapHeight, 0, mBitmapHeight-offset);
                mPath.lineTo(0, 0);
                break;
            default:
                mPath.moveTo(offset, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.lineTo(offset, mBitmapHeight);
                mPath.cubicTo(offset, mBitmapHeight, 0, mBitmapHeight/2, offset, 0);
                break;
        }

    }

    private void pathConfigIn(Path mPath, float offset) {
        int mBitmapWidth = getWidth();
        int mBitmapHeight = getHeight();
        switch (orientations){
            case 0:
                mPath.moveTo(0, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.lineTo(0, mBitmapHeight);
                mPath.cubicTo(0, mBitmapHeight, -offset, mBitmapHeight/2, 0, 0);
                break;
            case 1:
                mPath.moveTo(0, 0);
                mPath.cubicTo(0, 0, mBitmapWidth/2, -offset, mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.lineTo(0, mBitmapHeight);
                mPath.lineTo(0, 0);
                break;
            case 2:
                mPath.moveTo(0, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.cubicTo(mBitmapWidth, 0, mBitmapWidth+offset, mBitmapHeight/2, mBitmapWidth, mBitmapHeight);
                mPath.lineTo(0, mBitmapHeight);
                mPath.lineTo(0, 0);
                break;
            case 3:
                mPath.moveTo(0, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.cubicTo(mBitmapWidth, mBitmapHeight, mBitmapWidth/2, mBitmapHeight+offset, 0, mBitmapHeight);
                mPath.lineTo(0, 0);
                break;
            default:
                mPath.moveTo(0, 0);
                mPath.lineTo(mBitmapWidth, 0);
                mPath.lineTo(mBitmapWidth, mBitmapHeight);
                mPath.lineTo(0, mBitmapHeight);
                mPath.cubicTo(0, mBitmapHeight, -offset, mBitmapHeight/2, 0, 0);
                break;
        }

    }

    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }

        Drawable d = null;

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "Unable to find resource: " + mResource, e);
                // Don't try again.
                mResource = 0;
            }
        }
        return CurveRoundedCornerDrawable.fromDrawable(d, getResources());
    }

    private void updateDrawable() {
        if (mDrawable == null) {
            return;
        }

        if(mBitmap == null)
            mBitmap = LadderRoundedImageView.CustomDrawable.drawableToBitmap(mDrawable);

    }


    public void setOrientations(int orientations) {
        this.orientations = orientations;
        updateDrawable();
        invalidate();
    }

    static class CurveRoundedCornerDrawable extends Drawable {

        private static final String TAG = "CurveRoundedCornerDrawable";
        private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

        private final RectF mBitmapRect = new RectF();
        private  int mBitmapWidth;
        private int mBitmapHeight;

        private final Paint mBitmapPaint;
        private BitmapShader mBitmapShader;

        // Set default scale type to FIT_CENTER, which is default scale type of
        // original ImageView.
        private Bitmap mBitmap;

        public CurveRoundedCornerDrawable(Bitmap bitmap, Resources r) {
            mBitmap = bitmap;
            mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            if (bitmap != null) {
                mBitmapWidth = bitmap.getScaledWidth(r.getDisplayMetrics());
                mBitmapHeight = bitmap.getScaledHeight(r.getDisplayMetrics());
            } else {
                mBitmapWidth = mBitmapHeight = -1;
            }

            mBitmapRect.set(0, 0, mBitmapWidth, mBitmapHeight);

            mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBitmapPaint.setStyle(Paint.Style.FILL);
            mBitmapPaint.setShader(mBitmapShader);

        }

        public static CurveRoundedCornerDrawable fromBitmap(Bitmap bitmap, Resources r) {
            if (bitmap != null) {
                return new CurveRoundedCornerDrawable(bitmap, r);
            } else {
                return null;
            }
        }

        public static Drawable fromDrawable(Drawable drawable, Resources r) {
            if (drawable != null) {
                if (drawable instanceof CurveRoundedCornerDrawable) {
                    return drawable;
                } else if (drawable instanceof LayerDrawable) {
                    LayerDrawable ld = (LayerDrawable) drawable;
                    final int num = ld.getNumberOfLayers();
                    for (int i = 0; i < num; i++) {
                        Drawable d = ld.getDrawable(i);
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d, r));
                    }
                    return ld;
                }

                Bitmap bm = drawableToBitmap(drawable);
                if (bm != null) {
                    return new CurveRoundedCornerDrawable(bm, r);
                } else {
                    Log.w(TAG, "Failed to create bitmap from drawable!");
                }
            }
            return drawable;
        }

        public static Bitmap drawableToBitmap(Drawable drawable) {
            if (drawable == null) {
                return null;
            }

            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }

            Bitmap bitmap;
            int width = Math.max(drawable.getIntrinsicWidth(), 2);
            int height = Math.max(drawable.getIntrinsicHeight(), 2);
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                bitmap = null;
            }
            return bitmap;
        }



        @Override
        public void draw(Canvas canvas) {

        }


        @Override
        public int getOpacity() {
            return (mBitmap == null || mBitmap.hasAlpha() || mBitmapPaint.getAlpha() < 255) ? PixelFormat.TRANSLUCENT
                    : PixelFormat.OPAQUE;
        }

        @Override
        public void setAlpha(int alpha) {
            mBitmapPaint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mBitmapPaint.setColorFilter(cf);
            invalidateSelf();
        }

        @Override
        public void setDither(boolean dither) {
            mBitmapPaint.setDither(dither);
            invalidateSelf();
        }

        @Override
        public void setFilterBitmap(boolean filter) {
            mBitmapPaint.setFilterBitmap(filter);
            invalidateSelf();
        }

        @Override
        public int getIntrinsicWidth() {
            return mBitmapWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return mBitmapHeight;
        }

    }
}
