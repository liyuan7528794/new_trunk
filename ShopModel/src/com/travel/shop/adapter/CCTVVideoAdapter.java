package com.travel.shop.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.shop.R;
import com.travel.shop.activity.CCTVVideoInfoActivity;
import com.travel.shop.activity.CCTVVideoLableActivity;
import com.travel.shop.bean.CCTVVideoBean;
import com.travel.shop.widget.MyRecyclerView;

import java.util.ArrayList;

/**
 * cctv视频里的适配器
 * Created by wyp on 2017/12/01.
 */

public class CCTVVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<CCTVVideoBean> listData;
    private CCTVVideoBean mCCTVVideoBean;

    public CCTVVideoAdapter(ArrayList<CCTVVideoBean> listData, Context context) {
        this.listData = listData;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_cctv_video_item, parent, false);
        return new CCTVVideoHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder,  int position) {
        if (holder instanceof CCTVVideoHolder) {
            mCCTVVideoBean = listData.get(position);
            // 标签
            ((CCTVVideoHolder) holder).cctv_label.setText("CCTV小城" + mCCTVVideoBean.getLabel());
            // 内容
            ((CCTVVideoHolder) holder).mrv_cctv_content.setTag(position);
            ((CCTVVideoHolder) holder).mrv_cctv_content.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            CCTVVideoContentAdapter adapter = new CCTVVideoContentAdapter(mContext, mCCTVVideoBean.getContents());
            ((CCTVVideoHolder) holder).mrv_cctv_content.setAdapter(adapter);
            adapter.setmOnItemClickListener(new CCTVVideoContentAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int pos) {
                    int position = Integer.parseInt(((CCTVVideoHolder) holder).mrv_cctv_content.getTag().toString());
                    mCCTVVideoBean = listData.get(position);
                    if (pos < 5)
                        CCTVVideoInfoActivity.actionStart(mContext, mCCTVVideoBean.getContents().get(pos), 2);
                    else
                        CCTVVideoLableActivity.actionStart(mContext, "CCTV小城" + mCCTVVideoBean.getLabel(), mCCTVVideoBean.getType());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class CCTVVideoHolder extends RecyclerView.ViewHolder {

        TextView cctv_label;
        MyRecyclerView mrv_cctv_content;
        View v_line_cctv;

        public CCTVVideoHolder(View itemView) {
            super(itemView);
            cctv_label = (TextView) itemView.findViewById(R.id.cctv_label);
            mrv_cctv_content = (MyRecyclerView) itemView.findViewById(R.id.mrv_cctv_content);
            v_line_cctv = itemView.findViewById(R.id.v_line_cctv);
        }
    }
}
