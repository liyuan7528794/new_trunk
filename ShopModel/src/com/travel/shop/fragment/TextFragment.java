package com.travel.shop.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.bean.GoodsOtherInfoBean;
import com.travel.shop.R;
import com.travel.shop.widget.MyViewPager;

/**
 * 费用说明和须知提示
 */
public class TextFragment extends Fragment {

    private View mView;
    private MyViewPager vp;

    private GoodsOtherInfoBean mGoodsOtherInfoBean;
    private int tab;
    private int position;// 点击的选项卡的位置

    // 内容
    private TextView tv_commitorder_info_all;

    public static TextFragment newInstance(GoodsOtherInfoBean mGoodsOtherInfoBean) {

        Bundle args = new Bundle();
        TextFragment fragment = new TextFragment();
        fragment.setArguments(args);
        args.putSerializable("info", mGoodsOtherInfoBean);
        args.putInt("tab", 2);
        return fragment;
    }

    public static TextFragment newInstance(GoodsOtherInfoBean mGoodsOtherInfoBean, int tab, MyViewPager vp, int position) {
        TextFragment fragment = new TextFragment();
        fragment.setVp(vp);
        Bundle args = new Bundle();
        args.putSerializable("info", mGoodsOtherInfoBean);
        args.putInt("tab", tab);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    private void setVp(MyViewPager vp){
        this.vp = vp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGoodsOtherInfoBean = (GoodsOtherInfoBean) getArguments().getSerializable("info");
            tab = getArguments().getInt("tab");
            position = getArguments().getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_text, null);
        initView();
        if (tab == 1) {// 费用说明
            tv_commitorder_info_all.setText(mGoodsOtherInfoBean.getCostImplications());
        } else if (tab == 2) {// 预订须知
            tv_commitorder_info_all.setText(mGoodsOtherInfoBean.getBookingNotes());
        }
//        vp.setObjectForPosition(mView, position);
        return mView;
    }

    private void initView() {
        tv_commitorder_info_all = (TextView) mView.findViewById(R.id.tv_commitorder_info_all);
    }

}
