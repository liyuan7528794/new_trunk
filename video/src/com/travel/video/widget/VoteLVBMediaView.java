package com.travel.video.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.interfaces.LVBLiveInterface;
import com.tencent.qcloud.suixinbo.presenters.LVBLiveHelper;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.travel.communication.helper.VotePKCommandHelper;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;

import java.util.HashMap;
import java.util.Map;

/**
 * 众投中pk视频显示及控制
 * Created by Administrator on 2016/10/28.
 */

public class VoteLVBMediaView extends RelativeLayout implements View.OnClickListener,LVBLiveInterface,VotePKCommandHelper.Listener{
    private String Tag = "VoteLVBMediaView:";

    public final static int USER_LEFT = VotePKCommandHelper.USER_TYPE_BUYER;
    public final static int USER_RIGHT = VotePKCommandHelper.USER_TYPE_SELLER;
    public final static int USER_LOOKING = VotePKCommandHelper.USER_TYPE_UNKNOWN;

    public final static int START_VIDEO = 0x10001;
    public final static int CLOSE_VIDEO = 0x10002;
    public final static int START_BIG_VIDEO = 0x10003;
    public final static int CLOSE_BIG_VIDEO = 0x10004;
    public final static int ROOM_INFO = 0x10005;
    public final static int SEND_START_LIVE = 0x10006;//开始直播
    public final static int SEND_STOP_LIVE = 0x10007;//发送停止直播

    private Context context;
    private View rootView;
    private RelativeLayout leftContain, rightContain;
    private FrameLayout leftVideoView,rightVideoView;
    private RelativeLayout leftRe,rightRe,leftCoverRe,rightCoverRe;
    private ImageView leftCover, rightCover;
    private ImageView leftClose, rightClose;
    private ImageView leftChangeCamera, rightChangeCamera;
    private ImageView leftFullScreen, rightFullScreen;
    private TextView leftNameText,rightNameText;

    private LVBLiveHelper mLvbLiveHelper;
    private VotePKCommandHelper helper;
    private VoteLVBMediaView.PKVideoListener listener;

    private int userType;
    private String stream = "";
    private int voteId = 0;
    private String mLeftId = "0";
    private String mRightId = "0";
    private boolean isLeft = false;//是否是左边播放视频
    private boolean isHost = false; //是否是右边的直播

//    private PKHeartBeatHelper heartBeatHelper;

    public VoteLVBMediaView(Context context) {
        this(context,null);
    }

