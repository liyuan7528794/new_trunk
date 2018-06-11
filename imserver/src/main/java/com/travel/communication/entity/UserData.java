package com.travel.communication.entity;

import android.text.TextUtils;

import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用于用户信息的存储
 */
public class UserData extends com.travel.communication.dao.UserData{
	// 重写toString方法是因为
	// SortModel中使用了toString作为获取name

	@Override
	public String toString() {
		return getNickName();
	}
	
	public UserData(){
		super();
	}
	
	public static final UserData generateUserData(JSONObject jsonObject){
		String _id = JsonUtil.getJson(jsonObject, "id");
		if(TextUtils.isEmpty(_id)){
			_id = String.valueOf(JsonUtil.getJsonInt(jsonObject, "id"));
		}
		String _nickName = JsonUtil.getJson(jsonObject, "nickName");
		String _imgUrl = JsonUtil.getJson(jsonObject, "imgUrl");
		if(TextUtils.isEmpty(_id) || _id.equals("-1")){
			_id = JsonUtil.getJson(jsonObject, "userId");
			if(TextUtils.isEmpty(_id)){
				_id = String.valueOf(JsonUtil.getJsonInt(jsonObject, "userId"));
			}
			if(TextUtils.isEmpty(_id) || _id.equals("-1")){
				return null;
			}
		}
		UserData userData = new UserData();
		userData.setId(_id);
		userData.setNickName(_nickName);
		userData.setImgUrl(_imgUrl);
		return userData;
	}
	
	public static final UserData generateUserData(String jsonString){
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			return generateUserData(jsonObject);
		} catch (JSONException e) {
			MLog.e("UserData", e.getMessage());
			return null;
		}
	}
}
