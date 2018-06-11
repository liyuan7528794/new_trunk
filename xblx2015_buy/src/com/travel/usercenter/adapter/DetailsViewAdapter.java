package com.travel.usercenter.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.shop.adapter.MyBaseAdapter;
import com.travel.usercenter.entity.DetailsViewBean;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 查看明细的适配器
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/14
 * 
 */
public class DetailsViewAdapter extends MyBaseAdapter<DetailsViewBean> {

	private Context mContext;

	public DetailsViewAdapter(Context mContext, ArrayList<DetailsViewBean> listData) {
		super(listData);
		this.mContext = mContext;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		DetailsViewBean data = (DetailsViewBean) getItem(position);
		if (data.getType() == 0)
			return 0;
		else
			return 1;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder mHolder = new Holder();
		// 日期
		if (getItemViewType(position) == 0)
			if (convertView == null) {
				convertView = View.inflate(mContext, R.layout.adapter_detailsview_month, null);
				mHolder.tv_month = (TextView) convertView.findViewById(R.id.tv_month);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
		// 该月下的明细
		else {
			if (convertView == null) {
				convertView = View.inflate(mContext, R.layout.adapter_detailsview, null);
				mHolder.tv_detail_type = (TextView) convertView.findViewById(R.id.tv_detail_type);
				mHolder.tv_detail_time = (TextView) convertView.findViewById(R.id.tv_money_detail_time);
				mHolder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
				mHolder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
		}
		DetailsViewBean detail = (DetailsViewBean) getItem(position);
		if (getItemViewType(position) == 0) {
			// 日期
			mHolder.tv_month.setText(detail.getMonth());
		} else {
			// 红币：明细描述 人民币：明细类型+明细描述
			mHolder.tv_detail_type.setText((TextUtils.equals(detail.getStatus(), "0") ? ""
					: getTypeName(Integer.parseInt(detail.getDetailsType()))) + detail.getDesc());
			// 时间
			mHolder.tv_detail_time.setText(detail.getTime());
			// 状态 红币不显示，人民币([changeType][1]支付和[2]提现的“1”[status]状态)显示
			if (Arrays.asList(new String[] { "0", "2" }).contains(detail.getStatus())
					|| TextUtils.equals(detail.getDetailsType(), "3"))
				mHolder.tv_status.setVisibility(View.GONE);
			else {
				mHolder.tv_status.setVisibility(View.VISIBLE);
				mHolder.tv_status.setText(getStatusName(Integer.parseInt(detail.getDetailsType())));
			}
			// 钱数
			if (!TextUtils.isEmpty(detail.getMoney()))
				// 红币
				if (TextUtils.equals(detail.getStatus(), "0"))
					// 出项 → 绿色
					if (Arrays.asList(new String[] { "2", "3", "5" }).contains(detail.getDetailsType())) {
						mHolder.tv_money.setTextColor(ContextCompat.getColor(mContext,R.color.green_21C700));
						mHolder.tv_money.setText("-" + detail.getMoney());
						// 进项 → 红色
					} else {
						mHolder.tv_money.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));
						mHolder.tv_money.setText("+" + detail.getMoney());
					}
				else {// 人民币
						// 进项 → 红色
					if (TextUtils.equals(detail.getDetailsType(), "1")) {
						mHolder.tv_money.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));
						mHolder.tv_money.setText("+" + detail.getMoney());
						// 出项 → 绿色
					} else {
						mHolder.tv_money.setTextColor(ContextCompat.getColor(mContext,R.color.green_21C700));
						mHolder.tv_money.setText("-" + detail.getMoney());
					}
				}
		}
		return convertView;
	}

	class Holder {
		TextView tv_month, tv_detail_type, tv_detail_time, tv_money, tv_status;
	}

	/**
	 * 获取类型的名字
	 * 
	 * @param detailsType
	 *            1支付 2提现 3退款
	 */
	private String getTypeName(int detailsType) {
		String name = "";
		switch (detailsType) {
		case 1:
			name = "支付 - ";
			break;
		case 2:
			name = "提现 - ";
			break;
		case 3:
			name = "退款 - ";
			break;
		default:
			break;
		}
		return name;
	}

	/**
	 * 获取状态内容
	 * 
	 * @return
	 */
	private String getStatusName(int changeType) {
		String result = "";
		switch (changeType) {
		case 1:// 支付
			result = "待确认付款";
			break;
		case 2:// 提现
			result = "提现中";
			break;
		}
		return result;
	}

}
