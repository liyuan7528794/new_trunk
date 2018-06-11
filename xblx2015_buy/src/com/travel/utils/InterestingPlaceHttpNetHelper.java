/*
package com.travel.utils;

import android.util.Log;

import com.travel.Constants;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.helper.SQliteHelper;
import com.travel.entity.ScenicLiveEntity;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.Response.ErrorListener;
import com.volley.Response.Listener;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
*/
/**
 *  在景区群中用到的所有http网络交互辅助类 
 *//*

public class InterestingPlaceHttpNetHelper {
	private static final String TAG = "InterestingPlaceHttpNetHelper";
	private InterestingPlaceActivity context;

	public InterestingPlaceHttpNetHelper(InterestingPlaceActivity context) {
		this.context = context;
	}

	*/
/**
	 * 关注动作
	 * 
	 * @param myId
	 *            操作者id
	 * @param toId
	 *            关注或取消关注的对象Id
	 * @param type
	 *            关注对象的类型, 1 关注人, 2 关注群
	 * @param status
	 *            1 关注, 2 取消关注
	 *//*

	public void followAction(String myId, String toId, int type, final int status) {
		String url = Constants.Root_Url + "/user/userFollow.do";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("myId", myId);
		map.put("toId", toId);
		map.put("type", String.valueOf(type));
		map.put("status", String.valueOf(status));
		for (Object key : map.keySet()) {
			Log.v(TAG, "key is " + key + ", and value is " + map.get(key));
		}
		NetWorkUtil.postForm(context, url, new MResponseListener() {

			@Override
			protected void onDataFine(JSONObject data) {
				context.onFollowResult(true);
			}
			
			@Override
			protected void onDataFine(String data) {
				context.onFollowResult(true);
			}

			@Override
			protected void onErrorNotZero(int error, String msg) {
				MLog.e(TAG, "onErrorNotZero");
				context.onFollowResult(false);
			}

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, error.getMessage(), error);
				context.onFollowResult(false);
			}
		}, map);
	}

	*/
/**
	 * 获取景区详情
	 * @param roomNum
	 * @param userId
	 *//*

	public void getScenicInfo(String roomNum, String userId) {
		String url = Constants.Root_Url + "/live/intoScenic.do";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("roomNum", roomNum);
		map.put("userId", userId);
		NetWorkUtil.postForm(context, url, new MResponseListener() {

			@Override
			protected void onDataFine(JSONObject data) {
				if (data == null)
					return;
				try {
					boolean isFollowed = JsonUtil.getJsonBoolean(data, "follow", false);
					boolean isBlackList = JsonUtil.getJsonBoolean(data, "blackList", false);
					JSONObject scenicJSONObject = (JSONObject) data.get("scenic");
					String notice = JsonUtil.getJson(scenicJSONObject, "notice");
					String introduce = JsonUtil.getJson(scenicJSONObject, "introduceHTML");
					context.onGetScenicInfoResult(notice, introduce);
					String roomId = JsonUtil.getJson(scenicJSONObject, "id");
					context.onGetScenicInfoResult(roomId, isFollowed, isBlackList);
				} catch (JSONException e) {
					MLog.e(TAG, e.getMessage());
				}
			}
		}, map);
	}

	*/
/**
	 * 获取直播监控列表
	 * @param roomNum
	 * @param userId
	 * @param scenicId
     *//*

	public void getScenicLiveList(String roomNum, String userId, String scenicId){
		String url = Constants.Root_Url + "/live/scenicLiveList.do";
		Map<String, Object>map = new HashMap<String, Object>();
		map.put("roomNum", roomNum);
		map.put("userId", userId);
		map.put("scenicId", scenicId);

		NetWorkUtil.postForm(context, url, new MResponseListener() {
			@Override
			protected void onDataFine(JSONObject data) {
			}

			@Override
			protected void onDataFine(JSONArray data) {
				List<ScenicLiveEntity> list = new ArrayList<ScenicLiveEntity>();
				for (int i = 0; i < data.length(); i++) {
					JSONObject scenicLivesJsonObject = JsonUtil.getJSONObject(data, i);
					if (scenicLivesJsonObject == null)
						continue;
					list.add(ScenicLiveEntity.generateFromJson(scenicLivesJsonObject));
				}
				context.onGetScenicLiveResult(list);
			}
		}, map);
	}
	*/
/**
	 * 获取解说排队列表
	 * @param roomNum  房间号
	 * @param liveId   背景视频Id
	 *//*

	public void getExplainUserList(String roomNum, final String liveId){
		String url = Constants.Root_Url + "/live/explainUserList.do";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("roomNum", roomNum);
		map.put("scenicLiveId", liveId);
		NetWorkUtil.postForm(context, url, new MResponseListener() {
			
			@Override
			protected void onDataFine(JSONObject data) {
				
			}
			
			@Override
			protected void onDataFine(JSONArray data) {
				super.onDataFine(data);
				List<UserData> list = new ArrayList<UserData>();
				if(data == null){
					context.onGetExplainUserList(liveId, list);
					return;
				}
				for(int i = 0; i < data.length(); i++){
					JSONObject jsonObject = JsonUtil.getJSONObject(data, i);
					if(jsonObject == null) continue;
					list.add(UserData.generateUserData(jsonObject));
				}
				context.onGetExplainUserList(liveId, list);
			}
		}, map);
	}
	*/
/**
	 * 获取景区聊天群用户列表
	 * @param roomNum  房间号
	 *//*

	public void getScenicUserList(String roomNum){
		MLog.v(TAG, "getScenicUserList");
		String url = Constants.Root_Url + "/live/scenicUserList.do";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("roomNum", roomNum);
		NetWorkUtil.postForm(context, url, new MResponseListener() {
			
			@Override
			protected void onDataFine(JSONObject data) {
			}
			
			@Override
			protected void onDataFine(JSONArray data) {
				super.onDataFine(data);
				List<UserData> list = new ArrayList<UserData>();
				if(data != null){
					for(int i = 0; i < data.length(); i++){
						UserData userData = UserData.generateUserData(JsonUtil.getJSONObject(data, i));
						list.add(userData);
					}
				}
				context.onGetScenicUserList(list);
			}
		}, map);
	}
	
	public void getHistoryMessageList(String roomNum, final SQliteHelper sqHelper){
		MLog.v(TAG, "getHistoryMessageList and roomNum is " + roomNum);
		String url = Constants.Root_Url + "/live/chattingRecords.do";
		Map<String, String> map = new HashMap<String, String>();
		map.put("roomNum", roomNum);
		NetWorkUtil.postFormGetString(url, map, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				MLog.v(TAG, "getHistoryMessageList, and response is " + response);
				try {
					JSONArray jsonArray = new JSONArray(response);
					if(response == null) return;
					List<MessageEntity> messageList = new ArrayList<MessageEntity>();
					for(int i = jsonArray.length() - 1; i >= 0; i--){
						JSONObject jsonObject = JsonUtil.getJSONObject(jsonArray, i);
						MessageEntity messageEntity = SaveChatMsgToLocalDB.resolveMessage(true, sqHelper, jsonObject, false);
						messageList.add(messageEntity);
					}
					
					context.onGetHistoryMessageList(messageList);
				} catch (JSONException e) {
					MLog.e(TAG, e.getMessage());
				}
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				MLog.v(TAG, "onErrorResponse");
				context.onGetHistoryMessageList(null);
			}
		});
	}
}
*/
