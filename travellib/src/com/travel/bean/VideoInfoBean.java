package com.travel.bean;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.travel.Constants;
import com.travel.lib.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 视频信息的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 * 
 */
public class VideoInfoBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String videoId;// 视频Id
	private int hashId = -1;
	private String videoTitle;// 视频标题
	private String videoImg;// 视频封面
	private String url;// 视频播放地址
	private String activityId;// 视频所属活动Id
	private String share;// 视频分享字段
	private String longitude;// 经度
	private String latitude;// 纬度
	private String createTime;// 创建视频的时间
	private long timestamp;// 创建视频的时间戳
	private int videoType;// 用来添加标签，1标识回放，2标识回放的宣传片
	private int videoStatus;// 视频状态 1:直播 2:回放
	private int watchCount;// 观看人数
	private int commentCount;// 评论人数
	private int praiseNum;// 点赞数
	private int voteNum;// 视频投票数
	private String rtmpAddress;
	private String shareAddress; // 分享地址
	private String releaseAddress; // 发布地址
	private String releaseTime; // 发布时间
	private String videoDescription; // 视频介绍
	private String cityName;// 相关城市名
	private String cityImg;// 相关城市图片
	private String storyId;// 相关故事的id
	private String goodsId;// 相关商品的id

	private int playStatus;// 播放状态 0：播放 1：暂停
	private int shareNum;// 分享数

	private PersonalInfoBean personalInfoBean;// 视频发布者的相关信息

	public VideoInfoBean() {
		personalInfoBean = new PersonalInfoBean();
	}

	public boolean isNullVideoUrl(){
		if(videoId == null || url == null || "".equals(url)){
			return true;
		}
		return false;
	}

	public String getShareAddress() {
		return shareAddress;
	}

	public void setShareAddress(String shareAddress) {
		this.shareAddress = shareAddress;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public int getVideoStatus() {
		return videoStatus;
	}

	public void setVideoStatus(int videoStatus) {
		this.videoStatus = videoStatus;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
	}

	public int getVoteNum() {
		return voteNum;
	}

	public void setVoteNum(int voteNum) {
		this.voteNum = voteNum;
	}

	public String getVideoImg() {
		return videoImg = (videoImg!=null && !"".equals(videoImg)) ? videoImg : Constants.DefaultHeadImg;
	}

	public void setVideoImg(String videoImg) {
		this.videoImg = videoImg;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public PersonalInfoBean getPersonalInfoBean() {
		return personalInfoBean;
	}

	public void setPersonalInfoBean(PersonalInfoBean personalInfoBean) {
		this.personalInfoBean = personalInfoBean;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getShare() {
		return share;
	}

	public void setShare(String share) {
		this.share = share;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getVideoType() {
		return videoType;
	}

	public void setVideoType(int videoType) {
		this.videoType = videoType;
	}

	
	public int getWatchCount() {
		return watchCount;
	}

	public void setWatchCount(int watchCount) {
		this.watchCount = watchCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public int getPraiseNum() {
		return praiseNum;
	}

	public int getHashId() {
		return hashId;
	}

	public void setHashId(int hashId) {
		this.hashId = hashId;
	}

	public void setPraiseNum(int praiseNum) {
		this.praiseNum = praiseNum;
	}

	public String getRtmpAddress() {
		return rtmpAddress;
	}

	public void setRtmpAddress(String rtmpAddress) {
		this.rtmpAddress = rtmpAddress;
	}

	public String getReleaseAddress() {
		return releaseAddress;
	}

	public void setReleaseAddress(String releaseAddress) {
		this.releaseAddress = releaseAddress;
	}

	public String getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(String releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getVideoDescription() {
		return videoDescription;
	}

	public void setVideoDescription(String videoDescription) {
		this.videoDescription = videoDescription;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCityImg() {
		return cityImg;
	}

	public void setCityImg(String cityImg) {
		this.cityImg = cityImg;
	}

	public String getStoryId() {
		return storyId;
	}

	public void setStoryId(String storyId) {
		this.storyId = storyId;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public int getPlayStatus() {
		return playStatus;
	}

	public void setPlayStatus(int playStatus) {
		this.playStatus = playStatus;
	}

	public int getShareNum() {
		return shareNum;
	}

	public void setShareNum(int shareNum) {
		this.shareNum = shareNum;
	}

	@Override
	public String toString() {
		return "VideoInfoBean [videoId=" + videoId + ", videoStatus=" + videoStatus + ", videoTitle=" + videoTitle
				+ ", videoImg=" + videoImg + ", url=" + url + ", videoType=" + videoType + ", activityId="
				+ activityId + ", share=" + share + ", longitude=" + longitude + ", latitude=" + latitude
				+ ", createTime=" + createTime + ", watchCount=" + watchCount + ", commentCount=" + commentCount
				+ ", personalInfoBean=" + personalInfoBean + ", voteNum=" + voteNum + "]";
	}

	/**
	 * 将json对象转换成对象
	 * @param json
	 * @return
	 */
	public static VideoInfoBean getVideoInfoBean(JSONObject json) {
		VideoInfoBean bean = new VideoInfoBean();
		try{
			bean.setVideoType(JsonUtil.getJsonInt(json, "liveType"));
			bean.setVideoStatus(JsonUtil.getJsonInt(json, "status"));
			bean.setActivityId(JsonUtil.getJson(json, "activityId"));
			bean.setWatchCount(JsonUtil.getJsonInt(json, "watchNum"));
			bean.setCommentCount(JsonUtil.getJsonInt(json, "commentNum"));
			bean.setPraiseNum(JsonUtil.getJsonInt(json,"praiseNum"));
			
			bean.setVideoId(JsonUtil.getJson(json, "id"));
			bean.setVideoTitle(JsonUtil.getJson(json, "title"));
			bean.setShareAddress(TextUtils.equals("-1",JsonUtil.getJson(json, "place"))?"中国":JsonUtil.getJson(json, "place"));
			bean.setVideoImg(JsonUtil.getJson(json, "imgUrl"));
			bean.setVoteNum(JsonUtil.getJsonInt(json, "voteNum"));
			// TODO:由于群里的hashid传的是url字段 所以有以下写法
			if(TextUtils.isEmpty(JsonUtil.getJson(json, "hashedId")) && JsonUtil.getJson(json, "url").matches("[0-9]+") )
				bean.setHashId(Integer.valueOf(JsonUtil.getJson(json, "url")));
			else
				bean.setHashId(JsonUtil.getJsonInt(json, "hashedId"));

			// TODO: 群里取address为地址
			if(!JsonUtil.getJson(json, "url").startsWith("rtmp:") && !JsonUtil.getJson(json, "url").startsWith("http:") && json.has("address"))
				bean.setUrl(JsonUtil.getJson(json, "address"));
			else
				bean.setUrl(JsonUtil.getJson(json, "url"));
			bean.setShare(JsonUtil.getJson(json, "share"));
			bean.setLatitude(JsonUtil.getJson(json, "latitude"));
			bean.setLongitude(JsonUtil.getJson(json, "longitude"));
			bean.setCreateTime(JsonUtil.getJson(json, "createTime"));
			bean.setTimestamp(JsonUtil.getJsonLong(json, "createAt"));
			if (json.has("user")) {
				JSONObject userJson = json.getJSONObject("user");
				PersonalInfoBean userBean = new PersonalInfoBean();
				userBean.setUserId(JsonUtil.getJson(userJson, "id"));
				userBean.setUserName(JsonUtil.getJson(userJson, "nickName"));
				userBean.setUserPhoto(JsonUtil.getJson(userJson, "imgUrl"));
				userBean.setUserAddress(JsonUtil.getJson(userJson, "place"));
				bean.setPersonalInfoBean(userBean);
			}
			return bean;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public JsonObject toJson(){
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", videoType);
		jsonObject.addProperty("title", videoTitle);
		// 由于苹果用的是address，所以shareAddress没用（很牵强），还有hashId也被url代替了（很无奈）
		jsonObject.addProperty("address", rtmpAddress);
		jsonObject.addProperty("shareAddress", shareAddress);
		jsonObject.addProperty("imgUrl", videoImg);
		jsonObject.addProperty("url", url);
		jsonObject.addProperty("share", share);
		jsonObject.addProperty("status", 1);
		JsonObject user = new JsonObject();
		user.addProperty("id", personalInfoBean.getUserId());
		user.addProperty("nickName", personalInfoBean.getUserName());
		user.addProperty("imgUrl", personalInfoBean.getUserPhoto());
		jsonObject.add("user", user);
		return jsonObject;
	}
}
