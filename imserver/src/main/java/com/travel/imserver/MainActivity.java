package com.travel.imserver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;
import com.travel.lib.utils.MLog;

/**
 * 测试使用，可以删除
 * Created by ldkxingzhe on 2016/12/9.
 */
public class MainActivity extends Activity{
    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";

    Button mBtnLogin, mBtnChangeRoom;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
//        IMManager.init(this);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "点击登录");
                IMManager.getInstance().setClientData("4", "test", "who you are", new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "登录成功");
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.e(TAG, "登录失败 and errorCode is " + errorCode);
                    }
                });
            }
        });

        mBtnChangeRoom = (Button) findViewById(R.id.btn_change_room);
        mBtnChangeRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMManager.getInstance().changeRoom("test", new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "改变房间成功");
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.e(TAG, "改变房间失败");
                    }
                });
            }
        });

        findViewById(R.id.btn_send_chat_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        for(int i = 0; i < 100; i++){
                            sendChatMessage("chatMessage" + String.valueOf(i));
            /*                try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        }
                    }
                }.start();
            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseBean baseBean = new BaseBean();
                baseBean.setId("testse");
                baseBean.setType(0);
                baseBean.setMsgHead("logout");
                Gson gson = new Gson();
                BaseMessage baseMessage = new BaseMessage(baseBean.getId(), gson.toJson(baseBean));
                IMManager.getInstance().sendMessage(baseMessage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v(TAG, "发送成功");
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        MLog.v(TAG, "logout, %d, %s", errorCode, errorMsg);
                    }
                });
            }
        });

        IMManager.getInstance().setLogoutListener(new ResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean obj) {
                MLog.d(TAG, "异地登录");
            }
        });
    }

    private void sendChatMessage(final String id){
        BaseBean baseBean = new BaseBean();
        baseBean.setType(BaseBean.TYPE_SINGLE_CHAT);
        baseBean.setId(id);
        baseBean.setSendUser("4");
        baseBean.setReceive("9");
        baseBean.setMsgHead("private_chat");
        baseBean.setMsgBody("");
        IMManager.getInstance().sendBaseBean(baseBean, new Callback() {
            @Override
            public void onSuccess() {
                MLog.v(TAG, "onSuccess, and Id is %s.", id);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                MLog.e(TAG, "onError, and errorCode is %d, and id is %s.", errorCode, id);
            }
        });
    }
}
