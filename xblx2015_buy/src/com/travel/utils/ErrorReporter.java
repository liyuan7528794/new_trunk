package com.travel.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.travel.lib.helper.FileLog;
import com.travel.lib.utils.MLog;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * 仿照Acra实现的原理监听所有异常
 *  当然实现还是有很大区别的，　
 * Created by ke on 16-5-18.
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler{
    @SuppressWarnings("unused")
    private static final String TAG = "ErrorReporter";

    private static final String HAS_BUG = "has_bug";
    private static final String BUG_DETAIL = "bug_detail";
    private static final String BUG_MESSAGE = "bug_message";

    private Context mContext;
    private SharedPreferences mBugSharePreference;

    @SuppressLint("StaticFieldLeak")
    private static ErrorReporter mInstance;
    public static ErrorReporter getInstance(Context context){
        if(mInstance == null){
            mInstance = new ErrorReporter(context.getApplicationContext());
        }

        return mInstance;
    }

    /**
     * This method must be called in main thread
     */
    private ErrorReporter(Context context){
        if(Looper.getMainLooper().getThread() != Thread.currentThread()){
            throw new IllegalStateException("ErroReporter constructor must be called in main thread");
        }
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
        mBugSharePreference = mContext.getSharedPreferences("BUG_TRAVEL", Context.MODE_PRIVATE);
    }

    /**
     * 返回是否存在bug， true -- 表示存在
     */
    private boolean hasBug(){
        return mBugSharePreference.getBoolean(HAS_BUG, false);
    }

    /**
     * 返回上一个bug的exception
     */
    public String getLastBugDetail(){
        return mBugSharePreference.getString(BUG_DETAIL, "");
    }

    public String getLastBugMessage(){
        return mBugSharePreference.getString(BUG_MESSAGE, "");
    }

    public void upLoadBug(){
        if(!hasBug()) return;

        MLog.d(TAG, "upLoadBug, and Bug Message is %s, Bug Detail is %s.",
                getLastBugMessage(), getLastBugDetail());

        mBugSharePreference.edit().clear().commit();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        MLog.v(TAG, "uncaughtException, thread is %s, excption is %s.", thread, ex.getMessage());
        Log.e(TAG, ex.getMessage(), ex);
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        String exceptionStr = writer.toString();
        FileLog.getInstance().logWithoutFilter("UncaughtException", exceptionStr);
        FileLog.getInstance().flush();
        SharedPreferences.Editor editor = mBugSharePreference.edit();
        editor.putBoolean(HAS_BUG, true);
        editor.putString(BUG_MESSAGE, ex.getMessage());
        editor.putString(BUG_DETAIL, exceptionStr);
        editor.commit();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        Process.killProcess(Process.myPid());
        System.exit(10);
    }
}
