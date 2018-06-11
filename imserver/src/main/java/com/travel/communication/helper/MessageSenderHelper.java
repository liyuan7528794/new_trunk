package com.travel.communication.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.travel.communication.entity.MessageEntity;
import com.travel.imserver.Callback;
import com.travel.imserver.IMManager;
import com.travel.imserver.ResultCallback;
import com.travel.imserver.bean.BaseBean;
import com.travel.lib.helper.OSSHelper;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.MLog;

import java.util.Date;

/**
 * 消息发送辅助类
 * @author ldkxingzhe
 */
public class MessageSenderHelper implements ResultCallback<MessageEntity> {
	private static final String TAG = "MessageSenderHelper";

	// 聊天类型
	private final int mChatType;
	// 发送者Id, 接受者Id
	protected String mSenderId, mReceiverId;
	protected Context mContext;
	protected SQliteHelper mSQliteHelper;
	protected OSSHelper mOssHelper;

	@Override
	public void onResult(MessageEntity obj) {
		if(mListener != null){
			mListener.onMessageComming(obj);
		}
	}

	public interface MessageHelperListener{
		void onMessageComming(MessageEntity messageEntity);
	}
	private MessageHelperListener mListener;
	public void setListener(MessageHelperListener listener){
		mListener = listener;
	}

	public static MessageSenderHelper getMessageSenderHelper(Context context,
															 int chatType,
															 String senderId,
															 String receiverId){
		if (chatType == 0){
			// 单聊
			return new MessageSenderHelper(context, chatType, senderId, receiverId);
		}else if(chatType == 1){
			return new GroupMessageSenderHelper(context, chatType, senderId, receiverId);
		}else
			throw new IllegalStateException("chatType: 0 是单聊， 1 是群聊. 你的是：" + chatType);
	}

	/**
	 * 构造函数
	 * @param chatType  聊天类型, 0 -- 单聊, 1 群聊
	 * @param senderId 发送者Id
	 * @param receiverId 接受者Id
	 */
	protected MessageSenderHelper(Context context, int chatType, String senderId, String receiverId){
		mContext = context;
		mChatType = chatType;
		if(!TextUtils.isEmpty(receiverId)){
			setUserInfo(senderId, receiverId);
		}
		mSQliteHelper = new SQliteHelper(mContext);
		mOssHelper = new OSSHelper();
		MLog.v(TAG, "mReceiverId is " + mReceiverId);
	}

	/**
	 * 设置发送方Id与接受者Id(从我的视角出发)
	 */
	public void setUserInfo(String senderId, String receiverId){
		mSenderId = senderId;
		initIMServer(receiverId);
	}

	protected void initIMServer(String receiverId) {
		if(!TextUtils.isEmpty(mReceiverId)){
			IMManager.getInstance().unRegisterChatReceiver(mReceiverId);
		}
		mReceiverId = receiverId;
		IMManager.getInstance().registerChatReceiver(mReceiverId, this);
	}

	/**
	 * 发送文本消息
	 * @param textContent
	 */
	public MessageEntity sendTextMessage(String textContent){
		MessageEntity textMessage = new MessageEntity.MessageBuilder()
				.setContent(textContent.replace(" ", ""))
				.setType(MessageEntity.TYPE_TEXT)
				.setId(-1)
				.build();
		addCommonInfoToMessage(textMessage);
		sendMessageToNetAndSave(textMessage);
		return textMessage;
	}

	public MessageEntity getGoodsMessageEntity(String textContent){
		MessageEntity textMessage = new MessageEntity.MessageBuilder()
				.setContent(textContent.replace(" ", ""))
				.setType(MessageEntity.TYPE_TEXT)
				.setId(-1)
				.build();
		addCommonInfoToMessage(textMessage);
		return textMessage;
	}

	/* 发送其他自定义消息 */
	public MessageEntity sendOtherMessage(String messageContent, int messageType){
		MessageEntity entity = new MessageEntity.MessageBuilder()
				.setContent(messageContent.replace(" ", ""))
				.setType(messageType)
				.setId(-1)
				.build();
		addCommonInfoToMessage(entity);
		sendMessageToNetAndSave(entity);
		return entity;
	}

	/**
	 * 发送音频消息
	 */
	public MessageEntity sendAudioMessage(String path, long timeLong){
		final MessageEntity audioMessage = new MessageEntity.MessageBuilder()
				.setContent("file://" + path)
				.setType(MessageEntity.TYPE_SOUND)
				.setId(-1)
				.build();
		addCommonInfoToMessage(audioMessage);
		audioMessage.setTimeLong(timeLong/1000);
		mOssHelper.uploadFile(mOssHelper.generateObjectKey(mSenderId, ".mp4a"), path,
				new OSSCompletedCallback<PutObjectRequest, OSSResult>() {

					@Override
					public void onSuccess(PutObjectRequest arg0, OSSResult arg1) {
						audioMessage.setContent(mOssHelper.getUrlByObjectKey(arg0.getObjectKey()));
						sendMessageToNetAndSave(audioMessage);
					}

					@Override
					public void onFailure(PutObjectRequest arg0, ClientException arg1, ServiceException arg2) {
						audioMessage.setState(MessageEntity.STATE_FAILED);
					}
				});
		return audioMessage;
	}

