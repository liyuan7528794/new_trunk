package com.travel.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.communication.entity.UserData;
import com.travel.communication.view.sortlistview.SortListView;
import com.travel.communication.view.sortlistview.SortModel;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.activity.PersonalHomeActivity;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注Fragment.
 * 此界面包含我的粉丝和我的关注,
 *
 * @author ldkxingzhe
 * @added: 2016-04-05
 */
public class MyFollowsFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = "MyFollowsActivity";
    private Activity activity;
    private int type = 1;

    // 关注的个人数据列表
    private String mUserId;
    private SortListView<UserData> mFollowsPersonListView;
    private List<SortModel<UserData>> mFollowsPersonList = new ArrayList<>();

    private View noneNotify;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("type"))
            type = bundle.getInt("type");
        View root = inflater.inflate(R.layout.fragment_my_follows_sort_list, container, false);
        mFollowsPersonListView = (SortListView<UserData>) root.findViewById(R.id.lv_follows_person);
        noneNotify = root.findViewById(R.id.none_notify);
        initAdapter();
        initListener();
        return root;
    }

    private void initListener() {
        mFollowsPersonListView.setOnItemClickListener(new SortListView.OnItemClickListener<UserData>() {
            @Override
            public void onItemClick(SortListView view, UserData object) {
                PersonalHomeActivity.actionStart(activity, false, object.getId(), object.getNickName());
            }
        });
    }

    private void initAdapter() {
        mFollowsPersonListView.setListData(mFollowsPersonList);
        mFollowsPersonListView.setAdapterListener(new SortListView.AdapterListener<UserData>() {
            @Override
            public View getView(SortModel<UserData> model, View convertView, ViewGroup viewParent) {
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView =
                            LayoutInflater.from(viewParent.getContext())
                                    .inflate(R.layout.follows_list_item, viewParent, false);
                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_header);
                    viewHolder.textView = (TextView) convertView.findViewById(R.id.tv_name);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                UserData realObject = model.getRealObject();
                ImageDisplayTools.displayHeadImage(realObject.getImgUrl(), viewHolder.imageView);
                if (!OSUtil.isDayTheme())
                    viewHolder.imageView.setColorFilter(TravelUtil.getColorFilter(getContext()));
                viewHolder.textView.setText(realObject.getNickName());
                return convertView;
            }
        });
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUserId = UserSharedPreference.getUserId();
        if (UserSharedPreference.isLogin()) {
            loadFollowUserList();
        }
    }

    // 获取关注人的列表
    // 我关注与关注我的
    private void loadFollowUserList() {
        String url = Constants.Root_Url + "/user/followUserList.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("myId", mUserId);
        map.put("type", type); // 1 -- 表示我关注的人 2 -- 表示关注我的人
        NetWorkUtil.postForm(getActivity(), url, new MResponseListener(getActivity()) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                MLog.v(TAG, "onFollowUserList data fine");
                mFollowsPersonList.clear();
                for (int i = 0; i < data.length(); i++) {
                    mFollowsPersonList.add(
                            new SortModel<UserData>(UserData.generateUserData(JsonUtil.getJSONObject(data, i))));
                }
                mFollowsPersonListView.setListData(mFollowsPersonList);
                if(mFollowsPersonList != null && mFollowsPersonList.size() > 0)
                    noneNotify.setVisibility(View.GONE);
                else
                    noneNotify.setVisibility(View.VISIBLE);
            }
        }, map);
    }

}
