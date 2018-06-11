package com.travel.usercenter.entity;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wyp on 2017/9/22.
 */

public class PlanEntity implements Serializable {

    private int id ;
    private int userId;
    private String goodsId;// 商品id
    private String ordersId;// 订单id
    private String depart; // 出发地
    private String destination; //目的地
    private String departDate; // 出发时间
    private String destinationDate; // 返程时间
    private String createTime; // 创建时间
    private List<PlanLocation> locationList = new ArrayList<>(); //

    private String background;
    private String photo;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getOrdersId() {
        return ordersId;
    }

    public void setOrdersId(String ordersId) {
        this.ordersId = ordersId;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartDate() {
        return departDate;
    }

    public void setDepartDate(String departDate) {
        this.departDate = departDate;
    }

    public String getDestinationDate() {
        return destinationDate;
    }

    public void setDestinationDate(String destinationDate) {
        this.destinationDate = destinationDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<PlanLocation> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<PlanLocation> locationList) {
        this.locationList = locationList;
    }

    public static class PlanLocation{
        private int id;
        private String name;
        private String imgUrl;
        private String address;
        private String location;
        private double[] locations = {-1, -1};
        private String content;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public double[] getLocations() {
            if(!TextUtils.isEmpty(location) && location.contains(",")){
                try {
                    locations[0] = Double.parseDouble(location.split(",")[1]);
                    locations[1] = Double.parseDouble(location.split(",")[0]);
                }catch (Exception e){
                    locations[0] = -1;
                    locations[1] = -1;
                }
                setLocations(locations);
            }
            return locations;
        }

        public void setLocations(double[] locations) {
            this.locations = locations;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
