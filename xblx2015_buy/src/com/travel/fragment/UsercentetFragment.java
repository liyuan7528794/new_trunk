package com.travel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.activity.HomeActivity;
import com.travel.activity.LoginActivity;
import com.travel.activity.MyBoxRoomActivity;
import com.travel.activity.OneFragmentActivity;
import com.travel.communication.helper.ShopMessageHelper;
import com.travel.http_helper.GetCountHttp;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MessageBroadcastHelper;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.LocalFileGridFragment;
import com.travel.shop.activity.CouponChooseActivity;
import com.travel.shop.activity.ManagerOrderActivity;
import com.travel.shop.activity.PersonalHomeActivity;
import com.travel.shop.widget.SmallStoryCardView;
import com.travel.usercenter.BusinessIncomeActivity;
import com.travel.usercenter.LiveIncomeActivity;
import com.travel.usercenter.MyFollowActivity;
import com.travel.usercenter.PersonalDataActivity;
import com.travel.usercenter.PlanListActivity;
import com.travel.usercenter.SellerControlActivity;
import com.travel.usercenter.SettingActivity;
import com.travel.utils.HLLXLoginHelper;
import com.travel.video.MyHistoryLiveVideoListActivity;
import com.travel.widget.LoadingDialogGifView;

import java.util.ArrayList;

public class UsercentetFragment extends Fragment implements OnClickListener, MessageBroadcastHelper.MessageHelperCallback {
    public static final int UPDATA_VIEW = 1001;
    private HomeActivity homeActivity;
    private ImageView userHeadImg;
    private View rootView;
    private TextView userNameText, tv_red_point, tv_red_point_my, user_tv_id;
    private TextView tv_followNum, tv_followerNum, tv_liveNum;
    private FrameLayout fl_card;
    private String headImgUrl;

    private LinearLayout ll_seller_layout, ll_plan;
    private RelativeLayout business_income;
    private ArrayList<String> ordersList;
    private MessageBroadcastHelper mMessageBroadcastHelper;
    private GetCountHttp getCountHelper;

