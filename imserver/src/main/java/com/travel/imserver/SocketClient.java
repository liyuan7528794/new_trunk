package com.travel.imserver;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.travel.imserver.bean.BaseBean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * socket聊天的客户端
 * Created by ldkxingzhe on 2016/12/19.
 */
public class SocketClient {
    @SuppressWarnings("unused")
    private static final String TAG = "SocketClient";

    private ScheduledExecutorService mExecutorService;
    private String mIp;
    private int mPort;
    private volatile Socket mSocket;
    private volatile boolean mIsDisConnect = false;
    private volatile  boolean mIsActive = false;
    private HandlerListener mClientHandler;

    private BufferedReader mReader;
    private Writer mWriter;
    private Gson mGson;

    private TimeOutState mTimeOutState;

    public interface HandlerListener{
        void channelRead(BaseBean baseBean);
        void channelActive();
        void channelInactive();
        void exceptionCaught(Throwable e);
        void onTimeout(boolean isRead);
    }

    public SocketClient(@NonNull String ip, int port,
                        @NonNull ScheduledExecutorService service,
                        @NonNull HandlerListener jsonClientHandler){
        mIp = ip;
        mPort = port;
        mClientHandler = jsonClientHandler;
        mGson = new Gson();
        mExecutorService = service;
        mTimeOutState = new TimeOutState();
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setTimeout(long readTimeout, long writeTimeOut){
        mTimeOutState.mReadTimeOut = readTimeout;
        mTimeOutState.mWriteTimeOut = writeTimeOut;
    }

    public void connect() throws IOException {
        if (mSocket != null || mIsDisConnect)
            throw new IllegalStateException("function connect should call once");
        mSocket = new Socket(mIp, mPort);
        mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                read();
            }
        });
        try {
            mIsActive = true;
            mClientHandler.channelActive();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        mTimeOutState.start();
    }

    public void disConnect(){
        if(mIsDisConnect || mSocket == null) return;
        mIsDisConnect = true;
        try {
            mSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "disConnect: " + e.getMessage(), e);
        }finally {
            try {
                mClientHandler.channelInactive();
                mIsActive = false;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mSocket = null;
        }
    }

    public void sendMsg(final String msg, final Callback callback){
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if(mIsDisConnect){
                    callback.onError(-1, "socket已经关闭");
                    return;
                }
                try {
                    synchronized (SocketClient.this){
                        if(mIsDisConnect){
                            callback.onError(-1, "socket已经关闭");
                            return;
                        }
                        mWriter.write(msg);
                        mWriter.flush();
                    }
                    mTimeOutState.refreshWrite();
                    callback.onSuccess();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    callback.onError(-2, "发送异常, 断开连接");
                    disConnect();
                }
            }
        });
    }

    private void read(){
        while (!mIsDisConnect){
            String line = null;
            try {
                line = mReader.readLine();
                if(line == null){
                    Log.e(TAG, "消息获取为空, 出现异常");
                    break;
                }
                mTimeOutState.refreshRead();
                if(!mIsDisConnect){
                    final String finalLine = line;
                    mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            dealWithMsg(finalLine);
                        }
                    });
                }
            } catch (IOException e) {
                if(!mIsDisConnect){
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            }
        }
        disConnect();
    }

    private void dealWithMsg(String msg){
        try {
            Log.v(TAG, "dealWithMsg: " + msg);
            BaseBean baseBean = mGson.fromJson(msg, BaseBean.class);
            mClientHandler.channelRead(baseBean);
        } catch (Exception e) {
            try {
                Log.e(TAG, e.getMessage(), e);
                mClientHandler.exceptionCaught(e);
            } catch (Exception e1) {
                Log.e(TAG, e1.getMessage(), e);
            }
        }
    }

    private class TimeOutState{
        private volatile long mReadTimeOut = 30000, mWriteTimeOut = 30000;
        private volatile long mLastReadTime, mLastWriteTime;

        public void start(){
            refreshRead();
            refreshWrite();
            if(mReadTimeOut > 0){
                mExecutorService.schedule(mReadTimeOutRunnable, mReadTimeOut, TimeUnit.MILLISECONDS);
            }
            if(mWriteTimeOut > 0){
                mExecutorService.schedule(mWriteTimeOutRunnable, mWriteTimeOut, TimeUnit.MILLISECONDS);
            }
        }

        void refreshWrite(){
            mLastWriteTime = System.currentTimeMillis();
        }

        void refreshRead(){
            mLastReadTime = System.currentTimeMillis();
        }

        Runnable mReadTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mIsDisConnect) return;
                long current = System.currentTimeMillis();
                long nextDelay;
                if(current - mLastReadTime >= mReadTimeOut){
                    mClientHandler.onTimeout(true);
                    nextDelay = mReadTimeOut;
                }else{
                    // 没有触发timeout， 计算下次时间
                    nextDelay = mLastReadTime + mReadTimeOut - current;
                }
                mExecutorService.schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        };

        Runnable mWriteTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mIsDisConnect) return;
                long current = System.currentTimeMillis();
                long nextDelay;
                if(current - mLastWriteTime >= mWriteTimeOut){
                    mClientHandler.onTimeout(false);
                    nextDelay = mWriteTimeOut;
                }else{
                    nextDelay = mLastWriteTime + mWriteTimeOut - current;
                }
                mExecutorService.schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        };
    }
}
