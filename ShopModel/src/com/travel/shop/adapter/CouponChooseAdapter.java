package com.travel.shop.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.bean.CouponInfoBean;

import java.util.ArrayList;

/**
 * 卡券选择的适配器
 * Created by wyp on 2017/1/17.
 */

public class CouponChooseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<CouponInfoBean> listData;
    private CouponInfoBean mCouponInfoBean;
    private OnItemClickListener mOnItemClickListener = null;

    public CouponChooseAdapter(ArrayList<CouponInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CouponChooseHodler(View.inflate(mContext, R.layout.adapter_coupon_choose_item, null));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setPadding(OSUtil.dp2px(mContext, 15), 0, OSUtil.dp2px(mContext, 15), 0);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder);
                }
            }
        });
        if (holder instanceof CouponChooseHodler) {
            mCouponInfoBean = listData.get(position);
            // 背景 ①是否是赠送的
            if (mCouponInfoBean.isPresent())
                ((CouponChooseHodler) holder).ll_couponchoose.setBackgroundResource(R.drawable.coupon_pic_red);
            else
                ((CouponChooseHodler) holder).ll_couponchoose.setBackgroundResource(R.drawable.coupon_pic_bule);
            // ②是否是可用的
            if (mCouponInfoBean.getStatus() != 0 || mCouponInfoBean.getStatusCoupon() != 1)
                ((CouponChooseHodler) holder).ll_couponchoose.setBackgroundResource(R.drawable.coupon_pic_gray);
            // 名称
            ((CouponChooseHodler) holder).tv_couponchoose_name.setText(mCouponInfoBean.getCurrentPrice() + "元代金券");
//            ((CouponChooseHodler) holder).tv_couponchoose_name.setText(mCouponInfoBean.getCouponName() + (mCouponInfoBean.isPresent() ? "（赠）" : ""));
            // 价格
            ((CouponChooseHodler) holder).tv_couponchoose_price.setText("￥" + mCouponInfoBean.getCurrentPrice());
            // 有效期
            ((CouponChooseHodler) holder).tv_couponchoose_period.setText("有效期至：" + mCouponInfoBean.getEndDate());
            // 使用状态
            ((CouponChooseHodler) holder).tv_use.setVisibility(View.GONE);
            ((CouponChooseHodler) holder).iv_used.setVisibility(View.GONE);
            if (mCouponInfoBean.getStatus() == 0) {
                ((CouponChooseHodler) holder).tv_use.setVisibility(View.VISIBLE);
                ((CouponChooseHodler) holder).tv_use.setText("未使用");
                // 是否已选
                if (mCouponInfoBean.isChoosed())
                    ((CouponChooseHodler) holder).iv_choose.setBackgroundResource(R.drawable.coupon_pic_sel);
                else
                    ((CouponChooseHodler) holder).iv_choose.setBackground(null);
            } else if (mCouponInfoBean.getStatus() == 1) {
                ((CouponChooseHodler) holder).iv_used.setVisibility(View.VISIBLE);
            } else if (mCouponInfoBean.getStatus() == 2) {
                ((CouponChooseHodler) holder).tv_use.setVisibility(View.VISIBLE);
                ((CouponChooseHodler) holder).tv_use.setText("已过期");
            }
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class CouponChooseHodler extends RecyclerView.ViewHolder {

        LinearLayout ll_couponchoose;
        ImageView iv_choose, iv_used;
        TextView tv_couponchoose_name, tv_couponchoose_period, tv_couponchoose_price, tv_use;

        public CouponChooseHodler(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            ll_couponchoose = (LinearLayout) itemView.findViewById(R.id.ll_couponchoose);
            iv_choose = (ImageView) itemView.findViewById(R.id.iv_choose);
            iv_used = (ImageView) itemView.findViewById(R.id.iv_used);
            tv_couponchoose_name = (TextView) itemView.findViewById(R.id.tv_couponchoose_name);
            tv_couponchoose_period = (TextView) itemView.findViewById(R.id.tv_couponchoose_period);
            tv_couponchoose_price = (TextView) itemView.findViewById(R.id.tv_couponchoose_price);
            tv_use = (TextView) itemView.findViewById(R.id.tv_use);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder holder);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
