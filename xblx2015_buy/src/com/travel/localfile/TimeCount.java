package com.travel.localfile;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.travel.lib.utils.DateFormatUtil;

/**
 * 计时器,
 * 记录总时间, 有一个mainthread的定时回调(0.2s回调一次)
 * Created by ldkxingzhe on 2016/6/29.
 */
public class TimeCount {
    @SuppressWarnings("unused")
    private static final String TAG = "TimeCount";

    public interface Callback{
        /**
         * 回调
         * @param totalMilliseconds  开始的总时间
         * @param isWholeSeconds     是否是正秒
         */
        void onTimerCallback(long totalMilliseconds, boolean isWholeSeconds);
        void onTimerReset();
    }
    private Callback mCallback;
    private Handler mHandler;
    private volatile long mTotalTimeMilliseconds;  // 总的时间
    private boolean isStopped = false;
    /**
     * must called in main thread
     */
    public TimeCount(@NonNull Callback callback){
        mCallback = callback;
        if(Looper.myLooper() != Looper.getMainLooper()){
            throw new IllegalStateException("This constructor function must called from main thread," +
                    " and current Thread is " + Thread.currentThread());
        }
        mHandler = new Handler();
    }

    public void start(){
        mHandler.post(mCountTimeRunnable);
        isStopped = false;
    }

    public long getTotalTimeMilliseconds(){
        return mTotalTimeMilliseconds;
    }

    public void stop(){
        mHandler.removeCallbacks(mCountTimeRunnable);
        isStopped = true;
    }

    public void reset(){
        stop();
        mTotalTimeMilliseconds = 0;
        mCallback.onTimerReset();
    }

    public String formatString(){
        return DateFormatUtil.longMillisecondsFormat(mTotalTimeMilliseconds);
    }


    private final Runnable mCountTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if(isStopped) return;
            mTotalTimeMilliseconds += 200;
            mCallback.onTimerCallback(mTotalTimeMilliseconds, mTotalTimeMilliseconds % 1000 == 0);
            mHandler.postDelayed(mCountTimeRunnable, 200);
        }
    };

}
