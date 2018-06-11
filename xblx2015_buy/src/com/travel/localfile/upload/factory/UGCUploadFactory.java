package com.travel.localfile.upload.factory;

import android.content.Context;

import com.travel.localfile.upload.IUploadFile;
import com.travel.localfile.upload.impl.UGCUpload;

/**
 * Created by Administrator on 2017/8/8.
 */

public class UGCUploadFactory extends AbsUploadFileFactory{
    private UGCUploadFactory(){

    }
    private static UGCUploadFactory instance;
    public static UGCUploadFactory getInstance(){
        if(instance == null){
            instance = new UGCUploadFactory();
        }
        return instance;
    }
    @Override
    public <T extends IUploadFile> T createUploadFile(Context context) {
        return (T) new UGCUpload(context);
    }
}
