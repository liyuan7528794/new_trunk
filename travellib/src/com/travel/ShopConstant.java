package com.travel;

import android.os.Environment;

import static com.travel.Constants.Root_Url;
import static com.travel.Constants.Root_Url_ShareStory;

/**
 * 第二个选项卡中的网址
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/02
 */
public class ShopConstant {

    /******************** 常量 *******************/
    // SDCard路径
    public static final String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    // 压缩后的路径
    public static final String SMALL_IMAGE_CACHE = SD_PATH + "/cache/compass/";
    // 修改关注的状态
    public static final String IS_ATTENTION = "IS_ATTENTION";
    // 刷新页面的状态
    public static final String REFRESH_GOODSINFO = "REFRESH_GOODSINFO";
    // 登录的Action
    public static final String LOG_IN_ACTION = "com.travel.login";
    // 聊天的Action
    public static final String COMMUNICATION_ACTION = "com.travel.communication.activity.ChatActivity";
    // 修改人民币数的Action
    public static final String MONEY_ALERT = "MONEY_ALERT";
    // 众投结果历经天数
    public static final int PUBLIC_VOTE_DAY = 3;
    // 最多输入的红币数
    public static final int MAX_RED_MONEY = 100000;
    // 评价最多上传图片的张数
    public static final int PHOTO_MAX = 9;

