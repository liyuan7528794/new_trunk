package com.travel.lib.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class JsonUtil {

	/**
	 * 解析json
	 * @param j 要解析的json对象
	 * @return 对象中的属�?value�?
	 */
	public static String getJson(JSONObject j,String key){
		String data = "";
		try {
			if(isJSONObjectIllgal(j, key)){
				data = j.get(key)+"";
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * 获取jsonObject中对应的字段对象
	 * @param j
	 * @param key
	 * @return null -- 如果不存在, 或出现异常
	 */
	public static Object get(JSONObject j, String key){
		try {
			if(isJSONObjectIllgal(j, key)){
				return j.get(key);
			}
		} catch (JSONException e) {
			Log.e("JsonUtil", e.getMessage());
		}
		return null;
	}
	
	private static boolean isJSONObjectIllgal(JSONObject j, String key) throws JSONException {
		return j != null && j.has(key) && j.get(key)!=null && !"null".equals(j.get(key)+"");
	}
	
	public static int getJsonInt(JSONObject j, String key){
		int data = -1;
		try {
			if(isJSONObjectIllgal(j, key)){
				data = j.getInt(key);
			}
		} catch (JSONException e) {
			MLog.v("JsonUtil", e.getMessage());
		}
		return data;
	}
	
	public static long getJsonLong(JSONObject j, String key){
		long data = -1;
		try {
			if(isJSONObjectIllgal(j, key)){
				data = j.getLong(key);
			}
		} catch (JSONException e) {
			MLog.v("JsonUtil", e.getMessage());
		}
		return data;
	}
	
	public static double getJsonDouble(JSONObject j, String key){
		double data = -1;
		try {
			if(isJSONObjectIllgal(j, key)){
				data = j.getDouble(key);
			}
		} catch (JSONException e) {
			MLog.v("JsonUtil", e.getMessage());
		}
		return data;
	}
	
	/**
	 * 获取json中的boolean值
	 * @param json 
	 * @param key
	 * @param defaultBoolean 默认值
	 * @return
	 */
	public static boolean getJsonBoolean(JSONObject json, String key, boolean defaultBoolean){
		boolean result = defaultBoolean;
		try {
			if(isJSONObjectIllgal(json, key)){
				result = json.getBoolean(key);
			}
		} catch (JSONException e) {
			MLog.e("JsonUtil", e.getMessage());
		}
		return result;
	}
	
	public static JSONObject getJSONObject(JSONArray jsonArray, int i){
		if(jsonArray == null) return null;
		
		try {
			return jsonArray.getJSONObject(i);
		} catch (JSONException e) {
			MLog.e("JsonUtil", e.getMessage());
			return null;
		}
	}
	/**
	 * 将Json数组解析成相应的映射对象列表
	 * @param jsonData
	 * @param cls
	 * @param <T>
	 * @return
	 */
	public  static <T> List<T> parseJsonArrayWithGson(String jsonData, Class<T> cls) {
		/*
		// 会报错java.lang.ClassCastException: com.google.gson.internal.LinkedTreeMap cannot be cast to com.travel.shop.bean.CommentBean
		Gson gson = new Gson();
		List<T> result = gson.fromJson(jsonData, new TypeToken<List<T>>(){}.getType());
		return result;*/

		List<T> list = new ArrayList<T>();
		try {
			Gson gson = new Gson();
			JsonArray arry = new JsonParser().parse(jsonData).getAsJsonArray();
			for (JsonElement jsonElement : arry) {
				list.add(gson.fromJson(jsonElement, cls));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
