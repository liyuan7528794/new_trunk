/*
package com.travel.activity;

import java.util.List;

import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.helper.SQliteHelper;
import com.travel.communication.helper.SendCommandHelper;
import com.travel.widget.LiveCollectView;

import tv.danmaku.ijk.media.player.media.IjkVideoView;

*/
/**
 * 横屏状态的景区activity必须实现的接口
 * @author ldkxingzhe
 *//*

public interface InterestingFakeLandacapeActivityInterface {
	*/
/**
	 * 发送消息
	 * @param message  文字信息
	 *//*

	void onSendMessage(String message);
	*/
/**
	 * 获取图片
	 *//*

	void onPictureClick();
	*/
/**
	 * 更新同步播放器
	 * 用于横竖屏切换过程中某个视频播放器由null生成, 或者变成null 
	 *//*

	void updateVideoView(IjkVideoView videoScenic, IjkVideoView videoExplainer, LiveCollectView liveCollectView);
	*/
/**
	 * 获取消息列表, 获取一个引用, 每次都调用此方法获取
	 *//*

	List<MessageEntity> getMessageEntityList();
	*/
/**
	 * 获取主程序的数据库辅助类
	 * @return
	 *//*

	SQliteHelper getSQliteHelper();
	*/
/**
	 * 获取发送指令的辅助类 
	 * @return
	 *//*

	SendCommandHelper getSendCommandHelper();
	*/
/**
	 * 获取直播云自定义字段 
	 * @return
	 *//*

	String getLiveDIYParams();
	void onMessageClick(int position);
	void onImageClick(MessageEntity messageEntity);
	void onMessageHeaderClick(MessageEntity messageEntity);
	void showFollowPopWindow(UserData userData);
	UserData getExplainer();
	int getPraiseNum();
}
*/
