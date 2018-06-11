package com.travel.video.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMUserProfile;
import com.tencent.av.TIMAvManager;
import com.tencent.av.opengl.ui.GLRootView;
import com.tencent.av.sdk.AVView;
import com.tencent.qcloud.suixinbo.avcontrollers.LeftRightAVUIControl;
import com.tencent.qcloud.suixinbo.avcontrollers.QavsdkControl;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.LeftRightEnterLiveHelper;
import com.tencent.qcloud.suixinbo.presenters.LeftRightLiveHelper;
import com.tencent.qcloud.suixinbo.presenters.viewinface.EnterQuiteRoomView;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;
import com.travel.communication.helper.VotePKCommandHelper;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/9.
 */
public class VoteMediaViews extends RelativeLayout implements View.OnClickListener,LeftRightLiveHelper.LiveView{
    private final String TAG = "VoteMediaView_live:";
    //用户类型
    public final static int USER_LEFT = VotePKCommandHelper.USER_TYPE_BUYER;
    public final static int USER_RIGHT = VotePKCommandHelper.USER_TYPE_SELLER;
    public final static int USER_LOOKING = VotePKCommandHelper.USER_TYPE_UNKNOWN;

    private Context context;
    private View rootView;
    private PKVideoListener listener;
    private VotePKCommandHelper votePKCommandHelper;

    private GLRootView glRootView;
    private RelativeLayout leftCoverRe,rightCoverRe;
    private ImageView leftCover,rightCover,leftClose,leftChangeCamera,rightClose,rightChangeCamera;
    private TextView leftNameText,rightNameText;

    private boolean mIsHost = true;
    private LeftRightAVUIControl control;
    private LeftRightLiveHelper helper;
    private LeftRightEnterLiveHelper enterHelper;
    private int roomNum = 0;
    private String leftId = "-1";
    private int voteId = -1;

    //用户类型（卖方，买方，投票方）
    private int userType;

    public VoteMediaViews(Context context) {
        this(context,null);
    }

    public VoteMediaViews(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoteMediaViews(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }


    /** 是否隐藏pk视频布局接口 */
    public interface PKVideoListener{
        public void hideVideoView();
        public void showVideoView();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.left_close || v.getId() == R.id.right_close){
            //下麦按钮
            stopLive();
        }else if(v.getId() == R.id.left_change_camera || v.getId() == R.id.right_change_camera){
            //转换摄像头
            changeCamera();
        }
    }

    private void init(){
        rootView = LayoutInflater.from(context).inflate(R.layout.vote_media_layouts, null);

        initView();
        votePKCommandHelper = new VotePKCommandHelper(context);
    }

