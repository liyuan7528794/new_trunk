package com.tencent.qcloud.suixinbo.presenters;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMTextElem;
import com.tencent.TIMValueCallBack;
import com.tencent.av.TIMAvManager;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVView;
import com.tencent.qcloud.suixinbo.avcontrollers.QavsdkControl;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LiveView;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 直播的控制类presenter
 */
public class LiveHelper extends AbstractLiveHelper {
    private LiveView mLiveView;
    private static final String TAG = LiveHelper.class.getSimpleName();
    private static final int CAMERA_NONE = -1;
    private static final boolean LOCAL = true;
    private static final boolean REMOTE = false;
    private static final String UNREAD = "0";

    private float mBeautyParam;
    private float mWhiteBalanceParam;
    private boolean mIsFirstTimeInitBeautyParam = true;

    public static boolean S_IN_CROP = false;

    public LiveHelper(Context context, LiveView liveview) {
        super(context);
        mLiveView = liveview;
        if(mLiveView == null){
            throw new IllegalArgumentException("LiveView can't be null");
        }
        S_IN_CROP = false;
    }


    public boolean isFirstTime(){
        return mIsFirstTimeInitBeautyParam;
    }

    /**
     * AVSDK 请求主播数据
     */
    public void requestViewList(ArrayList<String> identifiers) {
        requestViewList(identifiers, mRequestViewListCompleteCallback);
    }


