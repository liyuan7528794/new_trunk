package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.entity.PublicVoteEntity;
import com.travel.layout.LadderRoundedImageView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/5/2.
 */

public class VoteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<PublicVoteEntity> listData;
    private ClickListener clickListener;

    public interface ClickListener{
        void onClick(int position);
    }

    public VoteListAdapter(Context context, List<PublicVoteEntity> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_vote_list_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PublicVoteEntity entity = listData.get(position);
        if(position == 0){
            holder.itemView.setPadding(0, OSUtil.dp2px(mContext, 10), 0, 0);
        }else if(position == listData.size()-1){
            holder.itemView.setPadding(0, 0, 0, OSUtil.dp2px(mContext, 10));
        }else{
            holder.itemView.setPadding(0, 0, 0, 0);
        }

        ((MyHolder)holder).tv_vote_status.setTextColor(mContext.getResources().getColor(R.color.gray_9));
        ((MyHolder)holder).tv_vote_status.setText("处理中...");

        ImageDisplayTools.displayImageRound(entity.getBuyer().getImgUrl(), ((MyHolder)holder).left_img);
        ImageDisplayTools.displayImageRound(entity.getSeller().getImgUrl(), ((MyHolder)holder).right_img);

        // 未发布众投
        if (entity.getStatus() == 0)
            ((MyHolder)holder).tv_vote_status.setText("待发布");
            // 已发布众投
        else if (entity.getStatus() == 1) {
            // 审核中
            if (entity.getCheckStatus() == 0)
                ((MyHolder)holder).tv_vote_status.setText("等待平台审核");
            else if (entity.getCheckStatus() == 1) {
                ((MyHolder) holder).tv_vote_status.setTextColor(mContext.getResources().getColor(R.color.yellow_F5A623));
                ((MyHolder) holder).tv_vote_status.setText("众投中");
            } else if (entity.getCheckStatus() == 2)
                ((MyHolder) holder).tv_vote_status.setText("审核失败");

            // 进行中 即 审核通过
        } else if (entity.getStatus() == 2) {
            ((MyHolder) holder).tv_vote_status.setTextColor(mContext.getResources().getColor(R.color.yellow_F5A623));
            ((MyHolder) holder).tv_vote_status.setText("众投中");
            // 已结束
        }else if (entity.getStatus() == 3) {
            ((MyHolder)holder).tv_vote_status.setText("已结束");
            // 若不刷新，则使当前条保持“距结束：0天0小时0分”
        } else if (entity.getStatus() == -1) {
            ((MyHolder)holder).tv_vote_status.setText("已结束");
        }

        ((MyHolder)holder).content.setText(entity.getReason());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView tv_vote_status, content;
        LadderRoundedImageView left_img, right_img;
        public MyHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.content);
            tv_vote_status = (TextView) itemView.findViewById(R.id.tv_vote_status);
            left_img = (LadderRoundedImageView) itemView.findViewById(R.id.left_img);
            right_img = (LadderRoundedImageView) itemView.findViewById(R.id.right_img);
        }
    }
}
