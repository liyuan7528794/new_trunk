package com.travel.shop.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 卡券详情的实体类
 * Created by wyp on 2017/1/17.
 */

public class CouponInfoBean implements Serializable, Comparable<CouponInfoBean> {
    private String couponId;// 卡券Id
    private String couponImg;// 卡券背景
    private String couponName;// 卡券名称
    private String originalCost;// 卡券原价
    private String currentPrice;// 卡券现价
    private String couponRule;// 卡券使用规则

    private boolean isPresent;// 是否是赠送的
    private String startDate;// 开始日期
    private String endDate;// 结束日期
    private int status;// 卡券状态
    private int statusCoupon;// 卡券是否可用的状态 只有为1时可用（仅是多了层保护，一般用status就够用了）
    private boolean isChoosed;// 是否已选
    private String requirMoney;// 需满足的金额

    private ArrayList<String> goodsIds;// 该券可用的商品id
    private int added;// 是否可叠加 0:不可叠加  1:可叠加

    private boolean isGet;// 是否点击领取

    public CouponInfoBean() {
        goodsIds = new ArrayList<>();
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getCouponImg() {
        return couponImg;
    }

    public void setCouponImg(String couponImg) {
        this.couponImg = couponImg;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getOriginalCost() {
        return originalCost;
    }

    public void setOriginalCost(String originalCost) {
        this.originalCost = originalCost;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getCouponRule() {
        return couponRule;
    }

    public void setCouponRule(String couponRule) {
        this.couponRule = couponRule;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatusCoupon() {
        return statusCoupon;
    }

    public void setStatusCoupon(int statusCoupon) {
        this.statusCoupon = statusCoupon;
    }

    public boolean isChoosed() {
        return isChoosed;
    }

    public void setChoosed(boolean choosed) {
        isChoosed = choosed;
    }

    public String getRequirMoney() {
        return requirMoney;
    }

    public void setRequirMoney(String requirMoney) {
        this.requirMoney = requirMoney;
    }

    public ArrayList<String> getGoodsIds() {
        return goodsIds;
    }

    public void setGoodsIds(ArrayList<String> goodsIds) {
        this.goodsIds = goodsIds;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    @Override
    public int compareTo(CouponInfoBean another) {
        return Integer.parseInt(this.getCurrentPrice()) - Integer.parseInt(another.getCurrentPrice());
    }


    public boolean isGet() {
        return isGet;
    }

    public void setGet(boolean get) {
        isGet = get;
    }
}
