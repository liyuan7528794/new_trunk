package com.travel.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ctsmedia.hltravel.BuildConfig;
import com.tencent.TIMConversationType;
import com.tencent.TIMManager;
import com.tencent.TIMOfflinePushListener;
import com.tencent.TIMOfflinePushNotification;
import com.tencent.TIMValueCallBack;
import com.tencent.qcloud.suixinbo.model.MySelfInfo;
import com.tencent.qcloud.suixinbo.presenters.LoginHelper;
import com.tencent.qcloud.suixinbo.presenters.viewinface.LoginView;
import com.travel.Constants;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.helper.NewSystemMessageHelper;
import com.travel.communication.helper.SQliteHelper;
import com.travel.communication.helper.TIMGroupMessageReceiver;
import com.travel.communication.helper.UnReadGroupMessageSyncTask;
import com.travel.imserver.Callback;
import com.travel.imserver.IMManager;
import com.travel.imserver.ResultCallback;
import com.travel.imserver.receiver.OrderMsgReceiver;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.sql.VideoVoteUtil;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldkxingzhe on 2016/9/1.
 * 登录辅助类, 所有的登录操作, 都需要在这里操作
 */
public class HLLXLoginHelper implements LoginView{
    @SuppressWarnings("unused")
    private static final String TAG = "HLLXLoginHelper";

    public static final String PREFIX = "us_";
    /** 登录成功的广播 */
    public static final String ACTION_LOGIN_SUCCESS = BuildConfig.APPLICATION_ID + "_hllx_login_success";

    private Context mContext;
    private LoginHelper mTencentLoginHelper;
    private NewSystemMessageHelper mSystemMessageHelper;
    private UnReadGroupMessageSyncTask mUnReadGroupMessageSyncTask;
    private static TIMGroupMessageReceiver mTIMessageReceiver;


    public HLLXLoginHelper(Context context){
        mContext = context;
        mTencentLoginHelper = new LoginHelper(context, this);
        mSystemMessageHelper = new NewSystemMessageHelper(mContext);
        TIMManager.getInstance().disableRecentContact();
        TIMManager.getInstance().removeMessageListener(mTIMessageReceiver);
        mTIMessageReceiver = new TIMGroupMessageReceiver(mContext);
        TIMManager.getInstance().addMessageListener(mTIMessageReceiver);
        mUnReadGroupMessageSyncTask = new UnReadGroupMessageSyncTask(context);
    }

    public void userLoginSuccess(JSONObject userJson){
        UserSharedPreference.saveUserInfo(userJson);
        UserSharedPreference.setIsLogin(true);
        String userId = JsonUtil.getJson(userJson, "id");
        String imgUrl = JsonUtil.getJson(userJson, "imgUrl");
        String nickName = JsonUtil.getJson(userJson, "nickName");
        String userSigature = JsonUtil.getJson(userJson, "userSigature");
        String ctsCid = JsonUtil.getJson(userJson, "ctsCid");
        UserSharedPreference.saveCtsCid(ctsCid);
        UserData userData = new UserData();
        userData.setId(userId);
        userData.setImgUrl(imgUrl);
        userData.setNickName(nickName);
        MessageEntity.mUserId = userId;
        new SQliteHelper(mContext).inserOrReplace(userData);
        IMManager.getInstance().setClientData(userId, nickName, imgUrl, new Callback() {
            @Override
            public void onSuccess() {
                MLog.d(TAG, "imserver 登录成功");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                MLog.e(TAG, "imserver 登录失败");
            }
        });
        MySelfInfo.getInstance().setId(PREFIX + userId);
        MySelfInfo.getInstance().setNickName(nickName);
        MySelfInfo.getInstance().setAvatar(imgUrl);
        MySelfInfo.getInstance().setMyRoomNum(Integer.valueOf(userId));
        MySelfInfo.getInstance().setUserSig(userSigature);
        MySelfInfo.getInstance().writeToCache(mContext);
        mTencentLoginHelper.stopAVSDK();
        mTencentLoginHelper.imLogin(MySelfInfo.getInstance().getId(), userSigature);
    }

