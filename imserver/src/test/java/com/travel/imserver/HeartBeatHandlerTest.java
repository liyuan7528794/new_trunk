package com.travel.imserver;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;

import org.junit.Test;

/**
 * Created by ldkxingzhe on 2016/12/13.
 */
public class HeartBeatHandlerTest {

    @Test
    public void generateGson() throws Exception{
        BaseBean baseBean = new BaseBean();
        baseBean.setType(0);
        baseBean.setSendUser("9");
        baseBean.setMsgHead("heartBeat");
        Gson gson = new Gson();
        System.out.println(gson.toJson(baseBean));
    }
}