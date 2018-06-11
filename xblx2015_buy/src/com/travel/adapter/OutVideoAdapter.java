package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.tools.ShopTool;

import java.util.List;

/**
 * Created by Administrator on 2017/1/17.
 */

public class OutVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<VideoInfoBean> list;
    private OnItemListener onItemListener;

    public OutVideoAdapter(Context context, List<VideoInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.out_video_item, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int lr = OSUtil.dp2px(context, 15);
        int td = OSUtil.dp2px(context, 18);
        holder.itemView.setPadding(0, td, lr, td);
        if (position == 0)
            holder.itemView.setPadding(lr, td, lr, td);
        VideoInfoBean bean = list.get(position);
        String imageUrl = bean.getVideoImg();
        if ("".equals(imageUrl)) {
            imageUrl = Constants.DefaultHeadImg;
        }
        ImageDisplayTools.displayImageRound(imageUrl, ((MyViewHolder) holder).coverImage);
        ShopTool.setRLParamsWidthPart(((MyViewHolder) holder).coverImage, 1, 30, 115, 53);
        ImageDisplayTools.displayHeadImage(bean.getPersonalInfoBean().getUserPhoto(), ((MyViewHolder) holder).headImg);
        ((MyViewHolder) holder).title.setText(bean.getVideoTitle());
        ((MyViewHolder) holder).name.setText(bean.getPersonalInfoBean().getUserName() + " 在 " + bean.getShareAddress());

        if (1 == bean.getVideoStatus()) {    //直播
            ((MyViewHolder) holder).mark.setVisibility(View.VISIBLE);
            ((MyViewHolder) holder).mark.setImageResource(R.drawable.live_mark);
        } else if (2 == bean.getVideoStatus()) {//回放
            ((MyViewHolder) holder).mark.setVisibility(View.VISIBLE);
            ((MyViewHolder) holder).mark.setImageResource(R.drawable.playback_mark);
        }

        if (onItemListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemListener.onItemClick(v, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemListener.onItemLongClick(v, position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage, mark, headImg;
        TextView title, name;

        public MyViewHolder(View view) {
            super(view);
            coverImage = (ImageView) view.findViewById(R.id.iv_cover);
            headImg = (ImageView) view.findViewById(R.id.iv_head_img);
            mark = (ImageView) view.findViewById(R.id.iv_mark);
            title = (TextView) view.findViewById(R.id.tv_title);
            name = (TextView) view.findViewById(R.id.tv_video_name);
        }
    }

}