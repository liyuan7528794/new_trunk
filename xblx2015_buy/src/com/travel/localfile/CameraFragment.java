package com.travel.localfile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.travel.activity.OneFragmentActivity;
import com.travel.communication.helper.RecorderHelper;
import com.travel.communication.utils.DirUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.CameraModule;
import com.travel.localfile.module.PhotoModule;
import com.travel.localfile.module.VideoModule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 照相, 录像, 录音界面
 * Created by ldkxingzhe on 2016/6/29.
 */
public class CameraFragment extends Fragment
        implements SurfaceHolder.Callback,
        PhotoModule.PhotoModuleListener,
        TimeCount.Callback,
        Camera.AutoFocusCallback {
    @SuppressWarnings("unused")
    private static final String TAG = "CameraFragment";

    public static final String TYPE = "type";
    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_LIVE = 3;

    private int mType = TYPE_AUDIO; //多媒体类型
    private boolean mIsFrontCamera = false;

    private SurfaceView mCameraPreview;
    private ImageView mStartImageView;
    private ImageView mThumbnailImageView, mThumbnailTypeImageView;
    private ImageView mCloseImageView;
    private View mThumbnailLayout;
    private ImageView mSwitchCameraImageView;

    private TextView mTimer;

    private VisualizerView mVisualizerView;
    private View mFocusRect;

    private VideoModule mVideoModule;
    private PhotoModule mPhotoModule;
    private RecorderHelper mAudioRecorderHelper;
    private LocalFileSQLiteHelper mSQLiteHelper;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;


    private TimeCount mTimeCount;
    private Handler mHandler;

    private String mUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mType = args.getInt(TYPE);
        } else {
            mType = new Random().nextInt(3);
        }
        if (mType == TYPE_VIDEO) {
            mVideoModule = new VideoModule();
        } else if (mType == TYPE_PHOTO) {
            mPhotoModule = new PhotoModule();
            mPhotoModule.setListener(this);
        } else if (mType == TYPE_AUDIO) {
            // 录音界面考虑使用服务, 有后台录音的可能
            mAudioRecorderHelper = RecorderHelper.getInstance(DirUtils.getRecorderDirPath());
        }

        if (mType != TYPE_PHOTO) {
            mTimeCount = new TimeCount(this);
        }
        mSQLiteHelper = new LocalFileSQLiteHelper(getActivity());
        mSQLiteHelper.init();
        mHandler = new Handler();
        mUserId = UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimeCount != null) {
            mTimeCount.stop();
        }
        if (mVideoModule != null) {
            mVideoModule.stopRecorderAndReleaseMediaRecorder();
        }
        if (mAudioRecorderHelper != null) {
            mAudioRecorderHelper.release();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        mCameraPreview = (SurfaceView) rootView.findViewById(R.id.sv_camera_preview);
        mStartImageView = (ImageView) rootView.findViewById(R.id.iv_start);
        mThumbnailImageView = (ImageView) rootView.findViewById(R.id.iv_thumbnail);
        mThumbnailTypeImageView = (ImageView) rootView.findViewById(R.id.iv_type);
        mThumbnailLayout = rootView.findViewById(R.id.fl_thumbnail_layout);
        mCloseImageView = (ImageView) rootView.findViewById(R.id.iv_close);
        mSwitchCameraImageView = (ImageView) rootView.findViewById(R.id.iv_switch_camera);

        mTimer = (TextView) rootView.findViewById(R.id.tv_timer);
        mVisualizerView = (VisualizerView) rootView.findViewById(R.id.visualizer_view);
        mFocusRect = rootView.findViewById(R.id.focus_rect);
        mStartImageView.setOnClickListener(mStartImageViewClickListener);
        mSwitchCameraImageView.setOnClickListener(mSwitchCameraListener);
        mThumbnailImageView.setOnClickListener(mPreviewClickListener);
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        if (mType == TYPE_AUDIO) {
            mSwitchCameraImageView.setVisibility(View.GONE);
            mVisualizerView.setVisibility(View.VISIBLE);
            mCameraPreview.setVisibility(View.GONE);
            setBackground(rootView);
        } else {
            mCameraPreview.getHolder().addCallback(this);
        }

        mThumbnailLayout.setVisibility(View.INVISIBLE);
        if (mType != TYPE_PHOTO) {
            onTimerReset();
            mTimer.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<LocalFile> localFileList = mSQLiteHelper.loadAllFile(mUserId, null);
        if (localFileList != null && !localFileList.isEmpty()) {
            LocalFile localFile = localFileList.get(0);
            String thumbPath = null;
            switch (localFile.getType()) {
                case TYPE_PHOTO:
                    thumbPath = localFile.getLocalPath();
                    break;
                case TYPE_VIDEO:
                    thumbPath = localFile.getLocalPath() + "_thumbnail";
                    break;
                case TYPE_AUDIO:
                    thumbPath = "";
                    break;
            }
            showThumbnail(thumbPath, localFile.getType());
        } else {
            // 设置预览图不可见
            mThumbnailLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setBackground(View rootView) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), R.drawable.bg_record_voice, options);
        BitmapFactory.decodeResource(getResources(), R.color.white, options);
        int sample = Math.max(options.outHeight / OSUtil.getScreenHeight(),
                options.outWidth / OSUtil.getScreenWidth());
        options.inSampleSize = sample <= 2 ? 2 : sample;
        options.inJustDecodeBounds = false;

        rootView.setBackgroundDrawable(
                new BitmapDrawable(
                        BitmapFactory.decodeResource(getResources(), R.color.white, options))); //R.drawable.bg_record_voice
    }

    private boolean isClick = true;
    private View.OnClickListener mPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(!isClick) return;
            isClick = false;
            v.setClickable(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.setClickable(true);
                    isClick = true;
                }
            }, 600);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArrayList<LocalFile> localFiles = (ArrayList<LocalFile>) mSQLiteHelper.loadAllFile(mUserId, null);
                    Bundle arg = new Bundle();
                    arg.putSerializable(LocalFileLookFragment.LOCAL_FILE_LIST, localFiles);
                    arg.putBoolean(LocalFileLookFragment.HAS_FEATURE_DELETE, true);
                    arg.putBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE_FROM_LOCAL, true);
                    arg.putBoolean(LocalFileLookFragment.HAS_FEATURE_SELECT, false);
                    OneFragmentActivity.startNewActivity(getActivity(), "", LocalFileLookFragment.class, arg);
                }
            },500);
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
                    mSwitchCameraImageView.setVisibility(View.VISIBLE);
                    if (mVideoModule.stopRecorderAndReleaseMediaRecorder()) {
                        saveLocalFileToDB(mVideoModule.getCurrentFilePath(),
                                TYPE_VIDEO, mTimeCount.getTotalTimeMilliseconds());
                        mVideoModule.generateFirstFrameAndSave();
                        showThumbnail(mVideoModule.getCurrentThumbnailPath(), TYPE_VIDEO);
                    } else {
                        Toast.makeText(getActivity(), "录制时间过短", Toast.LENGTH_SHORT).show();
                    }
                    mTimeCount.reset();
                    mStartImageView.setImageResource(R.drawable.oval_f_10);
                } else {
                    OSUtil.vibrate(getActivity(), 300);
                    if (mVideoModule.startRecorder(getFilePath())) {
                        mSwitchCameraImageView.setVisibility(View.INVISIBLE);
                        mStartImageView.setImageResource(R.drawable.oval_red_d25160);
                        mTimeCount.reset();
                        mTimeCount.start();
                    }
                }
            } else if (mType == TYPE_PHOTO) {
                mStartImageView.setEnabled(false);
                mPhotoModule.takePicture(getFilePath());
                mStartImageView.setScaleX(1f);
                mStartImageView.setScaleY(1f);
                mStartImageView.setRotation(0);
                mStartImageView.animate()
                        .scaleX(0.8f).scaleY(0.8f)
                        .setDuration(500)
                        .rotation(360)
                        .setInterpolator(new AccelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mStartImageView.setScaleX(1.0f);
                                mStartImageView.setScaleY(1.0f);
                                mStartImageView.setRotation(0);
                            }
                        })
                        .start();
            } else if (mType == TYPE_AUDIO) {
                if (mAudioRecorderHelper.isRecording()) {
                    if (mAudioRecorderHelper.stop()) {
                        showThumbnail("", TYPE_AUDIO);
                        saveLocalFileToDB(mCurrentAudioPath, TYPE_AUDIO, mTimeCount.getTotalTimeMilliseconds());
                    } else {
                        Toast.makeText(getActivity(), "录制时间过短", Toast.LENGTH_SHORT).show();
                    }
                    mTimeCount.reset();
                    mStartImageView.setBackgroundResource(R.drawable.oval_f_10);
                    mVisualizerView.updateAmplitude(0);
                } else {
                    OSUtil.vibrate(getContext(), 300);
                    mCurrentAudioPath = mAudioRecorderHelper.prepareAndStartRecorder();
                    if (TextUtils.isEmpty(mCurrentAudioPath)) {
                        // 录音失败, 很有可能是权限问题
                        Toast.makeText(getActivity(), "录音失败, 请赋予录音权限", Toast.LENGTH_SHORT).show();
                    } else {
                        mTimeCount.reset();
                        mTimeCount.start();
                        mStartImageView.setBackgroundResource(R.drawable.oval_red_d25160);
                    }
                }
            }
        }
    };

    /* 将本地资料信息保存入数据库 */
    private void saveLocalFileToDB(String path, int type, long duration) {
        // TODO: 添加用户信息, 订单信息部分
        LocalFile localFile = new LocalFile();
        localFile.setCreateTime(new Date().getTime());
        localFile.setIsUpLoaded(false);
        localFile.setDuration(duration);
        localFile.setLocalPath(path);
        localFile.setType(type);
        localFile.setUserId(mUserId);
        mSQLiteHelper.insert(localFile);
    }

    private void showThumbnail(String thumbnailPath, int type) {
        mThumbnailTypeImageView.setImageResource(
                type == TYPE_VIDEO ?
                        R.drawable.detail_icon_play_white : R.drawable.detail_icon_voice_black);
        mThumbnailTypeImageView.setVisibility(type != TYPE_PHOTO ? View.VISIBLE : View.GONE);
        mThumbnailLayout.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(thumbnailPath)) {
            mThumbnailImageView.setImageResource(R.drawable.bg_record_voice_circle);
        } else {
            String path = thumbnailPath.startsWith("http") || thumbnailPath.startsWith("drawable") ?
                    thumbnailPath : "file://" + thumbnailPath;
            ImageDisplayTools.displayImageRound(path, mThumbnailImageView);
        }
    }


    // 生成文件路径
    private String getFilePath() {
        String result = null;
        if (mType == TYPE_VIDEO) {
            result = DirUtils.getImageCachePath() + "/" + UUID.randomUUID() + ".mp4";
        } else if (mType == TYPE_PHOTO) {
            result = DirUtils.getImageCachePath() + "/" + UUID.randomUUID() + ".jpeg";
        } else if (mType == TYPE_AUDIO) {
            // FIXME: 2016/6/30 录音的功能保留在录音辅助类中
            result = DirUtils.getRecorderDirPath() + "/test_audio.mp3";
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mStartImageView.setClickable(true);
//        final CameraModule cameraModule = getCameraModule();
//        cameraModule.openCameraWithCheck(getActivity(), false, mCameraPreview);
//        cameraModule.setAutoFocusCallback(this);
//        showFocusRect(mCameraPreview.getWidth() / 2, mCameraPreview.getHeight() / 2);
//
//        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mScaleGestureDetector.onTouchEvent(event);
//                mGestureDetector.onTouchEvent(event);
//                return true;
//            }
//        });
//
//        if (mScaleGestureDetector == null) {
//            mScaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                @Override
//                public boolean onScale(ScaleGestureDetector detector) {
//                    float currentSpan = detector.getCurrentSpan();
//                    float preSpan = detector.getPreviousSpan();
//                    float deltaScale = Math.abs(currentSpan - preSpan) / currentSpan;
//                    if (deltaScale > 0.01) {
//                        float scaleFactor = detector.getScaleFactor();
//                        cameraModule.setZoom(scaleFactor);
//                        return true;
//                    }
//                    return false;
//                }
//            });
//            mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
//                @Override
//                public boolean onSingleTapConfirmed(MotionEvent event) {
//                    Rect rect = new Rect();
//                    int width = mCameraPreview.getWidth();
//                    int height = mCameraPreview.getHeight();
//                    float pointX = event.getX();
//                    float pointY = event.getY();
//                    showFocusRect((int) pointX, (int) pointY);
//
//                    float y = pointY / height * 2000;
//                    float x = pointX / width * 2000;
//
//                    int radius = (int) (Math.min(mCameraPreview.getWidth(), mCameraPreview.getHeight()) * 0.5f);
//                    int left = (int) Math.max(y - radius - 1000, -1000);
//                    int top = (int) Math.max(-1000, 2000 - x - radius - 1000);
//                    int right = Math.min(left + 2 * radius, 1000);
//                    int bottom = Math.min(top + 2 * radius, 1000);
//                    rect.set(left, top, right, bottom);
//                    cameraModule.setFocusArea(rect);
//                    return true;
//                }
//            });
//        }
    }

    private CameraModule getCameraModule() {
        return mType == TYPE_VIDEO ? mVideoModule : mPhotoModule;
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
    public void onPhotoModuleSaved(final String filePath) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //                showThumbnail(filePath);
                saveLocalFileToDB(filePath, TYPE_PHOTO, 0);
            }
        });
    }

    @Override
    public void onPhotoThumbnailPreview(final Bitmap bitmap) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mThumbnailTypeImageView.setVisibility(View.INVISIBLE);
                mThumbnailLayout.setVisibility(View.VISIBLE);
                mThumbnailImageView.setImageBitmap(bitmap);
                MLog.v(TAG, "onPhotoThumbnailPreview");
                if (mType == CameraFragment.TYPE_PHOTO) {
                    mStartImageView.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onTimerCallback(long totalMilliseconds, boolean isWholeSeconds) {
        if (isWholeSeconds) {
            mTimer.setText(mTimeCount.formatString());
        }
        if (mType == TYPE_AUDIO) {
            int level = mAudioRecorderHelper.getVoiceLevel(400);
            MLog.v(TAG, "audio void level is %d. ", level);
            mVisualizerView.updateAmplitude(level / 400f);
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

