package com.travel.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.adapter.AdapterCityInfo;
import com.travel.shop.http.OutCityHttpHelper;
import com.travel.layout.ScrollGridLayoutManager;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.activity.CityInfoActivity;
import com.travel.shop.bean.CityBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/11.
 */

public class CityInfoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {
    public View mView;

    private SwipeRefreshRecyclerView recyclerView;
    private AdapterCityInfo adapter;
    private ArrayList<CityBean> bigCity;
    private OutCityHttpHelper cityHttpHelper;
    private int times = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_city, container, false);
        bigCity = new ArrayList<>();
        cityHttpHelper = new OutCityHttpHelper(getContext(), CityNetListener);
        cityHttpHelper.getNetCities();
        return mView;
    }


    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srrv_fm_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new ScrollGridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        recyclerView.setOnRefreshListener(this);
//        recyclerView.setOnListLoadListener(this);

        adapter = new AdapterCityInfo(getContext(), bigCity);
        View headerView = new View(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, OSUtil.dp2px(getContext(), 69));
        headerView.setLayoutParams(params);
        adapter.setHeaderView(headerView);
        adapter.setOnItemClickListener(new AdapterCityInfo.OnItemClickListener() {
            @Override
            public void onItemClick(int position, CityBean data) {
                CityInfoActivity.actionStart(getContext(), data.getCityName(), data.getId()+"");
            }
        });
        recyclerView.setAdapter(adapter);

        recyclerView.getScrollView().setOnTouchListener(
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
        bigCity.clear();
        times = 1;
        cityHttpHelper.getNetCities();
        recyclerView.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        cityHttpHelper.getNetCities();
        recyclerView.setLoading(false);
    }

    private OutCityHttpHelper.CityNetListener CityNetListener = new OutCityHttpHelper.CityNetListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void getCitys(List<CityBean> bigCities) {
            mIsRefreshing = false;
            if(bigCities != null && bigCities.size() > 0) {
                // 大城
                bigCity.clear();
                bigCity.addAll(bigCities);
            }
            adapter.notifyDataSetChanged();
        }
    };
}
