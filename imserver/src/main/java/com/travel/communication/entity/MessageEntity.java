package com.travel.communication.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.travel.communication.dao.Message;
import com.travel.communication.helper.SQliteHelper;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * 聊天界面的消息实体
 * 由于使用了GreenDao, 这里做一下折中, 而且IPC中的实体需要实现Parcel接口
 * 感觉做的真的不想实体类, 四不像了
 */
public class MessageEntity implements Parcelable{
	
	// 加入一个mUserId, 作为判断是否为自己的消息
	public static String mUserId;
	
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_IMAGE = 2;
	public static final int TYPE_SOUND = 1;
	public static final int TYPE_ORDERS = 3; //订单消息类型
	public static final int TYPE_GOODS_INFO = 4; //商品详情
	public static final int TYPE_VIDEO = 5;
	
	public static final int STATE_SENDING = 0;
	public static final int STATE_SUCCESS = 1;
	public static final int STATE_FAILED = 2;
	
	public static final int TYPE_COUNT = 6;
	
	// Message在传输中的常量
	public static final String SENDER_ID = "senderId";
	public static final String SENDER_IMG_URL = "senderImgUrl";
	public static final String SENDER_NICK_NAME = "senderNickName";
	public static final String RECEIVER_ID = "receiverId";
	public static final String RECEIVER_IMG_URL = "receiverImgUrl";
	public static final String RECEIVER_NICK_NAME = "receiverNickName";
	public static final String CONTENT = "content";
	public static final String CREATE_TIME = "createTime";
	public static final String TYPE = "type";
	public static final String TIME_LONG = "timeLong";
	
	
	private Message mMessage;
	/**
	 * 获取其内部的Message对象
	 * @return
	 */
	public Message getMessage(){
		return mMessage;
	}
	
	private MessageEntity(long id, int type, String senderId, String receiverId, int state, Date sendTime,
			String content, long timeLong) {
		mMessage = new Message();
		if(id >= 0){
			mMessage.setId(id);
		}
		mMessage.setMessageType(type);
		mMessage.setSenderId(senderId);
		mMessage.setReceiverId(receiverId);
		mMessage.setState(state);
		mMessage.setCreate(sendTime);
		mMessage.setContent(content);
		mMessage.setTimeLong(timeLong);
	}
	
	public MessageEntity(Message message){
		this.mMessage = message;
	}
	
	// Message的builder
	public static class MessageBuilder{
		private long id;
		private int type; // 类型, TYPE_TEXT, TYPE_IMAGE, TYPE_SOUND
		private String senderId, receiverId; // 发送者的id, 接受者id
		private int state; // 发送状态 STATE_SENDING, STATE_SUCCESS, STATE_FAILED
		private Date sendTime; 
		private String content;
		private UserData senderUserData, receiverUserData;
		private long timeLong;
		
		public MessageEntity build(){
			return new MessageEntity(id, type, senderId, receiverId, state, sendTime, content, timeLong);
		}
		
		public MessageBuilder setId(long id) {
			this.id = id;
			return this;
		}
		public MessageBuilder setType(int type) {
			this.type = type;
			return this;
		}
		public MessageBuilder setSenderId(String senderId){
			this.senderId = senderId;
			return this;
		}
		public MessageBuilder setReceiverId(String receiverId){
			this.receiverId = receiverId;
			return this;
		}
		public MessageBuilder setState(int state) {
			this.state = state;
			return this;
		}
		public MessageBuilder setSendTime(Date sendTime) {
			this.sendTime = sendTime;
			return this;
		}
		public MessageBuilder setContent(String content) {
			this.content = content;
			return this;
		}
		public MessageBuilder setSenderUserData(UserData senderUserData) {
			this.senderUserData = senderUserData;
			this.senderId = this.senderUserData.getId();
			return this;
		}
		public MessageBuilder setReceiverUserData(UserData receiverUserData) {
			this.receiverUserData = receiverUserData;
			return this;
		}
		
		public MessageBuilder setTimeLong(long timeLong) {
			this.timeLong = timeLong;
			return this;
		}
		
	}
	
	public long getId(){
		return mMessage.getId();
	}
	public void setId(long id){
		mMessage.setId(id);
	}
	
	public String getSenderId(){
		return mMessage.getSenderId();
	}
	
