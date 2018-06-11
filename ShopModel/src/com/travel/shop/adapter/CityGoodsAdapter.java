package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.shop.R;

import java.util.ArrayList;

/**
 * 城市页的适配器----推荐商品
 * Created by wyp on 2017/1/11.
 */

public class CityGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GoodsBasicInfoBean> listData;
    private GoodsBasicInfoBean mGoodsBasicInfoBean;
    private OnItemClickListener mOnItemClickListener = null;

    public CityGoodsAdapter(ArrayList<GoodsBasicInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_city_story_item, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((String) v.getTag());
                }
            }
        });
        return new OutHodler(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OutHodler) {
//        if(position == listData.size() - 1){
//            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.
//            holder.itemView.setm
//        }
            mGoodsBasicInfoBean = listData.get(position);
            // 商品图片
            ImageDisplayTools.displayImageRound(mGoodsBasicInfoBean.getGoodsImg(),
                    ((OutHodler) holder).iv_city_story_img);
            // 标题
            ((OutHodler) holder).tv_city_story_title.setText(mGoodsBasicInfoBean.getGoodsTitle());
            // 副标题
            ((OutHodler) holder).tv_city_story_subhead.setText(mGoodsBasicInfoBean.getSubhead());
            // 阅读数
            ((OutHodler) holder).tv_city_story_readcount.setText("阅读 " + mGoodsBasicInfoBean.getReadCount());
            // 评论数
            ((OutHodler) holder).tv_city_story_commentcount.setText("评论 " + mGoodsBasicInfoBean.getCommentCount());
            ((OutHodler) holder).itemView.setTag(mGoodsBasicInfoBean.getStoryId());
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class OutHodler extends RecyclerView.ViewHolder {

        ImageView iv_city_story_img;
        TextView tv_city_story_title, tv_city_story_readcount, tv_city_story_commentcount, tv_city_story_subhead;

        public OutHodler(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_city_story_img = (ImageView) itemView.findViewById(R.id.iv_city_story_img);
            tv_city_story_title = (TextView) itemView.findViewById(R.id.tv_city_story_title);
            tv_city_story_readcount = (TextView) itemView.findViewById(R.id.tv_city_story_readcount);
            tv_city_story_commentcount = (TextView) itemView.findViewById(R.id.tv_city_story_commentcount);
            tv_city_story_subhead = (TextView) itemView.findViewById(R.id.tv_city_story_subhead);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(String storyId);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
