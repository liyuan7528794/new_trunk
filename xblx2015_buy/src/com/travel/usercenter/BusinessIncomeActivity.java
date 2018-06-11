package com.travel.usercenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * 电商收入
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/15
 * 
 */
public class BusinessIncomeActivity extends TitleBarBaseActivity implements OnClickListener {

	private Context mContext;

	private LinearLayout ll_businessincome_data;
	private TextView tv_money, tv_can_withdraw, tv_this_month_count, tv_withdraw_applicatin, tv_question;

	private MReceiver recceiver;
	private double canWithdrawCash;
	private String desc;
	private int times;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_business_income);

		init();

		// 有网
		if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
			getIncome();
		}

		rightButton.setOnClickListener(this);
		tv_withdraw_applicatin.setOnClickListener(this);
		tv_question.setOnClickListener(this);
	}

	/**
	 * 控件以及数据的初始化
	 */
	private void init() {
		mContext = this;

		ll_businessincome_data = findView(R.id.ll_businessincome_data);
		tv_money = findView(R.id.tv_money);
		tv_can_withdraw = findView(R.id.tv_can_withdraw);
		tv_this_month_count = findView(R.id.tv_this_month_count);
		tv_withdraw_applicatin = findView(R.id.tv_withdraw_applicatin);
		tv_question = findView(R.id.tv_question_business_income);

		setTitle(getString(R.string.usercenter_business_income));
		rightButton.setText(getString(R.string.liveincome_details));
		rightButton.setTextSize(15);
		OSUtil.setShareParam(rightButton, "", mContext);

		recceiver = new MReceiver();
		registerReceiver(recceiver, new IntentFilter(ShopConstant.MONEY_ALERT));

		rightButton.setVisibility(View.GONE);
		ll_businessincome_data.setVisibility(View.GONE);
	}

	/**
	 * 接收到修改人民币数的广播
	 *
	 */
	class MReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			getIncome();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(recceiver);
	}

	/**
	 * 获取收入
	 */
	private void getIncome() {
		NetWorkUtil.postForm(mContext, ShopConstant.BUSINESS_INCOME, new MResponseListener(mContext) {

			@Override
			protected void onDataFine(JSONObject data) {
				canWithdrawCash = data.optDouble("can");
				tv_money.setText(data.optDouble("total") + "");
				tv_can_withdraw.setText(canWithdrawCash + "元");
				times = data.optInt("maxAllowTimes") - data.optInt("thisMonthTimes");
				tv_this_month_count.setText(times + "次");
				desc = data.optString("remark");

				rightButton.setVisibility(View.VISIBLE);
				ll_businessincome_data.setVisibility(View.VISIBLE);
			}
		}, new HashMap<String, Object>());
	}

	@Override
	public void onClick(final View v) {
		// 禁止双击
		v.setEnabled(false);
		v.postDelayed(new Runnable() {

			@Override
			public void run() {
				v.setEnabled(true);
			}
		}, 500);
		Intent intent = null;
		// 明细
		if (v == rightButton) {
			intent = new Intent(mContext, BusinessDetailsViewActivity.class);
			intent.putExtra("details", "business");
			// 提现申请
		} else if (v == tv_withdraw_applicatin) {
			intent = new Intent(mContext, WithDrawApplicationActivity.class);
			intent.putExtra("withdraw_money", canWithdrawCash + "");
			intent.putExtra("desc", desc);
			intent.putExtra("times", times);
			// 常见问题
		} else if (v == tv_question) {
			intent = new Intent(mContext, MoreFragmentActivity.class);
			intent.putExtra("type", getString(R.string.liveincome_question));
			intent.putExtra("tag", "business_income");
		}
		startActivity(intent);
	}
}
