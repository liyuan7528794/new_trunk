package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.shop.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 订单时间的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/11/08
 */
public class OrderTimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String, String>> listData;
    private HashMap<String, String> time;

    public OrderTimeAdapter(ArrayList<HashMap<String, String>> listData, Context context) {
        this.mContext = context;
        this.listData = listData;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_order_time_item, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Holder) {
            time = listData.get(position);
            ((Holder) holder).tv_time_label.setText(time.get("label"));
            ((Holder) holder).tv_time_show.setText(time.get("show"));
        }

    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public TextView tv_time_label, tv_time_show;

        public Holder(View itemView) {
            super(itemView);
            tv_time_label = (TextView) itemView.findViewById(R.id.tv_time_label);
            tv_time_show = (TextView) itemView.findViewById(R.id.tv_time_show);
        }
    }
}
