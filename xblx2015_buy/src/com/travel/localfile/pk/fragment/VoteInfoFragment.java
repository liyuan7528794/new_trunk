package com.travel.localfile.pk.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.activity.HomeActivity;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.UDPSendInfoBean;
import com.travel.communication.helper.PlayerHelper;
import com.travel.fragment.VoteVideoPlayFragment;
import com.travel.layout.ImageViewPopupWindow;
import com.travel.layout.VideoViewFragment;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.CommentsView;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.pk.activity.EvidenceSelectActivity;
import com.travel.localfile.pk.activity.PublicVoteActivity;
import com.travel.localfile.pk.adapter.EvidenceMediaAdapter;
import com.travel.localfile.pk.adapter.EvidencePacketAdapter;
import com.travel.localfile.pk.entity.EvidencePacket;
import com.travel.localfile.pk.others.VoteInfoHelper;
import com.travel.shop.activity.OrderInfoActivity;
import com.travel.video.widget.VoteLVBMediaView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */
public class VoteInfoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SwipeRefreshRecyclerView.OnListLoadListener {
    private final static String TAG = "VoteInfoFragment";
    public static final int WIN_BUYER = 1; // buyer胜
    public static final int WIN_SELLER = 2; // seller胜
    public static final int WIN_VOTING = 3; // 投票ing
    public static final int WIN_UNKNOWN = 4; // 结果未知
    public static final int WIN_MUST_ADD_ONE_EVIDENCE = 5; // 必须提交一个证据阶段
    public static final int WIN_UNDER_REVIEWING = 6; // 审核中...
    public static final int WIN_APPLICATION_FAILED = 7; // 审核失败

    public static final String USER_ID_SELLER = "seller_user_id";
    public static final String USER_ID_BUYER = "buyer_user_id";
    public static final String GOTO_HOME_ACTIIVTY = "goto_home_activity";
    public static final String BUNDLE_VOTE_ID = "vote_id";
    public static final String BUNDLE_VOTE_STATUS = "vote_status";
    public static final String BUNDLE_VOTE_TITLE = "vote_title";

    private boolean mGotoHomeActivity = false;
    private int mVoteStatus = WIN_BUYER;
    private String mSellerId = "", mBuyerId = "", mUserId = "";
    private int mVoteId = 1; // 众投Id
    private String mVoteTitle = "";// 众投标题

    private VoteInfoHelper.VoteDetailsInfo voteDetailsInfo;
    private VoteInfoHelper httpHelper;

    private View rootView;
    private LinearLayout ll_container;
    private TextView tv_upload, tv_golive, tv_comment1;
    private LinearLayout ll_end;
    private TextView tv_end, tv_comment2;
    private LinearLayout ll_comment;
    private TextView tv_comment_send;
    private EditText et_comment;

    // 众投视频
    private VoteLVBMediaView voteMediaView;
    private boolean isCanLive = true;

    // 众投结束头部状态显示
    private View headView;
    private TextView buyerNum, sellerNum, buyerName, sellerName, endTime, result;

    private SwipeRefreshRecyclerView srRecyclerView;
    private AdapterJoiner joiner;

    // 订单信息
    private View goodsView;
    private ImageView goodsHeadImg, goodsCover;
    private TextView goodsName, goodsTime, goodsPrice, goodsReason, goodsTitle, goodsTotalPrice;
    // 证据列表
    private EvidencePacketAdapter adapter;
    private List<EvidencePacket> evidenceList;
    private List<LocalFile> mediaEvidenceList;
    private int mediaCount = 0;
    // 众投评论
    private CommentsView commentsView;

    private ImageViewPopupWindow mImageViewPopupWindow;
    //    private VideoViewPopWindow mVideoViewPopWindow;
//    private MediaController.MediaPlayerControl mVideoView;
    private PlayerHelper mPlayerHelper;

