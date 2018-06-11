package com.travel.usercenter.entity;

import java.io.Serializable;

/**
 * 查看明细的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/14
 * 
 */
public class DetailsViewBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String month;
	
	private int type;
	
	private String detailsType;
	private String time;
	private String money;
	private String status;
	private String desc;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDetailsType() {
		return detailsType;
	}

	public void setDetailsType(String detailsType) {
		this.detailsType = detailsType;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}


}
