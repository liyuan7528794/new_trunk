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
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;

import java.util.ArrayList;

/**
 * cctv视频里的适配器
 * Created by wyp on 2017/12/01.
 */

public class CCTVVideoContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<CCTVVideoInfoBean> cctvContents;
    private CCTVVideoInfoBean cctvContent;
    private OnItemClickListener mOnItemClickListener = null;
    private int flag = 1;// 1:从cctv视频进入 2:从“更多”进入

    public CCTVVideoContentAdapter(Context mContext, ArrayList<CCTVVideoInfoBean> cctvContents) {
        this.mContext = mContext;
        this.cctvContents = cctvContents;
    }

    public CCTVVideoContentAdapter(Context mContext, ArrayList<CCTVVideoInfoBean> cctvContents, int flag) {
        this.mContext = mContext;
        this.cctvContents = cctvContents;
        this.flag = flag;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_cctv_video_content_item, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((int) v.getTag());
                }
            }
        });
        return new CCTVVideoContentHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int padding = OSUtil.dp2px(mContext, 15);
        if (flag == 2) {
            holder.itemView.setPadding(0, 0, 0, padding);
            if (position == 0 || position == 1)
                holder.itemView.setPadding(0, padding, 0, padding);
        }
        if (holder instanceof CCTVVideoContentHolder) {
            cctvContent = cctvContents.get(position);
            ((CCTVVideoContentHolder) holder).layout_data.setVisibility(View.GONE);
            ((CCTVVideoContentHolder) holder).layout_more.setVisibility(View.GONE);
            if ((position < 5 && flag == 1) || flag == 2) {
                ((CCTVVideoContentHolder) holder).layout_data.setVisibility(View.VISIBLE);
                // 图片
                ImageDisplayTools.disPlayRoundDrawable(cctvContent.getImgUrl(), ((CCTVVideoContentHolder) holder).iv_cctv_content_img, OSUtil.dp2px(mContext, 2));
                if (flag == 2) {
                    TravelUtil.setLLParamsWidthPart(((CCTVVideoContentHolder) holder).iv_cctv_content_img, 2, 45, 158, 129);
                }
                // 标题
                ((CCTVVideoContentHolder) holder).tv_cctv_content_title.setText(cctvContent.getTitle());
            } else {
                ((CCTVVideoContentHolder) holder).layout_more.setVisibility(View.VISIBLE);
            }
            holder.itemView.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        if (flag == 1)
            return cctvContents == null ? 0 : (cctvContents.size() > 5 ? 6 : cctvContents.size());
        else
            return cctvContents == null ? 0 : cctvContents.size();
    }

    class CCTVVideoContentHolder extends RecyclerView.ViewHolder {

        View layout_data, layout_more;
        ImageView iv_cctv_content_img;
        TextView tv_cctv_content_title;


        public CCTVVideoContentHolder(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            layout_data = itemView.findViewById(R.id.layout_data);
            iv_cctv_content_img = (ImageView) itemView.findViewById(R.id.iv_cctv_content_img);
            tv_cctv_content_title = (TextView) itemView.findViewById(R.id.tv_cctv_content_title);
            layout_more = itemView.findViewById(R.id.layout_more);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
