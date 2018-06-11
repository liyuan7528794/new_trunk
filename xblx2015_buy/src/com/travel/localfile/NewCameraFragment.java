package com.travel.localfile;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.communication.utils.DirUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.CameraModule;
import com.travel.localfile.module.VideoModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 录像
 * Created by Administrator on 2017/7/27.
 */

public class NewCameraFragment extends Fragment implements SurfaceHolder.Callback,
        TimeCount.Callback, Camera.AutoFocusCallback {
    private static final String TAG = "NewCameraFragment";

    public static final String TYPE = "type";
    public static final int TYPE_VIDEO = 2;

    private int mType = TYPE_VIDEO; //多媒体类型
    private boolean mIsFrontCamera = false;

    private SurfaceView mCameraPreview;
    private ImageView mStartImageView;
    private ImageView mThumbnailImageView;
    private ImageView mCloseImageView;
    private View mThumbnailLayout;
    private ImageView goPublishImageView;
    private ImageView mSwitchCameraImageView;

    private TextView mTimer;

    private View mFocusRect;

    private VideoModule mVideoModule;
    private LocalFileSQLiteHelper mSQLiteHelper;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private TimeCount mTimeCount;
    private Handler mHandler;

    // 手机本地的视频
    private List<LocalFile> phoneLocalFiles;
    // 当前录制的视频集
    private List<LocalFile> localVideoFile;
    private LocalFile localFile;
    private String mUserId;
    private VideoUtils videoUtils ;

    private String activityId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 判断所必须的权限
        List<String> premissions = new ArrayList<>();
        // 摄像头
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.CAMERA);
        }
        // 录音
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if(premissions.size() > 0){
            String[] s = new String[premissions.size()];
            for (int i = 0; i < s.length; i++) {
                s[i] = premissions.get(i);
            }
            NewCameraFragment.this.requestPermissions(s, 1);
        }

        create();
    }

    private void create(){
        Bundle args = getArguments();
        if (args != null) {
            mType = args.getInt(TYPE);
            if(args.containsKey("activity_id")){
                activityId = args.getString("activity_id");
            }
        } else {
            mType = new Random().nextInt(3);
        }
        if (mType == TYPE_VIDEO) {
            mVideoModule = new VideoModule();
        }

        localVideoFile = new ArrayList<>();
        phoneLocalFiles = new ArrayList<>();

        mTimeCount = new TimeCount(this);

        mSQLiteHelper = new LocalFileSQLiteHelper(getActivity());
        mSQLiteHelper.setPhoneLocal(true);
        mSQLiteHelper.init();
        mSQLiteHelper.setPhotoListener(new LocalFileSQLiteHelper.GetMediaListener() {
            @Override
            public void GetLocalFile(List<LocalFile> localFiles) {
                phoneLocalFiles = localFiles;
                if (localFiles != null && !localFiles.isEmpty()) {
                    LocalFile localFile = localFiles.get(0);
                    String thumbPath = localFile.getThumbnailPath();
                    showThumbnail(thumbPath, localFile.getType());
                } else {
                    // 设置预览图不可见
                    mThumbnailLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
        mSQLiteHelper.loadFilesByType(mUserId, CameraFragment.TYPE_VIDEO);
        mHandler = new Handler();
        mUserId = UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "";

        videoUtils = new VideoUtils(new VideoUtils.VideoListener() {
            @Override
            public void spliceVideo(String filePath, boolean isSccess) {
                if(isSccess){
                    mVideoModule.generateVideoFirstFrame(filePath, filePath + "_thumbnail");
                    pauseAddVideo(filePath, filePath + "_thumbnail", TYPE_VIDEO, mTimeCount.getTotalTimeMilliseconds());
                    if(localVideoFile.size() > 1){
                        localFile = localVideoFile.get(localVideoFile.size() - 1);
                        localVideoFile.clear();
                        goPublish();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != 1) return;
        for (int i = 0; i < permissions.length; i++) {
            if(TextUtils.equals(Manifest.permission.RECORD_AUDIO, permissions[i])
                    && PackageManager.PERMISSION_DENIED == grantResults[i]){
                getActivity().finish();
                return;
            }
            if(TextUtils.equals(Manifest.permission.CAMERA, permissions[i])){
                    if(PackageManager.PERMISSION_DENIED == grantResults[i]) {
                        getActivity().finish();
                        return;
                    }else{
                        mVideoModule.openCameraWithCheck(getActivity(), false, mCameraPreview);
                    }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimeCount != null) {
            mTimeCount.reset();
        }
        if (mVideoModule != null) {
            mVideoModule.stopRecorderAndReleaseMediaRecorder();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera_new, container, false);
        mCameraPreview = (SurfaceView) rootView.findViewById(R.id.sv_camera_preview);
        mStartImageView = (ImageView) rootView.findViewById(R.id.iv_start);
        mThumbnailImageView = (ImageView) rootView.findViewById(R.id.iv_thumbnail);
        mThumbnailLayout = rootView.findViewById(R.id.fl_thumbnail_layout);
        goPublishImageView = (ImageView) rootView.findViewById(R.id.iv_go_publish);
        mCloseImageView = (ImageView) rootView.findViewById(R.id.iv_close);
        mSwitchCameraImageView = (ImageView) rootView.findViewById(R.id.iv_switch_camera);

        mTimer = (TextView) rootView.findViewById(R.id.tv_timer);
        mFocusRect = rootView.findViewById(R.id.focus_rect);
        mStartImageView.setOnClickListener(mStartImageViewClickListener);
        goPublishImageView.setOnClickListener(goPublishClickListener);
        mSwitchCameraImageView.setOnClickListener(mSwitchCameraListener);
        mThumbnailImageView.setOnClickListener(mPreviewClickListener);
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mCameraPreview.getHolder().addCallback(this);

        mThumbnailLayout.setVisibility(View.INVISIBLE);
        return rootView;
    }

    private View.OnClickListener goPublishClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pubilshVideo();
        }
    };

    private void pubilshVideo(){
        stopRecorde();
        mTimeCount.reset();

        if(localVideoFile.size() == 0) {
            Toast.makeText(getContext(), "请录制视频！", Toast.LENGTH_SHORT).show();
            return;
        }

        if(localVideoFile.size() > 1){
//                videoUtils.spliceVideos(localVideoFile, getFilePath());
                videoUtils.merge(localVideoFile, getFilePath());
                return;
//            localFile = localVideoFile.get(0);
        }

        if(localVideoFile.size() == 1)
            localFile = localVideoFile.get(0);
        goPublish();
    }

    private void goPublish(){
        Intent intent = new Intent(getActivity(), PublishVideoActivity.class);
        intent.putExtra("isSaved", false);
        intent.putExtra("localFile", localFile);
        if(!TextUtils.isEmpty(activityId))
            intent.putExtra("activityId", activityId);
        startActivity(intent);
    }

    private View.OnClickListener mPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(phoneLocalFiles.size() > 0){
                Intent intent = new Intent(getActivity(), LocalVideoCheckActivity.class);
                intent.putExtra("localFiles", (Serializable) phoneLocalFiles);
                if(!TextUtils.isEmpty(activityId))
                    intent.putExtra("activityId", activityId);
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener mSwitchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsFrontCamera = !mIsFrontCamera;
            getCameraModule().openCameraWithCheck(getActivity(), mIsFrontCamera, mCameraPreview);
        }
    };

    private String mCurrentAudioPath = null;
    private View.OnClickListener mStartImageViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mType == TYPE_VIDEO) {
                if (mVideoModule.isRecording()) {
                    stopRecorde();
                } else {
                    startRecorde();
                }
            }
        }
    };

    private void startRecorde(){
        OSUtil.vibrate(getActivity(), 300);
        if (mVideoModule.startRecorder(getFilePath())) {
            mSwitchCameraImageView.setVisibility(View.INVISIBLE);
            goPublishImageView.setVisibility(View.VISIBLE);
            mThumbnailLayout.setVisibility(View.GONE);
            mStartImageView.setImageResource(R.drawable.icon_recorder_stop);
            mTimeCount.start();
        }

    }

    private void stopRecorde(){
        mSwitchCameraImageView.setVisibility(View.VISIBLE);
        if (mVideoModule.stopRecorderAndReleaseMediaRecorder()) {
            pauseAddVideo(mVideoModule.getCurrentFilePath(), mVideoModule.getCurrentThumbnailPath(),
                    TYPE_VIDEO, mTimeCount.getTotalTimeMilliseconds());

            mVideoModule.generateFirstFrameAndSave();
        } else {
            Toast.makeText(getActivity(), "录制时间过短", Toast.LENGTH_SHORT).show();
        }
        mTimeCount.stop();
        mStartImageView.setImageResource(R.drawable.icon_recorder_start);
        goPublishImageView.setVisibility(View.GONE);
        if(mThumbnailLayout.getVisibility() == View.GONE)
            mThumbnailLayout.setVisibility(View.VISIBLE);
    }

    private void pauseAddVideo(String path, String thumbnailPath, int type, long duration) {
        LocalFile localFile = new LocalFile();
        localFile.setCreateTime(new Date().getTime());
        localFile.setIsUpLoaded(false);
        localFile.setDuration(duration);
        localFile.setLocalPath(path);
        localFile.setThumbnailPath(thumbnailPath);
        localFile.setType(type);
        localFile.setUserId(mUserId);
        localVideoFile.add(localFile);
    }

    private void showThumbnail(String thumbnailPath, int type) {
        if(goPublishImageView.getVisibility() != View.VISIBLE)
            mThumbnailLayout.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(thumbnailPath)) {
            mThumbnailImageView.setImageResource(R.drawable.bg_record_voice_circle);
        } else {
            String path = thumbnailPath.startsWith("http") || thumbnailPath.startsWith("drawable") ?
                    thumbnailPath : "file://" + thumbnailPath;
            ImageDisplayTools.displayImageRound(path, mThumbnailImageView);
        }
        if (!OSUtil.isDayTheme())
            mThumbnailImageView.setColorFilter(TravelUtil.getColorFilter(getActivity()));
    }


    // 生成文件路径
    private String getFilePath() {
        String result = null;
        if (mType == TYPE_VIDEO) {
            result = DirUtils.getImageCachePath() + "/" + System.currentTimeMillis() + ".mp4";
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mStartImageView.setClickable(true);
    }

    private CameraModule getCameraModule() {
        return mVideoModule;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        final CameraModule cameraModule = getCameraModule();
        cameraModule.setLayoutScale(((double) width) / height);
        cameraModule.openCameraWithCheck(getActivity(), false, mCameraPreview);
        cameraModule.setAutoFocusCallback(this);
        showFocusRect(mCameraPreview.getWidth() / 2, mCameraPreview.getHeight() / 2);

        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        if (mScaleGestureDetector == null) {
            mScaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float currentSpan = detector.getCurrentSpan();
                    float preSpan = detector.getPreviousSpan();
                    float deltaScale = Math.abs(currentSpan - preSpan) / currentSpan;
                    if (deltaScale > 0.01) {
                        float scaleFactor = detector.getScaleFactor();
                        cameraModule.setZoom(scaleFactor);
                        return true;
                    }
                    return false;
                }
            });
            mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent event) {
                    Rect rect = new Rect();
                    int width = mCameraPreview.getWidth();
                    int height = mCameraPreview.getHeight();
                    float pointX = event.getX();
                    float pointY = event.getY();
                    showFocusRect((int) pointX, (int) pointY);

                    float y = pointY / height * 2000;
                    float x = pointX / width * 2000;

                    int radius = (int) (Math.min(mCameraPreview.getWidth(), mCameraPreview.getHeight()) * 0.5f);
                    int left = (int) Math.max(y - radius - 1000, -1000);
                    int top = (int) Math.max(-1000, 2000 - x - radius - 1000);
                    int right = Math.min(left + 2 * radius, 1000);
                    int bottom = Math.min(top + 2 * radius, 1000);
                    rect.set(left, top, right, bottom);
                    cameraModule.setFocusArea(rect);
                    return true;
                }
            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        getCameraModule().stopPreviewAndCloseCamera();
    }

    @Override
    public void onTimerCallback(long totalMilliseconds, boolean isWholeSeconds) {
        // 限制最长录制5分钟，并且跳转到发布页面
        if(totalMilliseconds >= 5*60*1000){
            if (mVideoModule.isRecording()) {
                pubilshVideo();
            }
            return;
        }

        if (isWholeSeconds) {
            mTimer.setText(mTimeCount.formatString());
        }
    }

    @Override
    public void onTimerReset() {
        mTimer.setText(mTimeCount.formatString());
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
/*        MLog.v(TAG, "onAutoFocus, and success is " + success);
        if(success){
            showFocusRect(mCameraPreview.getWidth() / 2, mCameraPreview.getHeight()/2);
        }*/
    }

    private void showFocusRect(int centerX, int centerY) {
        mHandler.removeCallbacks(mHideFocusRect);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFocusRect.getLayoutParams();
        if (centerX >= 0) {
            params.leftMargin = centerX - params.width / 2;
        }
        if (centerY >= 0) {
            params.topMargin = centerY - params.height / 2;
        }
        mFocusRect.setLayoutParams(params);
        mFocusRect.setVisibility(View.VISIBLE);
        mFocusRect.animate().cancel();
        mFocusRect.setScaleX(1);
        mFocusRect.setScaleY(1);
        mFocusRect.animate()
                .scaleY(0.5f)
                .scaleX(0.5f)
                .setDuration(500)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // TODO: 播放聚焦声音
                        MLog.v(TAG, "animation end, and play sound");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
        mHandler.postDelayed(mHideFocusRect, 1000);
    }

    private Runnable mHideFocusRect = new Runnable() {
        @Override
        public void run() {
            mFocusRect.setVisibility(View.GONE);
        }
    };
}

