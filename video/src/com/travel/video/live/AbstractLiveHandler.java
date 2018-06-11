package com.travel.video.live;

import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMMessage;
import com.tencent.TIMTextElem;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.LiveHelper;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.views.customviews.HeartLayout;
import com.travel.communication.entity.UserData;
import com.travel.communication.wrapper.OnClickListenerWrapper;
import com.travel.layout.HorizontalListView;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;
import com.travel.video.adapter.BarrageAdapter;
import com.travel.video.adapter.HorizontalHeadListViewAdapter;
import com.travel.video.bean.BarrageInfo;
import com.travel.video.gift.GiftBean;
import com.travel.video.gift.GiftRelativeLayout;
import com.travel.video.help.LiveHttpRequest;
import com.travel.VideoConstant;
import com.travel.video.widget.BlurCoverTaskView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于直播逻辑控制的抽象类,
 * 此处放置了方法与变量
 *
 * FIXME 需要再次重构, 重构程度不好
 * Created by ldkxingzhe on 2016/8/17.
 */
public abstract class AbstractLiveHandler extends Handler{
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractLiveHandler";

    private static final int PUBLIC_INT = 20;

    protected RelativeLayout barrageLayout;//弹幕显示区域布局
    protected ListView barrageListView;//弹幕列表
    //直播主页面Activity
    protected HostWindowActivity mHostWindowActivity = null;
    protected Button barrageSendButton;//发送弹幕
    protected RelativeLayout barrageSendLayout;//发送弹幕布局
    protected EditText barrageSendEdit;//弹幕编辑框

    protected HeartLayout mHeartLayout;
    protected LinearLayout zanLinearLayout;
    protected ImageView zan;//点赞
    protected TextView zanNum;//点赞数
    protected Float praiseAnimaHeight;//点赞动画的初始高度
    protected RelativeLayout zanLayout;//点赞动画区域布局
    protected long mPraiseNum = 0;//总共点赞数
    protected TextView totalNum;//累计人数
    protected TextView lookingNum;//当前观看人数
    //UI操作显示布局
    protected RelativeLayout reLayout;
    LiveHttpRequest mLiveHttpRequest;

    private BarrageAdapter barrageAdapter;//弹幕适配器
    private boolean isEnd = true;//判断列表显示是否在底部
    protected List<BarrageInfo> barrageInfos =new ArrayList<BarrageInfo>();//弹幕列表

    /*用户进出房间显示控件及数据*/
    protected HorizontalListView watchListView;//观看人数列表ListView
    private HorizontalHeadListViewAdapter mWatchAdapter;
    protected List<UserData> mInRoomingList = new ArrayList<UserData>();

    //礼物显示
    protected GiftRelativeLayout giftRelativeLayout;

    private static final int[] LAYOUT_ID = {R.id.fl_index_0, R.id.fl_index_1, R.id.fl_index_2};
    FrameLayout[] mSmallVideoContainer;
    FrameLayout mSmallVideoContainerLayout;
    BlurCoverTaskView[] mBlurCoverTaskView;

    // 直播控制(闪光灯, 切换摄像头, 静音)
    protected ImageView changeCamera;//摄像头转换
    protected ImageView changeLight;//闪光灯
    protected ImageView soundOn;//打开关闭声音

    protected String userType = "";//用户类型
    protected long mTotalWatchNum = 0; // 总的观看数

    Map<String, UserData> mInLivingUser = new HashMap<>();

    public AbstractLiveHandler(HostWindowActivity hostWindowActivity){
        mHostWindowActivity = hostWindowActivity;
        praiseAnimaHeight = (float) (OSUtil.getScreenHeight());
        mSmallVideoContainer = new FrameLayout[Constants.VIDEO_VIEW_MAX - 1];
        mBlurCoverTaskView = new BlurCoverTaskView[Constants.VIDEO_VIEW_MAX - 1];
    }

    /**
     * 添加一条弹幕
     */
    public void addOneMessageToUI(BarrageInfo barrageInfo){
        barrageInfos.add(barrageInfo);
        barrageAdapter.notifyDataSetChanged();
        barrageListView.setSelection(barrageInfos.size());
    };

