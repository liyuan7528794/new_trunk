package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.shop.R;
import com.travel.shop.bean.TouristInfo;

import java.util.ArrayList;

/**
 * 旅客信息展示的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/11/08
 */
public class TouristShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<TouristInfo> listData;
    private TouristInfo tourist;

    public TouristShowAdapter(ArrayList<TouristInfo> listData, Context context) {
        this.mContext = context;
        this.listData = listData;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_tourist_show_item, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Holder) {
            if (position == listData.size() - 1)
                ((Holder) holder).v_line_tourist_show.setVisibility(View.GONE);
            else
                ((Holder) holder).v_line_tourist_show.setVisibility(View.VISIBLE);
            tourist = listData.get(position);
            // 旅客名
            ((Holder) holder).tv_tourist_name_label.setText("旅客" + (position + 1));
            ((Holder) holder).tv_tourist_name_content.setText(tourist.getName());
            // 性别
            ((Holder) holder).tv_tourist_sex_content.setText(tourist.getSex());
            // 手机号
            ((Holder) holder).tv_tourist_phone_content.setText(tourist.getTelephone());
            // 身份证号
            ((Holder) holder).tv_tourist_id_content.setText(tourist.getIDCard());
        }

    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public TextView tv_tourist_name_label, tv_tourist_name_content, tv_tourist_sex_content,
                tv_tourist_phone_content, tv_tourist_id_content;
        public View v_line_tourist_show;

        public Holder(View itemView) {
            super(itemView);
            tv_tourist_name_label = (TextView) itemView.findViewById(R.id.tv_tourist_name_label);
            tv_tourist_name_content = (TextView) itemView.findViewById(R.id.tv_tourist_name_content);
            tv_tourist_sex_content = (TextView) itemView.findViewById(R.id.tv_tourist_sex_content);
            tv_tourist_phone_content = (TextView) itemView.findViewById(R.id.tv_tourist_phone_content);
            tv_tourist_id_content = (TextView) itemView.findViewById(R.id.tv_tourist_id_content);
            v_line_tourist_show = itemView.findViewById(R.id.v_line_tourist_show);
        }
    }
}
