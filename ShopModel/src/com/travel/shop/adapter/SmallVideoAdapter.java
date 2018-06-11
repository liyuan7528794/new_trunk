package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.bean.CCTVVideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;

import java.util.ArrayList;

/**
 * cctv全视频中细分视频的适配器
 * Created by wyp on 2017/12/05.
 */

public class SmallVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<CCTVVideoInfoBean> listData;
    private CCTVVideoInfoBean mCCTVVideoInfoBean;
    private OnItemClickListener mOnItemClickListener = null;

    public SmallVideoAdapter(ArrayList<CCTVVideoInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_small_video_item, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((int) v.getTag());
                }
            }
        });
        return new SmallVideoHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == listData.size() - 1) {
            holder.itemView.setPadding(0, 0, 0, OSUtil.dp2px(mContext, 15));
        }
        if (holder instanceof SmallVideoHolder) {
            mCCTVVideoInfoBean = listData.get(position);
            // 视频图片
            ImageDisplayTools.disPlayRoundDrawable(mCCTVVideoInfoBean.getImgUrl(),
                    ((SmallVideoHolder) holder).small_video_img, OSUtil.dp2px(mContext, 2));
            // 标题
            ((SmallVideoHolder) holder).small_video_title.setText(mCCTVVideoInfoBean.getTitle());
            // 简介
            ((SmallVideoHolder) holder).small_video_intro.setText(mCCTVVideoInfoBean.getContent());
            ((SmallVideoHolder) holder).itemView.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class SmallVideoHolder extends RecyclerView.ViewHolder {

        ImageView small_video_img;
        TextView small_video_title, small_video_intro;

        public SmallVideoHolder(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            small_video_img = (ImageView) itemView.findViewById(R.id.small_video_img);
            small_video_title = (TextView) itemView.findViewById(R.id.small_video_title);
            small_video_intro = (TextView) itemView.findViewById(R.id.small_video_intro);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
