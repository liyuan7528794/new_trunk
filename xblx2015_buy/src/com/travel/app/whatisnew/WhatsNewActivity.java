package com.travel.app.whatisnew;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.ctsmedia.hltravel.R;
import com.travel.activity.HomeActivity;
import com.travel.adapter.GuideHolderView;
import com.travel.imserver.IMManager;
import com.travel.lib.TravelApp;
import com.travel.lib.helper.FileLog;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.utils.UpdateHelper;
import com.travel.utils.UpdateHelper.UpdateHelperListener;
import com.travel.widget.UpdateDialog;
import com.travel.widget.UpdateDialog.UpdateDialogListener;

import java.util.ArrayList;
import java.util.List;

public class WhatsNewActivity extends Activity implements OnViewChangeListener, UpdateHelperListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "WhatsNewActivity";

    private MyScrollLayout mScrollLayout;
    private ImageView[] imgs;
    private int count;
    private int currentItem;
    private LinearLayout pointLLayout;
    private RelativeLayout scrollRelayout;
    private ImageView[] welcomes = new ImageView[2];


    private UpdateHelper updateHelper;
    private ProgressDialog mProgressDialog;
    private boolean isLastScreenPict = false;
    private int mUpdateType = -1;
    private String mUpdateTime = null;
    private String mContent = null;
    private String mVersion = null;
    private String mUpdateUrl = null;

    private boolean mIsNotFirstTime;
    private boolean mIsFistInstill = false;

    // 可自动滑动的引导页
    private ConvenientBanner cb_guide;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.whatisnew_main);
        updateHelper = new UpdateHelper();
        updateHelper.setListener(this);
        getVersionData();
        initView();
        IMManager.init(this);
        System.out.println("版本号：" + OSUtil.getVersionCode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateHelper.onPause();
    }

    private void loadWelcomeBitmap() {
        for (int i = 0; i < welcomes.length; i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Resources resources = getResources();
            int resourId = getId(i);
            BitmapFactory.decodeResource(resources, resourId, options);
            int widthRadio = (int) (options.outWidth / getScreenWidth() + 0.5);
            int heightRadio = (int) (options.outHeight / getScreenHeight() + 0.5);
            int radio = Math.max(1, Math.max(widthRadio, heightRadio));
            options.inJustDecodeBounds = false;
            options.inSampleSize = radio;

            welcomes[i].setImageBitmap(BitmapFactory.decodeResource(resources, resourId, options));
        }
    }

    public int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        return screenW;
    }

    public int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        return screenW;
    }

    private int getId(int index) {
        switch (index) {
            case 0:
                return R.drawable.welcomebackgroud01;
            case 1:
                return R.drawable.welcomebackgroud02;
//		case 2:
//			return R.drawable.welcomebackgroud03;
//		case 3:
//			return R.drawable.welcome;
            default:
                throw new IllegalStateException("index is wrong, and index is " + index);
        }
    }

    /**
     * 0表示是更新的
     * 1表示是第二次进入
     */
    private void showWelcome() {
        scrollRelayout.setVisibility(View.GONE);
        if (mIsNotFirstTime && !mIsFistInstill) {
/*			if(mUpdateType != -1
                    && mUpdateType != UpdateHelper.TYPE_OPTIONAL_UPDATE
					&& mUpdateType != UpdateHelper.TYPE_MUST_UPDATE){
				gotoMainActivity();
			}*/
        } else {

            count = mScrollLayout.getChildCount();
            imgs = new ImageView[count];
            for (int i = 0; i < count; i++) {
                imgs[i] = (ImageView) pointLLayout.getChildAt(i);
                imgs[i].setEnabled(true);
                imgs[i].setTag(i);
            }
            currentItem = 0;
            imgs[currentItem].setEnabled(false);
            mScrollLayout.SetOnViewChangeListener(this);
            loadWelcomeBitmap();
        }
    }

    private void getVersionData() {
        String mCurrentVersionCode = OSUtil.getVersionCode() + "";
        SharedPreferences preferences = getSharedPreferences("app", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        String versionCode = preferences.getString("version_code", "");
//		mIsFistInstill = TextUtils.isEmpty(versionCode);
        mIsFistInstill = true;
        mIsNotFirstTime = false;
//		mIsNotFirstTime = !TextUtils.isEmpty(versionCode) && versionCode.equals(mCurrentVersionCode);
        if (!mIsNotFirstTime) {
            editor.putString("version_code", mCurrentVersionCode);
            editor.commit();
            getIMEIAndIMSI();
        }


    }

    private void gotoMainActivity(long time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoMainWithoutDelay();
            }
        }, time);
    }

    private void gotoMainWithoutDelay() {
        Intent intent = new Intent();
        intent.setClass(WhatsNewActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        scrollRelayout = (RelativeLayout) findViewById(R.id.scrollRelayout);
        mScrollLayout = (MyScrollLayout) findViewById(R.id.whatisnew_ScrollLayout);
        pointLLayout = (LinearLayout) findViewById(R.id.whatisnew_llayout_point);
        welcomes[0] = (ImageView) findViewById(R.id.welcome_1);
        welcomes[1] = (ImageView) findViewById(R.id.welcome_2);
//		welcomes[2] = (ImageView) findViewById(R.id.welcome_3);
        showWelcome();

        final ArrayList<Integer> imgs = new ArrayList<>();
        imgs.add(R.drawable.welcomebackgroud01);
        imgs.add(R.drawable.welcomebackgroud02);
        cb_guide = (ConvenientBanner) findViewById(R.id.cb_guide);
        cb_guide.setCanLoop(false);
        cb_guide.setPages(new CBViewHolderCreator() {
            @Override
            public Object createHolder() {
                return new GuideHolderView();
            }
        }, imgs)
                .setPointViewVisible(false);
        cb_guide.getViewPager().setCanScroll(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cb_guide.setcurrentitem(1);
            }
        }, 1500);

        cb_guide.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == imgs.size() - 1) {
                    gotoMainActivity(1500);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void OnViewChange(int position) {
        setcurrentPoint(position);
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < welcomes.length; i++) {
            Drawable drawable = welcomes[i].getDrawable();
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).getBitmap().recycle();
                welcomes[i].setImageBitmap(null);
            }
        }
        super.onDestroy();
    }

    //设置当前的位置
    private void setcurrentPoint(int position) {
        boolean isGotoMain = position >= (count - 1);
        if (isGotoMain) {
            if (!mIsNotFirstTime) gotoMainActivity(0);
            return;
        }
        imgs[currentItem].setEnabled(true);
        imgs[position].setEnabled(false);
        currentItem = position;
    }

    @Override
    public void onNetResult(final int resultType, String time, String content, final String url, String version) {
        MLog.v("WhatsNewActivity", "onNetResult, and resultType is " + resultType + ", and url is " + url);
        if (!isLastScreenPict) {
            mUpdateType = resultType;
            mUpdateTime = time;
            mContent = content;
            mVersion = version;
            mUpdateUrl = url;
//			return;
        }
        switch (resultType) {
            case UpdateHelper.TYPE_NET_FAILED:
                // 网络失败
                Toast.makeText(this, "网络连接不可用 ", Toast.LENGTH_SHORT).show();
                //TODO: 测试而已, 暂且如此
                gotoMainActivity(2000);
                break;
            case UpdateHelper.TYPE_MUST_UPDATE:
                // 必须更新
            case UpdateHelper.TYPE_OPTIONAL_UPDATE:
                // 可选更新
                UpdateDialog updateDialog = new UpdateDialog(this, null, content, new UpdateDialogListener() {

                    @Override
                    public void onUpdateNowClick() {
                        updateHelper.startDownloadNewApk(url);
                        showProgressDialog();
                    }

                    @Override
                    public void onCloseClick() {
                        if (resultType == UpdateHelper.TYPE_MUST_UPDATE) {
                            closeApp();
                        } else {
                            gotoMainWithoutDelay();
                        }
                    }
                });
                updateDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MLog.v(TAG, "onCancel");
                    }
                });
                updateDialog.show();
