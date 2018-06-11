package com.travel.localfile;

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

/**
 * Created by Administrator on 2018/4/2.
 */

public class UploadPictureHelper {
    private static final String TAG = "UploadPictureHelper";

    private OSSHelper mOssHelper;
    private Context mContext;
    private String mUserId;
    private volatile boolean mIsUploading = false;
    private String mUploadingFilePath;
    private String mCurrentLocalFile;

    /* 停止上传文件 */
    public void stopUploadFile(String localFile) {
        if(!mIsUploading) return;
        if(!TextUtils.isEmpty(mUploadingFilePath) && mUploadingFilePath.equals(localFile)){
            //fixme 不做任何出直接抛弃上传
            mIsUploading = false;
        }
    }

    public interface ProgressCallback{
        /* 进度中 */
        void onProgress(int progress);
        /* 失败 */
        void onFailed(String errorMessage, String tag);
        /* 进度完成 */
        void onComplete(String url, String tag);
    }

    public UploadPictureHelper(@NonNull Context context){
        mContext = context;
        mUserId = UserSharedPreference.getUserId();
    }

    private void makeSureOSSHelperExists(){
        if(mOssHelper == null){
            mOssHelper = new OSSHelper();
        }
    }

    public boolean isUploading(){
        return mIsUploading;
    }

    public void uploadFile(@NonNull String localFile, String tag, @NonNull ProgressCallback callBack){
        makeSureOSSHelperExists();
        mUploadingFilePath = localFile;
        uploadOthers(localFile, tag, callBack);
    }

    public String getCurrentUploadingFile(){
        if(isUploading()){
            return null;
        }
        return mCurrentLocalFile;
    }

    private void uploadOthers(final String localFile, final String tag, final ProgressCallback callBack) {
        // 其他使用阿里云对象存储
        String postfix = ".jpg";
        mIsUploading = true;
        mCurrentLocalFile = localFile;
        final String objectKey = mOssHelper.generateObjectKey(mUserId, postfix);
        final String finalPostfix = postfix;
        mOssHelper.uploadFile(objectKey, localFile, new OSSCompletedCallback<PutObjectRequest, OSSResult>() {
            @Override
            public void onSuccess(PutObjectRequest putObjectRequest, OSSResult ossResult) {
                // 上传成功
                String remotePath = ".jpg".equals(finalPostfix)
                        ? mOssHelper.getImageUrlByObjectKey(objectKey, null)
                        : mOssHelper.getUrlByObjectKey(objectKey);
                callBack.onComplete(remotePath, tag);
                mIsUploading = false;
            }

            @Override
            public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1) {
                callBack.onFailed("上传失败", tag);
                mIsUploading = false;
            }
        }, new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest putObjectRequest, long l, long l1) {
                callBack.onProgress((int)(l/l1));
            }
        });
    }
}
