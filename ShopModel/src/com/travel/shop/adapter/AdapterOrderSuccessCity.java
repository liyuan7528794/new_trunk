package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.bean.CityBean;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;

import java.util.List;

/**
 * Created by Administrator on 2017/11/3.
 */
public class AdapterOrderSuccessCity extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    private Context mContext;
    private List<CityBean> mList;
    private OnItemClickListener mListener;
    public AdapterOrderSuccessCity (Context context, List<CityBean> list){
        mContext = context;
        mList = list;

    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_order_success_city, parent, false);
        return new Holder(layout);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        int h = OSUtil.dp2px(mContext, 15);
        int c = h/2;
//        viewHolder.itemView.setPadding(c, c, c, c);
        if(position%2 == 1){
            viewHolder.itemView.setPadding(c, c, h, c);
        } else {
            viewHolder.itemView.setPadding(h, c, c, c);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ((Holder) viewHolder).cover.getLayoutParams();
        params.width = (OSUtil.getScreenWidth() - h*3)/2;
        params.height = params.width;
        ((Holder) viewHolder).cover.setLayoutParams(params);
        final CityBean bean = mList.get(position);
        ((Holder) viewHolder).cityName.setText(bean.getCityName());
        ((Holder) viewHolder).title.setText(bean.getSubhead());
        ImageDisplayTools.displayImage(bean.getImgUrl(), ((Holder) viewHolder).cover);
        if(mListener == null) return;
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(position, bean);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView cityName;
        TextView title;
        ImageView cover;
        public Holder(View itemView) {
            super(itemView);
            cityName = (TextView) itemView.findViewById(R.id.tv_cityname);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            cover = (ImageView) itemView.findViewById(R.id.iv_cover);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position, CityBean data);
    }

}