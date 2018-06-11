package com.travel.http_helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.travel.Constants;
import com.travel.bean.ActivitysBean;
import com.travel.bean.NotifyBean;
import com.travel.layout.SlideShowView;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/23.
 */

public class SlideHelper {
    public final static int TAG_ACTIVITYS = 0;
    public final static int TAG_NOTICE = 1;
    public final static int TAG_ACTIVITY_AND_NOTICE = 2;
    private Context context;
    private SlideHelperListener slideHelperListener;

    public SlideHelper(Context context, SlideHelperListener slideHelperListener) {
        this.context = context;
        this.slideHelperListener = slideHelperListener;
    }

    public interface SlideHelperListener {
        public void onGetSlideData(List<NotifyBean> noticeList);
    }

    public void intentBySlide(NotifyBean notifyBean) {
        if ((SlideShowView.MARKE_ACTIVITYS + "").equals(notifyBean.getType())) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("activitys_bean", (ActivitysBean) notifyBean);
            Intent intent = new Intent();
            intent.setAction(Constants.ACTIVITYS_ACTION);
            intent.setType(Constants.VIDEO_TYPE);
            intent.putExtra("activitys_bean", bundle);
            context.startActivity(intent);
        } else {
            if ("-1".equals(notifyBean.getWebUrl()))
                return;
            Bundle bundle = new Bundle();
            bundle.putSerializable("notice_bean", notifyBean);
            Intent intent = new Intent();
            intent.setAction(Constants.NOTICE_ACTION);
            intent.setType(Constants.VIDEO_TYPE);
            intent.putExtra("notice_bean", bundle);
            context.startActivity(intent);
        }
    }

    /**
     * 获取轮滚图活动网络数据
     *
     * @param type 1:第一级公告
     *             2:第二级公告
     * @param tag
     */
    public void getSlideData(int type, final int tag) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", type);
        NetWorkUtil.postForm(context, Constants.TOP_ACTIVITY, new MResponseListener(context) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                List<NotifyBean> noticeList = new ArrayList<NotifyBean>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject oj = data.getJSONObject(i);
                        Map<String, String> map = new HashMap<String, String>();
                        String type = JsonUtil.getJson(oj, "type");
                        if ("1".equals(type)) {
                            noticeList.add(getAcctivityList(oj));
                        } else if ("2".equals(type) && tag != TAG_ACTIVITYS) {
                            noticeList.add(getNotifyList(oj));
                        } else if ("3".equals(type) && tag != TAG_ACTIVITYS) {

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    slideHelperListener.onGetSlideData(noticeList);
                }
            }
        }, map);
    }

    private ActivitysBean getAcctivityList(JSONObject oj) throws JSONException {
        ActivitysBean activitysBean = new ActivitysBean();
        activitysBean.setType(SlideShowView.MARKE_ACTIVITYS + "");
        JSONObject activityJson = oj.getJSONObject("activity");
        activitysBean = activitysBean.getActivityBean(activityJson);
        return activitysBean;
    }

    private NotifyBean getNotifyList(JSONObject oj) throws JSONException {
        NotifyBean notifyBean = new NotifyBean();
        notifyBean.setType(SlideShowView.MARKE_TRAILER + "");
        JSONObject noticeJson = oj.getJSONObject("notice");
        notifyBean = notifyBean.getNotifyBean(noticeJson);
        return notifyBean;
    }
}
