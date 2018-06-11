package com.travel.lib.utils;

import android.text.TextUtils;
import android.util.Log;

import com.travel.lib.helper.FileLog;

/**
 * Log的封装， 便于Log的管理
 * 用法仿照Formatter进行书写. 其含义与系统Log相同
 * @author Administrator
 */
public class MLog {
	public static final int LOG_LEVEL = Log.VERBOSE;

	
	public static void v(String tag, String message, Object... args){
		if(LOG_LEVEL <= Log.VERBOSE){
			String formatString = formatString(message, args);
			Log.v(tag, formatString);
			FileLog.getInstance().log(tag, formatString);
		}
	}
	
	public static void d(String tag, String message, Object... args){
		if(LOG_LEVEL <= Log.DEBUG){
			String formatString = formatString(message, args);
			Log.d(tag, formatString);
			FileLog.getInstance().log(tag, formatString);
		}
	}
	
	public static void e(String tag, String message, Object... args){
		if(LOG_LEVEL <= Log.ERROR){
			String formatString = formatString(message, args);
			Log.e(tag, TextUtils.isEmpty(formatString) ? "NULL" : formatString);
			FileLog.getInstance().logWithoutFilter(tag, formatString);
		}
	}
	
	private static String formatString(String format, Object... args){
		if(args == null || args.length == 0) return format;
		return String.format(format, args);
	}
}
