package com.travel.communication.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.communication.dao.Message;
import com.travel.communication.helper.NewSystemMessageHelper;
import com.travel.communication.helper.SQliteHelper;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 系统订单消息列表
 * Created by ldkxingzhe on 2016/7/25.
 */
public class SystemOrderMessageFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = "SystemOrderMessageFragment";

    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;

    private SQliteHelper mSQliteHelper;
    private PullToRefreshHelper mPullToRefreshHelper;
    private BaseAdapter mAdapter;

    private List<SystemOrderEntity> mSystemOrderEntityList = new ArrayList<SystemOrderEntity>();
    private int page = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSQliteHelper = new SQliteHelper(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_one_pull_list_view, container, false);
        mPullToRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.lv_pull);
        mListView = mPullToRefreshListView.getRefreshableView();
        mPullToRefreshHelper = new PullToRefreshHelper(mPullToRefreshListView);
        mPullToRefreshHelper.initPullDownToRefreshView(null);
        mPullToRefreshHelper.initPullUpToRefreshView(null);

        mAdapter = new SystemOrderAdapter(mSystemOrderEntityList);
        mListView.setAdapter(mAdapter);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page = 0;
                        mSystemOrderEntityList.clear();
                        new LoadDataFromDB().execute();
                    }
                }, 2000);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ++page;
                        new LoadDataFromDB().execute();
                    }
                }, 2000);
            }

        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SystemOrderActionEntity entity = mSystemOrderEntityList.get(position - 1).getAction();
                OSUtil.intentOrderInfo(getActivity(), entity.getOrdersId());
//                OrderInfoRouteActivity.actionStart(getActivity(), entity.getOrdersId(), "manage",
//                        "", new AttachGoodsBean());
            }
        });

        new LoadDataFromDB().execute();
        mSQliteHelper.lastMessageResetUnRead(NewSystemMessageHelper.MESSAGE_TYPE_ORDER_ID, UserSharedPreference.getUserId());
        return rootView;
    }

    private class LoadDataFromDB extends AsyncTask<Void, Void, List<SystemOrderEntity>> {

        @Override
        protected List<SystemOrderEntity> doInBackground(Void... params) {
            String mUserId = UserSharedPreference.getUserId();
            List<Message> messageList = mSQliteHelper.getMessageList(NewSystemMessageHelper.MESSAGE_TYPE_ORDER_ID, mUserId, 10, page * 10);
            if (messageList == null) {
                return null;
            }
            Collections.reverse(messageList);
            List<SystemOrderEntity> result = new ArrayList<SystemOrderEntity>();
            Gson gson = new Gson();
            for (Message message : messageList) {
                try {
                    SystemOrderEntity entity = gson.fromJson(message.getContent(), SystemOrderEntity.class);
                    if (entity.time <= 1000) {
                        entity.time = message.getCreate().getTime() / 1000 + 28800;
                    }
                    result.add(entity);
                } catch (Exception e) {
                    MLog.d(TAG, "json格式不正确， 不匹配: %s", message.getContent());
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<SystemOrderEntity> systemOrderEntities) {
            mPullToRefreshListView.onRefreshComplete();
            if (systemOrderEntities == null || systemOrderEntities.size() == 0) {
                MLog.e(TAG, "onPostExecute, and systemOrderList is null");
                return;
            }
            if (systemOrderEntities.size() < 10)
                mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            else
                mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            mSystemOrderEntityList.addAll(systemOrderEntities);
            mAdapter.notifyDataSetChanged();
            //            mListView.setSelection(mSystemOrderEntityList.size() + 1);
        }
    }

    public static class SystemOrderAdapter extends ListBaseAdapter<SystemOrderEntity> {

        public SystemOrderAdapter(List<SystemOrderEntity> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SystemOrderMessageViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent
                        .getContext()).inflate(R.layout.list_item_system_order, parent, false);
                viewHolder = new SystemOrderMessageViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (SystemOrderMessageViewHolder) convertView.getTag();
            }
            SystemOrderEntity entity = getItem(position);
            viewHolder.createTime.setText(entity.getCreateTime());
            viewHolder.content.setText(entity.getContent());
            return convertView;
        }

        public static class SystemOrderMessageViewHolder {
            TextView createTime;
            TextView content;
            ImageView cover;

            public SystemOrderMessageViewHolder(View convertView) {
                createTime = (TextView) convertView.findViewById(R.id.tv_title);
                content = (TextView) convertView.findViewById(R.id.tv_content);
                cover = (ImageView) convertView.findViewById(R.id.iv_cover);
            }

            public TextView getCreateTime() {
                return createTime;
            }

            public TextView getContent() {
                return content;
            }

            public ImageView getCover() {
                return cover;
            }
        }
    }

    private static class SystemOrderActionEntity {
        // 1--线路, 2--导游
        int type;
        String userType;
        String status;
        long ordersId;

        public long getOrdersId() {
            return ordersId;
        }

        public void setOrdersId(long ordersId) {
            this.ordersId = ordersId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class SystemOrderEntity {
        String title;
        String content;
        SystemOrderActionEntity action;
        long time;

        transient String timeStr;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public SystemOrderActionEntity getAction() {
            return action;
        }

        public void setAction(SystemOrderActionEntity action) {
            this.action = action;
        }

        public String getCreateTime() {
            if (timeStr == null) {
                timeStr = DateFormatUtil.formatTime(new Date(time * 1000), DateFormatUtil.FORMAT_TIME_NO_SECOND);
            }
            return timeStr;
        }

        public void setCreateTime(long createTime) {
            this.time = createTime;
        }
    }
}
