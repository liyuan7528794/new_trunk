package com.travel.shop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.bean.ReasonBean;
import com.travel.shop.R;

import java.util.ArrayList;

/**
 * 订单详情页中选择理由的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/22
 */
public class ReasonAdapter extends MyBaseAdapter<ReasonBean> {

    private Holder mHolder;
    private Context mContext;
    private String tag;

    public ReasonAdapter(Context context, ArrayList<ReasonBean> listData,
                         String tag) {
        super(listData);
        this.mContext = context;
        this.tag = tag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            mHolder = new Holder();
            if ("reason".equals(tag))
                convertView = View.inflate(mContext, R.layout.adapter_reason, null);
            mHolder.iv_choose = (ImageView) convertView.findViewById(R.id.iv_choose);
            mHolder.tv_reason = (TextView) convertView.findViewById(R.id.tv_reason);

            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }

        ReasonBean mReasonBean = (ReasonBean) getItem(position);

        // 选择
        if (mReasonBean.getFlag() == 1) {
            mHolder.iv_choose.setImageResource(R.drawable.icon_use_yes);
            // 未选择
        } else {
            mHolder.iv_choose.setImageResource(R.drawable.icon_use_no);
        }
        // 理由
        mHolder.tv_reason.setText(mReasonBean.getReason());

        return convertView;
    }

    class Holder {
        public ImageView iv_choose;
        public TextView tv_reason;
    }

}
