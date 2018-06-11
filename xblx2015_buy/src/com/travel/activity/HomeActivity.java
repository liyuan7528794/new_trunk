package com.travel.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMValueCallBack;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.VideoConstant;
import com.travel.bean.NotifyBean;
import com.travel.communication.fragment.MessageFragment;
import com.travel.fragment.CityInfoFragment;
import com.travel.fragment.DiscoverFragment;
import com.travel.fragment.FullVideoFragment;
import com.travel.fragment.HomeFragment;
import com.travel.fragment.NewMainFragment;
import com.travel.fragment.OutFragment;
import com.travel.fragment.PlayOutFragment;
import com.travel.fragment.UsercentetFragment;
import com.travel.fragment.VoteListFragment;
import com.travel.imserver.IMManager;
import com.travel.layout.CornerDialog;
import com.travel.layout.DialogTemplet;
import com.travel.lib.helper.FileLog;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.NewCameraFragment;
import com.travel.map.utils.LocationTools;
import com.travel.utils.HLLXLoginHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.travel.fragment.OutFragment;

/**
 * 主activity 总控制器 通往各个视图，传递数据
 *
 * @version 1.0
 * @date 2015-1-16
 */
public class HomeActivity extends TitleBarBaseActivity implements OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = "HomeActivity";

    private int currentFragmentIndex = -1;
    private FragmentManager fragmentManager;// 当前正在显示的Fragment

    public static final int OUT = 0; // 哪去
    public static final int CITY = 1; // 哪去
    public static final int MESSAGE = 3; // 消息
    public static final int USER_CENTER = 2; // 用户中心
    private static final int VOTE = 5; // 众投
    public static final int PUBLIC_INVERSTMENT = 4; // 众投

    @SuppressWarnings("rawtypes")
    private static final Class[] fragmentClass = {
            DiscoverFragment.class, NewMainFragment.class, UsercentetFragment.class, MessageFragment.class,
            VoteListFragment.class};

    private Fragment fragments[] = new Fragment[5];
    private RadioGroup radioGroup;
    private RadioButton liveRbtn, cityRbtn, msgRbtn, meRbtn;

    private static TextView mMessagePoint, mUserCenterPoint; // 消息页面的小红点, 用户中心的小红点

    // 弹出页相关
    private CornerDialog mCornerDialog;
    private int count;// 需要弹出的页数
    private ArrayList<NotifyBean> nbs = new ArrayList<>();

    private TextView tv_theme, homeTitle;
    private ImageView iv_message;
    private View homeTitleLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OSUtil.enableStatusBar(this, true);
        // 判断所必须的权限
        List<String> premissions = new ArrayList<>();
        // 摄像头
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        // 录音
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (premissions.size() > 0) {
            String[] s = new String[premissions.size()];
            for (int i = 0; i < s.length; i++) {
                s[i] = premissions.get(i);
            }
            ActivityCompat.requestPermissions(this, s, 1);
        }

        setContentView(R.layout.activity_home);
//        if (savedInstanceState != null) {
//            Day2NightAnimDialog dialog = new Day2NightAnimDialog(this);
//            dialog.show();
//        }
        // 标题
        ll_title.setVisibility(View.GONE);
        titleLine.setVisibility(View.GONE);
        tv_theme = findView(R.id.tv_theme);
        iv_message = findView(R.id.iv_message);
        homeTitle = findView(R.id.homeTitle);
        homeTitleLayout = findView(R.id.homeTitleLayout);
        liveRbtn = findView(R.id.navigation_live);
        cityRbtn = findView(R.id.navigation_city);
        msgRbtn = findView(R.id.navigation_message);
        meRbtn = findView(R.id.navigation_usercenter);
        if (OSUtil.isDayTheme()) {
            liveRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_live_checked_day, R.drawable.main_tab_live_unchecked_day),
                    null, null);
            liveRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.black_373F47, R.color.gray_92));
            cityRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_city_checked_day, R.drawable.home_tab_ico_video_nor),
                    null, null);
            cityRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.black_373F47, R.color.gray_92));
            msgRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_msg_checked_day, R.drawable.main_tab_msg_unchecked_day),
                    null, null);
            msgRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.black_373F47, R.color.gray_92));
            meRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_usercenter_checked_day, R.drawable.main_tab_usercenter_unchecked_day),
                    null, null);
            meRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.black_373F47, R.color.gray_92));
        } else {
            liveRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_live_checked_night, R.drawable.main_tab_live_unchecked_night),
                    null, null);
            liveRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.gray_CF, R.color.gray_92));
            cityRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_city_checked_night, R.drawable.main_tab_city_unchecked_night),
                    null, null);
            cityRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.gray_CF, R.color.gray_92));
            meRbtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    ImageDisplayTools.createDrawableSelector(
                            this, R.drawable.main_tab_usercenter_checked_night, R.drawable.main_tab_usercenter_unchecked_night),
                    null, null);
            meRbtn.setTextColor(ImageDisplayTools.createColorStateList(this, R.color.gray_CF, R.color.gray_92));
        }

