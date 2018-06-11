package com.travel.shop.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.bean.NotifyBean;
import com.travel.shop.R;

import java.util.ArrayList;

/**
 * 预定页活动的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2017/05/04
 */
public class CommitOrderActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<NotifyBean> listData;
    private String tag;
    private NotifyBean activity;

    public CommitOrderActivityAdapter(ArrayList<NotifyBean> listData, String tag, Context context) {
        this.mContext = context;
        this.listData = listData;
        this.tag = tag;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.adapter_commit_activity_item, null);
        final Holder holder = new Holder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnActivityClickListener != null)
                    mOnActivityClickListener.onClick(holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Holder) {
            activity = listData.get(position);

            ((Holder) holder).tv_tag.setText(TextUtils.equals("ensure", tag) ? "保障" : "活动");
            if (position != 0)
                ((Holder) holder).tv_tag.setVisibility(View.INVISIBLE);
            // 活动名称
            ((Holder) holder).tv_activity.setText(position + 1 + ". " + activity.getTitle());
            if (!TextUtils.equals("ensure", tag)) {
                ((Holder) holder).tv_activity.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));
                ((Holder) holder).tv_activity.setTypeface(Typeface.DEFAULT_BOLD);
                ((Holder) holder).tv_tag.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));
                ((Holder) holder).tv_tag.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {
        public TextView tv_activity, tv_tag;

        public Holder(View itemView) {
            super(itemView);
            tv_activity = (TextView) itemView.findViewById(R.id.tv_activity);
            tv_tag = (TextView) itemView.findViewById(R.id.tv_tag);
        }
    }

    public interface OnActivityClickListener {
        void onClick(int position);
    }

    private OnActivityClickListener mOnActivityClickListener;

    public void setmOnActivityClickListener(OnActivityClickListener mOnActivityClickListener) {
        this.mOnActivityClickListener = mOnActivityClickListener;
    }
}
