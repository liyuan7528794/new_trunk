package com.travel.bean;

import java.io.Serializable;

import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsOtherInfoBean;
import com.travel.bean.PersonalInfoBean;

/**
 * 商品详情的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 * 
 */
public class GoodsDetailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private GoodsBasicInfoBean goodsBasicInfoBean;
	private GoodsOtherInfoBean goodsOtherInfoBean;
	private PersonalInfoBean personalInfoBean;

	public GoodsDetailBean() {
		goodsBasicInfoBean = new GoodsBasicInfoBean();
		goodsOtherInfoBean = new GoodsOtherInfoBean();
		personalInfoBean = new PersonalInfoBean();
	}

	public GoodsBasicInfoBean getGoodsBasicInfoBean() {
		return goodsBasicInfoBean;
	}

	public void setGoodsBasicInfoBean(GoodsBasicInfoBean goodsBasicInfoBean) {
		this.goodsBasicInfoBean = goodsBasicInfoBean;
	}

	public GoodsOtherInfoBean getGoodsOtherInfoBean() {
		return goodsOtherInfoBean;
	}

	public void setGoodsOtherInfoBean(GoodsOtherInfoBean goodsOtherInfoBean) {
		this.goodsOtherInfoBean = goodsOtherInfoBean;
	}

	public PersonalInfoBean getPersonalInfoBean() {
		return personalInfoBean;
	}

	public void setPersonalInfoBean(PersonalInfoBean personalInfoBean) {
		this.personalInfoBean = personalInfoBean;
	}
}
