package com.travel.layout;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import com.travel.lib.TravelApp;
import com.travel.lib.utils.OSUtil;

/**
 * Created by Administrator on 2017/3/22.
 */

public class LadderRoundedImageView extends ImageView{
    public static final String TAG = "SelectableRoundedImageView";

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

    private static final int LEFT_TOP_CORNER = 0;
    private static final int RIGHT_TOP_CORNER = 1;
    private static final int RIGHT_BOTTOM_CORNER = 2;
    private static final int LEFT_BOTTOM_CORNER = 3;

    private static final int HORIZONTAL_ORIENTATION = 0;
    private static final int VERTICAL_ORIENTATION = 1;

    private float mRadius = 0.0f;
    private float mDistanse = 0.0f;
    private int mCorner = LEFT_TOP_CORNER;
    private int mOrientation = HORIZONTAL_ORIENTATION;

    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private Drawable mDrawable;
    private Bitmap mBitmap;
    private RectF mBounds = new RectF();


    public LadderRoundedImageView(Context context) {
        super(context);
    }

    public LadderRoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LadderRoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LadderRoundedImageView, defStyle, 0);

        final int index = a.getInt(R.styleable.LadderRoundedImageView_android_scaleType, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }

        mRadius = a.getDimensionPixelSize(R.styleable.LadderRoundedImageView_radius, 0);
        mDistanse = a.getDimensionPixelSize(R.styleable.LadderRoundedImageView_distanse, 0);
        mCorner = a.getInt(R.styleable.LadderRoundedImageView_corner, 0);
        mOrientation = a.getInt(R.styleable.LadderRoundedImageView_orientations, 0);

        if (mRadius < 0.0f || mDistanse < 0.0f) {
            throw new IllegalArgumentException("radius values cannot be negative.");
        }

        a.recycle();

        updateDrawable();
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
        mBitmapPaint.setPathEffect(new CornerPathEffect(mRadius)); // 拐角处平滑半圆

        Path mPath = new Path();
        initCorner(mPath);
        mPath.close();

//        canvas.clipPath(mPath); // 裁剪掉mPath之外多余的部分
//        canvas.drawBitmap(mBitmap, mMatrix, mBitmapPaint);

        canvas.drawPath(mPath,mBitmapPaint);
        mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); //取交集
        mDrawable.setBounds(0, 0, (int)w, (int)h);
        canvas.saveLayer(mBounds, mBitmapPaint, Canvas.ALL_SAVE_FLAG);
        mDrawable.draw(canvas);
        canvas.restore();
    }

    private void initCorner(Path path){
        float w = getWidth();
        float h = getHeight();
        if(mCorner == LEFT_TOP_CORNER){
            path.moveTo(w, h); //右底部
            path.lineTo(0, h); // 左底部
            if(mOrientation == HORIZONTAL_ORIENTATION)
                path.lineTo(mDistanse, 0); //左顶点
            else
                path.lineTo(0, mDistanse); //左顶点
            path.lineTo(w, 0); //右顶点
            path.lineTo(w, h); //右底部
            path.lineTo(0, h); // 左底部
        }else if(mCorner == RIGHT_TOP_CORNER){
            path.moveTo(0, h); // 左底部
            path.lineTo(0, 0); //左顶点
            if(mOrientation == HORIZONTAL_ORIENTATION)
                path.lineTo(w - mDistanse, 0); //右顶点
            else
                path.lineTo(w, mDistanse); //右顶点
            path.lineTo(w, h); //右底部
            path.lineTo(0, h); // 左底部
            path.lineTo(0, 0); //左顶点
        }else if(mCorner == RIGHT_BOTTOM_CORNER){
            path.moveTo(0, 0); //左顶点 也即起始点
            path.lineTo(w, 0); //右顶点
            if(mOrientation == HORIZONTAL_ORIENTATION)
                path.lineTo(w - mDistanse, h); //右底部
            else
                path.lineTo(w, h - mDistanse);
            path.lineTo(0, h); // 左底部
            path.lineTo(0, 0); //左顶点
            path.lineTo(w, 0); //右顶点
        }else if(mCorner == LEFT_BOTTOM_CORNER){
            path.moveTo(0, 0); //左顶点 也即起始点
            path.lineTo(w, 0); //右顶点
            path.lineTo(w, h); //右底部
            if(mOrientation == HORIZONTAL_ORIENTATION)
                path.lineTo(mDistanse, h); // 左底部
            else
                path.lineTo(0, h - mDistanse); // 左底部
            path.lineTo(0, 0); //左顶点
            path.lineTo(w, 0); //右顶点
        }
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
        mDrawable = CustomDrawable.fromDrawable(drawable, getResources());
        super.setImageDrawable(mDrawable);
        updateDrawable();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mBitmap = bm;
        mResource = 0;
        mDrawable = CustomDrawable.fromBitmap(bm, getResources());
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
        return CustomDrawable.fromDrawable(d, getResources());
    }

    private void updateDrawable() {
        if (mDrawable == null) {
            return;
        }
        if(mBitmap == null)
            mBitmap = CustomDrawable.drawableToBitmap(mDrawable);
//        ((CustomDrawable) mDrawable).setScaleType(mScaleType);
    }

    public float getCornerRadius() {
        return mRadius;
    }
    static class CustomDrawable extends Drawable {

        private static final String TAG = "CustomDrawable";

        private final RectF mBitmapRect = new RectF();
        private final int mBitmapWidth;
        private final int mBitmapHeight;

        private final Paint mBitmapPaint;
        private BitmapShader mBitmapShader;

        // Set default scale type to FIT_CENTER, which is default scale type of
        // original ImageView.
        private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

        private Path mPath = new Path();
        private Bitmap mBitmap;

        public CustomDrawable(Bitmap bitmap, Resources r) {
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

        public static CustomDrawable fromBitmap(Bitmap bitmap, Resources r) {
            if (bitmap != null) {
                return new CustomDrawable(bitmap, r);
            } else {
                return null;
            }
        }

        public static Drawable fromDrawable(Drawable drawable, Resources r) {
            if (drawable != null) {
                if (drawable instanceof CustomDrawable) {
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
                    return new CustomDrawable(bm, r);
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

        /*@Override
        public void setDither(boolean dither) {
            mBitmapPaint.setDither(dither);
            invalidateSelf();
        }

        @Override
        protected boolean onStateChange(int[] state) {
            return super.onStateChange(state);
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

        public ImageView.ScaleType getScaleType() {
            return mScaleType;
        }

        public void setScaleType(ImageView.ScaleType scaleType) {
            if (scaleType == null) {
                return;
            }
            mScaleType = scaleType;
        }*/
    }

}
