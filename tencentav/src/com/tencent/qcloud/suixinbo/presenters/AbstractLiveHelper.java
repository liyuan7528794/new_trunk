package com.tencent.qcloud.suixinbo.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupSystemElem;
import com.tencent.TIMGroupSystemElemType;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.TIMAvManager;
import com.tencent.av.sdk.AVAudioCtrl;
import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVView;
import com.tencent.qcloud.suixinbo.avcontrollers.QavsdkControl;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.utils.Constants;
import com.tencent.qcloud.suixinbo.utils.LogConstants;
import com.tencent.qcloud.suixinbo.utils.SxbLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AV直播的控制类的一个抽象类
 * 提供了一下功能(基础功能, 不要设计业务):
 * TM消息功能: 发送C2C消息, 发送群消息, 初始化监听消息.
 * 摄像头功能: 打开摄像头, 关闭摄像头, 切换摄像头, 打开闪光灯, 关闭闪光灯
 * 权限相关的功能: 切换角色的功能
 * 录制功能: 开始录制, 结束录制
 * 推流功能:　开始推流，结束推流
 * 系统音量监听控制
 * Created by ldkxingzhe on 2016/8/11.
 */
public abstract class AbstractLiveHelper extends Presenter {
    protected static final int FRONT_CAMERA = 0;
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractLiveHelper";
    private static final int BACK_CAMERA = 1;


    protected Context mContext;
    protected TIMConversation mGroupConversation;
    protected TIMConversation mC2CConversation;

    private boolean isOpenCamera = false;
    private boolean isBakCameraOpen;
    private boolean isBakMicOpen;      // 切后时备份当前camera及mic状态
    private boolean isMicOpen = false;
    private boolean mIsFrontCamera = true;
    /**开关闪光灯*/
    private boolean flashLgihtStatus = false;
    protected boolean mIsRecording = false; // 是否录制中, 由子类设置
    protected boolean mIsPushing = false; // 是否在推流中, 由子类设置

    // 推流中使用到的变量
    protected TIMAvManager.RoomInfo roomInfo;
    protected long streamChannelID;

    private static final int MAX_REQUEST_VIEW_COUNT = Constants.VIDEO_VIEW_MAX; //当前最大支持请求画面个数
    private AVView mRequestViewList[] = new AVView[MAX_REQUEST_VIEW_COUNT];
    private String mRequestIdentifierList[] = new String[MAX_REQUEST_VIEW_COUNT];

    private AudioManager mAudioManager;

    /**
     * 群消息回调
     */
    protected TIMMessageListener msgListener = new TIMMessageListener() {
        @Override
        public boolean onNewMessages(List<TIMMessage> list) {
            //SxbLog.d(TAG, "onNewMessages readMessage " + list.size());
            //解析TIM推送消息
            parseIMMessage(list);
            return false;
        }
    };
    /**
     * 装换摄像头回调
     */
    private AVVideoCtrl.SwitchCameraCompleteCallback mSwitchCameraCompleteCallback = new AVVideoCtrl.SwitchCameraCompleteCallback() {
        protected void onComplete(int cameraId, int result) {
            super.onComplete(cameraId, result);

            if (result == AVError.AV_OK) {
                mIsFrontCamera = !mIsFrontCamera;
                setVideoFocus();
            }
        }
    };
    private AVVideoCtrl.CameraPreviewChangeCallback mCameraPreviewChangeCallback = new AVVideoCtrl.CameraPreviewChangeCallback() {
        @Override
        public void onCameraPreviewChangeCallback(int cameraId) {
            SxbLog.d(TAG, "mCameraPreviewChangeCallback.onCameraPreviewChangeCallback cameraId = " + cameraId);
            AbstractLiveHelper.this.onCameraPreviewChangeCallback(FRONT_CAMERA == cameraId);
        }
    };

    abstract void onCameraPreviewChangeCallback(boolean isFrontCamera);

