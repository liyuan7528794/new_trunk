package com.travel.lib.utils;

import android.text.TextUtils;

/**
 * 阿里云图片服务器的后缀名
 * 工具类 
 *
 */
public class OSSImagePostfixUtil {
	
	private OSSImagePostfixUtil(){}
	
	/**
	 * 获取强制缩放的路径
	 * @param originUrl 原始的url
	 * @param width		宽度
	 * @param height	高度
	 * @return			返回对应的路径名
	 */
	public static String forceWH(String originUrl, int width, int height){
		if(TextUtils.isEmpty(originUrl)) return null;
		return originUrl + "@" + height + "h_" + width + "w_2e";
	}
}
