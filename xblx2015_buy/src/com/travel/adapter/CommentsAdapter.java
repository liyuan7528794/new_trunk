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
import com.travel.layout.CustomLinearLayoutManager;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.shop.bean.CommentBean;

import java.util.List;

/**
 * Created by Administrator on 2017/12/4.
 */
public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<CommentBean> listData;
    private ClickListener clickListener;

    public interface ClickListener {
        void onClick(int position);

        void onClickContent(int position);
    }

    public CommentsAdapter(Context context, List<CommentBean> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyHolder myHolder = (MyHolder) holder;
        CommentBean bean = listData.get(position);
        ImageDisplayTools.displayHeadImage(bean.getUser().getImgUrl(), myHolder.iv_head);
        myHolder.tv_name.setText(bean.getUser().getNickName());
        myHolder.tv_time.setText(DateFormatUtil.getPastTime(bean.getSendTime()));
        myHolder.tv_content.setText(bean.getContent());

        // 子评论列表
        List<CommentBean> cBeans = bean.getSubComment();
        if (cBeans != null && cBeans.size() > 0) {
            myHolder.rv_fl.setVisibility(View.VISIBLE);
//            myHolder.recyclerView.addItemDecoration(
//                    new DividerItemDecoration(mContext,
//                            DividerItemDecoration.HORIZONTAL_LIST,
//                            OSUtil.dp2px(mContext, 10),
//                            android.R.color.transparent));
            CustomLinearLayoutManager manager = new CustomLinearLayoutManager(mContext);
            myHolder.recyclerView.setLayoutManager(manager);
            CommentsChildAdapter adapter = new CommentsChildAdapter(mContext, cBeans);
            adapter.setClickListener(new CommentsChildAdapter.ClickListener() {
                @Override
                public void onClick(int position) {

                }
            });
            myHolder.recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            myHolder.rv_fl.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.onClick(position);
            }
        });

        ((MyHolder) holder).tv_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.onClickContent(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_time, tv_content;
        ImageView iv_head;
        RecyclerView recyclerView;
        FrameLayout rv_fl;

        public MyHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            iv_head = (ImageView) itemView.findViewById(R.id.iv_head);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.rv);
            rv_fl = (FrameLayout) itemView.findViewById(R.id.rv_fl);
        }
    }
}
