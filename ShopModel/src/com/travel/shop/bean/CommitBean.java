package com.travel.shop.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/17.
 */

public class CommitBean implements Serializable {
    private String goodsId;// 商品Id
    private int goodsType;// 商品类型
    private String skuId;// 日历Id
    private String skuIds;// 多个日历Id ,eg:id1,id2
    private String packageName;// 套餐名称
    private String departTime;// 出行时间
    private String returnTime;// 返回时间
    private int adultNum;// 成人数、票数、房间数
    private int childNum;// 儿童数
    private int roomNum;// 单房差数
    private String buyerName;// 买家姓名
    private String buyerTelephone;// 买家电话
    private String remarks;// 备注
    private String invitationCode;// 邀请码
    private float totalPrice;// 总价
    private int sourceType;// 来源类型
    private String sourceId;// 来源Id
    private String userinfo;// 来源Id
    private String sellerId;// 卖家Id

    private String userCoupnoIds;// 优惠券
    private ArrayList<AttachGoodsBean> attachGoods;
    private String salesCode;// 促销优惠码 F1-20(1:附加服务的Id, 20:优惠金额)

    private String goodsTitle;// 商品标题
    private boolean isTwice;// 是否需要二次确认
    private boolean isInfoNeed;// 是否需要人员信息
    private int remainCount;//剩余次数
    private ArrayList<CouponInfoBean> couponDatas;// 可用的优惠券数据
    private float signlePrice;// 单房差的价格
    private int cardUseCount;// 小城卡使用次数
    private boolean isSupportCard;// 是否可以使用小城卡

    public CommitBean(){
        attachGoods = new ArrayList<>();
        couponDatas = new ArrayList<>();
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(int goodsType) {
        this.goodsType = goodsType;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSkuIds() {
        return skuIds;
    }

    public void setSkuIds(String skuIds) {
        this.skuIds = skuIds;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDepartTime() {
        return departTime;
    }

    public void setDepartTime(String departTime) {
        this.departTime = departTime;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public int getAdultNum() {
        return adultNum;
    }

    public void setAdultNum(int adultNum) {
        this.adultNum = adultNum;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerTelephone() {
        return buyerTelephone;
    }

    public void setBuyerTelephone(String buyerTelephone) {
        this.buyerTelephone = buyerTelephone;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getUserinfo() {
        return userinfo;
    }

    public void setUserinfo(String userinfo) {
        this.userinfo = userinfo;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getUserCoupnoIds() {
        return userCoupnoIds;
    }

    public void setUserCoupnoIds(String userCoupnoIds) {
        this.userCoupnoIds = userCoupnoIds;
    }

    public ArrayList<AttachGoodsBean> getAttachGoods() {
        return attachGoods;
    }

    public void setAttachGoods(ArrayList<AttachGoodsBean> attachGoods) {
        this.attachGoods = attachGoods;
    }

    public String getSalesCode() {
        return salesCode;
    }

    public void setSalesCode(String salesCode) {
        this.salesCode = salesCode;
    }

    public String getGoodsTitle() {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle) {
        this.goodsTitle = goodsTitle;
    }

    public boolean isTwice() {
        return isTwice;
    }

    public void setTwice(boolean twice) {
        isTwice = twice;
    }

    public boolean isInfoNeed() {
        return isInfoNeed;
    }

    public void setInfoNeed(boolean infoNeed) {
        isInfoNeed = infoNeed;
    }

    public int getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
    }

    public ArrayList<CouponInfoBean> getCouponDatas() {
        return couponDatas;
    }

    public void setCouponDatas(ArrayList<CouponInfoBean> couponDatas) {
        this.couponDatas = couponDatas;
    }

    public float getSignlePrice() {
        return signlePrice;
    }

    public void setSignlePrice(float signlePrice) {
        this.signlePrice = signlePrice;
    }

    public int getCardUseCount() {
        return cardUseCount;
    }

    public void setCardUseCount(int cardUseCount) {
        this.cardUseCount = cardUseCount;
    }

    public boolean isSupportCard() {
        return isSupportCard;
    }

    public void setSupportCard(boolean supportCard) {
        isSupportCard = supportCard;
    }
}
