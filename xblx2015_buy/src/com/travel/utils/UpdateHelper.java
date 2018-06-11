package com.travel.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import com.travel.Constants;
import com.travel.app.TravelApp;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 自动更新辅助类
 * receiver本包中
 * @author ldkxingzhe
 */
public class UpdateHelper {
	private static final String TAG = "UpdateHelper";
	
	private static final String PACKAGE_NAME = "com.ctsmedia.hltravel";
	static final String UPDATE_SHARED_PREFERENCES = "udpateId";
	private static final String UPDATE_URL = "update_url";
	
	private long updateId = -1;
	private DownloadManager mDownloadManager;
	private DownloadChangeObserver mDownloadObserver;
	
	/** 强制更新 */
	public static final int TYPE_MUST_UPDATE = 0; 
	/** 可选更新*/
	public static final int TYPE_OPTIONAL_UPDATE = 1;
	/** 无更新 */
	public static final int TYPE_NO_UPDATE = 2;
	/** 网络异常 */
	public static final int TYPE_NET_FAILED = 3;
	
	public interface UpdateHelperListener{
		/**
		 * 网络请求结果
		 * @param resultType  结果类型
		 * @param time        时间
		 * @param content     内容
		 * @param url         更新的url地址
		 * @param version     版本
		 */
		void onNetResult(int resultType, String time, String content, String url, String version);
		
		void onDownloadProgressChanged(int progress);
	}
	private UpdateHelperListener mListener;
	public void setListener(UpdateHelperListener listener) {
		this.mListener = listener;
	}
	
	/**
	 * 判断是否需要更新
	 * @param lastestVersionCode
	 * @return true -- 需要更新
	 */
	public boolean isNeedUpdate(int lastestVersionCode){
		return lastestVersionCode > OSUtil.getVersionCode();
	}
	
	public void startNetRequest(Context context){
		String url = Constants.Root_Url + "/foundation/appUpdate.do";
		Map<String, Object> map = new HashMap<String, Object>();
		int versionCode = OSUtil.getVersionCode();
		map.put("version", versionCode);
		MLog.e(TAG, "version is " + versionCode);
		NetWorkUtil.postForm(context, url, new MResponseListener() {
			
			@Override
			public void onResponse(JSONObject response) {
				if(response == null){
					resultWithoutUpdate(TYPE_NET_FAILED);
					return;
				}
				
				int error = JsonUtil.getJsonInt(response, "error");
				if(error == -1){
					resultWithoutUpdate(TYPE_NET_FAILED);
					return;
				}
				String msg = JsonUtil.getJson(response, "msg");
				if("forbidden".equals(msg)){
					// 必须更新
					resolveData(TYPE_MUST_UPDATE, response);
				}else if("update".equals(msg)){
					// 可选更新
					resolveData(TYPE_OPTIONAL_UPDATE, response);
				}else if("newest".equals(msg)){
					// 没有更新
					resultWithoutUpdate(TYPE_NO_UPDATE);
				}else{
					MLog.e(TAG, "as usual, I don't think you can come here");
					resultWithoutUpdate(TYPE_NET_FAILED);
				}
			}
			@Override
			public void onErrorResponse(VolleyError error) {
				super.onErrorResponse(error);
				resultWithoutUpdate(TYPE_NET_FAILED);
			}
		}, map);
	}
	
	/**
	 * onResume中调用
	 * 用于用户监听下载
	 */
	public void onResume(){
		if(mDownloadObserver == null) return;
		if(mDownloadManager != null && updateId >= 0){
			TravelApp.appContext.getContentResolver().registerContentObserver(getUri(), true, mDownloadObserver);
		}
	}
	
	private Uri getUri(){
		
		String uriString = "content://downloads/my_downloads/" + updateId;
		MLog.v(TAG, uriString);
		return Uri.parse(uriString);
	}
	/**
	 * 用户用户监听的部分
	 */
	public void onPause(){
		if(mDownloadObserver == null) return;
		TravelApp.appContext.getContentResolver().unregisterContentObserver(mDownloadObserver);
	}
	
	private void resolveData(int type, JSONObject response){
		Object dataObject;
		try {
			dataObject = response.get("data");
		} catch (JSONException e) {
			MLog.e(TAG, e.getMessage());
			resultWithoutUpdate(TYPE_NET_FAILED);
			return;
		}
		if(dataObject instanceof JSONObject){
			JSONObject jsonObject = (JSONObject) dataObject;
			String content = JsonUtil.getJson(jsonObject, "content");
			String createTime = JsonUtil.getJson(jsonObject, "createTime");
			String url = JsonUtil.getJson(jsonObject, "download");
			String version = JsonUtil.getJson(jsonObject, "version");
			if(mListener != null){
				mListener.onNetResult(type, createTime, content, url, version);
			}
		}else{
			MLog.e(TAG, "data is not jsonObject");
			resultWithoutUpdate(TYPE_NET_FAILED);
		}
		
	}
	
	private void resultWithoutUpdate(int type){
		if(mListener == null) return;
		if(type == TYPE_NO_UPDATE || type == TYPE_NET_FAILED){
			mListener.onNetResult(type, null, null, null, null);
			return;
		}
		
	}
	
	/**
	 * 开始下载apk
	 * **注意** 此方法必须在主线程中调用, 否则会报错
	 * @param apkUrl  apk地址
	 */
	public void startDownloadNewApk(String apkUrl){
		mDownloadManager = (DownloadManager) TravelApp.appContext.getSystemService(Context.DOWNLOAD_SERVICE);
		SharedPreferences sharedPreferences = TravelApp.appContext.getSharedPreferences(UPDATE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
		String lastApkUrl = sharedPreferences.getString(UPDATE_URL, "");
		updateId = sharedPreferences.getLong(UPDATE_SHARED_PREFERENCES, -1);
		boolean shouldStartNewTask = false;
		if(updateId != -1 && lastApkUrl.equals(apkUrl)){
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(updateId);
			Cursor cursor = mDownloadManager.query(query);
			if(cursor != null && cursor.moveToFirst()){
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				switch (status){
					case DownloadManager.STATUS_RUNNING:
						break;
					case DownloadManager.STATUS_PAUSED:
					case DownloadManager.STATUS_PENDING:
					default:
						shouldStartNewTask = true;
				}
			}
		}else{
			shouldStartNewTask = true;
		}
		if(shouldStartNewTask){
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
			request.setTitle("红了旅行");

			// in order for this to run , you must use the android 2.3 or later to compile your app
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				request.allowScanningByMediaScanner();
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"hltravel.apk");
			// 返回下载的id, 这个用于等下检测完成时间是否完成.
			updateId = mDownloadManager.enqueue(request);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putLong(UPDATE_SHARED_PREFERENCES, updateId);
			editor.putString(UPDATE_URL, apkUrl);
			editor.commit();
		}
		mDownloadObserver = new DownloadChangeObserver(new Handler());
		onResume();
	}
	
	private class DownloadChangeObserver extends ContentObserver{
		public DownloadChangeObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}
		
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if(updateId < 0) return;
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(updateId);
			Cursor cursor = mDownloadManager.query(query);
			if(cursor != null && cursor.moveToFirst()){
				int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
				int bytesNeed = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
				int progress = (int) (bytesDownloaded * 1.0 / bytesNeed * 100);
				if(mListener != null){
					mListener.onDownloadProgressChanged(progress);
				}
			}
		}
		
	}
}
