package com.travel.communication.view;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.ctsmedia.hltravel.R;
import com.travel.communication.utils.DirUtils;
import com.travel.lib.utils.MLog;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 小视频输入控件
 */
public class VideoInputDialog extends DialogFragment {

    private static final String TAG = "VideoInputDialog";

    public static final String ACTION = "ACTION:VIDEO_INPUT_DIALOG_RESULT";

    private Camera mCamera;
    private CameraPreview mPreview;
    private VideoView mVideoView;
    private FrameLayout mPreViewLayout;
    private PressProgressButton mRecordBtn;
    private ImageButton mCancelBtn, mAcceptBtn;
    private MediaRecorder mMediaRecorder;
    private Timer mTimer;
    private final int MAX_TIME = 1500;
    private int mTimeCount;
    private long time;
    private boolean isRecording = false;
    private String fileName;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private LocalBroadcastManager mLocalBroadcastManager;

    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            setProgress(mTimeCount);
        }
    };

    private void setProgress(int mTimeCount) {
        mRecordBtn.setProgress(mTimeCount);
    }

    private Runnable sendVideo = new Runnable() {
        @Override
        public void run() {
            recordStop();
        }
    };

    public static VideoInputDialog newInstance() {
        VideoInputDialog dialog = new VideoInputDialog();
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.maskDialog);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_video_input, container, false);
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(getActivity(), mCamera);
        mPreViewLayout = (FrameLayout) v.findViewById(R.id.camera_preview);
        mVideoView = (VideoView) v.findViewById(R.id.video_view);
        mRecordBtn = (PressProgressButton) v.findViewById(R.id.btn_record);
        mAcceptBtn = (ImageButton) v.findViewById(R.id.ib_ok);
        mCancelBtn = (ImageButton) v.findViewById(R.id.ib_cancel);
        mRecordBtn.setMaxProgress(MAX_TIME);
        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isRecording) {
                            if (prepareVideoRecorder()) {
                                time = Calendar.getInstance().getTimeInMillis();
                                mMediaRecorder.start();
                                isRecording = true;
                                mTimer = new Timer();
                                mTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        mTimeCount++;
                                        mainHandler.post(updateProgress);
                                        if (mTimeCount == MAX_TIME) {
                                            mainHandler.post(sendVideo);
                                        }
                                    }
                                }, 0, 10);
                            } else {
                                releaseMediaRecorder();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        recordStop();
                        break;
                }
                return false;
            }
        });
        mPreViewLayout.addView(mPreview);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultAndDismiss();
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        recordStop();
        releaseMediaRecorder();
        releaseCamera();
    }


    private void recordStop(){
        if (isRecording) {
            isRecording = false;
            if (isLongEnough()){
                mMediaRecorder.stop();
            }
            releaseMediaRecorder();
            mCamera.lock();
            if (mTimer != null) mTimer.cancel();
            mTimeCount = 0;
            mainHandler.post(updateProgress);

        }
    }


    /**
     * 显示小视频输入控件
     */
    public static void show(FragmentManager ft){
        DialogFragment newFragment = VideoInputDialog.newInstance();
        newFragment.show(ft, "VideoInputDialog");
    }



    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }



    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
            if (isLongEnough()){
                MLog.d(TAG, "视频足够长，开始播放");
                String fileFullPath = fileName;
                mVideoView.setVideoPath(fileFullPath);
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mPreViewLayout.setVisibility(View.GONE);
                        mPreViewLayout.removeAllViews();
                        mRecordBtn.setVisibility(View.GONE);
                        mCancelBtn.setVisibility(View.VISIBLE);
                        mAcceptBtn.setVisibility(View.VISIBLE);
                        mVideoView.setVisibility(View.VISIBLE);
                    }
                });

                mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        MLog.e(TAG, "播放失败:" + what + ", " + extra);
                        return false;
                    }
                });

                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
                mVideoView.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoView.start();
                    }
                });
            }else{
                Toast.makeText(getContext(), "小视频太短了",Toast.LENGTH_SHORT).show();
                try{
                    new File(fileName).deleteOnExit();
                }catch (Exception e){/*ingore*/}
                dismiss();
            }
        }
    }

    private void setResultAndDismiss() {
        Intent intent = new Intent(ACTION);
        intent.putExtra("result", fileName);
        mLocalBroadcastManager.sendBroadcast(intent);
        dismiss();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private boolean prepareVideoRecorder(){

        if (mCamera==null) return false;
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        try {
            mMediaRecorder.setOrientationHint(90);
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }



    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        fileName = DirUtils.getImageCachePath() + "/" + UUID.randomUUID() + ".mp4";
        return  new File(fileName);
    }

    private boolean isLongEnough(){
        return Calendar.getInstance().getTimeInMillis() - time > 3000;
    }
}
