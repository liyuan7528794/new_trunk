package com.travel.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.ctsmedia.hltravel.R;
import com.travel.ShopConstant;
import com.travel.adapter.CustomPagerAdapter;
import com.travel.adapter.DiscoverBannerHolderView;
import com.travel.bean.NotifyBean;
import com.travel.http_helper.SlideHelper;
import com.travel.layout.HeaderScrollHelper;
import com.travel.layout.HeaderScrollView;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.activity.ApplicationPublicVoteActivity;
import com.travel.video.widget.MediaMenu;
import com.travel.video.widget.VideoMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 仿抖音版本的“发现”页面
 */
public class DiscoverFragment extends Fragment implements View.OnClickListener, HeaderScrollHelper.ScrollableContainer {

    private static final String TAG = "DiscoverFragment";
    private Context mContext;
    private View view;

    // banner相关
    private ConvenientBanner cb_discover;
    private SlideHelper slideHelper;
    private ArrayList<NotifyBean> noticeList;
    private SlideHelper.SlideHelperListener slideNetListener = new SlideHelper.SlideHelperListener() {
        @Override
        public void onGetSlideData(List<NotifyBean> noticeList) {
            DiscoverFragment.this.noticeList = (ArrayList<NotifyBean>) noticeList;
            if (noticeList != null && noticeList.size() > 0) {
                cb_discover.setPages(new CBViewHolderCreator() {
                    @Override
                    public Object createHolder() {
                        return new DiscoverBannerHolderView();
                    }
                }, noticeList)
                        .setPointViewVisible(true) //设置指示器是否可见
                        .setPageIndicator(new int[]{R.drawable.circle7_373f47, R.drawable.circle7_da})
                        .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                        //                        .startTurning(2000) // 设置自动轮播时间
                        .setManualPageable(true);
            }
        }
    };

    // 标签相关
    private RelativeLayout rl_city, rl_talk, rl_vote;
    private TextView tv_slide_city, tv_slide_talk, tv_slide_vote;

    // 标签页内容相关
    private ViewPager vp_discover;
    private FragmentManager fragmentManager;
    private ArrayList<Fragment> fragments;

    // 解决滑动冲突相关
    private HeaderScrollView hsv_discover;
    private SmallCityFragment smallCityFragment;
    private TalkListFragment talkListFragment;
    private VoteListFragment voteListFragment;
    private int pos = 0;// 当前页面的位置

    // 悬浮按钮相关
    private VideoMenu videoMenu;
    private ImageView iv_start_vote;

