package com.travel.localfile.pk.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.communication.entity.UserData;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.activity.PersonalHomeActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投票明细界面
 * Created by ldkxingzhe on 2016/7/16.
 */
public class VoteDetailFragment extends ListFragment{
    @SuppressWarnings("unused")
    private static final String TAG = "VoteDetailFragment";

    public static final String BUNDLE_USER_ID = "user_id";
    public static final String BUNDLE_IS_WINNER = "is_winner";
    public static final String BUNDLE_VOTE_ID = "vote_id";

    private String mWhoseDetailId = "9"; // 查看者的Id
    private boolean mIsWinner;     // 是否是胜者, 是否有获奖者
    private int mAwardsUserId = -1;  // 获奖者Id
    private int mPublicVoteId = -1;

    private List<VoteDetailEntity> mVoteDetailEntityList = new ArrayList<VoteDetailEntity>();
    private VoteDetailAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args == null){
            MLog.e(TAG, "args is null");
        }else{
            mWhoseDetailId = args.getString(BUNDLE_USER_ID);
            mIsWinner = args.getBoolean(BUNDLE_IS_WINNER);
            mPublicVoteId = args.getInt(BUNDLE_VOTE_ID);
        }
        mAdapter = new VoteDetailAdapter(mVoteDetailEntityList);
        getVoteListFromNet();
        getAwardsList();
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView listView = getListView();
        listView.setDivider(null);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData userData = mVoteDetailEntityList.get(position).userData;
                PersonalHomeActivity.actionStart(getActivity(), false, userData.getId(), userData.getNickName());
            }
        });
        setEmptyText("没有投票详情");
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setAdapter(mAdapter);
    }

    private void getAwardsList(){
        String url = Constants.Root_Url + "/awards/awardsList.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", mPublicVoteId);
        NetWorkUtil.postForm(getActivity(), url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                if(data == null || data.length() == 0) return;
                mAwardsUserId = JsonUtil.getJsonInt(JsonUtil.getJSONObject(data, 0), "userid");
                mAdapter.notifyDataSetChanged();
            }
        }, map);
    }

    private void getVoteListFromNet(){
        String url = Constants.Root_Url + "/Vote/voteList.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", mPublicVoteId);
        map.put("selectedId", mWhoseDetailId);
        NetWorkUtil.postForm(getActivity(), url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                if(data == null) return;
                List<VoteDetailEntity> list = new ArrayList<VoteDetailEntity>();
                for(int i = 0, length = data.length(); i < length; i++){
                    JSONObject jsonObject = JsonUtil.getJSONObject(data, i);
                    VoteDetailEntity entity = new VoteDetailEntity();
                    entity.createTime = JsonUtil.getJsonLong(jsonObject, "createTime");
                    entity.userData = UserData.generateUserData(
                            (JSONObject) JsonUtil.get(jsonObject, "voteUser"));
                    list.add(entity);
                }

                mVoteDetailEntityList.clear();
                mVoteDetailEntityList.addAll(list);
                mAdapter.notifyDataSetChanged();
                setListShown(true);
            }
        }, map);
    }

    private static class VoteDetailEntity{
        long createTime;
        UserData userData;
    }

    private final class VoteDetailAdapter extends ListBaseAdapter<VoteDetailEntity>{

        public VoteDetailAdapter(List<VoteDetailEntity> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_vote_detail, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            VoteDetailEntity entity = getItem(position);
            ImageDisplayTools.displayHeadImage(entity.userData.getImgUrl(), viewHolder.headerImage);
            if(mAwardsUserId != -1
                    && entity.userData.getId().equals(String.valueOf(mAwardsUserId))){
                viewHolder.winnerFlagImage.setVisibility(View.VISIBLE);
            }else{
                viewHolder.winnerFlagImage.setVisibility(View.GONE);
            }
            viewHolder.userNameTextView.setText(entity.userData.getNickName());
            viewHolder.voteTimeTextView.setText(DateFormatUtil.formatTime(
                    new Date(entity.createTime), DateFormatUtil.FORMAT_TIME));
            return convertView;
        }

        class ViewHolder{
            ImageView headerImage;
            TextView userNameTextView;
            TextView voteTimeTextView;
            ImageView winnerFlagImage;

            public ViewHolder(View convertView){
                headerImage = (ImageView) convertView.findViewById(R.id.iv_header_img);
                userNameTextView = (TextView) convertView.findViewById(R.id.tv_name);
                voteTimeTextView = (TextView) convertView.findViewById(R.id.tv_vote_time);
                winnerFlagImage = (ImageView) convertView.findViewById(R.id.iv_adward_flag);
            }
        }
    }
}
