package com.travel.widget;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.communication.helper.PlayerHelper;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import java.util.Date;

/**
 * 录音回放Dialog
 * Created by ldkxingzhe on 2016/7/7.
 */
public class RecorderPlayerDialog extends Dialog implements View.OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "RecorderPlayerDialog";

    private View mTranslateView;
    private ImageView mHeaderImageView;
    private ImageView mPlayStopImageView;
    private ImageView mCloseImageView;
    private ProgressBar mProgressBar;
    private TextView mTimer;

    private PlayerHelper mPlayerHelper;
    private Handler mHandler;
    private String mRecorderUrl;
    private String mHeaderUrl;

    private boolean mPlaying = true;
    private int mTotalDuration = -1;

    private OnePictureLooperDrawable mOnePictureLooperDrawable;
    private Animation mHeaderViewAnimation;

    public RecorderPlayerDialog(Context context, String recorderUrl, String imageUrl) {
        super(context, R.style.MyDialogStyle);
        mPlayerHelper = PlayerHelper.getInstance(getContext(), null);
        mRecorderUrl = recorderUrl;
        mHeaderUrl = imageUrl;

        mHandler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_recorder_player);
        mTranslateView = findViewById(R.id.fl_recorder_up);
        mHeaderImageView = (ImageView) findViewById(R.id.iv_header_img);
        mCloseImageView = (ImageView) findViewById(R.id.iv_close);
        mPlayStopImageView = (ImageView) findViewById(R.id.iv_play_stop);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mTimer = (TextView) findViewById(R.id.tv_timer);

        mOnePictureLooperDrawable = new OnePictureLooperDrawable();
        int radius = OSUtil.dp2px(getContext(), 10);
        mOnePictureLooperDrawable.setRadius(radius, radius, 0, 0);
        mTranslateView.setBackgroundDrawable(mOnePictureLooperDrawable);
        ImageDisplayTools.displayHeadImage(
                TextUtils.isEmpty(mHeaderUrl) ? Constants.DefaultHeadImg : mHeaderUrl,
                mHeaderImageView);
        mHeaderViewAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim);
        mHeaderViewAnimation.setInterpolator(new LinearInterpolator());
        mHeaderImageView.startAnimation(mHeaderViewAnimation);
        mOnePictureLooperDrawable.setBitmapResource(getContext().getResources(),R.drawable.bg_record_voice);
        mOnePictureLooperDrawable.start();
        mPlayStopImageView.setOnClickListener(this);
        initPlayer();
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initPlayer() {
        mPlayerHelper.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "播放bug: " + what + ", and url is: " + mRecorderUrl);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "播放音频失败, 音频文件无效", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
                return true;
            }
        });

        mPlayerHelper.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                dismiss();
            }
        });
        mPlayerHelper.playerFullPathAudio(mRecorderUrl, true);
        final MediaPlayer player = mPlayerHelper.getPlayer();
        if(player == null){
            Log.e(TAG, "播放Bug, url is: " + mRecorderUrl);
            Toast.makeText(getContext(), "播放音频失败, 音频文件无效", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params) {
                try{
                    Thread.sleep(1000);
                    mTotalDuration = player.getDuration();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "播放bug: "  + ", and url is: " + mRecorderUrl);
                    Toast.makeText(getContext(), "播放音频失败, 音频文件无效", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                return DateFormatUtil.formatTime(new Date(mTotalDuration), "mm:ss");
            }

            @Override
            protected void onPostExecute(String s) {
                mTimer.setText(s);
                mHandler.post(mUpdateProgressRunnable);
            }
        }.execute();
    }

    private Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if(mPlayerHelper.getPlayer() == null) return;
            mHandler.removeCallbacks(mUpdateProgressRunnable);
            mProgressBar.setProgress(mPlayerHelper.getPlayer().getCurrentPosition()  * 100/ mTotalDuration);
            mHandler.postDelayed(mUpdateProgressRunnable, 500);
        }
    };


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOnePictureLooperDrawable.stop();
        mPlayerHelper.release();
        mHandler.removeCallbacks(mUpdateProgressRunnable);
    }

    @Override
    public void onClick(View v) {
        mPlayStopImageView.setImageResource(
                mPlaying ? R.drawable.icon_recorder_play : R.drawable.icon_recorder_pause);
        MediaPlayer player = mPlayerHelper.getPlayer();
        if(mPlaying){
            player.pause();
            mHeaderImageView.clearAnimation();
            mOnePictureLooperDrawable.stop();
        }else{
            player.start();
            mHeaderImageView.startAnimation(mHeaderViewAnimation);
            mOnePictureLooperDrawable.start();
        }
        mPlaying = !mPlaying;

    }
}
