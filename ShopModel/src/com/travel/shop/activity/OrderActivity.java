package com.travel.shop.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.GoodsDetailBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.shop.R;
import com.travel.shop.adapter.SelectStartPlaceAdapter;
import com.travel.shop.bean.AttachGoodsBean;
import com.travel.shop.bean.CalendarBean;
import com.travel.shop.bean.CommitBean;
import com.travel.shop.bean.CouponInfoBean;
import com.travel.shop.fragment.NewCalendarFragment;
import com.travel.shop.http.CommitHttp;
import com.travel.shop.http.ShopSqliteOpenHelper;
import com.travel.shop.tools.ShopTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 预订页
 *
 * @author wyp
 * @created 2017/11/01
 */
public class OrderActivity extends TitleBarBaseActivity implements View.OnClickListener {

    private static final String TAG = "OrderActivity";
    private Context mContext;
    // 本地存储日历数据
    private ShopSqliteOpenHelper mSqlite;
    private int goodsType;

    // 选择出发地
    private RecyclerView rv_start_place;
    private SelectStartPlaceAdapter placeAdapter;
    private String goodsId;
    private ArrayList<HashMap<String, Object>> places;
    // 选择月份
    private TextView tv_advance_day;
    private int advanceDays;
    private String packageId, oldPackageId;
    private ArrayList<CalendarBean> orderData;// 获取的的日历数据
    private boolean isFirst = true;// 是否是第一次获取日历数据
    // 人数选择
    private View ll_adult, ll_adult_minus, ll_adult_plus;
    private ImageView adult_minus;
    private TextView tv_adult_count;
    private int adultCount;
    private boolean isClear;// 是否清除人数和钱数（只有换出发地的时候会清除），即点击同一个出发地时所有的数据都不变

    // 优惠
    private TextView tv_card_show, tv_remain_count, tv_minus_count, tv_coupon_show, tv_minus_money, tv_card_buy;
    private ImageView iv_card_select;
    private LinearLayout ll_coupon_select;
    private int remainCount;// 剩余次数
    private String couponShow;// 优惠券的张数以及优惠的钱数
    private boolean isSupportCard;// 是否有小城卡
    private boolean existCard;// 是否有小城卡
    private boolean existCoupon;// 是否有优惠券
    private ArrayList<CouponInfoBean> couponData;// 优惠券的数据
    private int showType;// 优惠展示类型
    private String couponId = "";// 选择的卡券Id
    private float discountPrice = 0;// 选择的优惠券的金额
    private String cardId;

    // 保险
    private CardView cd_insurance;
    private TextView tv_insurance;
    private ImageView iv_insurance_select;

    // 单房差
    private CardView cd_single;
    private View ll_single_minus, ll_single_plus, ll_single;
    private TextView tv_single, tv_single_count;
    private ImageView iv_single_count_minus;
    private int singleCount;
    private String singlePrice;

