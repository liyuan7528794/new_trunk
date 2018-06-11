package com.travel.shop.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.travel.shop.R;
import com.travel.shop.adapter.AttachGoodsAdapter;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * 附加服务
 */
public class AttachGoodsFragment extends Fragment {

    private View mView;

    // 数据显示
    private RecyclerView mRecyclerView;
    private AttachGoodsAdapter mAdapter;
    private ArrayList<AttachGoodsBean> mList;
    private boolean isChoosed;

    private OnPriceListener mOnPriceListener;

    public interface OnPriceListener {
        void getPrice(String price, ArrayList<AttachGoodsBean> mList);
    }

    public static AttachGoodsFragment newInstance(ArrayList<AttachGoodsBean> goodsList) {
        AttachGoodsFragment fragment = new AttachGoodsFragment();
        Bundle args = new Bundle();
        args.putSerializable("goods_list", goodsList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnPriceListener = (OnPriceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnPriceListener");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = (ArrayList<AttachGoodsBean>) getArguments().getSerializable("goods_list");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_attach_goods, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rv_attach_goods);
        initData();
        mAdapter.setmOnEvaluateClickListener(mOnCountChangeListener);
        return mView;
    }

    private void initData() {
        mAdapter = new AttachGoodsAdapter(mList, true, getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
    }

    AttachGoodsAdapter.OnCountChangeListener mOnCountChangeListener = new AttachGoodsAdapter.OnCountChangeListener() {
        @Override
        public void onPlus(int position) {
            int count = mList.get(position).getCount();
            mList.get(position).setCount(++count);
            String totalPrice = ShopTool.getMoney(count * Float.parseFloat(mList.get(position).getPrice()) + "");
            mList.get(position).setTotalPrice(totalPrice);
            mAdapter.notifyItemChanged(position, "change");
            mOnPriceListener.getPrice(getTotalPrice(), mList);
        }

        @Override
        public void onMinus(int position) {
            int count = mList.get(position).getCount();
            if ((!TextUtils.equals(mList.get(position).getAttachId(), "1") && count > 0) ||
                    (!isChoosed && count > 0 && TextUtils.equals(mList.get(position).getAttachId(), "1"))||
                    (isChoosed && count > 1 && TextUtils.equals(mList.get(position).getAttachId(), "1"))) {
                mList.get(position).setCount(--count);
                String totalPrice = ShopTool.getMoney(count * Float.parseFloat(mList.get(position).getPrice()) + "");
                mList.get(position).setTotalPrice(totalPrice);
                if (isChoosed && count > 1 && TextUtils.equals(mList.get(position).getAttachId(), "1"))
                    mAdapter.notifyItemChanged(position, "1");
                else
                    mAdapter.notifyItemChanged(position, "change");
                mOnPriceListener.getPrice(getTotalPrice(), mList);
            }
        }
    };

    /**
     * 获取附加服务的总价
     *
     * @return
     */
    private String getTotalPrice() {
        float totalPrice = 0;
        for (AttachGoodsBean goods : mList)
            totalPrice += Float.parseFloat(TextUtils.isEmpty(goods.getTotalPrice()) ? "0" : goods.getTotalPrice());
        return totalPrice + "";
    }

    public void notifyDataSetChanged(String tag) {
        mAdapter.notifyItemChanged(0, tag);
        isChoosed = TextUtils.equals("1", tag) ? true : false;
    }

    public void setChoosed(boolean isChoosed){
        this.isChoosed = isChoosed;
    }

}
