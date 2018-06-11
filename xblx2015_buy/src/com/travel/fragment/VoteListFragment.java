package com.travel.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.google.gson.Gson;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.ShopConstant;
import com.travel.activity.HomeActivity;
import com.travel.adapter.DividerItemDecoration;
import com.travel.adapter.VoteListAdapter;
import com.travel.entity.PublicVoteEntity;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.pk.activity.PublicVoteActivity;
import com.travel.localfile.pk.fragment.VoteInfoFragment;
import com.travel.shop.activity.ApplicationPublicVoteActivity;
import com.travel.video.widget.MediaMenu;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/2.
 */

public class VoteListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SwipeRefreshRecyclerView.OnListLoadListener {

    private static final String TAG = "VoteListFragment";
    private Context mContext;
    private View mView;

    public static final String TYPE = "type";
    private String type;// "my":我的众投 "business":众投管理 "list":众投列表(现已弃用，若重新启用，则胜利失败会有问题)

    // 标题栏
    private View vote_title;
    private RelativeLayout rl_title;
    private Button btn_evidence;
    private TextView tv_title_public, tv_no_vote;
//    private MediaMenu mediaMenu;

    private SwipeRefreshRecyclerView srRecyclerView;
    private VoteListAdapter adapter;
    private List<PublicVoteEntity> list;
    private int mPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_vote_list, container, false);
        init();
        if (getActivity() instanceof HomeActivity) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rl_title.getLayoutParams();
            params.setMargins(0, OSUtil.dp2px(getContext(), 25), 0, 0);
            vote_title.findViewById(R.id.leftButton).setVisibility(View.GONE);
