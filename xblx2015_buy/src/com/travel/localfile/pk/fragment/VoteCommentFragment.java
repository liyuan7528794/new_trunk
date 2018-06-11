package com.travel.localfile.pk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.mylhyl.crlayout.SwipeRefreshAdapterView;
import com.mylhyl.crlayout.SwipeRefreshRecyclerView;
import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.pk.others.VoteCommentsHelper;
import com.travel.shop.adapter.GoodsInfoCommentAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/2/9.
 */

public class VoteCommentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SwipeRefreshAdapterView.OnListLoadListener {
    private View rootView;

    private EditText editText;
    private TextView tv_send, write;
    private LinearLayout ll_send;

    private SwipeRefreshRecyclerView srRecyclerView;
    private GoodsInfoCommentAdapter mGoodsInfoCommentAdapter;
    private ArrayList<EvaluateInfoBean> comments;
    private VoteCommentsHelper httpHelper;

    private int voteId;
    private int times = 1;
    private int evaluatePosition;

    private View noneNotify;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        voteId = getArguments().getInt(VoteFragment.BUNDLE_VOTE_ID);
        rootView = inflater.inflate(R.layout.vote_comment_fragment, null);
        comments = new ArrayList<>();
        mGoodsInfoCommentAdapter = new GoodsInfoCommentAdapter(comments, getActivity());
        mGoodsInfoCommentAdapter.setIsVoteComments(true);
        httpHelper = new VoteCommentsHelper(getActivity(), voteId, httpListener);

        initView();
        mGoodsInfoCommentAdapter.setLikeClick(new GoodsInfoCommentAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getLayoutPosition();
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
        return rootView;
    }

    private void initView() {
        editText = (EditText) rootView.findViewById(R.id.et_comment_edit);
        tv_send = (TextView) rootView.findViewById(R.id.tv_comment_send);
        write = (TextView) rootView.findViewById(R.id.tv_write);
        ll_send = (LinearLayout) rootView.findViewById(R.id.ll_comment_edit);
        noneNotify = rootView.findViewById(R.id.none_notify);
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String src = editText.getText().toString().trim();
                if (src == null || "".equals(src)) {
                    Toast.makeText(getContext(), "请输入内容！", Toast.LENGTH_SHORT).show();
                    return;
                }
                OSUtil.hideKeyboard(getActivity());
                httpHelper.addComments(UserSharedPreference.getUserId(), src);
                editText.setText("");
                ll_send.setVisibility(View.GONE);
                write.setVisibility(View.VISIBLE);
            }
        });
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write.setVisibility(View.GONE);
                ll_send.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        srRecyclerView = (SwipeRefreshRecyclerView) rootView.findViewById(R.id.srRecyclerView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        srRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        srRecyclerView.setOnListLoadListener(this);
        srRecyclerView.setOnRefreshListener(this);
        srRecyclerView.setAdapter(mGoodsInfoCommentAdapter);
        times = 1;
        httpHelper.getCommentList(times);
    }

    @Override
    public void onRefresh() {
        times = 1;
        comments.clear();
        httpHelper.getCommentList(times);
    }

    @Override
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
                return;
            }
            comments.addAll(comment);
            if (comments != null && comments.size() > 0)
                noneNotify.setVisibility(View.GONE);
            else
                noneNotify.setVisibility(View.VISIBLE);
            mGoodsInfoCommentAdapter.notifyDataSetChanged();
            if (comment.size() < 10)
                srRecyclerView.setEnabledLoad(false);
            else
                srRecyclerView.setEnabledLoad(true);
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
        srRecyclerView.setRefreshing(false);
        srRecyclerView.setLoading(false);
    }

}
