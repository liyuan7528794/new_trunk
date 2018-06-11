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
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * 直播收入-→我的红币
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/13
 * 
 */
public class LiveIncomeActivity extends TitleBarBaseActivity implements OnClickListener {

	private Context mContext;

	private LinearLayout ll_liveincome_data;
	private TextView tv_redmoney, tv_recharge, tv_withdraw_cash, tv_question;

	private MReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_income);

		init();

		// 有网
		if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
			getIncome();
		}

		rightButton.setOnClickListener(this);
		tv_recharge.setOnClickListener(this);
		tv_withdraw_cash.setOnClickListener(this);
		tv_question.setOnClickListener(this);
	}

	/**
	 * 控件以及数据的初始化
	 */
	private void init() {
		mContext = this;

		ll_liveincome_data = findView(R.id.ll_liveincome_data);
		tv_redmoney = findView(R.id.tv_redmoney);
		tv_recharge = findView(R.id.tv_recharge);
		tv_withdraw_cash = findView(R.id.tv_withdraw_cash);
		tv_question = findView(R.id.tv_question_live_income);

		setTitle(getString(R.string.usercenter_live_income));
		rightButton.setText(getString(R.string.liveincome_details));
		rightButton.setTextSize(15);
		OSUtil.setShareParam(rightButton, "", mContext);

		receiver = new MReceiver();
		registerReceiver(receiver, new IntentFilter(Constants.REDMONEY_ALERT));

		rightButton.setVisibility(View.GONE);
		ll_liveincome_data.setVisibility(View.GONE);
	}

	/**
	 * 收到改变红币数的广播
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
		unregisterReceiver(receiver);
	}

	/**
	 * 获取收入
	 */
	private void getIncome() {
		NetWorkUtil.postForm(mContext, ShopConstant.LIVE_INCOME, new MResponseListener(mContext) {

			@Override
			protected void onDataFine(JSONObject data) {
				tv_redmoney.setText(data.optString("myRedcoinTotal"));
				rightButton.setVisibility(View.VISIBLE);
				ll_liveincome_data.setVisibility(View.VISIBLE);
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
			intent = new Intent(mContext, LiveDetailsViewActivity.class);
			intent.putExtra("details", "live");
			// 充值
		} else if (v == tv_recharge) {
			intent = new Intent(mContext, RechargeActivity.class);
			intent.putExtra("withdraw_money", tv_redmoney.getText().toString());
			// 提现
		} else if (v == tv_withdraw_cash) {
			intent = new Intent(mContext, WithDrawCashActivity.class);
			intent.putExtra("withdraw_money", tv_redmoney.getText().toString());
			// 常见问题
		} else if (v == tv_question) {
			intent = new Intent(mContext, MoreFragmentActivity.class);
			intent.putExtra("type", getString(R.string.liveincome_question));
			intent.putExtra("tag", "live_income");
		}
		startActivity(intent);
	}
}
