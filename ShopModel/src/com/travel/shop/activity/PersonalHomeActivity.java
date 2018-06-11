package com.travel.shop.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mylhyl.crlayout.RefreshScrollviewLayout;
import com.travel.ShopConstant;
import com.travel.bean.ReasonBean;
import com.travel.layout.DialogTemplet;
import com.travel.lib.fragment_interface.Functions;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.adapter.ImageViewPagerAdapter;
import com.travel.shop.bean.MyPageBean;
import com.travel.shop.fragment.PersonalHomeAboutFragment;
import com.travel.shop.fragment.PersonalHomeStoryFragment;
import com.travel.shop.fragment.PersonalHomeVideoFragment;
import com.travel.shop.helper.PesonalSelectImageHelper;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/1/9.
 */

public class PersonalHomeActivity extends TitleBarBaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        RefreshScrollviewLayout.OnLoadListener {
    private static final String TAG = "PersonalHomeActivity";
    public static final String IS_HOME_PAGER = "is_home_pager";
    private Context mContext;
    // 背景图片相关
    // 判断是否为我的主页界面， true-- 是我的主页界面
    private boolean isHomePager;
    private List<ReasonBean> mPhotoUrls = new ArrayList<>();
    private ImageViewPagerAdapter mAdapter;
    // 主页照片轮滚图
    private ViewPager mViewPager;
    // 背景图片小圆点
    private int heightViewPage = 0;
    private int heightTitle = 0;

    private PesonalSelectImageHelper selectImageHelper;

    private int mPosition;// 当前操作图片的位置
    private String toId;// 被浏览人的id
    private String attentionStatus = "2";// 1:关注 2:取消关注或未关注
    private PersonalHomeActivity.RefreshReceiver rr;
    private HashMap<String, Object> map;

    private MyPageBean mPage;
    private DialogTemplet dialog;

    // 主页信息相关
    private View titleReLayout;
    private Button btn_attention;
    private ImageView iv_my_photo, sv_iv_attention;
    private TextView tv_my_name, tv_my_address, sv_tx_attention;
    private LinearLayout sv_my_attention, tv_my_private;
    private RelativeLayout ll_other;
    private RefreshScrollviewLayout swipeLayout;

    // 故事、视频相关
    private LinearLayout tab_story, tab_live, tab_about;
    private TextView tv_story, tv_live, tv_about;
    private View storyLine, liveLine, aboutLine;
    private ArrayList<TextView> tvs = new ArrayList<>();
    private PersonalHomeStoryFragment storyFragment;
    private PersonalHomeVideoFragment videoFragment;
    private PersonalHomeAboutFragment aboutFragment;
    private int currentTag = -1;
    private Fragment currentFragment = null;

