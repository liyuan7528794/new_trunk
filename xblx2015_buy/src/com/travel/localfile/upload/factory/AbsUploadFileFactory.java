package com.travel.localfile.upload.factory;

import android.content.Context;
import com.travel.localfile.upload.IUploadFile;

/**
 * Created by Administrator on 2017/8/7.
 */

public abstract class AbsUploadFileFactory {
    public abstract  <T extends IUploadFile> T createUploadFile(Context context);
}