    /******************** 接口 *******************/
    // 弹窗
    public static final String APP_POP = Root_Url + "/foundation/appStart.do?";
    // 优惠券是否领取
    public static final String COUPON_IS_GOT = Root_Url + "/user/userIsGet.do?";
    // 出发页
    public static final String OUT_GOODS = Root_Url + "/goods/goodsIndex.do?";
    // 首页中的故事和更多故事的接口
    public static final String MORE_STORY = Root_Url + "/story/storyList.do?";
    // 所有的结构，参数同上，只是比上面多了乱序的功能
    public static final String MORE_STORY_NO_SORT = Root_Url + "/story/storyPushList.do?";
    // 城市页
    public static final String CITY_INFO = Root_Url + "/city/cityDetail.do?";
    // 城市天气
    public static final String CITY_WEATHER_INFO = Root_Url + "/city/cityWeather.do?";
    // 城市页的故事列表
    public static final String CITY_STORY_LIST = Root_Url + "/story/storyList.do?";
    // 城市页的故事列表
    public static final String BOX_ROOM_LIST = Root_Url + "/story/storyFollowList.do?";
    // 故事详情页--此故事是否被收藏
    public static final String STORY_ISATTENTION = Root_Url + "/story/storyIsFollow.do?";
    // 故事详情页--故事收藏
    public static final String STORY_ATTENTION = Root_Url + "/story/storyFollow.do?";
    // 故事详情页--故事分享
    public static final String STORY_SHARE = Root_Url_ShareStory + "/webapp/story/storyDetail.do?storyId=";
    // 故事详情页
    public static final String STORY_INFO = Root_Url + "/story/storyDetail.do?";
    // 故事评论
    public static final String STORY_COMMENT = Root_Url + "/story/storyCommentList.do?";
    // 故事评论点赞
    public static final String STORY_COMMENT_LIKE = Root_Url + "/story/storyCommentPraise.do?";
    // 发表评论
    public static final String SEND_COMMENT = Root_Url + "/story/storyComment.do?";
    // 商品详情页
    public static final String GOODS_INFO = Root_Url + "/goods/goodsDetails.do?";
    // 商品详情页中视频信息
    public static final String VIDEOS_INFO = Root_Url + "/live/videoList.do?";
    // 商品详情页中关注
    public static final String ATTENTION = Root_Url + "/user/userFollow.do?";
    // 商品详情页中评价信息
    public static final String EVALUATE_INFO = Root_Url + "/goods/goodsEvaluate.do?";
    // 提交订单页-->附加服务
    public static final String COMMIT_ORDER_ATTACH_GOODS = Root_Url + "/goods/goodsAttach.do?";
    // 提交订单页-->活动
    public static final String COMMIT_ORDER_ACTIVITY = Root_Url + "/notice/goodsNotice.do?";
    // 提交订单页-->公告
    public static final String COMMIT_ORDER_NOTICE = Root_Url + "/notice/noticeDetail.do?";
    // 提交订单页-->获取价格套餐
    public static final String COMMIT_ORDER_BY_PEOPLE_SET = Root_Url + "/goods/priceSet.do?";
    // 提交订单页-->获取相应套餐的日历
    public static final String COMMIT_ORDER_BY_PEOPLE = Root_Url + "/goods/priceDay.do?";
    // 提交订单页-->获取卡券数据
    public static final String COUPON_LIST = Root_Url + "/user/coupnoList.do?";
    // 提交订单
    public static final String COMMIT = Root_Url + "/orders/createOrders.do?";
    // 订单详情页--附加服务的获取
    public static final String ORDER_INFO_ATTACH_GOODS = Root_Url + "/orders/ordersAttachList.do?";
    // 订单详情页
    public static final String ORDER_INFO = Root_Url + "/orders/ordersDetails.do?";
    // 订单管理页
    public static final String ORDER_MANAGE = Root_Url + "/orders/ordersList.do?";
    // 订单管理页--供应商的单子
    public static final String ORDER_MANAGE_3 = Root_Url + "/orders/ordersSellerList.do?";
    // 订单管理页--扫码
    public static final String ORDER_MANAGE_SCAN = Root_Url + "/orders/scanOrders.do?";
    // 订单删除
    public static final String ORDER_DELETE = Root_Url + "/orders/dleOrders.do?";
    // 买家取消订单理由
    public static final String BUYER_CANCLE_ORDER_REASON = Root_Url + "/orders/ordersReason.do?";
    // 支付0元时
    public static final String PAY_ZERO = Root_Url + "/orders/ordersSuccess.do?";
    // 签名
    public static final String SIGN = Root_Url + "/user/pay/sign.do";
    // 支付宝支付
    public static final String NOTIFY_PAY = Constants.Root_Url_Alipay + "/pay/notifyUrlPayMsg.do";
    public static final String RETURN_PAY = Constants.Root_Url_Alipay + "/pay/returnUrlPayMsg.do";
    // 微信支付
    public static final String WECHAT_PAY = Constants.Root_Url_Alipay + "/xblx/user/pay/weixinPay.do?";
    // 招行支付
    public static final String ZHAOHANG_PAY = Root_Url + "/zhaohangpay.do?";
    // 卖家修改价格
    public static final String SALLER_ALTER_PRICE = Root_Url + "/orders/changePrice.do?";
    // 卖家确认退款 买家再次同意/投诉此次退款
    public static final String SALLER_SURE_REFUND = Root_Url + "/orders/changeRefund.do?";
    // 卖家修改退款金额
    public static final String SALLER_CHANGE_REFUND = Root_Url + "/orders/changeRefundMoney.do?";
    // 退款详情（卖家点击同意退款后的退款信息的获取）
    public static final String REFUND_INFO = Root_Url + "/orders/refundDetail.do?";
    // 提交评价
    public static final String COMMIT_EVALUATE = Root_Url + "/orders/buyerEvaluate.do?";
    // 订单详情页中的评价
    public static final String ORDER_EVALUATE = Root_Url + "/orders/ordersEvaluate.do?";
    // 个人的详细信息
    public static final String PERSONAL_INFO = Root_Url + "/userBasicOnLine.do?";
    // 他人主页
    public static final String OTHER_PAGE = Root_Url + "/friendInfo.do?";
    // 我的主页
    public static final String MY_PAGE = Root_Url + "/user/userInfo.do?";
    // 我的主页修改背景图片
    public static final String MY_PAGE_BACKGROUD = Root_Url + "/upload/userBackImg.do?";
    // 意见反馈
    public static final String MY_PAGE_FEEDBACK = Root_Url + "/foundation/feedback.do?";
    // 我的红币
    public static final String LIVE_INCOME = Root_Url + "/gift/myRedCoinTotal.do?";
    // 我的红币-→ 明细
    public static final String LIVE_INCOME_LIST = Root_Url + "/gift/queryMyRedCoinBill.do?";
    // 我的红币-→ 充值
    public static final String LIVE_INCOME_RECHARGE = Root_Url + "/gift/reChargeGoodsList.do?";
    // 我的红币-→ 充值 -→ 获取订单号
    public static final String LIVE_INCOME_ORDERSID = Root_Url + "/gift/addReChargeApply.do?";
    // 我的红币-→ 提现（获取平台提成比例）
    public static final String LIVE_INCOME_RATION = Root_Url + "/gift/deductRation.do?";
    // 我的红币-→ 提现 -→ 立即提现
    public static final String LIVE_INCOME_WITHDRAW_CASH = Root_Url + "/gift/addCashedApply.do?";
    // 电商收入
    public static final String BUSINESS_INCOME = Root_Url + "/user/pay/getAccountNum.do?";
    // 电商收入-→ 明细
    public static final String BUSINESS_INCOME_LIST = Root_Url + "/user/pay/queryMyBill.do?";
    // 电商收入-→ 提现申请
    public static final String BUSINESS_INCOME_WITHDRAW_CASH = Root_Url + "/user/pay/addCashedApply.do?";
    // 众投列表
    public static final String PUBLIC_VOTE_LIST = Root_Url + "/publicVote/publicVoteList.do?";
    // 我的众投
    public static final String PUBLIC_VOTE_LIST_MINE = Root_Url + "/publicVote/onselfPublicVote.do?";
    // 申请众投
    public static final String APPLY_PUBLIC_VOTE = Root_Url + "/orders/subPublicVote.do?";
    // 个人主页 ---> 播放数量的获取
    public static final String PERSONAL_VIDEO_COUNT = Root_Url + "/live/userLiveNum.do?";
    // 个人主页 ---> 关注数量的获取粉丝数
    public static final String PERSONAL_FOLLOWER_COUNT = Root_Url + "/follow/followMyNum.do?";
    // 个人主页 ---> 我关注的人的数量
    public static final String PERSONAL_FOLLOW_COUNT = Root_Url + "/follow/myFollowNum.do?";
    // 激活页获取验证码
    public static final String ACTIVATE_VERIFICATION_CODE = Root_Url + "/user/cardSendCheck.do?";
    // 激活
    public static final String ACTIVATE = Root_Url + "/user/cardCheck.do?";
    // 首页城市列表
    public static final String OUT_CITY_LIST = Root_Url + "/city/typeAndCity.do?";
    // 新版（抖音版）首页城市列表
    public static final String DISCOVER_CITY_LIST = Root_Url + "/city/typeAndCity2.do?";
    // 上传图片
    public static final String UPLOAD_IMAGE = Root_Url + "/upload/imgUpload.do?";
    // 头像上传
    public static final String HEAD_UPLOAD = Root_Url + "/upload/userImgUpload.do?";
    // 是否显示直播按钮
    public static final String IS_SHOW_LIVE_BTN = Root_Url + "/data/dictionary.do?";
    // 提交订单时是否显示弹窗
    public static final String IS_SHOW_COMMIT_DIALOG = Root_Url + "/showctrl/SubmitOrderPrompt.do?";
    // 城市投票
    public static final String CITY_VOTE = Root_Url + "/user/city/addVote.do?";
    // 千城大趴发放优惠券
    public static final String THOUSAND_CITY_COUPON = Root_Url + "/user/sendCoupnos.do?";
    // 获取行程列表
    public static final String PLANS = Root_Url + "/user/journey.do?";
    // 行程详情
    public static final String PLAN_INFO = Root_Url + "/user/journeyInfo.do?"; //行程详情（通过行程id）
    public static final String PLAN_INFO_ORDER = Root_Url + "/user/ordersJourneyInfo.do?"; //行程详情（通过订单id）

