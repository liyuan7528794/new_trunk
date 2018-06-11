package com.travel.video.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.Constants;
import com.travel.VideoConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.activity.SearchActivity;
import com.travel.adapter.BannerHolderView;
import com.travel.adapter.DividerItemDecoration;
import com.travel.adapter.VideoHolderView;
import com.travel.bean.VideoInfoBean;
import com.travel.layout.DialogTemplet;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.adapter.VideoListAdapter;
import com.travel.video.adapter.VideoListWaterfallAdapter;
import com.travel.video.help.VideoIntentHelper;
import com.travel.video.widget.VideoMenu;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/13.
 */

public class VideoListWaterfallFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener , OneFragmentActivity.OneFragmentInterface{
    public final static String INTENT_NEWYEAR = "newyear";
    public final static String INTENT_MY = "my";
    public final static String INTENT_RANK = "ranking";
    public final static String INTENT_SEARCH = "search";
    private String type = INTENT_MY;
    private Context context;
    private View rootView;
    private int times = 1;
    private SwipeRefreshRecyclerView recyclerView;
    private StaggeredGridLayoutManager mGridLayoutManager;
    private List<VideoInfoBean> typeList;
    private VideoListWaterfallAdapter gridAdapter;
    private DialogTemplet deleteDialog;

    private ConvenientBanner convenientBanner;
    private List<VideoInfoBean> topList;

    private VideoMenu videoMenu;
    private RelativeLayout rl_no_collect;

    private String userId = "";
    private int activityId = -1;
    private String keyword = "";
    private String place = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey("videoType")){
            type = (String) bundle.get("videoType");
        }
        if(INTENT_SEARCH.equals(type)){
            if(bundle != null && bundle.containsKey("keyword")){
                keyword = (String) bundle.get("keyword");
            }
            if(bundle != null && bundle.containsKey("place")){
                place = (String) bundle.get("place");
            }
        }

        if(INTENT_NEWYEAR.equals(type) || INTENT_SEARCH.equals(type)){
            if(bundle != null && bundle.containsKey("activityId")){
                activityId = (int) bundle.get("activityId");
            }
        }
        context = getContext();
        rootView = inflater.inflate(R.layout.fragment_video_list_waterfall, null);
        rl_no_collect = (RelativeLayout) rootView.findViewById(R.id.rl_no_collect);
        videoMenu = (VideoMenu) rootView.findViewById(R.id.videoMenu);
        videoMenu.setActivityId(activityId);
        if(INTENT_MY.equals(type) || INTENT_SEARCH.equals(type))
            videoMenu.setVisibility(View.GONE);
        userId = UserSharedPreference.getUserId();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (SwipeRefreshRecyclerView) view.findViewById(R.id.swipeRefresh);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setOnListLoadListener(this);
        recyclerView.setOnRefreshListener(this);
//        recyclerView.getScrollView().addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST,OSUtil.dp2px(getContext(),10),android.R.color.transparent));
        recyclerView.setLayoutManager(mGridLayoutManager);
        recyclerView.getScrollView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mGridLayoutManager.invalidateSpanAssignments();
            }
        });

        if (OSUtil.isDayTheme())
            recyclerView.setLoadViewBackground(ContextCompat.getColor(context, android.R.color.white));
        else
            recyclerView.setLoadViewBackground(ContextCompat.getColor(context, R.color.black_3));
        typeList = new ArrayList<>();
        topList = new ArrayList<>();
        if(INTENT_MY.equals(type) || INTENT_RANK.equals(type))
            gridAdapter = new VideoListWaterfallAdapter(getContext(), typeList);
