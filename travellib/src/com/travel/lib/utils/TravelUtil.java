package com.travel.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.travel.activity.OneFragmentActivity;
import com.travel.layout.DialogTemplet;
import com.travel.layout.VideoViewFragment;
import com.travel.lib.R;
import com.travel.lib.TravelApp;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 整个项目的工具类
 * Created by wyp on 2017/3/2.
 */

public class TravelUtil {

    /**
     * 不带动画的启动一个activity
     *
     * @param context
     * @param activity
     * @param bundle
     */
    public static void launchActivity(Context context, Class<?> activity, Bundle bundle) {
        Intent intent = new Intent(context, activity);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    /**
     * 设置FrameLayout中view的等分宽,并且宽高比为 x : y
     *
     * @param v
     * @param part   分成几等分
     * @param margin 边距
     * @param x      宽比例
     * @param y      高比例
     */
    public static void setFLParamsWidthPart(View v, int part, int margin, int x, int y) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, margin)) / part;
        params.height = params.width * y / x;
        v.setLayoutParams(params);
    }

    /**
     * 设置LinearLayout中view的等分宽,并且宽高比为 x : y
     *
     * @param v
     * @param part   分成几等分
     * @param margin 边距
     * @param x      宽比例
     * @param y      高比例
     */
    public static void setLLParamsWidthPart(View v, int part, int margin, int x, int y) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(TravelApp.appContext, margin)) / part;
        params.height = params.width * y / x;
        v.setLayoutParams(params);
    }

    /**
     * 显示toast
     *
     * @param content
     */
    public static void showToast(String content) {
        Toast.makeText(TravelApp.appContext, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示toast
     *
     * @param id
     */
    public static void showToast(int id, Context context) {
        Toast.makeText(context, context.getResources().getString(id), Toast.LENGTH_SHORT).show();
    }

    // 返回是否有SD卡
    public static boolean GetSDState() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 返回SD卡存储路径
     *
     * @param subFile 子文件的名称
     * @return
     */
    public static String getFileAddress(String subFile, Context mContext) {

        String address = "";

        if (TravelUtil.GetSDState()) {
            address = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            address = Environment.getDataDirectory().getAbsolutePath();
        }
        address += File.separator + mContext.getResources().getString(R.string.app_name) + File.separator + subFile;
        File baseFile = new File(address);
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        return address;
    }

    public static String getIPAddress() {
        Context context = TravelApp.appContext;
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 将对象转成json字符串
     *
     * @param object
     * @return json字符串
     */
    public static String objectToJson(Object object) {
        Gson gson = new Gson();
        String jsonString = "";
        try {
            jsonString = gson.toJson(object, object.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static ColorMatrixColorFilter getColorFilter(Context context) {
        int mShadeColor = ContextCompat.getColor(context, R.color.black_alpha35);
        float r = Color.alpha(mShadeColor) / 255f;
        r=r-(1 - r)*0.15f;
        float rr = (1 - r)*1.15f;
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[]{
                rr, 0, 0, 0, Color.red(mShadeColor) * r,
                0, rr, 0, 0, Color.green(mShadeColor) * r,
                0, 0, rr, 0, Color.blue(mShadeColor) * r,
                0, 0, 0, rr, 0});
        return new ColorMatrixColorFilter(colorMatrix);
    }

    /**
     * 判断当前是否是自己的主页
     * @param userId
     * @return
     */
    public static boolean isHomePager(String userId) {
        if (UserSharedPreference.isLogin()) {
            if (TextUtils.equals(UserSharedPreference.getUserId(), userId)) {
                return true;
            }
        }
        return false;
    }

    public static void goPlay(final Context mContext, final Bundle bundle){
        String netType = CheckNetStatus.checkNetworkConnection();
        if (CheckNetStatus.wifiNetwork.equals(netType)) {
            OneFragmentActivity.startNewActivity(mContext, "", VideoViewFragment.class, bundle);
        }

        if (CheckNetStatus.unNetwork.equals(netType)) {// 没网
            TravelUtil.showToast("当前无网络，请检查网络！");
        }

        if (!CheckNetStatus.unNetwork.equals(netType) && !CheckNetStatus.wifiNetwork.equals(netType)) {
            if ("UNKNOWN".equals(netType)) {
                TravelUtil.showToast("当前无网络，请检查网络！");
            }
            //弹框提醒
            final DialogTemplet dialog = AlertDialogUtils.getNetStatusDialog(netType, mContext);
            dialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {

                @Override
                public void leftClick(View view) {
                    OneFragmentActivity.startNewActivity(mContext, "", VideoViewFragment.class, bundle);
                    dialog.dismiss();
                }
            });
            dialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {

                @Override
                public void rightClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

}
