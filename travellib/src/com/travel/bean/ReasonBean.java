package com.travel.bean;

import java.io.Serializable;

/**
 * 理由的实体类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/03/21
 * 
 */
public class ReasonBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private int reasonId;
	private String reason;
	private int flag;

	public int getReasonId() {
		return reasonId;
	}

	public void setReasonId(int reasonId) {
		this.reasonId = reasonId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

}
