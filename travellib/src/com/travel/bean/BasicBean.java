package com.travel.bean;

import java.io.Serializable;

/**
 * 第二个选项卡的实体类的基类
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/03/02
 * 
 */
public class BasicBean implements Serializable {

	private static final long serialVersionUID = -5458257700198661851L;
	private String backgroud;
	private String title;

	public String getBackgroud() {
		return backgroud;
	}

	public void setBackgroud(String backgroud) {
		this.backgroud = backgroud;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
