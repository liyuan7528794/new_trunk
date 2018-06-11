package com.travel.video.help;

import android.support.annotation.NonNull;

import com.ctsmedia.hltravel.BuildConfig;
import com.travel.lib.utils.MLog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ldkxingzhe on 2016/11/2.
 */

public class HeartBeatHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "HeartBeatHelper";

    private static HeartBeatHelper s_Instance;
    protected String mRoomNum, mUserId, mStatus;
    private Timer mTimer;
    protected HeartBeatHelper(){}

    public static HeartBeatHelper getInstance(){
        if(s_Instance == null){
            synchronized (HeartBeatHelper.class){
                if(s_Instance == null){
                    s_Instance = new HeartBeatHelper();
                }
            }
        }

        return s_Instance;
    }


    public HeartBeatHelper setRoomNum(@NonNull Object roomNum) {
        this.mRoomNum = roomNum.toString();
        return this;
    }

    public HeartBeatHelper setUserId(@NonNull Object userId) {
        this.mUserId = userId.toString();
        return this;
    }

    public HeartBeatHelper setStatus(@NonNull Object status){
        this.mStatus = status.toString();
        return this;
    }

    /*
    * 开始发送心跳， 从主进程中调用
    * */
    public void startSendHeartBeat(){
        if(mTimer != null){
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendHeartBeat(generateHeartStr(false));
            }
        }, 20, 10000);
    }

    /**
     * 结束心跳， called from main thread
     */
    public void stopSendHeartBeat(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
            new Thread(){
                @Override
                public void run() {
                    sendHeartBeat(generateHeartStr(true));
                }
            }.start();
        }
    }

    public void onDestroy(){
        stopSendHeartBeat();
        s_Instance = null;
    }

    private void sendHeartBeat(String heartBeatStr){
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(8899);
        } catch (SocketException e) {
            MLog.e(TAG, e.getMessage(), e);
            return;
        }

        byte[] buf = heartBeatStr.getBytes();
        InetAddress destination = null;
        try {
            destination = InetAddress.getByName(BuildConfig.Chat_IP);
        } catch (UnknownHostException e) {
            MLog.e(TAG, e.getMessage(), e);
            return;
        }
        DatagramPacket packet = new DatagramPacket(buf, buf.length, destination, 3345);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            MLog.e(TAG, e.getMessage(), e);
            return;
        }
        MLog.v(TAG, "sendHeartBeat, and complete");
        datagramSocket.close();
    }

    @NonNull
    protected String generateHeartStr(boolean isClose) {
        String FTS = " ";
        String heartBeatStr = (isClose ? "CloseFlowControlHeartbeat" : "FlowControlHeartbeat") + FTS +
                mRoomNum + FTS +
                mUserId + FTS +
                mStatus;
        MLog.v(TAG, "sendHeartBeat, starting and content is: %s", heartBeatStr);
        return heartBeatStr;
    }
}
