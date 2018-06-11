package com.travel.shop.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.shop.R;
import com.travel.shop.adapter.EvaluateAdapter;
import com.travel.shop.http.GoodsEvaluateHttp;
import com.travel.shop.widget.MyViewPager;

import java.util.ArrayList;

/**
 * 商品评价
 */
public class GoodsEvaluateFragment extends Fragment {

    private Context mContext;
    private View mView;
    private MyViewPager vp;
    private int position;
    private String goodsId;

    // 数据显示
    private LinearLayout ll_data;
    private RecyclerView mRecyclerView;
    private EvaluateAdapter mAdapter;
    private ArrayList<EvaluateInfoBean> mList;
    private TextView tv_no_evaluate;

    public static GoodsEvaluateFragment newInstance(String goodsId) {
        GoodsEvaluateFragment fragment = new GoodsEvaluateFragment();
        Bundle args = new Bundle();
        args.putString("goodsId", goodsId);
        fragment.setArguments(args);
        return fragment;
    }

    public static GoodsEvaluateFragment newInstance(String goodsId, MyViewPager vp, int position) {
        GoodsEvaluateFragment fragment = new GoodsEvaluateFragment(vp);
        Bundle args = new Bundle();
        args.putString("goodsId", goodsId);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsEvaluateFragment() {
    }

    public GoodsEvaluateFragment(MyViewPager vp) {
        this.vp = vp;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            goodsId = getArguments().getString("goodsId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_goods_evaluate, container, false);
        initView();
        initData();
        initListener();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            GoodsEvaluateHttp.evaluateData(mContext, goodsId, mListener);
        }
//        vp.setObjectForPosition(mView, position);
        return mView;
    }

    private void initView() {
        mContext = getActivity();
        ll_data = (LinearLayout) mView.findViewById(R.id.ll_data);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rv_goods_evaluate);
        tv_no_evaluate = (TextView) mView.findViewById(R.id.tv_no_evaluate);

    }

    private void initData() {
        ll_data.setVisibility(View.GONE);
        mList = new ArrayList<>();
        mAdapter = new EvaluateAdapter(mList, getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initListener() {
        mAdapter.setmOnEvaluateClickListener(new EvaluateAdapter.OnEvaluateClickListener() {
            @Override
            public void onPhotoClick(EvaluateInfoBean mEvaluateInfoBean) {
                PopWindowUtils.followPopUpWindow(getActivity(), mEvaluateInfoBean.getEvaluateUserId(),
                        mEvaluateInfoBean.getEvaluateUserName(), mEvaluateInfoBean.getEvaluateUserPhoto(), 1);
            }

            @Override
            public void onMeasureHeight() {
            }

        });
    }

    GoodsEvaluateHttp.GoodsEvaluateListener mListener = new GoodsEvaluateHttp.GoodsEvaluateListener() {
        @Override
        public void onSuccess(ArrayList<EvaluateInfoBean> list) {
            if (list.size() == 0) {
                ll_data.setVisibility(View.GONE);
                tv_no_evaluate.setVisibility(View.VISIBLE);
            } else {
                ll_data.setVisibility(View.VISIBLE);
                mList.addAll(list);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

}