    public void setLiveBaseInfo(LiveHttpRequest.LiveBaseInfo baseInfo){
        mTotalWatchNum = baseInfo.getTotalWatchNum();
//        mPraiseNum = baseInfo.getPraiseNum();
        refreshBaseInfo();
    }

    public void setmPraiseNum(int num){
        this.mPraiseNum = num;
    }

    void refreshBaseInfo() {
        lookingNum.setText(String.valueOf(mInRoomingList.size()));
        mTotalWatchNum = Math.max(mTotalWatchNum, mInRoomingList.size());
        totalNum.setText(String.valueOf(mTotalWatchNum));
        zanNum.setText(String.valueOf(mPraiseNum));
    }

    /**
     * 添加一条赞
     */
    public void addOnePraiseToUI(){
        //点赞动画
        mHeartLayout.addFavor();
        zanNum.setText(String.valueOf(++mPraiseNum));
    }

    public void showGif(GiftBean giftBean){
        giftRelativeLayout.showGiftAnimation(giftBean);
    }

    /* 用户进入房间 */
    public void onUserEnterRoom(UserData userData){
        for(UserData tmpUserData : mInRoomingList){
            if(userData.getId().equals(tmpUserData.getId())){
                return;
            }
        }
        mInRoomingList.add(userData);
        mWatchAdapter.notifyDataSetChanged();
        mTotalWatchNum++;
        refreshBaseInfo();
    }
    /* 用户离开房间 */
    public void onUserExistRoom(UserData userData){
        for(UserData tmpUserData : mInRoomingList){
            if(tmpUserData.getId().equals(userData.getId())){
                mInRoomingList.remove(tmpUserData);
                mWatchAdapter.notifyDataSetChanged();
                refreshBaseInfo();
                break;
            }
        }
    }

    /**
     * 设置群组成员
     */
    public void setGroupMembers(List<UserData> members){
        MLog.v(TAG, "onSetGroupMember, and members.size is %d", members.size());
        mInRoomingList.clear();
        mInRoomingList.addAll(members);
        mWatchAdapter.notifyDataSetChanged();
        refreshBaseInfo();
    }

    protected void sendMessage(String message){
        if(message.getBytes().length > 160){
            Toast.makeText(mHostWindowActivity, "发送的消息过长", Toast.LENGTH_SHORT).show();
            return;
        }
        TIMMessage timMessage = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(message);
        timMessage.addElement(elem);
        mHostWindowActivity.mLiveHelper.sendGroupText(timMessage);
    }

    public void onDestroy(){
        if(giftRelativeLayout!=null){
            giftRelativeLayout.stop();
        }
        //释放监听器
        barrageInfos.clear();
    }

    public void onResume(){

    }

    public void onPause(){

    }

    public boolean onBackPressed(){
        return false;
    }

