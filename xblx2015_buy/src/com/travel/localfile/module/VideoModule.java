package com.travel.localfile.module;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceView;

import com.travel.lib.utils.MLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 模仿Camera2的录像功能模块
 * 提供了录像的功能
 * 起名Module仅仅是出于对原作者的尊重, 就是个辅助类
 *
 * Note: 代码中的前置摄像头与横屏模式代码没有编写, 使用时可能会抛出异常,
 *        如有需要, 请根据需要添加所需的逻辑控制
 * Created by ldkxingzhe on 2016/6/28.
 */
public class VideoModule extends CameraModule{
    @SuppressWarnings("unused")
    private static final String TAG = "VideoModule";

    private MediaRecorder mMediaRecorder;
    private String mCurrentFilePath;
    private Camera.Size mOpCameraSize;

    private long mStartTime;

    /**
     * 开始录制视频,
     * called after openCamera;
     * @param filePath
     */
    public boolean startRecorder(String filePath){
        if(mCamera == null){
            MLog.e(TAG, "startRecorder must called after openCamera");
        }
        makeSureFileFine(filePath);
        mCurrentFilePath = filePath;
        try {
            initializeRecorder(filePath);
            mMediaRecorder.start();
            mStartTime = System.currentTimeMillis();
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            stopRecorderAndReleaseMediaRecorder();
            return false;
        }
    }

    /**
     * @return 是否正在录制, true -- 正在录像
     */
    public boolean isRecording(){
        return mMediaRecorder != null;
    }

    /**
     * 获取视频第一帧, 并保存到本地_thumbnail中
     * 第一帧为全大小
     */
    public void generateFirstFrameAndSave(){
        generateVideoFirstFrame(mCurrentFilePath, mCurrentFilePath + "_thumbnail");
    }

    public static boolean generateVideoFirstFrame(String videoPath, String thumbnailPath){
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try{
            mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime(-1);
        }catch (Exception e){
            MLog.e(TAG, e.getMessage(), e);
        }finally {
            try{
                mediaMetadataRetriever.release();
            }catch (Exception e){}
        }
        if(bitmap == null){
            MLog.e(TAG, "in function generateFirstFrameAndSave, bitmap is null");
            return false;
        }
        BufferedOutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(thumbnailPath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            MLog.e(TAG, e.getMessage(), e);
            return false;
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    /** 返回配置录像的地址 */
    public String getCurrentFilePath(){
        return mCurrentFilePath;
    }

    /** 发挥缩略图的地址 */
    public String getCurrentThumbnailPath(){
        return mCurrentFilePath + "_thumbnail";
    }

    @Override
    protected void diyCameraParameters(Camera.Parameters parameters, SurfaceView mSurfaceView) {
        List<String> supportFocusMode = parameters.getSupportedFocusModes();
        if(supportFocusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }else{
            Log.e(TAG, "In function openCamera, and not support continuous video");
        }
//        mOpCameraSize = getOptimalVideoSize(parameters);
        mOpCameraSize = getOptimalPreviewSize(parameters,mSurfaceView.getWidth(), mSurfaceView.getHeight());
        MLog.v(TAG, "diyCameraParameters, and with is %d, height is %d",
                mOpCameraSize.width, mOpCameraSize.height);
        setCameraScale((double) mOpCameraSize.height / mOpCameraSize.width);
//        parameters.setPreviewSize(OSUtil.getScreenWidth(), (int)(OSUtil.getScreenWidth()*layoutScale));
        parameters.setPreviewSize(mOpCameraSize.width, mOpCameraSize.height);
    }

    private void initializeRecorder(String filePath) throws IOException {
        if(mCamera == null) return;
        if(mMediaRecorder != null) stopRecorderAndReleaseMediaRecorder();
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        profile.videoFrameWidth = mOpCameraSize.width;
        profile.videoFrameHeight = mOpCameraSize.height;
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mMediaRecorder.setVideoEncodingBitRate(profile.videoFrameWidth * 1024);

        mMediaRecorder.setOutputFile(filePath);
        // FIXME: 2016/6/28 添加代码控制方向
        if(isFrontCamera()){
            mMediaRecorder.setOrientationHint(270);
        }else{
            mMediaRecorder.setOrientationHint(90);
        }
        mMediaRecorder.prepare();
    }

    /**
     * 停止录制, 并且释放MediaRecorder资源
     * @return true -- 停止成功, false -- 停止失败, 删除文件
     */
    public boolean stopRecorderAndReleaseMediaRecorder(){
        if(mMediaRecorder == null) return false;
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if(System.currentTimeMillis() - mStartTime < 2000){
                // 录制太短， 录制失败
                return false;
            }
            return true;
        }catch (RuntimeException e){
            MLog.e(TAG, "stop failed." + e.getMessage(), e);
            File file = new File(mCurrentFilePath);
            file.deleteOnExit();
            if(mMediaRecorder != null){
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }
        return false;
    }

    public Camera.Size getOptimalVideoSize(Camera.Parameters para){
        List<Camera.Size> supportedVideoSize = para.getSupportedVideoSizes();
        List<Camera.Size> previewSizes = para.getSupportedPreviewSizes();
        final double aspectTolerance = 0.1;
        if(supportedVideoSize == null){
            supportedVideoSize = previewSizes;
        }
        Camera.Size optimalSize = supportedVideoSize.get(0);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        for(Camera.Size size : supportedVideoSize){
            if(Math.abs(optimalSize.width - profile.videoFrameWidth)
                    > Math.abs(size.width - profile.videoFrameWidth)
                    && previewSizes.contains(size)){
                optimalSize = size;
            }
        }
        return optimalSize;
    }

    public Camera.Size getOptimalPreviewSize(Camera.Parameters para, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        List<Camera.Size> sizes = para.getSupportedVideoSizes();
        List<Camera.Size> previewSizes = para.getSupportedPreviewSizes();
        if(sizes == null){
            sizes = previewSizes;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            optimalSize = sizes.get(0);
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            for(Camera.Size size : sizes){
                if(Math.abs(optimalSize.width - profile.videoFrameWidth)
                        > Math.abs(size.width - profile.videoFrameWidth)
                        && previewSizes.contains(size)){
                    optimalSize = size;
                }
            }

            /*minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }*/
        }
        return optimalSize;
    }
}
