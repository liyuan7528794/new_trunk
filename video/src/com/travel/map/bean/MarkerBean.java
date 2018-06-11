package com.travel.map.bean;

import com.travel.Constants;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarkerBean {
	private String id,title,url,imageUrl,userId,userName,userImage,liveType,liveUserType,activityId;
	private double latitude,longitude;
	private long updateTime;
	
	
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	public String getLiveUserType() {
		return liveUserType;
	}
	public void setLiveUserType(String liveUserType) {
		this.liveUserType = liveUserType;
	}
	
	public String getLiveType() {
		return liveType;
	}
	public void setLiveType(String liveType) {
		this.liveType = liveType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserImage() {
		return userImage;
	}
	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public List<MarkerBean> getMarkers(JSONArray data){
		List<MarkerBean> list = new ArrayList<MarkerBean>();
		try {
			for (int i = 0; i < data.length(); i++) {
				JSONObject live = data.getJSONObject(i);
				MarkerBean marker = new MarkerBean();
				marker.setId(JsonUtil.getJson(live, "id"));
				marker.setTitle(JsonUtil.getJson(live, "title"));
				marker.setUrl(JsonUtil.getJson(live, "url"));
				marker.setLiveType(JsonUtil.getJson(live, "type"));
				marker.setImageUrl(JsonUtil.getJson(live, "imgUrl"));
				marker.setLatitude(Double.valueOf(JsonUtil.getJson(live, "latitude")));
				marker.setLongitude(Double.valueOf(JsonUtil.getJson(live, "longitude")));
				marker.setActivityId(JsonUtil.getJson(live, "activityId"));
				if (live.has("user")) {
					JSONObject userJson = live.getJSONObject("user");
					marker.setUserId(JsonUtil.getJson(userJson, "id"));
					marker.setUserImage(JsonUtil.getJson(userJson, "imgUrl"));
					marker.setUserName(JsonUtil.getJson(userJson, "nickName"));
					marker.setLiveUserType(JsonUtil.getJson(userJson, "userType"));
				}
				list.add(marker);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public List<MarkerBean> getMarkers(){
		List<MarkerBean> list = new ArrayList<MarkerBean>();
		MarkerBean marker1 = new MarkerBean();
		marker1.setId("1");
		marker1.setUserImage(UserSharedPreference.getUserHeading());
		marker1.setTitle("北京市中关村经纬度");
		marker1.setUserName("张三1");
		marker1.setLatitude(39.983456);
		marker1.setLongitude(116.3154950);
		MarkerBean marker2 = new MarkerBean();
		marker2.setId("2");
		marker2.setUserImage(Constants.DefaultHeadImg);
		marker2.setTitle("成都市经纬度");
		marker2.setUserName("张三2");
		marker2.setLatitude(30.679879);
		marker2.setLongitude(104.064855);
		MarkerBean marker3 = new MarkerBean();
		marker3.setId("3");
		marker3.setUserImage("http://p3.so.qhimg.com/t01452c07f26378d5f8.jpg");
		marker3.setTitle("方恒国际中心经纬度");
		marker3.setUserName("张三3");
		marker3.setLatitude(39.989614);
		marker3.setLongitude(116.481763);
		MarkerBean marker4 = new MarkerBean();
		marker4.setId("1");
		marker4.setUserImage(UserSharedPreference.getUserHeading());
		marker4.setTitle("上海市");
		marker4.setUserName("张三4");
		marker4.setLatitude(31.238068);
		marker4.setLongitude(121.501654);
		list.add(marker1);
		list.add(marker2);
		list.add(marker3);
		list.add(marker4);
		
		return list;
	}
}
