package com.travel.bean;

import java.io.Serializable;

/**
 * 商品列表的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 * 
 */
public class GoodsListBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private int gType;// 当前类型 0：公告(已不用) 1：集合 2：集合(暂不用) 3：推荐商品
	private int id;// gType是1时需要用的
	private GoodsBasicInfoBean goodsBasicInfoBean;// 商品基本数据
	private PersonalInfoBean personalInfoBean;// 用户信息

	public GoodsListBean() {
		goodsBasicInfoBean = new GoodsBasicInfoBean();
		personalInfoBean = new PersonalInfoBean();
	}
	
	public int getgType() {
		return gType;
	}

	public void setgType(int gType) {
		this.gType = gType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public GoodsBasicInfoBean getGoodsBasicInfoBean() {
		return goodsBasicInfoBean;
	}

	public void setGoodsBasicInfoBean(GoodsBasicInfoBean goodsBasicInfoBean) {
		this.goodsBasicInfoBean = goodsBasicInfoBean;
	}

	public PersonalInfoBean getPersonalInfoBean() {
		return personalInfoBean;
	}

	public void setPersonalInfoBean(PersonalInfoBean personalInfoBean) {
		this.personalInfoBean = personalInfoBean;
	}
}
