package com.travel.video.bean;

public class ActivityVoteInfo {
	private String id,nickName,imgUrl,place;
	private boolean followStatus,loginStatus;
	private int voteNum;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public boolean isFollowStatus() {
		return followStatus;
	}
	public void setFollowStatus(boolean followStatus) {
		this.followStatus = followStatus;
	}
	public boolean isLoginStatus() {
		return loginStatus;
	}
	public void setLoginStatus(boolean loginStatus) {
		this.loginStatus = loginStatus;
	}
	public int getVoteNum() {
		return voteNum;
	}
	public void setVoteNum(int voteNum) {
		this.voteNum = voteNum;
	}
	
	
}
