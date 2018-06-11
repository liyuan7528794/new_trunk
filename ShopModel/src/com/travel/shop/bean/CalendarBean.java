package com.travel.shop.bean;

import java.io.Serializable;

public class CalendarBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String packageId;// 套餐Id
	private String calendarId;// 日历Id
	private String goodsId;
	private String date;// 日期
	private String adult_price;// 成人价
	private String children_price;// 儿童价
	private String single_room_price;
	private String year;
	private String month;
	private boolean isStartCheck = false; // 是否在日历上选择开始日期
	private boolean isEndCheck = false; // 是否在日历上选择结束日期
	private int index ;// OrderData中ArrayList中的游标位置
	private int childIndex;// startDates中ArrayList中的游标位置
	private int position;// 当前日期处于当月日历的位置
	private int flag_month;// 当前标识的月份
	private boolean isCheckBox;// 是否可多选

	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAdult_price() {
		return adult_price;
	}

	public void setAdult_price(String adult_price) {
		this.adult_price = adult_price;
	}

	public String getChildren_price() {
		return children_price;
	}

	public void setChildren_price(String children_price) {
		this.children_price = children_price;
	}

	public String getSingle_room_price() {
		return single_room_price;
	}

	public void setSingle_room_price(String single_room_price) {
		this.single_room_price = single_room_price;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getFlag_month() {
		return flag_month;
	}

	public void setFlag_month(int flag_month) {
		this.flag_month = flag_month;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getChildIndex() {
		return childIndex;
	}

	public void setChildIndex(int childIndex) {
		this.childIndex = childIndex;
	}

	public boolean isStartCheck() {
		return isStartCheck;
	}

	public void setStartCheck(boolean isStartCheck) {
		this.isStartCheck = isStartCheck;
	}

	public boolean isEndCheck() {
		return isEndCheck;
	}

	public void setEndCheck(boolean isEndCheck) {
		this.isEndCheck = isEndCheck;
	}
	public boolean isCheckBox() {
		return isCheckBox;
	}

	public void setCheckBox(boolean isCheckBox) {
		this.isCheckBox = isCheckBox;
	}
}
