package com.travel.video.help;

import android.content.Context;

import com.google.gson.Gson;
import com.travel.ShopConstant;
import com.travel.entity.PublicVoteEntity;
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
 * Created by Administrator on 2017/1/16.
 */

public class VoteHttpHelper {

    private Context context;
    private VoteNetListener voteNetListener;
    public VoteHttpHelper(Context context, VoteNetListener voteNetListener) {
        this.context = context;
        this.voteNetListener = voteNetListener;
    }

    public interface VoteNetListener{
        public void getVotes(List<PublicVoteEntity> voteList);
    }

    /**
     * 获取众投数据
     */
    public void getvoteBeanData(final int times, String type) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pageNo", times);
        String url = ShopConstant.PUBLIC_VOTE_LIST;
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                List<PublicVoteEntity> list = new ArrayList<PublicVoteEntity>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        Gson gson = new Gson();
                        PublicVoteEntity voteBean = gson.fromJson(dataObject.toString(), PublicVoteEntity.class);
                        if (voteBean.getStatus() == 3) {
                            // 买家胜 type = 1
                            if (dataObject.optString("victory").equals(voteBean.getBuyerId()))
                                voteBean.setType(1);
                                // 卖家胜 type = 2
                            else if (dataObject.optString("victory").equals(voteBean.getSellerId()))
                                voteBean.setType(2);
                        } else if (voteBean.getStatus() == 2)
                            voteBean.setType(0);
                        list.add(voteBean);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                voteNetListener.getVotes(list);
                }
            }
        }, map);

    }
}
