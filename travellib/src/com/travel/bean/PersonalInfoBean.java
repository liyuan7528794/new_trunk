package com.travel.bean;

import java.io.Serializable;

/**
 * 个人信息的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 * 
 */
public class PersonalInfoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userId;// 用户Id
	private String userPhoto;// 用户头像
	private String userName;// 用户姓名
	private String userAddress;// 用户地址
	private String userInfo;// 用户简介
	private boolean isLogin;// 用户在线状态
	private boolean isAttention;// 对该用户是否关注
	private String hashId;// 该用户是否在直播 "-1":没在直播
	private String liveUrl;// 该用户有直播时的视频地址
	private String userType;// 用户类型 1:卖家 2:普通用户 3:背包用户
	private String userPhone;// 用户电话
	private String liveId;// 直播Id

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPhoto() {
		return userPhoto;
	}

	public void setUserPhoto(String userPhoto) {
		this.userPhoto = userPhoto;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}

	public boolean isAttention() {
		return isAttention;
	}

	public void setAttention(boolean isAttention) {
		this.isAttention = isAttention;
	}

	public String getHashId() {
		return hashId;
	}

	public void setHashId(String hashId) {
		this.hashId = hashId;
	}

	public String getLiveUrl() {
		return liveUrl;
	}

	public void setLiveUrl(String liveUrl) {
		this.liveUrl = liveUrl;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getLiveId() {
		return liveId;
	}

	public void setLiveId(String liveId) {
		this.liveId = liveId;
	}
}
