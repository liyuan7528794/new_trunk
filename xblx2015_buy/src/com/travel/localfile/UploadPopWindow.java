package com.travel.localfile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.MLog;

/**
 * 文件上传的一个PopupWindow
 * Created by ldkxingzhe on 2016/10/27.
 */

public class UploadPopWindow extends PopupWindow implements View.OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "UploadPopWindow";
    private View mRootView;
    private ImageView mCompleteImage;
    private TextView mProgressTextView;
    private TextView mUploadingTextView;
    private ProgressBar mProgressBar;
    private Handler mHandler;
    public UploadPopWindow(Context context){
        mRootView = LayoutInflater.from(context).inflate(R.layout.popwindow_upload_file, null);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.pb_upload);
        mCompleteImage = (ImageView) mRootView.findViewById(R.id.iv_complete);
        mProgressTextView = (TextView) mRootView.findViewById(R.id.tv_progress);
        mUploadingTextView = (TextView) mRootView.findViewById(R.id.tv_upload);
        setContentView(mRootView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(null);
        mRootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    Toast.makeText(mRootView.getContext(), "视频上传中， 请勿退出程序", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return true;
            }
        });
        mHandler = new Handler();
        mCompleteImage.setOnClickListener(this);
    }

    /**
     * 设置进度 百分制
     * @param progress 0 - 100
     */
    public void setProgress(final int progress){
        runInMainThread(new Runnable() {
            @Override
            public void run() {
                mProgressTextView.setText(progress + "%");
                if(progress >= 100){
                    // 下载完成
                }
                mProgressBar.setProgress(progress);
            }
        });
    }

    private void runInMainThread(Runnable runnable){
        if(Thread.currentThread() == Looper.getMainLooper().getThread()){
            runnable.run();
        }else{
            mHandler.post(runnable);
        }
    }

    public void onComplete(){
        setProgress(100);
        runInMainThread(new Runnable() {
            @Override
            public void run() {
                mUploadingTextView.setText("上传已完成");
                mCompleteImage.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        MLog.v(TAG, "onClick complete");
        dismiss();
    }
}
