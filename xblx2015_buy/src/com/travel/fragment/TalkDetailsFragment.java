package com.travel.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner;
import com.travel.AdapterJoiner.JoinableAdapter;
import com.travel.AdapterJoiner.JoinableLayout;
import com.travel.activity.OneFragmentActivity;
import com.travel.adapter.CommentsAdapter;
import com.travel.entity.TalkBean;
import com.travel.helper.TalkCommentsHelper;
import com.travel.layout.CustomLinearLayoutManager;
import com.travel.layout.InputPopupWindow;
import com.travel.layout.SendMessageLayout;
import com.travel.layout.VideoViewFragment;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.activity.PersonalHomeActivity;
import com.travel.shop.bean.CommentBean;
import com.travel.utils.TalkPraiseUtil;
import com.travel.video.adapter.PictureAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 说说详情页面
 * Created by Administrator on 2018/3/7.
 */
public class TalkDetailsFragment extends Fragment implements SwipeRefreshAdapterView.OnListLoadListener{

    private View mView;
    private Context mContext;
    private TalkBean talkBean;

    // 刷新控件
    private SwipeRefreshRecyclerView mSwipeRefreshRecyclerView;
    private CustomLinearLayoutManager linearLayoutManager;
    private AdapterJoiner joiner;
    // 说说详情信息
    private JoinableLayout detailsJoinableLayout;
    private JoinableLayout titleEndJoinable;
    // 评论列表
    private TalkCommentsHelper helper;
    private CommentsAdapter commentsAdapter;
    private ArrayList<CommentBean> commentList;
    private int mPage = 1;


