package com.travel.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.ctsmedia.hltravel.BuildConfig;
import com.ctsmedia.hltravel.R;
import com.huawei.android.pushagent.api.PushManager;
import com.tencent.TIMManager;
import com.tencent.TIMOfflinePushSettings;
import com.tencent.TIMOfflinePushToken;
import com.travel.app.TravelApp;

import java.util.Locale;

/**
 * Created by Administrator on 2016/12/15.
 */

public class IMPushHelper {
    private static final String TAG = "IMPushHelper";
    private Context context;
    private static IMPushHelper instance;

    private IMPushHelper(Context context) {
        this.context = context;
    }

    public static IMPushHelper getInstance(){
        if (instance == null) instance = new IMPushHelper(TravelApp.appContext);
        return instance;
    }

    public static void init(Context context){
        if(instance != null){
            Log.e(TAG, "IMManager has been inited");
        }else{
            instance = new IMPushHelper(context.getApplicationContext());
        }
    }

    /**
     * 初始化离线推送配置，需登录后设置才生效
     */
    public void initConfigOfflinePush(){
        TIMOfflinePushSettings settings = new TIMOfflinePushSettings();
        //开启离线推送
        settings.setEnabled(true);
        //设置收到C2C离线消息时的提示声音，这里把声音文件放到了res/raw文件夹下
        settings.setC2cMsgRemindSound(Uri.parse("android.resource://" + "com.travel.lib" + "/" + R.raw.audio));
        //设置收到群离线消息时的提示声音，这里把声音文件放到了res/raw文件夹下
        settings.setGroupMsgRemindSound(Uri.parse("android.resource://" + "com.travel.lib" + "/" + R.raw.beep));

        TIMManager.getInstance().configOfflinePushSettings(settings);
    }

    public void registerPush(){
        String vendor = Build.MANUFACTURER;
        if(vendor.toLowerCase(Locale.ENGLISH).contains("xiaomi")) {
            //注册小米推送服务
//            MiPushClient.registerPush(this, MIPUSH_APPID, MIPUSH_APPKEY);
        }else if(vendor.toLowerCase(Locale.ENGLISH).contains("huawei")) {
            //请求华为推送设备token
            PushManager.requestToken(context);
        }
    }

    /**
     * 登录成功后上报证书ID和设备token
     * @param token   设备token
     */
    public static void upHwXmPushToken(String token){
        TIMOfflinePushToken param = new TIMOfflinePushToken();
        param.setToken(token);
        param.setBussid(BuildConfig.BUSSID_HW); //证书ID
        TIMManager.getInstance().setOfflinePushToken(param);
    }

}
