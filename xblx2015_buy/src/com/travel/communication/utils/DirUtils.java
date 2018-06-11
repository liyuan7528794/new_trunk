package com.travel.communication.utils;

import com.travel.app.TravelApp;

import android.os.Environment;

/**
 * 文件地址的工具类
 * @author ldkxingzhe
 */
public class DirUtils {
	
	/**
	 * 获取录音的文件夹
	 * @return
	 */
	public static String getRecorderDirPath(){
		return getApplicationRootPath() + "/recorder";
	}
	
	public static String getRecorderCachePath(){
		return getApplicationRootPath() + "/recorder_cache";
	}
	
	/**
	 * 获取图片的缓存地址
	 * 大图片, 为聊天的大图片做的缓存
	 */
	public static String getImageCachePath(){
		return getApplicationRootPath() + "/image_cache";
	}
	
	private static String getApplicationRootPath(){
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/travel";
	}
}
