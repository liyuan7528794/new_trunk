package com.travel.usercenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.layout.CornerDialog;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.widget.PayMethodPopWindow;
import com.travel.usercenter.adapter.RechargeAdapter;
import com.travel.usercenter.entity.RechargeBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 充值
 *
 * @author WYP
 * @version 1.0
 * @created 2016/06/13
 */
public class RechargeActivity extends TitleBarBaseActivity {

    private Context mContext;

    private LinearLayout ll_recharge;
    private TextView tv_redmoney_balance, tv_recharge;

    // 选择充值的面值相关
    private GridView gv_face_value;
    private RechargeAdapter mAdapter;
    private ArrayList<RechargeBean> valueList;

    // 输入金额相关
    private EditText et_money_input;
    private CornerDialog mCornerDialog;

    // 网络相关
    private HashMap<String, Object> map;
    private String selected_money, selected_redmoney, selected_goodsid;

    private MReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        init();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getRechargeData();
        }

        gv_face_value.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == valueList.size() - 1) {
                    getMoneyDialog(position);
                } else {
                    selected_goodsid = valueList.get(position).getGoodsId();
                    selected_redmoney = valueList.get(position).getRedMoney();
                    selected_money = valueList.get(position).getMoney();
                }
                for (int i = 0; i < valueList.size(); i++) {
                    if (position == i)
                        valueList.get(i).setChoosed(true);
                    else
                        valueList.get(i).setChoosed(false);
                }
                mAdapter.notifyDataSetChanged();
            }

        });
        tv_recharge.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (valueList.size() > 0)
                    if (!valueList.get(valueList.size() - 1).isChoosed()
                            || !TextUtils.isEmpty(valueList.get(valueList.size() - 1).getRedMoney()))
                        getOrdersId();
                    else
                        showToast("请输入充值金额");
            }

        });
    }

    /**
     * 控件以及数据初始化
     */
    private void init() {
        mContext = this;

        ll_recharge = findView(R.id.ll_recharge);
        tv_redmoney_balance = findView(R.id.tv_redmoney_balance);
        tv_recharge = findView(R.id.tv_recharge);
        gv_face_value = findView(R.id.gv_face_value);

        setTitle(getString(R.string.recharge_red_recharge));
        tv_redmoney_balance.setText(getIntent().getStringExtra("withdraw_money"));
        valueList = new ArrayList<RechargeBean>();
        mAdapter = new RechargeAdapter(mContext, valueList);
        gv_face_value.setAdapter(mAdapter);

        ll_recharge.setVisibility(View.GONE);
        receiver = new MReceiver();
        registerReceiver(receiver, new IntentFilter(Constants.FINISH_RECHARGE));
    }

    /**
     * 接受到finish当前页面的广播
     */
    class MReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 获取面值
     */
    private void getRechargeData() {
        map = new HashMap<>();
        NetWorkUtil.postForm(mContext, ShopConstant.LIVE_INCOME_RECHARGE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                ll_recharge.setVisibility(View.VISIBLE);
                try {
                    valueList.clear();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        RechargeBean rechargeBean = new RechargeBean();
                        // 商品id
                        rechargeBean.setGoodsId(dataObject.optString("id"));
                        // 红币数
                        rechargeBean.setRedMoney(dataObject.optString("redCoinNum"));
                        // 人民币数
                        rechargeBean.setMoney(dataObject.optString("rmbNum"));
                        // 兑换利率
                        rechargeBean.setExchangeRatio(dataObject.optString("exchangeRatio"));
                        // tag 默认选择第一条
                        if (i == 0)
                            rechargeBean.setChoosed(true);
                        else
                            rechargeBean.setChoosed(false);
                        valueList.add(rechargeBean);
                    }
                    selected_goodsid = valueList.get(0).getGoodsId();
                    selected_redmoney = valueList.get(0).getRedMoney();
                    selected_money = valueList.get(0).getMoney();
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    mAdapter.notifyDataSetChanged();
                }

            }
        }, map);

    }

    /**
     * 获取订单id
     */
    private void getOrdersId() {
        map = new HashMap<>();
        map.put("goodsid", selected_goodsid);
        map.put("rmbNum", selected_money);
        map.put("redCoinNum", selected_redmoney);
        NetWorkUtil.postForm(mContext, ShopConstant.LIVE_INCOME_ORDERSID, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                if (response.optInt("error") == 0) {
                    long ordersId = Long.parseLong(response.optString("data"));
                    new PayMethodPopWindow(mContext, "充值" + selected_redmoney + "金币", RechargeActivity.this, ordersId,
                            selected_money, "redmoney", 2, 2);
                }
            }
        }, map);
    }

    /**
     * 弹出输入金额的dialog
     */
    private void getMoneyDialog(int position) {
        selected_money = "";
        selected_redmoney = "";
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_service_sure, null);

        ImageView iv_sure_cancle = (ImageView) view.findViewById(R.id.iv_sure_cancle);
        et_money_input = (EditText) view.findViewById(R.id.et_service_code);
        TextView tv_sure_service = (TextView) view.findViewById(R.id.tv_sure_service);
        et_money_input.setHint("请输入要充值的红币数");
        et_money_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                selected_redmoney = s.toString();
                if (!TextUtils.isEmpty(selected_redmoney)) {
                    if (selected_redmoney.length() > 6) {
                        // if (Float.parseFloat(selected_redmoney) >
                        // VideoConstant.MAX_RED_MONEY) {
                        selected_redmoney = ShopConstant.MAX_RED_MONEY + "";
                        et_money_input.setText(selected_redmoney);
                    }
                    selected_money = Float.parseFloat(selected_redmoney)
                            * Float.parseFloat(valueList.get(0).getExchangeRatio()) / 100.0 + "";
                }
            }
        });
        selected_goodsid = valueList.get(position).getGoodsId();
        mCornerDialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 90),
                LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);
        iv_sure_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCornerDialog.dismiss();
                RechargeBean rb = new RechargeBean();
                rb.setChoosed(true);
                rb.setRedMoney("");
                valueList.set(valueList.size() - 1, rb);
                mAdapter.notifyDataSetChanged();
            }
        });
        tv_sure_service.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCornerDialog.dismiss();
                RechargeBean rb = new RechargeBean();
                rb.setChoosed(true);
                rb.setGoodsId(selected_goodsid);
                if (!TextUtils.isEmpty(selected_redmoney)) {
                    rb.setMoney(selected_money);
                    rb.setRedMoney(selected_redmoney);
                } else {
                    rb.setRedMoney("");
                }
                valueList.set(valueList.size() - 1, rb);
                mAdapter.notifyDataSetChanged();
            }
        });
        mCornerDialog.show();
    }

}
