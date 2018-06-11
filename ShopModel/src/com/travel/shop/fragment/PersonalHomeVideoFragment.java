package com.travel.shop.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Constants;
import com.travel.VideoConstant;
import com.travel.bean.VideoInfoBean;
import com.travel.imserver.ResultCallback2;
import com.travel.layout.DialogTemplet;
import com.travel.layout.MyGridView;
import com.travel.lib.fragment_interface.NoFunctionException;
import com.travel.lib.ui.BaseFragment;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.adapter.PersonalHomeVideoAdapter;
import com.travel.shop.http.LiveInfoHttp;
import com.travel.shop.tools.ShopTool;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/10.
 */

public class PersonalHomeVideoFragment extends BaseFragment {
    public final static String STOP_REFRESH = PersonalHomeVideoFragment.class.getSimpleName() + "WFNR";
    private Context mContext;
    private View rootView;
    private TextView noneNotify;
    private MyGridView gridView;
    private PersonalHomeVideoAdapter videoAdapter;
    private ArrayList<VideoInfoBean> videoList;
    private String userId = "";
    private int times = 1;

    private DialogTemplet deleteDialog;

    private boolean isLongClick = false;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        videoList = new ArrayList<>();
        rootView = inflater.inflate(R.layout.gridview_layout, null);
        noneNotify = (TextView) rootView.findViewById(R.id.noneNotify);
        gridView = (MyGridView) rootView.findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(videoList.size() == 0) return;
                if(isLongClick)
                    return;
                ShopTool.play(videoList.get(position), mContext, 0);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if (TravelUtil.isHomePager(userId)) {
                    isLongClick = true;
                    if(deleteDialog == null)
                        deleteDialog = new DialogTemplet(getContext(), false, "是否删除该视频？", "", "否", "是");
                    deleteDialog.show();

                    deleteDialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {
                        @Override
                        public void leftClick(View view) {

                        }
                    });

                    deleteDialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {
                        @Override
                        public void rightClick(View view) {
                            deleteVideo(position);
                        }
                    });
                }
                return true;
            }
        });

        videoAdapter = new PersonalHomeVideoAdapter(getContext(), videoList, "");
        gridView.setAdapter(videoAdapter);

        times = 1;
        videosInfoData();
        return rootView;
    }

    private void stopRefresh(List<VideoInfoBean> obj){
        try {
            functions.invokeFunc(STOP_REFRESH, obj);
        } catch (NoFunctionException e) {
            e.printStackTrace();
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void refresh() {
        times = 1;
        videoList.clear();
        videosInfoData();
    }

    public void load() {
        pullUpToRefresh();
    }

    private void pullUpToRefresh() {
        if (videoList != null && videoList.size() > 0 && videoList.size() % Constants.ItemNum == 0) {
            times = times + 1;
            videosInfoData();
        } else if (videoList != null && videoList.size() > 0 && times != 1) {
            Toast.makeText(getContext(), R.string.no_more, Toast.LENGTH_SHORT).show();
            stopRefresh(null);
        } else if (videoList != null && videoList.size() == 0) {
            times = 1;
            videosInfoData();
        } else {
            stopRefresh(null);
        }
        if (stopLoadListener != null) {
            stopLoadListener.stopLoad(videoList);
        }

    }

    private void videosInfoData() {
        LiveInfoHttp.getPersonalVideoList(mContext, times, userId, new ResultCallback2<List<VideoInfoBean>>() {
            @Override
            public void onError(int errorCode, String errorReason) {
                stopRefresh(null);
            }

            @Override
            public void onResult(@NonNull List<VideoInfoBean> obj) {
                if (videoList.size() > 0 && obj.size() == 0) {
                    Toast.makeText(getContext(), R.string.no_more, Toast.LENGTH_SHORT).show();
                } else {
                    int listSize = videoList.size() % Constants.ItemNum;
                    if (listSize > 0) {
                        for (int i = 0; i < listSize; i++) {
                            if (videoList.size() > 0)
                                videoList.remove(videoList.size() - 1);
                        }
                    }
                    videoList.addAll(obj);
                    videoAdapter.notifyDataSetChanged();
                }

                stopRefresh(null);
                stopRefresh(obj);

                isShowNoneNotify();
            }
        });
    }

    /**
     * 删除视频
     */
    private void deleteVideo(final int position) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", videoList.get(position).getVideoId());
        NetWorkUtil.postForm(getContext(), VideoConstant.VIDEO_DELETE, new MResponseListener(getContext()) {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0){
                    videoList.remove(position);
                    isShowNoneNotify();
                    videoAdapter.notifyDataSetChanged();
                }
            }
        }, map);
    }

    private void isShowNoneNotify() {
        if (videoList.size() < 1)
            noneNotify.setVisibility(View.VISIBLE);
        else
            noneNotify.setVisibility(View.GONE);
    }

}
