package com.travel.usercenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.BuildConfig;
import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.utils.UpdateHelper;
import com.travel.widget.UpdateDialog;

/**
 * Created by Administrator on 2017/7/13.
 */

public class AboutMeActivity extends TitleBarBaseActivity {

    private RelativeLayout rl_version;
    private TextView tv_version;
    private View v_version;

    private UpdateHelper mUpdateHelper;
    private UpdateDialog mUpdateDialog;
    private boolean isUpdate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutme);
        titleText.setText("关于我们");

        rl_version = findView(R.id.rl_version);
        tv_version = findView(R.id.tv_version);
        v_version = findView(R.id.v_version);
        tv_version.setText("版本号：" + BuildConfig.VERSION_NAME);

        rl_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isUpdate) {
                    Toast.makeText(AboutMeActivity.this, "没有可更新的版本", Toast.LENGTH_SHORT).show();
                    return;
                }
                mUpdateDialog = new UpdateDialog(AboutMeActivity.this, null, contents, new UpdateDialog.UpdateDialogListener() {

                    @Override
                    public void onUpdateNowClick() {
                        mUpdateHelper.startDownloadNewApk(urls);
                    }

                    @Override
                    public void onCloseClick() {
                        // 考虑使用哪种方式
                    }
                });
                showProgressDialog();
            }
        });

        onVersionClick();
    }

    private String urls = "";
    private String contents = "";
    private void onVersionClick() {
        if (mUpdateHelper == null) {
            mUpdateHelper = new UpdateHelper();
            mUpdateHelper.setListener(new UpdateHelper.UpdateHelperListener() {

                @Override
                public void onNetResult(int resultType, String time, String content, final String url, String version) {
                    urls = url;
                    contents = content;
                    switch (resultType) {
                        case UpdateHelper.TYPE_MUST_UPDATE:
                        case UpdateHelper.TYPE_OPTIONAL_UPDATE:
                            isUpdate = true;
                            v_version.setVisibility(View.VISIBLE);
                            break;
                        case UpdateHelper.TYPE_NET_FAILED:
                            isUpdate = false;
                            Toast.makeText(AboutMeActivity.this, "获取版本号失败", Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateHelper.TYPE_NO_UPDATE:
                            isUpdate = false;
                            v_version.setVisibility(View.GONE);
//                            Toast.makeText(AboutMeActivity.this, "已是最新版本", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            isUpdate = false;
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
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

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


}
