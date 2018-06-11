package com.travel.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.adapter.CityStoryAdapter;
import com.travel.helper.SmallCityHttpHelper;
import com.travel.layout.CustomLinearLayoutManager;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.bean.CityBean;
import com.travel.shop.bean.SmallCityBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 发现页的第一个标签---->小城页
 *
 * @author WYP
 * @created 2018/5/15
 */
@SuppressWarnings("ResourceType")
public class SmallCityFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "SmallCityFragment";
    private static final String SMALL_CITY = "小城故事";
    private static final String BIG_SMALL = "大城小事";
    private static final String COMMAND_CITY = "推荐城市";
    private View mView;
    private Context mContext;

    // 刷新控件
    private SwipeRefreshRecyclerView mSwipeRefreshRecyclerView;
    private CustomLinearLayoutManager linearLayoutManager;
    private AdapterJoiner joiner;

    // 小城故事
    private JoinableLayout smallCityEntryJoinable;
    private JoinableLayout smallCityJoinable;
    private ArrayList<CityBean> smallCityBeans;
    private CityStoryAdapter smallCityStoryAdapter;
    // 大城小事
    private JoinableLayout bigCityEntryJoinable;
    private JoinableLayout bigCityJoinable;
    private ArrayList<CityBean> bigCityBeans;
    private CityStoryAdapter bigCityStoryAdapter;
    // 推荐城市
    private JoinableLayout commandCityEntryJoinable;
    private ArrayList<CityBean> commandCityBeans;
    private CityStoryAdapter commandCityStoryAdapter;
    // 更多城市
    private JoinableLayout moreCityEntryJoinable;

    // 网络相关
    private int mPage = 1;
    private SmallCityHttpHelper.CityDataListener cityDataListener = new SmallCityHttpHelper.CityDataListener() {
        @Override
        public void onSuccess(ArrayList<SmallCityBean> datas) {
            for (int i = 0; i < datas.size(); i++) {
                ArrayList<CityBean> cityDatas = datas.get(i).getCityBeans();
                switch (datas.get(i).getId()) {
                    case "1":// 小城故事
                        if (isHide(smallCityBeans, cityDatas)) {
                            smallCityEntryJoinable.hide();
                        } else {
                            smallCityEntryJoinable.show();
                            smallCityBeans.clear();
                            smallCityBeans.addAll(cityDatas);
                            smallCityStoryAdapter.notifyDataSetChanged();
                        }
                        break;
                    case "2":// 推荐城市
                        if (isHide(commandCityBeans, cityDatas)) {
                            commandCityEntryJoinable.hide();
                            moreCityEntryJoinable.hide();
                        } else {
                            commandCityEntryJoinable.show();
                            if (mPage == 1) {
                                commandCityBeans.clear();
                            }
                            commandCityBeans.addAll(cityDatas);
                            commandCityStoryAdapter.notifyDataSetChanged();
                            if (cityDatas.size() % 10 != 0) {
                                moreCityEntryJoinable.hide();
                            } else {
                                moreCityEntryJoinable.show();
                            }
                        }
                        break;
                    default:// 大城小事
                        if (isHide(bigCityBeans, cityDatas)) {
                            bigCityEntryJoinable.hide();
                        } else {
                            bigCityEntryJoinable.show();
                            bigCityBeans.clear();
                            bigCityBeans.addAll(cityDatas);
                            bigCityStoryAdapter.notifyDataSetChanged();
                        }
                }
            }
        }

        @Override
        public void onFail() {
            smallCityEntryJoinable.hide();
            bigCityEntryJoinable.hide();
            commandCityEntryJoinable.hide();
            moreCityEntryJoinable.hide();
        }
    };

    private CityStoryAdapter.OnItemListener onItemListener = new CityStoryAdapter.OnItemListener() {
        @Override
        public void onItemClick(String storyId, int type) {
            GoodsInfoActivity.actionStart(mContext, storyId, type);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fm_out, container, false);
        init();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getData();
        }
        initListener();
        return mView;
    }

    /**
     * 控件初始化
     */
    private void init() {
        mContext = getActivity();
        smallCityBeans = new ArrayList<>();
        smallCityStoryAdapter = new CityStoryAdapter(mContext, smallCityBeans, 1);
        bigCityBeans = new ArrayList<>();
        bigCityStoryAdapter = new CityStoryAdapter(mContext, bigCityBeans, 3);
        commandCityBeans = new ArrayList<>();
        commandCityStoryAdapter = new CityStoryAdapter(mContext, commandCityBeans, 2);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshRecyclerView = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srrv_fm_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linearLayoutManager = new CustomLinearLayoutManager(getContext());
        mSwipeRefreshRecyclerView.setLayoutManager(linearLayoutManager);
        mSwipeRefreshRecyclerView.setOnRefreshListener(this);
        mSwipeRefreshRecyclerView.setEnabledLoad(false);
        initJoinableLayout();
        joiner = new AdapterJoiner();
        // 小城故事
        joiner.add(smallCityEntryJoinable);
        joiner.add(smallCityJoinable);
        // 大城小事
        joiner.add(bigCityEntryJoinable);
        joiner.add(bigCityJoinable);
        // 推荐城市
        joiner.add(commandCityEntryJoinable);
        joiner.add(new JoinableAdapter(commandCityStoryAdapter));
        // 更多城市
        joiner.add(moreCityEntryJoinable);
        smallCityEntryJoinable.hide();
        bigCityEntryJoinable.hide();
        commandCityEntryJoinable.hide();
        moreCityEntryJoinable.hide();
        mSwipeRefreshRecyclerView.setAdapter(joiner.getAdapter());
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        getData();
        mSwipeRefreshRecyclerView.setRefreshing(false);
    }

    private void initJoinableLayout() {
        smallCityEntryJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout(SMALL_CITY);
            }
        });
        smallCityJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_recyclerview, null);
                RecyclerView rv_layout = (RecyclerView) view.findViewById(R.id.rv_layout);
                LinearLayoutManager horizonManager = new LinearLayoutManager(mContext);
                horizonManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                rv_layout.setLayoutManager(horizonManager);
                rv_layout.setAdapter(smallCityStoryAdapter);
                return view;
            }
        });
        bigCityEntryJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout(BIG_SMALL);
            }
        });
        bigCityJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_recyclerview, null);
                RecyclerView rv_layout = (RecyclerView) view.findViewById(R.id.rv_layout);
                LinearLayoutManager horizonManager = new LinearLayoutManager(mContext);
                horizonManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                rv_layout.setLayoutManager(horizonManager);
                rv_layout.setAdapter(bigCityStoryAdapter);
                return view;
            }
        });
        commandCityEntryJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout(COMMAND_CITY);
            }
        });
        moreCityEntryJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(final Context context) {
                View view = View.inflate(context, R.layout.layout_more_city, null);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ++mPage;
                        LoadingDialog.getInstance(context).showProcessDialog();
                        getData();
                    }
                });
                return view;
            }
        });
    }

    private View getTitleEntryLayout(String title) {
        View view = View.inflate(mContext, R.layout.layout_small_city_title, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_small_city_title);
        textView.setText(title);
        return view;
    }

    private void getData() {
        if (mPage == 1) {
            SmallCityHttpHelper.getCityData(mContext, 1, cityDataListener);
            SmallCityHttpHelper.getCityData(mContext, 3, cityDataListener);
        }
        SmallCityHttpHelper.getCityData(mContext, 2, mPage, cityDataListener);
    }

    private void initListener() {
        smallCityStoryAdapter.setOnItemListener(onItemListener);
        bigCityStoryAdapter.setOnItemListener(onItemListener);
        commandCityStoryAdapter.setOnItemListener(onItemListener);
    }

    private boolean isHide(List list1, List list2) {
        if ((list1 == null || list1.size() < 1) && list2.size() < 1)
            return true;
        return false;
    }


    private boolean mIsFirstTime = true;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mIsFirstTime)
            return;
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsFirstTime = false;
        setUserVisibleHint(true);
    }

    public RecyclerView getRecyclerViewInstance() {
        if (mSwipeRefreshRecyclerView != null) {
            return mSwipeRefreshRecyclerView.getScrollView();
        }
        return null;
    }
}
