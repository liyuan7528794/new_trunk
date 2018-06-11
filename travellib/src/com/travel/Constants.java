package com.travel;

import com.travel.lib.utils.MLog;

/**
 * 第二个选项卡中的网址
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/02
 */
public class Constants {


    //	 public static final String IpAddress="10.0.0.52";
    public static String Root_Url;
    public static String Chat_Ip;
    public static String OSS_ROOT_URL;
    public static String Root_Url_Alipay;
    public static String Root_Url_ShareStory;

    //	 public static final String VideoServiceCode="LOVFHBRS";
    public static final String VideoServiceCode = "XYEQXHDL";
    public static final int PORT = 8088;

    public static void init(String rootUrl, String chatIp, String ossRootUrl, String root_Url_ShareStory) {
        Root_Url = rootUrl;
//        Chat_Ip = "218.247.21.2";
        Chat_Ip = chatIp;
        OSS_ROOT_URL = ossRootUrl;
        Root_Url_ShareStory = root_Url_ShareStory;
        Root_Url_Alipay = "http://" + Chat_Ip;

        TOP_ACTIVITY = Root_Url + "/live/topActivity.do";
        DefaultHeadImg = OSS_ROOT_URL + "imgs/tourist.png";
        MLog.v("Constants", "rootUrl is %s, chatIp is %s", rootUrl, chatIp);
    }

    public static String DefaultHeadImg = OSS_ROOT_URL + "imgs/tourist.png";

    // APP_ID(微信支付)
    public static final String APP_ID = "wxc6bfb2deac95ed98";
    // 平台客服电话号码
    public static final String SERVICE_NUM = "01052663836";
    // 提现最大额度
    public static final int MAX_MONEY = 100;
    // 分页获取数据的页容量
    public static final int ItemNum = 10;

    /**
     * -----------------跳转页面的Action------------------
     */
    // 播放直播视频的Action
    public static final String LIVE_VIDEO_ACTION = "android.intent.action.VIEW";
    // 播放回放视频的Action
    public static final String VIDEO_ACTION = "com.travel.video.playback_video";
    // 播放视频的Tpye
    public static final String VIDEO_TYPE = "vnd.android.cursor.item/xxx";
    // 公告action
    public static final String NOTICE_ACTION = "com.travel.video.notice";
    // 公告action
    public static final String ACTIVITYS_ACTION = "com.travel.video.activitys";
    // 充值红币 Action
    public static final String Recharge_ACTION = "com.travel.usercenter.RechargeActivity";
    public static final String Recharge_TYPE = "vnd.android.cursor.item/xxx";
    // 商品详情 Action -->预定界面
    public static final String GOODS_ACTION = "com.travel.shop.activity.GoodsActivity";
    // finish充值页面的Action
    public static final String FINISH_RECHARGE = "FINISH_RECHARGE";
    // 修改红币数的Action
    public static final String REDMONEY_ALERT = "REDMONEY_ALERT";
    // 线路游支付的Action
    public static final String MANAGE_PAY = "MANAGE_PAY";
    // 登录
    public static String ACTION_LOGIN = "com.travel.login";
    // 立即使用进入的页面
    public static final String STORY_LIST_USE = "com.travel.activity.StoryHomeListActivity";
    // 支付成功跳转的页面
    public static final String ACTION_ORDER_SUCCESS = "com.travel.shop.activity.OrderSuccessActivity";
    // 支付失败跳转的页面
    public static final String ACTION_ORDER_FAIL = "com.travel.shop.activity.OrderInfoActivity";
    // 跳转到行程安排
    public static final String ACTION_ROUTE = "com.travel.map.RouteActivity";

    /**
     * ----------------公用的接口----------------
     **/
    // 获取顶部活动列表
    public static String TOP_ACTIVITY = Root_Url + "/live/topActivity.do";

    // 我的页面的下标
    public static int USERCENTER_POSITION = 2;

}
