package com.travel.lib.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.Constants;
import com.travel.lib.R;
import com.travel.lib.TravelApp;
import com.travel.lib.ui.SystemBarTintManager;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 提供系统级的功能, 目前提供的功能包 - dp2px - px2dp
 */
public class OSUtil {

    public static int dp2px(Context context, float dpValue) {
        final float scale = getDisplayMetrics(context).density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = getDisplayMetrics(context).density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getScreenWidth() {
        return getDisplayMetrics(TravelApp.appContext).widthPixels;
    }

    public static int getScreenHeight() {
        return getDisplayMetrics(TravelApp.appContext).heightPixels;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     * 生成随机数 [0,1.0)
     *
     * @return
     */
    public static float createRandom() {
        Random random = new Random();
        return random.nextFloat();
    }

    /**
     * 获取手机型号
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * 获取系统版本号
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取客户端本地ip
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;

    }

    /**
     * 显示软键盘
     */
    public static void showKeyboard(Activity context) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInputFromInputMethod(context.getCurrentFocus().getWindowToken(), 0);
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideKeyboard(Activity context) {
        if (context.getCurrentFocus() == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        // inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(Activity context, EditText edit) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInputFromInputMethod(edit.getApplicationWindowToken(), 0);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboardPopWindow(Activity context) {
        if (context.getCurrentFocus() == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
    }

    public static void hideKeyboardPopWindow(Activity context, EditText editText) {
        if (editText == null)
            return;
        InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
//        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 裁剪图片
     *
     * @param uri     uri
     * @param aspectX x比例
     * @param aspectY y比例
     * @param outFile 裁剪后的位置file， 必须保证存在
     * @param outputX 输出x大小
     * @param outputY 输出Y大小
     */
    public static Intent getPerformCrop(Uri uri, int aspectX, int aspectY, Uri outFile, int outputX, int outputY) {
        MLog.v("OSUtil", "performCrop");
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", aspectX);
        cropIntent.putExtra("aspectY", aspectY);
        cropIntent.putExtra("outputX", outputX);
        cropIntent.putExtra("outputY", outputY);
        cropIntent.putExtra("scaleUpIfNeeded", true);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outFile);
        return cropIntent;
    }

    private static boolean isWait = false;

    /**
     * 一键分享
     */
    public static void showShare(String platform, String title, final String text, String imageUrl, final String url, final String copyUrl,
                                 Context context) {
        showShare(platform, title, text, imageUrl, url, context);
    }

    /**
     * 一键分享
     */
    public static void showShare(String platform, String title, final String text, String imageUrl, final String url,
                                 Context context) {

        if (isWait) {
            isWait = false;
            return;
        }
        isWait = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    isWait = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        OnekeyShare oks = new OnekeyShare();
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
        // oks.setNotification(R.drawable.ic_launcher,
        // getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(title);
//        oks.setTitle(context.getResources().getString(R.string.app_name));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {

            @Override
            public void onShare(Platform platform, ShareParams paramsToShare) {
//                if (SinaWeibo.NAME.equals(platform.getName()))
//                    paramsToShare.setText("链接地址：" + copyUrl);
//                else
                paramsToShare.setText(text);
            }
        });
        oks.setImageUrl(imageUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(context.getResources().getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);

//        // 构造一个图标
//        Bitmap enableLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_copy);
//        String label = "复制到剪贴板";
//        OnClickListener listener = new OnClickListener() {
//            @SuppressWarnings("deprecation")
//            @SuppressLint("NewApi")
//            public void onClick(View v) {
//                copy(copyUrl);
//            }
//        };
//        oks.setCustomerLogo(enableLogo, label, listener);

        if (platform != null) {
            oks.setPlatform(platform);
        }

        // 启动分享GUI
        oks.show(context);
    }

    public static void copy(String content) {
        ClipboardManager cmb = (ClipboardManager) TravelApp.appContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content);
    }

    /**
     * QQ分享
     */
    public static void ShareToQQ(String title, String titleUrl, String text, String imageUrl, String url,
                                 String siteUrl) {
        QQ.ShareParams qq = new QQ.ShareParams();
        qq.setTitle(title);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        qq.setTitleUrl(titleUrl);
        // text是分享文本，所有平台都需要这个字段
        qq.setText(text);
        qq.setImageUrl(imageUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        qq.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        qq.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        qq.setSite(TravelApp.appContext.getResources().getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        qq.setSiteUrl(siteUrl);
        Platform qqq = ShareSDK.getPlatform(QQ.NAME);
        qqq.setPlatformActionListener(null);
        qqq.share(qq);
    }

    /**
     * QQ空间分享
     */
    public static void ShareToQZone(String title, String titleUrl, String text, String imageUrl, String url,
                                    String siteUrl) {
        QZone.ShareParams qq = new QZone.ShareParams();
        qq.setTitle(title);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        qq.setTitleUrl(titleUrl);
        // text是分享文本，所有平台都需要这个字段
        qq.setText(text);
        qq.setImageUrl(imageUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        // qq.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        qq.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        qq.setSite(TravelApp.appContext.getResources().getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        qq.setSiteUrl(siteUrl);
        Platform qqq = ShareSDK.getPlatform(QZone.NAME);
        qqq.setPlatformActionListener(null);
        qqq.share(qq);
    }

    /**
     * 微信分享
     */
    public static void ShareToWechat(String title, String titleUrl, String text, String imageUrl, String url,
                                     String siteUrl) {
        Wechat.ShareParams qq = new Wechat.ShareParams();
        qq.setTitle(title);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        qq.setTitleUrl(titleUrl);
        // text是分享文本，所有平台都需要这个字段
        qq.setText(text);
        qq.setImageUrl(imageUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        qq.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        qq.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        qq.setSite(TravelApp.appContext.getResources().getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        qq.setSiteUrl(siteUrl);
        Platform qqq = ShareSDK.getPlatform(Wechat.NAME);
        qqq.setPlatformActionListener(null);
        qqq.share(qq);
    }

    /**
     * 微信朋友圈分享
     */
    public static void ShareToWechatMoments(String title, String titleUrl, String text, String imageUrl, String url,
                                            String siteUrl) {
        WechatMoments.ShareParams qq = new WechatMoments.ShareParams();
        qq.setTitle(title);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        qq.setTitleUrl(titleUrl);
        // text是分享文本，所有平台都需要这个字段
        qq.setText(text);
        qq.setImageUrl(imageUrl);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        qq.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        qq.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        qq.setSite(TravelApp.appContext.getResources().getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        qq.setSiteUrl(siteUrl);
        Platform qqq = ShareSDK.getPlatform(WechatMoments.NAME);
        qqq.setPlatformActionListener(null);
        qqq.share(qq);
    }

//    /**
//     * 新浪微博分享
//     */
//    public static void ShareToSinaWeibo(String title, String titleUrl, String text, String imageUrl, String url,
//                                        String siteUrl) {
//        SinaWeibo.ShareParams qq = new SinaWeibo.ShareParams();
//        qq.setTitle(title);
//        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
//        qq.setTitleUrl(titleUrl);
//        // text是分享文本，所有平台都需要这个字段
//        qq.setText(text);
//        qq.setImageUrl(imageUrl);
//        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
//        // url仅在微信（包括好友和朋友圈）中使用
//        qq.setUrl(url);
//        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//        qq.setComment("我是测试评论文本");
//        // site是分享此内容的网站名称，仅在QQ空间使用
//        qq.setSite(TravelApp.appContext.getResources().getString(R.string.app_name));
//        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
//        qq.setSiteUrl(siteUrl);
//        Platform qqq = ShareSDK.getPlatform(TravelApp.appContext, SinaWeibo.NAME);
//        qqq.setPlatformActionListener(null);
//        qqq.share(qq);
//    }

    /**
     * 判断是否是游客
     *
     * @param userId
     * @return 返回true是游客，false是登录用户
     */
    public static boolean isVisitor(String userId) {
        if (userId.length() > 7) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是不是数字
     */
    public static boolean isNum(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 显示状态栏与否
     *
     * @param context
     * @param isEnable
     */
    public static void enableStatusBar(Activity context, boolean isEnable) {
        // 改变状态栏颜色代码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(context, isEnable);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(context);
        tintManager.setStatusBarTintEnabled(isEnable);
        if (isEnable) {
            tintManager.setStatusBarTintResource(Color.TRANSPARENT);
        } else {
            MLog.v("OSUTIL", "isEnable is false");
            //			// tintManager.setStatusBarTintResource(android.R.color.transparent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getSystem7Translant(context);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = context.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.black_3));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setMiuiStatusBarDarkMode(context, true);
        setMeizuStatusBarDarkIcon(context, true);
    }

    /**
     * 状态栏改变颜色用到
     */
    @TargetApi(19)
    private static void setTranslucentStatus(Activity context, boolean on) {
        Window win = context.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    private static void getSystem7Translant(Activity activity) {
        try {
            Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
            Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
            field.setAccessible(true);
            field.setInt(activity.getWindow().getDecorView(), Color.TRANSPARENT);  //改为透明
        } catch (Exception e) {
        }
    }

    public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //设置成白色的背景，字体颜色为黑色。
    public static boolean setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        boolean result = false;
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * 底部虚拟按键导航栏的高度
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getSoftButtonsBarHeight(Activity mActivity) {
        DisplayMetrics metrics = new DisplayMetrics();
        //这个方法获取可能不是真实屏幕的高度
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        //获取当前屏幕的真实高度
        mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    /**
     * 获取状态栏的高
     */
    public static int getStatusHeight(Context context) {
        int y = 0;
        try {
            Class c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            y = context.getResources().getDimensionPixelSize(x);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return y;
    }

    /**
     * 动态设置是否全屏
     *
     * @param activity
     * @param isFullScreen true 全屏
     */
    public static void setFullScreen(Activity activity, boolean isFullScreen) {
        if (isFullScreen) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(lp);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attr);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }


    /**
     * 提示音
     */
    public static void audioNotify(final Context context, final int type) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                int resAudio;// 震动铃声
                int duration;// 震动时长
                if (type == 1) {
                    resAudio = R.raw.beep;
                    duration = 200;
                } else {
                    resAudio = R.raw.audio;
                    duration = 1000;
                }

                // AudioManager audio =
                // (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                // if(AudioManager.RINGER_MODE_SILENT != audio.getRingerMode()){
                MediaPlayer mPlayer = MediaPlayer.create(context, resAudio);
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();// 释放资源。让资源得到释放;
                    }
                });
                mPlayer.start();
                vibrate(context, duration);
            }
        }).start();
    }

    public static void vibrate(Context context, long duration) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    }

    /**
     * 设置分享按钮的参数
     *
     * @param v
     */
    public static void setShareParam(View v, String tag, Context context) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        if (!"share".equals(tag))
            params.rightMargin = OSUtil.dp2px(context, 10);
        params.rightMargin = OSUtil.dp2px(context, 14);
        v.setLayoutParams(params);
        v.setVisibility(View.VISIBLE);
        if ("share".equals(tag))
            ((ImageView) v).setImageResource(isDayTheme() ? R.drawable.icon_goods_share_day : R.drawable.icon_story_share_night);
        else if ("groupChat".equals(tag)) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.icon_entry_group);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            ((TextView) v).setCompoundDrawables(drawable, null, null, null);
            ((TextView) v).setText("加入群聊");
            ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.black_3));
            ((TextView) v).setTextSize(13);
        } else if (TextUtils.equals("sure", tag))
            ((TextView) v).setText("确定");
        else if (TextUtils.equals("refresh", tag)) {
            ((TextView) v).setText("刷新");
            ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.black_6));
        } else if (TextUtils.equals("alter", tag)) {
            ((TextView) v).setText("修改");
            ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.black_6));
        } else if (TextUtils.equals("save", tag)) {
            ((TextView) v).setText("保存");
            ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.black_6));
        } else if (TextUtils.equals("scan", tag)) {
            ((ImageView) v).setImageResource(R.drawable.saomiao);
        }
    }

    /**
     * 是否是白天模式
     *
     * @return
     */
    public static boolean isDayTheme() {
        SharedPreferences sp = TravelApp.appContext.getSharedPreferences("appTheme", Context.MODE_PRIVATE);
        if (0 == sp.getInt("theme", 0))
            return true;
        else
            return false;
    }

    /**
     * 保存切换的状态
     *
     * @param isDay 0表示白天，1表示黑夜
     */
    public static void saveThemeStatus(boolean isDay) {
        SharedPreferences sp = TravelApp.appContext.getSharedPreferences("appTheme", Context.MODE_PRIVATE);
        if (isDay)
            sp.edit().putInt("theme", 0).commit();
        else
            sp.edit().putInt("theme", 1).commit();

    }

    /**
     * 将view'生成Bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap layoutToBitmap(View view) {

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.buildDrawingCache();

        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    public static void intentLogin(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.travel.login");
        context.startActivity(intent);
    }

    /**
     * 订单支付成功
     *
     * @param context
     * @param ordersId
     */
    public static void intentOrderSuccess(Context context, long ordersId) {
        Intent intent = new Intent();
        intent.putExtra("ordersId", ordersId);
        intent.setAction(Constants.ACTION_ORDER_SUCCESS);
        context.startActivity(intent);
    }

    /**
     * 订单支付失败或取消支付
     *
     * @param context
     * @param ordersId
     */
    public static void intentOrderInfo(Context context, long ordersId) {
        Intent intent = new Intent();
        intent.putExtra("ordersId", ordersId);
        intent.setAction(Constants.ACTION_ORDER_FAIL);
        context.startActivity(intent);
    }

    /**
     * 跳转到行程安排页面
     *
     * @param context
     * @param key     intent传值获取字段
     * @param goodsId
     */
    public static void intentPlan(Context context, String key, String goodsId) {
        Intent intent = new Intent();
        intent.putExtra(key, goodsId);
        intent.setAction(Constants.ACTION_ROUTE);
        context.startActivity(intent);
    }

