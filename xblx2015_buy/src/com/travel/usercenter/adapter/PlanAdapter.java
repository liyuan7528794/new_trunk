package com.travel.usercenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.usercenter.entity.PlanEntity;

import java.util.ArrayList;

/**
 * Created by wyp on 2017/9/22.
 */

public class PlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<PlanEntity> plans;

    public PlanAdapter(Context mContext, ArrayList<PlanEntity> plans) {
        this.mContext = mContext;
        this.plans = plans;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_plan_item, parent, false);
        final PlanHolder planHolder = new PlanHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(planHolder.getAdapterPosition());
            }
        });
        return planHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlanHolder) {
            PlanEntity entity = plans.get(position);
            // 头像
            ImageDisplayTools.displayCircleImage(entity.getPhoto(), ((PlanHolder) holder).photo, 0);
            if (!OSUtil.isDayTheme())
                ((PlanHolder) holder).iv_plan_mask.setVisibility(View.VISIBLE);
            // 出发地
            ((PlanHolder) holder).startPlace.setText(entity.getDepart());
            // 目的地
            ((PlanHolder) holder).backPlace.setText(entity.getDestination());
            // 出发时间
            ((PlanHolder) holder).startTime.setText(entity.getDepartDate().substring(entity.getDepartDate().indexOf("-", 1)+1));
            // 返回时间
            ((PlanHolder) holder).backTime.setText(entity.getDestinationDate().substring(entity.getDestinationDate().indexOf("-", 1)+1));
        }
    }

    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    class PlanHolder extends RecyclerView.ViewHolder {

        private ImageView background, photo, iv_plan_mask;
        private TextView startPlace, backPlace, startTime, backTime;

        public PlanHolder(View itemView) {
            super(itemView);
            background = (ImageView) itemView.findViewById(R.id.iv_plan_background);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            iv_plan_mask = (ImageView) itemView.findViewById(R.id.iv_plan_mask);
            startPlace = (TextView) itemView.findViewById(R.id.tv_startplace);
            backPlace = (TextView) itemView.findViewById(R.id.tv_backplace);
            startTime = (TextView) itemView.findViewById(R.id.tv_starttime);
            backTime = (TextView) itemView.findViewById(R.id.tv_backtime);
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
