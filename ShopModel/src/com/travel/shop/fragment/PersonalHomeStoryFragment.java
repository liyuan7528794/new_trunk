package com.travel.shop.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.adapter.DividerItemDecoration;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.http_helper.StoryListHttpHelper;
import com.travel.layout.ScrollGridLayoutManager;
import com.travel.lib.TravelApp;
import com.travel.lib.fragment_interface.NoFunctionException;
import com.travel.lib.ui.BaseFragment;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;
import com.travel.shop.activity.GoodsInfoActivity;
import com.travel.shop.adapter.PersonalHomeStoryAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/10.
 */
public class PersonalHomeStoryFragment extends BaseFragment {
    public final static String STOP_REFRESH = PersonalHomeStoryFragment.class.getSimpleName() + "WFNR";
    private View rootView;
    private TextView noneNotify;
    private RecyclerView recyclerView;
    private PersonalHomeStoryAdapter adapter;
    private ArrayList<GoodsBasicInfoBean> goods;
    private String userId = UserSharedPreference.getUserId();
    private int times = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.personal_home_story_layout, null);
        goods = new ArrayList<>();
        noneNotify = (TextView) rootView.findViewById(R.id.noneNotify);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.story_recyclerview);
        recyclerView.setLayoutManager(new ScrollGridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.BOTH_SET, OSUtil.dp2px(getContext(), 15), ContextCompat.getColor(getContext(), android.R.color.transparent)));
        adapter = new PersonalHomeStoryAdapter(goods, getContext());
        recyclerView.setAdapter(adapter);
        adapter.setmOnItemClickListener(new PersonalHomeStoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                GoodsInfoActivity.actionStart(getContext(), goods.get(position).getStoryId(), 1);
            }
        });
        outData();
        return rootView;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void refresh() {
        times = 1;
        goods.clear();
        outData();
    }

    public void load() {
        ++times;
        outData();
    }

    private void outData() {
        StoryListHttpHelper.getStoriesList(getActivity(), 3, userId, times, mCityInfoListener);
    }

    private void stopRefresh(List<GoodsBasicInfoBean> obj) {
        try {
            functions.invokeFunc(STOP_REFRESH, obj);
        } catch (NoFunctionException e) {
            e.printStackTrace();
        }
    }

    StoryListHttpHelper.OutStoriesHttpListener mCityInfoListener = new StoryListHttpHelper.OutStoriesHttpListener() {

        @Override
        public void getStoriesList(ArrayList<GoodsBasicInfoBean> goodsList, int flag) {
            stopRefresh(goodsList);
            if (stopLoadListener != null) {
                stopLoadListener.stopLoad(goodsList);
            }
            if (goodsList.size() == 0 && times != 1) {
                --times;
                Toast.makeText(TravelApp.appContext, getContext().getResources().getString(R.string.no_more), Toast.LENGTH_SHORT);
            } else {
                goods.addAll(goodsList);
                adapter.notifyDataSetChanged();
            }


            if (goods.size() == 0) {
                noneNotify.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noneNotify.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    };

}
