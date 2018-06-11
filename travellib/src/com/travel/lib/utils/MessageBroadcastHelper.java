package com.travel.lib.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 广播, 用于提醒消息的到达
 * Created by ldkxingzhe on 2016/9/1.
 */
public class MessageBroadcastHelper {
    private static final String TAG = "MessageBroadcastHelper";
    /* 消息刷新 */
    public static final String MESSAGE_COMMING = "com.travel.message_comming";

    public static void sendMessageComming(Context context){
        context.sendBroadcast(new Intent(MESSAGE_COMMING));
    }

    public interface MessageHelperCallback{
        void onMessageComming();
    }
    private MessageHelperCallback mCallback;
    private Context mContext;
    public MessageBroadcastHelper(Context context, MessageHelperCallback callback){
        mCallback = callback;
        mContext = context;
    }

    public void registerMessageCommingReceiver(){
        IntentFilter filter = new IntentFilter(MESSAGE_COMMING);
        mContext.registerReceiver(mMessageReceiver, filter);
    }

    public void unRegisterMessageCommingReceiver(){
        mContext.unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(MESSAGE_COMMING.equals(action)){
                mCallback.onMessageComming();
            }
        }
    };
}
