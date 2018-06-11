package com.travel.shop.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.layout.MyRecyclerView;
import com.travel.layout.ScrollGridLayoutManager;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.adapter.AdapterOrderSuccessCity;
import com.travel.shop.bean.CityBean;
import com.travel.shop.http.OutCityHttpHelper;
import com.travel.shop.widget.SmallStoryCardView;
import com.travel.shop.widget.SmallStoryTitleLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/2.
 */
public class OrderSuccessActivity extends TitleBarBaseActivity{
    private SwipeRefreshRecyclerView mSwipeRefreshRecyclerView;
    private AdapterJoiner joiner;
    private LinearLayoutManager manager;
    private JoinableLayout defaultJoinable;
    private JoinableLayout activeJoinable;
    private JoinableLayout cityJoinable;
    private JoinableLayout activeTitleJoinable;
    private JoinableLayout cityTitleJoinable;

    private MyRecyclerView srrv;
    private DefaultView defaultView;
    private SmallStoryTitleLayout activeTitle, cityTitle;

    private AdapterOrderSuccessCity adapter;
    private ArrayList<CityBean> bigCity;
    private OutCityHttpHelper cityHttpHelper;
    private int times = 1;
    private long ordersId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);
        setTitle("支付成功");
        ordersId = getIntent().getLongExtra("ordersId", 0);
        leftButton.setVisibility(View.GONE);
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setText("完成");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OSUtil.intentOrderInfo(OrderSuccessActivity.this, ordersId);
            }
        });

        bigCity = new ArrayList<>();
        cityHttpHelper = new OutCityHttpHelper(this, CityNetListener);

        initview();
        initData();
    }

    private void initview() {
        mSwipeRefreshRecyclerView = (SwipeRefreshRecyclerView) findViewById(R.id.srrv_city);
        initRecyclerView();
        adapter.setOnItemClickListener(new AdapterOrderSuccessCity.OnItemClickListener() {
            @Override
            public void onItemClick(int position, CityBean data) {
                CityInfoActivity.actionStart(OrderSuccessActivity.this, data.getCityName(), data.getId()+"");
            }
        });
    }

    private void initRecyclerView() {
        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSwipeRefreshRecyclerView.setLayoutManager(manager);
        mSwipeRefreshRecyclerView.setEnabled(false);
        adapter = new AdapterOrderSuccessCity(this, bigCity);
        joiner = new AdapterJoiner();
        initJoinable();
        mSwipeRefreshRecyclerView.setAdapter(joiner.getAdapter());
    }

    private void initJoinable() {
        defaultView = new DefaultView(this);
        activeTitle =  new SmallStoryTitleLayout(this);
        cityTitle =  new SmallStoryTitleLayout(this);
        activeTitle.getTitle().setText("您可以购买小城故事卡尊享1000元/城出行");
        cityTitle.getTitle().setText("您还可以去以下城市");

        defaultJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return defaultView.getView();
            }
        });
        activeJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                SmallStoryCardView cardView = new SmallStoryCardView(OrderSuccessActivity.this);
                View view = cardView.getView();
                view.setPadding(OSUtil.dp2px(context, 15), OSUtil.dp2px(context, 20), OSUtil.dp2px(context, 15), OSUtil.dp2px(context, 15));
                return view;
            }
        });
        activeTitleJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return activeTitle.getView();
            }
        });
        cityTitleJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return cityTitle.getView();
            }
        });
        cityJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                srrv = new MyRecyclerView(OrderSuccessActivity.this);
                srrv.setLayoutManager(new ScrollGridLayoutManager(OrderSuccessActivity.this, 2, GridLayoutManager.VERTICAL, false));
                srrv.setAdapter(adapter);
                return srrv;
            }
        });
        joiner.add(defaultJoinable);
        joiner.add(activeTitleJoinable);
        joiner.add(activeJoinable);
        joiner.add(cityTitleJoinable);
        joiner.add(cityJoinable);
    }


    private void initData() {
        // TODO:获取活动数据并显示
        cityHttpHelper.getNetCities();
    }

    private OutCityHttpHelper.CityNetListener CityNetListener = new OutCityHttpHelper.CityNetListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void getCitys(List<CityBean> bigCities) {
            if(bigCities != null && bigCities.size() > 0) {
                // 大城
                bigCity.clear();
                bigCity.addAll(bigCities);
            }
            adapter.notifyDataSetChanged();
        }
    };

    private class DefaultView{
        private View view;
        public DefaultView(Context context) {
            view = View.inflate(context, R.layout.layout_order_success_default, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        OSUtil.intentOrderInfo(OrderSuccessActivity.this, ordersId);
        return super.onKeyDown(keyCode, event);
    }
}
