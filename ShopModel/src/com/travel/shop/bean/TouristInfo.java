package com.travel.shop.bean;

import java.io.Serializable;

/**
 * 游客信息
 * Created by Administrator on 2017/11/1.
 */

public class TouristInfo implements Serializable {
    private String id;
    private String name; // 姓名
    private String IDCard; // 身份证号
    private String telephone; // 电话号码
    private String sex; // 性别

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIDCard() {
        return IDCard;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
