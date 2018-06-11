package com.travel.localfile.pk.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * User: tc
 * Date: 13-7-9
 * Time: 上午9:59
 */
public class LoacalPhotoCursorTask extends AsyncTask<Object, Object, Object> {
    private Context mContext;
    private final ContentResolver mContentResolver;

    private boolean mExitTasksEarly = false;

    private OnLocalMediaCursor onLoadPhotoCursor;//定义回调接口，获取解析到的数据

    public LoacalPhotoCursorTask(Context mContext) {
        this.mContext = mContext;
        mContentResolver = mContext.getContentResolver();
    }

    @Override
    protected Object doInBackground(Object... params) {
        List<LocalFile> localFileList = new ArrayList<>();
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };
        Uri ext_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String where = MediaStore.Images.Media.SIZE + ">=?";

        Cursor c = MediaStore.Images.Media.query(
                mContentResolver,
                ext_uri,
                projection,
                where,
                new String[]{1 * 100 * 1024 + ""},
                MediaStore.Images.Media.DATE_ADDED+" desc");
        int columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        int i = 0;
        while (c.moveToNext() && i < c.getCount() && !mExitTasksEarly) {   //移到指定的位置，遍历数据库
            LocalFile localFile = new LocalFile();
            String paths = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Long date_added = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));

            long origId = c.getLong(columnIndex);
            localFile.setId(origId);
            localFile.setType(CameraFragment.TYPE_PHOTO);
            localFile.setCreateTime(date_added * 1000);
            localFile.setLocalPath(paths);
            localFileList.add(localFile);
            c.moveToPosition(i);
            i++;
        }
        c.close();//关闭数据库
        return localFileList;
    }

    @Override
    protected void onPostExecute(Object o) {
        if (onLoadPhotoCursor != null && !mExitTasksEarly) {
            onLoadPhotoCursor.onLoadPhotoSursorResult((List<LocalFile>) o);
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

    public void setOnLoadPhotoCursor(OnLocalMediaCursor onLoadPhotoCursor) {
        this.onLoadPhotoCursor = onLoadPhotoCursor;
    }
}