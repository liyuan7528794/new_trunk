package com.travel;

public class VideoConstant {
	// 域名
	public static final String NET = Constants.Root_Url;

	public static final String MAP_KEY = "be74f4df09e74578df49a6a45f34ec8a";

	// // 直播主页，包括直播列表和当前轮滚图
	// public static final String LIVE_HOME = NET + "/live/liveList.do";

	// 通过hashid获取回放视频
	public static final String GET_VIDEO_URL = Constants.Root_Url + "/live/findVideoId.do";

	/**获取视频*/
	public static final String VIDEO_LIST = NET + "/live/videoList.do";
	public static final String HOME_VIDEO_LIST = NET + "/home/videoList.do";
	public static final String HOME_STORY_LIST = NET + "/home/storyList.do";

	/** 通过视频id删除视频 */
	public static final String VIDEO_DELETE = NET + "/live/userLiveDelete.do";
	/** 给视频投票 */
	public static final String VIDEO_VOTE = NET + "/live/liveVote.do";

	// 按照类别，用户Id，活动Id，关键字等查询回放列表
	public static final String HISTORY_TYPE_LIST = NET + "/live/listVideoByType.do";

	// 那时回放首页，包括回放分类和回放活动的基类列表
	public static final String HISTORY_VIDEO_HOME = NET + "/live/videoList.do";

	// 回放视屏页的数据更新
	public static final String HISTORY_VIDEO_DATA_UPDATA = NET + "/live/lookVideo.do";

	/** 回放视频页点赞 */
	public static final String HISTORY_VIDEO_ADD_PRAISE = NET + "/live/addPraiseNum.do";

	/** 视频举报 */
	public static final String REPORT_LIVE = NET + "/live/report.do";

	public static final String LIVE_QUIT = NET + "/live/stopTerraceLive.do";

	/** 分享视频 */
	public static final String SHARE_VIDEO_URL = Constants.Root_Url + "/live/share.do?share=";
	/** 分享视频_抖音 */
	public static final String TIK_SHARE_VIDEO_URL = Constants.Root_Url + "/live/shortVideoshare.do?share=";
	/** 获取视频评论数据 */
	public static final String VIDEO_COMMENT_DATA = Constants.Root_Url + "/live/liveVideoComment.do";
	/** 获取视频评论数据_抖音 */
	public static final String TIK_VIDEO_COMMENT_DATA = Constants.Root_Url + "/cctv/shortVideoCommentList.do";

	/** 送礼物列表 */
	public static final String GIFT_LIST = NET + "/gift/giftList.do";

	/** 送礼物列表 */
	public static final String SEND_GIFT_REQUEST = NET + "/gift/giftBuyApply.do";

	/** 我的红币 */
	public static final String GET_MY_RED_COIN = NET + "/gift/myRedCoinTotal.do";

	/** 获取某商家商品列表 */
	public static final String GET_PRODUCT_LIST = NET + "/goods/goodsList.do";

	/** 获取某商家商品列表 */
	public static final String GET_MY_PRODUCT_LIST = NET + "/goods/goodsListBasic.do";

	/** 地图 */
	/** 地图标注数据列表 */
	public static final String GET_MAP_MARKER_LIST = NET + "/map/queryMapActiveObject.do";

	/** 添加地图直播活动等 */
	public static final String MAP_ADD_ACTIONLIVE = NET + "/map/addMapActiveObject.do";

	/** 结束地图直播活动等 */
	public static final String MAP_ACTIONLIVE_END = NET + "/map/removeMapActiveObject.do";
	// 优惠券的领取
	public static final String COUPON_GET = NET + "/user/userGetCoupno.do";
	/** 活动投票排行榜列表 */
	public static final String ACTIVITYS_VOTE_RANK_LIST = NET + "/live/liveActivityVoteCount.do";
	/** 初始化投票数 */
	public static final String GET_ACTIVITYS_VOTE_COUNT = NET + "/live/liveVoteCount.do";
	/** 投票 */
	public static final String ACTIVITYS_VOTE_COMMIT = NET + "/live/liveActivityVote.do";
	/** 评论点赞 */
	public static final String VOTE_COMMENT_LIKE = NET + "/orders/commentPraise.do?";

	/** 活动信息 */
	public static final String GET_ACTIVITY_INFO = NET + "/live/liveActivity.do";
	/** 活动信息 */
	public static final String GET_HOT_WORD = NET + "/live/searchHot.do";
}