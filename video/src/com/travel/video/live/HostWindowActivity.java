package com.travel.video.live;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMCallBack;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.TIMAvManager;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.sdk.AVView;
import com.tencent.qcloud.suixinbo.avcontrollers.AVUIControl;
import com.tencent.qcloud.suixinbo.avcontrollers.AbstractAVUIControl;
import com.tencent.qcloud.suixinbo.avcontrollers.PackAVUIControl;
import com.tencent.qcloud.suixinbo.avcontrollers.QavsdkControl;
import com.tencent.qcloud.suixinbo.interfaces.LVBLiveInterface;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.EnterLiveHelper;
import com.tencent.qcloud.suixinbo.presenters.LVBLiveHelper;
import com.tencent.qcloud.suixinbo.presenters.LiveHelper;
import com.tencent.qcloud.suixinbo.presenters.LowPermissionEnterLiveHelper;
import com.tencent.qcloud.suixinbo.presenters.PackEnterLiveHelper;
import com.tencent.qcloud.suixinbo.presenters.viewinface.EnterQuiteRoomView;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LiveView;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.travel.bean.VideoInfoBean;
import com.travel.communication.entity.UserData;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;
import com.travel.video.bean.BarrageInfo;
import com.travel.video.gift.GiftBean;
import com.travel.video.help.HeartBeatHelper;
import com.travel.video.http.LiveHttpHelper;
import com.travel.video.tools.HostWindowTimerTask;
import com.travel.video.tools.RandomSetColor;
import com.travel.video.tools.SmallWindowMoveTouchListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

/**
 * 主播直播页
 */
public class HostWindowActivity extends FragmentActivity implements AVUIControl.AVControlListener {
    private final static String TAG = "HostWindowActivity";

    public static final int LIVE_TYPE_NORMAL = 1; // 互动直播
    public static final int LIVE_TYPE_LVB = 2; // 单直播
    public static final int LIVE_TYPE_PACK = 3; // 背包

    public static final String LIVE_IS_HOST = "is_host";
    public static final String LIVE_IS_GROUP = "is_group";

    private Bundle bundle = null;
    VideoInfoBean videoBean;
    int liveType = LIVE_TYPE_LVB; // 是否是背包直播
    AbstractLiveHandler mUIHandler;
    boolean isShowShopButton = true;
    String mPackLiveUrl;
    boolean mIsLowPermission = false;
    private UserTmpLeaveHandler mTmpLeaveHandler;
    private boolean mIsHost = false; // 是否是主播


    /* default scope: used in handler */
    // 所有需要重新绑定的属性
    EnterLiveHelper mEnterLiveHelper;
    LiveHelper mLiveHelper;
    AbstractAVUIControl mAVUIControl;

    private View mGlRootView;
    LiveView mLiveView;
    private IjkVideoView mIjkVideoView;

    private TXCloudVideoView lvbView;
    LVBLiveHelper lvbLiveHelper;
    String mLvbLiveUrl;

    private static WeakReference<HostWindowActivity> s_Intance;

