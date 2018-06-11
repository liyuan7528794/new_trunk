package com.travel.localfile;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.bean.EvaluateInfoBean;
import com.travel.layout.MyRecyclerView;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.pk.others.VoteCommentsHelper;
import com.travel.shop.adapter.GoodsInfoCommentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/6/16.
 */
public class CommentsView extends FrameLayout{
    private Context context;
    private View rootView;

    private MyRecyclerView srRecyclerView;
    private GoodsInfoCommentAdapter mGoodsInfoCommentAdapter;
    private ArrayList<EvaluateInfoBean> comments;
    private VoteCommentsHelper httpHelper;

    private int voteId;
    private int times = 1;
    private int evaluatePosition;

    private View noneNotify;
    private VoteCommentsListener listener;
    public interface VoteCommentsListener{
        void OnRefreshFinish();
    }
    public CommentsView(Context context) {
        this(context, null);
    }
    public CommentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CommentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void initData(int id, VoteCommentsListener listener){
        voteId = id;
        this.listener = listener;
        comments = new ArrayList<>();
        mGoodsInfoCommentAdapter = new GoodsInfoCommentAdapter(comments, context);
        mGoodsInfoCommentAdapter.setIsVoteComments(true);
        httpHelper = new VoteCommentsHelper(context, voteId, httpListener);

        srRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        srRecyclerView.setAdapter(mGoodsInfoCommentAdapter);
        times = 1;
        httpHelper.getCommentList(times);

        mGoodsInfoCommentAdapter.setLikeClick(new GoodsInfoCommentAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getLayoutPosition();
                if(comments.size() <= position){
                    return;
                }
                EvaluateInfoBean mEvaluateInfoBean = comments.get(position);
                int likeCount = mEvaluateInfoBean.getLikeCount();
                if (mEvaluateInfoBean.isLike()) {
                    comments.get(position).setLike(false);
                    --likeCount;
                    comments.get(position).setLikeCount(likeCount);
                    mGoodsInfoCommentAdapter.notifyItemChanged(position);
                } else {
                    evaluatePosition = position;
                    httpHelper.sendLike(comments.get(position).getStoryCommentId());
                }
            }
        });
    }

    private void initView() {
        rootView = LayoutInflater.from(context).inflate(R.layout.vote_comment_layout, null);
        noneNotify = rootView.findViewById(R.id.none_notify);
        srRecyclerView = (MyRecyclerView) rootView.findViewById(R.id.srRecyclerView);
        addView(rootView);
    }

    public void sendComments(String content) {
        httpHelper.addComments(UserSharedPreference.getUserId(), content);
    }

    public void onRefresh() {
        times = 1;
        comments.clear();
        httpHelper.getCommentList(times);
    }

    public void onListLoad() {
        ++times;
        httpHelper.getCommentList(times);
    }

    VoteCommentsHelper.CommentsHttpListener httpListener = new VoteCommentsHelper.CommentsHttpListener() {
        @Override
        public void OnGetComments(List<EvaluateInfoBean> comment, boolean isSuc) {
            hideLoading();
            if (!isSuc || comment.size() == 0) {
                if (times != 1) {
                    --times;
                    Toast.makeText(getContext(), R.string.no_more, Toast.LENGTH_SHORT).show();
                }
                if (comments != null && comments.size() > 0)
                    noneNotify.setVisibility(View.GONE);
                else
                    noneNotify.setVisibility(View.VISIBLE);
                return;
            }
            comments.addAll(comment);
            if (comments != null && comments.size() > 0)
                noneNotify.setVisibility(View.GONE);
            else
                noneNotify.setVisibility(View.VISIBLE);
            mGoodsInfoCommentAdapter.notifyDataSetChanged();
        }

        @Override
        public void AddCommentsResult(boolean isSuc) {
            Toast.makeText(getContext(), "评论成功！", Toast.LENGTH_SHORT).show();
            comments.clear();
            httpHelper.getCommentList(1);
        }

        @Override
        public void likeSuccess() {
            int likeCount = comments.get(evaluatePosition).getLikeCount();
            comments.get(evaluatePosition).setLike(true);
            ++likeCount;
            comments.get(evaluatePosition).setLikeCount(likeCount);
            mGoodsInfoCommentAdapter.notifyItemChanged(evaluatePosition);
        }
    };

    private void hideLoading() {
        listener.OnRefreshFinish();
    }

    public void setHashMap(HashMap<Integer, Integer> hashMap){
        mGoodsInfoCommentAdapter.setHashMap(hashMap);
        mGoodsInfoCommentAdapter.notifyDataSetChanged();
    }
}
