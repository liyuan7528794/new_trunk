package com.travel.communication.helper;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.travel.bean.VideoInfoBean;
import com.travel.communication.entity.MessageEntity;
import com.travel.imserver.ResultCallback;
import com.travel.imserver.receiver.ChatMessageReceiver;
import com.travel.lib.utils.MLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by ldkxingzhe on 2017/1/11.
 */
public class TIMGroupMessageReceiver implements TIMMessageListener {
    @SuppressWarnings("unused")
    private static final String TAG = "TIMGroupMessage";

    public static final String ACTION_START_LIVE = "action: me-start-live";
    public static final String ACTION_END_LIVE = "action: me-end-live";
    public static final String ACTION_ANTHER_START_LIVE = "action: another-start-live";
    public static final String ACTION_ANTHER_END_LIVE = "action: another-end-live";

    private SQliteHelper mSQliteHelper;

    private ChatMessageReceiver.MessageCommonParser mMsgParser;
    private ResultCallback<MessageEntity> mResultCallback;
    private Context mContext;

    public TIMGroupMessageReceiver(Context context) {
        mSQliteHelper = new SQliteHelper(context);
        mMsgParser = new ChatMessageReceiver.MessageCommonParser(mSQliteHelper);
        mContext = context;
    }

    public void setResultCallback(ResultCallback<MessageEntity> callback) {
        mResultCallback = callback;
    }

    @Override
    public boolean onNewMessages(List<TIMMessage> list) {
        if (list == null)
            return false;
        for (TIMMessage message : list) {
            //            if(message.getConversation().getType() != TIMConversationType.Group)
            //                continue;
            for (int i = 0; i < message.getElementCount(); i++) {
                TIMElem elem = message.getElement(i);
                if (elem.getType() == TIMElemType.Custom) {
                    // 自定义消息
                    try {
                        String customText = new String(((TIMCustomElem) elem).getData(), "UTF-8");
                        String customExt = new String(((TIMCustomElem) elem).getExt());
                        JSONObject jsonObject;
                        if (TextUtils.equals(((TIMCustomElem) elem).getDesc(), "订单通知"))
                            jsonObject = new JSONObject(customExt);
                        else
                            jsonObject = new JSONObject(customText);
                        if (jsonObject.has("userAction")) {
                            continue;
                        }
                        if (jsonObject.has("live-action")) {
                            if (mResultCallback != null) {
                                MLog.d(TAG, "收到直播消息: %s.", customText);
                                String liveAction = jsonObject.getString("live-action");
                                VideoInfoBean videoInfoBean = VideoInfoBean.getVideoInfoBean(jsonObject.getJSONObject("content"));
                                Intent intent = new Intent();
                                intent.putExtra("group-id", message.getConversation().getPeer());
                                if ("start-live".equals(liveAction)) {
                                    intent.setAction(ACTION_ANTHER_START_LIVE);
                                } else if ("end-live".equals(liveAction)) {
                                    intent.setAction(ACTION_ANTHER_END_LIVE);
                                } else {
                                    MLog.e(TAG, "未知指令：" + liveAction);
                                }
                                if (!TextUtils.isEmpty(intent.getAction())) {
                                    intent.putExtra("VideoInfoBean", videoInfoBean);
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                }
                            }
                            continue;
                        }
                        if (jsonObject.has("action")) {
                            NewSystemMessageHelper helper = new NewSystemMessageHelper(mContext);
                            helper.dealWithMsg(customExt);
                        } else {
                            MessageEntity msg = mMsgParser.parserMessageEntity(jsonObject);
                            onResult(msg);
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, e.getMessage(), e);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
        return false;
    }

    private void onResult(MessageEntity msg) {
        MLog.d(TAG, "onResult, and msg is %s.", msg.generateJson(mSQliteHelper));
        MLog.d(TAG, "MessageEntity ", msg.getMessage().toString());
        if (mResultCallback != null) {
            mResultCallback.onResult(msg);
        } else {
            // 入库处理
            mSQliteHelper.insertMessage(msg.getMessage());
            mSQliteHelper.lastMessageAddOne(msg.getMessage(), true, false);
        }
    }
}
