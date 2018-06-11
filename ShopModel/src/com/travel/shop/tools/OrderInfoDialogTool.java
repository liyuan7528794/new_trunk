package com.travel.shop.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.bean.ReasonBean;
import com.travel.layout.CornerDialog;
import com.travel.layout.DialogTemplet;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.adapter.ReasonAdapter;
import com.travel.shop.http.OrderInfoHttp;

import java.util.ArrayList;

/**
 * 订单详情中弹出的Dialog的方法的类
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/30
 */
@SuppressLint("InflateParams")
public class OrderInfoDialogTool {

    private static CornerDialog mCornerDialog;
    private static OrderInfoDialogTool tool = new OrderInfoDialogTool();
    private static String reason;
    private static Resources rs;
    private static DialogTemplet twiceSureDialog;

    // 拒绝订单
    private static ArrayList<ReasonBean> reasons;
    private static ReasonAdapter mAdapter;

    // 卖家确认退订
    private static String deduct_money;
    private static EditText et_deduct_money;

    // 卖家修改价格
    private static EditText et_price_input;
    private static int counter;
    // 卖家修改备注
    private static EditText et_alter_remark_input;

    /**
     * 显示操作成功的dialog
     *
     * @param tag
     * @param mContext
     */
    public static void successDialog(String tag, Context mContext) {
        rs = mContext.getResources();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_success, null);

        TextView tv_success = (TextView) view.findViewById(R.id.tv_control);
        TextView tv_success_display = (TextView) view.findViewById(R.id.tv_success_display);
        TextView tv_sure = (TextView) view.findViewById(R.id.tv_sure_success);
            // 扫描成功
        if (TextUtils.equals("scan_success", tag)) {
            tv_success_display.setVisibility(View.GONE);
            tv_success.setText("您已成功确认此订单！");
            // 扫描失败
        } else if (TextUtils.equals("scan_fail", tag)) {
            tv_success_display.setVisibility(View.GONE);
            tv_success.setText("此二维码失效或已过期！");
        }

        mCornerDialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 90),
                LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);

        tv_sure.setOnClickListener(new OnClickListener() {

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
                mCornerDialog.dismiss();
            }
        });
        mCornerDialog.show();
    }

    /**
     * 判断输入的金额中包含“.”的个数
     *
     * @param str1
     * @param str2
     * @return counter
     */
    private static int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) + 1), str2);
            return counter;
        }
    }

    /**
     * 线路游商家修改价格
     *
     * @param mContext
     * @param totalPrice
     * @param totalPrice
     */
    public static void alterPrice(final Context mContext, final float totalPrice,
                                  final long ordersId) {
        deduct_money = ShopTool.getMoney(totalPrice + "");

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_alter_price, null);

        TextView tv_alter_total = (TextView) view.findViewById(R.id.tv_alter_total);
        et_price_input = (EditText) view.findViewById(R.id.et_price_input);
        TextView tv_alter_price_cancle = (TextView) view.findViewById(R.id.tv_alter_price_cancle);
        TextView tv_alter_price_sure = (TextView) view.findViewById(R.id.tv_alter_price_sure);
        //        final TextView tv_alter_price_instruction = (TextView) view.findViewById(R.id.tv_alter_price_instruction);

        // 总价
        tv_alter_total.setText(ShopTool.getMoney(totalPrice + ""));
        // 修改价格
        et_price_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                counter = 0;
                deduct_money = s.toString();
                if (countStr(deduct_money, ".") > 1 || deduct_money.indexOf(".") == 0) {
                    TravelUtil.showToast("请输入合法的金额");
                    et_price_input.setText("");
                }

                //                if (("1".equals(salerId) || "21".equals(salerId))
                //                        && !"".equals(deduct_money)) {
                //                    deduct_money = ShopTool.getMoney(deduct_money);
                //                    tv_alter_price_instruction.setVisibility(View.VISIBLE);
                //                    tv_alter_price_instruction.setText("平台优惠：" +
                //                            ShopTool.getMoney(Float.parseFloat(deduct_money) * 0.01F + "") +
                //                            "元，顾客需支付：" +
                //                            ShopTool.getMoney(Float.parseFloat(deduct_money) - Float.parseFloat(deduct_money) * 0.01F + "") +
                //                            "元");
                //                }
            }
        });

        mCornerDialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 90),
                LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);

        tv_alter_price_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCornerDialog.dismiss();
            }
        });

        tv_alter_price_sure.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String alter_money = et_price_input.getText().toString();
                if (!TextUtils.isEmpty(alter_money)) {
                    mCornerDialog.dismiss();
                    OrderInfoHttp.alterPrice(ordersId, mContext,
                            ShopTool.getMoney(deduct_money), new OrderInfoHttp.ControlOrderSuccessListener() {
                                @Override
                                public void onSuccess() {

                                }
                            });
                } else
                    TravelUtil.showToast(R.string.orderinfo_discount_not_empty, mContext);
            }
        });
        mCornerDialog.show();

    }

    /**
     * 线路游商家修改价格
     *
     * @param mContext
     * @param totalPrice
     * @param totalPrice
     */
    public static void alterPrice(final Context mContext, final float totalPrice,
                                  final long ordersId, final OrderInfoHttp.ControlOrderSuccessListener controlOrderSuccessListener) {
        deduct_money = ShopTool.getMoney(totalPrice + "");

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_alter_price, null);

        TextView tv_alter_total = (TextView) view.findViewById(R.id.tv_alter_total);
        et_price_input = (EditText) view.findViewById(R.id.et_price_input);
        TextView tv_alter_price_cancle = (TextView) view.findViewById(R.id.tv_alter_price_cancle);
        TextView tv_alter_price_sure = (TextView) view.findViewById(R.id.tv_alter_price_sure);
        //        final TextView tv_alter_price_instruction = (TextView) view.findViewById(R.id.tv_alter_price_instruction);

        // 总价
        tv_alter_total.setText(ShopTool.getMoney(totalPrice + ""));
        // 修改价格
        et_price_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                counter = 0;
                deduct_money = s.toString();
                if (countStr(deduct_money, ".") > 1 || deduct_money.indexOf(".") == 0) {
                    TravelUtil.showToast("请输入合法的金额");
                    et_price_input.setText("");
                }

                //                if (("1".equals(salerId) || "21".equals(salerId))
                //                        && !"".equals(deduct_money)) {
                //                    deduct_money = ShopTool.getMoney(deduct_money);
                //                    tv_alter_price_instruction.setVisibility(View.VISIBLE);
                //                    tv_alter_price_instruction.setText("平台优惠：" +
                //                            ShopTool.getMoney(Float.parseFloat(deduct_money) * 0.01F + "") +
                //                            "元，顾客需支付：" +
                //                            ShopTool.getMoney(Float.parseFloat(deduct_money) - Float.parseFloat(deduct_money) * 0.01F + "") +
                //                            "元");
                //                }
            }
        });

        mCornerDialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 90),
                LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);

        tv_alter_price_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCornerDialog.dismiss();
            }
        });

        tv_alter_price_sure.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String alter_money = et_price_input.getText().toString();
                if (!TextUtils.isEmpty(alter_money)) {
                    mCornerDialog.dismiss();
                    OrderInfoHttp.alterPrice(ordersId, mContext,
                            ShopTool.getMoney(deduct_money), controlOrderSuccessListener);
                } else
                    TravelUtil.showToast(R.string.orderinfo_discount_not_empty, mContext);
            }
        });
        mCornerDialog.show();

    }

    /**
     * 卖家输入退款金额
     *
     * @param mContext
     * @param remainDay
     * @param totalPrice
     * @param ordersId
     */
    public static void sellerInputRefundMoney(final Context mContext, String remainDay, final String totalPrice,
                                              final long ordersId, final boolean isFirst, String refundMoney, final OrderInfoHttp.ControlOrderSuccessListener controlOrderSuccessListener) {
        deduct_money = "";
        counter = 0;
        rs = mContext.getResources();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_sure_unsubscribe, null);

        TextView tv_remain_time = (TextView) view.findViewById(R.id.tv_remain_time);
        TextView tv_travel_total = (TextView) view.findViewById(R.id.tv_travel_total);
        et_deduct_money = (EditText) view.findViewById(R.id.et_deduct_money);
        TextView tv_sure_unsubscribe_cancle = (TextView) view.findViewById(R.id.tv_sure_unsubscribe_cancle);
        TextView tv_sure_unsubscribe_sure = (TextView) view.findViewById(R.id.tv_sure_unsubscribe_sure);
        if (!isFirst) {
            et_deduct_money.setText(refundMoney);
            deduct_money = refundMoney;
        }
        // 剩余时间
        tv_remain_time.setText(rs.getString(R.string.orderinfo_remain_day) + remainDay);
        // 总价
        tv_travel_total.setText("总价：" + totalPrice + "元");
        // 扣除金额
        et_deduct_money.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                counter = 0;
                deduct_money = s.toString();
                if (countStr(deduct_money, ".") > 1 || deduct_money.indexOf(".") == 0) {
                    TravelUtil.showToast("请输入合法的金额");
                    et_deduct_money.setText("");
                } else if (!TextUtils.isEmpty(deduct_money)
                        && Float.parseFloat(deduct_money) > Float.parseFloat(totalPrice)) {
                    deduct_money = totalPrice;
                    et_deduct_money.setText(deduct_money);
                }
            }
        });

        mCornerDialog = new CornerDialog(mContext, OSUtil.getScreenWidth() - OSUtil.dp2px(mContext, 90),
                LinearLayout.LayoutParams.WRAP_CONTENT, view, R.style.MyDialogStyle);

        tv_sure_unsubscribe_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCornerDialog.dismiss();
            }
        });

        tv_sure_unsubscribe_sure.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(deduct_money)) {
                    mCornerDialog.dismiss();
                    if (isFirst)
                        OrderInfoHttp.refundControl(ordersId, mContext, "", 8, deduct_money, controlOrderSuccessListener);
                    else
                        OrderInfoHttp.changeRefund(ordersId, mContext, deduct_money, controlOrderSuccessListener);
                } else
                    TravelUtil.showToast(R.string.orderinfo_remain_day_not_empty, mContext);
            }
        });
        mCornerDialog.show();

    }

}
