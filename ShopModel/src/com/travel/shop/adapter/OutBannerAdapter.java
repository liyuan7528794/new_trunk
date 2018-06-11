package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.travel.bean.NotifyBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;

import java.util.List;

/**
 * Created by Administrator on 2017/1/16.
 */

public class OutBannerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<NotifyBean> listData;
    private OnItemClickListener mOnItemClickListener = null;

    public OutBannerAdapter(List<NotifyBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.out_banner_item, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        NotifyBean notifyBean = listData.get(position);
        ImageDisplayTools.disPlayRoundDrawable(notifyBean.getImgUrl(), ((MyHolder) holder).iv_banner, OSUtil.dp2px(mContext, 4));
        ShopTool.setRLParamsWidthPart(((MyHolder) holder).iv_banner, 2, 0, 188, 115);
        ((MyHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView iv_banner;

        public MyHolder(View itemView) {
            super(itemView);
            iv_banner = (ImageView) itemView.findViewById(R.id.iv_banner);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
