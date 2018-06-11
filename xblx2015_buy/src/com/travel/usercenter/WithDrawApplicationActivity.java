package com.travel.usercenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.travel.layout.DialogTemplet;
import com.travel.layout.DialogTemplet.DialogConfirmButtonListener;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * 提现申请
 *
 * @author WYP
 * @version 1.0
 * @created 2016/06/13
 */
public class WithDrawApplicationActivity extends TitleBarBaseActivity {

    private Context mContext;
    private LinearLayout ll_withdraw_application_focus;
    private TextView tv_money_total, tv_commit_application, tv_desc;
    private EditText et_withdraw_value;

    // 提现金额相关
    private String money;
    private String withdrawCash;
    private DialogTemplet mDialog;
    private int times;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_application);

        init();

        // 要提现的金额
        et_withdraw_value.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                withdrawCash = s.toString();
                counter = 0;
                if (countStr(withdrawCash, ".") > 1 || withdrawCash.indexOf(".") == 0) {
                    showToast("请输入合法的金额");
                    et_withdraw_value.setText("");
                } else if (!TextUtils.isEmpty(withdrawCash) && Float.parseFloat(withdrawCash) > Float.parseFloat(money)) {
                    // 提示：输入总数超出已有的人民币数
                    mDialog = new DialogTemplet(mContext, true, "当前可提现人民币数：" + money + "元", getString(R.string.sure),
                            "", "");
                    mDialog.setConfirmClick(new DialogConfirmButtonListener() {

                        @Override
                        public void confirmClick(View view) {
                            mDialog.dismiss();
                        }
                    });
                    mDialog.show();
                    withdrawCash = money;
                    et_withdraw_value.setText(withdrawCash);
                }

            }
        });

        tv_commit_application.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(withdrawCash))
                    showToast(R.string.withdraw_cash_input);
                else if (times == 0)
                    showToast("本月提现次数为0，请下个月再提取！");
                else if (Float.parseFloat(withdrawCash) < Constants.MAX_MONEY)
                    showToast("每次提现不能少于" + Constants.MAX_MONEY + "元");
                else {
                    commitApplication();
                }
            }
        });

        ll_withdraw_application_focus.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                OSUtil.hideKeyboard(WithDrawApplicationActivity.this);
                return false;
            }
        });
    }

    /**
     * 控件以及数据的初始化
     */
    private void init() {
        mContext = this;

        ll_withdraw_application_focus = findView(R.id.ll_withdraw_application_focus);
        tv_money_total = findView(R.id.tv_money_total);
        tv_commit_application = findView(R.id.tv_commit_application);
        tv_desc = findView(R.id.tv_desc);
        et_withdraw_value = findView(R.id.et_withdraw_value);

        setTitle(getString(R.string.businessincome_withdraw_application));
        money = getIntent().getStringExtra("withdraw_money");
        tv_money_total.setText(money);
        tv_desc.setText(getIntent().getStringExtra("desc"));
        times = getIntent().getIntExtra("times", 0);
    }

    /**
     * 提交申请
     */
    private void commitApplication() {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("applyNum", withdrawCash);
        NetWorkUtil.postForm(mContext, ShopConstant.BUSINESS_INCOME_WITHDRAW_CASH, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    // 发广播通知修改人民币数
                    sendBroadcast(new Intent(ShopConstant.MONEY_ALERT));
                    showToast(R.string.withdrawcash_application_success);
                    finish();
                }
            }
        }, map);
    }

    /**
     * 判断输入的金额中包含“.”的个数
     *
     * @param str1
     * @param str2
     * @return counter
     */
    private int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) + 1), str2);
            return counter;
        }
    }

}