//        tv_theme.setVisibility(View.VISIBLE);
        iv_message.setVisibility(View.VISIBLE);
        tv_theme.setOnClickListener(this);
        iv_message.setOnClickListener(this);
        try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(homeTitleLayout, 10f);
        } catch (Exception e) {
            ((View) findView(R.id.title_line)).setVisibility(View.VISIBLE);
            MLog.e(TAG, e.getMessage(), e);
        }

//        mMessagePoint = findView(R.id.tv_red_point_message);
        mMessagePoint = findView(R.id.tv_red_point_message1);
        mUserCenterPoint = findView(R.id.tv_red_point_my);
        ImageDisplayTools.initImageLoader(this);
        radioGroup = findView(R.id.navigation_group);
        radioGroup.setOnCheckedChangeListener(this);

        // 加载导航
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            int showPosition = savedInstanceState.getInt("position", 0);
            showFragment(showPosition);
            int btnId = 0;
            switch (showPosition) {
                case OUT:
                    btnId = R.id.navigation_live;
                    break;
                case CITY:
                    btnId = R.id.navigation_city;
                    break;
                case MESSAGE:
                    btnId = R.id.navigation_message;
                    break;
                case USER_CENTER:
                    btnId = R.id.navigation_usercenter;
                    break;
            }
            if (btnId == 0)
                return;
            radioGroup.check(btnId);
        } else {
            addFragment(CITY, false);
            addFragment(MESSAGE, false);
            addFragment(USER_CENTER, false);
            showFragment(CITY);
            radioGroup.check(R.id.navigation_city);
            defaultLoginNetWork();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userLogin();
                    //				ErrorReporter.getInstance(HomeActivity.this).upLoadBug();
                }
            }, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1) return;
        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(Manifest.permission.READ_EXTERNAL_STORAGE, permissions[i])
                    && PackageManager.PERMISSION_DENIED == grantResults[i]) {
                finish();
                IMManager.getInstance().unInit();
                FileLog.getInstance().flush();
                return;
            }
        }
    }

    /**
     * 弹出窗口
     */
    private void receiveWindow() {
        NetWorkUtil.postForm(this, ShopConstant.APP_POP, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if (data != null && data.length() != 0) {
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject popObject = data.getJSONObject(i);
                            NotifyBean nb = new NotifyBean();
                            // 类型 1:券 2:活动
                            nb.setType(popObject.optString("type"));
                            nb.setTitle(popObject.optString("title"));
                            // 弹出页显示的图片
                            nb.setShareUrl(popObject.optString("img"));
                            // 券：优惠券Id 活动：活动Id
                            nb.setId(popObject.optString("actionId"));
                            nbs.add(nb);
                        }
                        count = nbs.size();
                        if (UserSharedPreference.isLogin()) {
                            for (int i = nbs.size() - 1; i >= 0; i--)
                                if (TextUtils.equals("1", nbs.get(i).getType()))
                                    isGot(i);
                        } else
                            popWindow();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new HashMap<String, Object>());
    }

    private void isGot(final int i) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("couponId", nbs.get(i).getId());
        NetWorkUtil.postForm(this, ShopConstant.COUPON_IS_GOT, new MResponseListener() {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    if (i == 0)
                        popWindow();
                }
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                // 已领取，即不显示800券的领取
                nbs.remove(i);
                --count;
                if (i == 0 && nbs.size() != 0)
                    popWindow();
            }
        }, map);
    }

    private void popWindow() {
        View view = View.inflate(this, R.layout.dialog_receive_window, null);
        TextView tv_close = (TextView) view.findViewById(R.id.tv_close);
        ImageView iv_image = (ImageView) view.findViewById(R.id.iv_image);
        ImageDisplayTools.displayImage(nbs.get(nbs.size() - count).getShareUrl(), iv_image);
        TravelUtil.setLLParamsWidthPart(iv_image, 1, 95, 280, 381);
        int width = OSUtil.getScreenWidth() - OSUtil.dp2px(this, 100);
        mCornerDialog = new CornerDialog(this, width, LinearLayout.LayoutParams.WRAP_CONTENT, view, com.travel.shop.R.style.MyDialogStyle);
        mCornerDialog.show();
        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCornerDialog.dismiss();
                --count;
                if (count > 0)
                    popWindow();
            }
        });
        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals("1", nbs.get(nbs.size() - count).getType())) {
                    // 调领取800券的接口
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("userId", UserSharedPreference.getUserId());
                    map.put("couponId", nbs.get(nbs.size() - count).getId());
                    NetWorkUtil.postForm(HomeActivity.this, VideoConstant.COUPON_GET, new MResponseListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            super.onResponse(response);
                            if (response.optInt("error") == 0) {
                                showCenterToast("恭喜您，领取成功，已放入红钱袋！");
                                mCornerDialog.dismiss();
                                --count;
                                if (count > 0)
                                    popWindow();
                            }
                        }

                        @Override
                        protected void onErrorNotZero(int error, String msg) {
                            showCenterToast("您已参与过此活动！");
                            mCornerDialog.dismiss();
                            --count;
                            if (count > 0)
                                popWindow();
                        }
                    }, map);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("notice_bean", nbs.get(nbs.size() - count));
                    bundle.putString("tag", "activity");
                    Intent intent = new Intent();
                    intent.setAction(Constants.NOTICE_ACTION);
                    intent.setType(Constants.VIDEO_TYPE);
                    intent.putExtra("notice_bean", bundle);
                    startActivity(intent);
                    mCornerDialog.dismiss();
                    --count;
                    if (count > 0) {
                        popHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                }

            }
        });
    }

    private Handler popHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            popWindow();
        }
    };


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", currentFragmentIndex);
    }

    private void userLogin() {
        HLLXLoginHelper loginHelper = new HLLXLoginHelper(this);
        if (null != UserSharedPreference.getMobile() && null != UserSharedPreference.getPassword()) {
            loginHelper.login(UserSharedPreference.getMobile(),
                    UserSharedPreference.getPassword(), new TIMValueCallBack<String>() {

                        @Override
                        public void onError(int i, String s) {
                            showToast(s);
                            hideProgressDialog();
                        }

                        @Override
                        public void onSuccess(String s) {
                            hideProgressDialog();
                            receiveWindow();
                        }
                    });

        } else {
            loginHelper.visitorLogin();
            receiveWindow();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int showPosition = intent == null ? currentFragmentIndex
                : intent.getIntExtra("position", currentFragmentIndex);

        showFragment(showPosition);
        int btnId = 0;
        switch (showPosition) {
            case USER_CENTER:
                btnId = R.id.navigation_usercenter;
                break;
        }
        if (btnId == 0)
            return;
        radioGroup.check(btnId);
    }

    /**
     * 设置小红点的显示与否
     *
     * @param index     index 对应的位置
     * @param num       红点中显示的数量
     * @param isVisible 是否可见
     */
    public static void setShowRedPoint(int index, int num, boolean isVisible) {
        if (index != MESSAGE && index != USER_CENTER)
            return;
        if (mMessagePoint == null || mUserCenterPoint == null)
            return;

        int visibility = isVisible ? View.VISIBLE : View.GONE;
        TextView textView = index == MESSAGE ? mMessagePoint : mUserCenterPoint;
        textView.setVisibility(visibility);
        textView.setText(String.valueOf(num));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationTools.getInstans().startLocation();
        // Manager.init(this, null);
    }

    private void defaultLoginNetWork() {
        if (!UserSharedPreference.isLogin())
            return;
        String url = Constants.Root_Url + "/user/userInfo.do";
        NetWorkUtil.postForm(this, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONObject data) {
                MLog.v(TAG, "onDefaultLogin, and status fine");
            }
        }, null);
    }

    // 退出时提示
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogTemplet interDialog = new DialogTemplet(this, false, "您确定要退出吗？", "", "取消", "确定");
            interDialog.show();

            interDialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
                @Override
                public void leftClick(View view) {

                }
            });

            interDialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                @Override
                public void rightClick(View view) {
                    finish();
                    IMManager.getInstance().unInit();
                    FileLog.getInstance().flush();
                    // ActivityManager manager = (ActivityManager)
                    // getSystemService(ACTIVITY_SERVICE);
                    // manager.killBackgroundProcesses(getPackageName());
                }
            });

        }
        return super.onKeyDown(keyCode, event);
    }

    public String getAddress() {
        String address = Constants.Root_Url;
        return address;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int fragmentNumber = 0;
        switch (checkedId) {
            case R.id.navigation_live:
                fragmentNumber = OUT;
                break;
            case R.id.navigation_city:
                fragmentNumber = CITY;
                break;
            case R.id.navigation_message:
                fragmentNumber = MESSAGE;
                break;
            case R.id.navigation_usercenter:
                fragmentNumber = USER_CENTER;
                break;
            default:
                throw new IllegalStateException("wrong id, check source");
        }
        showFragment(fragmentNumber);
    }

    private void addFragment(int index, boolean show) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragments[index] == null) {
            try {
                fragments[index] = (Fragment) fragmentClass[index].newInstance();
            } catch (InstantiationException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            transaction.add(R.id.index_top, fragments[index], String.valueOf(index));
            if (!show)
                transaction.hide(fragments[index]);
        } else {
            if (show) {
                transaction.show(fragments[index]);
            }
        }
        transaction.commit();
    }

    private void showFragment(int index) {
        if (index == 1) {
            homeTitleLayout.setVisibility(View.GONE);
        } else {
            homeTitleLayout.setVisibility(View.VISIBLE);
        }
        switch (index) {
            case 0:
                homeTitle.setText("发现");
                break;
            case 1:
                homeTitle.setText("城市");
                break;
            case 2:
                homeTitle.setText("我的");
                break;
            case 3:
                homeTitle.setText("消息");
                break;
        }
        hideCurrentFragment();
        addFragment(index, true);
        MLog.v(TAG, "currentFragmentIndex is " + currentFragmentIndex + ", and index is " + index);
        currentFragmentIndex = index;
        setFragmentUserVisibleHint(currentFragmentIndex);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setFragmentUserVisibleHint(int index) {
        if (fragments[index] == null)
            return;
        fragments[index].setUserVisibleHint(true);
    }

    private void hideCurrentFragment() {
        if (currentFragmentIndex < 0)
            return;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragments[currentFragmentIndex]);
        transaction.commit();
        fragments[currentFragmentIndex].setUserVisibleHint(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserCenterPoint = null;
        mMessagePoint = null;
    }

    @Override
    public void onClick(View v) {
        if (v == tv_theme) {
            if (OSUtil.isDayTheme()) {
                OSUtil.saveThemeStatus(false);
            } else {
                OSUtil.saveThemeStatus(true);
            }
            recreate();
        } else if (v == iv_message) {
            OneFragmentActivity.startNewActivity(this, "消息", MessageFragment.class, null);
        }
    }

    public PlayOutFragment getInstance() {
        PlayOutFragment playOutFragment = (PlayOutFragment) fragments[1];
        return playOutFragment;
    }
}
