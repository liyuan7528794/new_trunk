package com.travel.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.travel.lib.helper.VoteVideoPlayHepler;
import com.travel.localfile.dao.LocalFile;

import java.util.ArrayList;

/**
 * 滑动播放众投中的视频
 * Created by wyp on 2018/5/29.
 */
public class VoteVideoPlayFragment extends Fragment {


    private Context mContext;
    private View view;
    private RecyclerView rv_layout;

    private VoteVideoPlayAdapter voteVideoPlayAdapter;
    private ArrayList<LocalFile> list;
    private int position;// 播放第几个
    private boolean isVideoClick = true;// 是否是点击视频进入的
    private LinearLayoutManager linearLayoutManager;

    public VoteVideoPlayFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_recyclerview, container, false);
        initData();
        initView();
        return view;
    }

    private void initData() {
        mContext = getActivity();
        list = (ArrayList<LocalFile>) getArguments().getSerializable("videos");
        position = getArguments().getInt("position", 1) - 1;
    }

    private void initView() {
        rv_layout = view.findViewById(R.id.rv_layout);
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_layout.setLayoutManager(linearLayoutManager);
        voteVideoPlayAdapter = new VoteVideoPlayAdapter();
        rv_layout.setAdapter(voteVideoPlayAdapter);
        rv_layout.scrollToPosition(position);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rv_layout);
        rv_layout.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

    /**
     * 众投视频播放的适配器
     */

    class VoteVideoPlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(mContext, R.layout.controller_advertising_video, null);
            return new VoteVideoHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int pos = position;
            if (isVideoClick) {
                isVideoClick = false;
                pos = VoteVideoPlayFragment.this.position;
            }
            VoteVideoPlayHepler voteVideoPlayHepler = new VoteVideoPlayHepler(getActivity(), list.get(pos).getRemotePath(), holder.itemView);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            holder.itemView.setLayoutParams(layoutParams);
            ((VoteVideoHolder) holder).fl_player_container.addView((View) voteVideoPlayHepler.getVideoView());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VoteVideoHolder extends RecyclerView.ViewHolder {

            private RelativeLayout fl_player_container;

            public VoteVideoHolder(View itemView) {
                super(itemView);
                fl_player_container = itemView.findViewById(R.id.fl_player_container);
            }
        }

    }


}
