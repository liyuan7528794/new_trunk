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
 * 出行人信息展示的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/11/03
 */
public class TouristInfoShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<TouristInfo> listData;
    private TouristInfo tourist;

    public TouristInfoShowAdapter(ArrayList<TouristInfo> listData, Context context) {
        this.mContext = context;
        this.listData = listData;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_tourist_info_show_item, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Holder) {
            if(position == listData.size() - 1)
                ((Holder) holder).v_line_tourist.setVisibility(View.GONE);
            tourist = listData.get(position);
            // 编号
            ((Holder) holder).tv_number.setText(position + 1 + "");
            // 姓名
            ((Holder) holder).tv_tourist_name.setText(tourist.getName());
            // 性别
            ((Holder) holder).tv_tourist_sex.setText(tourist.getSex());
            // 身份证号
            ((Holder) holder).tv_tourist_ID.setText("身份证号：" + tourist.getIDCard());
        }

    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public TextView tv_number, tv_tourist_name, tv_tourist_sex, tv_tourist_ID;
        public View v_line_tourist;

        public Holder(View itemView) {
            super(itemView);
            tv_number = (TextView) itemView.findViewById(R.id.tv_number);
            tv_tourist_name = (TextView) itemView.findViewById(R.id.tv_tourist_name);
            tv_tourist_sex = (TextView) itemView.findViewById(R.id.tv_tourist_sex);
            tv_tourist_ID = (TextView) itemView.findViewById(R.id.tv_tourist_ID);
            v_line_tourist = itemView.findViewById(R.id.v_line_tourist);
        }
    }
}
