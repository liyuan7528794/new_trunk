package com.travel.communication.view;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.travel.lib.utils.OSUtil;

import java.io.IOException;
import java.util.List;


/** 摄像头预览界面控件 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "CameraPreview";

    private double scale = 0;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera == null) return;
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mCamera == null||mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(90);
            Camera.Parameters parameters=mCamera.getParameters();
            parameters.set("orientation", "portrait");
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            List<Camera.Size> supportedVideoSize = mCamera.getParameters().getSupportedVideoSizes();
            List<Camera.Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
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

            scale = ((double) optimalSize.height) / optimalSize.width;
            if (optimalSize != null) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.width*w/h);
            }

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (OSUtil.getScreenHeight()*scale), OSUtil.getScreenHeight(), Gravity.CENTER);
            setLayoutParams(params);
            mCamera.setParameters(parameters);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    public double getScale(){
        return scale;
    }

}
