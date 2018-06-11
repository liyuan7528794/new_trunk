package com.travel.imserver;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 保存消息的回调一定时间
 * Created by ldkxingzhe on 2016/12/7.
 */
public class MessageTimer {
    @SuppressWarnings("unused")
    private static final String TAG = "MessageTimer";

    public static final int TIME_OUT = -318;
    Handler mHandler;
    private Map<ID, Callback> mCallbackMap;
    private List<ID> mIDList;
    Queue<BaseMessage> mMsgCache;
    private long mTimeOut = 8000; // 4s time out
    public MessageTimer(){
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mCallbackMap = new HashMap<>();
        mIDList = new LinkedList<>();
        mMsgCache = new ConcurrentLinkedQueue<>();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void addMessage(BaseMessage message, final Callback callback){
//        MLog.v(TAG, "addMessage, and messageId is %s.", message.getId());
        final ID id = new ID(message);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                addMessageInl(id, callback);
            }
        });
    }

    void addMessageInl(ID id, Callback callback) {
//        MLog.v(TAG, "addMessageInl, and id is %s.", id.id);
        if(mCallbackMap.containsKey(id)){
            timeOutID(id);
        }
        mCallbackMap.put(id, callback);
        mIDList.add(0, id);
        if(mIDList.size() == 1) mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lastOneTimeOut();
            }
        }, mTimeOut);
    }

    public void onMessageSuccess(final String id){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onMessageResultInl(id, true, 0, "");
            }
        });
    }

    public void onMessageFailed(final String id, final int errorCode, final String errorMsg){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onMessageResultInl(id, false, errorCode, errorMsg);
            }
        });
    }

    void onMessageResultInl(String id, boolean success, int errorCode, String errorMsg){
        ID keyId = new ID(id);
        Callback callback = mCallbackMap.get(keyId);
        if(callback == null){
            Log.v(TAG, "message has been time out");
            return;
        }
        if(success){
            callback.onSuccess();
        }else{
            callback.onError(errorCode, errorMsg);
        }
        if(mIDList.get(mIDList.size() - 1).equals(keyId)){
            removeMessage(keyId);
            findNextTimeOutID();
        }else{
            removeMessage(keyId);
        }
    }

    private void removeMessage(ID keyId) {
        mCallbackMap.remove(keyId);
        mIDList.remove(keyId);
        BaseMessage baseMessage = new BaseMessage(keyId.id, "removeMessage");
        mMsgCache.remove(baseMessage);
    }

    void lastOneTimeOut(){
        if(mIDList.isEmpty()) return;
        ID lastId = mIDList.get(mIDList.size() - 1);
        timeOutID(lastId);
        findNextTimeOutID();
    }

    void findNextTimeOutID() {
        for(int i = mIDList.size() - 1; i >= 0; i--){
            ID id = mIDList.get(i);
            if(id.remainTime() <= 0){
                timeOutID(id);
            }else{
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lastOneTimeOut();
                    }
                }, id.remainTime());
            }
        }
    }

    private void timeOutID(ID lastId) {
        mCallbackMap.get(lastId).onError(TIME_OUT, "time out");
        removeMessage(lastId);
    }

    public void stopTimer(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                failedAllCallbackIn();
                mHandler.getLooper().quit();
            }
        });
    }

    private void failedAllCallbackIn(){
        for (Callback callback: mCallbackMap.values()){
            callback.onError(TIME_OUT, "time out");
        }
    }

    class ID{
        private String id;
        private long deadTimeMill;
        public ID(BaseMessage baseMessage) {
            id = baseMessage.getId();
            deadTimeMill = System.currentTimeMillis() + mTimeOut;
        }

        public ID(String id){
            this.id = id;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            String obj_Id = (obj instanceof ID) ? ((ID) obj).id :
                    (obj == null ? "" : obj.toString());
            return obj_Id.equals(id);
        }

        @Override
        public int hashCode() {
            return TextUtils.isEmpty(id) ? 0 : id.hashCode();
        }

        long remainTime(){
            return deadTimeMill - System.currentTimeMillis();
        }
    }
}
