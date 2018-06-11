package com.travel.shop.bean;

/**
 * 商城首页的众投实体类
 * Created by wyp on 2017/1/10.
 */

public class OutVoteBean {

    private String id;
    private String voteTitile;// 众投标题
    private String voteContent;// 众投副标题
    private String leftImg;//正方头像
    private String leftName;//正方名称
    private String rightImg;//反方头像
    private String rightName;//反方名称
    private String count;// 参与人数
    private String time;// 距离结束的时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVoteTitile() {
        return voteTitile;
    }

    public void setVoteTitile(String voteTitile) {
        this.voteTitile = voteTitile;
    }

    public String getVoteContent() {
        return voteContent;
    }

    public void setVoteContent(String voteContent) {
        this.voteContent = voteContent;
    }

    public String getLeftImg() {
        return leftImg;
    }

    public void setLeftImg(String leftImg) {
        this.leftImg = leftImg;
    }

    public String getLeftName() {
        return leftName;
    }

    public void setLeftName(String leftName) {
        this.leftName = leftName;
    }

    public String getRightImg() {
        return rightImg;
    }

    public void setRightImg(String rightImg) {
        this.rightImg = rightImg;
    }

    public String getRightName() {
        return rightName;
    }

    public void setRightName(String rightName) {
        this.rightName = rightName;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
