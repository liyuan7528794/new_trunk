package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/19.
 */

public class PersonalHomeStoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GoodsBasicInfoBean> listData;
    private int paddingBelow = 0;
    private int paddingLeftAndRight = 0;
    private int radios = 0;
    private OnItemClickListener mOnItemClickListener = null;

    public PersonalHomeStoryAdapter(ArrayList<GoodsBasicInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
        paddingBelow = OSUtil.dp2px(mContext, 18);
        paddingLeftAndRight = OSUtil.dp2px(mContext, 30);
        radios = OSUtil.dp2px(mContext, 4);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_personal_home_story, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(position == 0)
            holder.itemView.setPadding(0, paddingBelow, 0, 0);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
        ImageDisplayTools.displayImageRound(listData.get(position).getGoodsImg(),
                ((MyHolder) holder).iv_out_goods_img);
        ShopTool.setLLParamsWidth(((MyHolder) holder).iv_out_goods_img, 11, 5, 30);
        if (!OSUtil.isDayTheme())
            ((MyHolder) holder).iv_out_goods_img.setColorFilter(TravelUtil.getColorFilter(mContext));
        ((MyHolder) holder).tv_out_goods_title.setText(listData.get(position).getGoodsTitle());
        ((MyHolder) holder).tv_comment_num.setText(listData.get(position).getCommentCount()+"");
        ((MyHolder) holder).tv_watch_num.setText(listData.get(position).getReadCount()+"");
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView iv_out_goods_img;
        TextView tv_out_goods_title, tv_comment_num, tv_watch_num;

        public MyHolder(View itemView) {
            super(itemView);
            iv_out_goods_img = (ImageView) itemView.findViewById(R.id.iv_out_goods_img);
            tv_out_goods_title = (TextView) itemView.findViewById(R.id.tv_out_goods_title);
            tv_comment_num = (TextView) itemView.findViewById(R.id.tv_comment_num);
            tv_watch_num = (TextView) itemView.findViewById(R.id.tv_watch_num);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
