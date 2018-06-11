package com.travel.communication.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.communication.adapter.MessageAdapter;
import com.travel.communication.adapter.MessageAdapter.MessageListener;
import com.travel.communication.dao.Message;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.helper.DefaultChatInputHelper;
import com.travel.communication.helper.GroupMessageSenderHelper;
import com.travel.communication.helper.ListViewOnScrollChangedListener;
import com.travel.communication.helper.MessageSenderHelper;
import com.travel.communication.helper.MessageSenderHelper.MessageHelperListener;
import com.travel.communication.helper.PlayerHelper;
import com.travel.communication.helper.SQliteHelper;
import com.travel.communication.utils.DirUtils;
import com.travel.communication.utils.GoodsInfoBeanJsonUtil;
import com.travel.communication.view.ChatKeyboard;
import com.travel.communication.view.PopupList;
import com.travel.layout.ImageViewPopupWindow;
import com.travel.layout.VideoViewFragment;
import com.travel.lib.TravelApp;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.AdapterViewUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 聊天界面, 囊括了群聊与单聊, 理论上, App中出现的所有聊天性质的的界面都应该使用这个
 *
 * @author ldkxingzhe
 */
public class ChatFragment extends Fragment implements DefaultChatInputHelper.AfterSendMessageListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ChatFragment";

    // 下面出现的辅助类, 应该由Activity进行注入
    private SQliteHelper mSQliteHelper;
    private boolean mIsBlackUser;

    // 这些对应私有
    private MessageSenderHelper mSenderHelper;
    private DefaultChatInputHelper mChatInputHelper;
    private PullToRefreshHelper mPullToRefreshHelper;
    private PlayerHelper mPlayerHelper;
    private MessageAdapter mAdapter;
    private List<MessageEntity> mMessageList;
    private Handler mHandler;
    private ListViewOnScrollChangedListener mScrollChangedListener;

    // UI
    private ListView mListView;
    private PullToRefreshListView mPullToRefreshListView;
    private ChatKeyboard mChatKeyboard;

    public static boolean mIsGroupChat = true; // 是否是群聊 true -- 是群聊
    private String mUserId; // 我的用户Id
    private String mOtherId; // 与我聊天人的Id, 或者群的Id(roomNum)
    private String groupAddress;// 群地址
    private ImageViewPopupWindow mImageViewPopupWindow;