    private AVRoomMulti.RequestViewListCompleteCallback mRequestViewListCompleteCallback = new AVRoomMulti.RequestViewListCompleteCallback() {
        public void OnComplete(String identifierList[], AVView viewList[], int count, int result) {
            String ids = "";

            for (String id : identifierList) {
                if(MySelfInfo.getInstance().getId().equals(id)){
                    mLiveView.showVideoView(true, id);
                }else{
                    mLiveView.showVideoView(REMOTE, id);
                }
                ids = ids + " " + id;
            }
            SxbLog.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "get stream data"
                    + LogConstants.DIV + LogConstants.STATUS.SUCCEED + LogConstants.DIV + "ids " + ids);
            SxbLog.d(TAG, "RequestViewListCompleteCallback.OnComplete");
        }
    };

    /**
     * 已经发完退出消息了
     */
    private void notifyQuitReady() {
        if (mLiveView != null)
            mLiveView.readyToQuit();
    }

    public void perpareQuitRoom(boolean bPurpose) {
        if (bPurpose) {
            sendGroupMessage(Constants.AVIMCMD_ExitLive, "", new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
//                    notifyQuitReady();
                    // ignore
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
//                    notifyQuitReady();
                    // ignore
                }
            });
            notifyQuitReady();
        } else {
            notifyQuitReady();
        }
    }

    @Override
    public void resume() {
        if(S_IN_CROP) return;
        super.resume();
    }

    @Override
    public void pause() {
        if(S_IN_CROP) return;
        super.pause();
    }

    /**
     * 推流操作
     */
    public void pushAction(){
        TIMAvManager.StreamParam streamParam = TIMAvManager.getInstance().new StreamParam();
        streamParam.setChannelName("ldkxingzhe-test");
        streamParam.setEncode(TIMAvManager.StreamEncode.HLS);
        pushAction(streamParam);
    }

    public void pushAction(TIMAvManager.StreamParam mStreamParam) {
        //推流的接口
        pushAction(mStreamParam, new TIMValueCallBack<TIMAvManager.StreamRes>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "url error " + i + " : " + s);
//                Toast.makeText(mContext, "start stream error,try again " + i + " : " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMAvManager.StreamRes streamRes) {
                SxbLog.i(TAG, "push stream success ");
                mIsPushing = true;
                List<TIMAvManager.LiveUrl> liveUrls = streamRes.getUrls();
                streamChannelID = streamRes.getChnlId();
                mLiveView.pushStreamSucc(streamRes);
            }
        });
    }

    public void stopPushAction() {
        stopPushAction(new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "stop  push error " + i + " : " + s);
//                Toast.makeText(mContext, "stop stream error,try again " + i + " : " + s, Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onSuccess() {
                SxbLog.i(TAG, "stop push success ");
                mIsPushing = false;
                if (null != mLiveView) {
                    mLiveView.stopStreamSucc();
                }
            }
        });
    }


    /*  开始录制 */
    public void startRecord(){
        TIMAvManager.RecordParam param = TIMAvManager.getInstance().new RecordParam();
        param.setClassId(2);
        param.setFilename(CurLiveInfo.getInstance().getShare());
        param.setSreenShot(false);
        param.setTransCode(true);
        param.setWaterMark(false);
        startRecord(param);
    }

    public boolean isEnableBeauty(){
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        return avVideoCtrl.isEnableBeauty();
    }

    public float getBeautyParam(){
        return mBeautyParam;
    }

    public float getWhiteBalanceParam(){
        return mWhiteBalanceParam;
    }

    public void setBeautyParam(float param){
        mBeautyParam = param;
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        avVideoCtrl.inputBeautyParam(param);
    }

    public void setWhiteBalanceParam(float param){
        mWhiteBalanceParam = param;
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        avVideoCtrl.inputWhiteningParam(param);

    }

    /**
     * 开始录制
     */
    public void startRecord(TIMAvManager.RecordParam mRecordParam) {
        super.startRecord(mRecordParam, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "start record error " + i + "  " + s);
                mLiveView.startRecordCallback(false);
            }

            @Override
            public void onSuccess() {
                SxbLog.i(TAG, "start record success ");
                mIsRecording = true;
                mLiveView.startRecordCallback(true);
            }
        });
    }

    public void initBeauty(){
        if(mIsFirstTimeInitBeautyParam){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setBeautyParam(5);
                    setWhiteBalanceParam(1);
                    mIsFirstTimeInitBeautyParam = false;
                }
            }, 400);
        }
    }

    public void stopRecord() {
        stopRecord(new TIMValueCallBack<List<String>>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "stop record error " + i + " : " + s);
                if(mLiveView != null){
                    mLiveView.stopRecordCallback(false, null);
                }
            }
            @Override
            public void onSuccess(List<String> files) {
                SxbLog.v(TAG, "stop record success ");
                mIsRecording = false;
                mLiveView.stopRecordCallback(true, files);
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mLiveView = null;
        Log.e(TAG, "LiveHelper onDestroy");
    }


    @Override
    void onCameraPreviewChangeCallback(boolean isFrontCamera) {
        mLiveView.setMirror(isFrontCamera);
    }

    /**
     * 申请连线
     */
    public void sendApplyInsertLine(String receiverId){
        Log.d(TAG, "sendApplyInsertLine, and receiverId is " + receiverId);
        sendC2CMessage(Constants.AVIMCMD_MUlTI_USER_APPLY_INSERT, "", receiverId);
    }

    public void sendCancelInsertWait(){
        Log.v(TAG, "sendCancelInsertWait");
        sendC2CMessage(Constants.AVIMCMD_CANCEL_INSERT_LINE, "", CurLiveInfo.getInstance().getHostID());
    }

    /**
     * 主播关闭插播方视频
     */
    public void sendCancelInteract(String insertMemberId){
        sendGroupMessage(Constants.AVIMCMD_MULTI_CANCEL_INTERACT, insertMemberId);
    }

    /**
     * 回复申请连线
     * @param receiverId   接受者Id(即申请者)
     * @param isReceived   true -- 接受连线
     */
    public void sendAnswerToApplyInsertLine(String receiverId, boolean isReceived){
        sendC2CMessage(isReceived ? Constants.AVIMCMD_RECEIVE_INSERT_LINE : Constants.AVIMCMD_CANCEL_INSERT_LINE,
                "", receiverId);
    }

    /**
     * 点赞
     */
    public void sendPraise(){
        sendGroupMessage(Constants.AVIMCMD_Praise, "");
    }

    /* 发送礼物指令 */
    public void sendGif(String giftBeanStr){
        sendGroupMessage(Constants.AVIMCMD_SEND_GIFT, giftBeanStr);
    }

    /**
     * 处理定制消息 赞 关注 取消关注
     * @param elem
     */
    @Override
    protected void handleCustomMsg(TIMElem elem, String identifier, String nickname, String userImg) {
        try {
            String customText = new String(((TIMCustomElem) elem).getData(), "UTF-8");
            SxbLog.i(TAG, "cumstom msg  " + customText);

            JSONTokener jsonParser = new JSONTokener(customText);
            // 此时还未读取任何json文本，直接读取就是一个JSONObject对象。
            // 如果此时的读取位置在"name" : 了，那么nextValue就是"yuanzhifei89"（String）
            JSONObject json = (JSONObject) jsonParser.nextValue();
            int action = json.getInt(Constants.CMD_KEY);
            String params = "";
            if(json.has(Constants.CMD_PARAM)){
                params = json.getString(Constants.CMD_PARAM);
            }
            switch (action) {
                case Constants.AVIMCMD_MUlTI_USER_APPLY_INSERT:
                    SxbLog.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "receive invite message" +
                            LogConstants.DIV + "id " + identifier);
                    mLiveView.onApplyInsertLine(identifier,nickname,userImg);
                    break;
                case Constants.AVIMCMD_MUlTI_JOIN:
                    Log.i(TAG, "handleCustomMsg " + identifier);
                    mLiveView.cancelInviteView(identifier);
                    break;
                case Constants.AVIMCMD_MUlTI_REFUSE:
                    mLiveView.cancelInviteView(identifier);
                    Toast.makeText(mContext, identifier + " refuse !", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.AVIMCMD_Praise:
                    mLiveView.refreshThumbUp();
                    break;
                case Constants.AVIMCMD_EnterLive:
                    //mLiveView.refreshText("Step in live", sendId);
                    if (mLiveView != null)
                        mLiveView.memberJoin(identifier, nickname, userImg);
                    break;
                case Constants.AVIMCMD_ExitLive:
                    //mLiveView.refreshText("quite live", sendId);
                    if (mLiveView != null)
                        mLiveView.memberQuit(identifier, nickname, userImg);
                    break;
                case Constants.AVIMCMD_MULTI_CANCEL_INTERACT://主播关闭摄像头命令
                    //如果是自己关闭Camera和Mic
                    changeAuthAndCloseMemberView(params);
                    break;
                case Constants.AVIMCMD_RECEIVE_INSERT_LINE:
                    // 主播同意插播
                    mLiveView.hideInviteDialog();
                    break;
                case Constants.AVIMCMD_CANCEL_INSERT_LINE:
                    // 插播者退出插播
                    mLiveView.cancelInsert(identifier);
                    break;
                case Constants.AVIMCMD_MULTI_HOST_CONTROLL_CAMERA:
                    toggleCamera();
                    break;
                case Constants.AVIMCMD_MULTI_HOST_CONTROLL_MIC:
                    toggleMic();
                    break;
                case Constants.AVIMCMD_Host_Leave:
                    mLiveView.hostLeave(identifier, nickname);
                    break;
                case Constants.AVIMCMD_Host_Back:
                    mLiveView.hostBack(identifier, nickname);
                    break;
                case Constants.AVIMCMD_SEND_GIFT:
                    mLiveView.onSendGif(params);
                    break;
                default:
                    break;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException ex) {
            // 异常处理代码
        }
    }

    /* 关闭成员View */
    public void changeAuthAndCloseMemberView(String closeId) {
        if (closeId.equals(MySelfInfo.getInstance().getId())) {//是自己
            changeAuthandRole(false, Constants.NORMAL_MEMBER_AUTH, Constants.NORMAL_MEMBER_ROLE);
        }
        //其他人关闭小窗口
        mLiveView.closeMemberView(closeId);
//        mLiveView.hideInviteDialog();
        mLiveView.refreshUI(closeId);
    }

    /**
     * 处理文本消息解析
     */
    @Override
    protected void handleTextMessage(TIMElem elem, String sendId, String name, String userImg) {
        TIMTextElem textElem = (TIMTextElem) elem;
        mLiveView.refreshText(textElem.getText(), sendId, name, userImg);
    }
}
