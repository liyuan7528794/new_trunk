package com.travel.shop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.travel.Constants;
import com.travel.bean.NotifyBean;
import com.travel.shop.R;
import com.travel.shop.adapter.CommitOrderActivityAdapter;

import java.util.ArrayList;

/**
 * 预定页的活动和保障
 */
public class CommitOrderActivityFragment extends Fragment {

    private View mView;

    // 数据显示
    private RecyclerView mRecyclerView;
    private CommitOrderActivityAdapter mAdapter;
    private ArrayList<NotifyBean> mList;
    private String tag;

    public static CommitOrderActivityFragment newInstance(ArrayList<NotifyBean> activities, String tag) {
        CommitOrderActivityFragment fragment = new CommitOrderActivityFragment();
        Bundle args = new Bundle();
        args.putSerializable("activities", activities);
        args.putString("tag", tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = (ArrayList<NotifyBean>) getArguments().getSerializable("activities");
            tag = getArguments().getString("tag");
            if(TextUtils.equals(tag, "activity")){
                int length = mList.size() - 1;
                for(int i = length;i > -1;i--){
                    if(mList.get(i).getStatus() != 1)
                        mList.remove(i);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_commit_order_activity, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rv_activity);
        initData();
        return mView;
    }

    private void initData() {
        mAdapter = new CommitOrderActivityAdapter(mList, tag, getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setmOnActivityClickListener(new CommitOrderActivityAdapter.OnActivityClickListener() {
            @Override
            public void onClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("notice_bean", mList.get(position));
                bundle.putString("tag", tag);
                Intent intent = new Intent();
                intent.setAction(Constants.NOTICE_ACTION);
                intent.setType(Constants.VIDEO_TYPE);
                intent.putExtra("notice_bean", bundle);
                startActivity(intent);
            }
        });
    }

}