//    private VideoViewPopWindow mVideoViewPopWindow;
    private PopupList mPopupList;

    private GoodsBasicInfoBean mGoodsBasicInfoBean;

    public ChatFragment() {
        mMessageList = new ArrayList<MessageEntity>();
        mHandler = new Handler();
    }

    public List<MessageEntity> getMessageList() {
        return mMessageList;
    }

    /**
     * 设置SQliteHelper
     *
     * @param sQliteHelper
     */
    public void setSQLiteHelper(SQliteHelper sQliteHelper) {
        mSQliteHelper = sQliteHelper;
    }

    public void setCityName(String address) {
        if (!TextUtils.isEmpty(address)) {
            CurLiveInfo.getInstance().setAddress(address);
            groupAddress = address;
        }
    }

    /**
     * 设置聊天的一些信息
     *
     * @param mUserId       我的Id
     * @param otherPersonId 对方人的Id
     * @param roomNum       房间的Id roomNum
     */
    public void setChatInfo(String mUserId, String otherPersonId, String roomNum) {
        if (!TextUtils.isEmpty(otherPersonId) && !TextUtils.isEmpty(roomNum))
            throw new IllegalArgumentException("is single chat or group chat?");
        this.mUserId = mUserId;
        if (mSQliteHelper == null) {
            mSQliteHelper = new SQliteHelper(TravelApp.appContext);
        }
        UserData mUserData = new UserData();
        mUserData.setId(UserSharedPreference.getUserId());
        mUserData.setNickName(UserSharedPreference.getNickName());
        mUserData.setImgUrl(UserSharedPreference.getUserHeading());
        mSQliteHelper.inserOrReplace(mUserData);
        mUserId = MessageEntity.mUserId = mUserData.getId();
        if (!TextUtils.isEmpty(otherPersonId)) {
            if (!otherPersonId.equals(mOtherId)) {
                mMessageList.clear();
            }
            mOtherId = otherPersonId;
            mIsGroupChat = false;
        }
        if (!TextUtils.isEmpty(roomNum)) {
            mOtherId = roomNum;
            mIsGroupChat = true;
        }
        if (mSenderHelper != null) {
            mSenderHelper.setUserInfo(mUserId, mOtherId);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSenderHelper = MessageSenderHelper.getMessageSenderHelper(getActivity(), mIsGroupChat ? 1 : 0, mUserId, mOtherId);
        mPlayerHelper = PlayerHelper.getInstance(getActivity(), DirUtils.getRecorderCachePath());
        mPlayerHelper.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!(oldVoiceImage.getDrawable() instanceof AnimationDrawable))
                            return;
                        AnimationDrawable animationDrawable = (AnimationDrawable) oldVoiceImage.getDrawable();
                        animationDrawable.stop();

                        if ("left".equals(oldVoiceImage.getTag()))
                            oldVoiceImage.setImageResource(R.drawable.chat_voice_left);
                        else
                            oldVoiceImage.setImageResource(R.drawable.chat_voice_right);
                        //                        oldVoiceImage.setImageResource(R.drawable.chat_voice_left);
                    }
                });
            }
        });
        initSenderHelper();
    }

    public void sendGoodsInfoMessage(GoodsBasicInfoBean mGoodsBasicInfoBean) {
        this.mGoodsBasicInfoBean = mGoodsBasicInfoBean;

    }

    private void sendGoodsInfoMessage() {
        if (mGoodsBasicInfoBean != null) {
            addOneMessageToListView(mSenderHelper.sendOtherMessage(GoodsInfoBeanJsonUtil.toJsonStr(mGoodsBasicInfoBean),
                    MessageEntity.TYPE_GOODS_INFO));
            mGoodsBasicInfoBean = null;
        }
    }

    public void sendLiveInfoMessage(String customLiveStr) {
        if (mSenderHelper instanceof GroupMessageSenderHelper) {
            GroupMessageSenderHelper groupMessageSenderHelper = (GroupMessageSenderHelper) mSenderHelper;
            groupMessageSenderHelper.sendCustomStr(customLiveStr);
        }
    }

    public void followGroup(boolean isFollow) {
        if (mSenderHelper instanceof GroupMessageSenderHelper) {
            ((GroupMessageSenderHelper) mSenderHelper).followGroup(isFollow);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //		mSenderHelper.onDestroy();
    }

    private void initSenderHelper() {
        mSenderHelper.setListener(new MessageHelperListener() {
            @Override
            public void onMessageComming(final MessageEntity messageEntity) {
                if (mUserId.equals(messageEntity.getSenderId()))
                    return;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mIsGroupChat || TextUtils.equals(messageEntity.getReceiverId(), mOtherId))
                            addOneMessageToListView(messageEntity);
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        mPullToRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.pull_to_refresh_list_view);
        mListView = mPullToRefreshListView.getRefreshableView();
        mChatKeyboard = (ChatKeyboard) rootView.findViewById(R.id.chat_key_board);
        mImageViewPopupWindow = new ImageViewPopupWindow();
//        mVideoViewPopWindow = new VideoViewPopWindow();
        mChatKeyboard.setFragmentManager(getChildFragmentManager());
        mChatInputHelper = new DefaultChatInputHelper(this, mSenderHelper, mChatKeyboard);
        mChatInputHelper.setAfterSendMessageListener(this);
        mChatInputHelper.onResume();
        initListViewAdapter();
        initPopupList();
        loadData();
        mChatKeyboard.requestFocus();
        return rootView;
    }

    private void initPopupList() {
        mPopupList = new PopupList();
        mPopupList.init(getActivity(), mListView, Arrays.asList(new String[]{"删除", "复制"}),
                new PopupList.OnPopupListClickListener() {
                    @Override
                    public void onPopupListClick(View contextView, int contextPosition, int position) {
                        MLog.d(TAG, "onPopupListClick and position is: %d.", contextPosition);
                        if (contextPosition <= 0)
                            return;
                        int realPosition = contextPosition - 1;
                        if (position == 0) {
                            mSQliteHelper.deleteMessage(mMessageList.remove(realPosition).getId());
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 复制
                            OSUtil.copy(mMessageList.get(realPosition).getContent());
                        }
                    }
                });
    }

    private void loadData() {
        // TODO:删除这个判断
        if (mSQliteHelper == null)
            return;
        List<Message> messageList = getHistoryMessage();
        addMessageList(messageList);
    }


    @Override
    public void onPause() {
        super.onPause();
        mSQliteHelper.lastMessageResetUnRead(mOtherId, mUserId);
        mPlayerHelper.stopPlayer();
        if (mHandler != null && oldVoiceImage != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!(oldVoiceImage.getDrawable() instanceof AnimationDrawable))
                        return;
                    AnimationDrawable animationDrawable = (AnimationDrawable) oldVoiceImage.getDrawable();
                    animationDrawable.stop();

                    if ("left".equals(oldVoiceImage.getTag()))
                        oldVoiceImage.setImageResource(R.drawable.chat_voice_left);
                    else
                        oldVoiceImage.setImageResource(R.drawable.chat_voice_right);
                    //                        oldVoiceImage.setImageResource(R.drawable.chat_voice_left);
                }
            });
    }

    /**
     * 向聊天列表添加一条消息
     *
     * @param messageEntity
     */
    public void addOneMessageToListView(MessageEntity messageEntity) {
        mMessageList.add(messageEntity);
        mAdapter.notifyDataSetChanged();
        postDelay(new Runnable() {

            @Override
            public void run() {
                mListView.smoothScrollToPosition(mMessageList.size());
                mListView.setOnScrollListener(new OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == SCROLL_STATE_IDLE) {
                            mListView.setSelection(mMessageList.size());
                            mListView.setOnScrollListener(null);
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                         int totalItemCount) {
                        // igonre
                    }
                });
            }
        }, 300);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(groupAddress)) {
            CurLiveInfo.getInstance().setAddress(groupAddress);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatInputHelper.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        mChatInputHelper.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isClick = false;

    private ImageView oldVoiceImage;

    private void initListViewAdapter() {
        mAdapter = new MessageAdapter(mMessageList, mSQliteHelper);
        mListView.setAdapter(mAdapter);
        mScrollChangedListener = new ListViewOnScrollChangedListener(mListView, mAdapter);
        //		mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setDividerHeight(OSUtil.dp2px(getActivity(), 10));
        mListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter.setOnHeaderClickListener(new MessageListener() {

            @Override
            public void onVoiceClick(MessageEntity entity, ImageView voiceImage) {
                if (oldVoiceImage != null && (oldVoiceImage.getDrawable() instanceof AnimationDrawable)) {
                    AnimationDrawable animationDrawable = (AnimationDrawable) oldVoiceImage.getDrawable();
                    animationDrawable.stop();
                    if ("left".equals(oldVoiceImage.getTag()))
                        oldVoiceImage.setImageResource(R.drawable.chat_voice_left);
                    else
                        oldVoiceImage.setImageResource(R.drawable.chat_voice_right);
                }

                if ("left".equals(voiceImage.getTag()))
                    voiceImage.setImageResource(R.drawable.anim_icon_voice_left);
                else
                    voiceImage.setImageResource(R.drawable.anim_icon_voice_right);
                AnimationDrawable animationDrawable = (AnimationDrawable) voiceImage.getDrawable();
                animationDrawable.start();
                oldVoiceImage = voiceImage;

                mPlayerHelper.playerFullPathAudio(entity.getContent(), true);
            }

            @Override
            public void onImageClick(MessageEntity entity) {
                ChatFragment.this.onImageClick(entity);
            }

            @Override
            public void onVideoClick(MessageEntity entity, @NonNull String videoPath) {
                if (isClick)
                    return;
                isClick = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isClick = false;
                    }
                }, 500);
                Bundle bundle = new Bundle();
                bundle.putString("path", videoPath);
                OneFragmentActivity.startNewActivity(getContext(), "", VideoViewFragment.class, bundle);
            }

            @Override
            public void onItemLongClick(View view, int position, float lastRawX, float lastRawY) {
                mPopupList.setRawPosition(lastRawX, lastRawY);
                mPopupList.showPopupListWindow(position + 1, view);
            }

            @Override
            public void onHeaderClick(MessageEntity entity) {
                onMessageHeaderClick(entity);
            }
        });
        mPullToRefreshHelper = new PullToRefreshHelper(mPullToRefreshListView);
        mPullToRefreshHelper.initPullDownToRefreshView(null);
        mPullToRefreshHelper.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                MLog.v(TAG, "onMessageRefresh");
                onMessageRefresh();
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                OSUtil.hideKeyboard(getActivity());
                mPopupList.hidePopupListWindow();
                if (mChatKeyboard.isFaceLayoutShowing()) {
                    mChatKeyboard.hideFragment();
                }
                return false;
            }
        });
    }

    public void onMessageHeaderClick(MessageEntity entity) {
        if (entity == null || String.valueOf(entity.getSenderId()).length() >= 8) {
            // 游客身份
            showToast("该用户为游客, 您不能对其进行任何操作");
        } else {
            showFollowPopWindow(mSQliteHelper.getUserData(entity.getSenderId()));
        }
    }

    public void showFollowPopWindow(UserData userData) {
        MLog.v(TAG, "onMessageClick, and userData is " + userData);
        PopWindowUtils.followPopUpWindow(getActivity(), userData.getId(),
                userData.getNickName(), userData.getImgUrl(), 1);
    }

    public void onMessageHeaderClick(int position) {
        onMessageHeaderClick(mMessageList.get(position));
    }

    /**
     * 模拟onBackPressed事件,
     *
     * @return true -- 消费此次点击事件,
     */
    public boolean onBackPressed() {
        if (mImageViewPopupWindow != null && mImageViewPopupWindow.isShowing()) {
            mImageViewPopupWindow.dismissPopupWindow();
            return true;
        }
//
//        if (mVideoViewPopWindow != null && mVideoViewPopWindow.isShowing()) {
//            mVideoViewPopWindow.dismiss();
//            return true;
//        }
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                && getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            // ignore
        } else {
            if (mChatKeyboard.isFaceLayoutShowing()) {
                mChatKeyboard.hideFragment();
                return true;
            }
        }
        return false;
    }

    public void onImageClick(MessageEntity entity) {
        MLog.v(TAG, "onImageClick, and url is %s", entity.getContent());
        mImageViewPopupWindow.show(getActivity(), mChatKeyboard, entity.getContent());
    }

    private void onMessageRefresh() {
        final List<Message> messageEntList = getHistoryMessage();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mPullToRefreshListView.onRefreshComplete();
            }
        }, 2000);
        if (messageEntList == null || messageEntList.size() == 0) {
            showToast("没有更多消息了...");
            return;
        }
        addMessageList(messageEntList);
    }

    private List<Message> getHistoryMessage() {
        int currentSize = mMessageList.size();
        if (mIsGroupChat) {
            return mSQliteHelper.getGroupMessage(mOtherId, 20, currentSize);
        } else {
            return mSQliteHelper.getCommunicationMessage(mUserId, mOtherId, 20, currentSize);
        }
    }

    /**
     * 向消息头部添加消息
     *
     * @param messageList
     */
    private void addMessageList(final List<Message> messageList) {
        postDelay(new Runnable() {
            @Override
            public void run() {
                for (Message message : messageList) {
                    MessageEntity messageEntity = new MessageEntity(message);
                    mMessageList.add(0, messageEntity);
                }
                AdapterViewUtil.addItemsToTop(mListView, messageList.size());
                mAdapter.notifyDataSetChanged();
            }
        }, 600);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSenderHelper.onDestroy();
        if (mImageViewPopupWindow != null && mImageViewPopupWindow.isShowing()) {
            mImageViewPopupWindow.dismissPopupWindow();
        }
    }

    private void postDelay(Runnable runnable, long delayTime) {
        mHandler.postDelayed(runnable, delayTime);
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSendMessage(MessageEntity entity) {
        sendGoodsInfoMessage();
        addOneMessageToListView(entity);
    }
}
