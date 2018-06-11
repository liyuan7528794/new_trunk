package com.travel.shop.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.adapter.VideoBannerHolderView;
import com.travel.bean.CCTVVideoInfoBean;
import com.travel.layout.CustomLinearLayoutManager;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.activity.CCTVVideoInfoActivity;
import com.travel.shop.adapter.CCTVVideoAdapter;
import com.travel.shop.bean.CCTVVideoBean;
import com.travel.shop.helper.CCTVVideoHttpHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * cctv视频
 *
 * @author wyp
 * @created 2017/12/26
 */
public class CCTVVideoFragment extends Fragment {

    private View view;

    private Context mContext;
    // 滑动控件
    private SwipeRefreshRecyclerView srrv_cctv;
    private CustomLinearLayoutManager linearLayoutManager;
    private AdapterJoiner joiner;

    // 左右滚动图片
    private JoinableLayout slideJoinableLayout;
    private ConvenientBanner convenientBanner;
    private ArrayList<CCTVVideoInfoBean> allVideos = new ArrayList<>();

    // cctv内容
    private CCTVVideoAdapter adapter;
    private ArrayList<CCTVVideoBean> list;

    // 全视频数据
    private CCTVVideoHttpHelper.CCTVAllVideoListener allVideoListener = new CCTVVideoHttpHelper.CCTVAllVideoListener() {
        @Override
        public void onSuccessGet(ArrayList<CCTVVideoInfoBean> videos) {
            if (allVideos != null && videos.size() > 0) {
                slideJoinableLayout.show();
                allVideos.clear();
                allVideos.addAll(videos);

                convenientBanner.setPages(new CBViewHolderCreator() {
                    @Override
                    public Object createHolder() {
                        return new VideoBannerHolderView();
                    }
                }, allVideos)
                        .setPointViewVisible(true) //设置指示器是否可见
                        .setPageIndicator(new int[]{R.drawable.oval_f50_4, R.drawable.oval_f_4})
                        .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                        .setManualPageable(true);

            } else if (isHide(allVideos, videos)) {
                slideJoinableLayout.hide();
            }
        }
    };
    // 视频类型数据
    private CCTVVideoHttpHelper.CCTVVideoTypeListener cctvVideoTypeListener = new CCTVVideoHttpHelper.CCTVVideoTypeListener() {
        @Override
        public void onSuccessGet(ArrayList<HashMap<String, String>> videos) {
            for (int i = 0; i < videos.size(); i++) {
                CCTVVideoBean cctvVideoBean = new CCTVVideoBean();
                cctvVideoBean.setLabel(videos.get(i).get("name"));
                int type = Integer.parseInt(videos.get(i).get("id"));
                cctvVideoBean.setType(type);
                list.add(cctvVideoBean);
                CCTVVideoHttpHelper.getSmallVideoList(mContext, type, "", cctvSmallVideoListener, i, 1);
            }
        }
    };
    // 短视频数据
    private int temp;
    private CCTVVideoHttpHelper.CCTVSmallVideoListener cctvSmallVideoListener = new CCTVVideoHttpHelper.CCTVSmallVideoListener() {
        @Override
        public void onSuccessGet(ArrayList<CCTVVideoInfoBean> videos, int position) {
            ++temp;
            list.get(position).setContents(videos);
            if (temp == list.size())
                adapter.notifyDataSetChanged();
        }
    };

    public CCTVVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_cctv_video, container, false);
        initView();
        initData();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getData();
        }
        return view;
    }

    private void initView() {
        mContext = getContext();
        srrv_cctv = (SwipeRefreshRecyclerView) view.findViewById(R.id.srrv_cctv);

        slideJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_banner, null);
                convenientBanner = (ConvenientBanner) view.findViewById(R.id.convenientBanner);
                FrameLayout.LayoutParams p1 = new FrameLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                        358 * OSUtil.getScreenWidth() / 375);
                convenientBanner.setLayoutParams(p1);
                convenientBanner.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        int pos = position % allVideos.size();
                        CCTVVideoInfoActivity.actionStart(mContext, allVideos.get(pos), 1);
                    }
                });
                return view;
            }
        });
    }

    private void initData() {
        linearLayoutManager = new CustomLinearLayoutManager(mContext);
        srrv_cctv.setLayoutManager(linearLayoutManager);
        joiner = new AdapterJoiner();
        //轮滚
        joiner.add(slideJoinableLayout);
        // cctv内容
        list = new ArrayList<>();
        adapter = new CCTVVideoAdapter(list, mContext);
        joiner.add(new JoinableAdapter(adapter));
        srrv_cctv.setAdapter(joiner.getAdapter());
        srrv_cctv.getSwipeRefreshLayout().setEnabled(false);
    }

    private void getData() {
        CCTVVideoHttpHelper.getvideoList(mContext, allVideoListener);
        CCTVVideoHttpHelper.getVideoTypeList(mContext, cctvVideoTypeListener);
    }

    private boolean isHide(List list1, List list2) {
        if ((list1 == null || list1.size() < 1) && list2.size() < 1)
            return true;
        return false;
    }

}
