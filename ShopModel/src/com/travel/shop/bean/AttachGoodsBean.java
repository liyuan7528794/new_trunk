package com.travel.shop.bean;

import java.io.Serializable;

/**
 * 附加服务的实体类
 * Created by wyp on 2017/4/28.
 */

public class AttachGoodsBean implements Serializable {

    private String attachId; // 附加服务的Id
    private String attachName; // 附加服务的名称
    private String price; // 价格
    private String unit; // 同线路游。。。等type
    private int count;
    private String totalPrice;// 当前选择的服务一共需要的价格

    public String getAttachId() {
        return attachId;
    }

    public void setAttachId(String attachId) {
        this.attachId = attachId;
    }

    public String getAttachName() {
        return attachName;
    }

    public void setAttachName(String attachName) {
        this.attachName = attachName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
