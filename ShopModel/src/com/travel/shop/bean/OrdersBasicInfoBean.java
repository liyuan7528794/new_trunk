package com.travel.shop.bean;

import java.io.Serializable;

/**
 * 订单基本信息的实体类
 * Created by WYP on 2016/11/25.
 */

public class OrdersBasicInfoBean implements Serializable {

    private long ordersId;// 订单Id
    private String goodsSnapshootId;// 商品快照Id
    private int statusManage;// 订单管理状态 1 进行中 2 已完成
    private String serviceId;// 服务Id
    private float totalPrice;// 总价
    private String travelDays;// 行程天数
    private int childrenCount;// 儿童数
    private String remarkInfoSeller;// 卖家备注信息
    private int isCheck;//是否被扫 0：未扫 1:扫过
    private String attachPrice;// 用于判断是否有附加服务 0:无 >0: 有

    private int status;// 订单状态
    private int refundStatus;// 退款状态
    private int publicStatus;// 众投状态
    private float refundMoney;// 退款金额
    private String buyerId;// 买家Id
    private String sellerId;// 卖家Id
    private String startDate;// 出行日期
    private float paymentPrice;// 应付金额
    private String buyerName;// 买家姓名
    private String buyerPhone;// 买家电话
    private String remarkInfoBuyer;// 买家备注信息
    private String userinfo;// 人员信息
    private float adultPrice;// 成人价
    private int adultCount;// 成人数
    private int cardCount;// 使用的小城卡数
    private float terraceDiscountPrice; // 平台优惠金额
    private float sellerDiscountPrice; // 优惠金额(商家改价)
    private float singlePrice;// 单房差价
    private int singleCount;// 单房差数
    private String createTime;// 下单时间
    private String payTime;// 支付时间
    private String refundTime;// 退款时间

    public long getOrdersId() {
        return ordersId;
    }

    public void setOrdersId(long ordersId) {
        this.ordersId = ordersId;
    }

    public String getGoodsSnapshootId() {
        return goodsSnapshootId;
    }

    public void setGoodsSnapshootId(String goodsSnapshootId) {
        this.goodsSnapshootId = goodsSnapshootId;
    }

    public int getStatusManage() {
        return statusManage;
    }

    public void setStatusManage(int statusManage) {
        this.statusManage = statusManage;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public float getTerraceDiscountPrice() {
        return terraceDiscountPrice;
    }

    public void setTerraceDiscountPrice(float terraceDiscountPrice) {
        this.terraceDiscountPrice = terraceDiscountPrice;
    }

    public float getPaymentPrice() {
        return paymentPrice;
    }

    public void setPaymentPrice(float paymentPrice) {
        this.paymentPrice = paymentPrice;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getTravelDays() {
        return travelDays;
    }

    public void setTravelDays(String travelDays) {
        this.travelDays = travelDays;
    }

    public int getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(int adultCount) {
        this.adultCount = adultCount;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public int getSingleCount() {
        return singleCount;
    }

    public void setSingleCount(int singleCount) {
        this.singleCount = singleCount;
    }

    public String getUserinfo() {
        return userinfo;
    }

    public void setUserinfo(String userinfo) {
        this.userinfo = userinfo;
    }

    public String getRemarkInfoBuyer() {
        return remarkInfoBuyer;
    }

    public void setRemarkInfoBuyer(String remarkInfoBuyer) {
        this.remarkInfoBuyer = remarkInfoBuyer;
    }

    public String getRemarkInfoSeller() {
        return remarkInfoSeller;
    }

    public void setRemarkInfoSeller(String remarkInfoSeller) {
        this.remarkInfoSeller = remarkInfoSeller;
    }

    public int getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(int isCheck) {
        this.isCheck = isCheck;
    }

    public String getAttachPrice() {
        return attachPrice;
    }

    public void setAttachPrice(String attachPrice) {
        this.attachPrice = attachPrice;
    }

    public int getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(int refundStatus) {
        this.refundStatus = refundStatus;
    }

    public int getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(int publicStatus) {
        this.publicStatus = publicStatus;
    }

    public float getRefundMoney() {
        return refundMoney;
    }

    public void setRefundMoney(float refundMoney) {
        this.refundMoney = refundMoney;
    }

    public float getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(float adultPrice) {
        this.adultPrice = adultPrice;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public float getSinglePrice() {
        return singlePrice;
    }

    public void setSinglePrice(float singlePrice) {
        this.singlePrice = singlePrice;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(String refundTime) {
        this.refundTime = refundTime;
    }

    public float getSellerDiscountPrice() {
        return sellerDiscountPrice;
    }

    public void setSellerDiscountPrice(float sellerDiscountPrice) {
        this.sellerDiscountPrice = sellerDiscountPrice;
    }
}

