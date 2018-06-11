package com.travel.shop.bean;

import java.io.Serializable;

/**
 * 订单流程的实体类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/18
 */
public class OrderPathBean implements Serializable {

    private static final long seialVersionUID = 1L;

    private int statusManage;// 当前订单的订单管理状态
    private String time;// 时间
    private String statusPath;// 订单已完成的流程
    private String reason;// 理由
    private int orderStatus;// 进行中订单状态
    private int overStatus;// 已完成订单状态
    private String refundMoney;// 退款金额

    public int getStatusManage() {
        return statusManage;
    }

    public void setStatusManage(int statusManage) {
        this.statusManage = statusManage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(String statusPath) {
        this.statusPath = statusPath;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getOverStatus() {
        return overStatus;
    }

    public void setOverStatus(int overStatus) {
        this.overStatus = overStatus;
    }

    public String getRefundMoney() {
        return refundMoney;
    }

    public void setRefundMoney(String refundMoney) {
        this.refundMoney = refundMoney;
    }

}
