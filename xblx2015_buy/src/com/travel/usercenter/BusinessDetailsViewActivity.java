package com.travel.usercenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.ShopConstant;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.shop.tools.ShopTool;
import com.travel.usercenter.adapter.DetailsViewAdapter;
import com.travel.usercenter.entity.DetailsViewBean;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 人民币的明细
 *
 * @author WYP
 * @version 1.0
 * @created 2016/06/14
 */
public class BusinessDetailsViewActivity extends TitleBarBaseActivity {

    private Context mContext;

    private TextView tv_details_no_data;
    private PullToRefreshListView ptrlv_details;
    private ListView lv_details;
    private ArrayList<DetailsViewBean> detailsList, newDetailsList;
    private DetailsViewAdapter mAdapter;

    // 网络获取相关
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailsview);

        init();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getDetails();
        }
    }

    /**
     * 控件以及数据初始化
     */
    private void init() {
        mContext = this;
        tv_details_no_data = findView(R.id.tv_details_no_data);
        ptrlv_details = findView(R.id.ptrlv_details);
        lv_details = ptrlv_details.getRefreshableView();
        lv_details.setHeaderDividersEnabled(false);
        lv_details.setFooterDividersEnabled(false);
        lv_details.setDivider(ContextCompat.getDrawable(mContext, R.color.gray_B2));
        lv_details.setDividerHeight(OSUtil.dp2px(mContext, 0.5f));

        setTitle(getString(R.string.liveincome_details));
        newDetailsList = new ArrayList<>();
        detailsList = new ArrayList<>();
        mAdapter = new DetailsViewAdapter(mContext, newDetailsList);
        lv_details.setAdapter(mAdapter);
        scrollViewInit();
    }

    /**
     * 刷新加载的设置
     */
    private void scrollViewInit() {
        PullToRefreshHelper ptrHelper = new PullToRefreshHelper(ptrlv_details);
        ptrHelper.initPullDownToRefreshView(null);
        ptrHelper.initPullUpToRefreshView(null);
        ptrlv_details.setMode(Mode.BOTH);
        ptrHelper.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPage = 1;
                getDetails();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++mPage;
                getDetails();
            }
        });
    }

    /**
     * 获取明细
     */
    private void getDetails() {
        detailsList.clear();
        HashMap<String, Object> map = new HashMap<>();
        map.put("times", mPage);
        NetWorkUtil.postForm(mContext, ShopConstant.BUSINESS_INCOME_LIST, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if (mPage == 1) {
                    newDetailsList.clear();
                }
                try {
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dataObject = data.getJSONObject(i);
                        DetailsViewBean detail = new DetailsViewBean();
                        detail.setType(1);
                        // 明细类型
                        detail.setDetailsType(dataObject.optString("changeType"));
                        // 时间
                        detail.setTime(dataObject.optString("changeTime"));
                        // 钱数
                        detail.setMoney(dataObject.optString("changeNum"));
                        // 中间状态
                        detail.setStatus(dataObject.optString("status"));
                        // 描述
                        detail.setDesc(dataObject.optString("remark"));
                        detailsList.add(detail);
                    }
                    exchangeData();
                    if (mPage == 1 && data.length() == 0) {
                        ptrlv_details.setVisibility(View.GONE);
                        tv_details_no_data.setVisibility(View.VISIBLE);
                    } else {
                        ptrlv_details.setVisibility(View.VISIBLE);
                        tv_details_no_data.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    ptrlv_details.onRefreshComplete();
                    // 拉取时没有更多数据了
                    if (mPage != 1 && data.length() == 0) {
                        showToast(R.string.no_more);
                        ptrlv_details.setMode(Mode.PULL_FROM_START);
                    }else
                        ptrlv_details.setMode(Mode.BOTH);
                    mAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                // 再次联网之前，初始化当前页面的控件和数据
                if (mPage != 1) {
                    --mPage;
                }
                ptrlv_details.onRefreshComplete();
            }
        }, map);

    }

    /**
     * 将网络获取的数据转成带有“xxxx年xx月”的数据
     */
    private void exchangeData() {
        // 获取数据后对日期进行分类
        for (int i = 0; i < detailsList.size(); i++) {
            if (i == 0) {
                String nextPageFirstData = ShopTool.signChangeWord(detailsList.get(i).getTime()).substring(0, 8);
                String currentPageLastData = newDetailsList.size() == 0 ? ""
                        : ShopTool.signChangeWord(newDetailsList.get(newDetailsList.size() - 1).getTime()).substring(0,
                        8);
                // 头
                // 从第一页的数据的第一条提取日期添加到newDetailsList
                // 其他页的数据如果第一条的日期和前一页的最后一条的日期不同则添加到newDetailsList
                if (mPage == 1 || (mPage != 1 && !nextPageFirstData.equals(currentPageLastData))) {
                    DetailsViewBean detail = new DetailsViewBean();
                    detail.setType(0);
                    detail.setMonth(nextPageFirstData);
                    newDetailsList.add(detail);
                }
            } else {
                String previewData = ShopTool.signChangeWord(detailsList.get(i - 1).getTime()).substring(0, 8);
                String currentData = ShopTool.signChangeWord(detailsList.get(i).getTime()).substring(0, 8);
                // 头
                // 前一条的月份与当前的月份不相同
                if (!previewData.equals(currentData)) {
                    DetailsViewBean detail = new DetailsViewBean();
                    detail.setType(0);
                    detail.setMonth(ShopTool.signChangeWord(detailsList.get(i).getTime()).substring(0, 8));
                    newDetailsList.add(detail);
                }
            }
            // 明细
            newDetailsList.add(detailsList.get(i));
        }
    }

}
