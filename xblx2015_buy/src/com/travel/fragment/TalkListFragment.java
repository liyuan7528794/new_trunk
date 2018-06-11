package com.travel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.google.gson.Gson;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.ShopConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.adapter.TalkListAdapter;
import com.travel.entity.TalkBean;
import com.travel.helper.TalkCommentsHelper;
import com.travel.layout.InputPopupWindow;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.bean.CommentBean;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仿抖音版的发现页----说说列表页
 * Created by wyp on 2018/5/11.
 */

public class TalkListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SwipeRefreshRecyclerView.OnListLoadListener {
    private final String TAG = "TalkListFragment";
    private Context mContext;
    private View mView;

    private TextView tv_none;
    private SwipeRefreshRecyclerView recyclerView;
    private TalkListAdapter adapter;
    private ArrayList<TalkBean> list;
    private int mPage = 1;

    private String talkId = "-1";
    private TalkCommentsHelper commentsHelper;

    // 发布后自动刷新的功能
    private MyBroadCastReceiver myBroadCastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mView = inflater.inflate(R.layout.fragment_talk_list, container, false);

        init();
        getData();
        return mView;
    }

    private void init() {
        tv_none = (TextView) mView.findViewById(R.id.tv_none_talk);

        commentsHelper = new TalkCommentsHelper(new TalkCommentsHelper.CommentsHttpListener() {
            @Override
            public void OnGetComments(List<CommentBean> comments, boolean isSuc) {

            }

            @Override
            public void AddCommentsResult(boolean isSuc) {
                if (isSuc) {
                    Toast.makeText(getContext(), "添加成功！", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getContext(), "添加失败！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void likeSuccess() {

            }
        });

        myBroadCastReceiver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter("RefreshTalkList");
        mContext.registerReceiver(myBroadCastReceiver, intentFilter);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srRecyclerView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setOnRefreshListener(this);
        recyclerView.setOnListLoadListener(this);

        list = new ArrayList<>();
        adapter = new TalkListAdapter(getContext(), list);

        recyclerView.setAdapter(adapter);
        adapter.setClickListener(new TalkListAdapter.ClickListener() {
            @Override
            public void onClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("talk_bean", list.get(position));
                OneFragmentActivity.startNewActivity(mContext, "详情", TalkDetailsFragment.class, bundle);
            }

            @Override
            public void onClickComments(int position) {
                talkId = list.get(position).getId();
                Bundle bundle = new Bundle();
                bundle.putSerializable("talk_bean", list.get(position));
                OneFragmentActivity.startNewActivity(mContext, "详情", TalkDetailsFragment.class, bundle);
//                new InputPopupWindow(getActivity(), talkId)
//                        .setListener(new InputPopupWindow.OnListener() {
//                            @Override
//                            public void onInputText(String content, String tag) {
//                                if (UserSharedPreference.isLogin()) {
//                                    LoadingDialog.getInstance(mContext).showProcessDialog();
//                                    commentsHelper.addComments(UserSharedPreference.getUserId(), tag, content, TalkCommentsHelper.TYPE_TALK, "-1", "-1");
//                                } else
//                                    OSUtil.intentLogin(getContext());
//                            }
//                        });
            }

            @Override
            public void onClickPraise(int praiseType, int index, int pos) {
                list.get(index).setPraiseNum(
                        Math.max(praiseType == 1 ?
                                        (list.get(index).getPraiseNum() - 1) :
                                        (list.get(index).getPraiseNum() + 1),
                                0)
                );
                list.get(index).setPraiseType(praiseType);
                adapter.notifyItemChanged(pos);
            }
        });
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        LoadingDialog.getInstance(mContext).showProcessDialog();
        getData();
        recyclerView.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        LoadingDialog.getInstance(mContext).showProcessDialog();
        getData();
        recyclerView.setLoading(false);
    }

    /**
     * 获取众投数据
     */
    private void getData() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pageNo", mPage);
        map.put("type", -1);
        NetWorkUtil.postForm(mContext, ShopConstant.TALK_LIST, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                if (mPage == 1) {
                    list.clear();
                }
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        Gson gson = new Gson();
                        TalkBean talkBean = gson.fromJson(dataObject.toString(), TalkBean.class);

                        list.add(talkBean);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    notifyRecyclerView();
                    // 拉取没有更多数据了
                    if (data.length() == 0 && mPage != 1) {
                        --mPage;
                        TravelUtil.showToast(R.string.no_more, mContext);
                        recyclerView.setEnabledLoad(false);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                // 再次联网之前，初始化当前页面的控件和数据
                if (mPage > 1) {
                    --mPage;
                }
                notifyRecyclerView();
            }

            @Override
            protected void onErrorNotZero(int error, String msg) {
                super.onErrorNotZero(error, msg);
                notifyRecyclerView();
            }
        }, map);
    }

    private void notifyRecyclerView() {
        if (list.size() < 1) {
            recyclerView.setEnabledLoad(false);
            tv_none.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setEnabledLoad(true);
            tv_none.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
        LoadingDialog.getInstance(mContext).hideProcessDialog(0);
    }

    public RecyclerView getRecyclerViewInstance() {
        if (recyclerView != null) {
            return recyclerView.getScrollView();
        }
        return null;
    }

    public class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(myBroadCastReceiver);
    }
}
