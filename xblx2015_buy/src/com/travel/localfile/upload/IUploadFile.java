package com.travel.localfile.upload;

import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.upload.inteface.UploadFileCallback;

/**
 * 上传文件的方法规则
 * Created by Administrator on 2017/8/7.
 */
public interface IUploadFile {
    /**
     * 上传文件
     * @param localFile
     * @param callback     回调接口
     */
    void UploadFile(LocalFile localFile, UploadFileCallback callback);

    /**
     * 当前是否有正在上传的文件
     */
    boolean isUploading();

    /**
     * 停止上传某个文件
     * @param localFile
     */
    void stopUploadFile(LocalFile localFile);
}