    public AbstractLiveHelper(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /*
    * 返回是否正在推流 true -->
    * */
    public boolean isPushing(){
        return mIsPushing;
    }

    /* 返回是否正在录制 */
    public boolean isRecording(){
        return mIsRecording;
    }

    /**
     * 发送群组消息(纯文本消息)
     */
    public void sendGroupText(TIMMessage Nmsg) {
        if (mGroupConversation != null)
            mGroupConversation.sendMessage(Nmsg, new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                    if (i == 85) { //消息体太长
                        Toast.makeText(mContext, "Text too long ", Toast.LENGTH_SHORT).show();
                    } else if (i == 6011) {//群主不存在
                        Toast.makeText(mContext, "Host don't exit ", Toast.LENGTH_SHORT).show();
                    }
                    SxbLog.e(TAG, "send message failed. code: " + i + " errmsg: " + s);
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {
                    //发送成回显示消息内容
                    for (int j = 0; j < timMessage.getElementCount(); j++) {
                        TIMElem elem = (TIMElem) timMessage.getElement(0);
                        if (timMessage.isSelf()) {
                            handleTextMessage(elem, MySelfInfo.getInstance().getId(),
                                    MySelfInfo.getInstance().getNickName(),
                                    MySelfInfo.getInstance().getAvatar());
                        } else {
                            TIMUserProfile sendUser = timMessage.getSenderProfile();
                            String name, id, userImg;
                            if (sendUser != null) {
                                name = sendUser.getNickName();
                                id = sendUser.getIdentifier();
                                userImg = sendUser.getFaceUrl();
                            } else {
                                name = timMessage.getSender();
                                id = name;
                                userImg = name;
                            }
                            handleTextMessage(elem, id ,name, userImg);
                        }
                    }
                    SxbLog.i(TAG, "Send text Msg ok");

                }
            });
    }

    /**
     * 发送群组消息
     * @param cmd        指令代码
     * @param param      参数
     * @param callback   回调
     */
    public void sendGroupMessage(int cmd, String param, TIMValueCallBack<TIMMessage> callback) {
        JSONObject inviteCmd = new JSONObject();
        try {
            inviteCmd.put(Constants.CMD_KEY, cmd);
            inviteCmd.put(Constants.CMD_PARAM, param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String cmds = inviteCmd.toString();
        SxbLog.i(TAG, "send cmd : " + cmd + "|" + cmds);
        TIMMessage Gmsg = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(cmds.getBytes());
        elem.setDesc("");
        Gmsg.addElement(elem);

        if (mGroupConversation != null)
            mGroupConversation.sendMessage(Gmsg, callback);
    }

    /**
     * 发送群组消息, 忽略回调
     * @param cmd
     * @param param
     */
    public void sendGroupMessage(int cmd, String param) {
        sendGroupMessage(cmd, param, new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                if (i == 85) { //消息体太长
                    Toast.makeText(mContext, "Text too long ", Toast.LENGTH_SHORT).show();
                } else if (i == 6011) {//群主不存在
                    Toast.makeText(mContext, "Host don't exit ", Toast.LENGTH_SHORT).show();
                }
                SxbLog.e(TAG, "send message failed. code: " + i + " errmsg: " + s);
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                SxbLog.i(TAG, "onSuccess ");
            }
        });
    }

