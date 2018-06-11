package com.travel.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.ctsmedia.hltravel.R;
import com.google.gson.Gson;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.travel.AdapterJoiner.AdapterJoiners;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.activity.ProductListActivity;
import com.travel.adapter.BannerHolderView;
import com.travel.adapter.HomeAdapter;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.NotifyBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.entity.HomePageBean;
import com.travel.entity.PublicVoteEntity;
import com.travel.http_helper.SlideHelper;
import com.travel.layout.CustomLinearLayoutManager;
import com.travel.layout.HeadZoomRecyclerView;
import com.travel.layout.SlideShowView;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.localfile.pk.activity.PublicVoteActivity;
import com.travel.localfile.pk.fragment.VoteInfoFragment;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.fragment.CCTVVideoFragment;
import com.travel.video.LiveHomeListActivity;
import com.travel.video.bean.RouteBean;
import com.travel.video.help.VideoIntentHelper;
import com.travel.video.widget.MediaMenu;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 已废弃
 * Created by Administrator on 2017/11/30.
 */

public class HomeFragment extends Fragment implements SwipeRefreshAdapterView.OnListLoadListener{
    private static final String TAG = "HomeFragment";
    private View mView;
    private Context mContext;

    // 取证按钮
    private MediaMenu voteMenu;

    // 刷新控件
    private HeadZoomRecyclerView recyclerView;
    private CustomLinearLayoutManager linearLayoutManager;
    private AdapterJoiners joiner;

    // 左右滚动图片
    private FrameLayout fl_banner;
    private ImageView iv_banner;
    private JoinableLayout slideJoinableLayout;
    private ConvenientBanner convenientBanner;
    private List<NotifyBean> activityList = new ArrayList<>();
    private SlideHelper slideHelper;

    // 第二级广告栏--众投和视频的入口
    private JoinableLayout bannerJoinableLayout;

    private ArrayList<HomePageBean> beans;
    private HomeAdapter homeAdapter;

