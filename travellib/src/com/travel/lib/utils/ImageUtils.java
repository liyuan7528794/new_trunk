package com.travel.lib.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;
import android.util.TypedValue;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;

import java.io.ByteArrayOutputStream;

/**
 * 提供了一些图片处理的工具类
 * Created by ldkxingzhe on 2016/7/28.
 */
public class ImageUtils {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageUtils";

    private ImageUtils(){}

    public static Bitmap loadProperBitmapResource(Context context, int viewWidth, int viewHeight, int resourceId){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        options.inSampleSize = Math.max(2, computeSampleSize(options, viewWidth, viewHeight));
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
    }

    public static int computeSampleSize(BitmapFactory.Options options, int viewWidth, int viewHeight){
        ImageSize bitmapSize = new ImageSize(options.outWidth, options.outHeight);
        ImageSize viewSize = new ImageSize(viewWidth, viewHeight);
        return ImageSizeUtils.computeImageSampleSize(bitmapSize, viewSize, ViewScaleType.FIT_INSIDE,true);
    }

    public static String Bitmap32StrToBase64(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);// 参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap setResourcesToBitmapImageSize(Context context, int sourceId, int width_dp, int height_dp){
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.outWidth = OSUtil.dp2px(context, width_dp);
        option.outHeight = OSUtil.dp2px(context, height_dp);
        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(),sourceId, option);
        return originalBitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = bitmap.getWidth() / 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap decodeResourceToBitmap(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }


}