    /*-------------------------新版接口----------------------*/
    // 商品分享
    public static final String GOODS_SHARE = Root_Url_ShareStory + "/html/goodsShare.html?goodsId=";
    // 是否购买过小城卡
    public static final String IS_BUY_CARD = Root_Url + "/card/cityCardDetail.do";
    // 小城卡id
    public static final String CARD_ID = Root_Url + "/goods/storyCardGoods.do";
    // 获取商品支持优惠券
    public static final String GOODS_SUPPORT_COUPON = Root_Url + "/goods/goodsCoupnoList.do";
    // 是否领过优惠券
    public static final String IS_GET_COUPON = Root_Url + "/coupon/userIsGet.do";
    // 领取优惠券
    public static final String GET_COUPON = Root_Url + "/user/userGetCoupno.do";
    // 1：跟团游 2：自由行 5：门票的提交订单
    public static final String SINGLE_COMMIT_ORDER = Root_Url + "/orders/createFreeOrders.do";
    // 3: 酒店 4：向导的提交订单
    public static final String MULTIPLE_COMMIT_ORDER = Root_Url + "/orders/createHotelOrders.do";
    // 6: 小城卡或其他类型的提交订单
    public static final String CARD_COMMIT_ORDER = Root_Url + "/orders/createOtherOrders.do";
    // 卖家接受订单
    public static final String SELLER_RECEIVE_ORDER = Root_Url + "/orders/sellerAffirm.do";
    // 买家满意付款
    public static final String BUYER_PAY = Root_Url + "/orders/buyerAffirm.do";
    // 买家取消订单
    public static final String BUYER_CANCEL_ORDER = Root_Url + "/orders/buyerCancel.do";
    // 卖家拒绝订单
    public static final String SELLER_REFUSE = Root_Url + "/orders/rejectOrders.do?";
    // 买家申请退款
    public static final String BUYER_APPLICATION_REFUND = Root_Url + "/orders/createRefund.do?";