	public MessageEntity sendVideoMessage(String videoPath, final String thumbnailPath){
		final MessageEntity videoMsg = new MessageEntity.MessageBuilder()
				.setContent("file://" + videoPath + ",file://" + thumbnailPath)
				.setType(MessageEntity.TYPE_VIDEO)
				.setId(-1)
				.build();
		addCommonInfoToMessage(videoMsg);
		final String objectkey = mOssHelper.generateObjectKey(mSenderId, ".mp4");
		mOssHelper.uploadFile(objectkey, videoPath, new OSSCompletedCallback<PutObjectRequest, OSSResult>() {
			@Override
			public void onSuccess(final PutObjectRequest putObjectRequest, OSSResult ossResult) {
				mOssHelper.uploadFileByUserId(mSenderId, thumbnailPath, new OSSCompletedCallback<PutObjectRequest, OSSResult>() {
					@Override
					public void onSuccess(PutObjectRequest put, OSSResult ossResult) {
						videoMsg.setContent(mOssHelper.getUrlByObjectKey(putObjectRequest.getObjectKey())
								+ "," + mOssHelper.getImageUrlByObjectKey(put.getObjectKey(), null));
						sendMessageToNetAndSave(videoMsg);
					}

					@Override
					public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1) {
						// 图片上传失败
					}
				});
//				videoMsg.setContent(mOssHelper.getUrlByObjectKey(objectkey));
//				sendMessageToNetAndSave(videoMsg);
			}

			@Override
			public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1) {
				videoMsg.setState(MessageEntity.STATE_FAILED);
				// TODO 入库处理
			}
		});
		return videoMsg;
	}

	public MessageEntity sendPictureMessage(String path){
		final MessageEntity picMessage = new MessageEntity.MessageBuilder()
				.setContent("file://" + path)
				.setType(MessageEntity.TYPE_IMAGE)
				.setId(-1)
				.build();
		addCommonInfoToMessage(picMessage);
		mOssHelper.uploadFileByUserId(mSenderId, path, new OSSCompletedCallback<PutObjectRequest, OSSResult>() {

			@Override
			public void onSuccess(PutObjectRequest arg0, OSSResult arg1) {
				MLog.v(TAG, "upload picture onSucess");
				// TODO: 图片缩略图的大小限制
				picMessage.setContent(mOssHelper.getImageUrlByObjectKey(arg0.getObjectKey(), null));
				sendMessageToNetAndSave(picMessage);
			}

			@Override
			public void onFailure(PutObjectRequest arg0, ClientException arg1, ServiceException arg2) {
				// TODO: 发送失败
				picMessage.setState(MessageEntity.STATE_FAILED);
			}
		});
		return picMessage;
	}

	private void sendMessageToNetAndSave(MessageEntity entity){
		Log.d(TAG, "MESSAGE " +entity.getMessage().toString());
		long id = mSQliteHelper.insertMessage(entity.getMessage());
		mSQliteHelper.lastMessageAddOne(entity.getMessage(), false, false);
		imServerSend(entity, id);
	}

	protected void imServerSend(MessageEntity entity, long id) {
		final BaseBean baseBean = new BaseBean();
		if(mChatType == 0){
			baseBean.setId(mSenderId + "single_chat" + id);
			baseBean.setType(BaseBean.TYPE_SINGLE_CHAT);
			baseBean.setMsgHead("private_chat");
		}else{
			baseBean.setId("group_chat" + id);
			baseBean.setType(BaseBean.TYPE_GROUP_CHAT);
			baseBean.setMsgHead("room_chat");
		}
		baseBean.setSendUser(mSenderId);
		baseBean.setReceive(mReceiverId);
		baseBean.setMsgBody(entity.generateJson(mSQliteHelper));
		IMManager.getInstance().sendBaseBean(baseBean, new Callback() {
			@Override
			public void onSuccess() {
				MLog.v(TAG, "%s 消息发送成功", baseBean.getId());
			}

			@Override
			public void onError(int errorCode, String errorMsg) {
				MLog.e(TAG, "%s 消息发送失败, %d", baseBean.getId(), errorCode);
			}
		});
	}

	private void addCommonInfoToMessage(MessageEntity entity){
		entity.setSenderId(mSenderId);
		entity.setChatType(mChatType);
		entity.setState(MessageEntity.STATE_SUCCESS);
		entity.setReceiverId(mReceiverId);
		// 使用UTC时间
		entity.setCreate(DateFormatUtil.localTime2UTCTime(new Date()));
	}


	/**
	 * 销毁前的操作,
	 * 在Activity的onDestroy调用这个方法
	 */
	public void onDestroy(){
		IMManager.getInstance().unRegisterChatReceiver(mReceiverId);
	}
}
