package com.travel.usercenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.ShopConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.fragment.VoteListFragment;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.activity.ManagerOrderActivity;
import com.travel.shop.fragment.PersonalHomeStoryFragment;
import com.travel.usercenter.adapter.SellerControlAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 商家后台管理
 */
public class SellerControlActivity extends TitleBarBaseActivity {

    private Context mContext;

    private TextView tv_money_control;
    private RecyclerView rv_seller_control;
    private SellerControlAdapter mSellerControlAdapter;
    private ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_control);

        init();
        initListener();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getIncome();
        }
    }

    private void init() {
        mContext = this;

        tv_money_control = findView(R.id.tv_money_control);
        rv_seller_control = findView(R.id.rv_seller_control);
        initData();
        mSellerControlAdapter = new SellerControlAdapter(list, mContext);
        rv_seller_control.setLayoutManager(new GridLayoutManager(mContext, 3));
        rv_seller_control.setAdapter(mSellerControlAdapter);

        setTitle("商家后台管理");
        recceiver = new MReceiver();
        registerReceiver(recceiver, new IntentFilter(ShopConstant.MONEY_ALERT));
    }

    private void initData() {
        list = new ArrayList<>();
        HashMap<String, String> data1 = new HashMap<>();
        data1.put("picture", R.drawable.icon_order_control + "");
        data1.put("title", "订单");
        list.add(data1);
        HashMap<String, String> data2 = new HashMap<>();
        data2.put("picture", R.drawable.icon_vote_control + "");
        data2.put("title", "众投管理");
        list.add(data2);
        HashMap<String, String> data3 = new HashMap<>();
        data3.put("picture", R.drawable.icon_supplier_control + "");
        data3.put("title", "供应商订单");
        list.add(data3);
        HashMap<String, String> data4 = new HashMap<>();
        data4.put("picture", R.drawable.icon_story_control + "");
        data4.put("title", "故事");
        list.add(data4);
    }

    private void initListener() {
        tv_money_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, BusinessIncomeActivity.class));
            }
        });
        mSellerControlAdapter.setmOnItemClickListener(new SellerControlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder) {
                int position = holder.getAdapterPosition();
                Intent intent = new Intent();
                switch (position) {
                    case 0:// 订单
                        ManagerOrderActivity.actionStart(mContext, "business");
                        break;
                    case 1:// 众投管理
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(VoteListFragment.TYPE, "business");
                        OneFragmentActivity.startNewActivity(mContext, "", VoteListFragment.class, bundle1);
                        break;
                    case 2:// 供应商订单
                        ManagerOrderActivity.actionStart(mContext, "supplier");
                        break;
                    case 3:// 故事
                        OneFragmentActivity.startNewActivity(mContext, "故事", PersonalHomeStoryFragment.class, new Bundle());
                        break;
                }
                if (intent.getComponent() != null) {
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 获取收入
     */
    private void getIncome() {
        NetWorkUtil.postForm(mContext, ShopConstant.BUSINESS_INCOME, new MResponseListener(mContext) {

            @Override
            protected void onDataFine(JSONObject data) {
                tv_money_control.setText("￥" + data.optDouble("total") + "");
            }
        }, new HashMap<String, Object>());
    }

    private MReceiver recceiver;

    /**
     * 接收到修改人民币数的广播
     */
    class MReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getIncome();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(recceiver);
    }
}