    /**
     * 初始化聊天室  设置监听器
     */
    public void initTIMListener(String chatRoomId) {
        SxbLog.v(TAG, "initTIMListener->current room id: " + chatRoomId);
        mGroupConversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, chatRoomId);
        TIMManager.getInstance().addMessageListener(msgListener);
        mC2CConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, chatRoomId);
    }

    /**
     * 解析消息回调
     * @param list 消息列表
     */
    private void parseIMMessage(List<TIMMessage> list) {
        List<TIMMessage> tlist = list;


        if (tlist.size() > 0) {
            if (mGroupConversation != null)
                mGroupConversation.setReadMessage(tlist.get(0));
            SxbLog.d(TAG, "parseIMMessage readMessage " + tlist.get(0).timestamp());
        }


        for (int i = tlist.size() - 1; i >= 0; i--) {
            TIMMessage currMsg = tlist.get(i);
            for (int j = 0; j < currMsg.getElementCount(); j++) {
                if (currMsg.getElement(j) == null)
                    continue;
                final TIMElem elem = currMsg.getElement(j);
                TIMElemType type = elem.getType();
                String sendId = currMsg.getSender();

                //系统消息
                if (type == TIMElemType.GroupSystem) {
                    if (TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE == ((TIMGroupSystemElem) elem).getSubtype()) {
                        mContext.sendBroadcast(new Intent(
                                Constants.ACTION_HOST_LEAVE));
                    }
                }
                final String id, nickname, userImg;
                if (currMsg.getSenderProfile() != null) {
                    id = currMsg.getSenderProfile().getIdentifier();
                    nickname = currMsg.getSenderProfile().getNickName();
                    userImg = currMsg.getSenderProfile().getFaceUrl();
                } else {
                    id = sendId;
                    nickname = sendId;
                    userImg = sendId;
                }
                //定制消息
                if (type == TIMElemType.Custom) {
                    if(currMsg.getConversation().getType() == TIMConversationType.Group){
                        handleCustomMsg(elem, id, nickname, userImg);
                    }else{
                        AbstractEnterLiveHelper.getUsersProfile(Arrays.asList(sendId), new TIMValueCallBack<List<TIMUserProfile>>() {
                            @Override
                            public void onError(int i, String s) {
                                SxbLog.e(TAG, "getUserProfile error, and reason is " + s);
                            }

                            @Override
                            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                                String _userImg = timUserProfiles.get(0).getFaceUrl();
                                String _nickName = timUserProfiles.get(0).getNickName();
                                handleCustomMsg(elem, id, _nickName, _userImg);
                            }
                        });
                    }
                    continue;
                }

                //其他群消息过滤

                if (currMsg.getConversation() != null && currMsg.getConversation().getPeer() != null)
                    if (!CurLiveInfo.getInstance().getChatRoomId().equals(currMsg.getConversation().getPeer())) {
                        continue;
                    }

                //最后处理文本消息
                if (type == TIMElemType.Text) {
                    if (currMsg.isSelf()) {
                        handleTextMessage(elem, MySelfInfo.getInstance().getId(),
                                MySelfInfo.getInstance().getNickName(),
                                MySelfInfo.getInstance().getAvatar());
                    } else {
                        handleTextMessage(elem, id, nickname, userImg);
                    }
                }
            }
        }
    }

    /**
     * 改变角色和权限 最终会控制自己Camera和Mic
     *
     * @param leverChange true代表上麦 false 代表下麦
     * @param auth_bits   权限字段
     * @param role        角色字段
     */
    public void changeAuthandRole(final boolean leverChange, long auth_bits, final String role) {
        changeAuthority(auth_bits, null, new AVRoomMulti.ChangeAuthorityCallback() {
            protected void onChangeAuthority(int retCode) {
                SxbLog.i(TAG, "changeAuthority auth " + retCode);
                if (retCode == AVError.AV_OK) {
                    changeRole(role, leverChange);
                }

            }
        });
    }

    /**
     * 改变权限
     *
     * @param auth_bits   权限
     * @param auth_buffer 密钥
     * @param callback
     * @return
     */
    private boolean changeAuthority(long auth_bits, byte[] auth_buffer, AVRoomMulti.ChangeAuthorityCallback callback) {
        SxbLog.d(TAG, " start change Auth ");
        QavsdkControl qavsdk = QavsdkControl.getInstance();
        AVContext avContext = qavsdk.getAVContext();
        AVRoomMulti room = (AVRoomMulti) avContext.getRoom();
        if (auth_buffer != null) {
            return room.changeAuthority(auth_bits, auth_buffer, auth_buffer.length, callback);
        } else {
            return room.changeAuthority(auth_bits, null, 0, callback);
        }
    }

    /**
     * 改变角色
     *
     * @param role 角色名
     */
    public void changeRole(final String role, final boolean leverupper) {
        ((AVRoomMulti) (QavsdkControl.getInstance().getRoom())).changeAVControlRole(role, new AVRoomMulti.ChangeAVControlRoleCompleteCallback() {
                    @Override
                    public void OnComplete(int arg0) {
                        if (arg0 == AVError.AV_OK) {
                            if (leverupper) {
                                openCameraAndMic();//打开摄像头
//                                sendC2CMessage(Constants.AVIMCMD_MUlTI_JOIN, "", CurLiveInfo.getHostID());//发送回应消息
                            } else {
                                SxbLog.standardMemberUnShowLog(TAG, "change role down", "" + LogConstants.STATUS.SUCCEED, "role " + role);
                                closeCameraAndMic();
                            }
//                            Toast.makeText(mContext, "change to VideoMember succ !", Toast.LENGTH_SHORT);
                        } else {
                            SxbLog.standardMemberUnShowLog(TAG, "change role ", "" + LogConstants.STATUS.FAILED, "code " + arg0);
//                            Toast.makeText(mContext, "change to VideoMember failed", Toast.LENGTH_SHORT);
                        }
                    }
                }

        );
    }

    protected void handleCustomMsg(TIMElem elem, String identifier, String nickname, String userImg){
        // ignore
    }

    protected void handleTextMessage(TIMElem elem, String sendId, String userName, String userImg){
        // ignore
    }

    /**
     * 开启摄像头和MIC
     */
    public void openCameraAndMic() {
        if(LiveHelper.S_IN_CROP) return;
        openCamera();
        AVAudioCtrl avAudioCtrl = QavsdkControl.getInstance().getAVContext().getAudioCtrl();//开启Mic
        avAudioCtrl.enableMic(true);
        isMicOpen = true;
    }

    /**
     * 打开摄像头
     */
    protected void openCamera() {
        if (mIsFrontCamera) {
            enableCamera(FRONT_CAMERA, true);
        } else {
            enableCamera(BACK_CAMERA, true);
        }
    }

    public void closeCameraAndMic() {
        closeCamera();
        closeMic();
    }

    /**
     * 开关摄像头
     */
    public void toggleCamera() {
        if (isOpenCamera) {
            closeCamera();
        } else {
            openCamera();
        }
    }

    /**
     * 开关Mic
     */
    public void toggleMic() {
        if (!isMicOpen) {
            openMic();
        } else {
            muteMic();
        }
        Log.d(TAG, "toggleMic and isMicOpen is " + isMicOpen);
    }

    public void closeCamera() {
        if (mIsFrontCamera) {
            enableCamera(FRONT_CAMERA, false);
        } else {
            enableCamera(BACK_CAMERA, false);
        }
    }

    public void closeMic() {
        AVAudioCtrl avAudioCtrl = QavsdkControl.getInstance().getAVContext().getAudioCtrl();//开启Mic
        avAudioCtrl.enableMic(false);
        isMicOpen = false;
    }

    /**
     * 开启摄像头
     *
     * @param camera
     * @param isEnable
     */
    private void enableCamera(final int camera, final boolean isEnable) {
        if(isOpenCamera == isEnable){
            SxbLog.i(TAG, "enableCamera, and isEnable is " + isEnable + ", and in this status");
            return;
        }
        if (isEnable) {
            isOpenCamera = true;
        } else {
            isOpenCamera = false;
        }
        SxbLog.i(TAG, "createlive enableCamera camera " + camera + "  isEnable " + isEnable);
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        //打开摄像头
        int ret = avVideoCtrl.enableCamera(camera, isEnable, new AVVideoCtrl.EnableCameraCompleteCallback() {
            protected void onComplete(boolean enable, int result) {//开启摄像头回调
                super.onComplete(enable, result);
                SxbLog.i(TAG, "createlive enableCamera result " + result);
                if (result == AVError.AV_OK && isEnable) {//开启成功
                    SxbLog.v(TAG, "enable camera and ok, is enable");
                    if (camera == LiveHelper.FRONT_CAMERA) {
                        mIsFrontCamera = true;
                    } else {
                        mIsFrontCamera = false;
                    }
                    setVideoFocus();
                }
            }
        });

        SxbLog.i(TAG, "enableCamera " + ret);

    }

    private void setVideoFocus() {
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        Object cameraPara;
        try{
            cameraPara = avVideoCtrl.getCameraPara();
        }catch (Exception e){
            Log.d(TAG, e.getMessage(), e);
            return;
        }
        if(cameraPara instanceof Camera.Parameters){
            Camera.Parameters parameters = (Camera.Parameters) cameraPara;
            if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                avVideoCtrl.setCameraPara(parameters);
            }
        }
    }

    public boolean isFrontCamera() {
        return mIsFrontCamera;
    }

    /**
     * 转换前后摄像头
     *
     * @return
     */
    public int switchCamera() {
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        int result = avVideoCtrl.switchCamera(mIsFrontCamera ? BACK_CAMERA : FRONT_CAMERA, mSwitchCameraCompleteCallback);
        return result;
    }

    public boolean isMicOpen() {
        return isMicOpen;
    }

    /**
     * 开启Mic
     */
    public void openMic() {
        AVAudioCtrl avAudioCtrl = QavsdkControl.getInstance().getAVContext().getAudioCtrl();//开启Mic
        avAudioCtrl.enableMic(true);
        isMicOpen = true;
    }

    /**
     * 关闭Mic
     */
    public void muteMic() {
        AVAudioCtrl avAudioCtrl = QavsdkControl.getInstance().getAVContext().getAudioCtrl();//关闭Mic
        avAudioCtrl.enableMic(false);
        isMicOpen = false;
    }

    public void toggleFlashLight() {
        AVVideoCtrl videoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        if (null == videoCtrl) {
            return;
        }

        final Object cam = videoCtrl.getCamera();
        if ((cam == null) || (!(cam instanceof Camera))) {
            return;
        }
        final Camera.Parameters camParam = ((Camera) cam).getParameters();
        if (null == camParam) {
            return;
        }

        Object camHandler = videoCtrl.getCameraHandler();
        if ((camHandler == null) || (!(camHandler instanceof Handler))) {
            return;
        }

        //对摄像头的操作放在摄像头线程
        if (flashLgihtStatus == false) {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        ((Camera) cam).setParameters(camParam);
                        flashLgihtStatus = true;
                    } catch (RuntimeException e) {
                        SxbLog.d("setParameters", "RuntimeException");
                    }
                }
            });
        } else {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        ((Camera) cam).setParameters(camParam);
                        flashLgihtStatus = false;
                    } catch (RuntimeException e) {
                        SxbLog.d("setParameters", "RuntimeException");
                    }

                }
            });
        }
    }

    public void sendC2CMessage(final int cmd, String Param, final String sendId) {
        JSONObject inviteCmd = new JSONObject();
        try {
            inviteCmd.put(Constants.CMD_KEY, cmd);
            inviteCmd.put(Constants.CMD_PARAM, Param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String cmds = inviteCmd.toString();
        SxbLog.i(TAG, "send cmd : " + cmd + "|" + cmds);
        TIMMessage msg = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(cmds.getBytes());
        elem.setDesc("");
        msg.addElement(elem);
        mC2CConversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, sendId);
        mC2CConversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                SxbLog.e(TAG, "enter error" + i + ": " + s);
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                SxbLog.i(TAG, "send praise succ !");
            }
        });
    }

    public void pause() {
        try{
            mContext.unregisterReceiver(mSystemVolumeReceiver);
            isBakCameraOpen = isOpenCamera;
            isBakMicOpen = isMicOpen;
            if (isBakCameraOpen || isBakMicOpen) {    // 若摄像头或Mic打开
                sendGroupMessage(Constants.AVIMCMD_Host_Leave, "", new TIMValueCallBack<TIMMessage>() {
                    @Override
                    public void onError(int i, String s) {
                    }

                    @Override
                    public void onSuccess(TIMMessage timMessage) {
                    }
                });
                closeCameraAndMic();
            }
        }catch (Exception e){
            Log.v(TAG, "unregisterReceiver exception");
        }
    }

    public void resume() {
        if (isBakCameraOpen || isBakMicOpen) {
            sendGroupMessage(Constants.AVIMCMD_Host_Back, "", new TIMValueCallBack<TIMMessage>() {
                @Override
                public void onError(int i, String s) {
                }

                @Override
                public void onSuccess(TIMMessage timMessage) {

                }
            });

            if (isBakCameraOpen) {
                openCamera();
            }
            if (isBakMicOpen) {
                openMic();
            }
        }
        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        mContext.registerReceiver(mSystemVolumeReceiver, filter);
    }

    public void setCameraPreviewChangeCallback() {
        AVVideoCtrl avVideoCtrl = QavsdkControl.getInstance().getAVContext().getVideoCtrl();
        if (avVideoCtrl != null)
            avVideoCtrl.setCameraPreviewChangeCallback(mCameraPreviewChangeCallback);
    }

    /* 开始录制 */
    protected void startRecord(TIMAvManager.RecordParam mRecordParam, TIMCallBack timCallBack) {
        if(mIsRecording) return;
        TIMAvManager.RoomInfo roomInfo = TIMAvManager.getInstance().new RoomInfo();
        roomInfo.setRelationId(CurLiveInfo.getInstance().getAVRoomNum());
        roomInfo.setRoomId(CurLiveInfo.getInstance().getAVRoomNum());
        TIMAvManager.getInstance().requestMultiVideoRecorderStart(roomInfo, mRecordParam, timCallBack);
    }

    /* 结束录制 */
    public void stopRecord(TIMValueCallBack<List<String>> callBack) {
        if(!mIsRecording) return;
        TIMAvManager.RoomInfo roomInfo = TIMAvManager.getInstance().new RoomInfo();
        roomInfo.setRelationId(CurLiveInfo.getInstance().getAVRoomNum());
        roomInfo.setRoomId(CurLiveInfo.getInstance().getAVRoomNum());
        TIMAvManager.getInstance().requestMultiVideoRecorderStop(roomInfo, callBack);
    }

    /* 开始推流 */
    protected void pushAction(TIMAvManager.StreamParam mStreamParam, TIMValueCallBack<TIMAvManager.StreamRes> callBack) {
        int roomid = (int) QavsdkControl.getInstance().getAVContext().getRoom().getRoomId();
        SxbLog.i(TAG, "Push roomid: " + roomid);
        roomInfo = TIMAvManager.getInstance().new RoomInfo();
        roomInfo.setRoomId(roomid);
        roomInfo.setRelationId(CurLiveInfo.getInstance().getAVRoomNum());
        //推流的接口
        TIMAvManager.getInstance().requestMultiVideoStreamerStart(roomInfo, mStreamParam, callBack);
    }

    /* 停止推流 */
    protected void stopPushAction(TIMCallBack callBack) {
        if(!mIsPushing) return;
        SxbLog.d(TAG, "Push stop Id " + streamChannelID);
        List<Long> myList = new ArrayList<Long>();
        myList.add(streamChannelID);
        TIMAvManager.getInstance().requestMultiVideoStreamerStop(roomInfo, myList, callBack);
    }

    /**
     * AVSDK 请求主播数据
     */
    protected void requestViewList(ArrayList<String> identifiers, AVRoomMulti.RequestViewListCompleteCallback callback) {
        SxbLog.i(TAG, "requestViewList " + identifiers);
        if (identifiers.size() == 0) return;
        AVEndpoint endpoint = ((AVRoomMulti) QavsdkControl.getInstance().getAVContext().getRoom()).getEndpointById(identifiers.get(0));
        SxbLog.d(TAG, "requestViewList hostIdentifier " + identifiers + " endpoint " + endpoint);
        if (endpoint != null) {
            ArrayList<String> alreadyIds = QavsdkControl.getInstance().getRemoteVideoIds();//已经存在的IDs

            SxbLog.i(TAG, "requestViewList identifiers : " + identifiers.size());
            SxbLog.i(TAG, "requestViewList alreadyIds : " + alreadyIds.size());
            for (String id : identifiers) {//把新加入的添加到后面
                if (!alreadyIds.contains(id)) {
                    alreadyIds.add(id);
                }
            }
            int viewindex = 0;
            for (String id : alreadyIds) {//一并请求
                if (viewindex >= MAX_REQUEST_VIEW_COUNT) break;
                AVView view = new AVView();
                view.videoSrcType = AVView.VIDEO_SRC_TYPE_CAMERA;
                view.viewSizeType = AVView.VIEW_SIZE_TYPE_BIG;
                //界面数
                mRequestViewList[viewindex] = view;
                mRequestIdentifierList[viewindex] = id;
                viewindex++;
            }
            int ret = QavsdkControl.getInstance().getRoom().requestViewList(mRequestIdentifierList, mRequestViewList, viewindex, callback);
        } else {
            Log.e(TAG, "Wrong Room!!!! Live maybe close already!");
        }
    }

    @Override
    public void onDestroy() {
        mContext = null;
        TIMManager.getInstance().removeMessageListener(msgListener);
    }

    private BroadcastReceiver mSystemVolumeReceiver = new  BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            int volume = currentVolume * 100 / maxVolume;
            QavsdkControl.getInstance().getAVContext().getAudioCtrl().setVolume(volume == 0 ? -1 : volume);
            SxbLog.v(TAG, "onReceive, and currentVolume is " + volume);
        }
    };
}