    // 底部按钮
    private TextView tv_goods_total_price, tv_order;
    private float unitMoney, totalMoney;
    private CommitBean mCommitBean;// 要提交的信息
    private NewCalendarFragment.OnDateChangeListener onDateChangeListener = new NewCalendarFragment.OnDateChangeListener() {
        @Override
        public void onDateChange(String[] positionData, ArrayList<CalendarBean> curData, int selectedDays) {
            mCommitBean.setSkuId(positionData[3]);// TODO 现在默认只有单选
            mCommitBean.setDepartTime(curData.get(0).getDate());
            mCommitBean.setSignlePrice(Float.parseFloat(positionData[2]));
            unitMoney = Float.parseFloat(positionData[0]);
            updateMoney(false);
        }

    };
    private CommitHttp.CommitOrderListener commitListener = new CommitHttp.CommitOrderListener() {

        @Override
        public void getAttachGoods(ArrayList<AttachGoodsBean> goodsList) {
        }

        @Override
        public void getGoodsData(GoodsDetailBean mGoodsDetailBean) {
        }

        @Override
        public void onErrorNotZero() {
        }

        @Override
        public void getPackageData(ArrayList<HashMap<String, Object>> packages) {
            if (packages.size() > 0 && isFirst) {
                isFirst = false;
                packageId = packages.get(0).get("id").toString();
                oldPackageId = packageId;
                mCommitBean.setPackageName(packages.get(0).get("name").toString());
                // 默认获取第一个套餐的日历数据
                CommitHttp.getCalendarData(mContext, goodsId, packageId, advanceDays, commitListener);
            }
            places.addAll(packages);
            placeAdapter.notifyDataSetChanged();
        }

        @Override
        public void getCalendarData(ArrayList<CalendarBean> orderData) {
            if (orderData.size() > 0) {
                // 将已经获取的套餐的日历数据存入数据库
                mSqlite = new ShopSqliteOpenHelper(mContext, orderData);
                mSqlite.insert("Calendar");
            }
            NewCalendarFragment calendarFragment = (NewCalendarFragment) getSupportFragmentManager().findFragmentById(R.id.new_calendar_fragment);
            calendarFragment.setList(orderData, goodsType);
            calendarFragment.setListener(onDateChangeListener);
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
            mCommitBean.setCouponDatas(couponList);
            // 券
            OrderActivity.this.couponData = couponList;
            existCoupon = OrderActivity.this.couponData.size() == 0 ? false : true;
            showType = getCouponShowType();
            couponShow = existCoupon ? "有" + OrderActivity.this.couponData.size() + "张优惠券可用" : "无可用优惠券";
            tv_minus_money.setText(couponShow);
        }
    };

