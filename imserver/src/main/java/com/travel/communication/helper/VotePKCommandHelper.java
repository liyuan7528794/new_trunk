package com.travel.communication.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.travel.Constants;
import com.travel.imserver.Callback;
import com.travel.imserver.IMManager;
import com.travel.imserver.ResultCallback;
import com.travel.imserver.bean.BaseBean;
import com.travel.imserver.bean.ClientData;
import com.travel.imserver.receiver.PKVoteReceiver;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 众投界面 pk视频的相关协议
 * Created by ldkxingzhe on 2016/7/20.
 */
public class VotePKCommandHelper implements ResultCallback<BaseBean> {
    @SuppressWarnings("unused")
    private static final String TAG = "VotePKCommandHelper";
    public interface Listener{
        void onStartPublish(String roomNum, String userId, String type, String url);
        void onStopPublish(String roomNum, String userId, String type);
        void onOpenPlayer(String roomNum);
        void onClosePlayer(String roomNum);
        void onRoomInfo(String buyerId, String buyerUrl, String sellerId, String sellerUrl);
        void onGetPlayUrl(String url, boolean isSuccess);
    }

    private Listener mListener;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private static final String IFS = " ";

    public static final int USER_TYPE_BUYER = 1;
    public static final int USER_TYPE_SELLER = 2;
    public static final int USER_TYPE_UNKNOWN = -1;
    
    private Context context;

    // 房间号
    private String mRoomNum = null;
    private String mUserId = null;
    private int mUserType = USER_TYPE_UNKNOWN;

    private boolean mHasEnterRoomed = false;

    public VotePKCommandHelper(Context context){
        this.context = context;
        IMManager.getInstance().registerReceiver("PKVoteReceiver", PKVoteReceiver.class, new PKVoteReceiver.MyResultReceiver(this));
    }

    public void onDestroy(){
        IMManager.getInstance().unRegisterReceiver("PKVoteReceiver");
    }

    /**
     * 设置房间号
     */
    public void setRoomId(String roomNum){
        mRoomNum = roomNum;
    }

    /**
     * 设置房间号
     */
    public void setRoomId(int voteId){
        mRoomNum = "stream-" + voteId;
        mHasEnterRoomed = false;
    }

    /**
     * 设置用户Id
     */
    public void setUserId(String userId, int type){
        mUserId = userId;
        mUserType = type;
    }

    /**
     * 进入房间
     */
    public void enterRoom(){
        IMManager.getInstance().changeRoom(mRoomNum, new Callback() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "进入房间成功");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e(TAG, "进入房间失败. and errorCode is " + errorCode);
            }
        });
        mHasEnterRoomed = true;

    }

    /**
     * 离开房间
     */
    public void leaveRoom(){
        IMManager.getInstance().changeRoom("home", new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
        mHasEnterRoomed = false;
    }


    /**
     * 开始发布
     */
    public void startPublish(){
        sendCommand("start_publish");
    }

    public void stopPublish(){
        sendCommand("stop_publish");
    }

    private void sendCommand(final String action){
        BaseBean baseBean = new BaseBean();
        ClientData clientData = new ClientData();
        clientData.setUserId(mUserId);
        clientData.setType("SM"+mUserType);
        baseBean.setClientData(clientData);
        baseBean.setId(action);
        baseBean.setType(3);
        baseBean.setMsgHead(action);
        baseBean.setMsgBody(String.valueOf(mUserType));
        baseBean.setSendUser(mUserId);
        baseBean.setRoom(mRoomNum);
        IMManager.getInstance().sendBaseBean(baseBean, new Callback() {
            @Override
            public void onSuccess() {
                MLog.v(TAG, "action %s command send success", action);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e(TAG, "action " + action + "command send failed, and errorCode is " + errorCode);
            }
        });
    }


    @Override
    public void onResult(BaseBean obj) {
        if(!mRoomNum.equals(obj.getRoom())){
            Log.e(TAG, "消息接受错误: " + obj);
            return;
        }
        if(mListener == null) return;
        if(!TextUtils.isEmpty(mUserId) && obj.getClientData() != null && mUserId.equals(obj.getClientData().getUserId())){
            Log.d(TAG, "本人PK消息");
            return;
        }

        String url = obj.getMsgBody();
        if(obj.getClientData() == null) return;
        String type = obj.getClientData().getType();
        if("SM1".equals(type)){
            type = "1";
        }else if("SM2".equals(type)){
            type = "2";
        }else if("PT".equals(type)) {

        }else{
            throw new IllegalArgumentException("错误类型");
        }
        if("start_publish".equals(obj.getMsgHead())){
            // 开始上麦
            mListener.onStartPublish(mRoomNum, obj.getClientData().getUserId(), type, url);
        }else if("stop_publish".equals(obj.getMsgHead())){
            // 停止上麦
            mListener.onStopPublish(mRoomNum, obj.getSendUser(), type);
        }else{
            Log.e(TAG, "unHandled msg " + obj.getMsgHead());
        }
    }

    public void initRoomInfo(int voteId){
        String url = Constants.Root_Url + "/publicVote/inStream.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("stream", "stream-"+voteId);

        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONObject data) {
                if(data!=null){

                    if(data.has("buyerUrl") && data.has("sellerUrl") ){
                        mListener.onRoomInfo(JsonUtil.getJson(data, "buyerId"),JsonUtil.getJson(data, "buyerUrl"),
                                JsonUtil.getJson(data, "sellerId"), JsonUtil.getJson(data, "sellerUrl"));
                    }
                }
            }
        }, map);
    }

    public void getNetPlayUrl(int voteId){
        String url = Constants.Root_Url + "/live/pkLiveAddress.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", mUserId);
        map.put("userType",mUserType);
        map.put("roomId","stream-"+voteId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONObject data) {
                if(data!=null){

                    if(data.has("pushRTMPURL") && data.has("pushRTMPURL") ){
                        if(!"".equals(JsonUtil.getJson(data, "pushRTMPURL"))){
                            mListener.onGetPlayUrl(JsonUtil.getJson(data, "pushRTMPURL"),true);
                        }else{
                            mListener.onGetPlayUrl("",false);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                mListener.onGetPlayUrl("",false);
            }
        }, map);
    }
}