    public DiscoverFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_discover, container, false);
        initView();
        initData();
        initListener();
        slideHelper.getSlideData(1, SlideHelper.TAG_ACTIVITY_AND_NOTICE);// banner数据
        return view;
    }

    private void initView() {
        cb_discover = (ConvenientBanner) view.findViewById(R.id.cb_discover);
        rl_city = (RelativeLayout) view.findViewById(R.id.rl_city);
        rl_talk = (RelativeLayout) view.findViewById(R.id.rl_talk);
        rl_vote = (RelativeLayout) view.findViewById(R.id.rl_vote);
        tv_slide_city = (TextView) view.findViewById(R.id.tv_slide_city);
        tv_slide_talk = (TextView) view.findViewById(R.id.tv_slide_talk);
        tv_slide_vote = (TextView) view.findViewById(R.id.tv_slide_vote);
        vp_discover = (ViewPager) view.findViewById(R.id.vp_discover);
        hsv_discover = (HeaderScrollView) view.findViewById(R.id.hsv_discover);
        videoMenu = (VideoMenu) view.findViewById(R.id.videoMenu);
        iv_start_vote = (ImageView) view.findViewById(R.id.iv_start_vote);
    }

    private void initData() {
        mContext = getActivity();
        // banner
        int bannerWidth = OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 30);
        int bannerHeight = bannerWidth * 9 / 16 + OSUtil.dp2px(mContext, 30);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(OSUtil.getScreenWidth(), bannerHeight);
        cb_discover.setLayoutParams(bannerParams);
        cb_discover.setCanLoop(true);
        noticeList = new ArrayList<>();
        slideHelper = new SlideHelper(mContext, slideNetListener);
        // 标签栏
        setLine(tv_slide_city, tv_slide_talk, tv_slide_vote);
        // 内容部分
        fragmentManager = getChildFragmentManager();
        fragments = new ArrayList<>();
        smallCityFragment = new SmallCityFragment();
        talkListFragment = new TalkListFragment();
        voteListFragment = new VoteListFragment();
        fragments.add(smallCityFragment);
        fragments.add(talkListFragment);
        fragments.add(voteListFragment);
        vp_discover.setAdapter(new CustomPagerAdapter(fragmentManager, fragments));
        vp_discover.setOffscreenPageLimit(2);

        hsv_discover.setCurrentScrollableContainer(this);
    }

    private void initListener() {
        cb_discover.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                int pos = position % noticeList.size();
                slideHelper.intentBySlide(noticeList.get(pos));
            }
        });

        rl_city.setOnClickListener(this);
        rl_talk.setOnClickListener(this);
        rl_vote.setOnClickListener(this);

        vp_discover.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pos = position;
                vp_discover.setCurrentItem(position);
                videoMenu.setVisibility(View.GONE);
                iv_start_vote.setVisibility(View.GONE);

                switch (position) {
                    case 0:
                        setLine(tv_slide_city, tv_slide_talk, tv_slide_vote);
                        break;
                    case 1:
                        setLine(tv_slide_talk, tv_slide_city, tv_slide_vote);
                        videoMenu.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        setLine(tv_slide_vote, tv_slide_talk, tv_slide_city);
                        iv_start_vote.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        iv_start_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserSharedPreference.isLogin()) {
                    startActivity(new Intent(ShopConstant.LOG_IN_ACTION));
                    return;
                }
                startActivity(new Intent(mContext, ApplicationPublicVoteActivity.class));
            }
        });
    }

    private void setLine(TextView... tv) {
        Drawable img = ContextCompat.getDrawable(mContext, com.travel.lib.R.drawable.find_nav_select);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        tv[0].setCompoundDrawables(null, null, null, img);
        tv[0].setTextColor(ContextCompat.getColor(mContext, com.travel.lib.R.color.black_5));
        tv[1].setCompoundDrawables(null, null, null, null);
        tv[1].setTextColor(ContextCompat.getColor(mContext, com.travel.lib.R.color.gray_A1));
        tv[2].setCompoundDrawables(null, null, null, null);
        tv[2].setTextColor(ContextCompat.getColor(mContext, com.travel.lib.R.color.gray_A1));
    }

    @Override
    public void onClick(View v) {
        videoMenu.setVisibility(View.GONE);
        iv_start_vote.setVisibility(View.GONE);
        if (v == rl_city) {// 小城
            setLine(tv_slide_city, tv_slide_talk, tv_slide_vote);
            pos = 0;
        } else if (v == rl_talk) {// 说说
            setLine(tv_slide_talk, tv_slide_city, tv_slide_vote);
            pos = 1;
            videoMenu.setVisibility(View.VISIBLE);
        } else if (v == rl_vote) {// 该不该买单
            setLine(tv_slide_vote, tv_slide_talk, tv_slide_city);
            pos = 2;
            iv_start_vote.setVisibility(View.VISIBLE);
        }
        vp_discover.setCurrentItem(pos);
    }


    @Override
    public View getScrollableView() {
        switch (pos) {
            case 0:
                return smallCityFragment.getRecyclerViewInstance();
            case 1:
                return talkListFragment.getRecyclerViewInstance();
            default:
                return voteListFragment.getRecyclerViewInstance();
        }
    }
}
