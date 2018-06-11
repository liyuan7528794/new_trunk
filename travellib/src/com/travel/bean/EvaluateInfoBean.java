package com.travel.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 评价信息的实体类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/09/13
 */
public class EvaluateInfoBean implements Serializable {

    private String evaluateUserId;// 评价者Id
    private String evaluateUserName;// 评价者姓名
    private String evaluateUserPhoto;// 评价者头像
    private int evaluateFlowerType;// 评价花色类型 1:好评(红花) 2:中评(黄花) 3:差评(灰花)
    private int evaluateStar;// 星评
    private ArrayList<PhotoModel> evaluatePictures;// 评价的图片
    private String evaluateContent;// 评价内容
    private String evaluateTime;// 评价时间

    // 商品评论中使用
    private String storyCommentId;// 评论Id
    private int likeCount;// 点赞数
    private boolean isLike;// 是否点赞

    public EvaluateInfoBean() {
        evaluatePictures = new ArrayList<PhotoModel>();
    }

    public String getEvaluateUserId() {
        return evaluateUserId;
    }

    public void setEvaluateUserId(String evaluateUserId) {
        this.evaluateUserId = evaluateUserId;
    }

    public String getEvaluateUserName() {
        return evaluateUserName;
    }

    public void setEvaluateUserName(String evaluateUserName) {
        this.evaluateUserName = evaluateUserName;
    }

    public String getEvaluateUserPhoto() {
        return evaluateUserPhoto;
    }

    public void setEvaluateUserPhoto(String evaluateUserPhoto) {
        this.evaluateUserPhoto = evaluateUserPhoto;
    }

    public int getEvaluateFlowerType() {
        return evaluateFlowerType;
    }

    public void setEvaluateFlowerType(int evaluateFlowerType) {
        this.evaluateFlowerType = evaluateFlowerType;
    }

    public int getEvaluateStar() {
        return evaluateStar;
    }

    public void setEvaluateStar(int evaluateStar) {
        this.evaluateStar = evaluateStar;
    }

    public ArrayList<PhotoModel> getEvaluatePictures() {
        return evaluatePictures;
    }

    public void setEvaluatePictures(ArrayList<PhotoModel> evaluatePictures) {
        this.evaluatePictures = evaluatePictures;
    }

    public String getEvaluateContent() {
        return evaluateContent;
    }

    public void setEvaluateContent(String evaluateContent) {
        this.evaluateContent = evaluateContent;
    }

    public String getEvaluateTime() {
        return evaluateTime;
    }

    public void setEvaluateTime(String evaluateTime) {
        this.evaluateTime = evaluateTime;
    }

    public String getStoryCommentId() {
        return storyCommentId;
    }

    public void setStoryCommentId(String storyCommentId) {
        this.storyCommentId = storyCommentId;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }
}
