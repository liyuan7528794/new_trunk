package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.bean.CityBean;

import java.util.ArrayList;

/**
 * 商城首页的适配器----第二级广告栏和城市
 * Created by wyp on 2017/1/9.
 */

public class OutCityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<CityBean> listData;
    private int type;// 1.第二级广告栏 2.小城市 3.大城市
    private OnItemClickListener mOnItemClickListener = null;

    public OutCityAdapter(ArrayList<CityBean> listData, int type, Context context) {
        this.listData = listData;
        this.type = type;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (type == 2)
            view = View.inflate(mContext, R.layout.adapter_out_small_city_item, null);
        else if(type == 3)
            view = View.inflate(mContext, R.layout.adapter_out_big_city_item, null);
        else
            view = View.inflate(mContext, R.layout.adapter_cover_item, null);
        final OutHodler outHodler = new OutHodler(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    CityBean cb = listData.get(outHodler.getAdapterPosition());
                    mOnItemClickListener.onItemClick(cb.getId() + "", cb.getCityName());
                }
            }
        });
        return outHodler;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int lr = OSUtil.dp2px(mContext, 10);
        int td = OSUtil.dp2px(mContext, 18);
        holder.itemView.setPadding(0, td, lr, td);
        if (position == 0)
            holder.itemView.setPadding(lr, td, lr, td);
        if (holder instanceof OutHodler) {
            CityBean cityBean = listData.get(position);
            if (type == 2) {// 小城市
                ImageDisplayTools.displayImageRoundCity(cityBean.getImgUrl(), ((OutHodler) holder).iv_out_small_city_img);
                ((OutHodler) holder).tv_out_small_city.setText(cityBean.getCityName());
                ((OutHodler) holder).tv_out_small_city_title.setText(cityBean.getSubhead());
            } else if(type == 3){
                ImageDisplayTools.displayImageRoundCity(cityBean.getImgUrl(), ((OutHodler) holder).iv_out_big_city_img);
                ((OutHodler) holder).tv_out_big_city.setText(cityBean.getCityName());
                ((OutHodler) holder).tv_out_big_content.setText(cityBean.getSubhead());
            } else {
                ImageDisplayTools.displayImageRoundCity(cityBean.getImgUrl(), ((OutHodler) holder).iv_out_big_city_img);
                ((OutHodler) holder).tv_out_big_city.setText(cityBean.getCityName());
            }
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class OutHodler extends RecyclerView.ViewHolder {

        ImageView iv_out_big_city_img, iv_out_small_city_img;
        TextView tv_out_big_city, tv_out_small_city, tv_out_small_city_title, tv_out_big_content;

        public OutHodler(View itemView) {
            super(itemView);
            ImageDisplayTools.initImageLoader(mContext);
            iv_out_big_city_img = (ImageView) itemView.findViewById(R.id.iv_out_big_city_img);
            iv_out_small_city_img = (ImageView) itemView.findViewById(R.id.iv_out_small_city_img);
            tv_out_big_city = (TextView) itemView.findViewById(R.id.tv_out_big_city);
            tv_out_small_city = (TextView) itemView.findViewById(R.id.tv_out_small_city);
            tv_out_small_city_title = (TextView) itemView.findViewById(R.id.tv_out_small_city_title);
            tv_out_big_content = (TextView) itemView.findViewById(R.id.tv_out_big_content);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id, String city);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
