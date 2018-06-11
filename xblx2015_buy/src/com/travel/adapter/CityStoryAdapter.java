package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.layout.SelectableRoundedImageView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.bean.CityBean;

import java.util.ArrayList;

/**
 * 小城故事的适配器
 * Created by wyp on 2018/5/15.
 */

public class CityStoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<CityBean> list;
    private int type;// 1:小城故事 2:推荐城市 3:大城小事
    private OnItemListener onItemListener;

    public CityStoryAdapter(Context context, ArrayList<CityBean> list, int type) {
        this.context = context;
        this.list = list;
        this.type = type;
    }

    public interface OnItemListener {
        void onItemClick(String storyId, int type);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (type == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.small_city_story_item, parent, false);
        } else if (type == 3) {
            view = LayoutInflater.from(context).inflate(R.layout.big_city_story_item, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.command_city_story_item, parent, false);
        }
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int lr = OSUtil.dp2px(context, 15);
        int middle = OSUtil.dp2px(context, 10);
        if (type == 1) {
            holder.itemView.setPadding(0, 0, middle, 0);
            if (position == 0)
                holder.itemView.setPadding(lr, 0, middle, 0);
            if (position == list.size() - 1 && position != 0)
                holder.itemView.setPadding(0, 0, lr, 0);
        } else if (type == 3) {
            holder.itemView.setPadding(0, 0, lr, 0);
            if (position == 0) {
                holder.itemView.setPadding(lr, 0, lr, 0);
            }
        }
        holder.itemView.setTag(position);
        CityBean bean = list.get(position);

        if (type == 1) {
            // 图片
            ImageDisplayTools.displayImageRoundCity(bean.getImgUrl(), ((MyViewHolder) holder).iv_small_city_story_img);
            // 标题
            ((MyViewHolder) holder).tv_small_city_story_title.setText(bean.getCityDescribe() + " " + bean.getSubhead());
        } else if (type == 3) {
            // 图片
            ImageDisplayTools.displayImageRoundCity(bean.getImgUrl(), ((MyViewHolder) holder).iv_big_city_story_img);
            // 标题
            ((MyViewHolder) holder).tv_big_city_story_title.setText(bean.getCityDescribe() + " " + bean.getSubhead());
        } else {
            // 图片
            ImageDisplayTools.displayImageRoundCity(bean.getImgUrl(), ((MyViewHolder) holder).iv_command_city_story_img);
            TravelUtil.setLLParamsWidthPart(((MyViewHolder) holder).iv_command_city_story_img, 1, 30, 69, 45);
            // 标题
            ((MyViewHolder) holder).tv_command_city_story_title.setText(bean.getCityDescribe() + " | " + bean.getSubhead());
            // 描述
            ((MyViewHolder) holder).tv_command_city_description.setText(bean.getCityDescribe());
        }

        if (onItemListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) holder.itemView.getTag();
                    onItemListener.onItemClick(list.get(position).getStoryId(), type);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_small_city_story_img, iv_big_city_story_img, iv_command_city_story_img;
        TextView tv_small_city_story_title, tv_big_city_story_title, tv_command_city_story_title, tv_command_city_description;

        public MyViewHolder(View view) {
            super(view);
            ImageDisplayTools.initImageLoader(context);
            iv_small_city_story_img = (SelectableRoundedImageView) view.findViewById(R.id.iv_small_city_story_img);
            tv_small_city_story_title = (TextView) view.findViewById(R.id.tv_small_city_story_title);
            iv_big_city_story_img = (SelectableRoundedImageView) view.findViewById(R.id.iv_big_city_story_img);
            tv_big_city_story_title = (TextView) view.findViewById(R.id.tv_big_city_story_title);
            iv_command_city_story_img = (SelectableRoundedImageView) view.findViewById(R.id.iv_command_city_story_img);
            tv_command_city_story_title = (TextView) view.findViewById(R.id.tv_command_city_story_title);
            tv_command_city_description = (TextView) view.findViewById(R.id.tv_command_city_description);
        }
    }

}