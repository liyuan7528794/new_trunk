package com.tencent.qcloud.suixinbo.presenters.viewinface;


import com.tencent.TIMUserProfile;

import java.util.List;

/**
 * 进出房间回调接口
 */
public interface EnterQuiteRoomView extends MvpView {


    void enterRoomComplete(int id_status, boolean succ);

    void quiteRoomComplete(int id_status, boolean succ, boolean isFromAvRoom);

    void memberQuiteLive(String[] list);

    void memberJoinLive(String[] list);

    void alreadyInLive(String[] list);

    void onGetGroupMembersList(List<TIMUserProfile> memberList);
}
