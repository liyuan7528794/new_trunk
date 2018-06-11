package com.travel.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * 下载apk并自动安装
 *
 */
public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        // 首先从SharePreference中获取id,判断是自己的下载
        long id = context.getSharedPreferences(UpdateHelper.UPDATE_SHARED_PREFERENCES,Context.MODE_PRIVATE).getLong(UpdateHelper.UPDATE_SHARED_PREFERENCES,0);
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

        if(id != intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)){
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = downloadManager.query(query);
        if(cursor != null){
            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                    String downloadFileUrl = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    startInstall(context, Uri.parse(downloadFileUrl));
                }
            }
        }
	}

    private void startInstall(Context context, Uri uri) {
        // 安装apk
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri,"application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("TAG","start installing application");
        context.startActivity(install);
    }

}
