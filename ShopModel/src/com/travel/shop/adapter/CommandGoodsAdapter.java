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

import java.util.ArrayList;

/**
 * 纪录片页的推荐视频的适配器
 * Created by wyp on 2017/6/16.
 */

public class CommandGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GoodsBasicInfoBean> listData;
    private GoodsBasicInfoBean mGoodsBasicInfoBean;
    private OnItemClickListener mOnItemClickListener = null;

    public CommandGoodsAdapter(ArrayList<GoodsBasicInfoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_command_goods, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((String) v.getTag());
                }
            }
        });
        return new CommandGoodsHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setPadding(OSUtil.dp2px(mContext, 5), 0, OSUtil.dp2px(mContext, 5), 0);
        if (position == 0)
            holder.itemView.setPadding(OSUtil.dp2px(mContext, 15), 0, OSUtil.dp2px(mContext, 5), 0);
        if (position == 2)
            holder.itemView.setPadding(OSUtil.dp2px(mContext, 5), 0, OSUtil.dp2px(mContext, 15), 0);
        if (holder instanceof CommandGoodsHolder) {
            mGoodsBasicInfoBean = listData.get(position);
            // 推荐图片
            ImageDisplayTools.displayImageRound(mGoodsBasicInfoBean.getGoodsImg(),
                    ((CommandGoodsHolder) holder).iv_command_goods);
            TravelUtil.setLLParamsWidthPart(((CommandGoodsHolder)holder).iv_command_goods, 3, 50, 1, 1);
            if (!OSUtil.isDayTheme())
                ((CommandGoodsHolder) holder).iv_command_goods.setColorFilter(TravelUtil.getColorFilter(mContext));
            // 标题
            ((CommandGoodsHolder) holder).tv_command_city.setText(mGoodsBasicInfoBean.getGoodsTitle());
            ((CommandGoodsHolder) holder).itemView.setTag(mGoodsBasicInfoBean.getStoryId());
        }
    }

    @Override
    public int getItemCount() {
        return listData.size() > 3 ? 3 : listData.size();
    }

    class CommandGoodsHolder extends RecyclerView.ViewHolder {

        ImageView iv_command_goods;
        TextView tv_command_city;

        public CommandGoodsHolder(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_command_goods = (ImageView) itemView.findViewById(R.id.iv_command_goods);
            tv_command_city = (TextView) itemView.findViewById(R.id.tv_command_city);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(String storyId);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
