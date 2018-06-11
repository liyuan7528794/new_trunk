package com.travel.lib.helper;


import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.travel.layout.PlayerControllerView;
import com.travel.lib.R;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

import java.util.Date;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 播放视频.
 */
public class VoteVideoPlayHepler implements PlayerControllerView.PlayerControllerListener {

    private static final String TAG = "VoteVideoPlayHepler";
    private View view;

    // 视频相关
    private MediaController.MediaPlayerControl mVideoView;

    // 控制栏相关
    private PlayerControllerView mPlayerControllerView;
    private ImageView mCloseImageView;
    private Handler mHandler;
    private long mVideoTimeLong;
    private Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mVideoTimeLong = mVideoView.getDuration();
            mPlayerControllerView.setTotalTime("/" + DateFormatUtil.formatTime(new Date(mVideoTimeLong), "mm:ss"));
            long currentPosition = mVideoView.getCurrentPosition();
            if (Math.abs(mVideoTimeLong - currentPosition) < 800) {
                // 防止出现播放完成后， 仍然显示还有一秒没播放的问题
                currentPosition = mVideoTimeLong;
            }
            if (mVideoTimeLong > 0) {
                int progress = (int) (currentPosition * 1.0f / mVideoTimeLong * 100f);
                if (mPlayerControllerView != null) {
                    MLog.v(TAG, "progress is " + progress + "and currentPosition is " + currentPosition);
                    mPlayerControllerView.setProgress(progress);
                }
            }
            mPlayerControllerView.setCurrentTime(DateFormatUtil.formatTime(new Date(currentPosition), "mm:ss"));
            mHandler.postDelayed(updateProgressRunnable, 500);
        }
    };

    private Activity activity;
    private String path;

    public VoteVideoPlayHepler(final Activity activity, String path, View view) {
        this.activity = activity;
        this.path = path;
        this.view = view;
        initView();
        initData();
        if (mVideoView instanceof IjkVideoView) {
            ((IjkVideoView) mVideoView).setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    float width = mp.getVideoWidth();
                    float height = mp.getVideoHeight();
                    float scale = width / height;
                    int screenHeight;
                    int screenWidth;
                    screenWidth = OSUtil.getScreenWidth();
                    screenHeight = (int) (OSUtil.getScreenWidth() / scale);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    ((IjkVideoView) mVideoView).setLayoutParams(layoutParams);
                }
            });
            ((IjkVideoView) mVideoView).setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    onComplete();
                }
            });

            ((IjkVideoView) mVideoView).setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    onErrors(what);
                    return false;
                }
            });

        } else if (mVideoView instanceof VideoView) {
            ((VideoView) mVideoView).setZOrderOnTop(true);
            ((VideoView) mVideoView).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    onComplete();
                }
            });
            ((VideoView) mVideoView).setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onErrors(what);
                    return false;
                }
            });
        }

        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        mHandler.post(updateProgressRunnable);
    }


    private void initView() {
        mPlayerControllerView = (PlayerControllerView) view.findViewById(R.id.player_controller_view);
        mCloseImageView = (ImageView) view.findViewById(R.id.iv_close);
    }

    private void initData() {
        mVideoView = new IjkVideoView(activity);
        ((IjkVideoView) mVideoView).setVideoPath(path);
        mHandler = new Handler();

//        mVideoViewContainer.addView((View) mVideoView);
        mPlayerControllerView.hideFullScreenImage(true);
        mPlayerControllerView.showTime();
        mPlayerControllerView.setListener(this);
//            mVideoView.start();
    }

    private void onComplete() {
        mPlayerControllerView.setPlayComplete();
    }

    private void onErrors(int what) {
        MLog.e(TAG, "play video failed, and i is " + what);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TravelUtil.showToast("视频播放出现问题啦");
                onComplete();
                activity.finish();
            }
        }, 800);
    }

    @Override
    public void onPlayStopClick(boolean isPlaying) {
        if (isPlaying) {
            mVideoView.pause();
        } else {
            mVideoView.start();
        }
    }

    @Override
    public void onFullScreenClick() {
        // 已禁用全屏
    }

    @Override
    public void onProgressChanged(int progress) {
        MLog.v(TAG, "onProgressChanged, and progress is " + progress);
        if (mVideoTimeLong <= 0)
            mVideoTimeLong = mVideoView.getDuration();
        long seekTo = progress * mVideoTimeLong / 100;
        MLog.v(TAG, "seekTo time " + seekTo);
        mVideoView.seekTo((int) seekTo);
        mVideoView.start();
    }

    public void onPause() {
        if (mVideoView instanceof IjkVideoView) {
            ((IjkVideoView) mVideoView).stopPlayback();
            ((IjkVideoView) mVideoView).release(true);
        } else {
            ((VideoView) mVideoView).stopPlayback();
        }
//        mHandler.removeCallbacks(updateProgressRunnable);
    }

    public void onStart() {
        if (mVideoView != null) {
            mVideoView.start();
        }

    }

    public MediaController.MediaPlayerControl getVideoView() {
        return mVideoView;
    }
}
