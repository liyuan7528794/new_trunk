package com.travel.communication.utils;

import java.io.File;
import java.io.InputStream;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;

/**
 * 大图片的缓存相关的静态类
 * @author ldkxingzhe
 *
 */
public class ImageDiskCacheUtil {
	
	private static DiskCache sDiskCache;
	
	public static synchronized DiskCache getDiskCache(){
		if(sDiskCache == null){
			sDiskCache = new UnlimitedDiskCache(new File(DirUtils.getImageCachePath()));
		}
		return sDiskCache;
	}
	
	/**
	 * 将流数据保存到本地缓存中
	 * inputStream函数中不进行关闭, 请手动关闭
	 * @param uri 地址
	 * @param inputStream 输入流
	 * @return true -- 保存成功, false -- 保存失败
	 */
	public static boolean saveStreamToCache(String uri, InputStream inputStream){		
		return DiskCacheUtil.saveStreamToCache(getDiskCache(), uri, inputStream);
	}
	
	public static boolean saveStreamToCache(String uri, File file){
		return DiskCacheUtil.saveStreamToCache(getDiskCache(), uri, file);
	}
}
