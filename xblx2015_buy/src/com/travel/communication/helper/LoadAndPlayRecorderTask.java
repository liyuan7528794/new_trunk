package com.travel.communication.helper;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.travel.communication.utils.RecorderDiskCacheUtil;

/**
 * 仿照ImageLoader的LoadAndDisplayImageTask的类
 * 此方法使用了ImageLoader的缓存方法,严重依赖Universal ImageLoader,
 * 如果需要分离两者, 请分离出ImageLoader的缓存功能
 * @author ldkxingzhe
 *
 */
public class LoadAndPlayRecorderTask extends LoaderFromDiskOrNet{
	@SuppressWarnings("unused")
	static final String TAG = "LoadAndPlayRecorderTask";
	
	private volatile boolean mIsPlayStop = false;
	
	public LoadAndPlayRecorderTask(String url) {
		super(url);
	}
	/**
	 * 终止播放
	 */
	public void playeStop(){
		mIsPlayStop = true;
	}
	@Override
	public DiskCache getDiskCache() {
		return RecorderDiskCacheUtil.getDiskCache();
	}

}
