package com.travel.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.adapter.BannerHolderView;
import com.travel.adapter.BookAdapter;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.NotifyBean;
import com.travel.http_helper.SlideHelper;
import com.travel.http_helper.StoryListHttpHelper;
import com.travel.layout.CustomLinearLayoutManager;
import com.travel.layout.PageListWidget;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.adapter.OutGoodsAdapter;
import com.travel.video.LiveHomeListActivity;
import com.travel.video.widget.MediaMenu;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城的入口 ---- 已废弃
 *
 * @author WYP
 * @version 1.0
 * @created 2017/01/09
 */
@SuppressWarnings("ResourceType")
public class OutFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SwipeRefreshAdapterView.OnListLoadListener {

    private static final String TAG = "OutFragment";
    private static final String VIDEO = "小城故事  CCTV9播出版";
    private static final String STORY = "小城故事  央视同款产品";
    private View mView;
    private Context mContext;

    // 取证按钮
    private MediaMenu voteMenu;

    // 刷新控件
    private SwipeRefreshRecyclerView mSwipeRefreshRecyclerView;
    private CustomLinearLayoutManager linearLayoutManager;
    private AdapterJoiner joiner;
    // 左右滚动图片
    private JoinableLayout slideJoinableLayout;
    private ConvenientBanner convenientBanner;
    private List<NotifyBean> activityList = new ArrayList<>();
    private PageListWidget page;
    private SlideHelper slideHelper;

    // 第二级广告栏--众投和视频的入口
    private JoinableLayout bannerJoinableLayout;

    // 书里故事
    private JoinableLayout smallCityEntryJoinable;
    private JoinableLayout smallCityJoinableLayout;
    private ArrayList<GoodsBasicInfoBean> smallCity;
    private ArrayList<NotifyBean> bookImage = new ArrayList<>();
    private BookAdapter bookAdapter;

    // 推荐故事
    private JoinableLayout goodsEntryJoinable;
    private OutGoodsAdapter mOutGoodsAdapter;
    private ArrayList<GoodsBasicInfoBean> goods;
    private JoinableLayout goodsEndJoinable;
    private int count = 10;// 显示和加载的条数

    // 网络相关
    private int mPage = 1;

