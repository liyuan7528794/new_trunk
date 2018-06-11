package com.travel.entity;

/**
 * Created by Administrator on 2017/1/16.
 */

public class VoteBean {
    private int id;
    private String ordersId;
    private String buyerId;
    private String sellerId;
    private double claimAmount;
    private String reason; //索赔理由
    private String createTime; //创建时间
    private int buyerPoll; //买家票数
    private int sellerPoll; //卖家票数
    private String victory; //胜利方
    private int status; //状态（0添加中 1 已发布 2进行中 3众投结束）
    private int showstatus;  //展示状态（0展示1不展示）
    private String verifier; //审核员Id
    private String checkTime; //审核时间
    private double payMoney; //支付金额
    private String snapshotId; // 商品快照id
    private int isCheck; //是否需要审核0未编辑完成数据1审核2审核完成
    private int checkStatus; //众投发起审核 （ 0 未审核 1 审核通过 2 驳回）
    private int checkPay; //支付审核0是默认值1待审核2审核完成
    private String refuseReason; //拒绝理由
    private String title; // 标题
    private String subhead; // 副标题
    private UserBean buyer;
    private UserBean seller;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrdersId() {
        return ordersId;
    }

    public void setOrdersId(String ordersId) {
        this.ordersId = ordersId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public double getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(double claimAmount) {
        this.claimAmount = claimAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getBuyerPoll() {
        return buyerPoll;
    }

    public void setBuyerPoll(int buyerPoll) {
        this.buyerPoll = buyerPoll;
    }

    public int getSellerPoll() {
        return sellerPoll;
    }

    public void setSellerPoll(int sellerPoll) {
        this.sellerPoll = sellerPoll;
    }

    public String getVictory() {
        return victory;
    }

    public void setVictory(String victory) {
        this.victory = victory;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getShowstatus() {
        return showstatus;
    }

    public void setShowstatus(int showstatus) {
        this.showstatus = showstatus;
    }

    public String getVerifier() {
        return verifier;
    }

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public double getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(double payMoney) {
        this.payMoney = payMoney;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public int getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(int isCheck) {
        this.isCheck = isCheck;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public int getCheckPay() {
        return checkPay;
    }

    public void setCheckPay(int checkPay) {
        this.checkPay = checkPay;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubhead() {
        return subhead;
    }

    public void setSubhead(String subhead) {
        this.subhead = subhead;
    }

    public UserBean getBuyer() {
        return buyer;
    }

    public void setBuyer(UserBean buyer) {
        this.buyer = buyer;
    }

    public UserBean getSeller() {
        return seller;
    }

    public void setSeller(UserBean seller) {
        this.seller = seller;
    }

    public class UserBean {
        private String id;
        private String nickName;
        private String imgUrl;
        private String place;
        private boolean followStatus;
        private boolean loginStatus;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public boolean isFollowStatus() {
            return followStatus;
        }

        public void setFollowStatus(boolean followStatus) {
            this.followStatus = followStatus;
        }

        public boolean isLoginStatus() {
            return loginStatus;
        }

        public void setLoginStatus(boolean loginStatus) {
            this.loginStatus = loginStatus;
        }
    }
}
