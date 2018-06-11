package com.tencent.qcloud.suixinbo.presenters;

import android.content.Context;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.TIMElem;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMValueCallBack;
import com.tencent.av.TIMAvManager;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVView;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 用于左右两个视频见的直播辅助类
 * 用于PK视频
 * Created by ldkxingzhe on 2016/8/25.
 */
public class LeftRightLiveHelper extends AbstractLiveHelper{
    @SuppressWarnings("unused")
    private static final String TAG = "LeftRightLiveHelper";

    public interface LiveView{
        void setMirror(boolean isMirror);
        void showVideoView(boolean isHost, String id);
        void closeMemberView(String id);
        void startRecordCallback(boolean isSucc);
        void stopRecordCallback(boolean isSucc, List<String> files);
        void pushStreamSucc(TIMAvManager.StreamRes streamRes);
        void stopStreamSucc();
    }
    private LiveView mLiveView;
    public LeftRightLiveHelper(Context context, LiveView liveView) {
        super(context);
        mLiveView = liveView;
    }

    /**
     * 请求多路视频
     * @param identifiers
     */
    public void requestViewList(ArrayList<String> identifiers) {
        requestViewList(identifiers, mRequestViewListCompleteCallback);
    }

    /**
     * 开始直播
     */
    public void startLive(){
        // TODO:　用户信息
        sendGroupMessage(Constants.AVIMCMD_PK_USER_START_LIVE, "", new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "send PK start failed: i is " + i + ", and error reason is " + s);
                Toast.makeText(mContext, "初始化失败, 请稍后重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMMessage message) {
                SxbLog.v(TAG, "send Pk Start success, and start live");
                changeAuthandRole(true, Constants.VIDEO_MEMBER_AUTH, Constants.VIDEO_MEMBER_ROLE);
            }
        });
    }

    /**
     * 结束直播
     */
    public void stopLive(){
        sendGroupMessage(Constants.AVIMCMD_PK_USER_STOP_LIVE, "", new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "stopLive and error code is " + i + ", and error reason is " + s);
            }

            @Override
            public void onSuccess(TIMMessage message) {
                SxbLog.v(TAG, "stopLive success");
                changeAuthandRole(false, Constants.NORMAL_MEMBER_AUTH, Constants.NORMAL_MEMBER_ROLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TIMManager.getInstance().removeMessageListener(msgListener);
    }



    @Override
    protected void handleCustomMsg(TIMElem elem, String identifier, String nickname, String userImg) {
        // 自定义消息的处理
        super.handleCustomMsg(elem, identifier, nickname, userImg);
    }

    private AVRoomMulti.RequestViewListCompleteCallback mRequestViewListCompleteCallback = new AVRoomMulti.RequestViewListCompleteCallback() {

        public void OnComplete(String identifierList[], AVView viewList[], int count, int result) {
            String ids = "";

            for (String id : identifierList) {
                if(MySelfInfo.getInstance().getId().equals(id)){
                    mLiveView.showVideoView(true, id);
                }else{
                    mLiveView.showVideoView(false, id);
                }
                ids = ids + " " + id;
            }
            SxbLog.d(TAG, LogConstants.ACTION_VIEWER_SHOW + LogConstants.DIV + MySelfInfo.getInstance().getId() + LogConstants.DIV + "get stream data"
                    + LogConstants.DIV + LogConstants.STATUS.SUCCEED + LogConstants.DIV + "ids " + ids);
            SxbLog.d(TAG, "RequestViewListCompleteCallback.OnComplete");
        }
    };

    @Override
    void onCameraPreviewChangeCallback(boolean isFrontCamera) {
        mLiveView.setMirror(isFrontCamera);
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
        param.setFilename(CurLiveInfo.getInstance().getTitle() + new Date().getTime());
        param.setSreenShot(false);
        param.setTransCode(true);
        param.setWaterMark(false);
        startRecord(param);
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


    public void stopRecord() {
        stopRecord(new TIMValueCallBack<List<String>>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "stop record error " + i + " : " + s);
                mLiveView.stopRecordCallback(false, null);
            }
            @Override
            public void onSuccess(List<String> files) {
                SxbLog.v(TAG, "stop record success ");
                mIsRecording = false;
                mLiveView.stopRecordCallback(true, files);
            }
        });
    }
}
