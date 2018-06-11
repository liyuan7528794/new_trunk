package com.travel.shop.bean;

import com.travel.bean.CCTVVideoInfoBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * cctv视频
 * Created by wyp on 2017/12/1.
 */

public class CCTVVideoBean implements Serializable {

    private String label;// cctv标签
    private int type;// cctv类型
    private ArrayList<CCTVVideoInfoBean> contents;

    public CCTVVideoBean() {
        contents = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<CCTVVideoInfoBean> getContents() {
        return contents;
    }

    public void setContents(ArrayList<CCTVVideoInfoBean> contents) {
        this.contents = contents;
    }
}
