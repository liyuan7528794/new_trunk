package com.travel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.VideoConstant;
import com.travel.adapter.DividerItemDecoration;
import com.travel.adapter.HomePageStoryAdapter;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.activity.GoodsInfoActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 已废弃
 * Created by Administrator on 2017/12/22.
 */

public class HomeStoryMoreAcrivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener{
    private SwipeRefreshRecyclerView recyclerView;
    private HomePageStoryAdapter adapter;
    private ArrayList<GoodsBasicInfoBean> storys;
    private int times = 1;
    private int groupId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_story_more);
        setTitle("更多");
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("groupId")){
            groupId = intent.getIntExtra("groupId", 0);
        }
        recyclerView = (SwipeRefreshRecyclerView) findViewById(R.id.swipeRefresh);
        recyclerView.setOnListLoadListener(this);
        recyclerView.setOnRefreshListener(this);
        recyclerView.getScrollView().addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST, OSUtil.dp2px(this,11),android.R.color.transparent));
        storys = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new HomePageStoryAdapter(this, storys);
        adapter.setClickListener(new HomePageStoryAdapter.ClickListener() {
            @Override
            public void onClick(GoodsBasicInfoBean bean) {
//                GoodsInfoActivity.actionStart(HomeStoryMoreAcrivity.this, bean.getStoryId());
            }
        });
        recyclerView.setAdapter(adapter);

        pullDownToRefresh();
    }

    @Override
    public void onRefresh() {
        pullDownToRefresh();
    }

    @Override
    public void onListLoad() {
        pullUpToRefresh();
    }

    private void pullDownToRefresh() {
        times = 1;
        initData(times);
    }

    private void pullUpToRefresh() {
        ++times;
        initData(times);
    }

    private void onRefreshComplete() {
        if (recyclerView.isLoading())
            recyclerView.setLoading(false);
        recyclerView.setRefreshing(false);
    }

    private void initData(final int pageNo) {
        String url = VideoConstant.HOME_STORY_LIST;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pageNo", pageNo);
        map.put("groupId", groupId);
        NetWorkUtil.postForm(this, url, new MResponseListener(this) {

            @Override
            protected void onDataFine(JSONArray data) {
                if (pageNo == 1) {
                    storys.clear();
                }
                try {
                    if(data.length() > 0){
                        for (int j = 0; j < data.length(); j++) {
                            JSONObject moreStory = data.getJSONObject(j);
                            GoodsBasicInfoBean goodsBeanMore = new GoodsBasicInfoBean();
                            goodsBeanMore.setStoryId(moreStory.optString("id"));
                            // 背景图片
                            goodsBeanMore.setGoodsImg(moreStory.optString("imgUrl"));
                            // 标题
                            goodsBeanMore.setGoodsTitle(moreStory.optString("title"));
                            // 副标题
                            goodsBeanMore.setSubhead(moreStory.optString("subhead"));
                            // 状态
                            goodsBeanMore.setGoodsStatus(moreStory.optInt("status"));
                            // GoodsId
                            goodsBeanMore.setGoodsId(moreStory.optString("goodsId"));
                            goodsBeanMore.setLabel(moreStory.optString("label"));

                            storys.add(goodsBeanMore);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    notifyRecyclerView();
                    // 拉取没有更多数据了
                    if (data.length() == 0 && pageNo != 1) {
                        --times;
                        TravelUtil.showToast(R.string.no_more, HomeStoryMoreAcrivity.this);
                    }
                }

            }

            @Override
            protected void onNetComplete() {
                onRefreshComplete();
            }
        }, map);

    }

    private void notifyRecyclerView() {
        if (storys.size() < 1) {
            recyclerView.setEnabledLoad(false);
            findViewById(R.id.tv_no_vote).setVisibility(View.VISIBLE);
        } else {
            recyclerView.setEnabledLoad(true);
            findViewById(R.id.tv_no_vote).setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

}
