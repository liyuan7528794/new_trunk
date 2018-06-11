package com.travel.layout;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

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
public class VideoViewFragment extends Fragment implements PlayerControllerView.PlayerControllerListener{

    private static final String TAG = "VideoViewFragment";
    private View view;

    // 视频相关
    private MediaController.MediaPlayerControl mVideoView;
    private RelativeLayout mVideoViewContainer;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        OSUtil.enableStatusBar(getActivity(), true);
        view = inflater.inflate(R.layout.controller_advertising_video, container, false);
        initView();
        initData();
        if (mVideoView instanceof IjkVideoView) {
            ((IjkVideoView) mVideoView).setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    float width = mp.getVideoWidth();
                    float height = mp.getVideoHeight();
                    float scale = width / height;
                    int screenHeight = 0;
                    int screenWidth = 0;
//                    if (scale > 1) {
//                        screenWidth = (int) (OSUtil.getScreenHeight() * scale);
//                        screenHeight = OSUtil.getScreenHeight();
//                    } else {
                        screenWidth = OSUtil.getScreenWidth();
                        screenHeight = (int) (OSUtil.getScreenWidth() / scale);
//                    }
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
                getActivity().finish();
            }
        });
        mHandler.post(updateProgressRunnable);
        return view;
    }

    private void initView() {
        mVideoViewContainer = (RelativeLayout) view.findViewById(R.id.fl_player_container);
        mPlayerControllerView = (PlayerControllerView) view.findViewById(R.id.player_controller_view);
        mCloseImageView = (ImageView) view.findViewById(R.id.iv_close);
    }

    private void initData() {
        Bundle args = getArguments();
        if (args == null)
            throw new IllegalStateException("args is null");
        mVideoView = new IjkVideoView(getContext());
        ((IjkVideoView) mVideoView).setVideoPath(args.getString("path"));
        mHandler = new Handler();

//        RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);
//        videoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        videoParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        videoParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        mVideoViewContainer.addView((View) mVideoView, videoParams);
        mVideoViewContainer.addView((View) mVideoView);
        mPlayerControllerView.hideFullScreenImage(true);
        mPlayerControllerView.showTime();
        mPlayerControllerView.setListener(this);
        mVideoView.start();
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
                getActivity().finish();
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

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView instanceof IjkVideoView) {
            ((IjkVideoView) mVideoView).stopPlayback();
            ((IjkVideoView) mVideoView).release(true);
        } else {
            ((VideoView) mVideoView).stopPlayback();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(updateProgressRunnable);
    }
}
