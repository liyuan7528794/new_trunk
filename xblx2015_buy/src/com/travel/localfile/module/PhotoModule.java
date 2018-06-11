package com.travel.localfile.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.util.Log;
import android.view.SurfaceView;

import com.travel.lib.utils.MLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 照相辅助类
 * 取名Module, 用于尊重Camera2的原作者
 * Created by ldkxingzhe on 2016/6/28.
 */
public class PhotoModule extends CameraModule implements Camera.PictureCallback, Camera.ShutterCallback {
    @SuppressWarnings("unused")
    private static final String TAG = "PhotoModule";
    private String mFileName;

    public interface PhotoModuleListener{
        /** 拍照结束, 并且保存完成 */
        void onPhotoModuleSaved(String filePath);
        void onPhotoThumbnailPreview(Bitmap bitmap);
    }
    private PhotoModuleListener mListener;
    public void setListener(PhotoModuleListener listener){
        mListener = listener;
    }

    public void takePicture(String fileName){
        if(mCamera == null){
            MLog.e(TAG, "openCamera must be called before takePicture");
            return;
        }

        makeSureFileFine(fileName);
        mFileName = fileName;
        try{
            mCamera.takePicture(this, null, this);
        }catch (Exception e){
            MLog.v(TAG, e.getMessage());
        }
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        MLog.v(TAG, "onPictureTaken, and data.length is %d ", data.length);
        new Thread(){
            @Override
            public void run() {
                try {
                    mCamera.startPreview();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    File outFile = new File(mFileName);
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));
                    ExifInterface exif = new ExifInterface(mFileName);
                    if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
                        bitmap= rotate(bitmap, 90);
                    } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                        bitmap= rotate(bitmap, 270);
                    } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                        bitmap= rotate(bitmap, 180);
                    } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
                        bitmap= rotate(bitmap, isFrontCamera() ? -90 : 90);
                    }
                    if(mListener != null)mListener.onPhotoThumbnailPreview(bitmap);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    if(mListener != null){
                        mListener.onPhotoModuleSaved(mFileName);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }.start();
    }

    private Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //  mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    @Override
    protected void diyCameraParameters(Camera.Parameters parameters, SurfaceView previewSurface) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        int curHeight = 0;
        Camera.Size betterSize = getCloselyPreSize(previewSurface.getWidth(),previewSurface.getHeight(),supportedPictureSizes);
        /*for(Camera.Size size : supportedPictureSizes){
            if(Math.abs(curHeight - size.height) <= Math.abs(1080 - size.height)){
                betterSize = size;
            }
        }*/

//        // 取最大分辨率
//        if(supportedPictureSizes.get(0).width > supportedPictureSizes.get(supportedPictureSizes.size()-1).width){
//            betterSize = supportedPictureSizes.get(0);
//        }else{
//            betterSize = supportedPictureSizes.get(supportedPictureSizes.size()-1);
//        }
//
//        MLog.d(TAG, "pictureSize is %d x %d", betterSize.width, betterSize.height);
        parameters.setPictureSize(betterSize.width, betterSize.height);
/*        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        MLog.d(TAG, "pictureSize is %d x %d", profile.videoFrameWidth, profile.videoFrameHeight);
        parameters.setPictureSize(profile.videoFrameWidth, profile.videoFrameHeight);*/

        setCameraScale((double) betterSize.height / betterSize.width);
    }

    @Override
    public void onShutter() {
        // 快门声添加
/*        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
        }*/
    }
    private Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth = Math.max(surfaceHeight,surfaceWidth);
        int reqTmpHeight = Math.min(surfaceHeight,surfaceWidth);
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for(Camera.Size size : preSizeList){
            if((size.width == reqTmpWidth) && (size.height == reqTmpHeight)){
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            if(size.width >= preSizeList.get(preSizeList.size()/2).width) { // 取分辨率为中间偏大
                curRatio = ((float) size.width) / size.height;
                deltaRatio = Math.abs(reqRatio - curRatio);
                if (deltaRatio < deltaRatioMin) {
                    deltaRatioMin = deltaRatio;
                    retSize = size;
                }
            }
        }

        return retSize;
    }
}