//        else if(INTENT_SEARCH.equals(type))
//            gridAdapter = new VideoListWaterfallAdapter(getContext(), typeList, VideoListWaterfallAdapter.VIDEO_TYPE.SEARCH);
//        else
//            gridAdapter = new VideoListWaterfallAdapter(getContext(), typeList, VideoListWaterfallAdapter.VIDEO_TYPE.OTHER);
        View view = View.inflate(context, com.travel.shop.R.layout.layout_banner, null);
        convenientBanner = (ConvenientBanner) view.findViewById(com.travel.shop.R.id.convenientBanner);
        initBanner();
        gridAdapter.setHeaderView(view);
        recyclerView.setAdapter(gridAdapter);
        if(INTENT_NEWYEAR.equals(type)){
            View headerView = View.inflate(context, R.layout.search_head_layout, null);
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    intent.putExtra("activityId", activityId);
                    startActivity(intent);
                }
            });
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, OSUtil.dp2px(getContext(), 50));
            headerView.setLayoutParams(params);
            gridAdapter.setHeaderView(headerView);
            gridAdapter.notifyDataSetChanged();
            recyclerView.getScrollView().smoothScrollBy(0, OSUtil.dp2px(getContext(), 50));
        }else{
            gridAdapter.notifyDataSetChanged();
        }
        gridAdapter.setOnItemListener(new VideoListWaterfallAdapter.OnItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(typeList.size() == 0) return;
                if(1 == typeList.get(position).getVideoStatus())
                    isLive = true;
                new VideoIntentHelper(context).intentWatchVideo(typeList.get(position), view);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if(type != INTENT_MY)
                    return;
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

            @Override
            public void updateHeaderView() {
                notifyBanner();
            }
        });
        pullDownToRefresh();
    }

    private void initBanner() {
        FrameLayout.LayoutParams p1 = new FrameLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                358 * OSUtil.getScreenWidth() / 375);
        convenientBanner.setLayoutParams(p1);
        convenientBanner.setCanLoop(true);
        convenientBanner.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                new VideoIntentHelper(context).intentWatchVideo(topList.get(position), null);
            }
        });
    }

    private boolean isLive = false;
    @Override
    public void onResume() {
        super.onResume();
//        if(isLive)
//            pullDownToRefresh();
        gridAdapter.notifyDataSetChanged();
        isLive = false;
    }

    private void pullDownToRefresh() {
        times = 1;
        typeList.clear();
        gridAdapter.clearCache();
        initData(1);
        initData(0);
    }

    private void pullUpToRefresh() {
        if (typeList != null && typeList.size() > 0 && typeList.size() % Constants.ItemNum == 0) {
            times = times + 1;
            initData(0);
        } else if (typeList != null && typeList.size() > 0 && typeList.size() % Constants.ItemNum != 0) {
            Toast.makeText(context, R.string.no_more, Toast.LENGTH_SHORT).show();
        } else if (typeList != null && typeList.size() == 0) {
            times = 1;
            initData(0);
        }
    }

    private void onRefreshComplete() {
        mIsRefreshing = false;
        if (recyclerView.isLoading())
            recyclerView.setLoading(false);
        recyclerView.setRefreshing(false);
    }

    private void initData(final int showStatus) {

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("times", times);
        if(INTENT_MY.equals(type))
            map.put("userId", userId);
        else
            map.put("statusShow", 1);

        if(activityId != -1){
            map.put("activityId", activityId);
        }
        if(!TextUtils.isEmpty(keyword)){
            map.put("keyword", keyword);
        }
        if(!TextUtils.isEmpty(place)){
            map.put("place", place);
        }
        map.put("showStatus", showStatus);
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

                        JSONArray live_list = data;// 顶置商品
                        if(showStatus == 1){
                            topList.clear();
                            for (int i = 0; i < live_list.length(); i++) {
                                System.out.println(live_list);
                                JSONObject live = live_list.getJSONObject(i);
                                VideoInfoBean bean = new VideoInfoBean().getVideoInfoBean(live);
                                topList.add(bean);
                            }
                            notifyBanner();
                            return;
                        }else {
                            for (int i = 0; i < live_list.length(); i++) {
                                System.out.println(live_list);
                                JSONObject live = live_list.getJSONObject(i);
                                VideoInfoBean bean = new VideoInfoBean().getVideoInfoBean(live);
                                typeList.add(bean);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if(times > 1){
                    if(showStatus == 1){

                        return;
                    }else {
                        Toast.makeText(context, R.string.no_more, Toast.LENGTH_SHORT).show();
                    }
                }

                onRefreshComplete();
                if(typeList.size() > 0){
                    rl_no_collect.setVisibility(View.GONE);
                }else{
                    rl_no_collect.setVisibility(View.VISIBLE);
                }
                gridAdapter.notifyDataSetChanged();
                if(INTENT_NEWYEAR.equals(type) && times == 1 && typeList.size() > 0){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getScrollView().smoothScrollBy(0, OSUtil.dp2px(getContext(), 50));
                        }
                    }, 300);
                }
                if(typeList.size() % 10 == 0 && typeList.size() != 0)
                    recyclerView.setEnabledLoad(true);
                else
                    recyclerView.setEnabledLoad(false);
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
    private VideoHolderView videoHolderView = new VideoHolderView();
    private void notifyBanner(){
        if (topList.size() <= 0) return;

        convenientBanner.setPages(new CBViewHolderCreator() {
                                @Override
                                public Object createHolder() {
                                    return videoHolderView;
                                }
                            }, topList)
                                    .setPointViewVisible(true) //设置指示器是否可见
                                    .setPageIndicator(new int[]{R.drawable.oval_f50_5, R.drawable.oval_f_5})
                                    .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                                    //                        .startTurning(2000) // 设置自动轮播时间
                                    .setManualPageable(true);
        convenientBanner.notifyDataSetChanged();
        convenientBanner.setcurrentitem(0);
        videoHolderView.UpdateUI(videoHolderView.getContext(), 0, topList.get(0));
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
                    gridAdapter.notifyDataSetChanged();
                }
            }
        }, map);
    }

    private boolean mIsRefreshing = false;
    @Override
    public void onRefresh() {
        mIsRefreshing = true;
        pullDownToRefresh();
    }

    @Override
    public void onListLoad() {
        pullUpToRefresh();
    }

    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    @Override
    public void onTouchDown() {

    }
}