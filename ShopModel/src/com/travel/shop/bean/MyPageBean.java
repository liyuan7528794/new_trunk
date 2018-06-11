package com.travel.shop.bean;

import java.io.Serializable;

/**
 * 我的主页的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/04/25
 * 
 */
public class MyPageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String myImgUrl;
	private String myName;
	private String myAddress;
	private boolean isMyAttention;
	private String myInfo;
	private int userType;

	public String getMyImgUrl() {
		return myImgUrl;
	}

	public void setMyImgUrl(String myImgUrl) {
		this.myImgUrl = myImgUrl;
	}

	public String getMyName() {
		return myName;
	}

	public void setMyName(String myName) {
		this.myName = myName;
	}

	public String getMyAddress() {
		return myAddress;
	}

	public void setMyAddress(String myAddress) {
		this.myAddress = myAddress;
	}

	public boolean isMyAttention() {
		return isMyAttention;
	}

	public void setMyAttention(boolean isMyAttention) {
		this.isMyAttention = isMyAttention;
	}

	public String getMyInfo() {
		return myInfo;
	}

	public void setMyInfo(String myInfo) {
		this.myInfo = myInfo;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}
}