    private String beginTime1, endTime1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beginTime1 = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initIntentData();
        evidenceList = new ArrayList<>();
        mediaEvidenceList = new ArrayList<>();
        adapter = new EvidencePacketAdapter(getContext(), evidenceList, mediaListener);
        httpHelper = new VoteInfoHelper(getContext(), mVoteId, new VoteInfoHelper.VoteHttpListener() {
            @Override
            public void OnVoteDetail(VoteInfoHelper.VoteDetailsInfo info) {
                voteDetailsInfo = info;
                initDetailData();
            }

            @Override
            public void OnVotedUsers(HashMap<Integer, Integer> userMap) {
                commentsView.setHashMap(userMap);
            }

            @Override
            public void OnEvidencePackets(final JSONArray data) {
                if (voteDetailsInfo == null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OnEvidencePackets(data);
                        }
                    }, 500);
                    return;
                }
                hideLoading();
                initAudioPlayer();
                if ((data == null || data.length() < 1)) {
                    return;
                }
                evidenceList.clear();
                for (int i = 0, length = data.length(); i < length; i++) {

                    JSONObject jsonObject = JsonUtil.getJSONObject(data, i);
                    EvidencePacket evi = EvidencePacket.generateFromJSONObject(jsonObject, mBuyerId,
                            mSellerId);
                    int packetUserId = JsonUtil.getJsonInt(jsonObject, "userid");
                    evi.setIsLeft(mBuyerId.equals(String.valueOf(packetUserId)));
                    evi.setUserData(evi.isLeft() ? voteDetailsInfo.getBuyer() : voteDetailsInfo.getSeller());
                    // 给多媒体文件添加标志位，从1开始记，加这个标志位为了后面点击视频知道是点击的多媒体文件的第几个，方便轮播
                    ArrayList<LocalFile> lfs = new ArrayList<>();
                    for (int j = 0; j < evi.getMultipleMediaList().size(); j++) {
                        LocalFile localFile = evi.getMultipleMediaList().get(j);
                        if (CameraFragment.TYPE_PHOTO != localFile.getType()) {
//                        if (CameraFragment.TYPE_PHOTO == localFile.getType() || CameraFragment.TYPE_AUDIO == localFile.getType()) {
                            localFile.setPosition(++mediaCount);
                        } else {
                            localFile.setPosition(0);
                        }
                        lfs.add(localFile);
                    }
                    evi.setMultipleMediaList(lfs);
                    evidenceList.add(evi);

                    // 将多媒体文件存到list中
                    ArrayList<LocalFile> localFiles = evi.getMultipleMediaList();
                    for (LocalFile localFile : localFiles) {
                        if (CameraFragment.TYPE_PHOTO != localFile.getType()) {
//                        if (CameraFragment.TYPE_PHOTO == localFile.getType() || CameraFragment.TYPE_AUDIO == localFile.getType()) {
                            mediaEvidenceList.add(localFile);
                        }
                    }
                }
                freshView();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPublishVoteResult(boolean isSuccess) {
                if (isSuccess) {
                    mVoteStatus = WIN_UNDER_REVIEWING;
                    freshView();
                }
            }