    private boolean isGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (QavsdkControl.getInstance().getAVContext() == null) {
            Toast.makeText(this, "视频控件正在初始化...\n 请稍后重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 判断所必须的权限
        List<String> premissions = new ArrayList<>();
        // 摄像头
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.CAMERA);
        }
        // 录音
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.RECORD_AUDIO);
        }
        // 读取存储卡
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            premissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(premissions.size() > 0){
            String[] s = new String[premissions.size()];
            for (int i = 0; i < s.length; i++) {
                s[i] = premissions.get(i);
            }
            ActivityCompat.requestPermissions(this, s, 1);
        }

        //直播的自定义数据
        if (getIntent().hasExtra("activity_bundle")) {
            bundle = getIntent().getBundleExtra("activity_bundle");
        }

        final Handler handler = new Handler();

        final Bundle videoBundle = getIntent().getExtras();
        if(videoBundle != null && videoBundle.containsKey("liveId")){
            LoadingDialog.getInstance(this).showProcessDialog();
            LiveHttpHelper liveHttpHelper = new LiveHttpHelper(this, new LiveHttpHelper.HttpListener() {
                @Override
                public void getLiveInfo(VideoInfoBean videoInfoBean) {
                    LoadingDialog.getInstance(HostWindowActivity.this).hideProcessDialog(0);
                    if(videoInfoBean == null) {
                        finish();
                        return;
                    }
                    videoBundle.putSerializable("video_info", videoInfoBean);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            create(videoBundle);
                        }
                    });
                }
            });
            liveHttpHelper.getLiveInfo(videoBundle.getString("liveId"));
        }else{
            create(videoBundle);
        }
    }

    /**
     * requestPermissions方法执行后的回调方法
     * @param requestCode 相当于一个标志，
     * @param permissions 需要传进的permission，不能为空
     * @param grantResults 用户进行操作之后，或同意或拒绝回调的传进的两个参数;
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != 1) return;
        //这里实现用户操作，或同意或拒绝的逻辑
        /*grantResults会传进android.content.pm.PackageManager.PERMISSION_GRANTED 或 android.content.pm.PackageManager.PERMISSION_DENIED两个常，前者代表用户同意程序获取系统权限，后者代表用户拒绝程序获取系统权限*/
        for (int i = 0; i < permissions.length; i++) {
            if(TextUtils.equals(Manifest.permission.RECORD_AUDIO, permissions[i])
                    && PackageManager.PERMISSION_DENIED == grantResults[i]){
                finish();
                return;
            }
            if(TextUtils.equals(Manifest.permission.CAMERA, permissions[i])
                    && PackageManager.PERMISSION_DENIED == grantResults[i]){
                finish();
                return;
            }
            if(TextUtils.equals(Manifest.permission.READ_EXTERNAL_STORAGE, permissions[i])
                    && PackageManager.PERMISSION_DENIED == grantResults[i]){
                finish();
                return;
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey("bundle"))
            bundle = savedInstanceState.getBundle("bundle");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBundle("bundle", bundle);
    }

    private void create(Bundle videoBundle){
        if (videoBundle != null && videoBundle.containsKey("video_info")) {
            videoBean = (VideoInfoBean) videoBundle.get("video_info");
        }

        if(videoBundle != null && videoBundle.containsKey(LIVE_IS_GROUP)){
            isGroup = videoBundle.getBoolean(LIVE_IS_GROUP);
        }

        if (videoBundle != null && videoBundle.containsKey("intent_source")
                && "shop".equals(videoBundle.getString("intent_source"))) {
            isShowShopButton = false;
        }
        mIsHost = getIntent().getBooleanExtra(LIVE_IS_HOST, false);
        if (mIsHost ) {
            liveType = UserSharedPreference.getLiveType();
        } else {
            liveType = videoBean.getVideoType();
        }
        CurLiveInfo.getInstance().setLiveType(liveType);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        getWindow().setFlags(0x80000000, 0x80000000);

        if (liveType == LIVE_TYPE_PACK) {
            //横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            //强制竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        //设置屏幕亮度
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.9F;
        dealWithOtherLive();
        bindAllField();
        mTmpLeaveHandler = new UserTmpLeaveHandler(this);
        s_Intance = new WeakReference<HostWindowActivity>(this);
    }

    void bindAllField() {
        setContentView(R.layout.activity_host_window);
        ViewStub liveStub = (ViewStub) findViewById(R.id.vs_full_live_layout);
        ViewStub packLiveStub = (ViewStub) findViewById(R.id.vs_pack_live_layout);
        (liveType==LIVE_TYPE_PACK ? packLiveStub : liveStub).inflate();
        mGlRootView = findViewById(liveType==LIVE_TYPE_PACK ? R.id.av_video_glview_small : R.id.av_video_glview);
        mIjkVideoView = (IjkVideoView) findViewById(R.id.ijk_video_view);
        mIjkVideoView.setZOrderOnTop(false);
        mIjkVideoView.setZOrderMediaOverlay(false);
        ((GLRootView) mGlRootView).setZOrderMediaOverlay(true);
        initTencentData();
        if (liveType == LIVE_TYPE_PACK) {
            FrameLayout packageFrameLayout = mUIHandler.mSmallVideoContainer[0];
            ViewGroup.LayoutParams params = packageFrameLayout.getLayoutParams();
            params.width = getResources().getDimensionPixelSize(R.dimen.video_small_view_height);
            params.height = getResources().getDimensionPixelSize(R.dimen.video_small_view_width);
            packageFrameLayout.setLayoutParams(params);
            mIjkVideoView.setVisibility(View.VISIBLE);
        } else if(liveType == LIVE_TYPE_LVB){
            mGlRootView.setVisibility(View.GONE);
            lvbView = (TXCloudVideoView) findViewById(R.id.lvb_TXCloudVideoView);
            lvbView.setVisibility(View.VISIBLE);
        }else{
            mIjkVideoView.setVisibility(View.GONE);
        }
        initTencentHelper();

        //初始化弹幕颜色
        RandomSetColor.randomColorAdapter();
        //初始化直播时间任务
        HostWindowTimerTask.instance(this);
        registerReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 切换屏幕处理
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initTencentData() {
        if (TextUtils.isEmpty(MySelfInfo.getInstance().getNickName())) {
            MySelfInfo.getInstance().setNickName(MySelfInfo.getInstance().getId());
        }
        if (mIsHost) {
            MySelfInfo.getInstance().setIdStatus(Constants.HOST);
            MySelfInfo.getInstance().setJoinRoomWay(true);
            CurLiveInfo.getInstance().setTitle("我的腾讯云测试");
            CurLiveInfo.getInstance().setHostID(MySelfInfo.getInstance().getId());
            CurLiveInfo.getInstance().setHostName(UserSharedPreference.getNickName());
            CurLiveInfo.getInstance().setHostAvator(UserSharedPreference.getUserHeading());
            findViewById(R.id.include_video_live_host).setVisibility(View.VISIBLE);
            //页面UI变化
            mUIHandler = new HostWindowHandler(this, bundle);

            generateRoomNum();
            if (liveType != LIVE_TYPE_NORMAL) {
                getRtmpAddressAndPlay(UserSharedPreference.getUserId());
            }

            HeartBeatHelper.getInstance().setRoomNum(MySelfInfo.getInstance().getMyRoomNum()).setUserId(MySelfInfo.getInstance().getId());
        } else {
            String hostId = (videoBean.getPersonalInfoBean().getUserId().equals("") ? "0" : videoBean.getPersonalInfoBean().getUserId());
            String hostName = videoBean.getPersonalInfoBean().getUserName();
            String hostImg = videoBean.getPersonalInfoBean().getUserPhoto();
            int roomNum = videoBean.getHashId();

            if(liveType == LIVE_TYPE_PACK){
                playUrl(videoBean.getUrl());
            }

            /*if (!TextUtils.isEmpty(videoBean.getUrl())) {
                String roomNumStr;
                if (liveType == LIVE_TYPE_NORMAL) {
                    roomNumStr = videoBean.getUrl();
                } else {
                    String[] urlArray = videoBean.getUrl().split(",");
                    roomNumStr = urlArray[0];
                    if (urlArray.length == 2){
                        String rtmpLiveUrl = videoBean.getUrl().split(",")[1];
                        CurLiveInfo.getInstance().setRtmpAddress(rtmpLiveUrl);
                        playUrl(rtmpLiveUrl);
                    }else{
                        getRtmpAddressAndPlay(hostId);
                    }
                }
                roomNum = Integer.parseInt(roomNumStr);
            }*/
            String roomName = videoBean.getVideoTitle();
            String roomImg = videoBean.getVideoImg();
            CurLiveInfo.getInstance().setShare(videoBean.getShare());
//            CurLiveInfo.getInstance().setRtmpAddress(videoBean.getShareAddress());
            MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
            MySelfInfo.getInstance().setJoinRoomWay(false);
            CurLiveInfo.getInstance().setHostID(HLLXLoginHelper.PREFIX + hostId);
            CurLiveInfo.getInstance().setAVRoomNum(roomNum);
            CurLiveInfo.getInstance().setHostHLLXUserId(Integer.valueOf(hostId));
            CurLiveInfo.getInstance().setTitle(roomName);
            CurLiveInfo.getInstance().setRoomNum(roomNum);
            CurLiveInfo.getInstance().setHostName(hostName);
            CurLiveInfo.getInstance().setHostAvator(hostImg);
            CurLiveInfo.getInstance().setCoverurl(roomImg);
            mUIHandler = new ViewerWindowHandler(this);
            mUIHandler.setmPraiseNum(videoBean.getPraiseNum());
            findViewById(R.id.include_video_live_normal).setVisibility(View.VISIBLE);
            HeartBeatHelper.getInstance().setRoomNum(roomNum).setUserId(MySelfInfo.getInstance().getId());
        }
    }

    private void getRtmpAddressAndPlay(String hostId) {
        mUIHandler.mLiveHttpRequest.getPackLiveUrl(hostId, new TIMValueCallBack<String>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(HostWindowActivity.this, "获取直播地址失败\n 请稍后重试", Toast.LENGTH_SHORT).show();
                onFinish();
            }

            @Override
            public void onSuccess(String s) {
                if(liveType == LIVE_TYPE_PACK) {
                    mPackLiveUrl = CurLiveInfo.getInstance().getRtmpAddress();
                    playUrl(s);
                }else{
                    mLvbLiveUrl = CurLiveInfo.getInstance().getRtmpStreamUrl();
                }
            }
        });
    }

    private void playUrl(String s) {
        if(TextUtils.isEmpty(s)) return;
        mIjkVideoView.setVisibility(View.VISIBLE);
        mIjkVideoView.setVideoPath(s);
        mIjkVideoView.start();
    }

    private void generateRoomNum() {
        int userID = Integer.valueOf(UserSharedPreference.getUserId());
        Random random2 = new Random();
        int random = random2.nextInt(89) + 10;
        String roomNum = userID + String.valueOf(random) + "1";
        int roomIntNum = Integer.valueOf(roomNum);
        MLog.d(TAG, "roomIntNum is %d.", roomIntNum);
        MySelfInfo.getInstance().setMyRoomNum(roomIntNum);
        CurLiveInfo.getInstance().setRoomNum(MySelfInfo.getInstance().getMyRoomNum());
        CurLiveInfo.getInstance().setHostHLLXUserId(userID);
        CurLiveInfo.getInstance().setAVRoomNum(roomIntNum);
    }

    private void initTencentHelper() {
        mLiveView = new MLiveView();
        mLiveHelper = new LiveHelper(this, mLiveView);
        final EnterQuiteRoomView enterQuiteRoomView = new EnterQuiteRoomView() {
            @Override
            public void enterRoomComplete(int id_status, boolean succ) {
                MLog.v(TAG, "enterRoomComplete, succ is " + succ);
                if (liveType == LIVE_TYPE_PACK) {
                    mAVUIControl = new PackAVUIControl(HostWindowActivity.this, mGlRootView);
                    mGlRootView.setOnTouchListener(new SmallWindowMoveTouchListener(mGlRootView, new SmallWindowMoveTouchListener.LeftUpPointChangedListener() {
                        @Override
                        public void onLeftUpChanged(int left, int top) {
                            MLog.v(TAG, "onLeftUpChanged, and left is %d, top is %d", left, top);
                            onLayoutSmallVideoArea(1, left, top, 0, 0);
                        }
                    }));
                    if(mUIHandler instanceof ViewerWindowHandler){
                        ((ViewerWindowHandler) mUIHandler).stopHostCloseTimer();
                    }
                } else {
                    mAVUIControl = new AVUIControl(HostWindowActivity.this, mGlRootView);
                    ((AVUIControl) mAVUIControl).setListener(HostWindowActivity.this);
                }
                mAVUIControl.setSelfId(MySelfInfo.getInstance().getId());
                mLiveHelper.setCameraPreviewChangeCallback();
                if (succ) {
                    if (!mIsHost) {
                        mLiveHelper.sendGroupMessage(Constants.AVIMCMD_EnterLive, "");
                    }
                }
            }

            @Override
            public void quiteRoomComplete(int id_status, boolean succ, boolean isFromAVRoom) {
                if(liveType == LIVE_TYPE_LVB) return;
                MLog.d(TAG, "quiteRoomComplete, and succ is %b, isFromAVRoom %b", succ, isFromAVRoom);
                if (mIsHost || isFromAVRoom) {
                    if (mLiveHelper != null) {
                        finish();
                    }// 边看边买
                } else {
                    // 进入房间, 主播已经关闭直播
                    if (mUIHandler != null) {
                        ((ViewerWindowHandler) mUIHandler).onHostCloseLive();
                    }
                }
            }

            @Override
            public void memberQuiteLive(String[] list) {

            }

            @Override
            public void memberJoinLive(String[] list) {

            }

            @Override
            public void alreadyInLive(final String[] list) {
                MLog.d(TAG, "alreadyInLive is " + list);
                EnterLiveHelper.getUsersProfile(Arrays.asList(list), new TIMValueCallBack<List<TIMUserProfile>>() {
                    @Override
                    public void onError(int i, String s) {
                        MLog.e(TAG, "getUserProfile failed: and error code is %d, and error reason is ", i, s);
                    }

                    @Override
                    public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                        MLog.v(TAG, "getUserProfile success");
                        for (TIMUserProfile timUserProfile : timUserProfiles) {
                            UserData userData = new UserData();
                            userData.setId(timUserProfile.getIdentifier().substring(HLLXLoginHelper.PREFIX.length()));
                            userData.setNickName(timUserProfile.getNickName());
                            userData.setImgUrl(timUserProfile.getFaceUrl());
                            mUIHandler.mInLivingUser.put(timUserProfile.getIdentifier(), userData);
                        }
                        mUIHandler.refreshSmallVideoBlurName(list);
                    }
                });
            }

            @Override
            public void onGetGroupMembersList(List<TIMUserProfile> memberList) {
                List<UserData> userDataList = new ArrayList<>();
                for (TIMUserProfile profile : memberList) {
                    UserData userData = new UserData();
                    userData.setId(profile.getIdentifier().substring(HLLXLoginHelper.PREFIX.length()));
                    userData.setNickName(profile.getNickName());
                    userData.setImgUrl(profile.getFaceUrl());
                    userDataList.add(userData);
                }
                mUIHandler.setGroupMembers(userDataList);
            }
        };

        if (mIsHost) {
            afterGetPermission(enterQuiteRoomView);
        } else {
            mUIHandler.mLiveHttpRequest.applyFlowControlOperationPermission(String.valueOf(CurLiveInfo.getInstance().getRoomNum()), MySelfInfo.getInstance().getId(), 3, new TIMCallBack() {
                @Override
                public void onError(int i, String s) {
                    mIsLowPermission = true;
                    afterGetPermission(enterQuiteRoomView);
                }

                @Override
                public void onSuccess() {
                    afterGetPermission(enterQuiteRoomView);
                    HeartBeatHelper.getInstance().setStatus(3).startSendHeartBeat();
                }
            });
        }
        if(liveType == LIVE_TYPE_LVB){
            lvbLiveHelper = new LVBLiveHelper(this, lvbLiveListener);
            if(mIsHost) {
                lvbLiveHelper.initLVBLive(LVBLiveHelper.LVB_LIVE, lvbView);
                lvbLiveHelper.startLive();
            }else {
                lvbLiveHelper.initLVBLive(LVBLiveHelper.LVB_WATCH_LIVE, lvbView);
                lvbLiveHelper.startVideo(videoBean.getUrl());
            }

        }
    }

    private void afterGetPermission(EnterQuiteRoomView enterQuiteRoomView) {
        if(mUIHandler == null)
            bindAllField();
        if (mIsLowPermission) {
            playUrl(CurLiveInfo.getInstance().getRtmpAddress());
            mGlRootView.setVisibility(View.GONE);
            ((ViewerWindowHandler) mUIHandler).onLineInsertCut();
            ((ViewerWindowHandler) mUIHandler).setBlurImageVisible(false);
            ((ViewerWindowHandler)mUIHandler).stopHostCloseTimer();
            MLog.e(TAG, "低权限直播用户");
            mEnterLiveHelper = new LowPermissionEnterLiveHelper(this, enterQuiteRoomView, mLiveHelper);
        } else if (liveType == LIVE_TYPE_NORMAL) {
            mEnterLiveHelper = new EnterLiveHelper(HostWindowActivity.this, enterQuiteRoomView, mLiveHelper);
        } else if (liveType == LIVE_TYPE_PACK) {
            mEnterLiveHelper = new PackEnterLiveHelper(HostWindowActivity.this, enterQuiteRoomView, mLiveHelper);
            //        } else if (liveType == LIVE_TYPE_LVB) {
        }else{
            mGlRootView.setVisibility(View.GONE);
            mUIHandler.onLineInsertCut();
            mEnterLiveHelper = new LowPermissionEnterLiveHelper(this, enterQuiteRoomView, mLiveHelper);
            mEnterLiveHelper.startEnterRoom();
            return;
        }

        mLiveHelper.setCameraPreviewChangeCallback();
        if (!mIsHost && MySelfInfo.getInstance().getId().equals(CurLiveInfo.getInstance().getHostID())) {
            // 没有登录， 或者是主播自己
            ((ViewerWindowHandler) mUIHandler).onHostCloseLive();
        }else{
            mEnterLiveHelper.startEnterRoom();
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_SURFACE_CREATED);
        intentFilter.addAction(Constants.ACTION_HOST_ENTER);
        intentFilter.addAction(Constants.ACTION_CAMERA_OPEN_IN_LIVE);
        intentFilter.addAction(Constants.ACTION_CAMERA_CLOSE_IN_LIVE);
        intentFilter.addAction(Constants.ACTION_SWITCH_VIDEO);
        intentFilter.addAction(Constants.ACTION_HOST_LEAVE);
        intentFilter.addAction(Constants.BD_EXIT_APP);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    LVBLiveInterface lvbLiveListener = new LVBLiveInterface() {
        @Override
        public void onPlayEvent(int event, Bundle bundle) {
            Log.e(TAG,"onPlayEvent : " + "status= "+event+" , bundle= "+bundle.toString());
            switch (event){
                case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                    // 加载中
                    break;
                case TXLiveConstants.PLAY_EVT_PLAY_BEGIN:
                    // 开始播放
                    ((ViewerWindowHandler) mUIHandler).setBlurImageVisible(false);
                    break;
                case TXLiveConstants.PLAY_ERR_NET_DISCONNECT | TXLiveConstants.PLAY_EVT_PLAY_END:
                    // 断开连接 或 播放结束
                    if (mUIHandler instanceof ViewerWindowHandler) {
                        ((ViewerWindowHandler) mUIHandler).onHostCloseLive();
                    }
                    break;
                case TXLiveConstants.PLAY_ERR_NET_DISCONNECT:
                    //三次抢救失败
                    if (mUIHandler instanceof ViewerWindowHandler) {
                        ((ViewerWindowHandler) mUIHandler).onHostCloseLive();
                    }
                    break;
            }
        }

        private boolean isPushed = false;
        @Override
        public void onPushEvent(int event, Bundle bundle) {
            Log.e(TAG,"onPushEvent : " + "status= "+event+" , bundle= "+bundle.toString());
            if (event < 0) {
                Toast.makeText(HostWindowActivity.this, bundle.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            }
            switch (event) {
                case TXLiveConstants.PUSH_EVT_OPEN_CAMERA_SUCC :
                    // 推流摄像机打开成功
                    break;
                case TXLiveConstants.PUSH_EVT_PUSH_BEGIN :
                    // 与服务器握手完毕,一切正常，准备开始推流
                    if(!isPushed) {
                        ((HostWindowHandler) mUIHandler).mLiveHttpRequest.notifyServerLiveStart();
                        isPushed = true;
                    }
                    break;
                case TXLiveConstants.PUSH_WARNING_NET_BUSY :
                    // 提示直播网络不好
                    Toast.makeText(HostWindowActivity.this,"哎吆！别刷淘宝了！\n网络信号不好使了！",Toast.LENGTH_SHORT).show();
                    break;
                case TXLiveConstants.PUSH_ERR_NET_DISCONNECT:
                    //网络断连,且经三次抢救无效,可以放弃治疗
                    Toast.makeText(HostWindowActivity.this, "人才啊！\n你尽然给直播失败了！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onNetStatus(Bundle bundle) {

        }
    };

    class MLiveView implements LiveView {
        @Override
        public void showVideoView(boolean isLocal, String id) {
            SxbLog.i(TAG, "showVideoView host :" + MySelfInfo.getInstance().getId());
            onInsertUserTmpBack(id);
            if (liveType == LIVE_TYPE_PACK) {
                mGlRootView.setVisibility(View.VISIBLE);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mGlRootView.getLayoutParams();
                onLayoutSmallVideoArea(1, params.leftMargin, params.topMargin, 0, 0);
            }else{
                if(!mIsHost && CurLiveInfo.getInstance().getHostID().equals(id)){
                    // 观众， 显示主播界面
                    ((ViewerWindowHandler)mUIHandler).stopHostCloseTimer();
                }
            }
            if (isLocal) {
                mAVUIControl.setSelfId(MySelfInfo.getInstance().getId());
                mAVUIControl.setLocalHasVideo(true, false, MySelfInfo.getInstance().getId());
                if (mIsHost) {
                    // 主播本地录制, ignore
                } else if (MySelfInfo.getInstance().getId().equals(id)) {
                    ((ViewerWindowHandler) mUIHandler).startInsertCut(mAVUIControl.getViewIndexById(id, AVView.VIDEO_SRC_TYPE_CAMERA));
                }
            } else {
                int index = mAVUIControl.setHasRemoteVideo(true, id, AVView.VIDEO_SRC_TYPE_CAMERA);
                if (!mIsHost) {
                    ((ViewerWindowHandler) mUIHandler).setBlurImageVisible(false);
                }
                if (index >= 1) {
                    mUIHandler.mBlurCoverTaskView[index - 1].hideBlurCover();
                }
            }
            mUIHandler.refreshSmallVideoBlurName(mAVUIControl.getIdentifiers());
        }

        @Override
        public void onApplyInsertLine(String id, String nickname, String userImg) {
            // 有人申请插播, must be host
            MLog.v(TAG, "onApplyInsertLine, and id is " + id);
            UserData userData = new UserData();
            userData.setId(id.substring(HLLXLoginHelper.PREFIX.length()));
            userData.setNickName(nickname);
            userData.setImgUrl(userImg);
            mUIHandler.mInLivingUser.put(id, userData);
            ((HostWindowHandler) mUIHandler).onApplyInsertLine(userData);
        }

        @Override
        public void refreshText(String text, String userId, String name, String userImg) {
            MLog.v(TAG, "onRefreshText, and text is %s, name is %s", text, name);
            BarrageInfo barrageInfo = new BarrageInfo();
            barrageInfo.setUserId(userId.substring(HLLXLoginHelper.PREFIX.length()));
            barrageInfo.setNickName(name);
            barrageInfo.setUserImg(userImg);
            barrageInfo.setContent(text);
            mUIHandler.addOneMessageToUI(barrageInfo);
        }

        @Override
        public void refreshThumbUp() {
            mUIHandler.addOnePraiseToUI();
        }

        @Override
        public void refreshUI(String id) {

        }

        @Override
        public boolean showInviteView(String id) {
            return false;
        }

        @Override
        public void cancelInviteView(String id) {
            mUIHandler.sendEmptyMessage(ViewerWindowHandler.REFUSE_INSER_CUT);
        }

        @Override
        public void cancelMemberView(String id) {
            // TODO delete
            closeMemberView(id);
        }

        @Override
        public void memberJoin(String id, String name, String userImg) {
            UserData userData = new UserData();
            userData.setId(id.substring(HLLXLoginHelper.PREFIX.length()));
            userData.setNickName(name);
            userData.setImgUrl(userImg);
            mUIHandler.onUserEnterRoom(userData);
        }

        @Override
        public void memberQuit(String id, String name, String userImg) {
            UserData userData = new UserData();
            userData.setId(id.substring(HLLXLoginHelper.PREFIX.length()));
            userData.setNickName(name);
            userData.setImgUrl(userImg);
            mUIHandler.onUserExistRoom(userData);
            mLiveView.closeMemberView(id);
        }

        @Override
        public void readyToQuit() {
            mEnterLiveHelper.quiteLive(true);
        }

        @Override
        public void hideInviteDialog() {
            // 上麦
            mLiveHelper.changeAuthandRole(true, Constants.VIDEO_MEMBER_AUTH, Constants.VIDEO_MEMBER_ROLE);
            HeartBeatHelper.getInstance().setStatus(2);
        }

        @Override
        public void pushStreamSucc(TIMAvManager.StreamRes streamRes) {
            // 推流成功
            List<TIMAvManager.LiveUrl> liveUrlList = streamRes.getUrls();
            String url = "";
            for (TIMAvManager.LiveUrl liveUrl : liveUrlList) {
                // need more
                url = liveUrl.getUrl();
            }
            ((HostWindowHandler) mUIHandler).onGotPushStreamUrl(url);
        }

        @Override
        public void onAlreadyPushing() {
            ((HostWindowHandler) mUIHandler).onGotPushStreamUrl("");
        }

        @Override
        public void stopStreamSucc() {

        }

        @Override
        public void startRecordCallback(boolean isSucc) {

        }

        @Override
        public void stopRecordCallback(boolean isSucc, List<String> files) {
            MLog.v(TAG, "stopRecordCallback and isSucc " + isSucc);
            ((HostWindowHandler) mUIHandler).mLiveHttpRequest.notifyServerLiveStop(String.valueOf(CurLiveInfo.getInstance().getRoomNum()), files);
            onFinish();
        }

        @Override
        public void hostLeave(String id, String name) {

        }

        @Override
        public void hostBack(String id, String name) {

        }

        @Override
        public void onSendGif(String giftBeanStr) {
            mUIHandler.showGif(GiftBean.getGiftBeanOfJson(giftBeanStr));
        }

        @Override
        public void closeMemberView(String id) {
            if (mAVUIControl != null) {
                onInsertUserTmpBack(id);
                if (mAVUIControl.closeMemberVideoView(id)) {
                    if (liveType == LIVE_TYPE_PACK) {
                        mGlRootView.setVisibility(View.GONE);
                    }
                    mUIHandler.refreshSmallVideoBlurName(mAVUIControl.getIdentifiers());
                }
            }
            if (MySelfInfo.getInstance().getId().equals(id) && !mIsHost) {
                ((ViewerWindowHandler) mUIHandler).hideCameraAndSoundOn();
                HeartBeatHelper.getInstance().setStatus(3);
            }
        }

        @Override
        public void setMirror(boolean isMirror) {
            mAVUIControl.setMirror(isMirror, MySelfInfo.getInstance().getId());
        }

        @Override
        public void cancelInsert(String id) {
            // 主播退出插播等待
            String hllxId = id.substring(HLLXLoginHelper.PREFIX.length());
            if (mIsHost)
                ((HostWindowHandler) mUIHandler).onCancelInsert(hllxId);
            else
                // 主播关闭拒绝插播
                cancelInviteView(id);
        }
    }

    ;

    private static class UserTmpLeaveHandler extends Handler {
        WeakReference<HostWindowActivity> mActivity;

        public UserTmpLeaveHandler(HostWindowActivity hostWindowActivity) {
            mActivity = new WeakReference<HostWindowActivity>(hostWindowActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null)
                return;
            if (msg.what < 0 || msg.what >= 4)
                return;
            String id = mActivity.get().mAVUIControl.getIdentifierByIndex(msg.what);
            if (TextUtils.isEmpty(id))
                return;
            mActivity.get().mLiveView.closeMemberView(id);
            mActivity.get().mLiveHelper.sendCancelInteract(id);
        }
    }

    private void unRegisterReceiver() {
        MLog.v(TAG, "unRegisterReceiver");
        try{
            unregisterReceiver(mBroadcastReceiver);
        }catch (IllegalArgumentException e){
            MLog.d(TAG, "ignore unRegisterReceiver Exception");
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(liveType == LIVE_TYPE_LVB) {
                if (action.equals(Constants.ACTION_HOST_LEAVE)) {//主播结束
                    //				quiteLivePassively();
                    // 主播关闭直播
                    mEnterLiveHelper.quiteLive(false);
                    if (mUIHandler instanceof ViewerWindowHandler) {
                        ((ViewerWindowHandler) mUIHandler).onHostCloseLive();
                    }
                }

                if (action.equals(Constants.BD_EXIT_APP)) {
                    //被异地登录了
                    if (mIsHost) {
                        mUIHandler.sendEmptyMessage(HostWindowHandler.CLOSE_LIVE);
                    } else {
                        onFinish();
                    }
                }
                return;
            }

            //AvSurfaceView 初始化成功
            if (action.equals(Constants.ACTION_SURFACE_CREATED)) {
                //打开摄像头
                if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST && liveType == LIVE_TYPE_NORMAL) {
                    mLiveHelper.openCameraAndMic();
                } else if (liveType == LIVE_TYPE_PACK) {
                    mLiveHelper.closeMic();
                    //					mLiveHelper.openCameraAndMic();
                    if (mLiveHelper.isFirstTime()) {
                        mAVUIControl.setRotation(180);
                    }
                }mLiveHelper.closeMic();
                mLiveHelper.initBeauty();
            }

            if (action.equals(Constants.ACTION_CAMERA_OPEN_IN_LIVE)) {//有人打开摄像头
                ArrayList<String> ids = intent.getStringArrayListExtra("ids");
                //如果是自己本地直接渲染
                for (String id : ids) {
                    mLiveView.showVideoView(MySelfInfo.getInstance().getId().equals(id), id);
                    if (!mIsHost && id.equals(CurLiveInfo.getInstance().getHostID())) {
                        ((ViewerWindowHandler) mUIHandler).onHostBack();
                    }
                }
                //其他人一并获取
                SxbLog.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "somebody open camera,need req data"
                        + LogConstants.DIV + LogConstants.STATUS.SUCCEED + LogConstants.DIV + "ids " + ids.toString());
                int requestCount = CurLiveInfo.getInstance().getCurrentRequestCount();
                mLiveHelper.requestViewList(ids);
                requestCount = requestCount + ids.size();
                CurLiveInfo.getInstance().setCurrentRequestCount(requestCount);
            }

            if (action.equals(Constants.ACTION_CAMERA_CLOSE_IN_LIVE)) {//有人关闭摄像头
                ArrayList<String> ids = intent.getStringArrayListExtra("ids");
                //如果是自己本地直接渲染
                for (String id : ids) {
                    if (id.equals(MySelfInfo.getInstance().getId()))
                        continue;

                    if (CurLiveInfo.getInstance().getHostID().equals(id)) {
                        if (!mIsHost) {
                            ((ViewerWindowHandler) mUIHandler).onHostTmpLeave();
                        }
                    } else {
                        // 插播方暂时离开
                        onInsertUserTmpLeave(id);
                    }
                }
            }

            if (action.equals(Constants.ACTION_SWITCH_VIDEO)) {//点击成员回调
                // TODO:
            }
            if (action.equals(Constants.ACTION_HOST_LEAVE)) {//主播结束
                //				quiteLivePassively();
                // 主播关闭直播
                mEnterLiveHelper.quiteLive(false);
                if (mUIHandler instanceof ViewerWindowHandler) {
                    ((ViewerWindowHandler) mUIHandler).onHostCloseLive();
                }
            }

            if(action.equals(Constants.BD_EXIT_APP)){
                //被异地登录了
                if(mIsHost){
                    mUIHandler.sendEmptyMessage(HostWindowHandler.CLOSE_LIVE);
                }else{
                    onFinish();
                }
            }
        }
    };

    private void onInsertUserTmpBack(String id) {
        if(mAVUIControl == null) return;
        int videoIndex = mAVUIControl.getViewIndexById(id, AVView.VIDEO_SRC_TYPE_CAMERA);
        if (videoIndex < 1)
            return;

        FrameLayout frameLayout = mUIHandler.mSmallVideoContainer[videoIndex - 1];
        mUIHandler.mBlurCoverTaskView[videoIndex - 1].onUserTmpLeaveBack();
        frameLayout.setClickable(false);
        if (mIsHost) {
            mTmpLeaveHandler.removeMessages(videoIndex);
        }
    }

    private void onInsertUserTmpLeave(String id) {
        int videoIndex = mAVUIControl.getViewIndexById(id, AVView.VIDEO_SRC_TYPE_CAMERA);
        MLog.v(TAG, "onInsertUserTmpLeave, and id is %s, videoIndex is %d.", id, videoIndex);
        if (videoIndex < 1)
            return;
        FrameLayout frameLayout = mUIHandler.mSmallVideoContainer[videoIndex - 1];
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MLog.v(TAG, "onClick decorations");
            }
        });
        mUIHandler.mBlurCoverTaskView[videoIndex - 1].onUserTmpLeave();
        if (mIsHost) {
            // 3分钟秒后关闭
            mTmpLeaveHandler.removeMessages(videoIndex);
            mTmpLeaveHandler.sendEmptyMessageDelayed(videoIndex, 180000);
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLiveHelper == null && liveType!=LIVE_TYPE_LVB) {
            // back from 边看边买
            bindAllField();
            s_Intance = new WeakReference<HostWindowActivity>(this);
        }

        if(lvbLiveHelper != null) {
            lvbLiveHelper.resume();
        }

        //当关闭屏幕回到页面时，调用该方法来恢复直播，
        if(mLiveHelper != null)
            mLiveHelper.resume();
        if (mAVUIControl != null) {
            mAVUIControl.onResume();
        }
        if (mUIHandler!=null)
            mUIHandler.onResume();
        MLog.v(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveHelper.pause();
        mUIHandler.onPause();
        if (mAVUIControl != null) {
            mAVUIControl.onPause();
        }
        if(lvbLiveHelper != null)
            lvbLiveHelper.pause();
        MLog.v(TAG, "onPause");
    }

    @Override
    public void onBackPressed() {
        if (mUIHandler != null && !mUIHandler.onBackPressed()) {
            // 防止在边看边买回来后， mUIHandler尚未初始化问题
            onFinish();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void close(){
        onFinish();
    }

    void onFinish() {
        MLog.v(TAG, "onFinish");
        if (mEnterLiveHelper != null && mEnterLiveHelper.isInAVRoom()) {
            if ((liveType==LIVE_TYPE_PACK || liveType==LIVE_TYPE_LVB) && mUIHandler.mLiveHttpRequest.isServerKnowThisLive() && !isGroup) {
                // 背包状态
                mUIHandler.mLiveHttpRequest.notifyServerPackLiveStop();
            }else if((liveType==LIVE_TYPE_PACK || liveType==LIVE_TYPE_LVB) && mUIHandler.mLiveHttpRequest.isServerKnowThisLive() && isGroup){
                mUIHandler.mLiveHttpRequest.notifyServerGroupLiveStop();
            }
            mLiveHelper.perpareQuitRoom(true);
        } /*else {
            finish();
        }*/
        finish();
    }

    void dealWithOtherLive() {
        if (s_Intance != null && s_Intance.get() != this) {
            HostWindowActivity old = s_Intance.get();
            old.setContentView(new FrameLayout(this));
            old.destroyResource();
        }
    }

    void destroyResource() {
        unRegisterReceiver();
        if (lvbLiveHelper != null)
            lvbLiveHelper.destroy();

        if (mAVUIControl != null) {
            mAVUIControl.onDestroy();
            mAVUIControl = null;
        }

        if (mIjkVideoView != null) {
            mIjkVideoView.stopPlayback();
            mIjkVideoView = null; // help gc
        }
        // 反初始化MySelfInfo
        HeartBeatHelper.getInstance().onDestroy();
        if (mEnterLiveHelper != null) {
            if (mEnterLiveHelper.isInAVRoom()) {
                onFinish();
            }
            HostWindowTimerTask.closeTimer();
            mLiveHelper.onDestroy();
            mEnterLiveHelper.onDestroy();
            mEnterLiveHelper = null;
            mLiveHelper = null; // help gc
        }
        if (mUIHandler != null) {
            mUIHandler.onDestroy();
            mUIHandler = null; // help gc
        }
        CurLiveInfo.destroyInstance();
    }

    @Override
    protected void onDestroy() {
        MLog.v(TAG, "onDestroy");
        super.onDestroy();
        destroyResource();
        if (s_Intance != null) {
            s_Intance.clear();
            s_Intance = null;
        }
    }

    @Override
    public void onLayoutSmallVideoArea(final int index, final int left, final int top, int right, int bottom) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                View smallVideoContainer = mUIHandler.mSmallVideoContainer[index - 1];
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) smallVideoContainer.getLayoutParams();
                layoutParams.leftMargin = left;
                layoutParams.topMargin = top;
                smallVideoContainer.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    public void onVideoOrderChanged(String[] ids) {
        mUIHandler.refreshSmallVideoBlurName(ids);
    }

    public void switchCamera(){
        if (liveType == LIVE_TYPE_LVB) {
            lvbLiveHelper.changeCamera();
        } else if (liveType == LIVE_TYPE_NORMAL) {
            mLiveHelper.switchCamera();
        }
    }
}