//            mediaMenu.setVisibility(View.VISIBLE);
        }

        getPublicVoteData();
        return mView;
    }

    private void init() {
        mContext = getActivity();

        vote_title = mView.findViewById(R.id.vote_title);
        rl_title = (RelativeLayout) vote_title.findViewById(R.id.titleLayout);
        tv_title_public = (TextView) vote_title.findViewById(R.id.tabTitle);
        btn_evidence = (Button) vote_title.findViewById(R.id.rightButton);
        tv_no_vote = (TextView) mView.findViewById(R.id.tv_no_vote);
//        mediaMenu = (MediaMenu) mView.findViewById(R.id.voteMenu);

        Bundle bundle = getArguments();
        type = bundle == null ? "list" : bundle.getString(TYPE, "list");
        if (TextUtils.equals(type, "my")) {// 我的众投
            vote_title.setVisibility(View.VISIBLE);
            tv_title_public.setText(getString(R.string.myPublicVote));
            btn_evidence.setVisibility(View.VISIBLE);
        } else if (TextUtils.equals(type, "business")) {// 众投管理
            vote_title.setVisibility(View.VISIBLE);
            tv_title_public.setText(getString(R.string.publicVoteManage));
        } else {
            tv_title_public.setText("众投");
//            mediaMenu.setVisibility(View.VISIBLE);
            btn_evidence.setVisibility(View.VISIBLE);
        }
        Drawable img = ContextCompat.getDrawable(mContext, OSUtil.isDayTheme() ? R.drawable.icon_add_day : R.drawable.icon_add_night);
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        btn_evidence.setCompoundDrawables(img, null, null, null);
        btn_evidence.setText("发起众投");
        try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(rl_title, 10f);
        } catch (Exception e) {
            vote_title.findViewById(R.id.views).setVisibility(View.VISIBLE);
            MLog.e(TAG, e.getMessage(), e);
        }

        btn_evidence.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!UserSharedPreference.isLogin()) {
                    startActivity(new Intent(ShopConstant.LOG_IN_ACTION));
                    return;
                }
                startActivity(new Intent(mContext, ApplicationPublicVoteActivity.class));
            }
        });

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        srRecyclerView = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srRecyclerView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        srRecyclerView.getScrollView().addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL_LIST, OSUtil.dp2px(getContext(), 10), android.R.color.transparent));
        srRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        srRecyclerView.setOnRefreshListener(this);
        srRecyclerView.setOnListLoadListener(this);
        if (OSUtil.isDayTheme())
            srRecyclerView.setLoadViewBackground(ContextCompat.getColor(mContext, android.R.color.white));
        else
            srRecyclerView.setLoadViewBackground(ContextCompat.getColor(mContext, R.color.black_3));
        list = new ArrayList<>();
        adapter = new VoteListAdapter(getContext(), list);
        srRecyclerView.setAdapter(adapter);
        adapter.setClickListener(new VoteListAdapter.ClickListener() {
            @Override
            public void onClick(int position) {
                PublicVoteEntity entity = list.get(position);
                Bundle bundle = new Bundle();
                int voteStatus = -1;
                if (entity.getStatus() == 0) {
                    voteStatus = VoteInfoFragment.WIN_VOTING;
                } else {
                    if (entity.getType() == 1) {
                        voteStatus = VoteInfoFragment.WIN_BUYER;
                    } else if (entity.getType() == 2) {
                        voteStatus = VoteInfoFragment.WIN_SELLER;
                    } else {
                        voteStatus = VoteInfoFragment.WIN_UNKNOWN;
                    }
                }
                bundle.putInt(VoteInfoFragment.BUNDLE_VOTE_ID, Integer.valueOf(entity.getId()));
                bundle.putInt(VoteInfoFragment.BUNDLE_VOTE_STATUS, voteStatus);
                bundle.putString(VoteInfoFragment.USER_ID_SELLER, entity.getSellerId());
                bundle.putString(VoteInfoFragment.USER_ID_BUYER, entity.getBuyerId());
                bundle.putString(VoteInfoFragment.BUNDLE_VOTE_TITLE, entity.getReason());
                Intent intent = new Intent(getActivity(), PublicVoteActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        getPublicVoteData();
        srRecyclerView.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        getPublicVoteData();
        srRecyclerView.setLoading(false);
    }

    /**
     * 获取众投数据
     */
    private void getPublicVoteData() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pageNo", mPage);
        String url = ShopConstant.PUBLIC_VOTE_LIST_MINE;
        if (TextUtils.equals(type, "my")) {// 我的众投
            map.put("userId", UserSharedPreference.getUserId());
            map.put("userType", 1);
        } else if (TextUtils.equals(type, "business")) {// 众投管理
            map.put("userId", UserSharedPreference.getUserId());
            map.put("userType", 2);
        } else
            url = ShopConstant.PUBLIC_VOTE_LIST;
        NetWorkUtil.postForm(mContext, url, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                if (mPage == 1) {
                    list.clear();
                }
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        Gson gson = new Gson();
                        PublicVoteEntity publicVote = gson.fromJson(dataObject.toString(), PublicVoteEntity.class);
                        if (publicVote.getStatus() == 3) {
                            // 买家胜 type = 1
                            if (dataObject.optString("victory").equals(publicVote.getBuyerId()))
                                publicVote.setType(1);
                                // 卖家胜 type = 2
                            else if (dataObject.optString("victory").equals(publicVote.getSellerId()))
                                publicVote.setType(2);
                        } else if (publicVote.getStatus() == 2)
                            publicVote.setType(0);

                        list.add(publicVote);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    notifyRecyclerView();
                    // 拉取没有更多数据了
                    if (data.length() == 0 && mPage != 1) {
                        --mPage;
                        TravelUtil.showToast(R.string.no_more, mContext);
                        srRecyclerView.setEnabledLoad(false);
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
            srRecyclerView.setEnabledLoad(false);
            mView.findViewById(R.id.tv_no_vote).setVisibility(View.VISIBLE);
        } else {
            srRecyclerView.setEnabledLoad(true);
            mView.findViewById(R.id.tv_no_vote).setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    public RecyclerView getRecyclerViewInstance() {
        if (srRecyclerView != null) {
            return srRecyclerView.getScrollView();
        }
        return null;
    }
}
