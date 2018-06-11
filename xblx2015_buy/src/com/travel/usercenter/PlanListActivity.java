package com.travel.usercenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.ctsmedia.hltravel.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.usercenter.adapter.PlanAdapter;
import com.travel.usercenter.entity.PlanEntity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import com.travel.map.RouteActivity;

/**
 * 行程安排列表页
 */

public class PlanListActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {

    private Context mContext;
    // 刷新控件
    private SwipeRefreshRecyclerView mSwipeRefreshRecyclerView;
    private AdapterJoiner joiner;

    private PlanAdapter planAdapter;
    private ArrayList<PlanEntity> plans;
    private int times = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        initView();
        initData();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection()))
            getPlanInfo();
    }

    private void initView() {
        mSwipeRefreshRecyclerView = findView(R.id.srrv_plan);
    }

    private void initData() {
        mContext = this;
        setTitle("行程安排");

        plans = new ArrayList<>();
        planAdapter = new PlanAdapter(mContext, plans);
        mSwipeRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSwipeRefreshRecyclerView.setOnListLoadListener(this);
        mSwipeRefreshRecyclerView.setOnRefreshListener(this);
        joiner = new AdapterJoiner();
        joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = new View(context);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, OSUtil.dp2px(mContext, 15));
                view.setLayoutParams(params);
                view.setBackgroundResource(android.R.color.transparent);
                return view;
            }
        }));
        joiner.add(new JoinableAdapter(planAdapter));
        planAdapter.setItemClickListener(listener);
        mSwipeRefreshRecyclerView.setAdapter(joiner.getAdapter());
    }

    @Override
    public void onRefresh() {
        plans.clear();
        times = 1;
        getPlanInfo();
        mSwipeRefreshRecyclerView.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++times;
        getPlanInfo();
        mSwipeRefreshRecyclerView.setLoading(false);
    }

    /**
     * 获取行程安排信息
     */
    public void getPlanInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", UserSharedPreference.getUserId());
        map.put("pageNo", times);
        NetWorkUtil.postForm(mContext, ShopConstant.PLANS, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                try {
                    if (times == 1) {
                        plans.clear();
                    }
                    if (data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            Gson gson = new Gson();
                            java.lang.reflect.Type type = new TypeToken<PlanEntity>() {
                            }.getType();
                            PlanEntity entity = null;
                            entity = gson.fromJson(data.getString(i), type);
                            entity.setDepart(TextUtils.isEmpty(entity.getDepart()) ? "--" :
                                    (entity.getDepart().contains("·") ?
                                            entity.getDepart().split("·")[entity.getDepart().split("·").length - 1] :
                                            entity.getDepart()));
                            entity.setDestination(TextUtils.isEmpty(entity.getDestination()) ? "--" :
                                    (entity.getDestination().contains("·") ?
                                            entity.getDestination().split("·")[entity.getDestination().split("·").length - 1] :
                                            entity.getDestination()));

                            entity.setBackground("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1506074708131&di=1deed3781eef1a0123d32bbce14defb2&imgtype=0&src=http%3A%2F%2Fpic67.nipic.com%2Ffile%2F20150514%2F21036787_181947848862_2.jpg");
                            entity.setPhoto(UserSharedPreference.getUserHeading());

                            plans.add(entity);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    mSwipeRefreshRecyclerView.setVisibility(View.VISIBLE);
                    hideNoDataView();
                    // 拉取没有更多数据了
                    if (data.length() == 0) {
                        if (times == 1) {
                            mSwipeRefreshRecyclerView.setVisibility(View.GONE);
                            showNoDataView("暂无行程安排");
                        } else {
                            --times;
                            TravelUtil.showToast(R.string.no_more, mContext);
                        }
                    }
                    planAdapter.notifyDataSetChanged();
                }

            }

            @Override
            protected void onNetComplete() {
                super.onNetComplete();
                mSwipeRefreshRecyclerView.setEnabledLoad(false);
            }
        }, map);
    }

    PlanAdapter.OnItemClickListener listener = new PlanAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position < 1)
                return;
            OSUtil.intentPlan(PlanListActivity.this, "journeyId", plans.get(position - 1).getId() + "");
        }
    };
}