    // 网络相关
    private int mPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            LoadingDialog.getInstance(mContext).showProcessDialog();
            getData();
            getLiveBtnShowStatus();
            //            getOrdersDataNum();
            //            mHandler.postDelayed(timerRunnable, 500);
        }
        initListener();
        return mView;
    }

    /**
     * 控件初始化
     */
    private void init() {
        mContext = getActivity();
        ImageDisplayTools.initImageLoader(mContext);

        voteMenu = (MediaMenu) mView.findViewById(R.id.voteMenu);
        slideHelper = new SlideHelper(mContext, SlideNetListener);

        beans = new ArrayList<>();
        homeAdapter = new HomeAdapter(mContext, beans);
    }

    private void initBanner() {
        FrameLayout.LayoutParams p1 = new FrameLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                72 * OSUtil.getScreenWidth() / 125);
        convenientBanner.setLayoutParams(p1);
        iv_banner.setLayoutParams(p1);
        convenientBanner.setCanLoop(true);
        convenientBanner.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                int pos = position % activityList.size();
                slideHelper.intentBySlide(activityList.get(pos));
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (HeadZoomRecyclerView) mView.findViewById(R.id.srrv_fm_out);
        fl_banner = (FrameLayout) mView.findViewById(R.id.fl_banner);
        iv_banner = (ImageView) mView.findViewById(R.id.iv_banner);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linearLayoutManager = new CustomLinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setOnListLoadListener(this);
        recyclerView.setOnRefreshListener(new HeadZoomRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage = 1;
                getData();
                recyclerView.setRefreshing(false);
            }
        });
        recyclerView.getSwipeRefreshLayout().setEnabled(false);
        recyclerView.setContainLayout(fl_banner);
        initJoinableLayout();
        joiner = new AdapterJoiners();
        //轮滚
        joiner.add(slideJoinableLayout);
        // 第二级广告栏-->众投和视频的入口
        joiner.add(bannerJoinableLayout);

        joiner.add(new JoinableAdapter(homeAdapter));
        joiner.setHeadZoomRecyler(recyclerView);
        recyclerView.setAdapter(joiner.getAdapter());
        homeAdapter.notifyDataSetChanged();
    }

    private void initJoinableLayout() {
        slideJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_home_banner, null);
                convenientBanner = (ConvenientBanner) view.findViewById(R.id.convenientBanner);
                initBanner();
                return view;
            }
        });
        bannerJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(mContext, R.layout.layout_home_fragment_mark, null);
                LinearLayout ll_product = (LinearLayout) view.findViewById(R.id.ll_product);
                LinearLayout ll_cctv = (LinearLayout) view.findViewById(R.id.ll_cctv);
                LinearLayout ll_video = (LinearLayout) view.findViewById(R.id.ll_video);
                LinearLayout ll_vote = (LinearLayout) view.findViewById(R.id.ll_vote);
                ll_product.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), ProductListActivity.class));
                    }
                });
                ll_cctv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OneFragmentActivity.startNewActivity(getContext(), "CCTV播放", CCTVVideoFragment.class, null);
                    }
                });
                ll_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), LiveHomeListActivity.class));
                    }
                });
                ll_vote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OneFragmentActivity.startNewActivity(getActivity(), "", VoteListFragment.class, null);
                    }
                });
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(params);
                return view;
            }
        });
    }

    /**
     * 获取首页的数据
     */
    private void getData() {
        slideHelper.getSlideData(1, SlideHelper.TAG_ACTIVITY_AND_NOTICE);// 公告的数据
        getHomePageData(mPage);
    }

    private void initListener() {
        homeAdapter.setClickListener(new HomeAdapter.ClickListener() {
            @Override
            public void onClickStory(GoodsBasicInfoBean bean) {
//                GoodsInfoActivity.actionStart(mContext, bean.getStoryId());
            }

            @Override
            public void onClickLive(final View itemView, final VideoInfoBean bean) {
                itemView.setClickable(false);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", bean.getPersonalInfoBean().getUserId());
                NetWorkUtil.postForm(getContext(), ShopConstant.PERSONAL_INFO, new MResponseListener(getContext()) {
                    @Override
                    protected void onDataFine(JSONObject data) {
                        if(data != null){
                            PersonalInfoBean userBean = new PersonalInfoBean();
                            userBean.setUserId(JsonUtil.getJson(data, "id"));
                            userBean.setUserName(JsonUtil.getJson(data, "nickName"));
                            userBean.setUserPhoto(JsonUtil.getJson(data, "imgUrl"));
                            userBean.setUserAddress(JsonUtil.getJson(data, "place"));
                            bean.setPersonalInfoBean(userBean);
                        }
                        new VideoIntentHelper(getContext()).intentWatchVideo(bean, itemView);
                    }

                    @Override
                    protected void onNetComplete() {
                        itemView.setClickable(true);
                    }
                }, map);
            }

            @Override
            public void onClickActivitys(NotifyBean bean) {
                slideHelper.intentBySlide(bean);
            }

            @Override
            public void onClickRoute(RouteBean bean) {

            }

            @Override
            public void onClickVote(PublicVoteEntity entity) {
                Bundle bundle = new Bundle();
                int voteStatus = -1;
                if (entity.getStatus() == 0) {
                    voteStatus = VoteInfoFragment.WIN_VOTING;
                } else {
                    if (entity.getType() == 1) {
                        voteStatus = VoteInfoFragment.WIN_BUYER;
                    } else if (entity.getType() == 2) {
                        voteStatus = VoteInfoFragment.WIN_SELLER;
                    } else {
                        voteStatus = VoteInfoFragment.WIN_UNKNOWN;
                    }
                }
                bundle.putInt(VoteInfoFragment.BUNDLE_VOTE_ID, Integer.valueOf(entity.getId()));
                bundle.putInt(VoteInfoFragment.BUNDLE_VOTE_STATUS, voteStatus);
                bundle.putString(VoteInfoFragment.USER_ID_SELLER, entity.getSellerId());
                bundle.putString(VoteInfoFragment.USER_ID_BUYER, entity.getBuyerId());
                bundle.putString(VoteInfoFragment.BUNDLE_VOTE_TITLE, entity.getReason());
                Intent intent = new Intent(getActivity(), PublicVoteActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private SlideHelper.SlideHelperListener SlideNetListener = new SlideHelper.SlideHelperListener() {

        @Override
        public void onGetSlideData(List<NotifyBean> noticeList) {
            if (activityList != null && noticeList.size() > 0) {
                slideJoinableLayout.show();
                activityList.clear();
                activityList.addAll(noticeList);
                if(!TextUtils.equals(activityList.get(0).getImgUrl(), (String) iv_banner.getTag())){
                    ImageDisplayTools.displayImage(activityList.get(0).getImgUrl(), iv_banner);
                    iv_banner.setTag(activityList.get(0).getImgUrl());
                }
                convenientBanner.setPages(new CBViewHolderCreator() {
                    @Override
                    public Object createHolder() {
                        return new BannerHolderView();
                    }
                }, activityList)
//                        .setPointViewVisible(true) //设置指示器是否可见
//                        .setPageIndicator(new int[]{com.travel.shop.R.drawable.oval_f50_5, com.travel.shop.R.drawable.oval_f_5})
//                        .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                        .startTurning(5000) // 设置自动轮播时间
                        .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int position) {
                                ImageDisplayTools.displayImage(activityList.get(position).getImgUrl(), iv_banner);
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        })
                        .setManualPageable(true);

            } else if (isHide(activityList, noticeList)) {
                slideJoinableLayout.hide();
            }
        }
    };

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

    @Override
    public void onPause() {
        super.onPause();
    }

    private void getLiveBtnShowStatus() {

        Map<String, Object> map = new HashMap<>();
        NetWorkUtil.postForm(mContext, ShopConstant.IS_SHOW_LIVE_BTN, new MResponseListener() {

            @Override
            protected void onDataFine(JSONObject data) {
                if (data == null && !data.has("live_butten"))
                    return;

                try {
                    if ("true".equals(data.getString("live_butten")))
                        voteMenu.setVisibility(View.VISIBLE);
                    else if ("false".equals(data.getString("live_butten")))
                        voteMenu.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                voteMenu.setVisibility(View.GONE);
            }

            @Override
            protected void onMsgWrong(String msg) {
                super.onMsgWrong(msg);
                voteMenu.setVisibility(View.GONE);
            }
        }, map);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        getHomePageData(mPage);
        recyclerView.setLoading(false);
    }

    public void getHomePageData(final int page) {
        Map<String, Object> map = new HashMap<>();
        String url = ShopConstant.GET_HOME_PAGE_DATA;
        map.put("pageNo", page);
        NetWorkUtil.postForm(mContext, url, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if(page == 1){
                    beans.clear();
                }
                ArrayList<HomePageBean> goodsList = new ArrayList<>();
                try {
                    // 商品数据
                    for (int i = 0; i < data.length(); i++) {
                        HomePageBean bean = new HomePageBean();
                        JSONObject jsonObject = data.getJSONObject(i);
                        bean.setId(JsonUtil.getJsonInt(jsonObject, "id"));
                        bean.setTitle(JsonUtil.getJson(jsonObject, "title"));
                        bean.setType(JsonUtil.getJsonInt(jsonObject, "type"));
                        bean.setIsGroup(JsonUtil.getJsonInt(jsonObject, "isGroup"));
                        bean.setIsShowTitle(JsonUtil.getJsonInt(jsonObject, "isShowTitle"));
                        bean.setRelevanceId(JsonUtil.getJsonInt(jsonObject, "relevanceId"));
                        bean.setSort(JsonUtil.getJsonInt(jsonObject, "sort"));
                        bean.setStatus(JsonUtil.getJsonInt(jsonObject, "status"));
                        switch (bean.getType()){
                            case 1:// 故事
                                if(bean.getIsGroup() == 0){// 单个
                                    bean.setShowType(HomePageBean.TYPE_STORY);
                                    GoodsBasicInfoBean goodsBean = new GoodsBasicInfoBean();
                                    JSONObject storyJson = jsonObject.getJSONObject("story");
                                    goodsBean.setStoryId(storyJson.optString("id"));
                                    // 背景图片
                                    goodsBean.setGoodsImg(storyJson.optString("imgUrl"));
                                    // 标题
                                    goodsBean.setGoodsTitle(storyJson.optString("title"));
                                    // 副标题
                                    goodsBean.setSubhead(storyJson.optString("subhead"));
                                    // 状态
                                    goodsBean.setGoodsStatus(storyJson.optInt("status"));
                                    goodsBean.setGoodsId(storyJson.optString("goodsId"));
                                    goodsBean.setLabel(storyJson.optString("label"));
                                    bean.setObj(goodsBean);
                                }else{
                                    bean.setShowType(HomePageBean.TYPE_STORY_MORE);
                                    ArrayList<GoodsBasicInfoBean> storyMore = new ArrayList<GoodsBasicInfoBean>();
                                    JSONArray storyArray = jsonObject.getJSONArray("storyList");
                                    if(storyArray.length() > 0){
                                        for (int j = 0; j < storyArray.length(); j++) {
                                            JSONObject moreStory = storyArray.getJSONObject(j);
                                            GoodsBasicInfoBean goodsBeanMore = new GoodsBasicInfoBean();
                                            goodsBeanMore.setStoryId(moreStory.optString("id"));
                                            // 背景图片
                                            goodsBeanMore.setGoodsImg(moreStory.optString("imgUrl"));
                                            // 标题
                                            goodsBeanMore.setGoodsTitle(moreStory.optString("title"));
                                            // 副标题
                                            goodsBeanMore.setSubhead(moreStory.optString("subhead"));
                                            // 状态
                                            goodsBeanMore.setGoodsStatus(moreStory.optInt("status"));
                                            // GoodsId
                                            goodsBeanMore.setGoodsId(moreStory.optString("goodsId"));
                                            goodsBeanMore.setLabel(moreStory.optString("label"));

                                            storyMore.add(goodsBeanMore);
                                        }
                                    }
                                    bean.setObj(storyMore);
                                }
                                break;
                            case 2:// 视频
//                                if(bean.getIsGroup() == 0){// 单个显示
                                    bean.setShowType(HomePageBean.TYPE_LIVE);
                                    JSONArray videoArray = jsonObject.getJSONArray("liveVideoList");
                                    ArrayList<VideoInfoBean> videoList = new ArrayList<VideoInfoBean>();
                                    if(videoArray.length() > 0){
                                        for (int k = 0; k < videoArray.length(); k++) {
                                            JSONObject videoJson = videoArray.getJSONObject(k);
                                            VideoInfoBean videoBean = new VideoInfoBean().getVideoInfoBean(videoJson);
                                            PersonalInfoBean userBean = new PersonalInfoBean();
                                            userBean.setUserId(videoJson.optString("userId"));
                                            videoBean.setPersonalInfoBean(userBean);
                                            videoList.add(videoBean);
                                        }
                                    }
                                    bean.setObj(videoList);
//                                }else{// 多个
                                    bean.setShowType(HomePageBean.TYPE_LIVE_MORE);

//                                }
                                break;
                            case 3:// 众投
                                bean.setShowType(HomePageBean.TYPE_VOTE);
                                Gson gson = new Gson();
                                PublicVoteEntity publicVote = gson.fromJson(JsonUtil.getJson(jsonObject, "publicVote"), PublicVoteEntity.class);
                                if (publicVote.getStatus() == 3) {
                                    // 买家胜 type = 1
                                    if (jsonObject.getJSONObject("publicVote").optString("victory").equals(publicVote.getBuyerId()))
                                        publicVote.setType(1);
                                        // 卖家胜 type = 2
                                    else if (jsonObject.getJSONObject("publicVote").optString("victory").equals(publicVote.getSellerId()))
                                        publicVote.setType(2);
                                } else if (publicVote.getStatus() == 2)
                                    publicVote.setType(0);
                                bean.setObj(publicVote);
                                break;
                            case 4:// 活动
                                bean.setShowType(HomePageBean.TYPE_ACTIVITY);
                                NotifyBean notifyBean = new NotifyBean();
                                notifyBean.setType(SlideShowView.MARKE_TRAILER + "");
                                JSONObject activityJson = jsonObject.getJSONObject("notice");
                                notifyBean = notifyBean.getNotifyBean(activityJson);
                                bean.setObj(notifyBean);
                                break;
                            case 5:// 行程
                                bean.setShowType(HomePageBean.TYPE_ROUTE);
                                break;
                        }
                        goodsList.add(bean);
                    }
                    beans.addAll(goodsList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                homeAdapter.notifyDataSetChanged();

            }

        }, map);
    }
}
