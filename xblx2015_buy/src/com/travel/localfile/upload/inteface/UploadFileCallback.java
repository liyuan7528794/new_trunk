package com.travel.localfile.upload.inteface;

/**
 * 上传文件的返回接口
 * Created by Administrator on 2017/8/8.
 */
public interface UploadFileCallback {
    /**
     * 计算sha1值,JS上传用
     */
    void onCalculationSha1(int index, int progress);

    /**
     * 上传进度
     * @param index
     * @param progress
     */
    void onUploadFile(int index, int progress);

    /**
     * 上传错误
     * @param index
     */
    void onError(int index);

    /**
     * 上传完成
     * @param index
     * @param url
     */
    void onUploadFileComplete(int index, String url);
}
