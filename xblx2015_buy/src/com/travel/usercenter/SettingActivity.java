package com.travel.usercenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.BuildConfig;
import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.layout.CornerDialog;
import com.travel.lib.TravelApp;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.DataCleanManager;
import com.travel.lib.utils.OSUtil;
import com.travel.utils.HLLXLoginHelper;
import com.travel.utils.UpdateHelper;
import com.travel.utils.UpdateHelper.UpdateHelperListener;
import com.travel.widget.UpdateDialog;
import com.travel.widget.UpdateDialog.UpdateDialogListener;

/**
 * 关于
 *
 * @author WYP
 * @version 1.0
 * @created 2016/04/22
 */
public class SettingActivity extends TitleBarBaseActivity implements
        OnClickListener {

    private final static int REQUEST_NICK_NAME = 2;
    private Context mContext;

    private TextView cacheSize;

    private UpdateHelper mUpdateHelper;
    private UpdateDialog mUpdateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_setting);

        init();
    }

    /**
     * 控件初始化与点击事件
     */
    private void init() {

        cacheSize = (TextView) findViewById(R.id.size);

        findViewById(R.id.rl_help).setOnClickListener(this);
        findViewById(R.id.rl_feedback).setOnClickListener(this);
        findViewById(R.id.rl_about).setOnClickListener(this);
        findViewById(R.id.rl_deal).setOnClickListener(this);
        findViewById(R.id.rl_version).setOnClickListener(this);
        findViewById(R.id.rl_service).setOnClickListener(this);
        findViewById(R.id.rl_safe).setOnClickListener(this);
        findViewById(R.id.rl_cache).setOnClickListener(this);
        findViewById(R.id.rl_unlogin).setOnClickListener(this);

        setTitle("设置");
        getCacheSize();
    }

    @Override
    public void onClick(View v) {

        // 帮助
        if (v.getId() == R.id.rl_help)
            startActivity(new Intent(mContext, MoreFragmentActivity.class)
                    .putExtra("type", "help"));
            // 意见反馈
        else if (v.getId() == R.id.rl_feedback)
            startActivity(new Intent(mContext, FeedbackActivity.class));
            // 关于我们
        else if (v.getId() == R.id.rl_about)
            /*startActivity(new Intent(mContext, MoreFragmentActivity.class)
                    .putExtra("type", "about"));*/
			startActivity(new Intent(mContext, AboutMeActivity.class));
            // 用户协议
        else if (v.getId() == R.id.rl_deal)
            startActivity(new Intent(mContext, MoreFragmentActivity.class)
                    .putExtra("type", "deal"));
        else if (v.getId() == R.id.rl_version) {
            onVersionClick();
        } else if (v.getId() == R.id.rl_service) {
            // 联系客服
            Intent intents = new Intent(Intent.ACTION_DIAL);
            Uri data = Uri.parse("tel:" + Constants.SERVICE_NUM);
            intents.setData(data);
            startActivity(intents);
        } else if (v.getId() == R.id.rl_safe) {
            // 账号 与安全
            Intent intent = new Intent(mContext, ModifyPassword.class);
            startActivity(intent);
        } else if (v.getId() == R.id.rl_cache) {
            // 清除缓存
            DataCleanManager.clearAllCache(mContext);
            View view = View.inflate(mContext, R.layout.dialog_clean_cache, null);
            final CornerDialog dialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 70),
                    LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);
            dialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        dialog.dismiss();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            getCacheSize();

        } else if (v.getId() == R.id.rl_unlogin) {
            // 退出登录
            SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
            new HLLXLoginHelper(SettingActivity.this).visitorLogin();
            finish();
        }
        ;

    }

    private void onVersionClick() {
        if (mUpdateHelper == null) {
            mUpdateHelper = new UpdateHelper();
            mUpdateHelper.setListener(new UpdateHelperListener() {

                @Override
                public void onNetResult(int resultType, String time, String content, final String url, String version) {
                    switch (resultType) {
                        case UpdateHelper.TYPE_MUST_UPDATE:
                        case UpdateHelper.TYPE_OPTIONAL_UPDATE:
                            mUpdateDialog = new UpdateDialog(SettingActivity.this, null, content, new UpdateDialogListener() {

                                @Override
                                public void onUpdateNowClick() {
                                    mUpdateHelper.startDownloadNewApk(url);
                                }

                                @Override
                                public void onCloseClick() {
                                    // 考虑使用哪种方式
                                }
                            });
                            showProgressDialog();
                            break;
                        case UpdateHelper.TYPE_NET_FAILED:
                            Toast.makeText(SettingActivity.this, "网络失败", Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateHelper.TYPE_NO_UPDATE:
                            Toast.makeText(SettingActivity.this, "已是最新版本", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onDownloadProgressChanged(int progress) {
                    mProgressDialog.setProgress(progress);
                }
            });
        }
        mUpdateHelper.startNetRequest(this);
    }

    private ProgressDialog mProgressDialog;

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
                mProgressDialog.dismiss();
            }
        });
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUpdateHelper != null) {
            mUpdateHelper.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUpdateHelper != null) {
            mUpdateHelper.onPause();
        }
    }

    public void getCacheSize() {
        try {
            String size = DataCleanManager.getTotalCacheSize(TravelApp.appContext);
            cacheSize.setText(size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
