package com.travel.video.adapter;

import java.util.List;

import com.travel.Constants;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;

/**
 * 视频列表Adapter
 */
public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<VideoInfoBean> list;
    private OnItemListener onItemListener;

    public VideosAdapter(Context context, List<VideoInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemListener {
        public void onItemClick(View view, int position);

        public void onItemLongClick(View view, int position);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_video_item, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int padding = OSUtil.dp2px(context, 5);
        holder.itemView.setPadding(padding, padding, padding, padding);
        VideoInfoBean bean = list.get(position);
        String imageUrl = bean.getVideoImg();
        if ("".equals(imageUrl)) {
            imageUrl = Constants.DefaultHeadImg;
        }
        ImageDisplayTools.displayImage(imageUrl, ((MyViewHolder) holder).coverImage);
        ((MyViewHolder) holder).liveTitle.setText(bean.getVideoTitle());
        ((MyViewHolder) holder).date.setText(bean.getCreateTime());

        if (1 == bean.getVideoStatus()) {    //直播
            ((MyViewHolder) holder).mark.setImageResource(R.drawable.live_mark);
            ((MyViewHolder) holder).image2.setVisibility(View.GONE);
            ((MyViewHolder) holder).image3.setVisibility(View.GONE);
            ((MyViewHolder) holder).watchNum.setVisibility(View.GONE);
            ((MyViewHolder) holder).commentNum.setVisibility(View.GONE);
            ((MyViewHolder) holder).mark.setVisibility(View.VISIBLE);
        } else if (2 == bean.getVideoStatus()) {
            ((MyViewHolder) holder).watchNum.setText(bean.getWatchCount() + "");
            ((MyViewHolder) holder).commentNum.setText(bean.getCommentCount() + "");
            ((MyViewHolder) holder).mark.setVisibility(View.GONE);
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
        ImageView coverImage, mark, image2, image3;
        TextView liveTitle, date, watchNum, commentNum;

        public MyViewHolder(View view) {
            super(view);
            coverImage = (ImageView) view.findViewById(R.id.liveImage);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) coverImage.getLayoutParams();
            params.width = (OSUtil.getScreenWidth() - OSUtil.dp2px(context, 15)) / 2;
            params.height = params.width;
            coverImage.setLayoutParams(params);
            image2 = (ImageView) view.findViewById(R.id.image2);
            image3 = (ImageView) view.findViewById(R.id.image3);
            liveTitle = (TextView) view.findViewById(R.id.liveTitleName);
            date = (TextView) view.findViewById(R.id.date);
            watchNum = (TextView) view.findViewById(R.id.watchNum);
            commentNum = (TextView) view.findViewById(R.id.commentNum);
            mark = (ImageView) view.findViewById(R.id.mark);
        }
    }

}