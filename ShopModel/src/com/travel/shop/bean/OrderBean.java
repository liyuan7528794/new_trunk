package com.travel.shop.bean;

import java.io.Serializable;

/**
 * 订单管理的实体类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/15
 */
public class OrderBean implements Serializable {
    public static final int STATUS_1 = 1;//下单成功（待支付）
    public static final int STATUS_2 = 2;// 支付成功（待二次确认）
    public static final int STATUS_3 = 3;//支付成功/二次确认成功（待出行）
    public static final int STATUS_4 = 4;//出行完成（待确认行程-确认付款）
    public static final int STATUS_5 = 5;//服务完成(待评价订单)
    public static final int STATUS_6 = 6;//已评价
    public static final int STATUS_7 = 7;//订单取消(未支付-----买家取消)
    public static final int STATUS_8 = 8;//订单取消(二次确认-卖家拒绝)
    public static final int STATUS_9 = 9;//订单取消(支付超时-系统取消)
    public static final int STATUS_10 = 10;//订单取消(买家退款-成功取消)



    private static final long serialVersionUID = 1L;

    private long ordersId;// 订单号
    private int statusManage;// 订单管理状态 1 进行中 2 已完成
    private String goodsTitle;// 商品标题
    private int status;// 订单状态
    private String goodsImg;// 商品图片
    private float totalPrice;// 总价
    private float paymentPrice;// 支付金额
    private String startTime;// 出行时间
    private String buyerId;// 买家Id
    private String salerId;// 卖家Id
    private String name;// 买家或卖家姓名
    private boolean isBrowsed;// 是否浏览
    private int goodsType;
    private String goodsAddress;
    private int checkStatus;// 供应商是否扫过 0:未扫 1:扫了
    private AttachGoodsBean attachGoodsBean;

    private int adultNum; // 成人数量
    private int totalNum; // 累计数量
    private int childNum; // 儿童数量
    private String departCity; // 出发地
    private String destCity; // 目的地
    private int publicStatus; // 退款状态
    private int refundStatus; // 众投状态
    private String goodsId;


    public OrderBean(){
        attachGoodsBean = new AttachGoodsBean();
    }

    public long getOrdersId() {
        return ordersId;
    }

    public void setOrdersId(long ordersId) {
        this.ordersId = ordersId;
    }

    public int getStatusManage() {
        return statusManage;
    }

    public void setStatusManage(int statusManage) {
        this.statusManage = statusManage;
    }

    public String getGoodsTitle() {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle) {
        this.goodsTitle = goodsTitle;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public float getPaymentPrice() {
        return paymentPrice;
    }

    public void setPaymentPrice(float paymentPrice) {
        this.paymentPrice = paymentPrice;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSalerId() {
        return salerId;
    }

    public void setSalerId(String salerId) {
        this.salerId = salerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBrowsed() {
        return isBrowsed;
    }

    public void setBrowsed(boolean browsed) {
        isBrowsed = browsed;
    }

    public int getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(int goodsType) {
        this.goodsType = goodsType;
    }

    public String getGoodsAddress() {
        return goodsAddress;
    }

    public void setGoodsAddress(String goodsAddress) {
        this.goodsAddress = goodsAddress;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public AttachGoodsBean getAttachGoodsBean() {
        return attachGoodsBean;
    }

    public void setAttachGoodsBean(AttachGoodsBean attachGoodsBean) {
        this.attachGoodsBean = attachGoodsBean;
    }

    public int getAdultNum() {
        return adultNum;
    }

    public void setAdultNum(int adultNum) {
        this.adultNum = adultNum;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }

    public String getDepartCity() {
        return departCity;
    }

    public void setDepartCity(String departCity) {
        this.departCity = departCity;
    }

    public String getDestCity() {
        return destCity;
    }

    public void setDestCity(String destCity) {
        this.destCity = destCity;
    }

    public int getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(int publicStatus) {
        this.publicStatus = publicStatus;
    }

    public int getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(int refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }
}
