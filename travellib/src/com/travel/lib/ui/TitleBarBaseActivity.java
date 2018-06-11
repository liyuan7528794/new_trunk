package com.travel.lib.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.travel.Constants;
import com.travel.bean.UDPSendInfoBean;
import com.travel.lib.R;
import com.travel.lib.fragment_interface.Functions;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.TravelUtil;

import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * BaseActivity
 * 添加了标题栏与断网通知
 */
public class TitleBarBaseActivity extends BaseActivity {
    private static final String TAG = "TitleBarBaseActivity";

    protected View layout_app_title;
    protected RelativeLayout titleLayout;
    protected View titleLine;
    protected Button rightButton;
    protected TextView titleText;
    protected ImageView iv_whole_background;
    protected RelativeLayout leftButton;
    protected ImageView group_chat;
    protected FrameLayout frameLayout;
    protected View netNotifyLayout;
    protected RelativeLayout contentLayout;
    protected View no_data;
    protected TextView tv_no_order;

    private static DatagramSocket socket;
    private static InetAddress serverAddress;

    // 主题切换相关
    private SharedPreferences sp;
    // 阴影相关
    protected LinearLayout ll_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + getClass().getSimpleName());

        if (socket == null)
            try {
                socket = new DatagramSocket(Constants.PORT);
                serverAddress = InetAddress.getByName("114.55.130.51");
                //			serverAddress = InetAddress.getByName(Constants.Chat_Ip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        sp = getSharedPreferences("appTheme", Context.MODE_PRIVATE);
    }

    public void onCreate() {
        if (sp.getInt("theme", 0) == 0) {// 白天
            setTheme(R.style.DayTheme);
        } else if (sp.getInt("theme", 0) == 1) {// 黑天
            setTheme(R.style.NightTheme);
        }
        super.setContentView(R.layout.activity_base);
        initView();
        initListener();
        initFunctions();
    }

    private void initListener() {
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected void hideOriginTitleLayout() {
        ll_title.setVisibility(View.GONE);
        titleLine.setVisibility(View.GONE);
    }

    protected void addNewTitleLayout(View view) {
        hideOriginTitleLayout();
        frameLayout.setVisibility(View.VISIBLE);
        frameLayout.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private void initView() {
        layout_app_title = findViewById(R.id.layout_app_title);
        ll_title = (LinearLayout) layout_app_title.findViewById(R.id.ll_title);
        titleLayout = (RelativeLayout) layout_app_title.findViewById(R.id.titleLayout);
        titleLine = layout_app_title.findViewById(R.id.views);
        leftButton = (RelativeLayout) layout_app_title.findViewById(R.id.leftButton);
        rightButton = (Button) layout_app_title.findViewById(R.id.rightButton);
        titleText = (TextView) layout_app_title.findViewById(R.id.tabTitle);
        group_chat = (ImageView) layout_app_title.findViewById(R.id.group_chat);
        iv_whole_background = findView(R.id.iv_whole_background);
        frameLayout = findView(R.id.frame_layout);
        netNotifyLayout = findView(R.id.netNotify);
        contentLayout = findView(R.id.contentLayout);
        no_data = findView(R.id.no_data);
        tv_no_order = (TextView) no_data.findViewById(R.id.tv_no_data);
        try {
            Method method = View.class.getMethod("setTranslationZ", float.class);
            method.invoke(titleLayout, 10f);
        } catch (Exception e) {
            titleLine.setVisibility(View.VISIBLE);
            MLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        titleText.setText(title);
    }

    @Override
    public void setContentView(int layoutResID) {
        onCreate();
        getLayoutInflater().inflate(layoutResID, contentLayout, true);
    }

    @Override
    protected void netNotifyShow() {
        netNotifyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void netNotifyHide() {
        netNotifyLayout.setVisibility(View.GONE);
    }

    @Override
    protected void addFunction(Functions functions) {

    }

    public void showNoDataView(String notify) {
        contentLayout.setVisibility(View.GONE);
        no_data.setVisibility(View.VISIBLE);
        tv_no_order.setText(notify);
    }

    public void hideNoDataView() {
        contentLayout.setVisibility(View.VISIBLE);
        no_data.setVisibility(View.GONE);
    }

    public void sendData(UDPSendInfoBean bean) {
        final String sendData = TravelUtil.objectToJson(bean);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte data[] = sendData.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, Constants.PORT);
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
