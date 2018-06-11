package com.travel.shop.bean;

import com.travel.bean.GoodsBasicInfoBean;

import java.io.Serializable;

/**
 * 订单信息的实体类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/09
 */
public class GoodsOrderBean implements Serializable {

    private static final long serialVersionUID = 7909910899536044471L;

    private OrdersBasicInfoBean mOrdersBasicInfoBean;
    private GoodsBasicInfoBean mGoodsBasicInfoBean;

    public GoodsOrderBean() {
        mOrdersBasicInfoBean = new OrdersBasicInfoBean();
        mGoodsBasicInfoBean = new GoodsBasicInfoBean();
    }

    public OrdersBasicInfoBean getmOrdersBasicInfoBean() {
        return mOrdersBasicInfoBean;
    }

    public void setmOrdersBasicInfoBean(OrdersBasicInfoBean mOrdersBasicInfoBean) {
        this.mOrdersBasicInfoBean = mOrdersBasicInfoBean;
    }

    public GoodsBasicInfoBean getmGoodsBasicInfoBean() {
        return mGoodsBasicInfoBean;
    }

    public void setmGoodsBasicInfoBean(GoodsBasicInfoBean mGoodsBasicInfoBean) {
        this.mGoodsBasicInfoBean = mGoodsBasicInfoBean;
    }
}
