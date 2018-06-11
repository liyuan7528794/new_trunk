package com.travel.communication.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.travel.lib.utils.MLog;

/**
 * 录音图片等磁盘缓存的一个简单工具类
 * @author ldkxingzhe
 *
 */
public class DiskCacheUtil {
	private static final String TAG = "DiskCacheUtil";
	
	/**
	 * 将流数据保存到本地缓存中
	 * inputStream函数中不进行关闭, 请手动关闭
	 * @param diskCache 磁盘缓存对象
	 * @param uri 录音的地址
	 * @param inputStream 输入流
	 * @return true -- 保存成功, false -- 保存失败
	 */
	public static boolean saveStreamToCache(DiskCache diskCache, String uri, InputStream inputStream){		
		try {
			return diskCache.save(uri, inputStream, null);
		} catch (IOException e) {
			MLog.e(TAG, e.getMessage());
		}
		
		return false;
	}
	
	public static boolean saveStreamToCache(DiskCache diskCache, String uri, File file){
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			return saveStreamToCache(diskCache, uri, inputStream);
		} catch (FileNotFoundException e) {
			MLog.e(TAG, e.getMessage());
			return false;
		}finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					MLog.e(TAG, e.getMessage());
				}
			}
		}
	}
}
