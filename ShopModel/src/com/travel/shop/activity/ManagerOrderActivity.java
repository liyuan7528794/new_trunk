package com.travel.shop.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.bean.OrderBean;
import com.travel.shop.fragment.ManageFragment;
import com.travel.shop.tools.OrderInfoDialogTool;
import com.travel.shop.tools.ShopTool;
import com.zxing.CaptureActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 订单管理页
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/15
 */
public class ManagerOrderActivity extends TitleBarBaseActivity implements OnClickListener {

    private static final String TAG = "ManagerOrderActivity";
    private Context mContext;
    // 选项卡以及下划线
    private HorizontalScrollView hs_tabs_manage;
    private TextView tv_all, tv_pay, tv_travel, tv_comments;
    private LinearLayout ll_manage_line;
    //    private View v_orders_line, ll_orders_title_layout;
    private ArrayList<TextView> tvs = new ArrayList<>();
    private int tabWidth;// 下划线宽度
    private LinearLayout.LayoutParams lpLine;

    private ViewPager vp_manage;
    // 判断网络
    public static boolean isNet;

    // 小红点相关
    private TextView tv_doing_red_point, tv_done_red_point;
    public static Handler mHandler;
    public static String orderType;
    public static int status = 0;// 此字段用于判断再次进入此Activity的时候是什么状态 1：进行中 2：已完成

