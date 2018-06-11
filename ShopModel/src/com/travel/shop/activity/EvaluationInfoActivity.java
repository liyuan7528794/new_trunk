package com.travel.shop.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.shop.R;
import com.travel.shop.adapter.EvaluateAdapter;
import com.travel.shop.http.OrderInfoHttp;

import java.util.ArrayList;

/**
 * 用户评价详情页
 *
 * @author wyp
 * @created 2017/11/20
 */
public class EvaluationInfoActivity extends TitleBarBaseActivity {

    private Context mContext;

    private RecyclerView rv_user_evaluate;
    private EvaluateAdapter mAdapter;
    private ArrayList<EvaluateInfoBean> mList;
    private long ordersId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation_info);

        init();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            OrderInfoHttp.getEvaluate(mContext, ordersId, getEvaluateListener);
        }

        mAdapter.setmOnEvaluateClickListener(new EvaluateAdapter.OnEvaluateClickListener() {
            @Override
            public void onPhotoClick(EvaluateInfoBean mEvaluateInfoBean) {
                PopWindowUtils.followPopUpWindow(EvaluationInfoActivity.this, mEvaluateInfoBean.getEvaluateUserId(),
                        mEvaluateInfoBean.getEvaluateUserName(), mEvaluateInfoBean.getEvaluateUserPhoto(), 1);
            }

            @Override
            public void onMeasureHeight() {
            }

        });
    }

    private void init() {
        mContext = this;
        rv_user_evaluate = findView(R.id.rv_user_evaluate);
        mList = new ArrayList<>();
        mAdapter = new EvaluateAdapter(mList, mContext);
        ordersId = getIntent().getLongExtra("ordersId", 0);

        setTitle("用户评价");
        rv_user_evaluate.setLayoutManager(new LinearLayoutManager(mContext));
        rv_user_evaluate.setAdapter(mAdapter);
    }

    OrderInfoHttp.GetEvaluateListener getEvaluateListener = new OrderInfoHttp.GetEvaluateListener() {
        @Override
        public void getSuccess(EvaluateInfoBean evaluateInfoBean) {
            rv_user_evaluate.setVisibility(View.VISIBLE);
            hideNoDataView();
            mList.add(evaluateInfoBean);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void getFail() {
            rv_user_evaluate.setVisibility(View.GONE);
            showNoDataView("此评价已被删除");
        }
    };
}
