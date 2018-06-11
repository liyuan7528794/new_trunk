package com.travel.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.RefreshScrollviewLayout;
import com.travel.ShopConstant;
import com.travel.VideoConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.PersonalInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.widget.VerticalAdapter;
import com.travel.widget.VerticalViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * 仿抖音版首页
 * Created by wyp on 2018/5/2.
 */

public class PlayOutFragment extends Fragment implements ViewPager.OnPageChangeListener, FullVideoFragment.UpdateData {
    private View mView;
    private RefreshScrollviewLayout scroll_video;
    private ViewPager vp_play_out;
    private TextView tv_none_video;

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private PlayoutAdapter playoutAdapter;
    private Handler dataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<VideoInfoBean> videos = (ArrayList<VideoInfoBean>) msg.obj;
            tv_none_video.setVisibility(View.GONE);
            vp_play_out.setVisibility(View.GONE);
            if (videos.size() > 0) {
                fragments.clear();
                fragments.add(FullVideoFragment.newInstance(videos));
                String id = videos.get(0).getPersonalInfoBean().getUserId();
                fragments.add(PersonalHomeFragment.newInstance(TravelUtil.isHomePager(id), id));
//                playoutAdapter.notifyDataSetChanged();
                playoutAdapter = new PlayoutAdapter(getChildFragmentManager(), fragments);
                vp_play_out.setAdapter(playoutAdapter);
                vp_play_out.setVisibility(View.VISIBLE);
            } else {
                tv_none_video.setVisibility(View.VISIBLE);
            }
            scroll_video.setEnabled(false);
        }
    };

    private boolean isFirst = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_play_out, container, false);
        scroll_video = (RefreshScrollviewLayout) mView.findViewById(R.id.scroll_video);
        scroll_video.setEnabled(false);
        scroll_video.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getVideos();
            }
        });
        vp_play_out = (ViewPager) mView.findViewById(R.id.vp_play_out);
        tv_none_video = (TextView) mView.findViewById(R.id.tv_none_video);
        playoutAdapter = new PlayoutAdapter(getActivity().getSupportFragmentManager(), fragments);
        vp_play_out.setAdapter(playoutAdapter);
        vp_play_out.setOffscreenPageLimit(0);
        vp_play_out.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                vp_play_out.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        getVideos();
        return mView;
    }

    /**
     * 获取视频数据
     */
    private void getVideos() {
        Map<String, Object> map = new HashMap<>();
        map.put("times", 1);
        map.put("statusShow", 1);
        map.put("showStatus", 0);
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
                LoadingDialog.getInstance(getContext()).hideProcessDialog(0);
                if (scroll_video.isRefreshing()) {
                    scroll_video.setRefreshing(false);
                }
            }
        }, map);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        vp_play_out.setCurrentItem(position);
        if (position == 1) {
            scroll_video.setEnabled(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void update(String id) {
        PersonalHomeFragment personalHomeFragment = (PersonalHomeFragment) fragments.get(1);
        personalHomeFragment.updateData(TravelUtil.isHomePager(id), id);
    }

    class PlayoutAdapter extends FragmentStatePagerAdapter {

        ArrayList<Fragment> viewList;

        public PlayoutAdapter(FragmentManager fm, ArrayList<Fragment> viewList) {
            super(fm);
            this.viewList = viewList;
        }

        @Override
        public Fragment getItem(int arg0) {
            return viewList.get(arg0);
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {
            return super.instantiateItem(arg0, arg1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirst = false;
    }

    private boolean isFirstLogin = true;
    private boolean isFirstLogout = true;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            if (!isFirst) {
                if (UserSharedPreference.isLogin() && isFirstLogin) {
                    isFirstLogin = false;
                    isFirstLogout = true;
                    if (fragments.size() > 0) {
                        FullVideoFragment fullVideoFragment = (FullVideoFragment) fragments.get(0);
                        fullVideoFragment.updateData();
                    }
                } else if (!UserSharedPreference.isLogin() && isFirstLogout) {
                    isFirstLogout = false;
                    isFirstLogin = true;
                    if (fragments.size() > 0) {
                        FullVideoFragment fullVideoFragment = (FullVideoFragment) fragments.get(0);
                        fullVideoFragment.updateData();
                    }
                }
            }
        } else {
            if (fragments.size() > 0) {
                FullVideoFragment fullVideoFragment = (FullVideoFragment) fragments.get(0);
                fullVideoFragment.onPause();
            }
        }
    }

    public void refresh() {
        scroll_video.setEnabled(true);
    }

    public void stopRefresh() {
        scroll_video.setEnabled(false);
    }
}
