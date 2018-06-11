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
 * 商城首页的适配器----推荐商品
 * Created by wyp on 2017/1/10.
 */

public class OutGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GoodsBasicInfoBean> listData;
    private int paddingBelow = 0;
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private OnItemClickListener mOnItemClickListener = null;

    public enum ITEM_TYPE {
        SMALL,
        BIG,
    }

    public OutGoodsAdapter(ArrayList<GoodsBasicInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
        paddingBelow = OSUtil.dp2px(mContext, 18);
        paddingLeft = OSUtil.dp2px(mContext, 25);
        paddingRight = OSUtil.dp2px(mContext, 14);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        //        if (viewType == ITEM_TYPE.SMALL.ordinal())
        view = View.inflate(mContext, R.layout.adapter_out_goods_item1, null);
        //        else // 已弃用
        //            view = View.inflate(mContext, R.layout.adapter_out_goods_item2, null);
        //        if (viewType == ITEM_TYPE.SMALL.ordinal())
        return new OutHodler1(view);
        //        else
        //            return new OutHodler2(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (listData.size() < 1)
            return;
        holder.itemView.setPadding(paddingLeft, 0, paddingRight, paddingBelow);
        if (position == 0)
            ((OutHodler1) holder).v_line_city.setVisibility(View.GONE);
        else
            ((OutHodler1) holder).v_line_city.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder);
                }
            }
        });
        if (holder instanceof OutHodler1) {
            ImageDisplayTools.displayImageRound(listData.get(position).getGoodsImg(), ((OutHodler1) holder).iv_out_goods_img1);
            if (!OSUtil.isDayTheme())
                ((OutHodler1) holder).iv_out_goods_img1.setColorFilter(TravelUtil.getColorFilter(mContext));
            ((OutHodler1) holder).tv_out_goods_title1.setText(listData.get(position).getGoodsTitle());
            ((OutHodler1) holder).tv_out_goods_content1.setText(listData.get(position).getSubhead());
        } else if (holder instanceof OutHodler2) {
            ShopTool.setLL345w2h193(((OutHodler2) holder).iv_out_goods_img2, paddingLeft + paddingRight);
            ImageDisplayTools.displayImageRound(listData.get(position).getGoodsImg(), ((OutHodler2) holder).iv_out_goods_img2);
            ((OutHodler2) holder).tv_out_goods_title2.setText(listData.get(position).getGoodsTitle());
            ((OutHodler2) holder).tv_out_goods_content2.setText(listData.get(position).getSubhead());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (listData.get(position).getGoodsLayout() == 1)
            return ITEM_TYPE.SMALL.ordinal();
        else
            return ITEM_TYPE.BIG.ordinal();
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class OutHodler1 extends RecyclerView.ViewHolder {

        ImageView iv_out_goods_img1;
        TextView tv_out_goods_title1, tv_out_goods_content1;
        View v_line_city;

        public OutHodler1(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            v_line_city = itemView.findViewById(R.id.v_line_city);
            iv_out_goods_img1 = (ImageView) itemView.findViewById(R.id.iv_out_goods_img1);
            tv_out_goods_title1 = (TextView) itemView.findViewById(R.id.tv_out_goods_title1);
            tv_out_goods_content1 = (TextView) itemView.findViewById(R.id.tv_out_goods_content1);
        }
    }

    class OutHodler2 extends RecyclerView.ViewHolder {

        ImageView iv_out_goods_img2;
        TextView tv_out_goods_title2, tv_out_goods_content2;

        public OutHodler2(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_out_goods_img2 = (ImageView) itemView.findViewById(R.id.iv_out_goods_img2);
            tv_out_goods_title2 = (TextView) itemView.findViewById(R.id.tv_out_goods_title2);
            tv_out_goods_content2 = (TextView) itemView.findViewById(R.id.tv_out_goods_content2);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder holder);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
