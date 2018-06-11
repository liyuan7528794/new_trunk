package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.FormatUtils;
import com.travel.shop.bean.CommentBean;

import java.util.List;

/**
 * Created by Administrator on 2017/12/4.
 */
public class CommentsChildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<CommentBean> listData;
    private ClickListener clickListener;

    public interface ClickListener{
        void onClick(int position);
    }

    public CommentsChildAdapter(Context context, List<CommentBean> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_comments_child, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyHolder myHolder = (MyHolder) holder;
        CommentBean bean = listData.get(position);
        SpannableStringBuilder spBuilder = FormatUtils.StringSetSpanColor(mContext, bean.getUser().getNickName() + ":" + bean.getContent(),
                bean.getUser().getNickName() + ":", R.color.blue_6C92C1);
        if(spBuilder != null){
            myHolder.tv_content.setText(spBuilder);
        }else{
            myHolder.tv_content.setText(bean.getUser().getNickName() + ":" + bean.getContent());
        }

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
        TextView tv_content;
        public MyHolder(View itemView) {
            super(itemView);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
