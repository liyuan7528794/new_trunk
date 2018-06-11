package com.travel.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.bean.CityBean;

import java.util.List;

/**
 * Created by Administrator on 2017/9/11.
 */

public class AdapterCityInfo extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    private Context mContext;
    private List<CityBean> mList;
    private View mHeaderView;
    private OnItemClickListener mListener;
    public AdapterCityInfo (Context context, List<CityBean> list){
        mContext = context;
        mList = list;

    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if(manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_HEADER
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mHeaderView == null)
            return TYPE_NORMAL;
        if(position == 0)
            return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER)
            return new Holder(mHeaderView);
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_city, parent, false);
        return new Holder(layout);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if(getItemViewType(position) == TYPE_HEADER) return;
        final int pos = getRealPosition(viewHolder);

        int h = OSUtil.dp2px(mContext, 15);
        int c = OSUtil.dp2px(mContext, 15)/2;
        viewHolder.itemView.setPadding(c, c, c, c);

        if (pos == 0 || pos == 1) {
//            viewHolder.itemView.setPadding(c, 0, c, c);
        }


        final CityBean bean = mList.get(pos);
            ((Holder) viewHolder).cityName.setText("-"+bean.getCityName()+"-");
            ((Holder) viewHolder).title.setText(bean.getSubhead());
        ImageDisplayTools.displayImage(bean.getImgUrl(), ((Holder) viewHolder).cover);
            if(mListener == null) return;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(pos, bean);
                }
            });
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? mList.size() : mList.size() + 1;
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView cityName;
        TextView title;
        ImageView cover;
        public Holder(View itemView) {
            super(itemView);
            if(itemView == mHeaderView) return;
            cityName = (TextView) itemView.findViewById(R.id.tv_cityname);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            cover = (ImageView) itemView.findViewById(R.id.iv_cover);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position, CityBean data);
    }

}
