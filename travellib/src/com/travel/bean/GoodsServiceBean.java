package com.travel.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/17.
 */

public class GoodsServiceBean implements Serializable {
    private int type;// 服务类型
    private String content;// 内容
    private int width;
    private int height;
    private String title;// 标题
    private String backImage;// 背景
    private boolean isStart;// 是否开始
    private boolean isPause;// 是否暂停
    private boolean isEnd;// 是否结束

    private boolean isRelease;// 是否需要释放资源
    private boolean isPauseStart;// 是否是从暂停状态开始的

    private String time;// 时间

    private int flag = 1;// 用于纪录片页面的简介内容  1: 显示展开图标 2:显示收起图标

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackImage() {
        return backImage;
    }

    public void setBackImage(String backImage) {
        this.backImage = backImage;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public boolean isRelease() {
        return isRelease;
    }

    public void setRelease(boolean release) {
        isRelease = release;
    }

    public boolean isPauseStart() {
        return isPauseStart;
    }

    public void setPauseStart(boolean pauseStart) {
        isPauseStart = pauseStart;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
