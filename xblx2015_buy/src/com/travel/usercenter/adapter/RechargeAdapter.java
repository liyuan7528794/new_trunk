package com.travel.usercenter.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.adapter.MyBaseAdapter;
import com.travel.usercenter.entity.RechargeBean;

import java.util.ArrayList;

/**
 * 充值的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2016/06/15
 */
public class RechargeAdapter extends MyBaseAdapter<RechargeBean> {

    private Context mContext;

    public RechargeAdapter(Context mContext, ArrayList<RechargeBean> listData) {
        super(listData);
        this.mContext = mContext;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder mHolder = null;
        if (convertView == null) {
            mHolder = new Holder();
            convertView = View.inflate(mContext, R.layout.adapter_recharge, null);
            mHolder.tv_redmoney = (TextView) convertView.findViewById(R.id.tv_redmoney);
            mHolder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }
        convertView.setLayoutParams(
                new AbsListView.LayoutParams(((OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 60)) / 3),
                        ((OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 60)) / 6)));
        RechargeBean detail = (RechargeBean) getItem(position);
        // 已选择
        if (detail.isChoosed()) {
            convertView.setBackgroundResource(R.drawable.bg_money_choosed);
            mHolder.tv_redmoney.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));
            mHolder.tv_money.setTextColor(ContextCompat.getColor(mContext, R.color.red_FF7A89));
        } else {
            convertView.setBackgroundResource(R.drawable.bg_money_unchoose);
            mHolder.tv_redmoney.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
            mHolder.tv_money.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
        }
        // 红币
        mHolder.tv_redmoney.setText(detail.getRedMoney() + "红币");
        // 人民币
        mHolder.tv_money.setText(detail.getMoney() + "元");

        if (position == getCount() - 1) {
            if (detail.isChoosed() && !TextUtils.isEmpty(detail.getRedMoney())) {
                // 红币
                mHolder.tv_redmoney.setText(detail.getRedMoney() + "红币");
                // 人民币
                mHolder.tv_money.setText(detail.getMoney() + "元");
            } else {
                // 红币
                mHolder.tv_redmoney.setText("输入金额");
                // 人民币
                mHolder.tv_money.setText("0元");
            }
        }
        return convertView;
    }

    class Holder {
        TextView tv_redmoney, tv_money;
    }

}
