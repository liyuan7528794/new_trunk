package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ctsmedia.hltravel.R;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */

public class HomePageVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<VideoInfoBean> listData;
    private ClickListener clickListener;

    public interface ClickListener{
        void onClick(VideoInfoBean bean);
        void onClickMore();
    }

    public HomePageVideoAdapter(Context context, List<VideoInfoBean> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_home_fragment_video, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder mHolder = (MyViewHolder) holder;
        if(position == 5) {
            mHolder.layout_more.setVisibility(View.VISIBLE);
            mHolder.layout_video.setVisibility(View.GONE);
            mHolder.tv_title.setText("");
            mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClickMore();
                }
            });
        }else{
            mHolder.layout_more.setVisibility(View.GONE);
            mHolder.layout_video.setVisibility(View.VISIBLE);
            VideoInfoBean bean = listData.get(position);
            ImageDisplayTools.disPlayRoundDrawable(bean.getVideoImg(), mHolder.iv_cover, OSUtil.dp2px(mContext, 2));
            mHolder.tv_title.setText(bean.getVideoTitle());
            mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(listData.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(listData.size() <= 5) {
            return listData.size();
        }else {
            return 6;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv_cover, iv_mark;
        private TextView tv_title;
        private FrameLayout layout_video;
        private LinearLayout layout_more;
        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cover = (ImageView) itemView.findViewById(R.id.iv_cover);
            iv_mark = (ImageView) itemView.findViewById(R.id.iv_mark);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            layout_more = (LinearLayout) itemView.findViewById(R.id.layout_more);
            layout_video = (FrameLayout) itemView.findViewById(R.id.layout_video);
        }
    }
}
