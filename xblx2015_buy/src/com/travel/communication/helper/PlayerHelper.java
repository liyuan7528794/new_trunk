package com.travel.communication.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.HandlerThread;

import com.travel.lib.utils.MLog;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 录音播放器辅助类
 * 对录音播放进行简单的封装
 *
 * @author ldkxingzhe
 */
public class PlayerHelper implements OnCompletionListener, OnErrorListener {
    @SuppressWarnings("unused")
    private static final String TAG = "PlayerHelper";

    private static WeakReference<PlayerHelper> sInstance;

    // 录音文件夹地址
    private String mDirPath;
    private MediaPlayer mPlayer;
    private Context mContext;

    private OnCompletionListener mCompletionListener;
    private OnErrorListener mOnErrorListener;
    private Handler mWorkerHandler;

    private PlayerHelper(Context context, String dirPath) {
        mContext = context;
        mDirPath = dirPath;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mWorkerHandler = new Handler(handlerThread.getLooper());
    }

    public static synchronized PlayerHelper getInstance(Context context, String dirPath) {
        if (sInstance == null || sInstance.get() == null) {
            sInstance = new WeakReference<PlayerHelper>(new PlayerHelper(context, dirPath));
        }
        return sInstance.get();
    }

    /**
     * 设置播放完成后的监听
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    /**
     * 获取播放器
     */
    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    break;
                default:
            }
        }
    };

    /**
     * 使用全路径播放视频
     *
     * @param filePath    全路径名, 也可以是网络地址
     * @param isUserSpeak true -- 使用外放
     */
    public void playerFullPathAudio(final String filePath, final boolean isUserSpeak) {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                MLog.d(TAG, "playerFullPathAudio, and filePath is %s.", filePath);
                resetPlayer();
                try {
                    mPlayer.setDataSource(filePath);
                    mPlayer.prepare();
                    mPlayer.seekTo(0);
                    mPlayer.setOnCompletionListener(PlayerHelper.this);
                } catch (Exception e) {
                    MLog.e(TAG, e.getMessage());
                    release();
                    if (mOnErrorListener != null)
                        mOnErrorListener.onError(null, 0, 0);
                }
                audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mPlayer.start();
                }
                /*if(isUserSpeak){
                    // 使用扬声器
					audioManager.setMode(AudioManager.MODE_NORMAL);
					audioManager.setSpeakerphoneOn(true);
					mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				}else{
					// 听筒模式
					audioManager.setMode(AudioManager.MODE_IN_CALL);
					audioManager.setSpeakerphoneOn(false);
					mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
				}*/

            }
        });
    }

    /**
     * 播放录音
     *
     * @param fileName    文件名称, 仅仅是文件名.
     * @param isUserSpeak 是否使用扬声器, true -- 使用扬声器
     */
    public void playerRecorder(String fileName, boolean isUserSpeak) {
        File file = new File(mDirPath, fileName);
        if (file == null || !file.exists()) {
            MLog.e(TAG, "playerRecorder, and file not exist, and file is " + file);
            return;
        }
        playerFullPathAudio(file.getAbsolutePath(), isUserSpeak);
    }

    private void resetPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnErrorListener(this);
        } else {
            mPlayer.stop();
            mPlayer.reset();
        }
    }

    public void release() {
        stopPlayer();
        mWorkerHandler.getLooper().quit();
        sInstance = null;
    }

    public void stopPlayer() {
        //		mWorkerHandler.post(new Runnable() {
        //			@Override
        //			public void run() {
        if (mPlayer == null)
            return;
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        //			}
        //		});
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        MLog.v(TAG, "onComplete");
        stopPlayer();
        if (mCompletionListener != null)
            mCompletionListener.onCompletion(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        MLog.e(TAG, "onError, and what is " + what + ", extra is " + extra);
        if (mOnErrorListener != null) {
            return mOnErrorListener.onError(mp, what, extra);
        }
        return false;
    }
}