    private JoinableLayout emptyLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fm_out, container, false);
        init();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
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

        smallCity = new ArrayList<>();
        goods = new ArrayList<>();
        mOutGoodsAdapter = new OutGoodsAdapter(goods, mContext);
    }

    private void initBanner() {
        FrameLayout.LayoutParams p1 = new FrameLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                72 * OSUtil.getScreenWidth() / 125);
        convenientBanner.setLayoutParams(p1);
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
        mSwipeRefreshRecyclerView = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srrv_fm_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linearLayoutManager = new CustomLinearLayoutManager(getContext());
        mSwipeRefreshRecyclerView.setLayoutManager(linearLayoutManager);
        mSwipeRefreshRecyclerView.setOnRefreshListener(this);
        mSwipeRefreshRecyclerView.setOnListLoadListener(this);
        initJoinableLayout();
        joiner = new AdapterJoiner();
        //占位布局
        joiner.add(emptyLayout);
        //轮滚
        joiner.add(slideJoinableLayout);
        // 第二级广告栏-->众投和视频的入口
        joiner.add(bannerJoinableLayout);
        // 小城故事
        joiner.add(smallCityEntryJoinable);
        joiner.add(smallCityJoinableLayout);
        // 推荐商品
        joiner.add(goodsEntryJoinable);
        joiner.add(new JoinableAdapter(mOutGoodsAdapter, 2));
        joiner.add(goodsEndJoinable);
        mSwipeRefreshRecyclerView.setAdapter(joiner.getAdapter());
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        activityList.clear();
        bookImage.clear();
        goods.clear();
        getData();
        StoryListHttpHelper.getStoriesList(mContext, 1, "", 1, cityNetListener);// 书里的数据
        mSwipeRefreshRecyclerView.setRefreshing(false);
    }

    private void initJoinableLayout() {
        emptyLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = new View(context);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, OSUtil.dp2px(context, 69));
                view.setLayoutParams(params);
                return view;
            }
        });
        slideJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_banner, null);
                convenientBanner = (ConvenientBanner) view.findViewById(R.id.convenientBanner);
                initBanner();
                return view;
            }
        });
        bannerJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(mContext, R.layout.layout_vote_video, null);
                ImageView iv_ovte = (ImageView) view.findViewById(R.id.iv_vote);
                ImageView iv_video = (ImageView) view.findViewById(R.id.iv_video);
                TravelUtil.setLLParamsWidthPart(iv_ovte, 2, 0, 188, 115);
                TravelUtil.setLLParamsWidthPart(iv_video, 2, 0, 188, 115);
                iv_ovte.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OneFragmentActivity.startNewActivity(getActivity(), "", VoteListFragment.class, null);
                    }
                });
                iv_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), LiveHomeListActivity.class));
                    }
                });
                return view;
            }
        });
        smallCityEntryJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout(VIDEO);
            }
        });
        smallCityJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(mContext, R.layout.layout_out_small_image, null);
                page = (PageListWidget) view.findViewById(R.id.page);
                StoryListHttpHelper.getStoriesList(mContext, 1, "", 1, cityNetListener);// 书里的数据
                page.setOutScroll(new PageListWidget.OutScroll() {
                    @Override
                    public void isScrolled(boolean isScrolled) {
                        if (isScrolled) {// 外边能滑动
                            linearLayoutManager.setScrollEnabled(true);
                        } else
                            linearLayoutManager.setScrollEnabled(false);
                    }
                });
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(params);
                return view;
            }
        });
        goodsEntryJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout(STORY);
            }
        });
        goodsEndJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout("");
            }
        });

    }

    private View getTitleEntryLayout(String title) {
        View view = View.inflate(mContext, R.layout.layout_out_titles, null);
        TextView cTitle = (TextView) view.findViewById(R.id.tv_c_title);
        ImageView more = (ImageView) view.findViewById(R.id.iv_more);
        ImageView iv_book = (ImageView) view.findViewById(R.id.iv_book);
        ImageView iv_product = (ImageView) view.findViewById(R.id.iv_product);
        cTitle.setText(title);
        if (TextUtils.equals(title, VIDEO)) {
            more.setVisibility(View.VISIBLE);
            iv_book.setVisibility(View.VISIBLE);
        } else {
            more.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                view.findViewById(R.id.ll_module_title).setVisibility(View.GONE);
                view.findViewById(R.id.v_module_line).setVisibility(View.GONE);
            } else
                iv_product.setVisibility(View.VISIBLE);
        }
        return view;
    }

    /**
     * 获取首页的数据
     */
    private void getData() {
        slideHelper.getSlideData(1, SlideHelper.TAG_ACTIVITY_AND_NOTICE);// 公告的数据
        //        OutGoodsHttpHelper.getGoodsList(mContext, mPage, mOutGoodsListener); // 推荐商品的数据
        StoryListHttpHelper.getStoriesList(mContext, 2, "", mPage, cityNetListener);// 推荐故事的数据
    }

    private void initListener() {
        // 推荐商品
        mOutGoodsAdapter.setmOnItemClickListener(new OutGoodsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder) {
                int position = joiner.getAdapterPositionByViewHolder(holder);
//                GoodsInfoActivity.actionStart(mContext, goods.get(position).getStoryId());
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


                convenientBanner.setPages(new CBViewHolderCreator() {
                    @Override
                    public Object createHolder() {
                        return new BannerHolderView();
                    }
                }, activityList)
                        .setPointViewVisible(true) //设置指示器是否可见
                        .setPageIndicator(new int[]{R.drawable.oval_f50_10, R.drawable.oval_f_10})
                        .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                        //                        .startTurning(2000) // 设置自动轮播时间
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

    private StoryListHttpHelper.OutStoriesHttpListener cityNetListener = new StoryListHttpHelper.OutStoriesHttpListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void getStoriesList(ArrayList<GoodsBasicInfoBean> storyList, int flag) {
            if (flag == 1) {
                if (smallCity != null && storyList.size() > 0) {
                    smallCityJoinableLayout.show();
                    smallCityEntryJoinable.show();
                    smallCity.clear();
                    smallCity.addAll(storyList);
                    for (int i = 0; i < storyList.size(); i++) {
                        NotifyBean nb = new NotifyBean();
                        nb.setImgUrl(smallCity.get(i).getTopImage());
                        nb.setShareUrl(smallCity.get(i).getGoodsImg());
                        nb.setId(smallCity.get(i).getStoryId());
                        bookImage.add(nb);
                    }
                    bookAdapter = new BookAdapter(mContext, R.layout.book_item, bookImage);
                    page.setAdapter(bookAdapter);
                    page.setList(bookImage);
                } else if (isHide(smallCity, storyList)) {
                    smallCityJoinableLayout.hide();
                    smallCityEntryJoinable.hide();
                }
            } else if (flag == 2) {
                goods.addAll(storyList);
                mOutGoodsAdapter.notifyDataSetChanged();
                if (storyList.size() % 10 != 0 || storyList.size() == 0)
                    mSwipeRefreshRecyclerView.setEnabledLoad(false);
                else
                    mSwipeRefreshRecyclerView.setEnabledLoad(true);
            }
        }
    };
    //
    //    OutGoodsHttpHelper.OutGoodsHttpListener mOutGoodsListener = new OutGoodsHttpHelper.OutGoodsHttpListener() {
    //        @Override
    //        public void getGoodsList(ArrayList<GoodsBasicInfoBean> goodsList) {
    //            goods.addAll(goodsList);
    //            mOutGoodsAdapter.notifyDataSetChanged();
    //        }
    //
    //        @Override
    //        public void onError() {
    //            if (mPage > 1)
    //                --mPage;
    //        }
    //    };

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
        }, map);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        StoryListHttpHelper.getStoriesList(mContext, 2, "", mPage, cityNetListener);// 推荐故事的数据
        mSwipeRefreshRecyclerView.setLoading(false);
    }
}
