package com.travel.communication.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.travel.app.TravelApp;
import com.travel.communication.utils.RecorderDiskCacheUtil;
import com.travel.lib.utils.MLog;

public abstract class LoaderFromDiskOrNet implements Runnable{
	private static final String TAG = "LoaderFromDiskOrNet";

	protected final String mUrl;
	private volatile boolean mIsLoadStop = false;
	private ImageDownloader mImageDownloader;

	public interface LoadFileTaskListener{
		/**
		 * 当文件缓存得当
		 * @param url 需要的url
		 * @param fileName  文件名
		 */
		void onFileFine(String url, String fileName);
		
		
		void onError();
	}

	private LoadFileTaskListener mListener;

	public LoaderFromDiskOrNet(String url) {
		mUrl = url;
		mImageDownloader = new BaseImageDownloader(TravelApp.appContext);
	}

	/**
	 * 设置监听
	 * @param listener
	 */
	public void setListener(LoadFileTaskListener listener) {
		mListener = listener;
	}

	/**
	 * 终止此任务, 如果此任务没有执行, 则不再执行
	 */
	public void loadStop() {
		mIsLoadStop = true;
	}

	@Override
	public void run() {
		if(mIsLoadStop) return;
		File cacheFile = getDiskCache().get(mUrl);
		if(cacheFile != null){
			MLog.v(TAG, "file is in disccache, mUrl is " + mUrl);
			if(mListener != null){
				mListener.onFileFine(mUrl, cacheFile.getName());
			}
			return;
		}
		if(mIsLoadStop) return;
		
		// 从网络获取录音文件
		InputStream inputStream = getInputStream(mUrl);
		boolean loaded = RecorderDiskCacheUtil.saveStreamToCache(mUrl, inputStream);
		try {
			inputStream.close();
		} catch (IOException e) {
			MLog.e(TAG, e.getMessage());
		}
		if(loaded){
			if(mListener != null){
				if(mIsLoadStop) return;
				File file = getDiskCache().get(mUrl);
				if(file == null){
					MLog.e(TAG, "file should not be null");
					return;
				}
				mListener.onFileFine(mUrl, file.getName());
			}
		}else{
			if(mListener != null){
				mListener.onError();
			}
		}
	}
	
	public abstract DiskCache getDiskCache();
	
	

	/**
	 * 根据url获取相对应的流
	 */
	private InputStream getInputStream(String url) {
		InputStream inputStream = null;
		try{
			inputStream = mImageDownloader.getStream(url, null);
			return inputStream;
		}catch(IOException e){
			return null;
		}
	}

}