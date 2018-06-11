package com.travel.shop.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.layout.DialogTemplet;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.adapter.TouristInfoShowAdapter;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.CommitBean;
import com.travel.shop.bean.CouponInfoBean;
import com.travel.shop.bean.GoodsOrderBean;
import com.travel.shop.bean.TouristInfo;
import com.travel.shop.http.CommitHttp;
import com.travel.shop.http.OrderInfoHttp;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.widget.PayMethodPopWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 填写信息页
 *
 * @author wyp
 * @created 2017/11/02
 */
public class FilloutInfoActivity extends TitleBarBaseActivity implements View.OnClickListener {

    private Context mContext;

    // 确认商品信息
    private TextView tv_goods_title, tv_goods_intro, tv_count, tv_startDate;
    private CardView cd_twice_need;// 是否需要二次确认
    private int count;// 出行人数

    // 出行人信息
    private TextView tv_fillout_count;
    private ImageView iv_add_tourist;
    private RecyclerView rv_tourist_info;
    private ArrayList<TouristInfo> touristInfos;
    private TouristInfoShowAdapter touristInfoShowAdapter;

    // 联系人信息
    private ImageView iv_local_phone;
    private EditText et_contact_name, et_contact_phone, et_contact_mark;

    // 优惠
    private CardView cd_discount;
    private TextView tv_card_show, tv_remain_count, tv_minus_count, tv_coupon_show, tv_minus_money;
    private ImageView iv_card_select;
    private LinearLayout ll_coupon_select;
    private int remainCount;// 剩余次数
    private String couponShow;// 优惠券的张数以及优惠的钱数
    private boolean existCoupon;// 是否有优惠券
    private ArrayList<CouponInfoBean> couponData;// 优惠券的数据
    private String couponId = "";// 选择的卡券Id
    private float discountPrice = 0;// 选择的优惠券的金额

    // 底部按钮
    private TextView tv_fillout_total_price, tv_pay;
    private float totalPrice;

    // 填写信息相关
    private CardView cd_tourist;
    private int goodsType;

    // 提交数据
    private CommitBean commitBean;
    private long ordersId;
    private String goodsId;
    private CommitHttp.ShowDialogListener showDialogListener = new CommitHttp.ShowDialogListener() {
        @Override
        public void isShowDialog(String notify) {
            if (TextUtils.isEmpty(notify)) {
                pay();
            } else {
                showDialog(notify);
            }
        }
    };
    private DialogTemplet dialog;

