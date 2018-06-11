package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.shop.R;
import com.travel.shop.bean.CouponInfoBean;

import java.util.ArrayList;

/**
 * 商品详情中领取优惠券的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/11/07
 */
public class GetCouponAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<CouponInfoBean> listData;
    private CouponInfoBean coupon;
    private OnCouponClickListener mOnCouponClickListener;

    public GetCouponAdapter(ArrayList<CouponInfoBean> listData, Context context) {
        this.mContext = context;
        this.listData = listData;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_get_coupon_item, null);
        final Holder holder = new Holder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCouponClickListener != null)
                    mOnCouponClickListener.onClick(holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Holder) {
            coupon = listData.get(position);
            // 商品名称
            if (coupon.isGet()) {
                ((Holder) holder).tv_coupon.setText(coupon.getCouponName() + "您已领取，单城产品购买可使用。");
            } else {
                ((Holder) holder).tv_coupon.setText(coupon.getCouponName() + "您还未领取，快去领取使用吧！");
            }
        }

    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public TextView tv_coupon;

        public Holder(View itemView) {
            super(itemView);
            tv_coupon = (TextView) itemView.findViewById(R.id.tv_coupon);
        }
    }

    public interface OnCouponClickListener {
        void onClick(int position);
    }

    public void setOnCouponClickListener(OnCouponClickListener mOnCouponClickListener) {
        this.mOnCouponClickListener = mOnCouponClickListener;
    }
}
