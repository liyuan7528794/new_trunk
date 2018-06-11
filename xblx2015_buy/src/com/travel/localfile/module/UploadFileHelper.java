package com.travel.localfile.module;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.travel.lib.helper.OSSHelper;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.LocalFileSQLiteHelper;
import com.travel.localfile.dao.LocalFile;

/**
 * 上传辅助类
 *  本地录像使用JS上传，如果支持JS上传的话
 *  其他使用阿里云上传
 * Created by ldkxingzhe on 2016/11/16.
 */
public class UploadFileHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "UploadFileHelper";

    private boolean isPhone = false;// 是否是手机内部文件

    private JsUploadHelper mJsUploadHelper;
    private OSSHelper mOssHelper;
    private Context mContext;
    private LocalFileSQLiteHelper mLocalFileSQLiteHelper;
    private String mUserId;
    private volatile boolean mIsUploading = false;
    private String mUploadingFilePath;
    private LocalFile mCurrentLocalFile;

    /* 停止上传文件 */
    public void stopUploadFile(LocalFile localFile) {
        if(!mIsUploading) return;
        if(!TextUtils.isEmpty(mUploadingFilePath) && mUploadingFilePath.equals(localFile.getLocalPath())){
            //fixme 不做任何出直接抛弃上传
            mIsUploading = false;
        }
    }

    public interface ProgressCallback{
        /* 进度中 */
        void onProgress(int progress);
        /* 失败 */
        void onFailed(String errorMessage);
        /* 进度完成 */
        void onComplete(LocalFile localFile);
    }

    public boolean isPhone() {
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }

    public UploadFileHelper(@NonNull Context context){
        mContext = context;
        mLocalFileSQLiteHelper = new LocalFileSQLiteHelper(context);
        mLocalFileSQLiteHelper.init();
        mUserId = UserSharedPreference.getUserId();
    }

    private void makeSureJSUploadHelperExists(){
        if(mJsUploadHelper == null)
            mJsUploadHelper = new JsUploadHelper(mContext);
    }

    private void makeSureOSSHelperExists(){
        if(mOssHelper == null){
            mOssHelper = new OSSHelper();
        }
    }

    public boolean isUploading(){
        return mIsUploading;
    }

    public void uploadFile(@NonNull LocalFile localFile,
                           @NonNull ProgressCallback callBack){
        if(localFile.getType() == CameraFragment.TYPE_LIVE){
            localFile.setIsUpLoaded(true);
            callBack.onComplete(localFile);
            return;
        }

        if(isPhone){

        }else {
            LocalFile sqlFile = mLocalFileSQLiteHelper.loadLocalFileById(localFile.getId());
            if (sqlFile.getIsUpLoaded() && !TextUtils.isEmpty(sqlFile.getRemotePath())) {
                localFile.setIsUpLoaded(true);
                localFile.setRemotePath(sqlFile.getRemotePath());
                // 已经上传完成了
                callBack.onComplete(localFile);
                return;
            }
        }


        makeSureOSSHelperExists();
        mUploadingFilePath = localFile.getLocalPath();
        switch (localFile.getType()){
            case CameraFragment.TYPE_VIDEO:
                makeSureJSUploadHelperExists();
                uploadVideo(localFile, callBack);
                break;
            default:
                // others
                uploadOthers(localFile, callBack);
        }
    }

    public LocalFile getCurrentUploadingFile(){
        if(isUploading()){
            return null;
        }
        return mCurrentLocalFile;
    }

    private void uploadOthers(final LocalFile localFile, final ProgressCallback callBack) {
        // 其他使用阿里云对象存储
        String postfix = null;
        switch (localFile.getType()){
            case CameraFragment.TYPE_PHOTO:
                postfix = ".jpg";
                break;
            case CameraFragment.TYPE_AUDIO:
                postfix = ".m4a";
                break;
            case CameraFragment.TYPE_VIDEO:
                postfix = ".mp4";
                break;
        }
        mIsUploading = true;
        mCurrentLocalFile = localFile;
        final String objectKey = mOssHelper.generateObjectKey(mUserId, postfix);
        final String finalPostfix = postfix;
        mOssHelper.uploadFile(objectKey, localFile.getLocalPath(), new OSSCompletedCallback<PutObjectRequest, OSSResult>() {
            @Override
            public void onSuccess(PutObjectRequest putObjectRequest, OSSResult ossResult) {
                // 上传成功
                String remotePath = ".jpg".equals(finalPostfix)
                        ? mOssHelper.getImageUrlByObjectKey(objectKey, null)
                        : mOssHelper.getUrlByObjectKey(objectKey);
                localFile.setRemotePath(remotePath);
                localFile.setIsUpLoaded(true);
                callBack.onComplete(localFile);
                mIsUploading = false;
                if(localFile.getType() == CameraFragment.TYPE_VIDEO){
                    // 上传的是视频
                    if(uploadVideoThumbnail(localFile)){
                        updateDB(localFile);
                    }
                }else{
                    updateDB(localFile);
                }
            }

            @Override
            public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1) {
                callBack.onFailed("上传失败");
                mIsUploading = false;
            }
        }, new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest putObjectRequest, long l, long l1) {
                callBack.onProgress((int)(l/l1));
            }
        });
    }

    private boolean uploadVideoThumbnail(LocalFile localFile) {
        if(localFile.getType() != CameraFragment.TYPE_VIDEO)
            throw new IllegalArgumentException("This method should upload video's thumbnail");
        String thumbnailKey = mOssHelper.generateObjectKey(mUserId);
        boolean result = mOssHelper.uploadFileSync(thumbnailKey, localFile.getLocalPath() + "_thumbnail");
        localFile.setThumbnailPath(mOssHelper.getImageUrlByObjectKey(thumbnailKey, null));
        return result;
    }

    private void updateDB(LocalFile localFile) {
        localFile.setIsUpLoaded(true);
        mLocalFileSQLiteHelper.update(localFile);
    }

    private void uploadVideo(final LocalFile localFile, final ProgressCallback callBack) {
        if(mJsUploadHelper.getWebIsSupported() == -1){
            // 不支持jS上传, 转为阿里云上传
            uploadOthers(localFile, callBack);
            return;
        }
        mJsUploadHelper.setListener(new JsUploadHelper.Listener() {
            @Override
            public void onCalculationSha1(int index, int progress) {
                // 计算sha1值进度
                callBack.onProgress(progress);
            }

            @Override
            public void onUploadFile(int index, int progress) {
                // 上传文件进度
                callBack.onProgress(progress);
            }

            @Override
            public void onError(int index) {
                // 上传失败
                callBack.onFailed("上传失败");
                mIsUploading = false;
            }

            @Override
            public void onUploadFileComplete(int index, String remoteFileId) {
                uploadVideoThumbnail(localFile);
                localFile.setRemotePath(remoteFileId);
                updateDB(localFile);
                callBack.onComplete(localFile);
                mIsUploading = false;
            }
        });
        mIsUploading = true;
        mCurrentLocalFile = localFile;
        mJsUploadHelper.uploadFile(localFile.getLocalPath());
    }
}
