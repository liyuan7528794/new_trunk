package com.travel.lib.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.travel.Constants;
import com.travel.lib.R;

import java.io.File;

public class ImageDisplayTools {
    public final static int ROUND_LEFT = 1;
    public final static int ROUND_TOP = 2;
    public final static int ROUND_RIGHT = 3;
    public final static int ROUND_BELOW = 4;
    // 图片缓存工具
    static ImageLoader imageLoader = ImageLoader.getInstance();
    static DisplayImageOptions options;

    public interface LoadingCompleteListener {
        public void onLoadingComplete();
    }

    public static void initImageLoader(Context context) {

        // 图片缓存地址
        File cacheDir = StorageUtils
                .getOwnCacheDirectory(context, "xblx/Cache");
        // 图片加载器默认设�?
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).memoryCacheExtraOptions(480, 800)
                .diskCacheExtraOptions(480, 800, null).threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(50 * 1024 * 1024)
                .memoryCacheSizePercentage(13)
                .memoryCache(new WeakMemoryCache())
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheSize(500 * 1024 * 1024).diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(context)) // default
                .imageDecoder(new BaseImageDecoder(false)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .build();
        ImageLoader.getInstance().init(config);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_loading)
                .showImageForEmptyUri(R.drawable.ic_loading)
                .showImageOnFail(R.drawable.ic_loading).cacheInMemory(true)
                .cacheOnDisk(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .build();

    }

    public static void displayImageRoundCity(String url, ImageView img) {
        DisplayImageOptions option = new DisplayImageOptions.Builder()
                .cloneFrom(options)
                .showImageOnLoading(R.drawable.ic_default_city)
                .showImageForEmptyUri(R.drawable.ic_default_city)
                .showImageOnFail(R.drawable.ic_default_city)
                .displayer(new SimpleBitmapDisplayer())
                .build();
        imageLoader.displayImage(url, img, option);
    }

    public static void displayImageRound(String url, ImageView img) {
        DisplayImageOptions option = new DisplayImageOptions.Builder()
                .cloneFrom(options).displayer(new SimpleBitmapDisplayer())
                .build();
        imageLoader.displayImage(url, img, option);
    }

    public static void displayImage(String url, ImageView img) {
        if ("-1".equals(url) || "".equals(url)) {
            url = Constants.DefaultHeadImg;
        }
        imageLoader.displayImage(url, img, options);
    }

    public static void displayHeadImage(String url, ImageView img) {
        displayHeadImage(url, img, null);
    }

    public static void displayHeadImage(String url, ImageView img, final LoadingCompleteListener listener) {
        DisplayImageOptions option = new DisplayImageOptions.Builder()
                .cloneFrom(options).displayer(new CircleBitmapDisplayer())
                .showImageOnLoading(R.drawable.common_pic_user)
                .showImageForEmptyUri(R.drawable.common_pic_user)
                .showImageOnFail(R.drawable.common_pic_user)
                .build();
        imageLoader.displayImage(url, img, option, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String arg0, View arg1) {

            }

            @Override
            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {

            }

            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                if (arg2 != null) {
                    getRoundedCornerBitmap(arg2, 2);
                }
                if (listener != null) {
                    listener.onLoadingComplete();
                }
            }

            @Override
            public void onLoadingCancelled(String arg0, View arg1) {

            }
        });
        ;
    }

    /**
     * 绘制圆形图标， 默认边界为白色
     *
     * @param url    链接
     * @param img    iamgeView
     * @param margin 边界
     */
    public static void displayCircleImage(String url, ImageView img, int margin) {
        displayCircleImage(url, img, margin, Color.WHITE);
    }

    /**
     * 回执圆形图标, 可以设置边界颜色
     *
     * @param url
     * @param img
     * @param margin
     * @param marginColor
     */
    public static void displayCircleImage(String url, ImageView img, int margin, int marginColor) {
        if (margin < 0)
            margin = 0;
        BitmapDisplayer bitmapDisplayer = new CircleBitmapDisplayer(margin, marginColor);
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .showImageOnLoading(R.drawable.common_pic_user)
                .showImageForEmptyUri(R.drawable.common_pic_user)
                .showImageOnFail(R.drawable.common_pic_user)
                .displayer(bitmapDisplayer)
                .build();
        imageLoader.displayImage(url, img, option);
    }

    /**
     * 绘制特殊形状
     */
    public static void displaySpecialDrawable(String url, ImageView img, @NonNull Bitmap specialShape) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new SpecialShapeDisplayer(specialShape))
                .build();
        imageLoader.displayImage(url, img, option);
    }

    /**
     * 绘制显示圆角图片
     */
    public static void disPlayRoundDrawable(String url, ImageView img, int circle) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new RoundedBitmapDisplayer(circle))
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();

        imageLoader.displayImage(url, img, option);
    }

    /**
     * 绘制显示头像的圆角图片
     */
    public static void disPlayRoundDrawableHead(String url, ImageView img, int circle) {
        DisplayImageOptions optionHead = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new RoundedBitmapDisplayer(circle))
                .showImageOnLoading(R.drawable.common_pic_user)
                .showImageForEmptyUri(R.drawable.common_pic_user)
                .showImageOnFail(R.drawable.common_pic_user)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();

        imageLoader.displayImage(url, img, optionHead);
    }

    public static void disPlayRoundDrawable(String url, ImageView img, int circle, int type) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .displayer(new MyRoundBitmapDisplayer(circle, type))
                .build();
        imageLoader.displayImage(url, img, option);
    }

    /**
     * 绘制显示梯形圆角图片
     *
     * @param leftTopX  左顶点X轴相对图片的坐标 -1表示左边为直角即0
     * @param rightTopX 左顶点X轴相对图片的坐标 -1表示为图片的宽，右边为直角
     */
    public static void disPlayLadderRoundDrawable(String url, ImageView img, int circle, int leftTopX, int rightTopX, int leftBellowX, int rightBellowX) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new LadderRoundBitmapDisplayer(leftTopX, rightTopX, leftBellowX, rightBellowX, circle))
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();
        imageLoader.displayImage(url, img, option);
    }

    /**
     * 绘制圆角的图片, 而且scaleType为 NONE_SAFE,默认使用自己的
     */
    public static void displayRoundDrawableEXACTLY(String url, final ImageView img,
                                                   int circle, ImageLoadingListener listener) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new RoundedBitmapDisplayer(circle))
                .imageScaleType(ImageScaleType.NONE_SAFE)
                .build();
        imageLoader.displayImage(url, img, option, listener);
    }

    /**
     * 聊天用的图片
     */
    public static void displayDrawableChat(String url, final ImageView img, ImageLoadingListener listener) {
        imageLoader.displayImage(url, img, options, listener);
    }

    /**
     * 正常显示图片, 不进行默认缩放
     */
    public static void displayImageEXACTLY(String url, final ImageView imageView) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .imageScaleType(ImageScaleType.EXACTLY).build();
        imageLoader.displayImage(url, imageView, option);
    }

    /**
     * 在img中绘制url对应图片的模糊处理图像
     * FIXME: 此方法没有缓存， 计算量大， 请慎重考虑。
     */
    public static void desplayBlurImage(String url, ImageView img) {
        DisplayImageOptions option = new DisplayImageOptions.Builder().cloneFrom(options)
                .displayer(new BlurBitmapDisplayer())
                .build();
        imageLoader.displayImage(url, img, option);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    public static Bitmap getBitMap(String url) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Bitmap bitmap = imageLoader.loadImageSync(url);
        return bitmap;
    }

    /**
     * 在img中绘制bitmap对应图片的模糊处理图像
     *
     * @param bitmap
     * @param img
     */
    public static void desplayBlurBitmat(Bitmap bitmap, ImageView img) {
        if (bitmap != null) {
            Drawable dra = BlurFilter.BoxBlurFilter(bitmap);
            if (dra != null) {
                img.setImageDrawable(dra);
            }
        }
    }

    /**
     * 将图片截取为圆角图片
     *
     * @param bitmap 原图�?
     * @param ratio  截取比例，如果是8，则圆角半径是宽高的1/8，如果是2，则是圆形图�?
     * @return 圆角矩形图片
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float ratio) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio,
                bitmap.getHeight() / ratio, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    /**
     * Drawable转为BitMap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bm = bd.getBitmap();
        return bm;
    }

    /**
     * Drawable转为BitMap
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        return bd;
    }

    public static Drawable createDrawableSelector(Context context, int checkId, int uncheckId) {
        StateListDrawable stateDrawable = new StateListDrawable();
        Drawable checkDrawable = context.getResources().getDrawable(checkId);
        Drawable uncheckDrawable = context.getResources().getDrawable(uncheckId);

        int[][] states = new int[4][];
        states[0] = new int[]{android.R.attr.state_checked};
        states[1] = new int[]{-android.R.attr.state_checked};

        stateDrawable.addState(states[0], checkDrawable);
        stateDrawable.addState(states[1], uncheckDrawable);

        return stateDrawable;
    }

    public static ColorStateList createColorStateList(Context context, int checkId, int unCheckedId) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked},
                },
                new int[]{
                        ContextCompat.getColor(context, checkId),
                        ContextCompat.getColor(context, unCheckedId),
                });
    }
}
