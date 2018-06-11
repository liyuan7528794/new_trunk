package com.travel.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 商品其他信息的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 * 
 */
public class GoodsOtherInfoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String productFeatures;// 产品特色
	private ArrayList<GoodsServiceBean> travelPlans;// 行程安排
	private String costImplications;// 费用说明
	private String bookingNotes;// 预订须知
	private String warmTips;// 温馨提示

	public GoodsOtherInfoBean() {
		travelPlans = new ArrayList<GoodsServiceBean>();
	}

	public String getProductFeatures() {
		return productFeatures;
	}

	public void setProductFeatures(String productFeatures) {
		this.productFeatures = productFeatures;
	}

	public ArrayList<GoodsServiceBean> getTravelPlans() {
		return travelPlans;
	}

	public void setTravelPlans(ArrayList<GoodsServiceBean> travelPlans) {
		this.travelPlans = travelPlans;
	}

	public String getCostImplications() {
		return costImplications;
	}

	public void setCostImplications(String costImplications) {
		this.costImplications = costImplications;
	}

	public String getBookingNotes() {
		return bookingNotes;
	}

	public void setBookingNotes(String bookingNotes) {
		this.bookingNotes = bookingNotes;
	}

	public String getWarmTips() {
		return warmTips;
	}

	public void setWarmTips(String warmTips) {
		this.warmTips = warmTips;
	}
}
