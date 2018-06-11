package com.travel.video.gift;

import android.text.TextUtils;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;

import org.json.JSONException;
import org.json.JSONObject;

public class GiftBean {
	private int id,image,beans,num,price;
	private String typeId,name,userName,userImage, userId;;

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public int getBeans() {
		return beans;
	}

	public void setBeans(int beans) {
		this.beans = beans;
	}
	
	public void marrayImage(int ids){
		switch (ids) {
		case 1:
			setImage(R.drawable.gift_01);
			break;
		case 2:
			setImage(R.drawable.gift_02);
			break;
		case 3:
			setImage(R.drawable.gift_03);
			break;

		default:
			break;
		}
	}
	public String getJson(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
			jsonObject.put("num", num);
			jsonObject.put("name",name );
			jsonObject.put("userId", getUserId());
			jsonObject.put("userNickName", getUserName());
			jsonObject.put("userImg", getUserImage());
		} catch (JSONException e) {
			MLog.e("GiftBean", e.getMessage(), e);
		}
		return jsonObject.toString();
	}
	
	public static GiftBean getGiftBeanOfJson(String giftBeanStr){
		if(TextUtils.isEmpty(giftBeanStr)) return null;
		GiftBean giftBean = new GiftBean();
		try {
			JSONObject giftJson = new JSONObject(giftBeanStr);
			giftBean.setId(giftJson.getInt("id"));
			giftBean.setName(giftJson.getString("name"));
			giftBean.setNum(giftJson.getInt("num"));
			giftBean.marrayImage(giftJson.getInt("id"));

			giftBean.setUserName(giftJson.getString("userNickName"));
			String userImage = JsonUtil.getJson(giftJson, "userImg");
			if("".equals(userImage)){
				userImage = Constants.DefaultHeadImg;
			}
			giftBean.setUserImage(userImage);
			giftBean.setUserId(giftJson.getString("userId"));
			return giftBean;
		} catch (JSONException e) {
			MLog.e("GiftBean", e.getMessage(), e);
		}
		
		return null;
	}
	
}
