package com.travel.localfile.module;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 拥有打开摄像头与关闭摄像头的方法
 * Created by ldkxingzhe on 2016/6/28.
 */
public class CameraModule {
    @SuppressWarnings("unused")
    private static final String TAG = "CameraModule";
    protected Camera mCamera;
    protected double layoutScale = 0;
    protected double cameraScale = 0;

    private boolean mIsFrontCamera;
    private Camera.AutoFocusCallback mAutoFocusCallback;
    private float mCurrentZoom = 1.0f;
    private int mMaxZoom = -1;

    public void openCameraWithCheck(Context context, boolean isFrontCamera, SurfaceView previewSurfaceView){
        try{
            openCamera(isFrontCamera, previewSurfaceView);
        }catch (RuntimeException e){
            MLog.e(TAG, e.getMessage(), e);
            Toast.makeText(context, "打开摄像头失败, 请检查权限后重试", Toast.LENGTH_SHORT).show();
            mCamera = null;
        }
    }

    /**
     * 打开摄像头
     * @param isFrontCamera   true -- 是前置摄像头
     * @param previewSurface  预览显示的SurfaceView, previewSurface must has SurfaceHolder
     */
    public void openCamera(boolean isFrontCamera, SurfaceView previewSurface){
        mIsFrontCamera = isFrontCamera;
        stopPreviewAndCloseCamera();
        mCamera = Camera.open(getCameraIndex(isFrontCamera));
        if(mCamera == null){
            MLog.e(TAG, "mCamera is null");
            return;
        }
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(previewSurface.getHolder());
        } catch (IOException e) {
            throw new IllegalArgumentException("previewSurface must has a valid surfaceHolder");
        }
        Camera.Parameters parameters = mCamera.getParameters();
        diyCameraParameters(parameters,previewSurface);
        if(cameraScale != 0){
            if(previewSurface.getParent() != null){
//                FrameLayout.LayoutParams para = new FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.MATCH_PARENT, (int)(OSUtil.getScreenWidth()/cameraScale));
//                ((RelativeLayout)(previewSurface.getParent())).setLayoutParams(para);
            }

//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) previewSurface.getLayoutParams();
//            params.width = OSUtil.getScreenWidth();
//            params.height = (int)(params.width/cameraScale);
//            previewSurface.setLayoutParams(params);
        }

        mCamera.setParameters(parameters);
        mCamera.startPreview();
        mCamera.autoFocus(_autoFocusCallback);
        cameraAutoMoveCallBack();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void cameraAutoMoveCallBack() {
/*        Camera.AutoFocusMoveCallback _AutoFocusMoveCallback = new Camera.AutoFocusMoveCallback() {
            @Override
            public void onAutoFocusMoving(boolean start, Camera camera) {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Area> focusAreas = parameters.getFocusAreas();
                MLog.v(TAG, "onAutoFocusMoving, and start is " + start);
            }
        };
        mCamera.setAutoFocusMoveCallback(_AutoFocusMoveCallback);*/
    }

    /**
     * 设置自动聚焦完成的回调
     */
    public void setAutoFocusCallback(Camera.AutoFocusCallback callback){
        mAutoFocusCallback = callback;
    }

    public void setFocusArea(Rect rect){
        if(mCamera == null) return;
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.getSupportedFocusModes().size() <= 0) return;
        List<Camera.Area> mFocusAreas = new ArrayList<Camera.Area>();
        mFocusAreas.add(new Camera.Area(rect, 1000));
        parameters.setFocusAreas(mFocusAreas);

        List<Camera.Area> mMeteringAreas = new ArrayList<Camera.Area>();
        Rect meteringRect = new Rect();
        meteringRect.set(rect.left + rect.width() / 4,
                rect.top + rect.height() /4,
                rect.right - rect.width() / 4,
                rect.bottom - rect.height() / 4);
        mMeteringAreas.add(new Camera.Area(meteringRect, 1000));
        parameters.setMeteringAreas(mMeteringAreas);
        mCamera.setParameters(parameters);
    }

    public void setZoom(float level){
        if(mCamera == null) return;
        if(mMaxZoom <= 0){
            Camera.Parameters parameters = mCamera.getParameters();
            mMaxZoom = parameters.getMaxZoom();
        }
        mCurrentZoom *= level;
        mCurrentZoom = Math.min(mMaxZoom, Math.max(1.0f, mCurrentZoom));
        mCamera.startSmoothZoom((int) mCurrentZoom);
    }

    private Camera.AutoFocusCallback _autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if(mAutoFocusCallback != null){
                mAutoFocusCallback.onAutoFocus(success, camera);
            }
        }
    };


    /**
     * @return  是否是前置摄像头 true -- 是前置摄像头
     */
    public boolean isFrontCamera(){
        return mIsFrontCamera;
    }

    /**
     * 是否是前置摄像头
     * @param isFrontCamera  true -- 是前置摄像头
     * @return
     */
    public int getCameraIndex(boolean isFrontCamera){
        int resultIndex = -1;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for(int i = 0; i < cameraNum; i++){
            Camera.getCameraInfo(i, cameraInfo);
            int cameraID = isFrontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT
                    : Camera.CameraInfo.CAMERA_FACING_BACK;
            if(cameraInfo.facing == cameraID){
                resultIndex = i;
                break;
            }
        }
        return resultIndex;
    }

    /** 照相机的参数自定义 */
    protected void diyCameraParameters(Camera.Parameters parameters,SurfaceView previewSurface){

    }

    /**
     * 关闭摄像头
     */
    public void stopPreviewAndCloseCamera(){
        if(mCamera == null) return;
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    protected void makeSureFileFine(String filePath) {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
    }

    public void setLayoutScale(double layoutScale) {
        this.layoutScale = layoutScale;
    }

    public void setCameraScale(double cameraScale) {
        this.cameraScale = cameraScale;
    }
}
