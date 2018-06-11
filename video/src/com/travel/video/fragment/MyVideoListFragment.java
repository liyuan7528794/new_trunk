package com.travel.video.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.Constants;
import com.travel.bean.VideoInfoBean;
import com.travel.layout.DialogTemplet;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.adapter.VideosAdapter;
import com.travel.video.help.VideoIntentHelper;
import com.travel.VideoConstant;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/10.
 */

public class MyVideoListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {
    private Context context;
    private View rootView;
    private int times = 1;
    private String userId = "";
    private SwipeRefreshRecyclerView recyclerView;
    private List<VideoInfoBean> typeList = new ArrayList<>();
    private VideosAdapter gridAdapter;
    private DialogTemplet deleteDialog;

    private TextView none_notify;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        rootView = inflater.inflate(R.layout.swipe_refresh_recycle_layout, null);
        userId = UserSharedPreference.getUserId();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (SwipeRefreshRecyclerView) view.findViewById(R.id.swipeRefresh);
        none_notify = (TextView) view.findViewById(R.id.none_notify);
        none_notify.setText("您当前还没发布视频，快去发布吧！");
    }

    private void isShowNoneNotify() {
        if (typeList != null && typeList.size() > 0)
            none_notify.setVisibility(View.GONE);
        else
            none_notify.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        recyclerView.setOnListLoadListener(this);
        recyclerView.setOnRefreshListener(this);

        gridAdapter = new VideosAdapter(getContext(), typeList);
        recyclerView.setAdapter(gridAdapter);
        recyclerView.setEmptyText("没有数据了!");
        gridAdapter.setOnItemListener(new VideosAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                new VideoIntentHelper(context).intentWatchVideo(typeList.get(position), view);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                deleteDialog = new DialogTemplet(context, false, "是否删除该视频？", "", "否", "是");
                deleteDialog.show();

                deleteDialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
                    @Override
                    public void leftClick(View view) {

                    }
                });

                deleteDialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                    @Override
                    public void rightClick(View view) {
                        deleteVideo(position);
                    }
                });
            }
        });
        pullDownToRefresh();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        userId = UserSharedPreference.getUserId();
        if (isVisibleToUser && context != null)
            pullDownToRefresh();

    }

    private void pullDownToRefresh() {
        times = 1;
        typeList.clear();
        initData();
    }

    private void pullUpToRefresh() {
        if (typeList != null && typeList.size() > 0 && typeList.size() % Constants.ItemNum == 0) {
            times = times + 1;
            initData();
        } else if (typeList != null && typeList.size() > 0 && typeList.size() % Constants.ItemNum != 0) {
            Toast.makeText(context, R.string.no_more, Toast.LENGTH_SHORT).show();
            recyclerView.setLoading(false);
            recyclerView.setEnabledLoad(false);
        } else if (typeList != null && typeList.size() == 0) {
            times = 1;
            initData();
        }
    }

    private void onRefreshComplete() {
        if (recyclerView.isLoading())
            recyclerView.setLoading(false);
        recyclerView.setRefreshing(false);
    }

    private void initData() {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("times", times);
        map.put("userId", userId);

        NetWorkUtil.postForm(context, VideoConstant.VIDEO_LIST, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                if (data.length() > 0) {// 直播列表列表个数

                    int listSize = typeList.size() % Constants.ItemNum;
                    if (listSize > 0) {
                        for (int i = 0; i < listSize; i++) {
                            if (typeList.size() > 0)
                                typeList.remove(typeList.size() - 1);
                        }
                    }
                    try {

                        JSONArray live_list = data;// 普通商品
                        for (int i = 0; i < live_list.length(); i++) {
                            System.out.println(live_list);
                            JSONObject live = live_list.getJSONObject(i);
                            VideoInfoBean bean = new VideoInfoBean().getVideoInfoBean(live);
                            typeList.add(bean);
                        }
                        isShowNoneNotify();
                    } catch (JSONException e) {
                    }
                } else {
                    Toast.makeText(context, R.string.no_more, Toast.LENGTH_SHORT).show();
                }

                onRefreshComplete();
                gridAdapter.notifyDataSetChanged();
                recyclerView.setEnabledLoad(true);
            }

            @Override
            protected void onNetComplete() {
                onRefreshComplete();
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                onRefreshComplete();
            }
        }, map);


    }

    /**
     * 删除视频
     */
    private void deleteVideo(final int position) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", typeList.get(position).getVideoId());
        NetWorkUtil.postForm(context, VideoConstant.VIDEO_DELETE, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    typeList.remove(position);
                    isShowNoneNotify();
                    gridAdapter.notifyDataSetChanged();
                }
            }
        }, map);
    }

    @Override
    public void onRefresh() {
        pullDownToRefresh();
    }

    @Override
    public void onListLoad() {
        pullUpToRefresh();
    }
}