    public Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATA_VIEW:
                    try {
                        Thread.sleep(1000);
                        onStart();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_usercenter, container, false);
        homeActivity = (HomeActivity) getActivity();
        initView();
        mMessageBroadcastHelper = new MessageBroadcastHelper(getActivity(), this);
        getCountHelper = new GetCountHttp(countListener);
        return rootView;
    }

    private void initView() {
        userHeadImg = (ImageView) rootView.findViewById(R.id.user_iv_image);
        userNameText = (TextView) rootView.findViewById(R.id.user_tv_name);
        tv_red_point_my = (TextView) rootView.findViewById(R.id.tv_red_point_my);
        user_tv_id = (TextView) rootView.findViewById(R.id.user_tv_id);
        tv_red_point = (TextView) rootView.findViewById(R.id.tv_red_point);
        ll_seller_layout = (LinearLayout) rootView.findViewById(R.id.ll_seller_layout);// 卖家要显示的内容
        ll_plan = (LinearLayout) rootView.findViewById(R.id.ll_plan);// 买家要显示的内容
        business_income = (RelativeLayout) rootView.findViewById(R.id.business_income);// 电商收入
        tv_followerNum = (TextView) rootView.findViewById(R.id.followerNum);
        tv_followNum = (TextView) rootView.findViewById(R.id.followNum);
        tv_liveNum = (TextView) rootView.findViewById(R.id.liveNum);
        fl_card = (FrameLayout) rootView.findViewById(R.id.fl_card);
        SmallStoryCardView smallStoryCardView = new SmallStoryCardView(homeActivity);
        smallStoryCardView.getGo().setText("点击查看");
        fl_card.addView(smallStoryCardView.getView());
        rootView.findViewById(R.id.rl_login).setOnClickListener(this);
//        userNameText.setOnClickListener(this);
        rootView.findViewById(R.id.live_layout).setOnClickListener(this);// 我的直播
//        rootView.findViewById(R.id.usercenterMain).setOnClickListener(this);// 我的主页
        rootView.findViewById(R.id.follower_layout).setOnClickListener(this);// 我的关注
        rootView.findViewById(R.id.follow_layout).setOnClickListener(this);// 我的关注
//        rootView.findViewById(R.id.live_income).setOnClickListener(this);// 直播收入-→我的红币
        rootView.findViewById(R.id.rl_my_order).setOnClickListener(this);// 我的订单
        rootView.findViewById(R.id.my_public_vote).setOnClickListener(this);// 我的众投
//        rootView.findViewById(R.id.my_record).setOnClickListener(this);// 我的记录
        rootView.findViewById(R.id.my_box_room).setOnClickListener(this);// 收藏
        rootView.findViewById(R.id.my_red_moneybag).setOnClickListener(this);// 红钱袋
        rootView.findViewById(R.id.my_seller_control).setOnClickListener(this);// 商家后台管理
//        rootView.findViewById(R.id.usercenterOrder).setOnClickListener(this);// 商家订单
//        rootView.findViewById(R.id.usercenter_supplier).setOnClickListener(this);// 供应商订单
//        rootView.findViewById(R.id.public_vote_manage).setOnClickListener(this);// 众投管理
        ll_plan.setOnClickListener(this);// 行程安排
        rootView.findViewById(R.id.usercenterSetting).setOnClickListener(this);// 设置
//        business_income.setOnClickListener(this);

        ordersList = new ArrayList<>();
    }

    public void initNumData() {
        if (UserSharedPreference.isLogin()) {
            getCountHelper.getVideoCount(UserSharedPreference.getUserId(), homeActivity);
            getCountHelper.getFollowerCount(UserSharedPreference.getUserId(), homeActivity);
            getCountHelper.getFollowCount(UserSharedPreference.getUserId(), homeActivity);
        } else {
            LoadingDialogGifView.getInstance(homeActivity).hideProcessDialog(0);
            tv_followerNum.setText("0");
            tv_followNum.setText("0");
            tv_liveNum.setText("0");
        }
    }

    @Override
    public void onResume() {
        ll_seller_layout.setVisibility(View.GONE);
        business_income.setVisibility(View.GONE);
        loadFromDB();
        initNumData();
        mMessageBroadcastHelper.registerMessageCommingReceiver();
        super.onResume();
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(HLLXLoginHelper.ACTION_LOGIN_SUCCESS);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mLoginSuccessReceiver, filter);
    }

    private BroadcastReceiver mLoginSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 登录成功
            loadFromDB();
            initNumData();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLoginSuccessReceiver);
        mMessageBroadcastHelper.unRegisterMessageCommingReceiver();
    }

    void loadFromDB() {
        if (UserSharedPreference.isLogin()) {

            if (TextUtils.equals("2", UserSharedPreference.getUserType())) {
                ll_seller_layout.setVisibility(View.VISIBLE);
                ll_plan.setVisibility(View.GONE);
            }else {
//                ll_plan.setVisibility(View.VISIBLE);
            }
            SharedPreferences preferences = homeActivity.getSharedPreferences("user", Context.MODE_PRIVATE);
            String user_id = preferences.getString("user_id", "");
            String nickname = preferences.getString("user_nickname", "");
            headImgUrl = preferences.getString("user_headimg", "");
            userNameText.setText(nickname);
            user_tv_id.setText("ID:" + (TextUtils.equals("-1", preferences.getString("ctsCid","-1"))
                    ? "——" : preferences.getString("ctsCid","-1")));
            if (headImgUrl != null && !"".equals(headImgUrl)) {
                ImageDisplayTools.displayHeadImage(headImgUrl, userHeadImg);
            } else
                userHeadImg.setImageResource(R.drawable.common_pic_user);

            // 查询数据库
            // 我的订单
            int count1 = select("my");
            //            tv_red_point_my.setText(count1 + "");
            if (count1 != 0)
                tv_red_point_my.setVisibility(View.VISIBLE);
            else
                tv_red_point_my.setVisibility(View.GONE);
            // 订单管理
            int count2 = select("business");
            //            tv_red_point.setText(count2 + "");
            if (count2 != 0)
                tv_red_point.setVisibility(View.VISIBLE);
            else
                tv_red_point.setVisibility(View.GONE);
            int count = count1 + count2;
            if (count != 0)
                HomeActivity.setShowRedPoint(HomeActivity.USER_CENTER, count, true);
            else
                HomeActivity.setShowRedPoint(HomeActivity.USER_CENTER, count, false);
        } else {
            userNameText.setText("点击登录");
            user_tv_id.setText("ID:——");
            userHeadImg.setImageResource(R.drawable.common_pic_user);
            tv_red_point_my.setVisibility(View.GONE);
            tv_red_point.setVisibility(View.GONE);
            HomeActivity.setShowRedPoint(HomeActivity.USER_CENTER, 0, false);
        }
        if (!OSUtil.isDayTheme())
            userHeadImg.setColorFilter(TravelUtil.getColorFilter(homeActivity));
    }

    /**
     * 搜索当前数据库中相应身份的变化的订单个数
     *
     * @param flag
     * @return
     */
    private int select(String flag) {
        ordersList.clear();
        ShopMessageHelper hepler = new ShopMessageHelper(homeActivity);
        Cursor c = hepler.query(ShopMessageHelper.TABLENAME_MESSAGE, flag, UserSharedPreference.getUserId());
        if (c.moveToFirst()) {
            do {
                ordersList.add(c.getString(c.getColumnIndex("ordersId")));
            } while (c.moveToNext());
        }
        c.close();
        hepler.close();
        return ordersList.size();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();

        if (!UserSharedPreference.isLogin()) {
            intent.setClass(homeActivity, LoginActivity.class);
            startActivity(intent);
            return;
        }

        // 设置
        if (v.getId() == R.id.usercenterSetting) {
            intent.setClass(homeActivity, SettingActivity.class);
            startActivity(intent);
            return;
        }

        switch (v.getId()) {
            case R.id.live_layout:// 我的直播
                intent.setClass(homeActivity, MyHistoryLiveVideoListActivity.class);
                break;
            case R.id.rl_login:// 我的主页
                PersonalHomeActivity.actionStart(homeActivity, true, UserSharedPreference.getUserId(), "");
                break;
            case R.id.follower_layout:// 我的关注
                intent.setClass(homeActivity, MyFollowActivity.class);
                intent.putExtra("type", 2);
                break;
            case R.id.follow_layout:// 我的关注
                intent.setClass(homeActivity, MyFollowActivity.class);
                intent.putExtra("type", 1);
                break;
            case R.id.live_income:// 直播收入-→我的红币
                intent.setClass(homeActivity, LiveIncomeActivity.class);
                break;
            case R.id.rl_my_order:// 我的订单
                ManagerOrderActivity.actionStart(homeActivity, "my");
                break;
            case R.id.my_public_vote:// 我的众投
                Bundle bundle = new Bundle();
                //                bundle.putString(PublicVoteFragment.TYPE, "my");
                //                OneFragmentActivity.startNewActivity(getActivity(), "", PublicVoteFragment.class, bundle);
                bundle.putString(VoteListFragment.TYPE, "my");
                OneFragmentActivity.startNewActivity(getActivity(), "", VoteListFragment.class, bundle);
                break;
            case R.id.my_record:// 我的记录
                Bundle localFileGridFragmentBundle = new Bundle();
                localFileGridFragmentBundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE, true);
                localFileGridFragmentBundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_LIVE, true);
                localFileGridFragmentBundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_SELECTED, false);
                localFileGridFragmentBundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE_FROM_LOCAL, true);
                OneFragmentActivity.startNewActivity(getActivity(), "", LocalFileGridFragment.class,
                        localFileGridFragmentBundle);
                break;
            case R.id.usercenterOrder:// 商家订单
                ManagerOrderActivity.actionStart(homeActivity, "business");
                break;
            case R.id.usercenter_supplier:// 供应商订单
                ManagerOrderActivity.actionStart(homeActivity, "supplier");
                break;
            case R.id.business_income:// 电商收入
                intent.setClass(homeActivity, BusinessIncomeActivity.class);
                break;
            case R.id.public_vote_manage:// 众投管理
                Bundle bundle1 = new Bundle();
                //                bundle1.putString(PublicVoteFragment.TYPE, "business");
                //                OneFragmentActivity.startNewActivity(getActivity(), "", PublicVoteFragment.class, bundle1);
                bundle1.putString(VoteListFragment.TYPE, "business");
                OneFragmentActivity.startNewActivity(getActivity(), "", VoteListFragment.class, bundle1);
                break;
            case R.id.my_box_room:// 收藏
                intent.setClass(homeActivity, MyBoxRoomActivity.class);
                break;
            case R.id.my_red_moneybag:// 红钱袋
                intent.setClass(homeActivity, CouponChooseActivity.class);
                intent.putExtra("tag", 1);
                break;
            case R.id.user_tv_name:
                intent.setClass(homeActivity, PersonalDataActivity.class);
                break;
            case R.id.my_seller_control:
                intent.setClass(homeActivity, SellerControlActivity.class);
                break;
            case R.id.ll_plan:
                intent.setClass(homeActivity, PlanListActivity.class);
                break;
            default:
                break;
        }

        if (intent.getComponent() != null) {
            startActivity(intent);
        }
    }

    GetCountHttp.CountListener countListener = new GetCountHttp.CountListener() {
        @Override
        public void OnGetVideoCount(boolean isResult, int videoCount) {
            if (!isResult)
                return;
            // 直播数
            tv_liveNum.setText(videoCount + "");
        }

        @Override
        public void OnGetFollowerCount(boolean isResult, int followCount) {
            if (!isResult)
                return;
            // 粉丝数
            tv_followerNum.setText(followCount + "");
        }

        @Override
        public void OnGetFollowCount(boolean isResult, int followerCount) {
            if (!isResult)
                return;
            tv_followNum.setText(followerCount + "");
        }

        @Override
        public void OnGetIsFollow(boolean isResult, boolean isFollowStatus) {

        }

        @Override
        public void onGetPlace(boolean isResult, String place) {

        }

        @Override
        public void onFollowControl(boolean isResult) {

        }

    };

    @Override
    public void onMessageComming() {
        loadFromDB();
    }
}
