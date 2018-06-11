package com.travel.communication.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.travel.activity.OneFragmentActivity;
import com.travel.activity.OneFragmentSingleTopActivity;
import com.travel.communication.dao.LastMessage;
import com.travel.communication.dao.Message;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.imserver.R;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * 信息系统消息 {nSystem.Order} Created by ldkxingzhe on 2016/7/23.
 */
public class NewSystemMessageHelper {
	private static final String TAG = "NewSystemMessageHelper";


	/** 系统的订单消息 */
	public static final String MESSAGE_TYPE_ORDER_ID = "system-orders";

	private SQliteHelper mSQliteHelper;
	private NotificationHelper mNotificationHelper;

	public NewSystemMessageHelper(Context context) {
		MLog.v(TAG, "New System Message Helper");
		mSQliteHelper = new SQliteHelper(context);
		mNotificationHelper = new NotificationHelper(context);
		UserData systemOrderUser = new UserData();
		systemOrderUser.setId(MESSAGE_TYPE_ORDER_ID);
		systemOrderUser.setNickName("系统订单消息");
		systemOrderUser.setImgUrl("drawable://" + R.drawable.ic_launcher);
		mSQliteHelper.inserOrReplace(systemOrderUser);
	}

	public void dealWithMsg(String orderMessage) {
		String mUserId = UserSharedPreference.getUserId();
		MLog.v(TAG, "orderMessage is %s", orderMessage);
		Message message = new Message();
		message.setSenderId(MESSAGE_TYPE_ORDER_ID);
		message.setReceiverId(mUserId);
		message.setCreate(new Date(new Date().getTime() - 28800000));
		message.setMessageType(MessageEntity.TYPE_ORDERS);
		message.setContent(orderMessage);
		long messageId = mSQliteHelper.insertMessage(message);
		LastMessage lastMessage = mSQliteHelper.getLastMessage(mUserId, MESSAGE_TYPE_ORDER_ID);
		if (lastMessage == null) {
			lastMessage = new LastMessage();
			lastMessage.setUnReadNumber(0);
		}
		lastMessage.setSenderId(MESSAGE_TYPE_ORDER_ID);
		lastMessage.setReceiverId(UserSharedPreference.getUserId());
		lastMessage.setIsVisible(true);
		lastMessage.setUnReadNumber(lastMessage.getUnReadNumber() + 1);
		lastMessage.setMessageId(messageId);
		mSQliteHelper.inserOrReplace(lastMessage);
		mSQliteHelper.playRingtone();
		mNotificationHelper.showSystemOrderMessageNotification(orderMessage);

		// 将有变化的订单存入本地数据库中
		ShopMessageHelper helper = null;
		JSONObject jsonObject = null;
		String ordersId = "";
		String userType = "";// 用于判断买卖家的身份
		String status = "";// 用于判断当前订单属于进行中的单还是已完成的单
		try {
			jsonObject = new JSONObject(orderMessage);
			JSONObject actionObject = jsonObject.getJSONObject("action");
			ordersId = actionObject.optString("ordersId");
			userType = actionObject.optString("userType");
			userType = TextUtils.equals(userType, "1") ? "my" : "business";
			status = actionObject.optString("status");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		helper = new ShopMessageHelper(TravelApp.appContext);
		helper.delete(ShopMessageHelper.TABLENAME_MESSAGE, ordersId, UserSharedPreference.getUserId());
		helper = new ShopMessageHelper(TravelApp.appContext);
		helper.insert(ShopMessageHelper.TABLENAME_MESSAGE, ordersId, userType, UserSharedPreference.getUserId(),
				status);
	}

	public void onDestroy() {

	}

	private  class NotificationHelper {
		private Context mContext;

		NotificationHelper(Context context){
			mContext = context;
		}

		void showSystemOrderMessageNotification(String message){
			try {
				JSONObject jsonObject = new JSONObject(message);
				String content = JsonUtil.getJson(jsonObject, "content");
				String title = JsonUtil.getJson(jsonObject, "title");
				Intent intent = new Intent(mContext, OneFragmentSingleTopActivity.class);
				intent.putExtra(OneFragmentActivity.TITLE, "系统订单消息");
				intent.putExtra(OneFragmentActivity.CLASS, "com.travel.communication.fragment.SystemOrderMessageFragment");
				PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				showNotification(content, title, content, pendingIntent);
			} catch (JSONException e) {
				MLog.e(TAG, e.getMessage(), e);
			}
		}

		void showNotification(String ticker, String title, String content, PendingIntent intent){
			NotificationManager manager
					= (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification.Builder(mContext)
					.setSmallIcon(R.drawable.ic_launcher)
					.setTicker(ticker)
					.setContentTitle(title)
					.setContentText(content)
					.setContentIntent(intent)
					.getNotification();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			manager.notify(10, notification);
		}
	}
}
