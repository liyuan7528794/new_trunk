package com.travel.localfile.pk.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/7.
 */

public class NetLiveTask extends AsyncTask<Object, Object, Object> {
    private Context mContext;
    private final ContentResolver mContentResolver;

    private boolean mExitTasksEarly = false;

    private OnLocalMediaCursor onLoadVideoCursor;//定义回调接口，获取解析到的数据

    public NetLiveTask(Context mContext) {
        this.mContext = mContext;
        mContentResolver = mContext.getContentResolver();
    }

    @Override
    protected Object doInBackground(Object... params) {
        List<LocalFile> localFileList = new ArrayList<>();


        return localFileList;
    }

    @Override
    protected void onPostExecute(Object o) {
        if (onLoadVideoCursor != null && !mExitTasksEarly) {
            onLoadVideoCursor.onLoadPhotoSursorResult((List<LocalFile>) o);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();    //To change body of overridden methods use File | Settings | File Templates.
        mExitTasksEarly = true;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        this.mExitTasksEarly = exitTasksEarly;
    }

    public void setOnLoadVideoCursor(OnLocalMediaCursor onLoadVideoCursor) {
        this.onLoadVideoCursor = onLoadVideoCursor;
    }
}
