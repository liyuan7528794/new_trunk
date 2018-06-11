package com.travel.lib.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.travel.lib.BuildConfig;
import com.travel.lib.TravelApp;
import com.travel.lib.diyvolley.StringRequestWithParams;
import com.volley.CookiePostRequest;
import com.volley.Request.Method;
import com.volley.RequestQueue;
import com.volley.Response.ErrorListener;
import com.volley.Response.Listener;
import com.volley.toolbox.JsonObjectRequest;
import com.volley.toolbox.Volley;

import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 关于网络的最上层封装�?
 * 理论上全部网络方法必须使用这个类
 * @author Administrator
 */
public class NetWorkUtil {
	
	private static SoftReference<RequestQueue> mRequestQueue;
	
	private synchronized static RequestQueue getRequestQueue(){
		if(mRequestQueue == null || mRequestQueue.get() == null){
			mRequestQueue =
					new SoftReference<RequestQueue>(
							Volley.newRequestQueue(TravelApp.appContext));
		}
		return mRequestQueue.get();
	}
	/**
	 * 以post方法提交(表单提交)
	 * @param context 上下文， 必须是activity�?因为启动登录页面的代码中没有写newTask�?后续估计会加�?
	 * @param url     访问地址
	 * @param listener 监听
	 * @param map      参数
	 */
	public static void postForm(Context context, String url, 
			MResponseListener listener, Map<String, Object> map){
		if(MLog.LOG_LEVEL <= Log.VERBOSE && map != null){
			StringBuilder builder = new StringBuilder();
			builder.append("post request, url is: ").append(url).append("  parameter is:");
			for(String key : map.keySet()){
				builder.append("\n");
				builder.append(key).append(": ").append(map.get(key));
			}
			MLog.v("NetWorkUtil", builder.toString());
			if(listener != null && TextUtils.isEmpty(listener.getTagMsg())){
				listener.setTagMsg(url);
			}
		}
		map = setUtilityMapParams(map);
		getRequestQueue().add(new CookiePostRequest(context.getApplicationContext(), url, listener, listener, map));
	}
	
	/**
	 * 以post方式进行提交, 并获取结果字符串
	 * 此方法不会获取进行Cookie校验处理. 也就是不会自动弹出
	 * @param url
	 * @param map
	 */
	public static void postFormGetString(String url, Map<String, String> map,
			Listener<String> listener, ErrorListener errorListener){
		getRequestQueue().add(new StringRequestWithParams(Method.POST, url, listener, errorListener, map));
	}
	/**
	 * 以post方式提交（json格式提交�?
	 * @param url
	 * @param json
	 */
	public static void postJson(String url, JSONObject json, MResponseListener listener){
		String resultUrl = setUtilityStringParams(url);
		getRequestQueue().add(new JsonObjectRequest(Method.POST, resultUrl,json, listener, listener));
	}
	
	/**
	 * 以get方式请求
	 * @param url 请求的地�?
	 * @param listener  监听
	 */
	public static void get(String url, MResponseListener listener){
		String resultUrl = setUtilityStringParams(url);
		getRequestQueue().add(new JsonObjectRequest(Method.GET, resultUrl, listener,listener));
	}

	/**
	 * 设置Map类型的通用的参数
	 * @param map
	 * @return
	 */
	private static Map<String, Object> setUtilityMapParams(Map<String, Object> map) {
		Map<String, Object> resultMap = map;
		if (resultMap == null) {
			resultMap = new HashMap<>();
		}
		// 用户Id
		resultMap.put("USER_ID", UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "0");
		// DEVICE_MODEL 1安卓2IOS
		resultMap.put("DEVICE_MODEL", "1");
		// IMEI
		resultMap.put("IMEI", OSUtil.getIMEI());
		// IMSI
		resultMap.put("IMSI", OSUtil.getIMSI());
		// MAC
		resultMap.put("MAC", OSUtil.getMAC());
		// OS_NAME
		resultMap.put("OS_NAME", android.os.Build.MODEL);
		// VERSION_CODE
		resultMap.put("VERSION_CODE", OSUtil.getVersionCode());
		// VERSION_NAME
		resultMap.put("VERSION_NAME", OSUtil.getVersionName());
		return resultMap;
	}
	/**
	 * 设置String类型的通用的参数
	 * @param url
	 * @return
	 */
	private static String setUtilityStringParams(String url) {
		String result = url;
		if (url.contains("=")) {
			url += "&";
		}
		// 用户Id
		url += "USER_ID=" + (UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "0") + "&";
		// DEVICE_MODEL 1安卓2IOS
		url += "DEVICE_MODEL=1&";
		// IMEI
		url += "IMEI=" + OSUtil.getIMEI() + "&";
		// IMSI
		url += "IMSI=" + OSUtil.getIMSI() + "&";
		// MAC
		url += "MAC=" + OSUtil.getMAC() + "&";
		// OS_NAME
		url += "OS_NAME=" + android.os.Build.MODEL + "&";
		// VERSION_CODE
		url += "VERSION_CODE=" + OSUtil.getVersionCode() + "&";
		// VERSION_NAME
		url += "VERSION_NAME=" + OSUtil.getVersionName();
		return url;
	}
}