    /**
     * @param context
     * @param goodsInfo 商品信息
     * @param count     剩余次数
     */
    public static void actionStart(Context context, GoodsBasicInfoBean goodsInfo, int count, String cardId) {
        Intent intent = new Intent(context, OrderActivity.class);
        intent.putExtra("goodsInfo", goodsInfo);
        intent.putExtra("count", count);
        intent.putExtra("cardId", cardId);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initView();
        initData();
        initListener();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            CommitHttp.getPackagData(mContext, goodsId, commitListener);
//            // 获取卡券信息
//            CommitHttp.getCouponData(mContext, 0, couponListener);
        }
    }

    private void initView() {
        mContext = this;

        rv_start_place = findView(R.id.rv_start_place);
        tv_advance_day = findView(R.id.tv_advance_day);

        ll_adult = findView(R.id.ll_adult);
        ll_adult_minus = ll_adult.findViewById(R.id.ll_m_click);
        ll_adult_plus = ll_adult.findViewById(R.id.ll_p_click);
        adult_minus = (ImageView) ll_adult.findViewById(R.id.iv_minus);
        tv_adult_count = (TextView) ll_adult.findViewById(R.id.tv_count);

        tv_card_show = findView(R.id.tv_card_show);
        tv_remain_count = findView(R.id.tv_remain_count);
        tv_minus_count = findView(R.id.tv_minus_count);
        tv_coupon_show = findView(R.id.tv_coupon_show);
        tv_minus_money = findView(R.id.tv_minus_money);
        tv_card_buy = findView(R.id.tv_card_buy);
        iv_card_select = findView(R.id.iv_card_select);
        ll_coupon_select = findView(R.id.ll_coupon_select);

        cd_insurance = findView(R.id.cd_insurance);
        tv_insurance = findView(R.id.tv_insurance);
        iv_insurance_select = findView(R.id.iv_insurance_select);

        cd_single = findView(R.id.cd_single);
        ll_single = findView(R.id.ll_single);
        ll_single_minus = ll_single.findViewById(R.id.ll_m_click);
        ll_single_plus = ll_single.findViewById(R.id.ll_p_click);
        tv_single = findView(R.id.tv_single);
        tv_single_count = (TextView) ll_single.findViewById(R.id.tv_count);
        iv_single_count_minus = (ImageView) ll_single.findViewById(R.id.iv_minus);

        tv_goods_total_price = findView(R.id.tv_goods_total_price);
        tv_order = findView(R.id.tv_order);
    }

    private void initData() {
        mSqlite = new ShopSqliteOpenHelper(mContext, new ArrayList<CalendarBean>());
        mSqlite.delete(ShopSqliteOpenHelper.TABLENAME_CALENDAR, "");
        setTitle("预订");

        rv_start_place.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        places = new ArrayList<>();
        orderData = new ArrayList<>();
        placeAdapter = new SelectStartPlaceAdapter(places, mContext, 1);
        rv_start_place.setAdapter(placeAdapter);

        GoodsBasicInfoBean info = (GoodsBasicInfoBean) getIntent().getSerializableExtra("goodsInfo");
        goodsId = info.getGoodsId();
        goodsType = info.getGoodsType();
        advanceDays = info.getGoodsReserveDays();
        tv_advance_day.setText("选择出发日期：请至少提前" + advanceDays + "天预定");

        mCommitBean = new CommitBean();
        mCommitBean.setGoodsId(goodsId);
        mCommitBean.setGoodsType(info.getGoodsType());
        mCommitBean.setGoodsTitle(info.getGoodsAddress() + "|" + info.getGoodsTitle());
        mCommitBean.setTwice(info.getTwiceSure() == 1 ? true : false);
        mCommitBean.setInfoNeed(TextUtils.equals(info.getInfoNeed(), "0") ? false : true);
        mCommitBean.setSupportCard(info.isSupportCard());
        remainCount = getIntent().getIntExtra("count", 0);
        mCommitBean.setRemainCount(remainCount);

        // 优惠 券的部分在获取到卡券时设置
        // 卡
        tv_remain_count.setText("剩余" + remainCount + "次");
        isSupportCard = info.isSupportCard();
        existCard = remainCount == 0 ? false : true;
        cardId = getIntent().getStringExtra("cardId");

        // 保险 TODO 假数据 暂不显示
        cd_insurance.setVisibility(View.GONE);
        //            cd_insurance.setVisibility(View.VISIBLE);

        couponData = new ArrayList<>();
    }

    private void initListener() {
        placeAdapter.setmOnItemClickListener(new SelectStartPlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                for (int i = 0; i < places.size(); i++) {
                    if (i == position) {
                        packageId = places.get(i).get("id").toString();
                        if (oldPackageId == packageId)
                            isClear = false;
                        else
                            isClear = true;
                        oldPackageId = packageId;

                        if (isClear) {
                            places.get(i).put("isChecked", true);
                            mCommitBean.setPackageName(places.get(i).get("name").toString());
                            boolean isSaved = getLocalData(packageId);
                            if (isSaved) {
                                NewCalendarFragment calendarFragment = (NewCalendarFragment) getSupportFragmentManager().findFragmentById(R.id.new_calendar_fragment);
                                calendarFragment.setList(orderData, goodsType);
                                calendarFragment.setListener(onDateChangeListener);
                            } else {
                                CommitHttp.getCalendarData(mContext, goodsId, packageId, advanceDays, commitListener);
                            }
                        }
                        updateMoney(true);
                    } else
                        places.get(i).put("isChecked", false);
                }
                if (isClear)
                    placeAdapter.notifyDataSetChanged();
            }
        });

        ll_adult_minus.setOnClickListener(this);
        ll_adult_plus.setOnClickListener(this);
        iv_card_select.setOnClickListener(this);
        ll_coupon_select.setOnClickListener(this);
        iv_insurance_select.setOnClickListener(this);
        ll_single_minus.setOnClickListener(this);
        ll_single_plus.setOnClickListener(this);
        tv_card_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, GoodsActivity.class).putExtra("goodsId", cardId));
            }
        });
        tv_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unitMoney == 0) {
                    showToast("请选择出发日期");
                    return;
                }
                if (adultCount == 0) {
                    showToast("请选择人数");
                    return;
                }
                if (goodsType == 1 || goodsType == 2 || goodsType == 5) {
                    if (iv_card_select.isSelected())
                        mCommitBean.setCardUseCount(getMinusCount());
                    else
                        mCommitBean.setCardUseCount(0);
                }
                if (goodsType == 1 || goodsType == 2 || goodsType == 3) {
                    mCommitBean.setRoomNum(singleCount);
                }
                mCommitBean.setTotalPrice(totalMoney);
                startActivity(new Intent(mContext, FilloutInfoActivity.class).putExtra("info", mCommitBean));
            }
        });
    }

    /**
     * 获取本地数据
     *
     * @return true:获取到 false:没获取到
     */
    private boolean getLocalData(String packageId) {
        mSqlite = new ShopSqliteOpenHelper(mContext, new ArrayList<CalendarBean>());
        Cursor c = mSqlite.query(packageId, "", "");
        orderData.clear();
        if (c.moveToFirst()) {
            do {
                CalendarBean cb = new CalendarBean();
                // 日历Id
                cb.setCalendarId(c.getString(c.getColumnIndex("calendarId")));
                // 商品Id
                cb.setGoodsId(c.getString(c.getColumnIndex("goodsId")));
                // 出发日期
                cb.setDate(c.getString(c.getColumnIndex("startDate")));
                // 成人价
                cb.setAdult_price(c.getString(c.getColumnIndex("adultsPrice")));
                // 儿童价
                cb.setChildren_price(c.getString(c.getColumnIndex("childrenPrice")));
                // 单房差
                cb.setSingle_room_price(c.getString(c.getColumnIndex("roomPrice")));
                // 年份
                cb.setYear(c.getString(c.getColumnIndex("year")));
                // 月份
                cb.setMonth(c.getString(c.getColumnIndex("month")));
                // 下标数
                cb.setIndex(c.getInt(c.getColumnIndex("indexId")));
                // 套餐Id
                cb.setPackageId(c.getString(c.getColumnIndex("packageId")));
                orderData.add(cb);
            } while (c.moveToNext());
        }
        c.close();
        mSqlite.close();
        if (orderData.size() == 0)
            return false;
        else
            return true;
    }

    @Override
    public void onClick(View v) {
        if (v == ll_adult_minus) {// 成人数"-"
            initDiscount();
            if (adultCount > 0) {
                --adultCount;
                tv_adult_count.setText(adultCount + "");
                adult_minus.setImageResource(adultCount == 0 ?
                        R.drawable.icon_card_minus_no : R.drawable.icon_card_minus_yes);
                tv_insurance.setText("旅游险 ￥20 * " + adultCount);
                iv_insurance_select.setSelected(adultCount == 0 ? false : true);
            }
        } else if (v == ll_adult_plus) {
            // 成人数"+" 选择日期后再选人数
            if (unitMoney == 0) {
                showToast("请选择出发日期");
                return;
            }
            ++adultCount;
            tv_adult_count.setText(adultCount + "");
            adult_minus.setImageResource(R.drawable.icon_card_minus_yes);
            tv_insurance.setText("旅游险 ￥20 * " + adultCount);
            iv_insurance_select.setSelected(true);
        } else if (v == iv_card_select) {// 点击小城卡优惠的按钮
            if (showType == 1) {
                setShow(0, iv_card_select.isSelected() ? 1 : 2);
            } else if (showType == 3) {
                if (iv_card_select.isSelected()) {
                    setShow(0, 1);
                    setShow(1, 1);
                } else {
                    setShow(0, 2);
                    setShow(1, 0);
                }
            }
            if (iv_card_select.isSelected() && adultCount != 0)
                tv_minus_count.setText("-" + getMinusCount());
        } else if (v == ll_coupon_select) {// 点击优惠券
            Intent intent = new Intent(mContext, CouponChooseActivity.class);
            intent.putExtra("price", ShopTool.getMoney(totalMoney + discountPrice + ""));
            intent.putExtra("discountPrice", ShopTool.getMoney(discountPrice + ""));
            intent.putExtra("couponData", couponData);
            intent.putExtra("couponId", couponId);
            startActivityForResult(intent, 2);
        } else if (v == iv_insurance_select) {// 保险
            iv_insurance_select.setSelected(!iv_insurance_select.isSelected());
            computeTotalPrice();
        } else if (v == ll_single_minus) {// 单房差"-"
            if (singleCount >= 2) {
                singleCount -= 2;
                tv_single_count.setText(singleCount + "");
                iv_single_count_minus.setImageResource((singleCount == 0 || singleCount == 1) ?
                        R.drawable.icon_card_minus_no : R.drawable.icon_card_minus_yes);

            }
            tv_single.setText(singleCount == 0 ? "" : (singlePrice + singleCount));
            computeTotalPrice();
        } else if (v == ll_single_plus) {// 单房差"+"
            if (adultCount == 0) {
                showToast("请选择人数");
                return;
            }
            if (singleCount < adultCount) {
                singleCount += 2;
                tv_single_count.setText(singleCount + "");
                iv_single_count_minus.setImageResource(R.drawable.icon_card_minus_yes);
            }
            tv_single.setText(singlePrice + singleCount);
            computeTotalPrice();
        }
        if (v == ll_adult_plus || v == ll_adult_minus) {
            if (isSupportCard && existCard && iv_card_select.isSelected())
                tv_minus_count.setText("-" + getMinusCount());
            // 单房差
            cd_single.setVisibility(View.VISIBLE);
            if (goodsType == 1 || goodsType == 2 || goodsType == 3) {
                singleCount = adultCount % 2 == 1 ? 1 : 0;
                singlePrice = "￥" + ShopTool.getMoney(mCommitBean.getSignlePrice() + "") + " * ";
                tv_single.setText(singleCount == 0 ? "" : (singlePrice + singleCount));
                tv_single_count.setText(singleCount + "");
            }
        }
        updateMoney(false);
    }

    /**
     * 刷新人数和钱数
     */
    private void updateMoney(boolean isNeed) {
        if (isClear && isNeed) {
            // 成人数相关
            adult_minus.setImageResource(R.drawable.icon_card_minus_no);
            tv_adult_count.setText("0");
            adultCount = 0;
            unitMoney = 0;
            totalMoney = 0;

            // 优惠
            initDiscount();

            // 单房差相关
            iv_single_count_minus.setImageResource(R.drawable.icon_card_minus_no);
            tv_single.setText("");
            singleCount = 0;
            tv_single_count.setText("0");
            // 总价
            tv_goods_total_price.setText("￥0");
        } else {
            totalMoney = adultCount * unitMoney;
            mCommitBean.setTotalPrice(totalMoney);
            mCommitBean.setAdultNum(adultCount);
            computeTotalPrice();
        }
    }

    private void computeTotalPrice() {
        float beforeTotalPrice = mCommitBean.getTotalPrice();
        float couponCard = (showType == 1 || showType == 3) ? (iv_card_select.isSelected() ? 2000 * getMinusCount() : 0) : 0;
        float coupon = discountPrice;
        float touristInsurance = 0;
        //        float touristInsurance = 20 * (iv_insurance_select.isSelected() ? count : 0);
        float singlePrice = mCommitBean.getSignlePrice() * singleCount;
        float tempPrice = beforeTotalPrice - couponCard - coupon;// 优惠只针对于成人价
        totalMoney = (tempPrice < 0 ? 0 : tempPrice) + touristInsurance + singlePrice;
        tv_goods_total_price.setText("￥" + ShopTool.getMoney(totalMoney + ""));
    }

    /**
     * 初始化优惠部分的效果
     */
    private void initDiscount() {
        getCouponShowType();
        couponId = "";
        discountPrice = 0;
        for (int i = 0; i < couponData.size(); i++) {
            couponData.get(i).setChoosed(false);
        }
        couponShow = existCoupon ? "有" + OrderActivity.this.couponData.size() + "张优惠券可用" : "无可用优惠券";
        tv_minus_money.setText(couponShow);
    }

    /**
     * 获取到优惠的小城卡数
     *
     * @return
     */
    private int getMinusCount() {
        return remainCount < adultCount ? remainCount : adultCount;
    }

    /**
     * 获取小城卡和优惠券的展示类型并且设置优惠的初始展示形态
     *
     * @return 0：都没有
     * 1：卡有，券没有
     * 2：卡没有，券有
     * 3：都有
     */
    private int getCouponShowType() {
        if (!existCard && !existCoupon) {
            if (isSupportCard) {
                setShow(0, 3);
            } else {
                setShow(0, 0);
            }
            setShow(1, 0);
            return 0;
        } else if (existCard && !existCoupon) {
            if (isSupportCard) {
                setShow(0, 1);
                setShow(1, 0);
                return 1;
            } else {
                setShow(0, 0);
                setShow(1, 0);
                return 0;
            }
        } else if (!existCard && existCoupon) {
            if (isSupportCard) {
                setShow(0, 3);
            } else {
                setShow(0, 0);
            }
            setShow(1, 1);
            return 2;
        } else {
            if (isSupportCard) {
                setShow(0, 1);
                setShow(1, 1);
                return 3;
            } else {
                setShow(0, 0);
                setShow(1, 1);
                return 2;
            }
        }
    }

    /**
     * @param couponType 0: 卡 1：券
     * @param show       卡：0-->置灰（不支持小城卡）  1-->不置灰，未选择 2-->不置灰，已选择 3-->置灰，显示“购买小城卡”
     *                   券：0-->置灰 1-->不置灰
     */
    private void setShow(int couponType, int show) {
        switch (couponType) {
            case 0:
                switch (show) {
                    case 0:
                        iv_card_select.setVisibility(View.VISIBLE);
                        tv_card_show.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_remain_count.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_minus_count.setText("");
                        iv_card_select.setSelected(false);
                        iv_card_select.setEnabled(false);
                        break;
                    case 1:
                        iv_card_select.setVisibility(View.VISIBLE);
                        tv_card_show.setTextColor(ContextCompat.getColor(mContext, R.color.black_48494C));
                        tv_remain_count.setTextColor(ContextCompat.getColor(mContext, R.color.black_6C6F73));
                        tv_minus_count.setText("");
                        iv_card_select.setSelected(false);
                        iv_card_select.setEnabled(true);
                        break;
                    case 2:
                        iv_card_select.setVisibility(View.VISIBLE);
                        tv_card_show.setTextColor(ContextCompat.getColor(mContext, R.color.black_48494C));
                        tv_remain_count.setTextColor(ContextCompat.getColor(mContext, R.color.black_6C6F73));
                        tv_minus_count.setText(getMinusCount() == 0 ? "" : ("-" + getMinusCount()));
                        iv_card_select.setSelected(true);
                        iv_card_select.setEnabled(true);
                        break;
                    case 3:
                        tv_card_buy.setVisibility(View.VISIBLE);
                        tv_card_show.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_remain_count.setTextColor(ContextCompat.getColor(mContext, R.color.gray_C0));
                        tv_minus_count.setText("");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 2) {
                discountPrice = data.getFloatExtra("discountPrice", 0);
                if (discountPrice != 0) {
                    tv_minus_money.setText("- " + ShopTool.getMoney(discountPrice + ""));
                    if (showType == 3) {
                        setShow(0, 0);
                    }
                    setShow(1, 1);
                } else {
                    tv_minus_money.setText(couponShow);
                    if (showType == 3) {
                        setShow(0, 1);
                    }
                    setShow(1, 1);
                }
                couponId = data.getStringExtra("couponId");
                mCommitBean.setUserCoupnoIds(couponId);
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
            }
        }
    }
}
