package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * 发起众投的订单选择的适配器
 * Created by wyp on 2017/2/20.
 */

public class SelectOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<OrderBean> listData;
    private OrderBean mOrderBean;
    private OnItemClickListener mOnItemClickListener = null;

    public SelectOrdersAdapter(ArrayList<OrderBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_select_orders_item, null);
        final SelectOrdersHolder mSelectOrdersHolder = new SelectOrdersHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = mSelectOrdersHolder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(listData.get(position));
                }
            }
        });
        return mSelectOrdersHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setPadding(OSUtil.dp2px(mContext, 15), 0, OSUtil.dp2px(mContext, 15), 0);
        if (holder instanceof SelectOrdersHolder) {
            mOrderBean = listData.get(position);
            // 订单号
            ((SelectOrdersHolder) holder).tv_ordersId.setText("订单号：" + mOrderBean.getOrdersId() + "");
            // 商品名
            ((SelectOrdersHolder) holder).tv_goods_name.setText(mOrderBean.getGoodsTitle());
            // 支付金额
            ((SelectOrdersHolder) holder).tv_goods_price.setText("￥" + ShopTool.getMoney(mOrderBean.getPaymentPrice() + ""));
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class SelectOrdersHolder extends RecyclerView.ViewHolder {

        TextView tv_ordersId, tv_goods_name, tv_goods_price;

        public SelectOrdersHolder(View itemView) {
            super(itemView);
            tv_ordersId = (TextView) itemView.findViewById(R.id.tv_ordersId);
            tv_goods_name = (TextView) itemView.findViewById(R.id.tv_goods_name);
            tv_goods_price = (TextView) itemView.findViewById(R.id.tv_goods_price);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(OrderBean mOrderBean);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