    /**
     * initView之后
     */
    protected void afterInitView(){
        userType = UserSharedPreference.getUserType();
        //listVIew滚动位置监听
        barrageListView.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState){
                // 当不滚动时
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 判断是否滚动到底部
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        //加载更多功能的代码
                        isEnd = true;
                    }else{
                        isEnd = false;
                    }
                    barrageAdapter.notifyDataSetChanged();
                    if(isEnd){
                        barrageListView.setSelection(barrageAdapter.getCount());
                    }

                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mSmallVideoContainerLayout = (FrameLayout) mHostWindowActivity.findViewById(R.id.fl_video_decoration);
        for(int i = 0; i < mSmallVideoContainer.length; i++){
            mSmallVideoContainer[i] = (FrameLayout) mHostWindowActivity.findViewById(LAYOUT_ID[i]);
            final int finalI = i;
            mBlurCoverTaskView[i] = new BlurCoverTaskView(mHostWindowActivity, new BlurCoverTaskView.BlurCoverListener() {
                @Override
                public void blurNotify(String notifyType) {
                    if(BlurCoverTaskView.NOTIFY_END_BLUR_COVER.equals(notifyType)){
                        // 点击了确认关闭的按钮
                        onCloseSmallVideoViewClick(finalI + 1);
                    }
                }
            });
            mSmallVideoContainer[i].addView(mBlurCoverTaskView[i], new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        barrageAdapter = new BarrageAdapter(mHostWindowActivity, barrageInfos);
        barrageListView.setAdapter(barrageAdapter);

        barrageSendButton.setOnClickListener(new SendBarrageListener());
        zanLinearLayout.setOnClickListener(ZanListener);

        mWatchAdapter = new HorizontalHeadListViewAdapter(mHostWindowActivity, mInRoomingList,true);
        watchListView.setAdapter(mWatchAdapter);

        changeCamera.setOnClickListener(new ChangeCameraListener());
        if(changeLight != null){
            changeLight.setOnClickListener(new ChangeLightListener());
        }
        soundOn.setOnClickListener(new SoundListener());

        refreshBaseInfo();
        barrageSendEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                MLog.d(TAG, "弹幕输入框焦点, hasFocus is " + hasFocus);
            }
        });

        barrageSendEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removeCallbacks(mHideEditText);
                return false;
            }
        });

        reLayout.setOnTouchListener(new OnClickListenerWrapper(new OnClickListenerWrapper.Listener() {
            @Override
            public void onClick(View view, float rawX, float rawY) {
                ZanListener.onClick(view);
            }

            @Override
            public void onLongClick(View view, float rawX, float rawY) {
                // ignore
            }
        }, false));

        mSmallVideoContainerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(barrageSendLayout.getVisibility() == View.VISIBLE){
                        postDelayed(mHideEditText, 300);
                    }
                }/*else if(event.getAction() == MotionEvent.ACTION_UP){
                    mHostWindowActivity.mLiveHelper.sendPraise();
                    addOnePraiseToUI();
                }*/
                return false;
            }
        });
    }

    private Runnable mHideEditText = new Runnable() {
        @Override
        public void run() {
            MLog.v(TAG, "onClick blank area");
            hideEditText();
        }
    };

    protected void onCloseSmallVideoViewClick(int index) {
        /*ignore*/
    }

    public void refreshSmallVideoBlurName(String[] ids){
        if(ids == null || ids.length != mSmallVideoContainer.length + 1){
            MLog.e(TAG, "refreshSmallVideoBlurName, not match");
            return;
        }

        for(int i = 1; i < ids.length; i++){
            final String id = ids[i];
            UserData profile = null;
            if(TextUtils.isEmpty(id) || (profile = mInLivingUser.get(id)) == null){
                mSmallVideoContainer[i - 1].setVisibility(View.GONE);
                if(profile == null){
                    final ArrayList<String> user = new ArrayList<>();
                    user.add(id);
                    mHostWindowActivity.mEnterLiveHelper.getUsersProfile(user, new TIMValueCallBack<List<TIMUserProfile>>() {
                        @Override
                        public void onError(int i, String s) {
                            MLog.e(TAG, "getUsersProfile, and error code is %d. error message is %s", i, s);
                        }

                        @Override
                        public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                            TIMUserProfile userProfile = timUserProfiles.get(0);
                            UserData userData = new UserData();
                            userData.setId(userProfile.getIdentifier().substring(HLLXLoginHelper.PREFIX.length()));
                            userData.setNickName(userProfile.getNickName());
                            userData.setImgUrl(userProfile.getFaceUrl());
                            mInLivingUser.put(id, userData);
                            refreshSmallVideoBlurName(mHostWindowActivity.mAVUIControl.getIdentifiers());
                        }
                    });
                }
                continue;
            }
            mSmallVideoContainer[i - 1].setVisibility(View.VISIBLE);
            BlurCoverTaskView taskView = mBlurCoverTaskView[i - 1];
            taskView.setViewData(profile.getNickName(), profile.getImgUrl());
            if(MySelfInfo.getInstance().getIdStatus() == Constants.HOST){
                taskView.setCloseVisibility(MySelfInfo.getInstance().getId().equals(id));
            }else{
                if(MySelfInfo.getInstance().getId().equals(id)){
                    taskView.setCloseVisibility(false);
                }else{
                    taskView.setCloseVisibility(true);
                }
            }
        }
    }

    private class SendBarrageListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            barrageSendEdit.setFocusable(true);
            if(barrageSendEdit.getText()!=null && !"".equals(barrageSendEdit.getText().toString())){
                String message = barrageSendEdit.getText().toString().trim();
                sendMessage(message);
//                addOneMessageToUI(message);
                barrageSendEdit.setText("");
            }else{
                Toast.makeText(mHostWindowActivity, "输入消息不能为空！", Toast.LENGTH_SHORT).show();
            }
            removeCallbacks(mHideEditText);
        }
    }

    void hideEditText() {
        //隐藏输入框
        OSUtil.hideKeyboard(mHostWindowActivity);
        barrageSendLayout.setVisibility(View.GONE);
    }

    /**
     * 隐藏插播按钮
     */
    protected void onLineInsertCut(){}

    private View.OnClickListener ZanListener = new  View.OnClickListener() {
        @Override
        public void onClick(View v) {
            zan.setImageResource(R.drawable.icon_red_zan);
            mHostWindowActivity.mLiveHelper.sendPraise();
            addOnePraiseToUI();
            hideEditText();
        }
    };

    private class SoundListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if("3".equals(userType)){
                Toast.makeText(mHostWindowActivity, "很抱歉，当前设备不能控制声音！", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_NORMAL) {
                mHostWindowActivity.mLiveHelper.toggleMic();
                soundOn.setImageResource(mHostWindowActivity.mLiveHelper.isMicOpen() ?
                        R.drawable.live_point_sound_open : R.drawable.live_point_sound_close);
            }else{
                soundOn.setImageResource(!mHostWindowActivity.lvbLiveHelper.setMetu() ?
                        R.drawable.live_point_sound_open : R.drawable.live_point_sound_close);
            }
        }
    }

    private class ChangeLightListener implements View.OnClickListener {

        private boolean isLightOpened = false;
        @Override
        public void onClick(View v) {
            if("3".equals(userType)){
                Toast.makeText(mHostWindowActivity, "很抱歉，当前设备不能打开闪关灯！", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_NORMAL) {
                if (mHostWindowActivity.mLiveHelper.isFrontCamera()) {
                    Toast.makeText(mHostWindowActivity, "没有检测到前置摄像头闪光灯", Toast.LENGTH_SHORT).show();
                    return;
                }
                mHostWindowActivity.mLiveHelper.toggleFlashLight();
                isLightOpened = !isLightOpened;
            }else if(mHostWindowActivity.liveType == HostWindowActivity.LIVE_TYPE_LVB){
                if(!mHostWindowActivity.lvbLiveHelper.turnOnFlashLight()){
                    Toast.makeText(mHostWindowActivity, "很抱歉，打开闪关灯失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                isLightOpened = !isLightOpened;
            }
            changeLight.setImageResource(isLightOpened ?
                    R.drawable.live_point_light_open : R.drawable.live_point_light_close);
        }
    }

    private class ChangeCameraListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            changeCamera.setClickable(false);
            if ("3".equals(userType)) {
                Toast.makeText(mHostWindowActivity, "很抱歉，当前设备不能改变摄像头！", Toast.LENGTH_SHORT).show();
                return;
            }
            mHostWindowActivity.switchCamera();
            changeLight.setImageResource(R.drawable.live_point_light_close);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeCamera.setClickable(true);
                }
            }, 1000);
        }
    }

    class ShareLiveListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            LiveHelper.S_IN_CROP = true;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    LiveHelper.S_IN_CROP = false;
                }
            }, 1000);
            String shareUrl = VideoConstant.SHARE_VIDEO_URL + CurLiveInfo.getInstance().getShare();
            OSUtil.showShare(null, CurLiveInfo.getInstance().getTitle(),
                    CurLiveInfo.getInstance().getTitle(), CurLiveInfo.getInstance().getCoverurl(), shareUrl, shareUrl, mHostWindowActivity);
        }
    }
}