    private SendMessageLayout sendMessageLayout;
    private boolean isPraise = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        talkBean = (TalkBean) getArguments().getSerializable("talk_bean");
//        if (talkBean.getPraiseType() == 0)
//            isPraise = true;
        if(TalkPraiseUtil.getInstance().getDatas().containsKey(Integer.parseInt(talkBean.getId()))){
            isPraise = true;
        }
        mView = inflater.inflate(R.layout.fragment_talk_details, container, false);
        init();
        initListener();
        // 有网
        if (!CheckNetStatus.unNetwork.equals(CheckNetStatus.checkNetworkConnection())) {
            getData();
        }
        return mView;
    }

    /**
     * 控件初始化
     */
    private void init() {
        mContext = getActivity();
        ImageDisplayTools.initImageLoader(mContext);

        sendMessageLayout = (SendMessageLayout) mView.findViewById(R.id.send_comment_layout);
        sendMessageLayout.setLikeContent(isPraise, talkBean.getPraiseNum());
        helper = new TalkCommentsHelper(listener);
        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(mContext, commentList);

    }

    private void initListener() {
        sendMessageLayout.setSendListener(new SendMessageLayout.SendListener() {
            @Override
            public void sendMessage(String message) {
                if(UserSharedPreference.isLogin())
                    helper.addComments(UserSharedPreference.getUserId(), talkBean.getId(), message, TalkCommentsHelper.TYPE_TALK, "-1", "-1");
                else
                    OSUtil.intentLogin(getContext());
            }

            @Override
            public void cityLike() {
                if(!UserSharedPreference.isLogin()){
                    OSUtil.intentLogin(mContext);
                }
                String praiseType = isPraise ? "1" : "0";
                TalkPraiseUtil.getInstance().voteVideo(getContext(), talkBean.getId(), praiseType,
                        new TalkPraiseUtil.VideoVoteListener() {
                            @Override
                            public void onSuccess(int error, boolean isSuccess) {
                                if(isPraise){
                                    if (isSuccess) {
                                        isPraise = false;
                                        sendMessageLayout.minusOneLikeNum();
                                    }else if(error == 1){
                                        Toast.makeText(mContext, "取消失败", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    if (isSuccess) {
                                        isPraise = true;
                                        sendMessageLayout.addOneLikeNum();
                                    }else if(error == 1){
                                        Toast.makeText(mContext, "点赞失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }

            @Override
            public void cityShare() {

            }
        });

        commentsAdapter.setClickListener(new CommentsAdapter.ClickListener() {
            @Override
            public void onClick(int position) {

            }

            @Override
            public void onClickContent(final int position) {
                int cId = commentList.get(position).getBusinessId();
                new InputPopupWindow(getActivity(), cId+"")
                        .setListener(new InputPopupWindow.OnListener() {
                            @Override
                            public void onInputText(String content, String tag) {
                                if(UserSharedPreference.isLogin())
                                    helper.addComments(
                                            UserSharedPreference.getUserId(),
                                            tag,
                                            content,
                                            TalkCommentsHelper.TYPE_TALK,
                                            commentList.get(position).getUser().getId(),
                                            commentList.get(position).getId() + "");
                                else
                                    OSUtil.intentLogin(getContext());
                            }
                        });
            }
        });
    }

    // 说说详情基本信息展示
    TextView tv_name, tv_time, tv_content, tv_content_all, tv_place, tv_follow;
    ImageView iv_head, iv_sex, iv_video_picture;
    RecyclerView recyclerView;
    View layout_video_picture;
    private void initDetails(View view) {
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_time = (TextView) view.findViewById(R.id.tv_time);
        tv_content = (TextView) view.findViewById(R.id.tv_content);
        tv_content_all = (TextView) view.findViewById(R.id.tv_content_all);
        tv_place = (TextView) view.findViewById(R.id.tv_place);
        tv_follow = (TextView) view.findViewById(R.id.tv_follow);
        iv_head = (ImageView) view.findViewById(R.id.iv_head);
        iv_sex = (ImageView) view.findViewById(R.id.iv_sex);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_picture);
        layout_video_picture =  view.findViewById(R.id.layout_video_picture);
        iv_video_picture = (ImageView) view.findViewById(R.id.iv_video_picture);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);

        ImageDisplayTools.displayHeadImage(talkBean.getUser().getImgUrl(), iv_head);
        iv_sex.setImageResource(talkBean.getUser().getSex() == 1 ? R.drawable.icon_sex_man : R.drawable.icon_sex_woman);
        tv_name.setText(talkBean.getUser().getNickName());
        tv_time.setText(DateFormatUtil.getPastTime(talkBean.getTime()));
        tv_place.setText(talkBean.getUser().getPlace());

        layout_video_picture.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tv_content.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(talkBean.getVideoUrl())) {
            layout_video_picture.setVisibility(View.VISIBLE);
            ImageDisplayTools.disPlayRoundDrawable(talkBean.getImgUrl(), iv_video_picture, 8);
            TravelUtil.setFLParamsWidthPart(iv_video_picture, 1, 90, 16, 9);
        } else if(!TextUtils.isEmpty(talkBean.getImgUrl())) {// 图片列表
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText(talkBean.getContent());
            List<String> imgs = new ArrayList<>();
            if (talkBean.getImgUrl().contains(",")) {
                String[] img = talkBean.getImgUrl().split(",");
                imgs.addAll(Arrays.asList(img));
            } else {
                imgs.add(talkBean.getImgUrl());
            }
            recyclerView.setVisibility(View.VISIBLE);
            GridLayoutManager manager = new GridLayoutManager(mContext, 3);
            recyclerView.setLayoutManager(manager);
            PictureAdapter adapter = new PictureAdapter(mContext, imgs);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else if (!TextUtils.isEmpty(talkBean.getContent())) {
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText(talkBean.getContent());
        }

        tv_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        iv_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalHomeActivity.actionStart(mContext, false, talkBean.getUser().getId(), talkBean.getUser().getNickName());
            }
        });
        layout_video_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("path", talkBean.getVideoUrl());
                TravelUtil.goPlay(mContext, bundle);
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshRecyclerView = (SwipeRefreshRecyclerView) mView.findViewById(R.id.srrv_fm_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linearLayoutManager = new CustomLinearLayoutManager(getContext());
        mSwipeRefreshRecyclerView.setLayoutManager(linearLayoutManager);
        mSwipeRefreshRecyclerView.setEnabled(false);
//        mSwipeRefreshRecyclerView.setOnListLoadListener(this);
        initJoinableLayout();
        joiner = new AdapterJoiner();
        joiner.add(detailsJoinableLayout);
        joiner.add(titleEndJoinable);
        joiner.add(new JoinableAdapter(commentsAdapter));
        mSwipeRefreshRecyclerView.setAdapter(joiner.getAdapter());
        joiner.getAdapter().notifyDataSetChanged();
    }

    /*@Override
    public void onRefresh() {
        mPage = 1;
        getData();
        mSwipeRefreshRecyclerView.setRefreshing(false);
    }*/

    private void initJoinableLayout() {
        detailsJoinableLayout = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                View view = View.inflate(context, R.layout.layout_talk_details, null);
                initDetails(view);
                return view;
            }
        });

        titleEndJoinable = new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
            @Override
            public View onNeedLayout(Context context) {
                return getTitleEntryLayout("精彩评论");
            }
        });

    }

    private View getTitleEntryLayout(final String title) {
        View view = View.inflate(mContext, R.layout.layout_out_title, null);
        ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        TextView cTitle = (TextView) view.findViewById(R.id.tv_c_title);
        View more = view.findViewById(R.id.more);
        TextView more_text = (TextView) view.findViewById(R.id.more_text);
        ImageView more_icon = (ImageView) view.findViewById(R.id.more_icon);
        cTitle.setText(title);
        more.setVisibility(View.VISIBLE);
        more_text.setText("");
        more_icon.setImageResource(R.drawable.home_ico_arrow);
        more_icon.setVisibility(View.GONE);
        return view;
    }

    /**
     * 获取评论数据
     */
    private void getData() {
        helper.getCommentsList(mPage, talkBean.getId(), TalkCommentsHelper.TYPE_TALK, -1);
    }

    private boolean mIsFirstTime = true;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mIsFirstTime)
            return;
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsFirstTime = false;
        setUserVisibleHint(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onListLoad() {
        ++mPage;
        getData();
    }

    private TalkCommentsHelper.CommentsHttpListener listener = new TalkCommentsHelper.CommentsHttpListener() {
        @Override
        public void OnGetComments(List<CommentBean> comments, boolean isSuc) {
            mSwipeRefreshRecyclerView.setLoading(false);
            if(mPage > 1 && (comments == null || comments.size() == 0)) {
                --mPage;
                Toast.makeText(mContext, "没有更多数据了！", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mPage == 1)
                commentList.clear();
            if(isSuc && comments != null && comments.size() != 0) {
                commentList.addAll(comments);

            }
            commentsAdapter.notifyDataSetChanged();
        }

        @Override
        public void AddCommentsResult(boolean isSuc) {
            if(isSuc){
                mPage = 1;
                getData();
                Toast.makeText(getContext(), "添加成功！", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(), "添加失败！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void likeSuccess() {

        }
    };
}