    public VoteLVBMediaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoteLVBMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public interface PKVideoListener{
        /** 播放状态 true表示正在播放，否则播放结束 */
        public void playStatus(boolean isPlaying);
        public void hideLiveButton();
        public void showLiveButton();
        public void videoFullScreen(boolean isHost);
        public void videoZoomScreen();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.left_close :
            case R.id.right_close :
                stopLive();
                break;
            case R.id.left_change_camera :
            case R.id.right_change_camera :
                changeCamera();
                break;
            case R.id.left_full_screen :
            case R.id.right_full_screen :
                listener.videoFullScreen(isHost);
                break;
        }

    }

    private void init(){
        rootView = LayoutInflater.from(context).inflate(R.layout.vote_lvb_media_layout, null);
        initView();

        mLvbLiveHelper = new LVBLiveHelper(context, this);
        helper = new VotePKCommandHelper(context);
        helper.setListener(this);
//        heartBeatHelper = new PKHeartBeatHelper();
    }

    private void initView() {
        leftContain = (RelativeLayout) rootView.findViewById(R.id.leftContain);
        rightContain = (RelativeLayout) rootView.findViewById(R.id.rightContain);
        leftVideoView = (FrameLayout) rootView.findViewById(R.id.left_video_view);
        rightVideoView = (FrameLayout) rootView.findViewById(R.id.right_video_view);
        leftRe = (RelativeLayout) rootView.findViewById(R.id.leftRe);
        rightRe = (RelativeLayout) rootView.findViewById(R.id.rightRe);
        leftFullScreen = (ImageView) rootView.findViewById(R.id.left_full_screen);
        rightFullScreen = (ImageView) rootView.findViewById(R.id.right_full_screen);
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
        leftFullScreen.setOnClickListener(this);
        rightClose.setOnClickListener(this);
        rightChangeCamera.setOnClickListener(this);
        rightFullScreen.setOnClickListener(this);

        leftCover = (ImageView) rootView.findViewById(R.id.leftCover);
        rightCover = (ImageView) rootView.findViewById(R.id.rightCover);

        leftNameText = (TextView) rootView.findViewById(R.id.leftName);
        rightNameText = (TextView) rootView.findViewById(R.id.rightName);

        leftCoverRe = (RelativeLayout) rootView.findViewById(R.id.leftCoverRe);
        rightCoverRe = (RelativeLayout) rootView.findViewById(R.id.rightCoverRe);

        addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT, OSUtil.getScreenWidth()/2*3/2));
    }

    /**
     *  设定用户类型，并初始化显示布局
     */
    public void initShowLayout(int voteId,String leftId,String rightId){
        stream = "stream-" + voteId;
        this.voteId = voteId;
        this.mLeftId = leftId;
        this.mRightId = rightId;
        if(leftId.equals(UserSharedPreference.getUserId())){
            //蓝方，左边为采集端
            userType = USER_LEFT;
        }else if(rightId.equals(UserSharedPreference.getUserId())){
            //红方，右边为采集端
            userType = USER_RIGHT;
        }else{
            //观看方
            userType = USER_LOOKING;
        }

        helper.setRoomId(stream);
        helper.setUserId(UserSharedPreference.getUserId(), userType);
        helper.enterRoom();
        helper.initRoomInfo(voteId);
//        heartBeatHelper.setRoomNum(stream);
//        heartBeatHelper.setUserId(UserSharedPreference.getUserId());
//        heartBeatHelper.setUserType(userType);
    }

    public void initData(String leftName,String leftImg,String rightName,String rightImg){
        //初始化封面
        leftNameText.setText("买方·" + leftName);
        rightNameText.setText("卖方·" + rightName);
        ImageDisplayTools.displayCircleImage(leftImg, leftCover, OSUtil.dp2px(context, 1), context.getResources().getColor(R.color.red_EC6262));
        ImageDisplayTools.displayCircleImage(rightImg, rightCover, OSUtil.dp2px(context, 1), context.getResources().getColor(R.color.blue_6DB7CA));
    }

    public void setListener(VoteLVBMediaView.PKVideoListener listener){
        this.listener = listener;
    }


    private void startVideo(boolean isLeft,String videoUrl){
        // 隐藏开始直播按钮
        isHost = false;
        this.isLeft = isLeft;
        if(userType != USER_LOOKING)
            listener.hideLiveButton();

        initVideo(isLeft);
        mLvbLiveHelper.startVideo(videoUrl);

//        videoPopWindow.initData("直播人昵称", Constants.DefaultHeadImg, "share");
    }
    private TXCloudVideoView videoView;
    private FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT);
    private void initVideo(boolean isLeft){
        videoView = new TXCloudVideoView(context);
        if(isLeft){
            if(leftVideoView.getChildCount() > 0) leftVideoView.removeAllViews();
            leftVideoView.addView(videoView, frameLayoutParams);
        }else{
            if(rightVideoView.getChildCount() > 0) rightVideoView.removeAllViews();
            rightVideoView.addView(videoView, frameLayoutParams);
        }
        mLvbLiveHelper.initLVBLive(LVBLiveHelper.LVB_WATCH_LIVE,videoView);
//        mLvbLiveHelper.resume();
    }

    private void stopVideo(){
        if(mLvbLiveHelper!=null){
            mLvbLiveHelper.destroy();
        }

        if(userType != USER_LOOKING)
            listener.showLiveButton();
    }

    public void getUrlAndStartLive(){
        helper.getNetPlayUrl(voteId);
    }

    public void startLive(final String url){
        // 隐藏开始直播按钮
        listener.hideLiveButton();
        listener.playStatus(true);
        videoView = new TXCloudVideoView(context);
        if(userType == USER_LEFT){
            if(leftVideoView.getChildCount() > 0) leftVideoView.removeAllViews();
            leftVideoView.addView(videoView, frameLayoutParams);
            leftClose.setVisibility(View.VISIBLE);
            leftChangeCamera.setVisibility(View.VISIBLE);
            leftCoverRe.setVisibility(View.GONE);
            isLeft = true;
        }else if(userType == USER_RIGHT){
            if(rightVideoView.getChildCount() > 0) rightVideoView.removeAllViews();
            rightVideoView.addView(videoView, frameLayoutParams);
            rightClose.setVisibility(View.VISIBLE);
            rightChangeCamera.setVisibility(View.VISIBLE);
            rightCoverRe.setVisibility(View.GONE);
            isLeft = false;
        }
        isHost = true;
        mLvbLiveHelper.initLVBLive(LVBLiveHelper.LVB_LIVE, videoView);
        mLvbLiveHelper.pause();
        mLvbLiveHelper.startPush(url);
        resume();

//        videoPopWindow.initData("直播人昵称", Constants.DefaultHeadImg, "share");
//        heartBeatHelper.startSendHeartBeat();
    }

    /** 停止直播 */
    public void stopLive(){
        isHost = false;
        listener.showLiveButton();
        helper.stopPublish();
        mLvbLiveHelper.destroy();
//        heartBeatHelper.onDestroy();
        hideWindow();
    }

    public void resume(){
		mLvbLiveHelper.resume();
    }

    public void pause(){
        mLvbLiveHelper.pause();
    }

    public void destroy(){
        if(mLvbLiveHelper.isPushing()){
            helper.stopPublish();
        }
        mLvbLiveHelper.destroy();
//        heartBeatHelper.onDestroy();
        helper.leaveRoom();
        helper.onDestroy();
    }

    public void changeCamera(){
        leftChangeCamera.setClickable(false);
        rightChangeCamera.setClickable(false);
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                leftChangeCamera.setClickable(true);
                rightChangeCamera.setClickable(true);
            }
        }, 1000);

        mLvbLiveHelper.changeCamera();
    }

    private void hideWindow() {
        listener.playStatus(false);
        listener.videoZoomScreen();
        leftCoverRe.setVisibility(View.VISIBLE);
        leftClose.setVisibility(View.GONE);
        leftChangeCamera.setVisibility(View.GONE);
        rightClose.setVisibility(View.GONE);
        rightChangeCamera.setVisibility(View.GONE);
        rightCoverRe.setVisibility(View.VISIBLE);
    }

    private void bigCoverShow(boolean isLeft){
        listener.playStatus(true);
        if(isLeft){
            leftCoverRe.setVisibility(View.GONE);
        }else{
            rightCoverRe.setVisibility(View.GONE);
        }
    }

    private void startVideo(String buyerUrl, String sellerUrl){
        if(!"".equals(buyerUrl) && !"-1".equals(buyerUrl)){
            startVideo(true,buyerUrl);
        }

        if(!"".equals(sellerUrl) && !"-1".equals(sellerUrl)){
            startVideo(false,sellerUrl);
        }
    }

    private Handler handler = new Handler() {
        // 回调处理
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String tmp = msg.getData().getString("msg");
            Log.e("what:", msg.what+"");
            switch (msg.what) {
                case SEND_START_LIVE:
                    // 开始直播，改变状态
//                    helper.startPublish();
                    break;
                case START_VIDEO:
                    Map<String,String> map = (Map<String, String>) msg.obj;
                    String url = map.get("url");
                    String type = map.get("type");

                    if((USER_LEFT+"").equals(type)){
                        startVideo(url, "-1");
                    }else if((USER_RIGHT+"").equals(type)){
                        startVideo("-1", url);
                    }
                    break;
                case CLOSE_VIDEO:
                    Map<String,String> closeMap = (Map<String, String>) msg.obj;
                    String closeType = closeMap.get("type");
                    if((USER_LEFT+"").equals(closeType)){
                        leftCoverRe.setVisibility(View.VISIBLE);
                    }else if((USER_RIGHT+"").equals(closeType)){
                        rightCoverRe.setVisibility(View.VISIBLE);
                    }
                    stopVideo();
                    break;
                case  ROOM_INFO:
                    // 初始化房间信息
                    Map<String,String> mapRoom = (Map<String, String>) msg.obj;
                    startVideo(mapRoom.get("buyerUrl"),mapRoom.get("sellerUrl"));

                    break;
                default:
                    break;
            }
        }
    };

    /*private boolean isWaiting = false;
    private void videoZoomScreen(){
        if(!videoPopWindow.isShowing() && !isWaiting)
            return;
        isWaiting = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isWaiting = false;
            }
        },500);

        View view = videoPopWindow.removeVideoView();
        if(isLeft)
            leftContain.addView(view);
        else
            rightContain.addView(view);

        videoPopWindow.dismiss();
    }

    private void videoFullScreen(){
        if(isLeft) {
            if (leftContain.getChildCount() > 0){
                leftContain.removeView(leftVideoView);
            }
            videoPopWindow.addVideoView(leftVideoView);
        }else {
            if(rightContain.getChildCount()>0)
                rightContain.removeView(rightVideoView);
            videoPopWindow.addVideoView(rightVideoView);
        }
        videoPopWindow.show();
    }*/

    public void addVideoView(View view){
        if(isLeft)
            leftContain.addView(view);
        else
            rightContain.addView(view);
    }

    public View removeVideoView(){
        View view = null;
        if(isLeft && leftContain.getChildCount() > 0) {
            leftContain.removeView(leftVideoView);
            view = leftVideoView;
        }else if(!isLeft && rightContain.getChildCount()>0){
            rightContain.removeView(rightVideoView);
            view =  rightVideoView;
        }
        return view;
    }

    @Override
    public void onStartPublish(String roomNum, String userId,String type,String url) {
        Log.e("VoteMedia", "onStartPublish");
        if(url==null || "".equals(url) || "-1".equals(url) || "".equals(url) || "-1".equals(url)) return;

        Map<String,String> map = new HashMap<String, String>();
        Message msg = new Message();
        map.put("url", url);
        map.put("type", type);
        msg.obj = map;
        msg.what = START_VIDEO;
        handler.sendMessage(msg);
    }

    @Override
    public void onStopPublish(String roomNum, String userId,String type) {
        Log.e("VoteMedia", "onStopPublish");
        if("PT".equals(type)){
            if(mLeftId.equals(userId)){
                type = USER_LEFT+"";
            }else{
                type = USER_RIGHT+"";
            }
        }
        Map<String,String> map = new HashMap<String, String>();
        Message msg = new Message();
        map.put("type", type);
        msg.obj = map;
        msg.what = CLOSE_VIDEO;
        handler.sendMessage(msg);
    }

    @Override
    public void onOpenPlayer(String roomNum) {
        Log.e("VoteMedia", "onOpenPlayer");
        handler.sendEmptyMessage(START_BIG_VIDEO);
            handler.sendEmptyMessage(START_VIDEO);
    }

    @Override
    public void onClosePlayer(String roomNum) {
        Log.e("VoteMedia", "onClosePlayer");
        handler.sendEmptyMessage(CLOSE_BIG_VIDEO);
            handler.sendEmptyMessage(CLOSE_VIDEO);
    }

    @Override
    public void onRoomInfo(String buyerId,String buyerUrl,String sellerId,String sellerUrl) {
        if(("".equals(buyerUrl) || "-1".equals(buyerUrl)) && ("".equals(sellerUrl) || "-1".equals(sellerUrl))) return;
        Message msg = new Message();
        Map<String,String> map = new HashMap<String, String>();
        map.put("buyerUrl", buyerUrl);
        map.put("sellerUrl", sellerUrl);
        msg.obj = map;
        msg.what = ROOM_INFO;
        handler.sendMessage(msg);
    }

    @Override
    public void onGetPlayUrl(String url, boolean isSuccess) {
        if(isSuccess){
            startLive(url);
        }else{
            Toast.makeText(context,"生成直播地址失败！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPlayEvent(int event, Bundle bundle) {
        Log.e(Tag,"onPlayEvent : " + "status= "+event+" , bundle= "+bundle.toString());
        switch (event){
            case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                // 加载中

                break;
            case TXLiveConstants.PLAY_EVT_PLAY_BEGIN:
                // 开始播放
                bigCoverShow(isLeft);
                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT | TXLiveConstants.PLAY_EVT_PLAY_END:
                // 断开连接 或 播放结束
                hideWindow();
                break;
            case TXLiveConstants.PLAY_ERR_NET_DISCONNECT:
                //三次抢救失败
                hideWindow();
                break;
        }
    }

    @Override
    public void onPushEvent(int event, Bundle bundle) {
        Log.e(Tag,"onPushEvent : " + "status= "+event+" , bundle= "+bundle.toString());
        if (event < 0) {
            Toast.makeText(context, bundle.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
        switch (event) {
            case TXLiveConstants.PUSH_EVT_OPEN_CAMERA_SUCC :
                // 推流成功 发送消息
                handler.sendEmptyMessage(SEND_START_LIVE);
                break;
            case TXLiveConstants.PUSH_EVT_PUSH_BEGIN :
                // 与服务器握手完毕,一切正常，准备开始推流

                break;
            case TXLiveConstants.PUSH_WARNING_NET_BUSY :
                // 提示直播网络不好
                Toast.makeText(context,"哎吆！别刷淘宝了！\n网络信号不好使了！",Toast.LENGTH_SHORT).show();
                break;
            case TXLiveConstants.PUSH_ERR_NET_DISCONNECT:
                //网络断连,且经三次抢救无效,可以放弃治疗
				Toast.makeText(context, "人才啊！\n你尽然给直播失败了！", Toast.LENGTH_SHORT).show();
                stopLive();
                // 显示直播按钮
                if(userType != USER_LOOKING)
                    listener.showLiveButton();
                listener.videoZoomScreen();
                break;
        }
    }

    @Override
    public void onNetStatus(Bundle bundle) {
        Log.e(Tag,"onNetStatus : " + "bundle= "+bundle.toString());
    }

}