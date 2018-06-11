package com.travel.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 商品基本信息的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 * 
 */
public class GoodsBasicInfoBean implements Serializable {

	private String goodsId;// 商品Id
	private String goodsImg;// 商品图片
	private String goodsPrice;// 商品价格
	private String goodsNum;// 商品编号
	private String goodsTitle;// 商品标题
	private int goodsType;// 商品类型 1:线路游 2:自由行 3:酒店 4:向导游 5:门票
	private String goodsAddress;// 商品地址
	private String goodsTradingVolume;// 商品交易量
	private String goodsFeedbackRate;// 商品好评率
	private String label;
	private ArrayList<String> goodsLabelList;// 商品标签
	private String goodsSelectedPackage;// 选择的套餐 -- 订单详情页中使用
	private int goodsDays;// 商品的天数
	private int goodsReserveDays;// 商品的提前预定天数
	private String infoNeed;// 需要填写的信息 若空，则不需要填写
	private int goodsStatus;// 只要不是3，就是失效的商品 -- 我的关注中商品关注里使用
	private int twiceSure;// 1：需要二次确认 2：不需要 -- 预订界面使用
	private String goodsStory;// 商品故事
	private int goodsLayout;// 商品布局 TODO 1.小 2.大

	private int type;// 故事类型 1:纪录片故事 2:图文故事 3:富文本故事 （还有一种网页形式，无需判断这个，只需判断url是否有数据）
	private String storyId;// 故事Id
	private int readCount;// 故事阅读数
	private int commentCount;// 故事评论数

	private String subhead;// 故事副标题
	private String keyWord;

	private int activityId;// 活动Id 为0时没有活动
	private String activityName;// 活动名称

	private String topImage;// 首页小城的背景，若没有，则用goodsImage
	private String descriptionUrl;// 有，则引入H5,否则正常取

	private String content;// 纪录片同款数据
	private String introduceGoods;// 当type是3时的数据

	private boolean isSupportCard;// 该商品是否可以使用小城卡
	private int remainCount;// 小城卡剩余数量

	public GoodsBasicInfoBean() {
		goodsLabelList = new ArrayList<String>();
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public String getGoodsImg() {
		return goodsImg;
	}

	public void setGoodsImg(String goodsImg) {
		this.goodsImg = goodsImg;
	}

	public String getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(String goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	public String getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(String goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getGoodsTitle() {
		return goodsTitle;
	}

	public void setGoodsTitle(String goodsTitle) {
		this.goodsTitle = goodsTitle;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public String getGoodsAddress() {
		return goodsAddress;
	}

	public void setGoodsAddress(String goodsAddress) {
		this.goodsAddress = goodsAddress;
	}

	public String getGoodsTradingVolume() {
		return goodsTradingVolume;
	}

	public void setGoodsTradingVolume(String goodsTradingVolume) {
		this.goodsTradingVolume = goodsTradingVolume;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getGoodsFeedbackRate() {
		return goodsFeedbackRate;
	}

	public void setGoodsFeedbackRate(String goodsFeedbackRate) {
		this.goodsFeedbackRate = goodsFeedbackRate;
	}

	public ArrayList<String> getGoodsLabelList() {
		return goodsLabelList;
	}

	public void setGoodsLabelList(ArrayList<String> goodsLabelList) {
		this.goodsLabelList = goodsLabelList;
	}

	public String getGoodsSelectedPackage() {
		return goodsSelectedPackage;
	}

	public void setGoodsSelectedPackage(String goodsSelectedPackage) {
		this.goodsSelectedPackage = goodsSelectedPackage;
	}

	public int getGoodsDays() {
		return goodsDays;
	}

	public void setGoodsDays(int goodsDays) {
		this.goodsDays = goodsDays;
	}

	public int getGoodsReserveDays() {
		return goodsReserveDays;
	}

	public void setGoodsReserveDays(int goodsReserveDays) {
		this.goodsReserveDays = goodsReserveDays;
	}

	public String getInfoNeed() {
		return infoNeed;
	}

	public void setInfoNeed(String infoNeed) {
		this.infoNeed = infoNeed;
	}

	public int getGoodsStatus() {
		return goodsStatus;
	}

	public void setGoodsStatus(int goodsStatus) {
		this.goodsStatus = goodsStatus;
	}

	public int getTwiceSure() {
		return twiceSure;
	}

	public void setTwiceSure(int twiceSure) {
		this.twiceSure = twiceSure;
	}

	public String getGoodsStory() {
		return goodsStory;
	}

	public void setGoodsStory(String goodsStory) {
		this.goodsStory = goodsStory;
	}

	public int getGoodsLayout() {
		return goodsLayout;
	}

	public void setGoodsLayout(int goodsLayout) {
		this.goodsLayout = goodsLayout;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getStoryId() {
		return storyId;
	}

	public void setStoryId(String storyId) {
		this.storyId = storyId;
	}

	public int getReadCount() {
		return readCount;
	}

	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public String getSubhead() {
		return subhead;
	}

	public void setSubhead(String subhead) {
		this.subhead = subhead;
	}


	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getTopImage() {
		return topImage;
	}

	public void setTopImage(String topImage) {
		this.topImage = topImage;
	}

	public String getDescriptionUrl() {
		return descriptionUrl;
	}

	public void setDescriptionUrl(String descriptionUrl) {
		this.descriptionUrl = descriptionUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getIntroduceGoods() {
		return introduceGoods;
	}

	public void setIntroduceGoods(String introduceGoods) {
		this.introduceGoods = introduceGoods;
	}

	public boolean isSupportCard() {
		return isSupportCard;
	}

	public void setSupportCard(boolean supportCard) {
		isSupportCard = supportCard;
	}

	public int getRemainCount() {
		return remainCount;
	}

	public void setRemainCount(int remainCount) {
		this.remainCount = remainCount;
	}
}
