package com.travel.shop.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.travel.ShopConstant;
import com.travel.bean.GoodsServiceBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.shop.R;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.adapter.NewGoodsInfoAdapter;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.tools.SuperPlayer;
import com.travel.shop.tools.SuperPlayerManage;
import com.travel.shop.widget.MyViewPager;

import java.io.IOException;
import java.util.ArrayList;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 行程安排和产品特色
 */
public class GoodsInfoFragment extends Fragment  {

    private View mView;
    private LinearLayout ll_data;
    private MyViewPager vp;
    private String title;// 采集需要

    private RecyclerView rv_goods_evaluate;
    private NewGoodsInfoAdapter mGoodsInfoAdapter;
    private ArrayList<GoodsServiceBean> listData;
    private int position;// 点击的选项卡的位置
    private String content;
    private WebView wv_html;

    // 大小屏转换
    private LinearLayoutManager manager;
    private SuperPlayer playerVoice, playerVideo;
    private int full_position = -1;
    private int lastPostion = -1;

    public static GoodsInfoFragment newInstance(ArrayList<GoodsServiceBean> listData, MyViewPager vp, int position, String content, String title) {
        GoodsInfoFragment fragment = new GoodsInfoFragment(vp);
        Bundle args = new Bundle();
        args.putSerializable("info", listData);
        args.putString("content", content);
        args.putString("title", title);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    public static GoodsInfoFragment newInstance(ArrayList<GoodsServiceBean> listData, String content, String title, String priceInfo) {
        GoodsInfoFragment fragment = new GoodsInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("info", listData);
        args.putString("content", content);
        args.putString("title", title);
        args.putInt("position", 0);
        args.putString("priceInfo", priceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsInfoFragment(MyViewPager vp) {
        this.vp = vp;
    }

    public GoodsInfoFragment() {
    }

    public SuperPlayer getPlayerVoice() {
        return playerVoice;
    }

    public SuperPlayer getPlayerVideo() {
        return playerVideo;
    }

    public LinearLayoutManager getManager() {
        return manager;
    }

    public int getPosition() {
        return full_position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listData = (ArrayList<GoodsServiceBean>) getArguments().getSerializable("info");
            for (int i = 0; i < listData.size(); i++) {// 获取时间
                int type = listData.get(i).getType();
                if (type == 3) {
                    if (TextUtils.isEmpty(listData.get(i).getTime()))
                        try {
                            MediaPlayer mp = new MediaPlayer();
                            mp.setDataSource(listData.get(i).getContent());
                            mp.prepare();
                            listData.get(i).setTime(ShopTool.generateTime(mp.getDuration()));
                            mp.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    else
                        listData.get(i).setTime(ShopTool.secondToTime(listData.get(i).getTime()));
                }
            }
            position = getArguments().getInt("position");
            content = getArguments().getString("content");
            title = getArguments().getString("title");
            for (int i = listData.size() - 1; i > -1; i--) {
                if (TextUtils.equals(listData.get(i).getContent(), getArguments().getString("priceInfo"))) {
                    listData.remove(i);
                }
            }
            GoodsServiceBean goodsServiceBean = new GoodsServiceBean();
            goodsServiceBean.setContent(getArguments().getString("priceInfo"));
            goodsServiceBean.setType(1);
            listData.add(goodsServiceBean);
        }
    }

    private static final String TAG = "GoodsInfoFragment";
    /**
     * 判断是否是初始化Fragment
     */
    private boolean hasStarted = false;
    private boolean isVisibleToUser;
    private String beginTime, endTime;
    private String videoBeginTime, videoEndTime;
    private boolean isFirst = true;// 是否是首次进入

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (position != 0) {
            GoodsInfoActivity activity = (GoodsInfoActivity) getActivity();
            this.isVisibleToUser = isVisibleToUser;
            if (isVisibleToUser) {
                hasStarted = true;
                beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
            } else {
                wv_html.onPause();
                if (hasStarted) {
                    endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
                    UDPSendInfoBean bean = new UDPSendInfoBean();
                    bean.getData("003_2_" + activity.storyId, title + "_纪录片同款",
                            ShopConstant.STORY_INFO + "id=" + activity.storyId, beginTime, endTime);
                    activity.sendData(bean);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_goods_evaluate, null);
        initView();
        initData();
        initListener();
        return mView;
    }

    private void initView() {
        rv_goods_evaluate = (RecyclerView) mView.findViewById(R.id.rv_goods_evaluate);
        ll_data = (LinearLayout) mView.findViewById(R.id.ll_data);
        wv_html = (WebView) mView.findViewById(R.id.wv_html);
        if (TextUtils.isEmpty(content) || TextUtils.equals(content, "null"))
            ll_data.setVisibility(View.VISIBLE);
        else {
            wv_html.setVisibility(View.VISIBLE);
            wv_html.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        }
    }

    private void initData() {
        playerVideo = SuperPlayerManage.getSuperManage(getContext()).initializeVideoCommit();
        playerVideo.setShowTopControl(false).setSupportGesture(false);
        playerVoice = SuperPlayerManage.getSuperManage(getContext()).initializeVoiceCommit();
        mGoodsInfoAdapter = new NewGoodsInfoAdapter(getContext(), listData, "other");
        manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rv_goods_evaluate.setLayoutManager(manager);
        rv_goods_evaluate.setAdapter(mGoodsInfoAdapter);
        if (position != 0) {
            mView.findViewById(R.id.iv_mask).setVisibility(View.VISIBLE);
            vp.setObjectForPosition(mView, position);
        }
    }

    private void initListener() {
        mGoodsInfoAdapter.setPlayClick(new NewGoodsInfoAdapter.onPlayClick() {
            @Override
            public void onPlayclick(RecyclerView.ViewHolder holder, RelativeLayout image) {
                videoBeginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
                full_position = holder.getAdapterPosition();
                image.setVisibility(View.GONE);
                if (playerVoice != null) {
                    if (playerVoice.isPlaying() || playerVoice.getVideoStatus() == IjkVideoView.STATE_PAUSED)
                        doPlayer(playerVoice);
                    if (playerVoice.isPlaying()) {
                        playerVoice.stopPlayVideo();
                        playerVoice.release();
                    }
                }
                doPlayer(playerVideo);
                playerVideo.play(listData.get(full_position).getContent());
                playerVideo.setTitle(listData.get(full_position).getTitle());
                playerVideo.setFullScreenBackgroud(listData.get(full_position).getBackImage());
                lastPostion = full_position;
            }

            @Override
            public void onVoicePlayclick(RecyclerView.ViewHolder holder, RelativeLayout image) {
                full_position = holder.getAdapterPosition();
                image.setVisibility(View.GONE);
                if (playerVideo != null) {
                    if (playerVideo.isPlaying() || playerVideo.getVideoStatus() == IjkVideoView.STATE_PAUSED)
                        doPlayer(playerVideo);
                    if (playerVideo.isPlaying()) {
                        playerVideo.stopPlayVideo();
                        playerVideo.release();
                    }
                }
                doPlayer(playerVoice);
                playerVoice.play(listData.get(full_position).getContent());
                // 时长和标题
                playerVoice.setTitleAndTime(listData.get(full_position).getTitle(), listData.get(full_position).getTime());
                lastPostion = full_position;
            }
        });
    }

    private void doPlayer(SuperPlayer player) {
        if (player.isPlaying() && lastPostion == full_position) {
            return;
        }

        if (player.getVideoStatus() == IjkVideoView.STATE_PAUSED) {
            if (full_position != lastPostion) {
                player.stopPlayVideo();
                player.release();
            }
        }
        if (lastPostion != -1) {
            player.showView(R.id.adapter_player_control);
        }

        View view = rv_goods_evaluate.getChildAt(full_position);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.adapter_super_video);
        frameLayout.removeAllViews();
        player.showView(R.id.adapter_player_control);
        frameLayout.addView(player);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirst = false;
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        if (wv_html != null && !isFirst) {
            wv_html.onResume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (position != 0) {
            GoodsInfoActivity activity = (GoodsInfoActivity) getActivity();
            if (isVisibleToUser) {
                endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
                UDPSendInfoBean bean = new UDPSendInfoBean();
                bean.getData("003_2_" + activity.storyId, title + "_纪录片同款",
                        ShopConstant.STORY_INFO + "id=" + activity.storyId, beginTime, endTime);
                activity.sendData(bean);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wv_html != null) {
            wv_html.clearCache(true);
            wv_html.destroy();
        }
    }
}