//    /**
//     * 商家端跳转到行程安排页面
//     *
//     * @param context
//     * @param key     intent传值获取字段
//     * @param obj
//     */
//    public static void intentPlan(Context context, String key, Serializable obj) {
//        Intent intent = new Intent();
//        intent.putExtra(key, obj);
//        intent.setAction(Constants.ACTION_ROUTE_SETTING);
//        context.startActivity(intent);
//    }

    /**
     * 获取点赞数 多于10000-->1万+
     *
     * @return
     */
    public static String getLikeCount(int likeCount) {
        String result = "";
        int temp = likeCount / 10000;
        if (temp == 0) {
            result = likeCount + "";
        } else if (temp > 0) {
            if (likeCount % 10000 == 0) {
                result = temp + "万";
            } else {
                result = temp + "万+";

            }
        }
        return result;
    }

    /**
     * Mac地址
     *
     * @return
     */
    public static String getMAC() {

        String strMac = null;
        Context context = TravelApp.appContext;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            strMac = getLocalMacAddressFromWifiInfo(context);
            return strMac;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            strMac = getMacAddress(context);
            return strMac;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            strMac = getMachineHardwareAddress();
            return strMac;
        }

        return "02:00:00:00:00:00";
    }

    /**
     * 根据wifi信息获取本地mac
     *
     * @param context
     * @return
     */
    public static String getLocalMacAddressFromWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String mac = winfo.getMacAddress();
        return mac;
    }

    /**
     * android 6.0及以上、7.0以下 获取mac地址
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {

        // 如果是6.0以下，直接通过wifimanager获取
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            String macAddress0 = getMacAddress0(context);
            if (!TextUtils.isEmpty(macAddress0)) {
                return macAddress0;
            }
        }

        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e("----->" + "NetInfoManager", "getMacAddress:" + ex.toString());
        }
        if (macSerial == null || "".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("----->" + "NetInfoManager",
                        "getMacAddress:" + e.toString());
            }

        }
        return macSerial;
    }

    private static String getMacAddress0(Context context) {
        if (isAccessWifiStateAuthorized(context)) {
            WifiManager wifiMgr = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = null;
            try {
                wifiInfo = wifiMgr.getConnectionInfo();
                return wifiInfo.getMacAddress();
            } catch (Exception e) {
                Log.e("----->" + "NetInfoManager",
                        "getMacAddress0:" + e.toString());
            }

        }
        return "";

    }

    /**
     * Check whether accessing wifi state is permitted
     *
     * @param context
     * @return
     */
    private static boolean isAccessWifiStateAuthorized(Context context) {
        if (PackageManager.PERMISSION_GRANTED == context
                .checkCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE")) {
            Log.e("----->" + "NetInfoManager", "isAccessWifiStateAuthorized:"
                    + "access wifi state is enabled");
            return true;
        } else
            return false;
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    /**
     * android 7.0及以上 扫描各个网络接口获取mac地址
     *
     */
    /**
     * 获取设备HardwareAddress地址
     *
     * @return
     */
    public static String getMachineHardwareAddress() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String hardWareAddress = null;
        NetworkInterface iF = null;
        if (interfaces == null) {
            return null;
        }
        while (interfaces.hasMoreElements()) {
            iF = interfaces.nextElement();
            try {
                hardWareAddress = bytesToString(iF.getHardwareAddress());
                if (hardWareAddress != null)
                    break;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return hardWareAddress;
    }

    /***
     * byte转为String
     *
     * @param bytes
     * @return
     */
    private static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }

        return buf.toString();
    }

    public static int getVersionCode() {
        PackageInfo packageInfo = getPackInfo(TravelApp.appContext);
        if (packageInfo == null) {
            return 1;
        }
        return packageInfo.versionCode;
    }

    public static String getVersionName() {
        PackageInfo packageInfo = getPackInfo(TravelApp.appContext);
        if (packageInfo == null) {
            return null;
        }
        return packageInfo.versionName;
    }

    private static PackageInfo getPackInfo(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            return packageInfo;
        } catch (PackageManager.NameNotFoundException e) {
            MLog.e("OsUtil", e.getMessage());
            return null;
        }
    }

    public static String getIMEI() {
        SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences("app", Context.MODE_PRIVATE);
        return sharedPreferences.getString("IMEI", "");
    }

    public static String getIMSI() {
        SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences("app", Context.MODE_PRIVATE);
        return sharedPreferences.getString("IMSI", "");
    }


}
