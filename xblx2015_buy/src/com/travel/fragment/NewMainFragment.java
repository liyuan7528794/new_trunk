package com.travel.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.navi.view.PoiInputResItemWidget;
import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.tencent.qcloud.suixinbo.views.LVBPlayerControler;
import com.travel.ShopConstant;
import com.travel.adapter.MainVideoAdapter;
import com.travel.bean.PersonalInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 仿抖音版首页
 * Created by wyp on 2018/5/23.
 */
public class NewMainFragment extends Fragment implements SwipeRefreshRecyclerView.OnListLoadListener, SwipeRefreshLayout.OnRefreshListener {


    private Context mContext;
    private View view;
    private SwipeRefreshRecyclerView srv_video;
    private TextView none_video;

    private MainVideoAdapter mainVideoAdapter;
    private ArrayList<VideoInfoBean> list;
    private int mPage = 1;

    private LinearLayoutManager linearLayoutManager;
    private boolean isFirst = true;// 获取到第一条能播的视频
    private int firstPosition, lastPosition;
    private boolean isLoadVideo = true;// 是否加载当前视频
    private boolean isSlide = false;// 是否可滑动

    private Handler dataHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (mPage == 1) {
                list.clear();
                srv_video.setVisibility(View.GONE);
                none_video.setVisibility(View.GONE);
            }
            list.addAll((ArrayList<VideoInfoBean>) msg.obj);
            if (((ArrayList<VideoInfoBean>) msg.obj).size() > 0) {
                srv_video.setVisibility(View.VISIBLE);
                mainVideoAdapter.notifyDataSetChanged();
                isLoadVideo = true;
                srv_video.setEnabledLoad(true);
            } else {
                if (mPage == 1) {
                    none_video.setVisibility(View.VISIBLE);
                } else {
                    isLoadVideo = false;
                    srv_video.setEnabledLoad(false);
                }
            }
        }
    };


    public NewMainFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_main, container, false);
        initData();
        initSwipRefreshView();

        getVideos();
        return view;
    }

    private void initData() {
        mContext = getActivity();
        list = new ArrayList<>();
        mainVideoAdapter = new MainVideoAdapter(mContext, list);
    }

    private void initSwipRefreshView() {
        none_video = (TextView) view.findViewById(R.id.none_video);
        srv_video = (SwipeRefreshRecyclerView) view.findViewById(R.id.srv_video);
        linearLayoutManager = new LinearLayoutManager(mContext);
        srv_video.setLayoutManager(linearLayoutManager);
        srv_video.setAdapter(mainVideoAdapter);
        srv_video.setOnRefreshListener(this);
        srv_video.setOnListLoadListener(this);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(srv_video.getScrollView());
        srv_video.getScrollView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (isLoadVideo || isSlide) {
                    isSlide = false;
                    if (newState == 0) {// 滑动后播放当前的视频
                        firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                        if (firstPosition > -1) {
                            list.get(firstPosition).setPlayStatus(0);
                            mainVideoAdapter.notifyDataSetChanged();
                        }
                    }
                    if (newState == 2) {// 拖动后让能显示在屏幕上的视频都暂停
                        firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                        lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                        if (firstPosition > -1) {
                            for (int i = firstPosition; i < lastPosition + 1; i++) {
                                list.get(i).setPlayStatus(1);
                            }
                            mainVideoAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                isSlide = true;
            }
        });

        mainVideoAdapter.setCountListener(new MainVideoAdapter.CountListener() {
            @Override
            public void onZanCountChanged(int position, int count) {
                list.get(position).setPraiseNum(count);
            }

            @Override
            public void onCommentCountChanged(int position, int count) {
                list.get(position).setCommentCount(count);
            }
        });
    }

    /**
     * 获取视频数据
     */
    private void getVideos() {
        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", mPage);
        map.put("status", 0);
        map.put("videoType", 1);
        LoadingDialog.getInstance(getContext()).showProcessDialog();
        NetWorkUtil.postForm(getContext(), ShopConstant.VIDEO_DATA, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                try {
                    ArrayList<VideoInfoBean> videoInfoBeans = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject live = data.getJSONObject(i);
                        VideoInfoBean bean = new VideoInfoBean();
                        // 视频ID
                        bean.setVideoId(live.optString("id"));
                        // 视频类型
                        bean.setVideoType(live.optInt("videoType"));
                        // 视频状态
                        bean.setVideoStatus(live.optInt("status"));
                        // 视频地址
                        bean.setUrl(live.optString("videoUrl"));
                        // 视频封面
                        bean.setVideoImg(live.optString("imgUrl"));
                        // 视频名称
                        bean.setVideoTitle(live.optString("title"));
                        // 视频上传地址
                        bean.setReleaseAddress(live.optString("location"));
                        // 视频上传时间
                        bean.setReleaseTime(live.optString("addTime"));
                        // 视频介绍
                        bean.setVideoDescription(live.optString("content"));
                        // 视频点赞数
                        bean.setPraiseNum(live.optInt("praiseNum"));
                        // 视频评论数
                        bean.setCommentCount(live.optInt("commentNum"));
                        // 视频分享数
                        bean.setShareNum(live.optInt("shareNum"));
                        // 分享的参数值
                        bean.setShare(live.optString("share"));
                        if (live.isNull("cityList")) {
                            continue;
                        }
                        JSONObject cityListObject = live.getJSONObject("cityList");
                        // 视频相关城市名
                        bean.setCityName(cityListObject.optString("cityName"));
                        // 视频相关城市图片
                        bean.setCityImg(cityListObject.optString("imgUrl3"));
                        // 视频相关商品Id
                        bean.setGoodsId(cityListObject.optString("productId"));
                        if (isFirst) {
                            isFirst = false;
                            bean.setPlayStatus(0);
                        } else
                            bean.setPlayStatus(1);

                        PersonalInfoBean personalInfoBean = new PersonalInfoBean();
                        JSONObject userObject = live.getJSONObject("user");
                        // 视频上传者的id
                        personalInfoBean.setUserId(userObject.optString("id"));
                        // 视频上传者的头像
                        personalInfoBean.setUserPhoto(userObject.optString("imgUrl"));
                        bean.setPersonalInfoBean(personalInfoBean);
                        videoInfoBeans.add(bean);
                    }
                    Message message = dataHandler.obtainMessage();
                    message.obj = videoInfoBeans;
                    dataHandler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onNetComplete() {
            }
        }, map);
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        isFirst = true;
        getVideos();
        srv_video.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        isFirst = true;
        getVideos();
        srv_video.setLoading(false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser) {
            pauseVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideo();
    }

    private void pauseVideo() {
        if (mainVideoAdapter != null) {
            if (mainVideoAdapter.getPauseIcon() != null) {
                mainVideoAdapter.getPauseIcon().setVisibility(View.VISIBLE);
            }
            if (mainVideoAdapter.getLvbPlayerControler() != null) {
                mainVideoAdapter.getLvbPlayerControler().pause();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mainVideoAdapter != null) {
            if (mainVideoAdapter.getPlaybackPlayerHandler() != null) {
                mainVideoAdapter.getPlaybackPlayerHandler().getFollowZanData();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainVideoAdapter != null) {
            if (mainVideoAdapter.getLvbPlayerControler() != null) {
                mainVideoAdapter.getLvbPlayerControler().destroy();
            }
        }
    }

}
