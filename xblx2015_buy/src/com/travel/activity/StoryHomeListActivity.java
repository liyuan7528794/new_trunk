package com.travel.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.ShopConstant;
import com.travel.adapter.DividerItemDecoration;
import com.travel.adapter.StoryHomeAdapter;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.http_helper.StoryListHttpHelper;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.activity.GoodsInfoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 已废弃
 * Created by Administrator on 2017/4/25.
 */

public class StoryHomeListActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener
       /* ,SwipeRefreshAdapterView.OnListLoadListener*/ {

    private TextView noneNotify;
    private SwipeRefreshRecyclerView swipeRefreshRecyclerView;
    private StoryHomeAdapter adapter;
    private GridLayoutManager mGridLayoutManager;
    private List<GoodsBasicInfoBean> datas;
    private int times = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_home_list);
        setTitle("小城故事");
        datas = new ArrayList<>();
        initview();
        mGridLayoutManager = new GridLayoutManager(this, 2);
        //        swipeRefreshRecyclerView.setOnListLoadListener(this);
        swipeRefreshRecyclerView.setOnRefreshListener(this);
        swipeRefreshRecyclerView.getScrollView().addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST, OSUtil.dp2px(this, 11), android.R.color.transparent));
        swipeRefreshRecyclerView.setLayoutManager(mGridLayoutManager);

        adapter = new StoryHomeAdapter(this, datas);
        swipeRefreshRecyclerView.setAdapter(adapter);

        adapter.setOnItemClick(new StoryHomeAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
//                GoodsInfoActivity.actionStart(StoryHomeListActivity.this, datas.get(position).getStoryId());
            }
        });

        getData();
    }

    private void initview() {
        noneNotify = findView(R.id.noneNotify);
        swipeRefreshRecyclerView = findView(R.id.swipeRefresh);
        swipeRefreshRecyclerView.getScrollView().setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (mIsRefreshing) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
    }
    private boolean mIsRefreshing = false;
    @Override
    public void onRefresh() {
        mIsRefreshing = true;
        times = 1;
        datas.clear();
        getData();
    }
    //    @Override
    //    public void onListLoad() {
    //        ++times;
    //        getData();
    //    }

    private void getData() {
        StoryListHttpHelper.getStoriesList(this, 1, "", 1, mOutGoodsListener);
    }

    StoryListHttpHelper.OutStoriesHttpListener mOutGoodsListener = new StoryListHttpHelper.OutStoriesHttpListener() {

        @Override
        public void getStoriesList(ArrayList<GoodsBasicInfoBean> goodsList, int flag) {
            mIsRefreshing = false;
            if (times > 1 && goodsList.size() == 0)
                showToast(R.string.no_more);
            else {
                datas.addAll(goodsList);
                adapter.notifyDataSetChanged();
            }
            if (swipeRefreshRecyclerView.isLoading())
                swipeRefreshRecyclerView.setLoading(false);
            swipeRefreshRecyclerView.setRefreshing(false);
            if (datas != null && datas.size() < 1)
                noneNotify.setVisibility(View.VISIBLE);
            else
                noneNotify.setVisibility(View.GONE);
        }

    };

    String beginTime = "";
    @Override
    protected void onStart() {
        super.onStart();
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
    }

    String endTime = "";
    @Override
    protected void onStop() {
        super.onStop();
        endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        UDPSendInfoBean bean = new UDPSendInfoBean();
        bean.getData("002", "小城故事", ShopConstant.MORE_STORY + "topStatus=1", beginTime, endTime);
        sendData(bean);
    }
}
