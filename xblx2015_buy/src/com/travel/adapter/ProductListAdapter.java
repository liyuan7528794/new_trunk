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
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.tools.ShopTool;

import java.util.List;

/**
 * Created by Administrator on 2017/1/17.
 */

public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<GoodsBasicInfoBean> list;
    private OnItemListener onItemListener;

    public ProductListAdapter(Context context, List<GoodsBasicInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_product_list, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int lr = OSUtil.dp2px(context, 15);
        holder.itemView.setPadding(lr, 0, lr, lr);
        if (position == 0)
            holder.itemView.setPadding(lr, lr, lr, lr);
        GoodsBasicInfoBean bean = list.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        ImageDisplayTools.disPlayRoundDrawable(bean.getGoodsImg(), myViewHolder.iv_cover, OSUtil.dp2px(context, 5));
        myViewHolder.tv_address.setText(bean.getGoodsAddress());
        myViewHolder.tv_title.setText(bean.getGoodsTitle());
        if (onItemListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemListener.onItemClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_cover;
        TextView tv_title, tv_address;

        public MyViewHolder(View view) {
            super(view);
            iv_cover = (ImageView) view.findViewById(R.id.iv_cover);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_address = (TextView) view.findViewById(R.id.tv_address);
        }
    }

}