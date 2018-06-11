package com.travel.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.adapter.DividerItemDecoration;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.layout.ScrollGridLayoutManager;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.adapter.PersonalHomeStoryAdapter;
import com.travel.shop.bean.CityBean;
import com.travel.shop.http.CityInfoHttp;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/23.
 */
public class MyBoxRoomActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {
    private SwipeRefreshRecyclerView recyclerView;
    private TextView rl_no_collect;
    private PersonalHomeStoryAdapter adapter;
    private ArrayList<GoodsBasicInfoBean> goods = new ArrayList<>();
    private int times = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_refresh_recycle_layout);
        setTitle("我的收藏");
        initLayout();
        refresh();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initLayout() {
        rl_no_collect = (TextView) findViewById(R.id.none_notify);
        recyclerView = (SwipeRefreshRecyclerView) findViewById(R.id.swipeRefresh);
        recyclerView.setPadding(OSUtil.dp2px(this, 15), 0, OSUtil.dp2px(this, 15), 0);
        recyclerView.setLayoutManager(new ScrollGridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));
        recyclerView.getScrollView().addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.BOTH_SET, OSUtil.dp2px(this, 15), ContextCompat.getColor(this, android.R.color.transparent)));
        recyclerView.setOnListLoadListener(this);
        recyclerView.setOnRefreshListener(this);
        adapter = new PersonalHomeStoryAdapter(goods, this);
        recyclerView.setAdapter(adapter);
        adapter.setmOnItemClickListener(new PersonalHomeStoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                GoodsInfoActivity.actionStart(MyBoxRoomActivity.this, goods.get(position).getStoryId(), 1);
            }
        });
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onListLoad() {
        load();
    }

    public void refresh() {
        times = 1;
        goods.clear();
        outData();
    }

    public void load() {
        ++times;
        outData();
    }

    private void outData() {
        CityInfoHttp.getBoxRommList(this, UserSharedPreference.getUserId(), times, mCityInfoListener);
    }

    CityInfoHttp.CityInfoListener mCityInfoListener = new CityInfoHttp.CityInfoListener() {
        @Override
        public void getCityInfo(CityBean mCityBean) {

        }

        @Override
        public void getCityWeatherInfo(CityBean mCityBean) {

        }

        @Override
        public void getCityStoryList(ArrayList<GoodsBasicInfoBean> mList) {
            if (mList.size() == 0 && times == 1) {
                rl_no_collect.setVisibility(View.VISIBLE);
            }
            if (mList.size() == 0 && times != 1) {
                --times;
                showToast(getResources().getString(com.travel.shop.R.string.no_more));
                recyclerView.setEnabledLoad(false);
            } else {
                recyclerView.setEnabledLoad(true);
                goods.addAll(mList);
                adapter.notifyDataSetChanged();
            }
            stopRefresh();
        }
    };

    private void stopRefresh() {
        recyclerView.setRefreshing(false);
        recyclerView.setLoading(false);
    }
}
