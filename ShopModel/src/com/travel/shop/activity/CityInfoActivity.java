package com.travel.shop.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.ShopConstant;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.UDPSendInfoBean;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.FastBlurUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.adapter.CityGoodsAdapter;
import com.travel.shop.bean.CityBean;
import com.travel.shop.http.CityInfoHttp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 城市页---已废弃
 *
 * @author WYP
 * @version 1.0
 * @created 2017/01/10
 */
public class CityInfoActivity extends TitleBarBaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshAdapterView.OnListLoadListener {

    private Context mContext;
    // 刷新控件
    private SwipeRefreshRecyclerView mSwipeRefreshRecyclerView;
    private AdapterJoiner joiner;

    // 城市基本信息
    private ImageView cityImg;
    private TextView cityName, temperature, weather;
    private CityBean cityBean;
    // 城市商品
    private CityGoodsAdapter mCityGoodsAdapter;
    private ArrayList<GoodsBasicInfoBean> mList;

    // 网络相关
    private HashMap<String, String> map;
    private String id, name;// 城市Id和名字
    private int mPage = 1;

    public static void actionStart(Context context, String city, String id){
        Intent intent = new Intent(context, CityInfoActivity.class);
        intent.putExtra("city", city);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_swiperefresh_recyclerview);
        initData();
        initSwipeRefreshView();

        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection()))
            CityInfoHttp.getCityInfo(mContext, id, mCityInfoListener);
        initListener();
    }

    private void initSwipeRefreshView() {
        mSwipeRefreshRecyclerView = findView(R.id.srrv);
        mSwipeRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSwipeRefreshRecyclerView.setOnListLoadListener(this);
        mSwipeRefreshRecyclerView.setOnRefreshListener(this);

        joiner = new AdapterJoiner();
        joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(mContext, R.layout.activity_cityinfo, null);
                cityImg = (ImageView) view.findViewById(R.id.iv_cityinfo_img);
                cityName = (TextView) view.findViewById(R.id.tv_cityinfo_name);
                temperature = (TextView) view.findViewById(R.id.tv_cityinfo_temperature);
                weather = (TextView) view.findViewById(R.id.tv_cityinfo_weather);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(params);
                return view;
            }
        }));
        joiner.add(new JoinableAdapter(mCityGoodsAdapter));

        mSwipeRefreshRecyclerView.setAdapter(joiner.getAdapter());

    }

    private void initData() {
        mContext = this;
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("city");
        setTitle(name);
        group_chat.setVisibility(View.VISIBLE);

        map = new HashMap<>();
        ImageDisplayTools.initImageLoader(mContext);
        mList = new ArrayList<>();
        mCityGoodsAdapter = new CityGoodsAdapter(mList, mContext);
        cityBean = new CityBean();
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        mList.clear();
        CityInfoHttp.getCityInfo(mContext, id, mCityInfoListener);
        mSwipeRefreshRecyclerView.setRefreshing(false);
    }

    @Override
    public void onListLoad() {
        ++mPage;
        CityInfoHttp.getStoryList(mContext, "", name, mPage, mCityInfoListener);
        mSwipeRefreshRecyclerView.setLoading(false);
    }

    /**
     * 更新UI
     */
    CityInfoHttp.CityInfoListener mCityInfoListener = new CityInfoHttp.CityInfoListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void getCityInfo(CityBean mCityBean) {
            cityBean = mCityBean;
            CityInfoHttp.getCityWeatherInfo(mContext, id, name, this);
            CityInfoHttp.getStoryList(mContext, "", name, mPage, this);
            // 背景
            ImageDisplayTools.displayImage(mCityBean.getImgUrl(), cityImg);
            TravelUtil.setFLParamsWidthPart(cityImg, 1, 0, 125, 59);
            Bitmap blurImage = FastBlurUtil.blur(mContext, ImageDisplayTools.getBitMap(mCityBean.getImgUrl()));
            iv_whole_background.setBackground(ImageDisplayTools.bitmapToDrawable(blurImage));
            // 城市
            cityName.setText("-" + name + "-");
        }

        @Override
        public void getCityWeatherInfo(CityBean mCityBean) {
            // 温度
            temperature.setText(mCityBean.getTemperature() + "℃");
            // 天气
            weather.setText(mCityBean.getWeather());
        }

        @Override
        public void getCityStoryList(ArrayList<GoodsBasicInfoBean> mList) {
            // 拉取没有更多数据了
            if (mList.size() == 0 && mPage != 1) {
                --mPage;
                showToast(R.string.no_more);
                mSwipeRefreshRecyclerView.setEnabledLoad(false);
            } else {
                CityInfoActivity.this.mList.addAll(mList);
                mCityGoodsAdapter.notifyDataSetChanged();
                mSwipeRefreshRecyclerView.setEnabledLoad(true);
            }
        }
    };

    private void initListener() {
        group_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterGroupChat(v);
            }
        });
        // 商品列表点击
        mCityGoodsAdapter.setmOnItemClickListener(new CityGoodsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String storyId) {
//                GoodsInfoActivity.actionStart(mContext, storyId);
            }
        });
    }

    /**
     * 进入群聊
     */
    public void enterGroupChat(View view) {
        final String groupId = cityBean.getGroupId();
        final String groupName = cityBean.getGroupName();
        final String groupFaceUrl = cityBean.getGroupImageUrl();
        if (TextUtils.isEmpty(groupId)) {
            showToast("非法操作（群不存在）");
            return;
        }
        AlertDialogUtils.needLoginOperator(CityInfoActivity.this,
                new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ShopConstant.COMMUNICATION_ACTION);
                        intent.putExtra("id", groupId);
                        intent.putExtra("is_group_chat", true);
                        intent.putExtra("nick_name", groupName == null ? "" : groupName);
                        intent.putExtra("img_url", groupFaceUrl == null ? "" : groupFaceUrl);
                        startActivity(intent);
                    }
                });
    }
    String beginTime = "";
    @Override
    protected void onStart() {
        super.onStart();
        beginTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
    }

    String endTime = "";
    @Override
    protected void onStop() {
        super.onStop();
        endTime = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        UDPSendInfoBean bean = new UDPSendInfoBean();
        bean.getData("005_" + id, name,
                ShopConstant.CITY_INFO + "cityId=" + id, beginTime, endTime);
        sendData(bean);
    }

}
