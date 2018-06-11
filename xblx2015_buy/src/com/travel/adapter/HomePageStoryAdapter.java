package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ctsmedia.hltravel.R;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */

public class HomePageStoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<GoodsBasicInfoBean> listData;
    private ClickListener clickListener;

    public interface ClickListener{
        void onClick(GoodsBasicInfoBean bean);
    }

    public HomePageStoryAdapter(Context context, List<GoodsBasicInfoBean> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_home_fragment_story, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder mHolder = (MyViewHolder) holder;
        GoodsBasicInfoBean bean = listData.get(position);
        ImageDisplayTools.disPlayRoundDrawable(bean.getGoodsImg(), mHolder.iv_cover, OSUtil.dp2px(mContext, 2));
        mHolder.tv_title.setText(bean.getGoodsTitle());
        mHolder.tv_content.setText(bean.getSubhead());
        mHolder.tv_lable.setText(bean.getLabel());
        mHolder.tv_lable.setVisibility(View.VISIBLE);
        mHolder.tv_status.setVisibility(View.VISIBLE);
        if(bean.getLabel().isEmpty()){
            mHolder.tv_lable.setVisibility(View.GONE);
        }
        if(TextUtils.equals(bean.getGoodsId() ,"0")){
            mHolder.tv_status.setVisibility(View.GONE);
        }
        mHolder.line.setVisibility(View.VISIBLE);
        if(position == listData.size()-1){
            mHolder.line.setVisibility(View.INVISIBLE);
        }
        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(listData.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv_cover;
        private TextView tv_title, tv_content, tv_status, tv_lable;
        private View line;
        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);
            tv_lable = (TextView) itemView.findViewById(R.id.tv_lable);
            line = itemView.findViewById(R.id.line);
        }
    }
}