    private void initView() {
        glRootView = (GLRootView) rootView.findViewById(R.id.gLRootView);
        leftClose = (ImageView) rootView.findViewById(R.id.left_close);
        leftChangeCamera = (ImageView) rootView.findViewById(R.id.left_change_camera);
        rightClose = (ImageView) rootView.findViewById(R.id.right_close);
        rightChangeCamera = (ImageView) rootView.findViewById(R.id.right_change_camera);
        leftClose.setVisibility(View.GONE);
        leftChangeCamera.setVisibility(View.GONE);
        rightClose.setVisibility(View.GONE);
        rightChangeCamera.setVisibility(View.GONE);
        leftClose.setOnClickListener(this);
        leftChangeCamera.setOnClickListener(this);
        rightClose.setOnClickListener(this);
        rightChangeCamera.setOnClickListener(this);

        leftCover = (ImageView) rootView.findViewById(R.id.leftCover);
        rightCover = (ImageView) rootView.findViewById(R.id.rightCover);

        leftNameText = (TextView) rootView.findViewById(R.id.leftName);
        rightNameText = (TextView) rootView.findViewById(R.id.rightName);

        leftCoverRe = (RelativeLayout) rootView.findViewById(R.id.leftCoverRe);
        rightCoverRe = (RelativeLayout) rootView.findViewById(R.id.rightCoverRe);

        addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT, OSUtil.getScreenWidth()/2*3/2));
    }

    private void initTencentDate() {
        String hostId = HLLXLoginHelper.PREFIX + leftId;
        String hostName = "";
        String hostImg = "";
        roomNum = Integer.valueOf(voteId + "0");
//        roomNum = 100000000 + voteId;
        String roomName = "";
        if(!UserSharedPreference.getUserId().equals(leftId)) mIsHost = false;
        if(mIsHost){
            MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
            MySelfInfo.getInstance().setJoinRoomWay(true);
            MySelfInfo.getInstance().setMyRoomNum(roomNum);
            CurLiveInfo.getInstance().setTitle(hostName);
            CurLiveInfo.getInstance().setHostID(hostId);
            CurLiveInfo.getInstance().setRoomNum(roomNum);
            QavsdkControl.getInstance().getAVContext().getVideoCtrl().inputWhiteningParam(40);
        }else{
            MySelfInfo.getInstance().setIdStatus(Constants.MEMBER);
            MySelfInfo.getInstance().setJoinRoomWay(true);
            CurLiveInfo.getInstance().setHostID(hostId);
            CurLiveInfo.getInstance().setHostHLLXUserId(Integer.valueOf(leftId));
            CurLiveInfo.getInstance().setTitle(roomName);
            CurLiveInfo.getInstance().setRoomNum(roomNum);
            CurLiveInfo.getInstance().setHostName(hostName);
            CurLiveInfo.getInstance().setHostAvator(hostImg);
            QavsdkControl.getInstance().getAVContext().getVideoCtrl().inputWhiteningParam(40);
        }
    }

    private void intTencentHelp() {
        helper = new LeftRightLiveHelper(context,this);
        enterHelper = new LeftRightEnterLiveHelper(context, new EnterQuiteRoomView() {
            @Override
            public void enterRoomComplete(int id_status, boolean succ) {
                control = new LeftRightAVUIControl(context, glRootView);
                control.setSelfId(MySelfInfo.getInstance().getId());
                control.setLeftId(HLLXLoginHelper.PREFIX+leftId);
                helper.setCameraPreviewChangeCallback();
                if(succ){
                    if(!mIsHost){
                        helper.sendGroupMessage(Constants.AVIMCMD_EnterLive, "");
                    }
                }
            }

            @Override
            public void quiteRoomComplete(int id_status, boolean succ, boolean isFromAvRoom) {

            }

            @Override
            public void memberQuiteLive(String[] list) {
                MLog.e(TAG,"memberQuiteLive::"+list.toString());
            }

            @Override
            public void memberJoinLive(String[] list) {
                MLog.e(TAG,"memberJoinLive::"+list.toString());
            }

            @Override
            public void alreadyInLive(String[] list) {
                MLog.e(TAG,"alreadyInLive::"+list.toString());
            }

            @Override
            public void onGetGroupMembersList(List<TIMUserProfile> memberList) {
                MLog.e(TAG,"onGetGroupMembersList::"+memberList.toString());
            }
        }, helper);
//        enterHelper.createIMRoom();
        enterHelper.startEnterRoom();
//        helper.setCameraPreviewChangeCallback();
        int s = 3;
    }

    /**
     * 设定用户类型，并初始化显示布局
     */
    public void initShowLayout(int voteId,String leftId,String rightId){
        this.voteId = voteId;
        this.leftId = leftId;
        if(leftId.equals(UserSharedPreference.getUserId())){
            userType = USER_LEFT;
        }else if(rightId.equals(UserSharedPreference.getUserId())){
            userType = USER_RIGHT;
        }else{
            userType = USER_LOOKING;
        }

        initTencentDate();
        intTencentHelp();
        registerReceiver();

        votePKCommandHelper.setRoomId("stream-"+roomNum);
        votePKCommandHelper.setUserId(UserSharedPreference.getUserId(), userType);
        votePKCommandHelper.enterRoom();

    }

    public void initData(String leftName,String leftImg,String rightName,String rightImg){
        //初始化封面
        leftNameText.setText(leftName);
        rightNameText.setText(rightName);
        ImageDisplayTools.displayCircleImage(leftImg, leftCover, OSUtil.dp2px(context, 3), R.color.white_alpha50);
        ImageDisplayTools.displayCircleImage(rightImg, rightCover, OSUtil.dp2px(context, 3), R.color.white_alpha50);
    }

    public void startLive(){
        String[] ids = control.getIdentifiers();
        if(userType == USER_LEFT ){
            if(control.getIdentifierByIndex(0)==null || "".equals(control.getIdentifierByIndex(0))){
                helper.changeAuthandRole(true, Constants.VIDEO_MEMBER_AUTH, Constants.HOST_ROLE);

            }else{
                Toast.makeText(context,"您当前已经在上麦了！",Toast.LENGTH_SHORT).show();
            }
        }

        if(userType == USER_RIGHT){
            if(control.getIdentifierByIndex(1)==null || "".equals(control.getIdentifierByIndex(1))){
                helper.changeAuthandRole(true, Constants.VIDEO_MEMBER_AUTH, Constants.HOST_ROLE);

            }else{
                Toast.makeText(context,"您当前已经在上麦了！",Toast.LENGTH_SHORT).show();
            }
        }

//        helper.openCameraAndMic();
    }

    private void stopLive(){
        helper.changeAuthandRole(false, Constants.NORMAL_MEMBER_AUTH, Constants.HOST_ROLE);
//        helper.closeCameraAndMic();
    }

    public void setListener(PKVideoListener listener){
        this.listener = listener;
    }

    /**
     * 创建房间
     */
    public void createRoom(){
        /*new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {*/
                        if(enterHelper!=null){
                            enterHelper.createIMRoom();
                        }
                    /*}
                },200);*/
    }

    /** 再回到页面 */
    public void resume(){
        helper.resume();
        if(control != null){
            control.onResume();
        }
    }

    public void pause(){
        helper.pause();
        if(control != null){
            control.onPause();
        }
    }

    /** 销毁资源 */
    public void destroy(){

        unRegisterReceiver();
        helper.onDestroy();
        enterHelper.onDestroy();
        if(control != null){
            control.onDestroy();
        }

        votePKCommandHelper.leaveRoom();
        votePKCommandHelper.onDestroy();
    }
    /**
     * 改变摄像头
     */
    public void changeCamera(){
        helper.switchCamera();
    }

    /**
     * 隐藏左边窗口
     */
    private void hideLeftWindow() {
        // TODO Auto-generated method stub
        leftCoverRe.setVisibility(View.VISIBLE);
        leftClose.setVisibility(View.GONE);
        leftChangeCamera.setVisibility(View.GONE);
    }

    /**
     * 隐藏右边窗口
     */
    private void hideRightWindow(){
        rightClose.setVisibility(View.GONE);
        rightChangeCamera.setVisibility(View.GONE);
        rightCoverRe.setVisibility(View.VISIBLE);
    }

    /**
     * 显示左边窗口
     */
    private void showLeftWindow(boolean isPublic) {
        // TODO Auto-generated method stub
        leftCoverRe.setVisibility(View.GONE);
        if(isPublic){
            leftClose.setVisibility(View.VISIBLE);
            leftChangeCamera.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示右边窗口
     */
    private void showRightWindow(boolean isPublic){
        rightCoverRe.setVisibility(View.GONE);
        if (isPublic){
            rightClose.setVisibility(View.VISIBLE);
            rightChangeCamera.setVisibility(View.VISIBLE);
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
        context.registerReceiver(mBroadcastReceiver, intentFilter);

        //电源键监听
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(homePressReceiver, filter);

    }

    private void unRegisterReceiver() {
        context.unregisterReceiver(mBroadcastReceiver);
        context.unregisterReceiver(homePressReceiver);
    }

    @Override
    public void setMirror(boolean isMirror) {
        control.setMirror(isMirror,MySelfInfo.getInstance().getId());
    }

    @Override
    public void showVideoView(boolean isHost, String id) {
        MLog.e(TAG,isHost+"---"+id);
        if(MySelfInfo.getInstance().getId().equals(id)){
            control.setLocalHasVideo(isHost,false,id);
            if(userType == USER_LEFT){
                showLeftWindow(true);
            }else{
                showRightWindow(true);
            }
            // TODO: 开始直播，通知服务器
            helper.pushAction();
        }else{
            if(id.startsWith(HLLXLoginHelper.PREFIX) && (HLLXLoginHelper.PREFIX + leftId).equals(id)){
                showLeftWindow(false);
            }else{
                showRightWindow(false);
            }
            control.setHasRemoteVideo(true,id, AVView.VIDEO_SRC_TYPE_CAMERA);
        }

    }

    @Override
    public void closeMemberView(String id) {
        control.closeMemberVideoView(id);
    }

    @Override
    public void startRecordCallback(boolean isSucc) {
        // TODO 开始录制回调
    }

    @Override
    public void stopRecordCallback(boolean isSucc, List<String> files) {
        // TODO 结束录制

    }

    @Override
    public void pushStreamSucc(TIMAvManager.StreamRes streamRes) {
        // TODO 开始推流回调
        votePKCommandHelper.startPublish();

    }

    @Override
    public void stopStreamSucc() {
        // TODO 停止推流回调

    }

    /**
     * home键监听
     */
    private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                //home键监听
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

                }
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
                //监听关闭屏幕
                // TODO: 有问题
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //AvSurfaceView 初始化成功
            if (action.equals(Constants.ACTION_SURFACE_CREATED)) {
                //打开摄像头
                /*if (MySelfInfo.getInstance().getIdStatus() == Constants.HOST) {
                    helper.openCameraAndMic();
                }*/

            }

            if (action.equals(Constants.ACTION_CAMERA_OPEN_IN_LIVE)) {//有人打开摄像头
                ArrayList<String> ids = intent.getStringArrayListExtra("ids");
                //如果是自己本地直接渲染
                for (String id : ids) {
                    showVideoView(true, id);
                }

                //其他人一并获取
                SxbLog.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "somebody open camera,need req data"
                        + LogConstants.DIV + LogConstants.STATUS.SUCCEED + LogConstants.DIV + "ids " + ids.toString());
                int requestCount = CurLiveInfo.getInstance().getCurrentRequestCount();
                helper.requestViewList(ids);
                requestCount = requestCount + ids.size();
                CurLiveInfo.getInstance().setCurrentRequestCount(requestCount);
            }

            if (action.equals(Constants.ACTION_CAMERA_CLOSE_IN_LIVE)) {//有人关闭摄像头
                ArrayList<String> ids = intent.getStringArrayListExtra("ids");
                //如果是自己本地直接渲染
                for (String id : ids) {
                    if (id.equals(MySelfInfo.getInstance().getId())) {
                        if(userType== USER_LEFT)
                            hideLeftWindow();
                        else
                            hideRightWindow();
                        helper.stopPushAction();
                        votePKCommandHelper.stopPublish();
                    }else{
                        if(id.startsWith(HLLXLoginHelper.PREFIX) && (HLLXLoginHelper.PREFIX + leftId).equals(id)){
                            hideLeftWindow();
                        }else{
                            hideRightWindow();
                        }
                    }
                    closeMemberView(id);
                }
            }

            if (action.equals(Constants.ACTION_SWITCH_VIDEO)) {//点击成员回调

            }
            if (action.equals(Constants.ACTION_HOST_LEAVE)) {//主播结束

            }
        }
    };

}