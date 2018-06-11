package com.travel.usercenter.entity;

import java.io.Serializable;

/**
 * 充值的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/15
 * 
 */
public class RechargeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String goodsId;
	private String redMoney;
	private String money;
	private String exchangeRatio;
	private boolean isChoosed;

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public String getRedMoney() {
		return redMoney;
	}

	public void setRedMoney(String redMoney) {
		this.redMoney = redMoney;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getExchangeRatio() {
		return exchangeRatio;
	}

	public void setExchangeRatio(String exchangeRatio) {
		this.exchangeRatio = exchangeRatio;
	}

	public boolean isChoosed() {
		return isChoosed;
	}

	public void setChoosed(boolean isChoosed) {
		this.isChoosed = isChoosed;
	}

}
