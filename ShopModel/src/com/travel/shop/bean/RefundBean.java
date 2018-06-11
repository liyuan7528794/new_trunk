package com.travel.shop.bean;

import com.travel.bean.ReasonBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 退款信息的实体类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/22
 */
public class RefundBean implements Serializable {

    private String date;
    private String pay;
    private float refund;
    private String reason;
    private ArrayList<ReasonBean> mList;

    private int actionUser;//1买家 2卖家
    private int over;//0:双方可操作，再判断actionUser 貌似没用了
    private int agreeOrReject;// 1 同意 2 拒绝

    public RefundBean() {
        mList = new ArrayList<ReasonBean>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public float getRefund() {
        return refund;
    }

    public void setRefund(float refund) {
        this.refund = refund;
    }

    public ArrayList<ReasonBean> getmList() {
        return mList;
    }

    public void setmList(ArrayList<ReasonBean> mList) {
        this.mList = mList;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getActionUser() {
        return actionUser;
    }

    public void setActionUser(int actionUser) {
        this.actionUser = actionUser;
    }

    public int getOver() {
        return over;
    }

    public void setOver(int over) {
        this.over = over;
    }

    public int getAgreeOrReject() {
        return agreeOrReject;
    }

    public void setAgreeOrReject(int agreeOrReject) {
        this.agreeOrReject = agreeOrReject;
    }
}
