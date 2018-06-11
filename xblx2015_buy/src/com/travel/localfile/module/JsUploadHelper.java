package com.travel.localfile.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.travel.Constants;
import com.travel.lib.utils.MLog;

import java.io.File;

/**
 * *NOTE:* all callback is run in a worker thread
 *         Constructor called from main thread.
 * Created by ldkxingzhe on 2016/11/1.
 */
public class JsUploadHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "JsUploadHelper";

    private ValueCallback<Uri[]> mFileValueCallback;
    private WebView mWebView;

    private Handler mMainHandler;

    private static  int s_WebIsSupported = 0;   // 0 没有返回结果， -1 不支持， 1 支持
    private int mUploadTimes = 0;
    private Uri[] mUploadFileUri;

    private String mRemoteFileId;

    public interface Listener{
        // 计算sha1值,
        void onCalculationSha1(int index, int progress);
        void onUploadFile(int index, int progress);
        void onError(int index);
        void onUploadFileComplete(int index, String remoteFileId);
    }
    private Listener mListener;
    public void setListener(Listener listener){
        mListener = listener;
    }

    /* This constructor, all function must called form main thread */
    public JsUploadHelper(@NonNull WebView webView){
        initWithWebView(webView);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface"})
    private void initWithWebView(@NonNull WebView webView) {
        mWebView = webView;
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(this, "Android");
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mFileValueCallback = filePathCallback;
                mFileValueCallback.onReceiveValue(mUploadFileUri);
                return true;
            }
        });
        mMainHandler = new Handler();
        mWebView.loadUrl(Constants.Root_Url.replace("xblx", "mobileUploadVideo.jsp"));
        MLog.v(TAG, "loadUrl, and thread is " + Thread.currentThread());
    }

    public JsUploadHelper(@NonNull final Context context){
        WebView webView = new WebView(context.getApplicationContext());
        initWithWebView(webView);
    }

    public String uploadFileSync(final String filePath){
        runInMainThread(new Runnable() {
            @Override
            public void run() {
                uploadFile(filePath);
            }
        });
        try {
            synchronized(this){
                wait();
            }
            return mRemoteFileId;
        } catch (InterruptedException e) {
            MLog.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            mRemoteFileId = null;
        }
    }

    /* 返回本机是否支持JS上传文件, -1 不支持 */
    public int getWebIsSupported(){
        return s_WebIsSupported;
    }

    private void runInMainThread(Runnable runnable){
        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            mMainHandler.post(runnable);
        }else{
            runnable.run();
        }
    }

    private void uploadFile(Uri[] fileUriArray){
        mUploadTimes++;
        mUploadFileUri = fileUriArray;
        if(s_WebIsSupported == 1){
            chooseFile();
        }
    }

    public void uploadFile(Uri fileUri){
        uploadFile(new Uri[]{fileUri});
    }

    public void uploadFile(String filePath){
        MLog.v(TAG, "uploadFile and filePath is %s, thread is " + Thread.currentThread());
        uploadFile(Uri.fromFile(new File(filePath)));
    }

    /**
     * @return -1 if not supported， 0 -- unknown， 1 -- support
     */
    public static int isOperationSupported(){
        return s_WebIsSupported;
    }

    private void chooseFile(){
        MLog.v(TAG, "chooseFile");
        mWebView.loadUrl("javascript:chooseFile();");
    }

    /* not called, because this function is called by js auto */
    private void uploadFile(){
        mWebView.loadUrl("javascript:startUpload();");
    }

    @JavascriptInterface
    public void onSDKInitResult(boolean isResultOK){
        MLog.v(TAG, "onSDKInitResult and result is %b", isResultOK);
        s_WebIsSupported = isResultOK ? 1 : -1;
        if(mUploadTimes > 0) mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chooseFile();
            }
        }, 500);
        if(!isResultOK && mListener != null) mListener.onError(-1);
    }

    @JavascriptInterface
    public void onFileUploadComplete(String fileRemoteId){
        MLog.v(TAG, "onFileUploadComplete, and fileRemoteId is %s", fileRemoteId);
        if(mListener != null) mListener.onUploadFileComplete(0, fileRemoteId);
        mRemoteFileId = fileRemoteId;
        try{
            synchronized (this){
                notify();
            }
        }catch (Exception e){
            MLog.d(TAG, e.getMessage());
        }
    }

    @JavascriptInterface
    public void onError(int errorCode, String errorMessage){
        MLog.e(TAG, "onError, and errorCode is %d, errorMessage is %s",
                errorCode, errorMessage);
        try{
            if(mListener != null){
                mListener.onError(0);
            }
            synchronized (this){
                mRemoteFileId = null;
                notify();
            }
        }catch (Exception e){
            MLog.d(TAG, e.getMessage());
        }
    }

    @JavascriptInterface
    public void onStatusUpdate(String statusName, long totalSize, String percent, String speed){
        MLog.v(TAG,
                "onStatusUpdate, " +
                        "and statusName is %s, percent is %s, and speed is %s.",
                statusName, percent, speed);
        if(mListener == null) return;
        try{
            if("10".equals(statusName)){
                // 校验sha1值
                mListener.onCalculationSha1(0, f2i(percent));
            }else if("2".equals(statusName)){
                // 上传中...
                mListener.onUploadFile(0, f2i(percent));
            }else if("4".equals(statusName)){
                // 上传失败
//                onError(4, "上传失败");
            }
        }catch (NumberFormatException e){
            MLog.e(TAG, e.getMessage(), e);
        }
    }

    private int f2i(String fl) throws NumberFormatException{
        float number = Float.valueOf(fl);
        return (int) number;
    }
}
