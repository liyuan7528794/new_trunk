package com.travel.localfile.upload.impl;

import android.content.Context;
import android.util.Log;

import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.ugc.TXRecordCommon;
import com.tencent.rtmp.ugc.TXUGCPublish;
import com.travel.Constants;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.localfile.LocalFileSQLiteHelper;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.upload.IUploadFile;
import com.travel.localfile.upload.inteface.UploadFileCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 腾讯云上传视频功能
 * Created by Administrator on 2017/8/8.
 */

public class UGCUpload implements IUploadFile{
    private final String TAG = "UGCUpload";
    private Context context;
    private TXUGCPublish txugcPublish;
    private TXRecordCommon.TXPublishParam param;
    private UploadFileCallback callback;
    private String mCosSignature;
    public UGCUpload(Context context){
        this.context = context;
        init();
        getSign();
    }

    private void init(){
        txugcPublish = new TXUGCPublish(context);
        txugcPublish.setListener(listener);
        param = new TXRecordCommon.TXPublishParam();
        mCosSignature = "FMVXOAlBQUVzPtGVdbTEoCRUL1YdbYSC";
    }

    public void getSign() {
        HashMap<String,Object> map = new HashMap<>();
        String url = Constants.Root_Url + "/upload/getUploadSign.do";
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if(response.optInt("error") == 0){
                    mCosSignature = response.optString("data");
                }
            }

        }, map);
    }

    @Override
    public void UploadFile(LocalFile localFile, UploadFileCallback callback) {
        this.callback = callback;

        String path = stringFilter(localFile.getLocalPath());
        if(!"".equals(path) && !path.equals(localFile.getLocalPath())){
            File oleFile = new File(localFile.getLocalPath() );
            File newFile = new File(path);
            oleFile.renameTo(newFile);

            localFile.setLocalPath(path);
            LocalFileSQLiteHelper mSQLiteHelper = new LocalFileSQLiteHelper(context);
            mSQLiteHelper.setPhoneLocal(true);
            mSQLiteHelper.init();
            mSQLiteHelper.update(localFile);
        }
        if("".equals(path)){
            path = localFile.getLocalPath();
        }

        param.signature = mCosSignature;
        // 录制生成的视频文件路径, ITXVideoRecordListener 的 onRecordComplete 回调中可以获取
        param.videoPath = path;
        // 录制生成的视频首帧预览图，ITXVideoRecordListener 的 onRecordComplete 回调中可以获取
//        param.coverPath = localFile.getThumbnailPath();
        txugcPublish.publishVideo(param);
    }

    /**
     * 过滤特殊字符
     * @param str
     * @return
     */
    private String stringFilter(String str){
        try {
            String regEx = "，/[:*?<>|\"\n\t/]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            String s = m.replaceAll("").replace(" ", "");
            String name = s.substring(s.lastIndexOf("/"), s.lastIndexOf("."));

            if(name.length() > 9){
                s = s.replace(name, name.substring(0, 9) + (char)(Math.random ()*26+'a'));
            }
            return s;
        }catch (PatternSyntaxException e){
            return str;
        }
    }


    @Override
    public boolean isUploading() {
        return false;
    }

    @Override
    public void stopUploadFile(LocalFile localFile) {

    }

    TXRecordCommon.ITXVideoPublishListener listener = new TXRecordCommon.ITXVideoPublishListener() {
        @Override
        public void onPublishProgress(long uploadBytes, long totalBytes) {
            callback.onUploadFile(0, (int) (100 * uploadBytes / totalBytes));
        }

        @Override
        public void onPublishComplete(TXRecordCommon.TXPublishResult txPublishResult) {
            switch (txPublishResult.retCode){
                case 0:
                    callback.onUploadFileComplete(0, txPublishResult.videoURL);
                    break;
                default:
                    Log.e(TAG, txPublishResult.retCode + " : " + txPublishResult.descMsg);
                    callback.onError(txPublishResult.retCode);
                    break;
            }
        }
    };

}