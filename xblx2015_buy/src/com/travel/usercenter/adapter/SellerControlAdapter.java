package com.travel.usercenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 商家后台管理的适配器
 * Created by Administrator on 2017/7/12.
 */

public class SellerControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String, String>> listData;
    private OnItemClickListener mOnItemClickListener = null;

    public SellerControlAdapter(ArrayList<HashMap<String, String>> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_seller_control_item, null);
        final SellerControlHodler holder = new SellerControlHodler(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int margin = OSUtil.dp2px(mContext, 15);
        holder.itemView.setPadding(margin, margin, 0, 0);
        if (position == 1)
            holder.itemView.setPadding(OSUtil.dp2px(mContext, 10), margin, 0, 0);
        if (position == 2)
            holder.itemView.setPadding(OSUtil.dp2px(mContext, 5), margin, 0, 0);
        if (holder instanceof SellerControlHodler) {
            ShopTool.setLLParamsWidthPart(((SellerControlHodler) holder).ll_control_layout, 3, 90, 1, 1);
            // 图片
            ((SellerControlHodler) holder).iv_control_picture.setBackgroundResource(Integer.parseInt(listData.get(position).get("picture")));
            // 标题
            ((SellerControlHodler) holder).tv_control_content.setText(listData.get(position).get("title"));
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class SellerControlHodler extends RecyclerView.ViewHolder {

        ImageView iv_control_picture;
        TextView tv_control_content;
        View ll_control_layout;

        public SellerControlHodler(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_control_picture = (ImageView) itemView.findViewById(R.id.iv_control_picture);
            tv_control_content = (TextView) itemView.findViewById(R.id.tv_control_content);
            ll_control_layout = itemView.findViewById(R.id.ll_control_layout);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder holder);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}