    private void showDialog(String notify) {
        dialog = new DialogTemplet(mContext, false, notify, "", "取消下单", "继续下单");
        dialog.show();
        dialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
            @Override
            public void leftClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
            @Override
            public void rightClick(View view) {
                pay();
            }
        });
    }

    // 获取订单Id
    private CommitHttp.CommitListener commitListener = new CommitHttp.CommitListener() {
        @Override
        public void getOrdersId(long ordersId) {
            FilloutInfoActivity.this.ordersId = ordersId;
            OrderInfoHttp.getManageOrderData(ordersId, mContext, listener);
        }
    };
    // 获取支付金额
    private OrderInfoHttp.Listener listener = new OrderInfoHttp.Listener() {
        @Override
        public void onOrderDataFine(GoodsOrderBean goodsOrderBean) {
            float paymentPrice = goodsOrderBean.getmOrdersBasicInfoBean().getPaymentPrice();
            if (paymentPrice == 0) {
                OrderInfoHttp.payZero(ordersId, mContext, zeroPayListener);
            } else {
                new PayMethodPopWindow(mContext, commitBean.getGoodsTitle(), FilloutInfoActivity.this, ordersId,
                        paymentPrice + "", "orderRoute", 2, 1);
            }
        }

        @Override
        public void onErrorNotZero(int error, String msg) {

        }

        @Override
        public void onAttachGoodsGot(ArrayList<AttachGoodsBean> attachGoods) {

        }
    };
    // 支付0元
    private OrderInfoHttp.ControlListener zeroPayListener = new OrderInfoHttp.ControlListener() {
        @Override
        public void onPayZero(long ordersId) {
            OSUtil.intentOrderSuccess(mContext, ordersId);
        }
    };

    // 获取到优惠券
    private CommitHttp.CouponListener couponListener = new CommitHttp.CouponListener() {
        @Override
        public void getCouponData(ArrayList<CouponInfoBean> couponData) {
            ArrayList<CouponInfoBean> couponList = new ArrayList<>();
            for (int i = 0; i < couponData.size(); i++) {// 只有当前商品可用的券添加到卡券数据中
                if (couponData.get(i).getGoodsIds().contains(goodsId)
                        && couponData.get(i).getStatusCoupon() == 1)// 并且卡券是可用的（保护而已）
                    couponList.add(couponData.get(i));
            }
            commitBean.setCouponDatas(couponList);
            FilloutInfoActivity.this.couponData = commitBean.getCouponDatas();
            existCoupon = FilloutInfoActivity.this.couponData.size() == 0 ? false : true;
            couponShow = existCoupon ? "有" + FilloutInfoActivity.this.couponData.size() + "张优惠券可用" : "无可用优惠券";
            tv_minus_money.setText(couponShow);
            setCouponShowType();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fillout_info);

        initView();
        initData();
        initListener();
        if (goodsType == 6) {
            // 有网
            if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
                // 获取卡券信息
                CommitHttp.getCouponData(mContext, 0, couponListener);
            }
        }
    }

    private void initView() {
        tv_goods_title = findView(R.id.tv_goods_title);
        tv_goods_intro = findView(R.id.tv_goods_intro);
        tv_count = findView(R.id.tv_count);
        tv_startDate = findView(R.id.tv_startDate);
        cd_twice_need = findView(R.id.cd_twice_need);

        tv_fillout_count = findView(R.id.tv_fillout_count);
        iv_add_tourist = findView(R.id.iv_add_tourist);
        rv_tourist_info = findView(R.id.rv_tourist_info);

        iv_local_phone = findView(R.id.iv_local_phone);
        et_contact_name = findView(R.id.et_contact_name);
        et_contact_phone = findView(R.id.et_contact_phone);
        et_contact_mark = findView(R.id.et_contact_mark);

        cd_discount = findView(R.id.cd_discount);
        tv_card_show = findView(R.id.tv_card_show);
        tv_remain_count = findView(R.id.tv_remain_count);
        tv_minus_count = findView(R.id.tv_minus_count);
        tv_coupon_show = findView(R.id.tv_coupon_show);
        tv_minus_money = findView(R.id.tv_minus_money);
        iv_card_select = findView(R.id.iv_card_select);
        ll_coupon_select = findView(R.id.ll_coupon_select);

        tv_fillout_total_price = findView(R.id.tv_fillout_total_price);
        tv_pay = findView(R.id.tv_pay);

        cd_tourist = findView(R.id.cd_tourist);
    }

    private void initData() {
        mContext = this;

        // 解决键盘自动弹出的问题
        tv_goods_title.setFocusable(true);
        tv_goods_title.setFocusableInTouchMode(true);

        setTitle("填写信息");
        commitBean = (CommitBean) getIntent().getSerializableExtra("info");
        goodsId = commitBean.getGoodsId();
        goodsType = commitBean.getGoodsType();
        count = commitBean.getAdultNum();
        tv_goods_title.setText(commitBean.getGoodsTitle());
        if (goodsType == 6)
            tv_goods_intro.setText("本卡为两年内10人次《小城故事》同款产品消费卡。"); // TODO 数据暂无

        // 优惠
        if (goodsType == 6) {
            cd_discount.setVisibility(View.VISIBLE);
            // 卡
            remainCount = commitBean.getRemainCount();
            tv_remain_count.setText("剩余" + remainCount + "次");
            // 券
            couponData = commitBean.getCouponDatas();
            existCoupon = couponData.size() == 0 ? false : true;
            couponShow = existCoupon ? "有" + couponData.size() + "张优惠券可用" : "无可用优惠券";
            tv_minus_money.setText(couponShow);
        }
        if (goodsType == 6) {// 小城卡
            tv_startDate.setText(count + "张");
        } else {
            tv_count.setText("成人：" + count + "人");// TODO 默认只有成人
            tv_startDate.setText("出行日期：" + commitBean.getDepartTime());
            if (commitBean.isTwice())
                cd_twice_need.setVisibility(View.VISIBLE);

            if (commitBean.isInfoNeed()) {
                cd_tourist.setVisibility(View.VISIBLE);
                tv_fillout_count.setText("出行人信息：  添加" + count + "位出行人");
                rv_tourist_info.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                touristInfos = new ArrayList<>();
                touristInfoShowAdapter = new TouristInfoShowAdapter(touristInfos, mContext);
                rv_tourist_info.setAdapter(touristInfoShowAdapter);
            }

        }

        // 联系人的默认信息
        et_contact_name.setText(UserSharedPreference.getNickName());
        et_contact_phone.setText(UserSharedPreference.getMobile());

        // 总价
        computeTotalPrice();
    }

    private void initListener() {
        iv_add_tourist.setOnClickListener(this);
        iv_local_phone.setOnClickListener(this);
        ll_coupon_select.setOnClickListener(this);
        tv_pay.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == iv_add_tourist) {// 添加出行人
            Intent intent = new Intent(mContext, TouristsActivity.class);
            intent.putExtra("count", count);
            startActivityForResult(intent, 0);
        } else if (v == iv_local_phone) {// 调起本地联系人
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FilloutInfoActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
            } else {
                Uri uri = Uri.parse("content://contacts/people");
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                startActivityForResult(intent, 1);
            }
        } else if (v == ll_coupon_select) {// 点击优惠券
            Intent intent = new Intent(mContext, CouponChooseActivity.class);
            intent.putExtra("price", ShopTool.getMoney(totalPrice + discountPrice + ""));
            intent.putExtra("discountPrice", ShopTool.getMoney(discountPrice + ""));
            intent.putExtra("couponData", couponData);
            intent.putExtra("couponId", couponId);
            startActivityForResult(intent, 2);
        } else if (v == tv_pay) {// 支付
            CommitHttp.isShowDialog(mContext, showDialogListener);
        }
    }

    private void pay() {
        if (commitBean.isInfoNeed()) {
            if (TextUtils.isEmpty(commitBean.getUserinfo())) {
                showToast("请添加出行人信息");
                return;
            }
        }
        String name = et_contact_name.getText().toString();
        String phone = et_contact_phone.getText().toString();
        String mark = et_contact_mark.getText().toString();
        commitBean.setBuyerName(name);
        commitBean.setBuyerTelephone(phone);
        commitBean.setRemarks(mark);
        if (TextUtils.isEmpty(name)) {
            showToast("请填写联系人姓名");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("请填写联系人电话");
            return;
        }
        if (!phone.matches("1[3|4|5|7|8|][0-9]{9}")) {
            showToast("请填写正确的手机号码");
            return;
        }
        CommitHttp.commintOrderNew(commitBean, mContext, commitListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Uri uri = Uri.parse("content://contacts/people");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    startActivityForResult(intent, 1);
                } else {
                    showToast("您已禁止获取联系人权限");
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {// 添加出行人
                case 0:
                    touristInfos.clear();
                    touristInfos.addAll((Collection<? extends TouristInfo>) data.getSerializableExtra("touristChoosed"));
                    touristInfoShowAdapter.notifyDataSetChanged();
                    commitBean.setUserinfo(ShopTool.mapToJson(touristInfos));
                    break;
                case 1:// 添加联系人
                    Uri uri = data.getData();
                    String[] contacts = getPhoneContacts(uri);
                    et_contact_name.setText(contacts[0]);
                    et_contact_phone.setText(TextUtils.isEmpty(contacts[1]) ? "" : contacts[1].replace(" ", ""));
                    break;
                case 2: // 选择完优惠券
                    discountPrice = data.getFloatExtra("discountPrice", 0);
                    setShow(1, 1);
                    if (discountPrice != 0) {
                        tv_minus_money.setText("- " + ShopTool.getMoney(discountPrice + ""));
                    } else {
                        tv_minus_money.setText(couponShow);
                    }
                    couponId = data.getStringExtra("couponId");
                    commitBean.setUserCoupnoIds(couponId);
                    String[] coupon = couponId.split(",");
                    // 再次进入时之前选择的仍然存在
                    discountPrice = 0;
                    for (int i = 0; i < couponData.size(); i++) {
                        if (Arrays.asList(coupon).contains(couponData.get(i).getCouponId())) {
                            couponData.get(i).setChoosed(true);
                            discountPrice += Float.parseFloat(couponData.get(i).getCurrentPrice());
                        } else
                            couponData.get(i).setChoosed(false);
                    }
                    computeTotalPrice();
                    break;
            }
        }
    }

    /**
     * 获取手机联系人信息
     *
     * @param uri
     * @return
     */
    private String[] getPhoneContacts(Uri uri) {
        String[] contact = new String[2];
        //得到ContentResolver对象
        ContentResolver cr = getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //取得联系人姓名
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            contact[0] = cursor.getString(nameFieldColumnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            if (phone != null && phone.getCount() > 0) {
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phone.close();
            cursor.close();
        } else {
            return null;
        }
        return contact;
    }

    /**
     * 获取小城卡和优惠券的展示类型并且设置优惠的初始展示形态
     */
    private void setCouponShowType() {
        setShow(0, 0);
        if (!existCoupon) {
            setShow(1, 0);
        } else {
            setShow(1, 1);
        }
    }

    /**
     * @param couponType 0: 卡 1：券
     * @param show       卡：0-->置灰
     *                   券：0-->置灰 1-->不置灰
     */
    private void setShow(int couponType, int show) {
        switch (couponType) {
            case 0:
                switch (show) {
                    case 0:
                        tv_card_show.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_remain_count.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_minus_count.setText("");
                        iv_card_select.setSelected(false);
                        iv_card_select.setEnabled(false);
                        break;
                }
                break;
            case 1:
                switch (show) {
                    case 0:
                        ll_coupon_select.setEnabled(false);
                        tv_coupon_show.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_minus_money.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        break;
                    case 1:
                        ll_coupon_select.setEnabled(true);
                        tv_coupon_show.setTextColor(ContextCompat.getColor(mContext, R.color.black_48494C));
                        tv_minus_money.setTextColor(ContextCompat.getColor(mContext, R.color.red_FA7E7F));
                        break;
                }
                break;
        }
    }

    private void computeTotalPrice() {
        float beforeTotalPrice = commitBean.getTotalPrice();
        float coupon = discountPrice;
        float touristInsurance = 0;
        //        float touristInsurance = 20 * (iv_insurance_select.isSelected() ? count : 0);
        float cardPrice = 10000 * count;
        float tempPrice = goodsType == 6 ? (cardPrice - coupon) : beforeTotalPrice;
        totalPrice = (tempPrice < 0 ? 0 : tempPrice) + touristInsurance;
        tv_fillout_total_price.setText("￥" + ShopTool.getMoney(totalPrice + ""));
    }

}