	public void setSenderId(String senderId){
		mMessage.setSenderId(senderId);
	}
	
	public String getReceiverId(){
		return mMessage.getReceiverId();
	}
	
	public void setReceiverId(String receiverId){
		mMessage.setReceiverId(receiverId);
	}
	
	public int getState(){
		return mMessage.getState();
	}
	public void setState(int state){
		mMessage.setState(state);
	}
	
	public int getChatType(){
		return mMessage.getChatType();
	}
	
	public void setChatType(int chatType){
		mMessage.setChatType(chatType);
	}
	
	public Date getCreate(){
		return mMessage.getCreate();
	}
	
	public void setCreate(Date date){
		mMessage.setCreate(date);
	}
	
	public String getContent(){
		return mMessage.getContent();
	}
	
	public void setContent(String content){
		mMessage.setContent(content);
	}
	
	public int getMessageType(){
		return mMessage.getMessageType();
	}
	
	public void setMessageType(int messageType){
		mMessage.setMessageType(messageType);
	}
	
	public long getTimeLong(){
		return mMessage.getTimeLong();
	}
	
	public void setTimeLong(long timeLong){
		mMessage.setTimeLong(timeLong);
	}
	
	
	
	public boolean isMine() {
		return mMessage.getSenderId().equals(mUserId);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(getId());
		dest.writeString(getSenderId());
		dest.writeString(getReceiverId());
		dest.writeInt(getState());
		dest.writeInt(getChatType());
		dest.writeLong(getCreate().getTime());
		dest.writeString(getContent());
		dest.writeInt(getMessageType());
		dest.writeLong(getTimeLong());
	}
	
	private MessageEntity(Parcel in){
		mMessage = new Message();
		setId(in.readLong());
		setSenderId(in.readString());
		setReceiverId(in.readString());
		setState(in.readInt());
		setChatType(in.readInt());
		setCreate(new Date(in.readLong()));
		setContent(in.readString());
		setMessageType(in.readInt());
		setTimeLong(in.readLong());
	}
	
	public static final Creator<MessageEntity> CREATOR
		= new Creator<MessageEntity>() {
		
		@Override
		public MessageEntity[] newArray(int size) {
			return new MessageEntity[size];
		}
		
		@Override
		public MessageEntity createFromParcel(Parcel source) {
			return new MessageEntity(source);
		}
	};
	
	public String generateJson(SQliteHelper sQliteHelper){
		JSONObject jsonObject = new JSONObject();
		try {
			UserData senderUserData = sQliteHelper.getUserData(getSenderId());
			if(senderUserData == null){
				senderUserData = new UserData();
				senderUserData.setId(getSenderId());
				senderUserData.setImgUrl(UserSharedPreference.getUserHeading());
				String nickName = UserSharedPreference.getNickName();
				nickName = nickName == null ? "游客" : nickName;
				senderUserData.setNickName(nickName);
				sQliteHelper.inserOrReplace(senderUserData);
			}
			jsonObject.put(SENDER_ID, senderUserData.getId());
			jsonObject.put(SENDER_IMG_URL, senderUserData.getImgUrl());
			jsonObject.put(SENDER_NICK_NAME, senderUserData.getNickName());
			UserData receiverUserData = sQliteHelper.getUserData(getReceiverId());
			if(receiverUserData == null){
				jsonObject.put(RECEIVER_ID, getReceiverId());
			}else{
				jsonObject.put(RECEIVER_ID, receiverUserData.getId());
				jsonObject.put(RECEIVER_IMG_URL, receiverUserData.getImgUrl());
				jsonObject.put(RECEIVER_NICK_NAME, receiverUserData.getNickName());
			}
			jsonObject.put(CONTENT, getContent());
			// 使用的是UNIX时间戳
			jsonObject.put(CREATE_TIME, String.valueOf(getCreate().getTime()/1000));
			jsonObject.put(TYPE, String.valueOf(getMessageType()));
			jsonObject.put(TIME_LONG, String.valueOf(getTimeLong()));
			jsonObject.put("groupOrSingle", String.valueOf(mMessage.getChatType()));
		} catch (JSONException e) {
			MLog.e("MessageEntity", e.getMessage());
		}
		return jsonObject.toString();
	}
}
