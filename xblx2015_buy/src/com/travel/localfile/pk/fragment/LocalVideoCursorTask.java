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
 * Created by Administrator on 2017/6/16.
 */

public class LocalVideoCursorTask extends AsyncTask<Object, Object, Object> {
    private Context mContext;
    private final ContentResolver mContentResolver;

    private boolean mExitTasksEarly = false;

    private OnLocalMediaCursor onLoadVideoCursor;//定义回调接口，获取解析到的数据

    public LocalVideoCursorTask(Context mContext) {
        this.mContext = mContext;
        mContentResolver = mContext.getContentResolver();
    }

    @Override
    protected Object doInBackground(Object... params) {
        List<LocalFile> localFileList = new ArrayList<>();
        Cursor cursor = mContentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                long createTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)) * 1000;
                Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
                if(bitmap == null) continue;
                String thumbnailPath = path + "_thumbnail";
                BufferedOutputStream outputStream = null;
                try {
                    outputStream = new BufferedOutputStream(new FileOutputStream(thumbnailPath));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    outputStream.flush();
                } catch (IOException e) {
                }finally {
                    if(outputStream != null){
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
                LocalFile localFile = new LocalFile();
                localFile.setId(id);
                localFile.setLocalPath(path);
                localFile.setUserId(UserSharedPreference.getUserId());
                localFile.setCreateTime(createTime);
                localFile.setType(CameraFragment.TYPE_VIDEO);
                localFile.setDuration(duration);
                localFile.setThumbnailPath(thumbnailPath);

                localFileList.add(localFile);
            }
            cursor.close();
        }
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
