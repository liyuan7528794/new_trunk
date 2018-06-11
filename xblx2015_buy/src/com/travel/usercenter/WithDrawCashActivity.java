package com.travel.usercenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.layout.CornerDialog;
import com.travel.layout.DialogTemplet;
import com.travel.layout.DialogTemplet.DialogConfirmButtonListener;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.tools.ShopTool;
import com.travel.usercenter.entity.WithdrawCashBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 提现
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/06/14
 * 
 */
public class WithDrawCashActivity extends TitleBarBaseActivity implements OnClickListener {

	private Context mContext;
	private LinearLayout ll_withdraw_cash_info;

	private TextView tv_redmoney_total, tv_exchange_money, tv_immediately_withdraw, tv_question_withdraw_cash;
	private EditText et_redmoney_exchange, et_alipay_account, et_name;

	// 兑换相关
	private String redmoneyCount;// 总的红币数
	private String exchange;// 要兑换的红币数
	private float exchangeMoney;// 兑换的人民币数
	private float poundage;// 手续费
	private float exchangeCan;// 能兑换的人民币数
	private DialogTemplet mDialog;

	// 网络获取相关
	private HashMap<String, Object> map;
	private ArrayList<WithdrawCashBean> rations;
	private CornerDialog mCornerDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_withdraw_cash);

		init();

		// 有网
		if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
			getRation();
		}

		// 要兑换的金额
		et_redmoney_exchange.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				exchange = s.toString();
				// 兑换币多余10并且小于总额时可兑换
				if (!TextUtils.isEmpty(exchange) && Float.parseFloat(exchange) >= 10) {
					if (Float.parseFloat(exchange) > Float.parseFloat(redmoneyCount)) {
						// 提示：输入总数超出已有的红币数
						mDialog = new DialogTemplet(mContext, true, "当前可兑换红币数：" + redmoneyCount + "枚",
								getString(R.string.sure), "", "");
						mDialog.setConfirmClick(new DialogConfirmButtonListener() {

							@Override
							public void confirmClick(View view) {
								mDialog.dismiss();
							}
						});
						mDialog.show();
						exchange = redmoneyCount;
						et_redmoney_exchange.setText(exchange);
					}

					getExchangeData();
				}

			}
		});
		tv_immediately_withdraw.setOnClickListener(this);
		tv_question_withdraw_cash.setOnClickListener(this);

		ll_withdraw_cash_info.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				OSUtil.hideKeyboard(WithDrawCashActivity.this);
				return false;
			}
		});
	}

	/**
	 * 控件以及数据初始化
	 */
	private void init() {
		mContext = this;

		tv_redmoney_total = findView(R.id.tv_redmoney_total);
		tv_exchange_money = findView(R.id.tv_exchange_money);
		tv_immediately_withdraw = findView(R.id.tv_immediately_withdraw);
		tv_question_withdraw_cash = findView(R.id.tv_question_withdraw_cash);
		et_redmoney_exchange = findView(R.id.et_redmoney_exchange);
		et_alipay_account = findView(R.id.et_alipay_account);
		et_name = findView(R.id.et_name);
		ll_withdraw_cash_info = findView(R.id.ll_withdraw_cash_info);

		setTitle(getString(R.string.liveincome_withdraw_cash));
		redmoneyCount = getIntent().getStringExtra("withdraw_money");
		rations = new ArrayList<WithdrawCashBean>();

		ll_withdraw_cash_info.setVisibility(View.GONE);
	}

	/**
	 * 获取平台提成比例
	 */
	private void getRation() {
		map = new HashMap<>();
		NetWorkUtil.postForm(mContext, ShopConstant.LIVE_INCOME_RATION, new MResponseListener(mContext) {

			@Override
			protected void onDataFine(JSONObject data) {
				tv_redmoney_total.setText(redmoneyCount);
				int exchangeRatio = data.optInt("ExchangeRatio");
				try {
					JSONArray deductRatioArray = data.getJSONArray("DeductRatio");
					for (int i = 0; i < deductRatioArray.length(); i++) {
						JSONObject deductRatioObject = deductRatioArray.getJSONObject(i);
						WithdrawCashBean ration = new WithdrawCashBean();
						// 提取范围上限
						ration.setUpperLimit(deductRatioObject.optInt("upperLimit"));
						// 提取范围下限
						ration.setLowerLimit(deductRatioObject.optInt("lowerLimit"));
						// 此范围提取比例
						ration.setRatio(Float.parseFloat(deductRatioObject.optString("ratio")));
						// 此范围最小手续费
						ration.setMinNum(deductRatioObject.optInt("minNum"));
						// 兑换比例
						ration.setExchangeRatio(exchangeRatio);
						rations.add(ration);
					}
					ll_withdraw_cash_info.setVisibility(View.VISIBLE);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, map);
	}

	/**
	 * 获取兑换后的人民币
	 */
	private void getExchangeData() {
		// 红币-→人民币
		exchangeMoney = Float.parseFloat(exchange) * rations.get(0).getExchangeRatio() / 100;
		for (int i = 0; i < rations.size(); i++) {
			// 不同区间提取比例不同
			if (exchangeMoney >= rations.get(i).getLowerLimit() && exchangeMoney < rations.get(i).getUpperLimit()) {
				// 提现金额不多于此区间的最小手续费
				if (exchangeMoney * rations.get(i).getRatio() <= rations.get(i).getMinNum())
					poundage = rations.get(i).getMinNum();
				else
					poundage = exchangeMoney * rations.get(i).getRatio();
				exchangeCan = Float.parseFloat(new DecimalFormat("##0.00").format(exchangeMoney - poundage));
				tv_exchange_money.setText(getString(R.string.withdrawcash_can_exchange)
						+ (exchangeCan <= 0 ? 0 : ShopTool.getMoney(exchangeCan + "")) + "元");
				return;
			}
		}
	}

	/**
	 * 提交提现申请
	 */
	private void withDrawCash() {
		map = new HashMap<>();
		map.put("cashedRedCoinNum", exchange);
		map.put("exchangeRate", rations.get(0).getExchangeRatio() / 100.0);
		map.put("rmbNum", exchangeCan);
		map.put("accountType", 1);
		map.put("accountName", et_name.getText().toString());
		map.put("account", et_alipay_account.getText().toString());
		NetWorkUtil.postForm(mContext, ShopConstant.LIVE_INCOME_WITHDRAW_CASH, new MResponseListener(mContext) {

			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				if (response.optInt("error") == 0) {
					// 发广播通知修改红币数
					sendBroadcast(new Intent(Constants.REDMONEY_ALERT));
					showToast(R.string.withdrawcash_application_success);
					finish();
				}
			}
		}, map);
	}

	/**
	 * 二次确认 红币数，兑换人民币数
	 */
	private void sureExchangeCountDialog() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_sure_exchange_count, null);

		TextView tv_redmoney_count = (TextView) view.findViewById(R.id.tv_redmoney_count);
		TextView tv_money_count = (TextView) view.findViewById(R.id.tv_money_count);
		TextView tv_poundage = (TextView) view.findViewById(R.id.tv_poundage);
		TextView tv_can_exchange = (TextView) view.findViewById(R.id.tv_can_exchange);
		TextView tv_cancle_exchange = (TextView) view.findViewById(R.id.tv_cancle_exchange);
		TextView tv_sure_exchange = (TextView) view.findViewById(R.id.tv_sure_exchange);

		// 要兑换的红币数
		tv_redmoney_count.setText("要兑换的红币数：" + exchange + "枚");
		// 兑换的人民币数
		tv_money_count.setText("兑换的人民币数：" + ShopTool.getMoney(exchangeMoney + "") + "元");
		// 手续费
		tv_poundage.setText(
				"手续费：" + ShopTool.getMoney(Float.parseFloat(new DecimalFormat("##0.00").format(poundage)) + "") + "元");
		// 最终能兑换的人民币数
		tv_can_exchange.setText("最终能兑换的人民币数：" + ShopTool.getMoney(exchangeCan + "") + "元");

		mCornerDialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 90),
				LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);
		// 取消
		tv_cancle_exchange.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				mCornerDialog.dismiss();
			}
		});
		// 确定
		tv_sure_exchange.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				withDrawCash();
				mCornerDialog.dismiss();
			}
		});
		mCornerDialog.show();
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
		// 立即提现
		if (v == tv_immediately_withdraw) {
			// 兑换币不能为空
			if (!TextUtils.isEmpty(exchange)) {
				// 红币不足10枚，不可提现
				if (Float.parseFloat(exchange) >= 10) {
					// 支付宝账号不能为空
					if (!TextUtils.isEmpty(et_alipay_account.getText().toString())) {
						// 姓名不能为空
						if (!TextUtils.isEmpty(et_name.getText().toString())) {
							// 红币数不为0时，才能兑换 二次确认 红币数，兑换人民币数
							if (Float.parseFloat(tv_redmoney_total.getText().toString()) != 0)
								sureExchangeCountDialog();
						} else
							showToast(R.string.withdrawcash_name_not_empty);
					} else
						showToast(R.string.withdrawcash_account_not_empty);
				} else
					showToast(R.string.withdrawcash_exchange_input);
			} else
				showToast(R.string.withdrawcash_input_exchange_value);
			// 常见问题
		} else if (v == tv_question_withdraw_cash) {
			Intent intent = new Intent(mContext, MoreFragmentActivity.class);
			intent.putExtra("type", getString(R.string.liveincome_question));
			intent.putExtra("tag", "withdraw_cash");
			startActivity(intent);
		}
	}
}