    // 出行人信息列表
    public static final String TOURISTS = Root_Url + "/user/userAddressList.do";
    // 添加出行人
    public static final String ADD_TOURIST = Root_Url + "/user/createUserAddress.do";
    // 修改出行人信息
    public static final String UPDATE_TOURIST = Root_Url + "/user/updateUserAddress.do";
    // 修改出行人信息
    public static final String DELETE_TOURIST = Root_Url + "/user/delUserAddress.do";
    // 修改出行人信息
    public static final String GET_STORY_CARD_ID = Root_Url + "/goods/storyCardGoods.do";
    // 主页数据
    public static final String GET_HOME_PAGE_DATA = Root_Url + "/home/homePage.do";

    /*---------------cctv视频相关接口-----------------*/
    // 全部cctv视频
    public static final String CCTV_ALL_VIDEO = Root_Url + "/cctv/cctvVideo.do";
    // cctv短视频
    public static final String CCTV_SMALL_VIDEO = Root_Url + "/cctv/cctvShortVideo.do";
    // cctv视频分类
    public static final String CCTV_VIDEO_TYPE = Root_Url + "/cctv/cctvType.do";
    // cctv视频分享
    public static final String CCTV_VIDEO_SHARE = Root_Url + "/cctv/share.do?cityVideoId=";
    // cctv视频是否点赞
    public static final String CCTV_VIDEO_LIKE_GET = Root_Url + "/user/shortVideoIsVote.do";
    // cctv视频点赞操作
    public static final String CCTV_VIDEO_LIKE_CLICK = Root_Url + "/user/shortVideoVote.do";
    // cctv视频点赞数
    public static final String CCTV_VIDEO_LIKE_COUNT = Root_Url + "/cctv/voteCount.do";
    // cctv视频评论列表
    public static final String CCTV_VIDEO_EVALUATE = Root_Url + "/cctv/shortVideoCommentList.do";
    // cctv视频评论个数
    public static final String CCTV_VIDEO_EVALUATE_COUNT = Root_Url + "/cctv/shortVideoCommentCount.do";
    // cctv视频评论
    public static final String CCTV_VIDEO_EVALUATE_SEND = Root_Url + "/user/shortVideoComment.do";

    // 众投投票详情
    public static final String VOTE_STATUS = Root_Url + "/orders/voteDetail.do";

    // 仿抖音版首页数据
    public static final String VIDEO_DATA = Root_Url + "/city/videoAndCity.do";
    // 仿抖音版关注
    public static final String FOLLOW_CONTROL = Root_Url + "/user/userFollow.do";
    // 仿抖音版是否关注
    public static final String ISFOLLOW_CONTROL = Root_Url + "/user/isFollow.do";

    // 说说点赞
    public static final String TALK_PRAISR_ADD = Root_Url + "/user/addTalkPraiseNum.do";
    // 评论列表
    public static final String COMMMENTS_LIST = Root_Url + "/comment/commentList.do";
    // 给说说添加评论
    public static final String TALK_COMMMENTS_ADD = Root_Url + "/user/AddComment.do";
    // 说说列表
    public static final String TALK_LIST = Root_Url + "/talk/talkList.do";
    // 添加说说
    public static final String TALK_ADD = Root_Url + "/user/Addtalk.do";
}