//			gotoMainWithoutDelay();
                break;
            case UpdateHelper.TYPE_NO_UPDATE:
                // 没有更新
                if (!mIsNotFirstTime || mIsFistInstill) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            scrollRelayout.setVisibility(View.VISIBLE);
                        }
                    }, 0);//TODO:原来是2000
                } else {
                    gotoMainActivity(2000);
                }
                break;
            default:
                if (mIsNotFirstTime) {
                    gotoMainActivity(2000);
                }
                break;
        }
    }

    private void closeApp() {
        finish();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("正在下载新应用");
        mProgressDialog.setMax(100);
        mProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                closeApp();
            }
        });
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    @Override
    public void onDownloadProgressChanged(int progress) {
        MLog.v(TAG, "onDownloadProgressChanged, and progress is " + progress);
        if (mProgressDialog == null || !mProgressDialog.isShowing()) return;
        mProgressDialog.setProgress(progress);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1) return;
        SharedPreferences preferences = getSharedPreferences("app", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if (TextUtils.equals(Manifest.permission.READ_PHONE_STATE, permissions[0])
                && PackageManager.PERMISSION_DENIED == grantResults[0]) {
            editor.putString("IMEI", "");
            editor.putString("IMSI", "");
        } else {
            String imei = "";
            String imsi = "";
            TelephonyManager telephonyManager = (TelephonyManager) TravelApp.appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
                    imei += telephonyManager.getDeviceId(slot) + ",";
                }
            } else {
                imei = telephonyManager.getDeviceId();
            }
            imsi = telephonyManager.getSubscriberId();
            editor.putString("IMEI", imei);
            editor.putString("IMSI", imsi);
        }
        editor.commit();
        updateHelper.startNetRequest(this);
    }

    /**
     * 国际移动身份识别码和国际移动用户识别码
     *
     * @return
     */
    public void getIMEIAndIMSI() {
        String imei = "";
        String imsi = "";
        // 判断所必须的权限 获取IMSI和IMEI
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) TravelApp.appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
                    imei += telephonyManager.getDeviceId(slot) + ",";
                }
            } else {
                imei = telephonyManager.getDeviceId();
            }
            imsi = telephonyManager.getSubscriberId();
            SharedPreferences preferences = getSharedPreferences("app", Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putString("IMEI", imei);
            editor.putString("IMSI", imsi);
            editor.commit();
            updateHelper.startNetRequest(this);
        }

    }

}