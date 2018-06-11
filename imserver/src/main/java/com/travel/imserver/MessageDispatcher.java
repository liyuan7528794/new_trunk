package com.travel.imserver;

import android.util.Log;

import com.travel.imserver.bean.BaseBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息调度者， 订阅管理器
 * 用于消息结果的分发， 以及接受到的消息处理
 * Created by ldkxingzhe on 2016/12/7.
 */
public class MessageDispatcher implements Receiver<BaseBean>{
    @SuppressWarnings("unused")
    private static final String TAG = "MessageDispatcher";
    private Map<String, Receiver<BaseBean>> mReceiverMap = new HashMap<>();
    public void registerReceiver(String receiverName, Receiver<BaseBean> receiver){
        if(mReceiverMap.get(receiverName) != null){
            Log.e(TAG, "receiverName: " + receiverName + " has been register");
            return;
        }
        mReceiverMap.put(receiverName, receiver);
    }

    public void unRegisterReceiver(String receiverName){
        mReceiverMap.remove(receiverName);
    }

    Receiver<BaseBean> getReceiver(String receiverName){
        return mReceiverMap.get(receiverName);
    }

    @Override
    public boolean isDealWith(BaseBean obj) {
        return true;
    }

    @Override
    public void dealWith(BaseBean obj) {
        for (Receiver<BaseBean> receiver : mReceiverMap.values()){
            if(receiver.isDealWith(obj)){
                receiver.dealWith(obj);
            }
        }
    }
}
