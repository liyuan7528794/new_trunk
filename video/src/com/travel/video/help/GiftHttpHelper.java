package com.travel.video.help;

import android.content.Context;
import android.widget.Toast;

import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.travel.VideoConstant;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.gift.GiftBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/28.
 */

public class GiftHttpHelper {
    private String TAG = "GiftHttpHelper";
    private Context context;
    private GiftHttpListener giftHttpListener;
    public GiftHttpHelper(Context context,GiftHttpListener giftHttpListener){
        this.context = context;
        this.giftHttpListener = giftHttpListener;
    }

    public interface GiftHttpListener{
        public void onGiftBeans(List<GiftBean> giftBeans);
        public void onSendGiftSuccess(GiftBean giftBean);
    }

    public void initGiftList(){
        Map<String, Object> map = new HashMap<String, Object>();
        NetWorkUtil.postForm(context, VideoConstant.GIFT_LIST, new MResponseListener() {
            @Override
            protected void onDataFine(JSONArray data) {
                if (data.length() <= 0) {
                    return;
                }
                try {
                    List<GiftBean> giftBeans = new ArrayList<GiftBean>();
                    JSONArray live_list = data;
                    for (int i = 0; i < live_list.length(); i++) {
                        System.out.println(live_list);
                        JSONObject live = live_list.getJSONObject(i);
                        GiftBean gift = new GiftBean();
                        gift.setId(Integer.parseInt(JsonUtil.getJson(live, "giftid")));
                        gift.setName(JsonUtil.getJson(live, "giftName"));
                        gift.setPrice(Integer.parseInt(JsonUtil.getJson(live, "price")));
                        gift.marrayImage(Integer.parseInt(JsonUtil.getJson(live, "giftid")));
                        giftBeans.add(gift);
                    }
                    giftHttpListener.onGiftBeans(giftBeans);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, map);
    }

    public void sendGiftRequest(final GiftBean giftBean){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("giftid", giftBean.getId());
        map.put("giftnum", giftBean.getNum());
        map.put("giftPrice", giftBean.getPrice());
        map.put("giveUserid", CurLiveInfo.getInstance().getHostHLLXUserId());
        map.put("giftTotal", giftBean.getNum() * giftBean.getPrice());
        NetWorkUtil.postForm(context, VideoConstant.SEND_GIFT_REQUEST, new MResponseListener(context) {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if("0".equals(response.get("error")+"") && "OK".equals(response.get("msg"))){
                        MLog.d(TAG, "送礼");
                        giftBean.setUserId(UserSharedPreference.getUserId());
                        giftBean.setUserName(UserSharedPreference.getNickName());
                        giftBean.setUserImage(UserSharedPreference.getUserHeading());
                        giftHttpListener.onSendGiftSuccess(giftBean);
                    }else{
                        Toast.makeText(context, "送礼失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, map);


    }
}