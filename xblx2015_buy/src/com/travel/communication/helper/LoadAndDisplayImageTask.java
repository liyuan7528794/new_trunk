package com.travel.communication.helper;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.travel.communication.utils.ImageDiskCacheUtil;

/**
 * 本地或网络加载图片, 并展示的任务
 * 之所以出现此类是因为ImageLoader设置全局展示图片的大小为480x800
 * 为了完全展示图片, 仿照ImageLoader的LoadAndDisplayImageTask完成此类. 
 * 
 * 此类严重依赖ImageLoader的缓存机制, 请务必保留ImageLoader的缓存代码
 * @author ldkxingzhe
 *
 */
public class LoadAndDisplayImageTask extends LoaderFromDiskOrNet{
	@SuppressWarnings("unused")
	private static final String TAG = "LoadAndDisplayImageTask";
	
	public LoadAndDisplayImageTask(String url) {
		super(url);
	}

	@Override
	public DiskCache getDiskCache() {
		return ImageDiskCacheUtil.getDiskCache();
	}

}