            @Override
            public void onVoteResult(boolean isSuccess) {

            }
        });

        httpHelper.setBuyerAndSellerId(Integer.parseInt(mBuyerId), Integer.parseInt(mSellerId));
    }

    private void initIntentData() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        mVoteId = bundle.getInt(BUNDLE_VOTE_ID, mVoteId);
        mVoteStatus = bundle.getInt(BUNDLE_VOTE_STATUS, mVoteStatus);
        mSellerId = bundle.getString(USER_ID_SELLER, mSellerId);
        mBuyerId = bundle.getString(USER_ID_BUYER, mBuyerId);
        mVoteTitle = bundle.getString(BUNDLE_VOTE_TITLE, mVoteTitle);
        mGotoHomeActivity = bundle.getBoolean(GOTO_HOME_ACTIIVTY, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.vote_info_fragment, null);
        headView = inflater.inflate(R.layout.vote_finish_head_layout, null);
        initView();
        initGoods();
        initComments();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        srRecyclerView = (SwipeRefreshRecyclerView) rootView.findViewById(R.id.srRecyclerView);
        setListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        srRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        srRecyclerView.setLoading(false);
        srRecyclerView.setOnRefreshListener(this);
        srRecyclerView.setOnListLoadListener(this);
        if (OSUtil.isDayTheme())
            srRecyclerView.setLoadViewBackground(ContextCompat.getColor(getContext(), android.R.color.white));
        else
            srRecyclerView.setLoadViewBackground(ContextCompat.getColor(getContext(), R.color.black_3));

        joinerAdapter();
    }

    private void initView() {
        ll_container = (LinearLayout) rootView.findViewById(R.id.ll_container);
        tv_upload = (TextView) rootView.findViewById(R.id.tv_upload);
        tv_golive = (TextView) rootView.findViewById(R.id.tv_golive);
        tv_comment1 = (TextView) rootView.findViewById(R.id.tv_comment1);
        ll_end = (LinearLayout) rootView.findViewById(R.id.ll_end);
        tv_end = (TextView) rootView.findViewById(R.id.end);
        tv_comment2 = (TextView) rootView.findViewById(R.id.tv_comment2);
        ll_comment = (LinearLayout) rootView.findViewById(R.id.ll_comment);
        tv_comment_send = (TextView) rootView.findViewById(R.id.tv_comment_send);
        et_comment = (EditText) rootView.findViewById(R.id.et_comment_edit);
//        OSUtil.hideKeyboard(getActivity());

        voteMediaView = new VoteLVBMediaView(getContext());
        voteMediaView.setListener(pkVideoListener);
        voteMediaView.initShowLayout(mVoteId, mBuyerId + "", mSellerId + "");

        buyerNum = (TextView) headView.findViewById(R.id.buyerNum);
        sellerNum = (TextView) headView.findViewById(R.id.sellerNum);
        buyerName = (TextView) headView.findViewById(R.id.buyerName);
        sellerName = (TextView) headView.findViewById(R.id.sellerName);
        endTime = (TextView) headView.findViewById(R.id.endTime);
        result = (TextView) headView.findViewById(R.id.result);
    }

    private void initGoods() {
        goodsView = View.inflate(getContext(), R.layout.adapter_evidence_goods, null);
        goodsHeadImg = (ImageView) goodsView.findViewById(R.id.headImg);
        goodsCover = (ImageView) goodsView.findViewById(R.id.cover);
        goodsName = (TextView) goodsView.findViewById(R.id.nickName);
        goodsTime = (TextView) goodsView.findViewById(R.id.time);
        goodsPrice = (TextView) goodsView.findViewById(R.id.price);
        goodsReason = (TextView) goodsView.findViewById(R.id.reason);
        goodsTitle = (TextView) goodsView.findViewById(R.id.title);
        goodsTotalPrice = (TextView) goodsView.findViewById(R.id.totalPrice);
    }

    private void initComments() {
        commentsView = new CommentsView(getContext());
        commentsView.initData(mVoteId, new CommentsView.VoteCommentsListener() {
            @Override
            public void OnRefreshFinish() {
                hideLoading();
            }
        });
    }

    private Handler handler;

    private void setListener() {
        tv_upload.setOnClickListener(uploadClick);
        tv_golive.setOnClickListener(goLiveClick);
        tv_end.setOnClickListener(endClick);
        tv_comment1.setOnClickListener(commentClick);
        tv_comment2.setOnClickListener(commentClick);
        tv_comment_send.setOnClickListener(sendCommentClick);
        srRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibilityBelow();
            }
        });


        handler = new Handler();
        goodsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserSharedPreference.isLogin()) {
                    Intent intent = new Intent(getContext(), OrderInfoActivity.class);
                    intent.putExtra("ordersId", voteDetailsInfo.getOrderId());
                    intent.putExtra("identity", "viewer");
                    startActivity(intent);
//                    OrderInfoRouteActivity.actionStart(getContext(), voteDetailsInfo.getOrderId(), "",
//                            "viewer", new AttachGoodsBean());
                } else
                    startActivity(new Intent(ShopConstant.LOG_IN_ACTION).putExtra("refresh", "refresh"));

            }
        });
    }

    private void initAudioPlayer() {
        mPlayerHelper = PlayerHelper.getInstance(getContext(), null);
        mPlayerHelper.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //                        audioImg.setImageResource(R.drawable.detail_icon_play);
                    }
                });
                return true;
            }
        });
        mPlayerHelper.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        audioImg.setImageResource(R.drawable.detail_icon_play);
                        audioImg = null;
                    }
                });
            }
        });
    }

    private void initDetailData() {
        voteMediaView.initData(voteDetailsInfo.getBuyer().getNickName(),
                voteDetailsInfo.getBuyer().getImgUrl(),
                voteDetailsInfo.getSeller().getNickName(),
                voteDetailsInfo.getSeller().getImgUrl());

        ImageDisplayTools.disPlayRoundDrawableHead(voteDetailsInfo.getBuyer().getImgUrl(), goodsHeadImg, OSUtil.dp2px(getContext(), 2));
        ImageDisplayTools.displayImage(voteDetailsInfo.getCover(), goodsCover);
        if (!OSUtil.isDayTheme()) {
            goodsHeadImg.setColorFilter(TravelUtil.getColorFilter(getContext()));
            goodsCover.setColorFilter(TravelUtil.getColorFilter(getContext()));
        }
        goodsName.setText("买方 " + voteDetailsInfo.getBuyer().getNickName());
        goodsTime.setText(voteDetailsInfo.getCreateTime());
        goodsPrice.setText("要求赔付: " + voteDetailsInfo.getClaimAmount() + "元");
        goodsReason.setText("赔付理由：" + voteDetailsInfo.getReason());
        goodsTitle.setText(voteDetailsInfo.getSubhead());
        goodsTotalPrice.setText("￥ " + voteDetailsInfo.getPaymentPrice());

        buyerNum.setText("" + voteDetailsInfo.getBuyerPoll());
        sellerNum.setText("" + voteDetailsInfo.getSellerPoll());
        buyerName.setText("买方·" + voteDetailsInfo.getBuyer().getNickName());
        sellerName.setText("卖方·" + voteDetailsInfo.getSeller().getNickName());
        if (!"".equals(voteDetailsInfo.getCheckTime())) {
            endTime.setText("截止至 " + DateFormatUtil.formatTime(
                    new Date(3 * 24 * 60 * 60 * 1000 + Long.parseLong(voteDetailsInfo.getCheckTime())),
                    DateFormatUtil.FORMAT_DATE_ZH_CN) //本来是FORMAT_DTAE2_TIME2，但为了和苹果保持一致所以改成 年月日 了
            );
        } else {
            endTime.setText("截止至 ");
        }
        mVoteStatus = getVoteStatus(voteDetailsInfo.getStatus(), voteDetailsInfo.getCheckStatus(),
                voteDetailsInfo.getBuyerId() + "", voteDetailsInfo.getSellerId() + "", voteDetailsInfo.getVictory());
        freshView();
    }

    private int getVoteStatus(int showStatus, int checkStatus, String sellerId, String buyerId, String victory) {
        int resultStatus = 0;
        switch (showStatus) {
            case 0:
                resultStatus = WIN_MUST_ADD_ONE_EVIDENCE;
                break;
            case 1:
                switch (checkStatus) {
                    case 0:
                        resultStatus = WIN_UNDER_REVIEWING;
                        break;
                    case 1:
                        resultStatus = WIN_VOTING;
                        break;
                    case 2:
                        resultStatus = WIN_APPLICATION_FAILED;
                        break;
                }
                break;
            case 2:
                resultStatus = WIN_VOTING;
                break;
            case 3:
                if (sellerId.equals(victory)) {
                    resultStatus = WIN_SELLER;
                } else if (buyerId.equals(victory)) {
                    resultStatus = WIN_BUYER;
                }
                break;
        }
        return resultStatus;
    }

    private void freshView() {
        // 显示头部布局
        if (mVoteStatus == WIN_BUYER || mVoteStatus == WIN_SELLER || mVoteStatus == WIN_UNKNOWN) {
            joiner.replace(0, headLayout);
            if (mVoteStatus == WIN_BUYER)
                result.setText("买方 胜出");
            else if (mVoteStatus == WIN_SELLER)
                result.setText("卖方 胜出");
            else if (mVoteStatus == WIN_UNKNOWN) {
                if (voteDetailsInfo.getBuyerPoll() >= voteDetailsInfo.getSellerPoll()) {
                    result.setText("买方 胜出");
                } else {
                    result.setText("卖方 胜出");
                }
            }
        } else {
            joiner.replace(0, mediaLayout);
//            mediaLayout.hide();
        }
        adapter.notifyDataSetChanged();

        // 显示底部布局
        setVisibilityBelow();

        switch (mVoteStatus) {
            case WIN_MUST_ADD_ONE_EVIDENCE:
                if (evidenceList.isEmpty())
                    tv_end.setText("上传证据");
                else
                    tv_end.setText("发布众投");
                break;
            case WIN_UNDER_REVIEWING:
                tv_end.setText("平台审核中...");
                break;
            case WIN_APPLICATION_FAILED:
                tv_end.setText("审核未通过");
                break;
            case WIN_BUYER:
            case WIN_SELLER:
            case WIN_UNKNOWN:
                tv_end.setText("众投已结束");
            default:
                // ignore
        }

        // 买卖双方
        if (UserSharedPreference.getUserId().equals(mBuyerId) ||
                (UserSharedPreference.getUserId().equals(voteDetailsInfo.getSeller().getId()))) {
            tv_upload.setText("上传证据");
            tv_golive.setText("直播申诉");
        } else {
            tv_upload.setText("支持买方");
            tv_golive.setText("支持卖方");
        }
    }

    private void joinerAdapter() {
        joiner = new AdapterJoiner();
        joiner.add(nullLayout);
        joiner.add(orderLayout);
        joiner.add(new JoinableAdapter(adapter));
        joiner.add(commentsLayout);
        srRecyclerView.setAdapter(joiner.getAdapter());
    }

    // 头部占位布局
    JoinableLayout nullLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
        @Override
        public View onNeedLayout(Context context) {
            View view = new View(getContext());
            view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, OSUtil.dp2px(context, 210));
            view.setLayoutParams(params);
            return view;
        }
    });
    JoinableLayout mediaLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
        @Override
        public View onNeedLayout(Context context) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, OSUtil.dp2px(context, 210));
            voteMediaView.setLayoutParams(params);
            return voteMediaView;
        }
    });
    JoinableLayout headLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
        @Override
        public View onNeedLayout(Context context) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            headView.setLayoutParams(params);
            return headView;
        }
    });
    JoinableLayout orderLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
        @Override
        public View onNeedLayout(Context context) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            goodsView.setLayoutParams(params);
            return goodsView;
        }
    });
    JoinableLayout commentsLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {

        @Override
        public View onNeedLayout(Context context) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            commentsView.setLayoutParams(params);
            return commentsView;
        }
    });


    private VoteLVBMediaView.PKVideoListener pkVideoListener = new VoteLVBMediaView.PKVideoListener() {

        @Override
        public void playStatus(boolean isPlaying) {
            if (!joiner.isContains(mediaLayout))
                return;
            if (isPlaying) {
//                mediaLayout.show();
                srRecyclerView.getScrollView().smoothScrollToPosition(0);
            } else {
//                mediaLayout.hide();
            }
        }

        @Override
        public void hideLiveButton() {
            isCanLive = false;
        }

        @Override
        public void showLiveButton() {
            isCanLive = true;
        }

        @Override
        public void videoFullScreen(boolean isHost) {
            //            voteLVBFullScreenView.isHost(isHost);
            //            lvbVideoFullScreen();
        }

        @Override
        public void videoZoomScreen() {
            //            lvbVideoZoomScreen();
        }
    };
    private ImageView audioImg = null;
    private EvidenceMediaAdapter.OnMediaItemClickListener mediaListener = new EvidenceMediaAdapter.OnMediaItemClickListener() {

        @Override
        public void onClick(LocalFile localFile, ImageView view) {
            switch (localFile.getType()) {
                case CameraFragment.TYPE_PHOTO:
                    if (mImageViewPopupWindow == null)
                        mImageViewPopupWindow = new ImageViewPopupWindow();
                    mImageViewPopupWindow.show(getActivity(), srRecyclerView, localFile.getRemotePath());
                    break;
                case CameraFragment.TYPE_AUDIO:
                    //                    Bundle bundleAudio = new Bundle();
//                    bundleAudio.putSerializable("videos", (ArrayList<LocalFile>) mediaEvidenceList);
//                    bundleAudio.putInt("position", localFile.getPosition());
//                    OneFragmentActivity.startNewActivity(getContext(), "", VoteVideoPlayFragment.class, bundleAudio);
                    MediaPlayer player = mPlayerHelper.getPlayer();
                    if (audioImg != null && audioImg != view) {
                        mPlayerHelper.playerFullPathAudio(localFile.getRemotePath(), true);
                        audioImg.setImageResource(R.drawable.detail_icon_play);
                        view.setImageResource(R.drawable.detail_icon_stop);
                        audioImg = view;
                    }

                    if (player != null && player.isPlaying()) {
                        view.setImageResource(R.drawable.detail_icon_play);
                        player.pause();
                    } else if (player != null) {
                        player.start();
                        view.setImageResource(R.drawable.detail_icon_stop);
                    } else if (audioImg == null) {
                        mPlayerHelper.playerFullPathAudio(localFile.getRemotePath(), true);
                        view.setImageResource(R.drawable.detail_icon_stop);
                        audioImg = view;
                    }

                    break;
                case CameraFragment.TYPE_VIDEO:
//                    Bundle bundleVideo = new Bundle();
//                    bundleVideo.putSerializable("videos", (ArrayList<LocalFile>) mediaEvidenceList);
//                    bundleVideo.putInt("position", localFile.getPosition());
//                    OneFragmentActivity.startNewActivity(getContext(), "", VoteVideoPlayFragment.class, bundleVideo);
                    MediaPlayer players = mPlayerHelper.getPlayer();
                    if (audioImg != null)
                        audioImg.setImageResource(R.drawable.detail_icon_play);
                    if (players != null && players.isPlaying())
                        players.pause();


                    //                if (mVideoViewPopWindow == null) {
                    //                    mVideoViewPopWindow = new VideoViewPopWindow();
                    //                    mVideoView = new IjkVideoView(getActivity());
                    //                }
                    //                if (mVideoView instanceof IjkVideoView) {
                    //                    ((IjkVideoView) mVideoView).stopPlayback();
                    //                    mVideoView = new IjkVideoView(getActivity());
                    //                }
                    //                ((IjkVideoView) mVideoView).setVideoPath(localFile.getRemotePath());
                    //                mVideoView.start();
                    //                mVideoViewPopWindow.show(getActivity(), srRecyclerView, mVideoView);
                    Bundle bundle = new Bundle();
                    bundle.putString("path", localFile.getRemotePath());
                    OneFragmentActivity.startNewActivity(getContext(), "", VideoViewFragment.class, bundle);
                    break;
                case CameraFragment.TYPE_LIVE:
//                    Bundle bundleLive = new Bundle();
//                    bundleLive.putSerializable("videos", (ArrayList<LocalFile>) mediaEvidenceList);
//                    bundleLive.putInt("position", localFile.getPosition());
//                    OneFragmentActivity.startNewActivity(getContext(), "", VoteVideoPlayFragment.class, bundleLive);
                    MediaPlayer playerss = mPlayerHelper.getPlayer();
                    if (audioImg != null)
                        audioImg.setImageResource(R.drawable.detail_icon_play);
                    if (playerss != null && playerss.isPlaying())
                        playerss.pause();
                    Bundle bundleLive = new Bundle();
                    bundleLive.putString("path", localFile.getRemotePath());
                    OneFragmentActivity.startNewActivity(getContext(), "", VideoViewFragment.class, bundleLive);
////
//                    if (mVideoViewPopWindow == null) {
//                        mVideoViewPopWindow = new VideoViewPopWindow();
//                        mVideoView = new IjkVideoView(getActivity());
//                    }
//                    if (mVideoView instanceof IjkVideoView) {
//                        ((IjkVideoView) mVideoView).stopPlayback();
//                        mVideoView = new IjkVideoView(getActivity());
//                    }
//                    ((IjkVideoView) mVideoView).setVideoPath(localFile.getRemotePath());
//                    mVideoView.start();
//                    mVideoViewPopWindow.show(getActivity(), srRecyclerView, mVideoView);
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        if (mPlayerHelper != null)
            mPlayerHelper.release();
        evidenceList.clear();
        httpHelper.getVoteDataList();
        commentsView.onRefresh();
    }

    @Override
    public void onListLoad() {
        commentsView.onListLoad();
    }

    private void hideLoading() {
        srRecyclerView.setRefreshing(false);
        srRecyclerView.setLoading(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        httpHelper.getPublicVoteDetail();
    }

    @Override
    public void onResume() {
        super.onResume();
        httpHelper.getVoteDataList();
        if (voteMediaView != null)
            voteMediaView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (voteMediaView != null)
            voteMediaView.pause();
    }

    @Override
    public void onDestroy() {
        MLog.d(TAG, "onDestroy");
        super.onDestroy();
        endTime1 = DateFormatUtil.formatCurrenttime(DateFormatUtil.FORMAT_TIME);
        UDPSendInfoBean bean = new UDPSendInfoBean();
        bean.getData("009_" + mVoteId, mVoteTitle,
                Constants.Root_Url + "/VoteData/voteDataList.do?publicVoteId=" + mVoteId, beginTime1, endTime1);
        ((PublicVoteActivity) getActivity()).sendData(bean);
        if (voteMediaView != null)
            voteMediaView.destroy();
//
//        if (mVideoViewPopWindow != null && mVideoViewPopWindow.isShowing()) {
//            mVideoViewPopWindow.dismiss();
//            if (mVideoView instanceof VideoView) {
//                ((VideoView) mVideoView).stopPlayback();
//            } else {
//                ((IjkVideoView) mVideoView).stopPlayback();
//            }
//        }
        if (mImageViewPopupWindow != null && mImageViewPopupWindow.isShowing()) {
            mImageViewPopupWindow.dismissPopupWindow();
        }
        if (mPlayerHelper != null) {
            mPlayerHelper.release();
        }
        if (mGotoHomeActivity) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.putExtra("position", HomeActivity.PUBLIC_INVERSTMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        evidenceList.clear();
        httpHelper.getVoteDataList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1)
            return;
        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(Manifest.permission.RECORD_AUDIO, permissions[i])
                    && PackageManager.PERMISSION_DENIED == grantResults[i]) {
                Toast.makeText(getContext(), "请打开录音权限！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.equals(Manifest.permission.CAMERA, permissions[i])) {
                if (PackageManager.PERMISSION_DENIED == grantResults[i]) {
                    Toast.makeText(getContext(), "请打开录音权限！", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        voteMediaView.getUrlAndStartLive();
        srRecyclerView.getScrollView().smoothScrollToPosition(0);
    }

    private void setVisibilityBelow() {
        ll_comment.setVisibility(View.GONE);
        if (mVoteStatus == WIN_VOTING) {
            ll_container.setVisibility(View.VISIBLE);
            ll_end.setVisibility(View.GONE);
        } else {
            ll_end.setVisibility(View.VISIBLE);
            ll_container.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener endClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!UserSharedPreference.getUserId().equals(mBuyerId))
                return;
            switch (tv_end.getText().toString()) {
                case "上传证据":
                    EvidenceSelectActivity.startActivityForResult(VoteInfoFragment.this, 1, mVoteId,
                            UserSharedPreference.getUserId().equals(mBuyerId) ? true : false);
                    break;
                case "发布众投":
                    httpHelper.publishVote();
                    break;
                case "平台审核中...":

                    break;
                case "审核未通过":
                    break;
                default:
                    // ignore
            }
        }
    };

    private View.OnClickListener uploadClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!UserSharedPreference.isLogin()) {
//                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                OSUtil.intentLogin(getContext());
                return;
            }
            if ("上传证据".equals(((TextView) v).getText().toString())) {
                EvidenceSelectActivity.startActivityForResult(VoteInfoFragment.this, 1, mVoteId,
                        UserSharedPreference.getUserId().equals(mBuyerId) ? true : false);
            } else {
                httpHelper.getIsVoted(mBuyerId);
            }
        }
    };
    private View.OnClickListener goLiveClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!UserSharedPreference.isLogin()) {
//                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                OSUtil.intentLogin(getContext());
                return;
            }
            if ("直播申诉".equals(((TextView) v).getText().toString())) {
                if (!isCanLive) {
                    Toast.makeText(getContext(), "当前已有人上麦", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 判断所必须的权限
                List<String> premissions = new ArrayList<>();
                // 摄像头
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    premissions.add(Manifest.permission.CAMERA);
                }
                // 录音
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    premissions.add(Manifest.permission.RECORD_AUDIO);
                }
                if (premissions.size() > 0) {
                    String[] s = new String[premissions.size()];
                    for (int i = 0; i < s.length; i++) {
                        s[i] = premissions.get(i);
                    }
                    VoteInfoFragment.this.requestPermissions(s, 1);
                } else {
                    voteMediaView.getUrlAndStartLive();
                    srRecyclerView.getScrollView().smoothScrollToPosition(0);
                }
            } else {
                httpHelper.getIsVoted(mSellerId);
            }
        }
    };

    private View.OnClickListener commentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            et_comment.setVisibility(View.VISIBLE);
            ll_comment.setVisibility(View.VISIBLE);
            ll_container.setVisibility(View.GONE);
            ll_end.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener sendCommentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setVisibilityBelow();
            String src = et_comment.getText().toString().trim();
            if (src == null || "".equals(src)) {
                Toast.makeText(getContext(), "请输入内容！", Toast.LENGTH_SHORT).show();
                return;
            }
            OSUtil.hideKeyboard(getActivity());
            commentsView.sendComments(src);
            et_comment.setText("");

//            setVisibilityBelow();
        }
    };

}
