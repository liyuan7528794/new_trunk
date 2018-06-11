package com.travel.localfile.pk.others;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.travel.lib.helper.OSSHelper;
import com.travel.lib.utils.MLog;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.LocalFileSQLiteHelper;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.JsUploadHelper;

import java.util.ArrayList;

/**
 * 文件上传服务, not used, need refactor
 * Created by ldkxingzhe on 2016/7/7.
 */
public class FileUploadService extends IntentService{
    @SuppressWarnings("unused")
    private static final String TAG = "FileUploadService";
    private OSSHelper mOssHelper;
    private JsUploadHelper mJsUploadHelper;
    private LocalFileSQLiteHelper mLocalFileSQLiteHelper;

    /**
     * 上传的文件列表
     */
    public static final String UPLOAD_FILE_LIST = "upload_file_list";
    /** 用户Id */
    public static final String USER_ID = "user_id";
    /** ResultReceiver */
    public static final String RESULT_RECEIVER = "result_receiver";

    public static final String IS_SINGLE_UPLOAD_SUCCESS = "is_single_upload_success";
    public static final String LOCAL_FILE = "local_file";

    public static final int ALL_COMPLETE = 1;
    public static final int SINGLE_COMPLETE = 2;
    public static final int SINGLE_FILE_PROGRESS = 3;  // 单个文件的上传进程回调

    // 用于单个文件进度的字段
    public static final String POSITION = "position";
    public static final String TOTAL_SIZE = "total_size";
    public static final String CURRENT_SIZE = "current_size";

    public FileUploadService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mJsUploadHelper == null){
            mJsUploadHelper = new JsUploadHelper(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent == null) return;
        fileListUpload(intent);
    }

    private void fileListUpload(Intent intent) {
        ArrayList<LocalFile> localFileList =
                (ArrayList<LocalFile>) intent.getSerializableExtra(UPLOAD_FILE_LIST);
        String userId = intent.getStringExtra(USER_ID);
        if(localFileList == null || localFileList.size() == 0 || TextUtils.isEmpty(userId)) return;

        final ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
        if(mOssHelper == null){
            mOssHelper = new OSSHelper();
            mLocalFileSQLiteHelper = new LocalFileSQLiteHelper(this);
            mLocalFileSQLiteHelper.init();
        }
        int size = localFileList.size();
        for(int i = 0; i < size; i++){
            final LocalFile localFile = localFileList.get(i);
            if(localFile.getType() == CameraFragment.TYPE_LIVE) continue;
            LocalFile sqlFile = mLocalFileSQLiteHelper.loadLocalFileById(localFile.getId());
            boolean result = true;
            if(sqlFile.getIsUpLoaded() && !TextUtils.isEmpty(sqlFile.getRemotePath())){
                localFile.setIsUpLoaded(true);
                localFile.setRemotePath(sqlFile.getRemotePath());
            }else{
                final int finalI = i;
                if(localFile.getType() == CameraFragment.TYPE_VIDEO
                        && mJsUploadHelper.getWebIsSupported() != -1){
                    // 视频上传腾讯云视频服务
                    String remoteFileId = mJsUploadHelper.uploadFileSync(localFile.getLocalPath());
                    result = !TextUtils.isEmpty(remoteFileId);
                    localFile.setRemotePath(remoteFileId);
                    if(result){
                        String thumbnailKey = mOssHelper.generateObjectKey(userId);
                        result = mOssHelper.uploadFileSync(thumbnailKey, localFile.getLocalPath() + "_thumbnail");
                        localFile.setThumbnailPath(mOssHelper.getImageUrlByObjectKey(thumbnailKey, null));
                    }
                }else{
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
                    String objectKey = mOssHelper.generateObjectKey(userId, postfix);
                    result = mOssHelper.uploadFileSync(objectKey, localFile.getLocalPath(), new OSSProgressCallback<PutObjectRequest>() {
                        @Override
                        public void onProgress(PutObjectRequest putObjectRequest, long l, long l1) {
                            onSingleFileProgress(finalI, localFile, l, l1, resultReceiver);
                        }
                    });
                    if(result){
                        // 上传成功
                        String remotePath = ".jpg".equals(postfix)
                                ? mOssHelper.getImageUrlByObjectKey(objectKey, null)
                                : mOssHelper.getUrlByObjectKey(objectKey);
                        localFile.setRemotePath(remotePath);
                        if(localFile.getType() == CameraFragment.TYPE_VIDEO){
                            // 上传的是视频
                            String thumbnailKey = mOssHelper.generateObjectKey(userId);
                            result = mOssHelper.uploadFileSync(thumbnailKey, localFile.getLocalPath() + "_thumbnail");
                            localFile.setThumbnailPath(mOssHelper.getImageUrlByObjectKey(thumbnailKey, null));
                        }
                    }
                }
                if(result){
                    localFile.setIsUpLoaded(true);
                    mLocalFileSQLiteHelper.update(localFile);
                }else {
                    // 上传失败
                    i--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        MLog.e(TAG, e.getMessage(), e);
                    }
                    continue;
                }
            }
            onResult(false, result, localFile, resultReceiver, null);
        }
        onResult(true, false, null, resultReceiver, localFileList);
    }

    private void onSingleFileProgress(int position, LocalFile localFile,
                                      long currentSize, long totalSize,
                                      ResultReceiver resultReceiver){
        // 返回结果给receiver
        if(resultReceiver == null) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable(LOCAL_FILE, localFile);
        bundle.putInt(POSITION, position);
        bundle.putLong(TOTAL_SIZE, totalSize);
        bundle.putLong(CURRENT_SIZE, currentSize);
        resultReceiver.send(SINGLE_FILE_PROGRESS, bundle);
    }

    private void onResult(boolean isAllComplete, boolean isSingleComplete,
                          LocalFile localFile, ResultReceiver resultReceiver,
                          ArrayList<LocalFile> localFileList){
        if(resultReceiver == null) return;

        Bundle bundle = new Bundle();
        int resultCode = -1;
        if(isAllComplete){
            bundle.putSerializable(UPLOAD_FILE_LIST, localFileList);
            resultCode = ALL_COMPLETE;
        }else{
            bundle.putBoolean(IS_SINGLE_UPLOAD_SUCCESS, isSingleComplete);
            bundle.putSerializable(LOCAL_FILE, localFile);
            resultCode = SINGLE_COMPLETE;
        }
        resultReceiver.send(resultCode, bundle);
    }
}
