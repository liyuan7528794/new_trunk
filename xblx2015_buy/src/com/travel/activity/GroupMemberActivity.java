package com.travel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.tencent.TIMGroupManager;
import com.tencent.TIMGroupMemberInfo;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.qcloud.suixinbo.presenters.AbstractEnterLiveHelper;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.communication.entity.UserData;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.tools.ShopTool;
import com.travel.utils.HLLXLoginHelper;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.connect.common.Constants.ACTIVITY_OK;

public class GroupMemberActivity extends TitleBarBaseActivity {
    private static final String TAG = "GroupMemberActivity";

    private GridView mGridView;

    private GridViewAdapter mAdapter;
    private List<UserData> mGroupMemberLists = new ArrayList<>();

    private String mRoomNum;
    public static final String ROOM_NUM = "room_num";

    public static void startActivity(Context context, String roomNum) {
        Intent intent = new Intent(context, GroupMemberActivity.class);
        intent.putExtra(ROOM_NUM, roomNum);
        context.startActivity(intent);
    }

    public static void startActivityForResult(Activity context, int requestCode, String roomNum) {
        Intent intent = new Intent(context, GroupMemberActivity.class);
        intent.putExtra(ROOM_NUM, roomNum);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIntentData();
        initView();
        initTitleLayout();
        getGroupMemberInfo();
    }

    private void getGroupMemberInfo() {
        TIMGroupManager.getInstance().getGroupMembers(mRoomNum, new TIMValueCallBack<List<TIMGroupMemberInfo>>() {
            @Override
            public void onError(int i, String s) {
                MLog.e(TAG, "获取群成员列表出错: " + s + i);
            }

            @Override
            public void onSuccess(List<TIMGroupMemberInfo> timGroupMemberInfos) {
                List<String> userIds = new ArrayList<String>();
                for (TIMGroupMemberInfo info : timGroupMemberInfos) {
                    userIds.add(info.getUser());
                }

                AbstractEnterLiveHelper.getUsersProfile(userIds, new TIMValueCallBack<List<TIMUserProfile>>() {
                    @Override
                    public void onError(int i, String s) {
                        MLog.e(TAG, "获取群内成员信息失败: " + s);
                    }

                    @Override
                    public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                        for (TIMUserProfile profile : timUserProfiles) {
                            UserData userData = new UserData();
                            userData.setId(profile.getIdentifier());
                            userData.setImgUrl(profile.getFaceUrl());
                            userData.setNickName(profile.getNickName());
                            mGroupMemberLists.add(userData);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initView() {
        setContentView(R.layout.activity_group_member);
        mGridView = findView(R.id.gv_group_member);
        mAdapter = new GridViewAdapter(mGroupMemberLists);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mGroupMemberLists == null)
                    return;
                UserData userData = mGroupMemberLists.get(position);
                PopWindowUtils.followPopUpWindow(GroupMemberActivity.this,
                        userData.getId().substring(HLLXLoginHelper.PREFIX.length()), userData.getNickName(), userData.getImgUrl(), 1);
            }
        });
        findViewById(R.id.tv_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(ACTIVITY_OK);
                finish();
            }
        });
    }

    private void initIntentData() {
        mRoomNum = getIntent().getStringExtra(ROOM_NUM);
        if (TextUtils.isEmpty(mRoomNum)) {
            throw new IllegalArgumentException("mRoomNum is empty");
        }
    }

    // TODO:这里的代码明显有些重复，　考虑重构
    private void initTitleLayout() {
        setTitle("群聊设置");
    }

    private class GridViewAdapter extends ListBaseAdapter<UserData> {

        public GridViewAdapter(List<UserData> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.group_memeber_grid_item_layout, parent, false);
                viewHolder.headerImage = (ImageView) convertView.findViewById(R.id.iv_header);
                viewHolder.nameText = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            UserData userData = getItem(position);
            ImageDisplayTools.disPlayRoundDrawableHead(userData.getImgUrl(),
                    viewHolder.headerImage,
                    OSUtil.dp2px(GroupMemberActivity.this, 5));
            ShopTool.setLLParamsWidthPart(viewHolder.headerImage, 5, 120, 1, 1);
            if(!OSUtil.isDayTheme())
            viewHolder.headerImage.setColorFilter(TravelUtil.getColorFilter(parent.getContext()));
            viewHolder.nameText.setText(userData.getNickName());
            return convertView;
        }


        private class ViewHolder {
            ImageView headerImage;
            TextView nameText;
        }
    }
}
