package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;

import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.bean.CCTVVideoInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.shop.R;
import com.travel.shop.adapter.CCTVVideoContentAdapter;
import com.travel.shop.helper.CCTVVideoHttpHelper;

import java.util.ArrayList;

/**
 * 点击更多的展示页
 */
public class CCTVVideoLableActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {

    private Context mContext;
    private SwipeRefreshRecyclerView rv_cctv_video_lable;
    private ArrayList<CCTVVideoInfoBean> list;
    private CCTVVideoContentAdapter cctvVideoContentAdapter;
    private int page = 1;
    private int type;

    // 短视频数据
    private CCTVVideoHttpHelper.CCTVSmallVideoListener smallVideoListener = new CCTVVideoHttpHelper.CCTVSmallVideoListener() {
        @Override
        public void onSuccessGet(ArrayList<CCTVVideoInfoBean> videos, int position) {
            if (videos.size() == 0 && page != 1) {
                showToast("没有更多了");
                rv_cctv_video_lable.setEnabledLoad(false);
            } else {
                if (videos.size() % 10 != 0) {
                    rv_cctv_video_lable.setEnabledLoad(false);
                } else {
                    rv_cctv_video_lable.setEnabledLoad(true);
                }
                list.addAll(videos);
                cctvVideoContentAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * @param context
     * @param title   标题
     * @param type    此标签的类型
     */
    public static void actionStart(Context context, String title, int type) {
        Intent intent = new Intent(context, CCTVVideoLableActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv_video_lable);

        initView();
        initData();
        cctvVideoContentAdapter.setmOnItemClickListener(new CCTVVideoContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CCTVVideoInfoActivity.actionStart(mContext, list.get(position), 2);
            }
        });
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            CCTVVideoHttpHelper.getSmallVideoList(mContext, type, "", smallVideoListener, page);
        }
    }

    private void initView() {
        rv_cctv_video_lable = findView(R.id.rv_cctv_video_lable);
    }

    private void initData() {
        mContext = this;

        setTitle(getIntent().getStringExtra("title"));
        type = getIntent().getIntExtra("type", 1);
        list = new ArrayList<>();
        cctvVideoContentAdapter = new CCTVVideoContentAdapter(mContext, list, 2);
        rv_cctv_video_lable.setLayoutManager(new GridLayoutManager(mContext, 2));
        rv_cctv_video_lable.setAdapter(cctvVideoContentAdapter);
        rv_cctv_video_lable.setOnRefreshListener(this);
        rv_cctv_video_lable.setOnListLoadListener(this);
    }

    @Override
    public void onRefresh() {
        page = 1;
        list.clear();
        CCTVVideoHttpHelper.getSmallVideoList(mContext, type, "", smallVideoListener, page);
        rv_cctv_video_lable.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++page;
        CCTVVideoHttpHelper.getSmallVideoList(mContext, type, "", smallVideoListener, page);
        rv_cctv_video_lable.setLoading(false);
    }
}
