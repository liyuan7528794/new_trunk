package com.travel.shop.bean;

/**
 * 第二级的广告栏和城市的实体类
 * Created by wyp on 2017/1/9.
 */

public class CityBean {

    private int id; // 2,
    private String imgUrl; // "http://hltext.img-cn-hangzhou.aliyuncs.com/2017/1/11/1484103244329810.jpg"
    private String cityName; // "上海",
    private String subhead; // "全国经济中心",
    private String cityDescribe; // "上海，简称“沪”或“申”，中华人民共和国直辖市，中国国家中心城市，中国的经济、金融、贸易、航运中心，首批沿海开放城市。地处长江入海口，隔东中国海... 详情>>",
    private int type; // 1,
    private String storyId; // 1501,

    private String cover; // 1501,
    private int sort; // 10,
    private int status; // 0,
    private String groupId; // "群id",
    private String groupOwner; // null,
    private String groupName; // 该城市的群聊的名称
    private String groupImageUrl; // 该城市的群聊的图标
    private String groupOwnerName; // null,

    private String temperature;//温度
    private String weather;//天气
    private String wind;//风向
    private String windLevel;// 风向等级
    private String humidity;// 湿度
    private String quality;// 空气质量

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getSubhead() {
        return subhead;
    }

    public void setSubhead(String subhead) {
        this.subhead = subhead;
    }

    public String getCityDescribe() {
        return cityDescribe;
    }

    public void setCityDescribe(String cityDescribe) {
        this.cityDescribe = cityDescribe;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    public String getGroupOwnerName() {
        return groupOwnerName;
    }

    public void setGroupOwnerName(String groupOwnerName) {
        this.groupOwnerName = groupOwnerName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWindLevel() {
        return windLevel;
    }

    public void setWindLevel(String windLevel) {
        this.windLevel = windLevel;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