    public static void actionStart(Context context, String orderType) {
        Intent intent = new Intent(context, ManagerOrderActivity.class);
        intent.putExtra("orderType", orderType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_manager_order);
        init();

        vp_manage.setOffscreenPageLimit(3);
        vp_manage.setAdapter(new ManageAdapter(getSupportFragmentManager()));
        tv_all.setOnClickListener(this);
        tv_pay.setOnClickListener(this);
        tv_travel.setOnClickListener(this);
        tv_comments.setOnClickListener(this);
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(orderType, "my"))
                    startActivity(new Intent("com.travel.activity.HomeActivity").putExtra("position", Constants.USERCENTER_POSITION));
                finish();
            }
        });
        group_chat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ManagerOrderActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    Intent openCameraIntent = new Intent(mContext,
                            CaptureActivity.class);
                    startActivityForResult(openCameraIntent, 0);
                }
            }
        });

        vp_manage.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                // 选项卡字的颜色
                for (TextView tview : tvs) {
                    tview.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
                }
                selectTab(tvs.get(position), position);
                status = position + 1;
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                hs_tabs_manage.setScrollX((int) ((positionOffset + position) * tabWidth * 0.8));
                lpLine = (LinearLayout.LayoutParams) ll_manage_line.getLayoutParams();
                lpLine.leftMargin = (int) ((positionOffset + position) * tabWidth);
                lpLine.width = tabWidth;

                // 将对下划线的布局调整应用到下划线本身
                ll_manage_line.setLayoutParams(lpLine);
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String num = "";
                if (msg.obj != null && msg.obj instanceof Integer && (Integer) msg.obj != 0)
                    num = "(" + msg.obj + ")";
                switch (msg.what) {
                    case 0:// 全部
                        tv_all.setText(tv_all.getTag() + num);
                        //                        initTabLine(false);
                        break;
                    case OrderBean.STATUS_1:// 待支付
                        tv_pay.setText(tv_pay.getTag() + num);
                        //                        initTabLine(true);
                        break;
                    case OrderBean.STATUS_3:// 待出行
                        tv_travel.setText(tv_travel.getTag() + num);
                        //                        initTabLine(true);
                        break;
                    case OrderBean.STATUS_5:// 待评价
                        tv_comments.setText(tv_comments.getTag() + num);
                        //                        initTabLine(true);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 对控件初始化
     */
    private void init() {
        mContext = this;
        //        v_orders_line = findView(R.id.v_orders_line);
        //        ll_orders_title_layout = findView(R.id.ll_orders_title_layout);
        hs_tabs_manage = findView(R.id.hs_tabs_manage);
        tv_all = findView(R.id.tv_all);
        tv_all.setTag(tv_all.getText().toString());
        tv_pay = findView(R.id.tv_pay);
        tv_pay.setTag(tv_pay.getText().toString());
        tv_travel = findView(R.id.tv_travel);
        tv_travel.setTag(tv_travel.getText().toString());
        tv_comments = findView(R.id.tv_comments);
        tv_comments.setTag(tv_comments.getText().toString());
        ll_manage_line = findView(R.id.ll_manage_line);
        vp_manage = findView(R.id.vp_manage);
        tv_doing_red_point = findView(R.id.tv_doing_red_point);
        tv_done_red_point = findView(R.id.tv_travel_red_point);

        /*try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(ll_orders_title_layout, 10f);
        } catch (Exception e) {
            MLog.e(TAG, e.getMessage(), e);
            v_orders_line.setVisibility(View.VISIBLE);
        }*/

        orderType = TextUtils.isEmpty(getIntent().getStringExtra("orderType")) ? "my"
                : getIntent().getStringExtra("orderType");

        if (TextUtils.equals(orderType, "my"))
            setTitle(getString(R.string.manageorder_my_order));
        else {
            setTitle(getString(R.string.manageorder_order_manage));
        }
        OSUtil.setShareParam(group_chat, "scan", mContext);
        // 只有供应商有扫一扫
        if (TextUtils.equals(orderType, "supplier")) {
            group_chat.setVisibility(View.VISIBLE);
        } else {
            group_chat.setVisibility(View.INVISIBLE);
        }
        tvs.add(tv_all);
        tvs.add(tv_pay);
        tvs.add(tv_travel);
        tvs.add(tv_comments);
    }

    /**
     * 订单管理选项卡的适配器
     *
     * @author WYP
     */
    class ManageAdapter extends FragmentStatePagerAdapter {

        ArrayList<Fragment> viewList;

        public ManageAdapter(FragmentManager fm) {
            super(fm);
            viewList = new ArrayList<>();
            viewList.add(ManageFragment.newInstance(0));
            viewList.add(ManageFragment.newInstance(OrderBean.STATUS_1));
            viewList.add(ManageFragment.newInstance(OrderBean.STATUS_3));
            viewList.add(ManageFragment.newInstance(OrderBean.STATUS_5));
        }

        @Override
        public Fragment getItem(int arg0) {
            return viewList.get(arg0);
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int arg1) {
            return super.instantiateItem(arg0, arg1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    /**
     * 选项卡的点击事件
     */
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
        unTabColor();
        if (v.getId() == R.id.tv_all) {
            selectTab(tv_all, 0);
        } else if (v.getId() == R.id.tv_pay) {
            selectTab(tv_pay, 1);
        } else if (v.getId() == R.id.tv_travel) {
            selectTab(tv_travel, 2);
        } else if (v.getId() == R.id.tv_comments) {
            selectTab(tv_pay, 3);
        }

    }

    private void unTabColor() {
        tv_all.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
        tv_pay.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
        tv_travel.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
        tv_comments.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
    }

    private void selectTab(TextView tv, int index) {
        if (OSUtil.isDayTheme())
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
        else
            tv.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
        vp_manage.setCurrentItem(index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 进入时的界面
        if (status == 2) {
            unTabColor();
            selectTab(tv_pay, 1);
        } else if (status == 3) {
            unTabColor();
            selectTab(tv_travel, 2);
        } else if (status == 4) {
            unTabColor();
            selectTab(tv_comments, 3);
        } else {
            unTabColor();
            selectTab(tv_all, 0);
        }
    }

    /**
     * 显示网络断开
     */
    protected void netNotifyShow() {
        super.netNotifyShow();
        isNet = false;
    }

    /**
     * 隐藏网络断开
     */
    protected void netNotifyHide() {
        super.netNotifyHide();
        isNet = true;
    }

    /**
     * 按下键盘的返回键 确保无论什么情况从订单列表返回都是返回到“我”的界面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (TextUtils.equals(orderType, "my"))
                startActivity(new Intent("com.travel.activity.HomeActivity").putExtra("position", Constants.USERCENTER_POSITION));
            finish();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent openCameraIntent = new Intent(mContext,
                            CaptureActivity.class);
                    startActivityForResult(openCameraIntent, 0);
                }else{
                    showToast("您已禁止拍照权限");
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String ordersId = bundle.getString("result");
            if (ShopTool.isNumeric(ordersId.toString()))
                scanOrdersId(Long.parseLong(ordersId));
            else
                showToast("此订单无效！");
        }
    }

    /**
     * 扫码
     *
     * @param ordersId
     */
    private void scanOrdersId(long ordersId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("ordersId", ordersId);
        map.put("userId", UserSharedPreference.getUserId());
        NetWorkUtil.postForm(mContext, ShopConstant.ORDER_MANAGE_SCAN, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    OrderInfoDialogTool.successDialog("scan_success", mContext);
                    mContext.sendBroadcast(new Intent("refresh_orders"));
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                if (error == 1)
                    OrderInfoDialogTool.successDialog("scan_fail", mContext);
            }
        }, map);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status = 1;
    }

    /**
     * 下划线的编辑
     */
    private void initTabLine(boolean isDone) {
        tabWidth = OSUtil.getScreenWidth() / 5;
        if (isDone)
            tv_pay.setWidth(tv_pay.getWidth() < tabWidth ? tabWidth : tv_pay.getWidth());
        else
            tv_all.setWidth(tv_all.getWidth() < tabWidth ? tabWidth : tv_all.getWidth());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll_manage_line.getLayoutParams();
        lp.width = tabWidth;
        ll_manage_line.setLayoutParams(lp);
    }
}
