package com.travel.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.ShopConstant;
import com.travel.activity.HomeStoryMoreAcrivity;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.GoodsBasicInfoBean;
import com.travel.bean.NotifyBean;
import com.travel.bean.VideoInfoBean;
import com.travel.entity.HomePageBean;
import com.travel.entity.PublicVoteEntity;
import com.travel.entity.VoteBean;
import com.travel.layout.MyRecyclerView;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.pk.others.VoteInfoHelper;
import com.travel.video.activitys_notice.GoodsInfo;
import com.travel.video.bean.RouteBean;
import com.travel.video.fragment.VideoListFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/4.
 */
public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<HomePageBean> listData;
    private ClickListener clickListener;

    public interface ClickListener{
        void onClickStory(GoodsBasicInfoBean bean);
        void onClickLive(View itemView, VideoInfoBean bean);
        void onClickActivitys(NotifyBean bean);
        void onClickRoute(RouteBean bean);
        void onClickVote(PublicVoteEntity bean);
    }

    public HomeAdapter(Context context, List<HomePageBean> listData) {
        this.mContext = context;
        this.listData = listData;
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_home_fragment, parent, false);
        ViewHolderStory holder = new ViewHolderStory(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(listData.size() <= position) return;

        final HomePageBean entity = listData.get(position);
        final ViewHolderStory mHolder = (ViewHolderStory) holder;
        mHolder.vote_status.setVisibility(View.GONE);
        mHolder.tv_mark.setText(entity.getTitle());
        switch (entity.getShowType()) {
            case HomePageBean.TYPE_STORY:
                mHolder.iv_mark.setImageResource(R.drawable.icon_home_mark_story);
                final GoodsBasicInfoBean goodsBean = (GoodsBasicInfoBean) listData.get(position).getObj();
                showLayout(mHolder, mHolder.story);

                ImageDisplayTools.displayImage(goodsBean.getGoodsImg(), mHolder.stiry_cover);
                Animation mAnimation = AnimationUtils.loadAnimation(mContext,R.anim.balloonscale);
                mHolder.stiry_cover.setAnimation(mAnimation );
                mAnimation.start();
                mHolder.story_title.setText(goodsBean.getGoodsTitle());
                mHolder.story_content.setText(goodsBean.getSubhead());
                mHolder.story_lable.setText(goodsBean.getLabel());
                mHolder.story_lable.setVisibility(View.VISIBLE);
                if(goodsBean.getLabel().isEmpty()){
                    mHolder.story_lable.setVisibility(View.GONE);
                }
                if(TextUtils.equals(goodsBean.getGoodsId() ,"0")){
                    mHolder.story_status.setVisibility(View.GONE);
                }else{
                    mHolder.story_status.setVisibility(View.VISIBLE);
                }
                mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onClickStory(goodsBean);
                    }
                });
                break;
            case HomePageBean.TYPE_STORY_MORE:
                mHolder.iv_mark.setImageResource(R.drawable.icon_home_mark_story_more);
                showLayout(mHolder, mHolder.story_more);
                LinearLayoutManager manager = new LinearLayoutManager(mContext);
                mHolder.story_recyclerview.setLayoutManager(manager);
                ArrayList<GoodsBasicInfoBean> storys = (ArrayList<GoodsBasicInfoBean>) listData.get(position).getObj();
                if(listData.get(position).getIsShowTitle() > 0 && listData.get(position).getIsShowTitle() < storys.size()){
                    ArrayList<GoodsBasicInfoBean> arrayList = new ArrayList<>();
                    arrayList.addAll(storys.subList(0, listData.get(position).getIsShowTitle()));
                    storys.clear();
                    storys.addAll(arrayList);
                }
                HomePageStoryAdapter homePageStoryAdapter = new HomePageStoryAdapter(mContext, storys);
                homePageStoryAdapter.setClickListener(new HomePageStoryAdapter.ClickListener() {
                    @Override
                    public void onClick(GoodsBasicInfoBean bean) {
                        clickListener.onClickStory(bean);
                    }
                });
                mHolder.story_recyclerview.setAdapter(homePageStoryAdapter);
                homePageStoryAdapter.notifyDataSetChanged();

                mHolder.rl_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, HomeStoryMoreAcrivity.class);
                        intent.putExtra("groupId", entity.getId());
                        mContext.startActivity(intent);
                    }
                });
                break;

            case HomePageBean.TYPE_LIVE:
                showLayout(mHolder, mHolder.live);
            case HomePageBean.TYPE_LIVE_MORE:
                showLayout(mHolder, mHolder.live_more);
                mHolder.iv_mark.setImageResource(R.drawable.icon_home_mark_video);
                ArrayList<VideoInfoBean> videoList = (ArrayList<VideoInfoBean>) entity.getObj();
                LinearLayoutManager videoManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                mHolder.live_recyclerview.setLayoutManager(videoManager);
                HomePageVideoAdapter homePageVideoAdapter = new HomePageVideoAdapter(mContext, videoList);
                homePageVideoAdapter.setClickListener(new HomePageVideoAdapter.ClickListener() {
                    @Override
                    public void onClick(VideoInfoBean bean) {
                        clickListener.onClickLive(mHolder.itemView, bean);
                    }

                    @Override
                    public void onClickMore() {
                        Bundle bundles = new Bundle();
                        bundles.putString("videoType", VideoListFragment.INTENT_MORE);
                        bundles.putInt("groupId", entity.getId());
                        OneFragmentActivity.startNewActivity(mContext, "更多", VideoListFragment.class, bundles);
                    }
                });
                mHolder.live_recyclerview.setAdapter(homePageVideoAdapter);
                homePageVideoAdapter.notifyDataSetChanged();
                break;

            case HomePageBean.TYPE_VOTE:
                showLayout(mHolder, mHolder.vote);
                mHolder.iv_mark.setImageResource(R.drawable.icon_home_mark_vote);

                final PublicVoteEntity voteEntity = (PublicVoteEntity) entity.getObj();
                if(voteEntity == null)
                    return;
                if(voteEntity.getBuyer() == null)
                    return;
                mHolder.vote_title.setText(voteEntity.getTitle());
                mHolder.vote_content.setText(voteEntity.getReason());
                mHolder.vote_left_name.setText(voteEntity.getBuyer().getNickName());
                mHolder.vote_right_name.setText(voteEntity.getSeller().getNickName());
                ImageDisplayTools.displayHeadImage(voteEntity.getBuyer().getImgUrl(), mHolder.vote_left_head);
                ImageDisplayTools.displayHeadImage(voteEntity.getSeller().getImgUrl(), mHolder.vote_right_head);
                mHolder.vote_status.setVisibility(View.VISIBLE);
                mHolder.vote_status.setText("处理中...");
                if (voteEntity.getStatus() == 0)
                    mHolder.vote_status.setText("待发布");
                    // 已发布众投
                else if (voteEntity.getStatus() == 1) {
                    // 审核中
                    if (voteEntity.getCheckStatus() == 0)
                        mHolder.vote_status.setText("等待平台审核");
                    else if (voteEntity.getCheckStatus() == 1) {
                        mHolder.vote_status.setText("众投中");
                    } else if (voteEntity.getCheckStatus() == 2)
                        mHolder.vote_status.setText("审核失败");
                    // 进行中 即 审核通过
                } else if (voteEntity.getStatus() == 2) {
                    mHolder.vote_status.setText("众投中");
                    // 已结束
                }else if (voteEntity.getStatus() == 3) {
                    mHolder.vote_status.setText("已结束");
                } else if (voteEntity.getStatus() == -1) {
                    mHolder.vote_status.setText("已结束");
                }
                mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onClickVote(voteEntity);
                    }
                });

                if(!UserSharedPreference.isLogin()){
                    mHolder.vote_left_zan.setImageResource(R.drawable.icon_hand_white);
                    mHolder.vote_right_zan.setImageResource(R.drawable.icon_hand_white);
                }else if(mHolder.vote_left_zan.getTag() == null){
                    getVoteStatus(voteEntity, mHolder.vote_left_zan, mHolder.vote_right_zan, null);
                }
                mHolder.vote_left_zan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!UserSharedPreference.isLogin()){
                            mHolder.vote_left_zan.setImageResource(R.drawable.icon_hand_white);
                            mHolder.vote_right_zan.setImageResource(R.drawable.icon_hand_white);
//                            Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
                            OSUtil.intentLogin(mContext);
                            return;
                        }
                        getVoteStatus(voteEntity, mHolder.vote_left_zan, mHolder.vote_right_zan, voteEntity.getBuyerId());
                    }
                });
                mHolder.vote_right_zan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!UserSharedPreference.isLogin()){
                            mHolder.vote_left_zan.setImageResource(R.drawable.icon_hand_white);
                            mHolder.vote_right_zan.setImageResource(R.drawable.icon_hand_white);
//                            Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
                            OSUtil.intentLogin(mContext);
                            return;
                        }
                        getVoteStatus(voteEntity, mHolder.vote_left_zan, mHolder.vote_right_zan, voteEntity.getSellerId());
                    }
                });


                break;
            case HomePageBean.TYPE_ACTIVITY:
                showLayout(mHolder, mHolder.activitys);
                mHolder.iv_mark.setImageResource(R.drawable.icon_home_mark_activitys);
                final NotifyBean notifyBean = (NotifyBean) entity.getObj();
                ImageDisplayTools.disPlayRoundDrawable(notifyBean.getImgUrl(), mHolder.activity_cover, OSUtil.dp2px(mContext, 2));
                mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onClickActivitys(notifyBean);
                    }
                });
                break;
            case HomePageBean.TYPE_ROUTE:
                showLayout(mHolder, mHolder.route);

                break;
        }
    }

    private void getVoteStatus(final PublicVoteEntity bean, final ImageView iv_left, final ImageView iv_right, final String userId) {
        String url = ShopConstant.VOTE_STATUS;
        Map<String, Object> map = new HashMap<>();
        map.put("publicVoteId", bean.getId());
        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                if(response.optInt("error") == 0){
                    String selectedId = response.optJSONObject("data").optString("selectedId");
                    if(TextUtils.equals(bean.getBuyerId(), selectedId)){
                        iv_left.setTag(selectedId);
                        iv_left.setImageResource(R.drawable.icon_hand_red);
                        iv_right.setImageResource(R.drawable.icon_hand_white);
                        return;
                    }

                    if(TextUtils.equals(bean.getSellerId(), selectedId)){
                        iv_left.setTag(selectedId);
                        iv_right.setImageResource(R.drawable.icon_hand_red);
                        iv_left.setImageResource(R.drawable.icon_hand_white);
                        return;
                    }
                }else if(response.optInt("error") == 1 && TextUtils.equals("not vote", response.optString("msg"))){
                    iv_right.setImageResource(R.drawable.icon_hand_white);
                    iv_left.setImageResource(R.drawable.icon_hand_white);
                    if(userId != null){
                        new VoteInfoHelper(mContext, Integer.parseInt(bean.getId()), new VoteInfoHelper.VoteHttpListener() {
                            @Override
                            public void OnVoteDetail(VoteInfoHelper.VoteDetailsInfo info) {
                            }

                            @Override
                            public void OnVotedUsers(HashMap<Integer, Integer> userMap) {
                            }

                            @Override
                            public void OnEvidencePackets(final JSONArray data) {
                            }

                            @Override
                            public void onPublishVoteResult(boolean isSuccess) {
                            }

                            @Override
                            public void onVoteResult(boolean isSuccess) {
                                if(TextUtils.equals(bean.getBuyerId(), userId)){
                                    iv_left.setTag(userId);
                                    iv_left.setImageResource(R.drawable.icon_hand_red);
                                    iv_right.setImageResource(R.drawable.icon_hand_white);
                                    return;
                                }

                                if(TextUtils.equals(bean.getSellerId(), userId)){
                                    iv_left.setTag(userId);
                                    iv_right.setImageResource(R.drawable.icon_hand_red);
                                    iv_left.setImageResource(R.drawable.icon_hand_white);
                                    return;
                                }
                            }
                        }).getIsVoted(userId);
                    }
                }
            }
        }, map);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
    private void showLayout(ViewHolderStory holder ,View view){
        holder.story.setVisibility(View.GONE);
        holder.story_more.setVisibility(View.GONE);
        holder.live.setVisibility(View.GONE);
        holder.live_more.setVisibility(View.GONE);
        holder.vote.setVisibility(View.GONE);
        holder.activitys.setVisibility(View.GONE);
        holder.route.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }

    class ViewHolderStory extends RecyclerView.ViewHolder{
        private ImageView iv_mark;
        private TextView tv_mark, vote_status;
        private View story, story_more, live, live_more, vote, activitys, route;

        // STORY
        private ImageView stiry_cover;
        private TextView story_title, story_content, story_status, story_lable;

        // STORY_MORE
        private MyRecyclerView story_recyclerview;
        private LinearLayout rl_more;
        // LIVE
        // LIVE_MORE
        private RecyclerView live_recyclerview;
        // VOTE
        private TextView vote_title, vote_content, vote_vote_num, vote_left_name, vote_right_name;
        private ImageView vote_left_head, vote_left_zan, vote_right_head, vote_right_zan;
        // ACTIVITYS
        private ImageView activity_cover;
        // ROUTE

        public ViewHolderStory(View itemView) {
            super(itemView);
            tv_mark = (TextView) itemView.findViewById(R.id.tv_mark);
            iv_mark = (ImageView) itemView.findViewById(R.id.iv_mark);
            vote_status = (TextView) itemView.findViewById(R.id.tv_status);

            // STORY
            story = itemView.findViewById(R.id.story);
            stiry_cover = (ImageView) story.findViewById(R.id.iv_cover);
            story_title = (TextView) story.findViewById(R.id.tv_title);
            story_content = (TextView) story.findViewById(R.id.tv_content);
            story_status = (TextView) story.findViewById(R.id.tv_status);
            story_lable = (TextView) story.findViewById(R.id.tv_lable);

            // STORY_MORE
            story_more = itemView.findViewById(R.id.story_more);
            story_recyclerview = (MyRecyclerView) story_more.findViewById(R.id.story_recyclerview);
            rl_more = (LinearLayout) story_more.findViewById(R.id.rl_more);
            // LIVE
            live = itemView.findViewById(R.id.live);
            // LIVE_MORE
            live_more = itemView.findViewById(R.id.live_more);
            live_recyclerview = (RecyclerView) live_more.findViewById(R.id.live_recyclerview);
            // VOTE
            vote = itemView.findViewById(R.id.vote);
            vote_title = (TextView) vote.findViewById(R.id.tv_title);
            vote_content = (TextView) vote.findViewById(R.id.tv_content);
            vote_vote_num = (TextView) vote.findViewById(R.id.tv_vote_num);
            vote_left_name = (TextView) vote.findViewById(R.id.tv_left_name);
            vote_right_name = (TextView) vote.findViewById(R.id.tv_right_name);
            vote_left_head = (ImageView) vote.findViewById(R.id.iv_left_head);
            vote_left_zan = (ImageView) vote.findViewById(R.id.iv_left_zan);
            vote_right_head = (ImageView) vote.findViewById(R.id.iv_right_head);
            vote_right_zan = (ImageView) vote.findViewById(R.id.iv_right_zan);
            // ACTIVITYS
            activitys = itemView.findViewById(R.id.activitys);
            activity_cover = (ImageView) activitys.findViewById(R.id.iv_cover);
            // ROUTE
            route = itemView.findViewById(R.id.route);

        }
    }
}
