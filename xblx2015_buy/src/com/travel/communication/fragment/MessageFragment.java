package com.travel.communication.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.activity.HomeActivity;
import com.travel.activity.OneFragmentSingleTopActivity;
import com.travel.app.TravelApp;
import com.travel.communication.activity.ChatActivity;
import com.travel.communication.adapter.LastMessageAdapter;
import com.travel.communication.dao.LastMessage;
import com.travel.communication.dao.Message;
import com.travel.communication.entity.MessageEntity;
import com.travel.communication.entity.UserData;
import com.travel.communication.helper.NewSystemMessageHelper;
import com.travel.communication.helper.SQliteHelper;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MessageBroadcastHelper;
import com.travel.lib.utils.UserSharedPreference;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 消息回话列表
 * 位于主页的消息Fragment
 */
public class MessageFragment extends Fragment implements OnItemClickListener, MessageBroadcastHelper.MessageHelperCallback, AdapterView.OnItemLongClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MessageFragment";
    // UI部分
    private View layout_message_title;
    private RelativeLayout titleLayout;
    private ListView mListView;
    private PullToRefreshListView mPullToRefreshListView;
    private TextView mTitle;

    private LastMessageAdapter mAdapter;
    private SQliteHelper mSQliteHelper;
    private PullToRefreshHelper mPullToRefreshHelper;
    private MessageBroadcastHelper mMessageBroadcastHelper;

    private View mEmptyView;

    private List<LastMessage> mLastMessageList;
    // 接受者Id
    private String receiverId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageBroadcastHelper = new MessageBroadcastHelper(getActivity(), this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);
        mPullToRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.pull_to_refresh_list_view);
        mListView = mPullToRefreshListView.getRefreshableView();
        mEmptyView = rootView.findViewById(R.id.tv_no_message);
        layout_message_title = rootView.findViewById(R.id.layout_message_title);
        titleLayout = (RelativeLayout) layout_message_title.findViewById(R.id.titleLayout);
        mTitle = (TextView) layout_message_title.findViewById(R.id.tabTitle);
        mTitle.setText("消息");
        try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(titleLayout, 10f);
        } catch (Exception e) {
            MLog.e(TAG, e.getMessage(), e);
            layout_message_title.findViewById(R.id.views).setVisibility(View.VISIBLE);
        }
        layout_message_title.setVisibility(View.GONE);
        initRefreshListView();
        initRealListView();
        //		initTestData();
        return rootView;
    }

    private void initTestData() {
        receiverId = "xingzhe";
        Message message = new Message();
        message.setReceiverId("9");
        message.setSenderId("9");
        message.setContent("测试最后一条数据");
        message.setCreate(new Date());
        message.setMessageType(0);
        message.setState(MessageEntity.STATE_SUCCESS);
        mSQliteHelper.insertMessage(message);
        mSQliteHelper.lastMessageAddOne(message, true, true);
        UserData userData = new UserData();
        userData.setId("ldkxingzhe");
        userData.setNickName("明月天涯");
        userData.setImgUrl("http://pic31.nipic.com/20130718/12834382_112335424179_2.jpg");
        mSQliteHelper.inserOrReplace(userData);
        userData.setId("xingzhe");
        userData.setNickName("行者无疆");
        mSQliteHelper.inserOrReplace(userData);
    }


    private void initRefreshListView() {
        mPullToRefreshHelper = new PullToRefreshHelper(mPullToRefreshListView);
        mPullToRefreshHelper.initPullDownToRefreshView(null);
        mPullToRefreshHelper.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {

            @Override
            public void onRefresh(PullToRefreshBase refreshView) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        loadSQLite(true);
                    }
                }, 300);
            }
        });
    }

    private void initRealListView() {
        mSQliteHelper = new SQliteHelper(TravelApp.appContext);
        mAdapter = new LastMessageAdapter(mLastMessageList);
        mListView.setAdapter(mAdapter);
        mListView.setDivider(null);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiverId = UserSharedPreference.getUserId();
        loadSQLite(false);
        mMessageBroadcastHelper.registerMessageCommingReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMessageBroadcastHelper.unRegisterMessageCommingReceiver();
    }

    // 加载数据库刷新界面
    private void loadSQLite(final boolean sleep) {
        AsyncTask<Void, Void, List<LastMessage>> asyncTask = new AsyncTask<Void, Void, List<LastMessage>>() {

            @Override
            protected List<LastMessage> doInBackground(Void... params) {
                MLog.v(TAG, "doInBackground, load data");
                if (sleep) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        MLog.e(TAG, e.getMessage(), e);
                    }
                }
                List<LastMessage> result = mSQliteHelper.getLastMessageList(receiverId);
                for (int i = 0; i < result.size(); i++) {
                    if (result.get(i).getMessage().getChatType() != null && result.get(i).getMessage().getChatType() == 1) {// 若不是订单详情且是群聊，则用群聊的Id再次获取最后一条数据
                        List<LastMessage> resultGroup = mSQliteHelper.getLastMessageListSenderId(result.get(i).getMessage().getReceiverId());
                        result.get(i).getMessage().setContent(resultGroup.get(0).getMessage().getContent());
                        result.get(i).getMessage().setCreate(resultGroup.get(0).getMessage().getCreate());
                        result.get(i).getMessage().setMessageType(resultGroup.get(0).getMessage().getMessageType());
                        result.get(i).setUnReadNumber(resultGroup.get(0).getUnReadNumber());
                    }
                }
                List<LastMessage> unReadList = new ArrayList<>();
                for (int i = result.size() - 1; i >= 0; i--) {
                    LastMessage message = result.get(i);
                    if (message.getUnReadNumber() != null && message.getUnReadNumber() > 0) {
                        result.remove(message);
                        unReadList.add(message);
                    }
                }
                result.addAll(0, unReadList);
                return result;
            }

            @Override
            protected void onPostExecute(List<LastMessage> result) {
                MLog.v(TAG, "result.size is " + result.size());
                mLastMessageList = result;
                mEmptyView.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
                mAdapter.notifyDataSetChanged(mLastMessageList);
                mPullToRefreshListView.onRefreshComplete();

                int unReadNum = 0;
                for (LastMessage message : result) {
                    unReadNum += message.getUnReadNumber();
                }
                HomeActivity.setShowRedPoint(HomeActivity.MESSAGE, unReadNum, unReadNum > 0);
            }
        };
        asyncTask.execute();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MLog.v(TAG, "onItemClick, and position is " + position);
        String senderId = mLastMessageList.get(position - 1).getSenderId();
        if (NewSystemMessageHelper.MESSAGE_TYPE_ORDER_ID.equals(senderId)) {
            OneFragmentSingleTopActivity.startNewActivity(getActivity(), "公告通知",
                    SystemOrderMessageFragment.class, null);
        } else {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            UserData userData = mSQliteHelper.getUserData(senderId);
            intent.setClass(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.ID, senderId);
            intent.putExtra(ChatActivity.NICK_NAME, userData.getNickName());
            if (senderId.startsWith("@TGS")) {
                intent.putExtra("is_group_chat", true);
                //			intent.putExtra("img_url", groupFaceUrl == null ? "" : groupFaceUrl);
            }
            startActivity(intent);
        }
        mSQliteHelper.lastMessageResetUnRead(senderId, receiverId);
    }

    @Override
    public void onMessageComming() {
        loadSQLite(false);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        String senderId = mLastMessageList.get(position - 1).getSenderId();
        final UserData userData = mSQliteHelper.getUserData(senderId);
        MLog.v(TAG, "onItemLongClick, and nickName is %s.", userData.getNickName());
        AlertDialogUtils.alertDialog(getActivity(), "是否删除与\n" + userData.getNickName() + "\n的会话", new Runnable() {
            @Override
            public void run() {
                mSQliteHelper.lastMessageRemoveConversation(userData.getId(), receiverId);
                mLastMessageList.remove(position - 1);
                mAdapter.notifyDataSetChanged();
            }
        });
        return true;
    }
}
