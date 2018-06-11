package com.travel.shop.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.travel.shop.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 选择出发地以及月份的适配器
 * Created by wyp on 2017/11/01.
 */

public class SelectStartPlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String, Object>> listData;
    private int flag;
    private OnItemClickListener mOnItemClickListener = null;

    public SelectStartPlaceAdapter(ArrayList<HashMap<String, Object>> listData, Context context, int flag) {
        this.listData = listData;
        mContext = context;
        this.flag = flag;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_select_place_item, parent, false);
        final OutHodler hodler = new OutHodler(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick((int) hodler.rb_goods_start_place.getTag());
            }
        });
        return new OutHodler(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OutHodler) {
            boolean isChecked = (boolean) listData.get(position).get("isChecked");
            ((OutHodler) holder).rb_goods_start_place.setTag(position);
            ((OutHodler) holder).rb_goods_start_place.setChecked(isChecked);
            if (flag == 1) {// 出发地选择
                ((OutHodler) holder).rb_goods_start_place.setText(listData.get(position).get("name").toString());
                if (isChecked)
                    ((OutHodler) holder).rb_goods_start_place.setTextColor(ContextCompat.getColor(mContext, R.color.red_FA7E7F));
                else
                    ((OutHodler) holder).rb_goods_start_place.setTextColor(ContextCompat.getColor(mContext, R.color.black_6C6F73));
            } else if (flag == 2) {// 月份选择
                ((OutHodler) holder).rb_goods_start_place.setText(listData.get(position).get("month") + "月");
                if (isChecked)
                    ((OutHodler) holder).rb_goods_start_place.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                else
                    ((OutHodler) holder).rb_goods_start_place.setTextColor(ContextCompat.getColor(mContext, R.color.black_6C6F73));

            }
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class OutHodler extends RecyclerView.ViewHolder {

        RadioButton rb_goods_start_place;

        public OutHodler(View itemView) {
            super(itemView);
            rb_goods_start_place = (RadioButton) itemView.findViewById(R.id.rb_goods_start_place);
            if (flag == 2) {
                rb_goods_start_place.setBackgroundResource(R.drawable.selector_select_month);
                rb_goods_start_place.setTextSize(14);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
