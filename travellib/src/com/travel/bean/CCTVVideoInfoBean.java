package com.travel.bean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/12/5.
 */

public class CCTVVideoInfoBean implements Serializable {

    private String id;// 视频id
    private String title;// 视频标题
    private String imgUrl;// 视频封面
    private String content;// 视频简介
    private String videoUrl;// 视频源
    private String goodsId;// 视频关联的商品Id
    private int praiseNum;// 视频点赞数

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getPraiseNum() {
        return praiseNum;
    }

    public void setPraiseNum(int praiseNum) {
        this.praiseNum = praiseNum;
    }

    public static CCTVVideoInfoBean getJson(JSONObject cctvObject) {
        CCTVVideoInfoBean mCCTVVideoInfoBean = new CCTVVideoInfoBean();
        // 视频Id
        mCCTVVideoInfoBean.setId(cctvObject.optString("id"));
        // 标题
        mCCTVVideoInfoBean.setTitle(cctvObject.optString("title"));
        // 背景图片
        mCCTVVideoInfoBean.setImgUrl(cctvObject.optString("imgUrl"));
        // 视频简介
        mCCTVVideoInfoBean.setContent(cctvObject.optString("content"));
        // 视频源
        mCCTVVideoInfoBean.setVideoUrl(cctvObject.optString("videoUrl"));
        // 视频相关商品id
        mCCTVVideoInfoBean.setGoodsId(cctvObject.optString("goodsId"));
        // 视频点赞数
        mCCTVVideoInfoBean.setPraiseNum(cctvObject.optInt("praiseNum"));
        return mCCTVVideoInfoBean;
    }
}
