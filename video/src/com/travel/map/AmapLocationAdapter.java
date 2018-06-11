package com.travel.map;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/8/14.
 */

public class AmapLocationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<PoiItem> list;
    private OnItemListener onItemListener;

    public AmapLocationAdapter(Context context, List<PoiItem> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemListener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_amap_location, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        /*int outMargin = OSUtil.dp2px(context, 15);
        int inMargin = OSUtil.dp2px(context, 5.5f);
        if (position % 2 == 0) {
            if (position == 0)
                ((MyViewHolder) holder).itemView.setPadding(0, outMargin, inMargin, 0);
            else if (position == list.size() - 2 || position == list.size() - 1)
                ((MyViewHolder) holder).itemView.setPadding(0, 0, inMargin, outMargin);
            else
                ((MyViewHolder) holder).itemView.setPadding(0, 0, inMargin, 0);
        } else {
            if (position == 1)
                ((MyViewHolder) holder).itemView.setPadding(inMargin, outMargin, 0, 0);
            else if (position == list.size() - 1)
                ((MyViewHolder) holder).itemView.setPadding(inMargin, 0, 0, outMargin);
            else
                ((MyViewHolder) holder).itemView.setPadding(inMargin, 0, 0, 0);
        }*/

        PoiItem poiItem = list.get(position);
        String adName = poiItem.getAdName();
        String businessArea = poiItem.getBusinessArea();
        String cityName = poiItem.getCityName();
        String direction = poiItem.getDirection();
        String provinceName = poiItem.getProvinceName();
        String title= poiItem.getTitle();
        String city = poiItem.getCityName() + poiItem.getAdName();
        String addres = adName + businessArea;
        ((MyViewHolder) holder).tv_city.setText(title);
        ((MyViewHolder) holder).tv_address.setText(addres);


        ((MyViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_city, tv_address;

        public MyViewHolder(View view) {
            super(view);
            tv_city = (TextView) view.findViewById(R.id.city);
            tv_address = (TextView) view.findViewById(R.id.address);

        }
    }
}