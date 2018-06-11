package com.travel.video.layout;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.ctsmedia.hltravel.R;
import com.travel.layout.PlayerControllerView;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

import java.lang.reflect.Method;
import java.util.Date;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 一个播放视频的PopWindow
 * 如没有特殊需求, 几乎所有的视频均应该使用这个
 * Created by ldkxingzhe on 2016/6/21.
 */
public class VideoViewPopWindow implements PlayerControllerView.PlayerControllerListener {
    @SuppressWarnings("unused")
    private static final String TAG = "VideoViewPopWindow";

    private PopupWindow mPopupWindow;
    private MediaController.MediaPlayerControl mVideoView;
    private PlayerControllerView mPlayerControllerView;
    private ImageView mCloseImageView;
    private RelativeLayout mVideoViewContainer;
    private Handler mHandler;
    private long mVideoTimeLong;
    private Activity mActivity;

    public VideoViewPopWindow(){
        mHandler = new Handler();
    }

    public boolean isShowing(){
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void show(final Activity context, View anchorView, MediaController.MediaPlayerControl videoView){
        mActivity = context;
        mVideoView = videoView;
        ((View)mVideoView).setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        View view = (View) mVideoView;
        if(view.getParent() != null){
            ((ViewGroup)view.getParent()).removeView(view);
        }
        View rootView = LayoutInflater.from(context).inflate(R.layout.controller_advertising_video, null);
        if(mPopupWindow == null){
            mPopupWindow = new PopupWindow(rootView, rootView.getWidth(), rootView.getHeight());
            mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            try {
                Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", int.class);
                setWindowLayoutType.invoke(mPopupWindow, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                Method setLayoutInScreenEnabled = PopupWindow.class.getMethod("setLayoutInScreenEnabled", boolean.class);
                setLayoutInScreenEnabled.invoke(mPopupWindow, true);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        mVideoViewContainer = (RelativeLayout) rootView.findViewById(R.id.fl_player_container);
        mPlayerControllerView = (PlayerControllerView) rootView.findViewById(R.id.player_controller_view);
        mCloseImageView = (ImageView) rootView.findViewById(R.id.iv_close);
        RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
//        videoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        videoParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        videoParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        videoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mVideoViewContainer.addView(view, videoParams);
        mPlayerControllerView.hideFullScreenImage(true);
        mPlayerControllerView.showTime();
        mPlayerControllerView.setListener(this);
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (Build.VERSION.SDK_INT > 22)
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    dismiss();
                }
            });
        if(mVideoView instanceof IjkVideoView){
            ((IjkVideoView) mVideoView).setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            VideoViewPopWindow.this.onPrepared();
                        }
                    }, 0);
                }
            });
            ((IjkVideoView) mVideoView).setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    onComplete();
                }
            });

            ((IjkVideoView)mVideoView).setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    VideoViewPopWindow.this.onError(what);
                    return false;
                }
            });
            
        }else if(mVideoView instanceof VideoView){
            ((VideoView) mVideoView).setZOrderOnTop(true);
            ((VideoView) mVideoView).setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    VideoViewPopWindow.this.onPrepared();
                }
            });
            ((VideoView) mVideoView).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    onComplete();
                }
            });
            ((VideoView)mVideoView).setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    VideoViewPopWindow.this.onError(what);
                    return false;
                }
            });
        }


//        ViewGroup.LayoutParams windowParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        rootView.setLayoutParams(windowParams);
//        mPopupWindow.setContentView(rootView);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, 0, 0);
//        mPopupWindow.showAsDropDown(anchorView, 0, 0);
        mPopupWindow.setFocusable(true);
        mPopupWindow.update(OSUtil.getScreenWidth(), OSUtil.getScreenHeight());

        mHandler.post(updateProgressRunnable);
    }

    private void onError(int what) {
        MLog.e(TAG, "play video failed, and i is " + what);
//        if(what < 0) return;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mPopupWindow == null) return;
                Toast.makeText(mVideoViewContainer.getContext(), "视频播放出现问题啦", Toast.LENGTH_SHORT).show();
                onComplete();
                dismiss();
            }
        }, 800);
    }

    private void onComplete(){
        mPlayerControllerView.setPlayComplete();
    }

    private void onPrepared(){
//        mVideoViewContainer.setForeground(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mVideoViewContainer.setForeground(null);
        }
        ((View)mVideoView).setBackgroundDrawable(null);
    }

    private void stopAndReleaseMediaPlayer(){
        if(mVideoView instanceof IjkVideoView){
            ((IjkVideoView)mVideoView).stopPlayback();
            ((IjkVideoView) mVideoView).release(true);
        }else{
            ((VideoView)mVideoView).stopPlayback();
        }
    }

    private Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if(!isShowing()) return;
           /* if(mVideoTimeLong <= 0) {*/
                mVideoTimeLong = mVideoView.getDuration();
                mPlayerControllerView.setTotalTime("/" + DateFormatUtil.formatTime(new Date(mVideoTimeLong), "mm:ss"));
//            }
            long currentPosition = mVideoView.getCurrentPosition();
            if(Math.abs(mVideoTimeLong - currentPosition) < 800){
                // 防止出现播放完成后， 仍然显示还有一秒没播放的问题
                currentPosition = mVideoTimeLong;
            }
            if(mVideoTimeLong > 0){
                int progress = (int) (currentPosition * 1.0f / mVideoTimeLong * 100f);
                if(mPlayerControllerView != null){
                    MLog.v(TAG, "progress is " + progress + "and currentPosition is " + currentPosition);
                    mPlayerControllerView.setProgress(progress);
                }
            }
            mPlayerControllerView.setCurrentTime(DateFormatUtil.formatTime(new Date(currentPosition), "mm:ss"));
            mHandler.postDelayed(updateProgressRunnable, 500);
        }
    };

    public void dismiss(){
        stopAndReleaseMediaPlayer();
        if(mPopupWindow == null) return;
        mPopupWindow.dismiss();
        mPopupWindow = null;
        View view = (View) mVideoView;
        if(view.getParent() != null)
            ((ViewGroup)view.getParent()).removeView(view);
    }

    @Override
    public void onPlayStopClick(boolean isPlaying) {
        if(isPlaying){
            mVideoView.pause();
        }else{
            mVideoView.start();
        }
    }

    @Override
    public void onFullScreenClick() {
        mActivity.onBackPressed();
    }

    @Override
    public void onProgressChanged(int progress) {
        MLog.v(TAG, "onProgressChanged, and progress is " + progress);
        if(mVideoTimeLong <= 0) mVideoTimeLong = mVideoView.getDuration();
        long seekTo = progress * mVideoTimeLong / 100;
        MLog.v(TAG, "seekTo time " + seekTo);
        mVideoView.seekTo((int) seekTo);
        mVideoView.start();
    }
}
