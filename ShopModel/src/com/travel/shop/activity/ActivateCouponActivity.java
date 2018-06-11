package com.travel.shop.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.layout.DialogTemplet;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.R;
import com.travel.shop.http.ActivateCouponHttp;

import java.util.HashMap;

/**
 * 卡券激活页
 *
 * @author WYP
 * @version 1.0
 * @created 2017/02/06
 */
public class ActivateCouponActivity extends TitleBarBaseActivity implements View.OnClickListener {

    private Context mContext;
    private EditText et_coupon_number, et_coupon_password, et_coupon_phone, et_verification_code;
    private TextView tv_verification_code, tv_activate;

    private HashMap<String, Object> map;
    private String phone;
    private String code;

    private DialogTemplet dialog;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_coupon);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        et_coupon_number = findView(R.id.et_coupon_number);
        et_coupon_password = findView(R.id.et_coupon_password);
        et_coupon_phone = findView(R.id.et_coupon_phone);
        et_verification_code = findView(R.id.et_verification_code);
        tv_verification_code = findView(R.id.tv_verification_code);
        tv_activate = findView(R.id.tv_activate);
        ((LinearLayout) findView(R.id.ll)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OSUtil.hideKeyboard(ActivateCouponActivity.this);
            }
        });
    }

    private void initData() {
        mContext = this;
        setTitle("激活");
        map = new HashMap<>();
    }

    private void initListener() {
        tv_verification_code.setOnClickListener(this);
        tv_activate.setOnClickListener(this);
        et_verification_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                code = s.toString();
                if (code.length() == 6 && verificateInfo())
                    tv_activate.setBackgroundResource(R.drawable.circle5_3);
                else
                    tv_activate.setBackgroundResource(R.drawable.circle5_d);
            }
        });
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
        if (verificateInfo())
            if (v == tv_verification_code) { // 获取验证码
                map.put("account", et_coupon_number.getText().toString());
                map.put("password", et_coupon_password.getText().toString());
                map.put("telephone", phone);
                ActivateCouponHttp.getVerificationCode(this, map, mListener);
            } else if (v == tv_activate) { // 激活
                if (TextUtils.isEmpty(code) || code.length() != 6)
                    showToast("请输入正确的验证码");
                else {
                    map.put("checkCode", code);
                    ActivateCouponHttp.onActivateCoupon(mContext, map, mListener);
                }
            }
    }

    ActivateCouponHttp.ActivateCouponListener mListener = new ActivateCouponHttp.ActivateCouponListener() {
        @Override
        public void sendSuccess() {
            showToast("发送成功");
            startTimeCount();
        }

        @Override
        public void activateCoupon() {
            dialog = new DialogTemplet(mContext, true, "激活成功！", "确定", "", "");
            dialog.show();
            dialog.setConfirmClick(new DialogTemplet.DialogConfirmButtonListener() {
                @Override
                public void confirmClick(View view) {
                    finish();
                }
            });
        }
    };

    /**
     * 验证除验证码以外的信息
     */
    private boolean verificateInfo() {
        boolean result = true;
        phone = et_coupon_phone.getText().toString();
        if (TextUtils.isEmpty(et_coupon_number.getText().toString())) {
            showToast("请输入卡号");
            result = false;
        } else if (TextUtils.isEmpty(et_coupon_password.getText().toString())) {
            showToast("请输入密码");
            result = false;
        } else if (TextUtils.isEmpty(phone)) {
            showToast("请输入手机号");
            result = false;
        } else if (!phone.matches("1[3|4|5|7|8|][0-9]{9}")) {
            showToast("请输入正确的手机号");
            result = false;
        }
        return result;
    }

    protected void startTimeCount() {
        tv_verification_code.setEnabled(false);
        tv_verification_code.setClickable(false);
        tv_verification_code.setTextColor(ContextCompat.getColor(mContext, R.color.red_EC6262));

        new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                isFirst = false;
                tv_verification_code.setText(millisUntilFinished / 1000 + "秒");
            }

            @Override
            public void onFinish() {
                tv_verification_code.setEnabled(true);
                tv_verification_code.setClickable(true);
                tv_verification_code.setTextColor(ContextCompat.getColor(mContext, R.color.black_6));
                if (isFirst) {
                    tv_verification_code.setText("获取验证码");
                } else {
                    tv_verification_code.setText("再次获取");
                }
            }
        }.start();
    }
}
