package com.travel.app;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.ctsmedia.hltravel.BuildConfig;
import com.tencent.qcloud.suixinbo.QavsdkApplication;
import com.tencent.qcloud.suixinbo.presenters.InitBusinessHelper;
import com.tencent.qcloud.suixinbo.utils.SxbLogImpl;
import com.travel.Constants;
import com.travel.lib.helper.OSSHelper;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.map.utils.LocationTools;
import com.travel.utils.ErrorReporter;

/*import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;*/

public class TravelApp extends MultiDexApplication {
    private static final String TAG = "TravelApp";

    public static Context appContext;
    // 全局异常捕获, 彻底杀死应用
    private ErrorReporter mErrorReporter;
/*	private RefWatcher mRefRefWatcher;

	public static RefWatcher getRefWatcher(Context context){
		return ((TravelApp)context.getApplicationContext()).mRefRefWatcher;
	}*/

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        com.travel.lib.TravelApp.appContext = appContext;
        ImageDisplayTools.initImageLoader(appContext);
        mErrorReporter = ErrorReporter.getInstance(appContext);
        //		mRefRefWatcher = LeakCanary.install(this);
        // 初始化http地址
        Constants.init(BuildConfig.Root_Url, BuildConfig.Chat_IP, BuildConfig.OSS_ROOT_URL, BuildConfig.Root_Url_ShareStory);
        // 初始化腾讯云的Sdkid等
        com.tencent.qcloud.suixinbo.utils.Constants.initAppIdAndType(BuildConfig.SDK_APPID, BuildConfig.ACCOUNT_TYPE);

        QavsdkApplication.app = this;
        QavsdkApplication.context = getApplicationContext();

        SxbLogImpl.init(getApplicationContext());

        //初始化APP
        InitBusinessHelper.initApp(getApplicationContext());
        OSSHelper.OSS_ROOT_URL = BuildConfig.OSS_ROOT_URL;
        MLog.d(TAG, "application onCreate");

        // 初始化定位
        LocationTools.getInstans().init();
        //android 7.0系统解决拍照的问题
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MLog.d(TAG, "application onTerminate");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