    // 头像更改相关
    private final String oldImg = UserSharedPreference.getUserHeading();// 修改之前的头像

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    // 获取基本信息（点击关注后需要更新关注的人数）
                    getPageData();
                    break;
                case 4:
                    // 确定取消关注
                    attentionStatus = "2";
                    cancleAttention();
                    break;
                default:
                    break;
            }
        }
    };

    public static void actionStart(Context context, boolean isHomePager, String id, String nickName) {
        Intent intent = new Intent(context, PersonalHomeActivity.class);
        intent.putExtra(IS_HOME_PAGER, isHomePager);
        intent.putExtra("id", id);
        intent.putExtra("nick_name", nickName);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heightViewPage = OSUtil.getScreenWidth() * 201 / 360;
        heightTitle = OSUtil.dp2px(this, 44);
        setContentView(R.layout.activity_personal_home);
        hideOriginTitleLayout();
        storyFragment = new PersonalHomeStoryFragment();
        videoFragment = new PersonalHomeVideoFragment();
        aboutFragment = new PersonalHomeAboutFragment();
        selectImageHelper = new PesonalSelectImageHelper(this);
        initView();
        setListener();

        videoFragment.setUserId(toId);
        storyFragment.setUserId(toId);

        swipeLayout.setRefreshing(true);
        swipeLayout.setRefreshing(false);

        // 我的主页
        if (isHomePager || toId.equals(UserSharedPreference.getUserId())) {
            initHomePage();
            tv_my_private.setVisibility(View.GONE);
            btn_attention.setVisibility(View.GONE);
            sv_my_attention.setVisibility(View.GONE);
            ll_other.setVisibility(View.GONE);
            // 他人的主页
        } else {
            initShopDetail();
            tv_my_private.setVisibility(View.VISIBLE);
            btn_attention.setVisibility(View.VISIBLE);
            sv_my_attention.setVisibility(View.VISIBLE);
            ll_other.setVisibility(View.VISIBLE);
        }

        mViewPager.setAdapter(mAdapter);
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getPageData();
        }

    }

    private void initView() {
        mContext = this;
        titleReLayout = findView(R.id.titleReLayout);
        btn_attention = (Button) titleReLayout.findViewById(R.id.rightButton);
        titleReLayout.setY(-heightTitle);
        swipeLayout = findView(R.id.scroll);
        swipeLayout.setLoadingLayout((LinearLayout) findView(R.id.ll));

        mViewPager = findView(R.id.vp_bg);
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
        params.height = heightViewPage;

        iv_my_photo = findView(R.id.iv_my_photo);
        tv_my_name = findView(R.id.tv_my_name);
        tv_my_address = findView(R.id.tv_my_address);
        sv_my_attention = findView(R.id.sv_my_attention);
        sv_iv_attention = findView(R.id.sv_iv_attention);
        sv_tx_attention = findView(R.id.sv_tx_attention);
        tv_my_private = findView(R.id.tv_my_private);
        ll_other = findView(R.id.ll_other);

        tab_story = findView(R.id.tab_story);
        tab_live = findView(R.id.tab_live);
        tab_about = findView(R.id.tab_about);
        tv_story = findView(R.id.tv_story);
        tv_live = findView(R.id.tv_live);
        tv_about = findView(R.id.tv_about);
        storyLine = findView(R.id.storyLine);
        liveLine = findView(R.id.liveLine);
        aboutLine = findView(R.id.aboutLine);
        tvs.add(tv_story);
        tvs.add(tv_live);
        tvs.add(tv_about);

        isHomePager = getIntent().getBooleanExtra(IS_HOME_PAGER, true);
        toId = isHomePager ? UserSharedPreference.getUserId() : getIntent().getStringExtra("id");
        if (toId.startsWith("us_"))
            toId = toId.replace("us_", "");
        mPage = new MyPageBean();
        mAdapter = new ImageViewPagerAdapter(mPhotoUrls);

        ImageDisplayTools.initImageLoader(mContext);

        // 注册广播
        rr = new PersonalHomeActivity.RefreshReceiver();
        registerReceiver(rr, new IntentFilter(ShopConstant.REFRESH_GOODSINFO));

    }

    @Override
    protected void addFunction(Functions functions) {
        // 添加Fragment中的回调
        functions.addFunction(new Functions.FunctionWithParamNoResult(PersonalHomeVideoFragment.STOP_REFRESH) {
            @Override
            public void function(Object o) {
                stopRefresh((List) o);
            }
        });
        functions.addFunction(new Functions.FunctionWithParamNoResult(PersonalHomeStoryFragment.STOP_REFRESH) {
            @Override
            public void function(Object o) {
                stopRefresh((List) o);
            }
        });
    }

    private void stopRefresh(List list) {
        hideRefreshAnimation();

        if (list != null) {
            swipeLayout.isCanLoad(list.size());
        }
    }

    private void setListener() {
        swipeLayout.setOnLoadListener(this);
        swipeLayout.setOnRefreshListener(this);

        iv_my_photo.setOnClickListener(this);
        btn_attention.setOnClickListener(this);
        sv_my_attention.setOnClickListener(this);
        tv_my_private.setOnClickListener(this);
        tab_story.setOnClickListener(this);
        tab_live.setOnClickListener(this);
        tab_about.setOnClickListener(this);

        selectImageHelper.setListener(new PesonalSelectImageHelper.Listener() {
            @Override
            public void onUploadImageSuccess(String filePath, int id) {
                addOnePhoto(filePath, id);
                mViewPager.setCurrentItem(mPosition);
            }

            @Override
            public void onImageResultBitmap(Bitmap bitmap) {

            }
        });

        swipeLayout.setOnRefreshScrollListener(new RefreshScrollviewLayout.OnRefreshScrollListener() {
            @Override
            public void onScrollChanged(ScrollView scroll, int x, int y, int oldx, int oldy) {
                if (y <= (heightTitle + heightViewPage) && y >= (heightViewPage - heightTitle)) {
                    int titleY = y - heightViewPage;
                    Log.e(TAG, "titleY:" + titleY);
                    if (titleY > 0 || titleY < -heightTitle)
                        return;
                    titleReLayout.setY(titleY);
                    return;
                }
                if (y > (heightTitle + heightViewPage)) {
                    Log.e(TAG, "大于:" + y);
                    titleReLayout.setY(0);
                    return;
                }
                if (y < (heightViewPage - heightTitle)) {
                    Log.e(TAG, "小于:" + y);
                    titleReLayout.setY(-heightTitle);
                    return;
                }

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
        if (v == btn_attention || v == sv_my_attention) {
            // 点击关注
            if (UserSharedPreference.isLogin()) {
                if (attentionStatus.equals("2")) {
                    attentionStatus = "1";
                    controlAttention();
                } else
                    mHandler.sendEmptyMessage(4);
            } else {
                startActivity(new Intent(ShopConstant.LOG_IN_ACTION).putExtra("refresh", "refresh"));
            }
        } else if (v == tv_my_private) {
            // 私聊
            if (UserSharedPreference.isLogin()) {
                Intent intent = new Intent(ShopConstant.COMMUNICATION_ACTION);
                intent.putExtra("id", toId);
                intent.putExtra("nick_name", mPage.getMyName());
                intent.putExtra("img_url", mPage.getMyImgUrl());
                startActivity(intent);
            } else {
                startActivity(new Intent(ShopConstant.LOG_IN_ACTION).putExtra("refresh", "refresh"));
            }
        } else if (v.getId() == R.id.tab_story) {
            addFragment(0);
        } else if (v.getId() == R.id.tab_live) {
            addFragment(1);
        } else if (v.getId() == R.id.tab_about) {
            addFragment(2);
        } else if (v.getId() == R.id.iv_my_photo) {
            if (isHomePager) {
                Intent intent = new Intent();
                intent.setAction("com.travel.usercenter.PersonalDataActivity");
                startActivity(intent);
            }
        }
    }

    private void addFragment(int index) {
        if (currentTag != index) {
            hideRefreshAnimation();

            tv_story.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            tv_live.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            tv_about.setTextColor(ContextCompat.getColor(mContext, R.color.gray_9));
            liveLine.setVisibility(View.INVISIBLE);
            storyLine.setVisibility(View.INVISIBLE);
            aboutLine.setVisibility(View.INVISIBLE);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (index == 0) {
                if (!storyFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fl_fragment_container, storyFragment).hide(storyFragment).show(storyFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(storyFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                if (OSUtil.isDayTheme())
                    tv_story.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
                else
                    tv_story.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                storyLine.setVisibility(View.VISIBLE);
                currentFragment = storyFragment;
            } else if (index == 1) {
                if (!videoFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fl_fragment_container, videoFragment).hide(videoFragment).show(videoFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(videoFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                if (OSUtil.isDayTheme())
                    tv_live.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
                else
                    tv_live.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                liveLine.setVisibility(mPage.getUserType() == 2 ? View.VISIBLE : View.INVISIBLE);
                currentFragment = videoFragment;
            } else if (index == 2) {
                if (!aboutFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fl_fragment_container, aboutFragment).hide(aboutFragment).show(aboutFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(aboutFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
                if (OSUtil.isDayTheme())
                    tv_about.setTextColor(ContextCompat.getColor(mContext, R.color.black_3));
                else
                    tv_about.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                aboutLine.setVisibility(View.VISIBLE);
                currentFragment = aboutFragment;
            }
        }
        currentTag = index;
    }

    private void hideRefreshAnimation() {
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
        swipeLayout.setLoading(false);
    }

    @Override
    public void onRefresh() {
        getPageData();
        if (currentTag == 0) {
            storyFragment.refresh();
        } else if (currentTag == 1) {
            videoFragment.refresh();
        } else {
            hideRefreshAnimation();
        }
    }

    @Override
    public void onLoad() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentTag == 0) {
                    storyFragment.load();
                } else if (currentTag == 1) {
                    videoFragment.load();
                } else {
                    hideRefreshAnimation();
                }
            }
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(rr);
    }

    /**
     * 接到刷新页面的广播
     *
     * @author wyp
     */
    class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getIntent().getStringExtra("id").equals(UserSharedPreference.getUserId())) {
                isHomePager = true;
            } else {
                isHomePager = false;
            }
            // 我的主页
            if (isHomePager) {
                initHomePage();
                tv_my_private.setVisibility(View.GONE);
                btn_attention.setVisibility(View.GONE);
                sv_my_attention.setVisibility(View.GONE);
                ll_other.setVisibility(View.GONE);
                // 他人的主页
            } else {
                initShopDetail();
                tv_my_private.setVisibility(View.VISIBLE);
                btn_attention.setVisibility(View.VISIBLE);
                sv_my_attention.setVisibility(View.VISIBLE);
                ll_other.setVisibility(View.VISIBLE);
            }
            getPageData();
        }

    }

    // 初始化个人主页
    private void initHomePage() {
        setTitle(getString(R.string.minehome_my_page));
        //        mAdapter.setOnItemClickListener(listener);
    }

    // 初始化店铺详情的相关数据
    private void initShopDetail() {
        setTitle(getIntent().getStringExtra("nick_name") + getString(R.string.minehome_page));
        //        mAdapter.setOnItemClickListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            MLog.v(TAG, "onActivityResult, and requestCode is " + requestCode + ", and cancled");
            return;
        }
        switch (requestCode) {
            case PesonalSelectImageHelper.REQUEST_IMAGE_PICK:
                // 图片裁剪
                selectImageHelper.onImagePickResult(resultCode, data);
                break;
            case PesonalSelectImageHelper.REQUEST_IMAGE_CROP:
                selectImageHelper.onImageCropResult(resultCode, data, mPhotoUrls.get(mPosition).getReasonId());
                break;
            default:
                MLog.e(TAG, "errnor, and requestCode is " + requestCode);
                break;
        }
    }


    /**
     * 修改图片listener
     */
    private ImageViewPagerAdapter.ViewPagerOnItemClickListener listener = new ImageViewPagerAdapter.ViewPagerOnItemClickListener() {

        @Override
        public void onItemClick(ViewPager viewPager, View view, int position) {
            mPosition = position;
            MLog.v(TAG, "ViewPager Click, and position is " + position);
            if (isHomePager) {
                MLog.v(TAG, "need add one Photo");
                selectImageHelper.pickFromGallery();
            }
        }
    };

    /**
     * 添加图片
     *
     * @param photo
     * @param id
     */
    private void addOnePhoto(String photo, int id) {
        MLog.v(TAG, "add one photo, and photopath is " + photo);
        ReasonBean mPhoto = new ReasonBean();
        mPhoto.setReasonId(id);
        mPhoto.setReason(photo);
        mPhoto.setFlag(-1);
        mPhotoUrls.set(mPosition, mPhoto);
        mAdapter.notifyDataSetChanged();
    }


    /**
     * 获取主页数据
     */
    private void getPageData() {
        map = new HashMap<>();
        // 查看别人的主页
        if (!isHomePager)
            map.put("id", toId);
        NetWorkUtil.postForm(mContext, isHomePager ? ShopConstant.MY_PAGE : ShopConstant.OTHER_PAGE, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                MyPageBean myPage = new MyPageBean();
                try {
                    ReasonBean photo = new ReasonBean();
                    photo.setReason(data.optString("imgUrl"));
                    photo.setFlag(-1);
                    mPhotoUrls.add(photo);
                    mAdapter.notifyDataSetChanged();
                    // 背景图片
                    /*JSONArray backImgsArray = data.getJSONArray("backImgs");
                    mPhotoUrls.clear();
                    for (int i = 0; i < backImgsArray.length(); i++) {
                        ReasonBean photo = new ReasonBean();
                        JSONObject backImgsObject = backImgsArray.getJSONObject(i);
                        // 图片id
                        photo.setReasonId(backImgsObject.optInt("id"));
                        // 图片地址
                        photo.setReason(data.optString("imgUrl"));
                        photo.setFlag(-1);
                        mPhotoUrls.add(photo);
                        break;// 因为以前背景可以多个，现在只一个，故加此句
                    }
                    // 背景图片
                    mAdapter.notifyDataSetChanged();*/
                    // 头像
                    myPage.setMyImgUrl(data.optString("imgUrl"));
                    // 名字
                    myPage.setMyName(data.optString("nickName"));
                    // 所在地
                    myPage.setMyAddress(data.optString("place"));
                    // 关注
                    myPage.setMyAttention(data.optBoolean("followStatus"));
                    attentionStatus = myPage.isMyAttention() ? "1" : "2";
                    // 用户类型 1:卖家
                    myPage.setUserType(data.optInt("userType"));
                    // 个人介绍
                    myPage.setMyInfo(data.optString("myIntroduction"));

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mPage = myPage;
                    // 更新UI
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = myPage;
                    mHandler.sendMessage(msg);

                    setMainData();
                    hideRefreshAnimation();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                attentionStatus = attentionStatus == "1" ? "2" : "1";
                hideRefreshAnimation();
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                super.onErrorNotZero(error, msg);
                attentionStatus = attentionStatus == "1" ? "2" : "1";
                hideRefreshAnimation();
            }
        }, map);

    }

    /**
     * 更新UI
     */
    private void setMainData() {

        // 头像
        ImageDisplayTools.displayHeadImage(mPage.getMyImgUrl(), iv_my_photo);
        if (!OSUtil.isDayTheme())
            iv_my_photo.setColorFilter(TravelUtil.getColorFilter(mContext));
        // 名字
        tv_my_name.setText(mPage.getMyName());
        // 所在地
        tv_my_address.setText(mPage.getMyAddress());
        // 关注
        btn_attention.setVisibility(View.VISIBLE);
        Drawable img = ContextCompat.getDrawable(mContext, mPage.isMyAttention() ? R.drawable.nav_icon_check_black : R.drawable.nav_icon_add_black);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        btn_attention.setCompoundDrawables(img, null, null, null);
        btn_attention.setText(mPage.isMyAttention() ? "已关注" : "关注");

        sv_iv_attention.setImageResource(mPage.isMyAttention() ? R.drawable.nav_icon_check_white : R.drawable.nav_icon_add_white);
        sv_tx_attention.setText(mPage.isMyAttention() ? "已关注" : "关注");

        if (mPage.isMyAttention()) {
            ll_other.setVisibility(View.VISIBLE);
        } else {
            ll_other.setVisibility(View.GONE);
        }

        aboutFragment.setAbout(mPage.getMyInfo());

        if (mPage.getUserType() == 2) {
            findViewById(R.id.tab_story).setVisibility(View.GONE);// TODO 因为bug的原因，暂时隐藏
            findViewById(R.id.tab_about).setVisibility(View.VISIBLE);
            if (currentTag == 0 || currentTag == -1)
                currentFragment = storyFragment;
            else if (currentTag == 1)
                currentFragment = videoFragment;
            else if (currentTag == 2)
                currentFragment = aboutFragment;
            addFragment(currentTag < 2 ? 1 : currentTag);
//            addFragment(currentTag < 0 ? 0 : currentTag);// TODO 因为bug的原因，暂时隐藏
        } else {
            findViewById(R.id.tab_story).setVisibility(View.GONE);
            findViewById(R.id.tab_about).setVisibility(View.GONE);
            liveLine.setVisibility(View.INVISIBLE);
            currentFragment = videoFragment;
            addFragment(1);
        }
    }

    /**
     * 关注的操作
     */
    private void controlAttention() {
        map = new HashMap<>();
        map.put("myId", UserSharedPreference.getUserId());
        map.put("toId", toId);
        map.put("type", 1);// 1:关注人 2:关注群
        map.put("status", attentionStatus);
        NetWorkUtil.postForm(mContext, ShopConstant.ATTENTION, new MResponseListener(mContext) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    sendBroadcast(new Intent(ShopConstant.IS_ATTENTION).putExtra("attentionStatus", attentionStatus));
                    // 更新关注的人数
                    mHandler.sendEmptyMessage(3);
                }
            }
        }, map);
    }

    /**
     * 询问用户是否取消关注
     */
    private void cancleAttention() {
        dialog = new DialogTemplet(mContext, false,
                getString(R.string.minehome_cancle_attention), "",
                getString(R.string.minehome_not_cancle),
                getString(R.string.minehome_yes));
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
                controlAttention();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.equals(oldImg, UserSharedPreference.getUserHeading())) {
            // 头像
            ImageDisplayTools.displayHeadImage(UserSharedPreference.getUserHeading(), iv_my_photo);
            if (!OSUtil.isDayTheme())
                iv_my_photo.setColorFilter(TravelUtil.getColorFilter(mContext));
            ReasonBean rb = new ReasonBean();
            rb.setReason(UserSharedPreference.getUserHeading());
            rb.setFlag(-1);
            mPhotoUrls.set(0, rb);
            mAdapter.notifyDataSetChanged();
        }
    }
}
