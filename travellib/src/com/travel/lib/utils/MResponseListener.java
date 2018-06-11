package com.travel.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.travel.Constants;
import com.travel.lib.TravelApp;
import com.travel.lib.ui.LoadingDialog;
import com.volley.Response;
import com.volley.Response.ErrorListener;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * 针对Volley的网路监听部�? 理论上所有的返回结果是JSON的网络接口都应该使用这个监听
 *
 * @author Administrator
 */
abstract public class MResponseListener implements
        Response.Listener<JSONObject>, ErrorListener {
    private static final String TAG = "MResponseListener";

    private static final String ERROR = "error";
    private static final String MSG = "msg";

    private String tagMsg;

    public MResponseListener() {
        tagMsg = "";
    }

    private WeakReference<Context> mContext;

    public MResponseListener(Context mContext) {
        this.mContext = new WeakReference<Context>(mContext);
        tagMsg = "";
        if (!((Activity) this.mContext.get()).isFinishing())
            LoadingDialog.getInstance(mContext).showProcessDialog();
    }

    public MResponseListener(String tag) {
        this.tagMsg = tag;
    }

    public String getTagMsg() {
        return tagMsg;
    }

    public void setTagMsg(String tagMsg) {
        this.tagMsg = tagMsg;
    }

    @Override
    public void onResponse(JSONObject response) {
        if (mContext != null && !((Activity) this.mContext.get()).isFinishing())
            LoadingDialog.getInstance(mContext.get()).hideProcessDialog(0);
        if (MLog.LOG_LEVEL <= Log.VERBOSE) {
            try {
                MLog.v(TAG, "tag (url) is %s, result is:\n%s",
                        tagMsg, response.toString(4));
            } catch (JSONException e) {
                MLog.e(TAG, e.getMessage(), e);
            }
        }
        MLog.v(TAG,
                "tag(may be is url) is " + tagMsg + ", json result is " + response.toString());
        try {
            int error = response.getInt(ERROR);
            String msg = response.getString(MSG);
            onNetComplete();
            if (error == 0) {
                if (response.has(MSG)) {
                    if ("0".equals(msg) || "OK".equals(msg)) {
                        Object dataObject = null;
                        if (response.has("data")
                                && (dataObject = response.get("data")) != null) {
                            if (dataObject instanceof JSONObject) {
                                onDataFine((JSONObject) dataObject);
                            } else if (dataObject instanceof JSONArray) {
                                onDataFine((JSONArray) dataObject);
                            } else if (dataObject instanceof String
                                    && !TextUtils
                                    .isEmpty(dataObject.toString())) {
                                onDataFine((String) dataObject);
                            } else {
                                if (dataObject instanceof Integer) {
                                    onDataFine((Integer) dataObject);
                                }
                                onDataFine((JSONObject) null);
                            }
                        } else {
                            onDataFine((JSONObject) null);
                        }
                    } else {
                        onMsgWrong(msg);
                    }
                } else {
                    onNoMsg();
                }
            } else if ("logout".equals(msg)) {
                onLogout();
            } else {
                onErrorNotZero(error, msg);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            onException();
        }
    }

    private void showLoginActivity() {
        MLog.v(TAG, "showLoginActivity");
        Intent loginIntent = new Intent();
        loginIntent.setAction(Constants.ACTION_LOGIN);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TravelApp.appContext.startActivity(loginIntent);
        Intent broadCastIntent = new Intent(Constants.ACTION_LOGIN);
        broadCastIntent.putExtra("from", "http");
        TravelApp.appContext.sendBroadcast(broadCastIntent);
    }

    /**
     * 当异常发生时
     */
    private void onException() {
    }

    /**
     * 当error字段不为0�?
     *
     * @param error 此时的error字段
     * @param msg   msg字段
     */
    protected void onErrorNotZero(int error, String msg) {
        if (!TextUtils.isEmpty(msg))
            TravelUtil.showToast(msg);
    }

    /**
     * 没有msg字段（在error�?的前提下�?
     */
    protected void onNoMsg() {
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mContext != null)
            LoadingDialog.getInstance(mContext.get()).hideProcessDialog(0);
        if (!TextUtils.isEmpty(error.getMessage()))
            TravelUtil.showToast(error.getMessage());
        MLog.e(TAG,
                "tag is " + tagMsg + ", and response error "
                        + error.getMessage(), error);
    }

    /**
     * msg提示不正�?
     *
     * @param msg msg信息
     */
    protected void onMsgWrong(String msg) {
    }

    /**
     * �?��正常�?获取到了data数据
     *
     * @param data data部分的JSONObject对象
     */
    protected void onDataFine(JSONObject data) {
    }

    protected void onDataFine(int data) {

    }

    protected void onDataFine(JSONArray data) {

    }

    protected void onDataFine(String data) {
    }

    /**
     * 当网络结�?
     */
    protected void onNetComplete() {
    }

    /**
     * logout
     */
    protected void onLogout() {
        showLoginActivity();
    }

}
