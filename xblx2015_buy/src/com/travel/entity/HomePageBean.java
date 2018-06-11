package com.travel.entity;

/**
 * Created by Administrator on 2017/12/4.
 */

public class HomePageBean {
    public final static int TYPE_STORY = 0;
    public final static int TYPE_STORY_MORE = 1;
    public final static int TYPE_LIVE = 2;
    public final static int TYPE_LIVE_MORE = 3;
    public final static int TYPE_VOTE = 4;
    public final static int TYPE_ACTIVITY = 5;
    public final static int TYPE_ROUTE = 6;

    private int id;
    private int showType; // 显示类型，对应上面的集中状态
    private int type; // 1故事2视频3众投4活动5行程
    private int isGroup; // 0一对一， 1一对多
    private String title; // 标题
    private int isShowTitle; // 0显示标题，1不显示
    private long relevanceId; // 一对一时使用的参数
    private int sort; // 排序
    private int status; // 0显示， 1不显示
    private Object obj; // 具体数据

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIsShowTitle() {
        return isShowTitle;
    }

    public void setIsShowTitle(int isShowTitle) {
        this.isShowTitle = isShowTitle;
    }

    public long getRelevanceId() {
        return relevanceId;
    }

    public void setRelevanceId(long relevanceId) {
        this.relevanceId = relevanceId;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
