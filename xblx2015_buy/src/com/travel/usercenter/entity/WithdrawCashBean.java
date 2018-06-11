package com.travel.usercenter.entity;

import java.io.Serializable;

/**
 * 获取平台提成比例相关的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/17
 * 
 */
public class WithdrawCashBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private int upperLimit;
	private int lowerLimit;
	private float ratio;
	private int minNum;
	private int exchangeRatio;

	public int getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(int upperLimit) {
		this.upperLimit = upperLimit;
	}

	public int getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(int lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public float getRatio() {
		return ratio;
	}

	public void setRatio(float ratio) {
		this.ratio = ratio;
	}

	public int getMinNum() {
		return minNum;
	}

	public void setMinNum(int minNum) {
		this.minNum = minNum;
	}

	public int getExchangeRatio() {
		return exchangeRatio;
	}

	public void setExchangeRatio(int exchangeRatio) {
		this.exchangeRatio = exchangeRatio;
	}
}