    public void login(final String telephoneCode, final String password,
                      final TIMValueCallBack<String> callBack) {
        String url = Constants.Root_Url + "/login.do";

        Map<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("telephoneCode", telephoneCode);
        paraMap.put("password", password);
        paraMap.put("phoneModel", OSUtil.getPhoneModel());
        paraMap.put("osVersion", OSUtil.getOSVersion());
        if(OSUtil.getLocalIpAddress()!=null)
            paraMap.put("clientIp", OSUtil.getLocalIpAddress());
        System.out.println("map:"+paraMap.toString());
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject userJson) {
                System.out.println(userJson);
                UserSharedPreference.savePassword(password);
                userLoginSuccess(userJson);
                callBack.onSuccess("登录成功");
                // 发送登录成功的广播消息
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_LOGIN_SUCCESS));
                VideoVoteUtil.getInstance().start();
            }

            @Override
            protected void onMsgWrong(String msg) {
                callBack.onError(0, msg);
                visitorLogin();
            };

            @Override
            protected void onErrorNotZero(int error, String msg) {
                if ("1".equals(msg)) {
                    callBack.onError(1, "账户不存在");
                } else if ("2".equals(msg)) {
                    callBack.onError(2, "密码不正确");
                } else if ("3".equals(msg)) {
                    callBack.onError(3,"账号被禁用");
                } else {
                    Log.e(TAG,
                            "There is a msg, i don't know what to do. and msg is "
                                    + msg);
                }
                UserSharedPreference.clearContent();
                visitorLogin();
            };

            public void onErrorResponse(VolleyError error) {
                callBack.onError(4, "网络失败");
                visitorLogin();
            };

        }, paraMap);
    }

    /* 游客登录 */
    public void visitorLogin() {
        VideoVoteUtil.getInstance().release();

        String url = Constants.Root_Url + "/visitorSignature.do";
        UserSharedPreference.setIsLogin(false);
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            protected void onDataFine(JSONObject data) {
                String signature = JsonUtil.getJson(data, "visitorSignature");
                final String userId = JsonUtil.getJson(data, "visitor");
                MessageEntity.mUserId = userId;
                MLog.d(TAG, "visitor login, and signature is %s, userId is %s.", signature, userId);

                MySelfInfo.getInstance().setUserSig(signature);
                MySelfInfo.getInstance().setId(userId);
                MySelfInfo.getInstance().setNickName("游客");
                MySelfInfo.getInstance().setAvatar(Constants.DefaultHeadImg);
                mTencentLoginHelper.stopAVSDK();
                mTencentLoginHelper.imLogin(MySelfInfo.getInstance().getId(), signature);
                IMManager.getInstance().setClientData(userId, "游客", Constants.DefaultHeadImg, new Callback() {
                    @Override
                    public void onSuccess() {
                        MLog.d(TAG, "游客%s 登录成功", userId);
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        MLog.d(TAG, "游客" + userId + "登录失败: " + errorCode);
                    }
                });
            }
        }, new HashMap<String, Object>());
    }

    @Override
    public void loginSucc() {
        // 腾讯云登录成功
        new Thread(mUnReadGroupMessageSyncTask).start();
        IMPushHelper.getInstance().initConfigOfflinePush();
        IMPushHelper.getInstance().registerPush();
        TIMManager.getInstance().setOfflinePushListener(new TIMOfflinePushListener() {
            @Override
            public void handleNotification(TIMOfflinePushNotification timOfflinePushNotification) {
                try{
                    String ext = new String(timOfflinePushNotification.getExt(), "utf-8");
                    MLog.d(TAG, "handleNotification: %s.", ext);
                    if(timOfflinePushNotification.getConversationType() == TIMConversationType.Group){
                        // 群消息忽略
                        return;
                    }
                    mSystemMessageHelper.dealWithMsg(ext);
                }catch (Exception e){
                    Log.e(TAG, "推送异常");
                }
            }
        });

        IMManager.getInstance().registerReceiver("OrderPush", OrderMsgReceiver.class,
                new OrderMsgReceiver.MyResultReceiver(new ResultCallback<String>() {
            @Override
            public void onResult(String obj) {
                MLog.d(TAG, "离线消息推送， %s.", obj);
                mSystemMessageHelper.dealWithMsg(obj);
            }
        }));
    }

    @Override
    public void loginFail() {
        // 腾讯云登录失败
    }
}
