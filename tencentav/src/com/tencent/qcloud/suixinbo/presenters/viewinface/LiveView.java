package com.tencent.qcloud.suixinbo.presenters.viewinface;


import com.tencent.av.TIMAvManager;

import java.util.List;

/**
 *  直播界面回调
 */
public interface LiveView extends MvpView {

    void showVideoView(boolean isHost, String id);

    void onApplyInsertLine(String id, String nickname, String userImg);

    void refreshText(String text, String senderId, String name, String userImg);

    void refreshThumbUp();

    void refreshUI(String id);

    boolean showInviteView(String id);

    void cancelInviteView(String id);

    void cancelMemberView(String id);

    void memberJoin(String id, String name, String userImg);

    void memberQuit(String id, String name, String userImg);

    void readyToQuit();

    void hideInviteDialog();

    void pushStreamSucc(TIMAvManager.StreamRes streamRes);

    void onAlreadyPushing();

    void stopStreamSucc();

    void startRecordCallback(boolean isSucc);

    void stopRecordCallback(boolean isSucc, List<String> files);

    void hostLeave(String id, String name);

    void hostBack(String id, String name);

    void onSendGif(String giftBeanStr);

    void closeMemberView(String id);

    void setMirror(boolean isMirror);

    void cancelInsert(String id);
}
