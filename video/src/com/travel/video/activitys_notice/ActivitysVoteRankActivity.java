package com.travel.video.activitys_notice;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.video.adapter.ActivityVoteAdapter;
import com.travel.video.bean.ActivityVoteInfo;
import com.travel.VideoConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动投票排行列表
 *
 * @author Administrator
 */
public class ActivitysVoteRankActivity extends TitleBarBaseActivity {
    private ListView listView;
    private PullToRefreshListView pullRefreshListView;
    private List<ActivityVoteInfo> cList;
    private ActivityVoteAdapter myAdapter;
    private String activityId = "-1";
    private RelativeLayout rl_no_vote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitys_vote_rank);
        rl_no_vote = findView(R.id.rl_no_vote);
        setTitle("排行榜");
        activityId = getIntent().getStringExtra("activityId");
        initListView();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection()))
            loadingData();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initListView() {
        pullRefreshListView = (PullToRefreshListView) findViewById(R.id.voteList);
        cList = new ArrayList<>();
        pullRefreshListView.setMode(Mode.PULL_FROM_START);
        pullRefreshListView.setOnRefreshListener(new OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                String label = DateUtils.formatDateTime(ActivitysVoteRankActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                cList.clear();
                loadingData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            }
        });

        listView = pullRefreshListView.getRefreshableView();

        myAdapter = new ActivityVoteAdapter(ActivitysVoteRankActivity.this, cList);
        listView.setAdapter(myAdapter);
        listView.setDivider(ContextCompat.getDrawable(getApplicationContext(), R.color.gray_D8));
        listView.setDividerHeight(OSUtil.dp2px(getApplicationContext(), 0.5f));
    }


    /**
     * 加载评论
     */
    private void loadingData() {
        if ("-1".equals(activityId)) {
            return;
        }

        Map<String, Object> paramap = new HashMap<String, Object>();
        paramap.put("activityId", activityId);
        System.out.println("sdsdd---->" + paramap);
        NetWorkUtil.postForm(ActivitysVoteRankActivity.this, VideoConstant.ACTIVITYS_VOTE_RANK_LIST, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                try {
                    if (data != null && data.length() > 0) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataObject = data.getJSONObject(i);
                            ActivityVoteInfo bean = new ActivityVoteInfo();
                            bean.setId(JsonUtil.getJson(dataObject, "id"));
                            bean.setImgUrl(JsonUtil.getJson(dataObject, "imgUrl"));
                            bean.setNickName(JsonUtil.getJson(dataObject, "nickName"));
                            bean.setVoteNum(JsonUtil.getJsonInt(dataObject, "voteNum"));
                            cList.add(bean);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    pullRefreshListView.onRefreshComplete();
                    if (cList.size() == 0) {
                        rl_no_vote.setVisibility(View.VISIBLE);
                        pullRefreshListView.setVisibility(View.GONE);
                    } else {
                        rl_no_vote.setVisibility(View.GONE);
                        myAdapter.notifyDataSetChanged();
                        pullRefreshListView.setVisibility(View.VISIBLE);
                    }
                }

            }
        }, paramap);
    }
}
