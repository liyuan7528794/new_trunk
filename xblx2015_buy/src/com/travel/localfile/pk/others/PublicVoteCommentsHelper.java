package com.travel.localfile.pk.others;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.Constants;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.communication.entity.UserData;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.PopWindowUtils;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 众投界面的评论模块
 * Created by ldkxingzhe on 2016/7/11.
 */
public class PublicVoteCommentsHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "PublicVoteCommentsHelper";

    private int mPublicVoteId = -1;
    private String mUserId;

    private PullToRefreshListView mPullToRefreshListView;
    private ListView mCommentsListView;
    private Button mSendBtn;
    private EditText mEditTextEditText;
    private PullToRefreshHelper mPullToRefreshHelper;

    private List<CommentEntity> mCommentsList = new ArrayList<CommentEntity>();
    private HttpRequest mHttpRequest;
    private CommentsAdapter mAdapter;

    private Activity mContext;
    private Handler mHandler;
    private int mBottomLastValue;

    /** 设置基本信息 */
    public void setInfo(int publicVoteId, String userId){
        mPublicVoteId = publicVoteId;
        mUserId = userId;
        getComments();
    }

    private void getComments() {
        if(TextUtils.isEmpty(mUserId) || mPublicVoteId != -1){
            mHttpRequest.getCommentList(mPublicVoteId);
        }
    }

    public PublicVoteCommentsHelper(Activity activity, PullToRefreshListView commentListView, Button sendBtn, EditText editText) {
        mHttpRequest = new HttpRequest(activity);
        mAdapter = new CommentsAdapter(mCommentsList);
        mPullToRefreshListView = commentListView;
        mCommentsListView = mPullToRefreshListView.getRefreshableView();
        mSendBtn = sendBtn;
        mEditTextEditText = editText;
        mContext = activity;
        mCommentsListView.setAdapter(mAdapter);
        initBottomBarListener();
        mPullToRefreshHelper = new PullToRefreshHelper(commentListView);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshHelper.initPullUpToRefreshView(null);
        mPullToRefreshHelper.initPullDownToRefreshView(null);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mHttpRequest.getCommentList(mPublicVoteId);
            }
        });
        mHandler = new Handler();
        mCommentsListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < mBottomLastValue){
                    // 开始键盘弹出
                    mHandler.removeCallbacks(mScrollToStart);
                    mHandler.postDelayed(mScrollToStart, 300);
                }
                mBottomLastValue = bottom;
            }
        });
    }

    /**
     * 消息队列滑动顶端
     * */
    public void scrollToStart(){
        if(mCommentsListView == null) return;
        mCommentsListView.setSelection(0);
    }


    private void initBottomBarListener() {
        mEditTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if(TextUtils.isEmpty(s)){
//                    mSendBtn.setBackgroundResource(R.drawable.send_barrage_grey);
//                }else{
//                    mSendBtn.setBackgroundResource(R.drawable.send_barrage_icon);
//                }
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mEditTextEditText.getText())){
                    Toast.makeText(mContext, "不能发送空消息", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialogUtils.needLoginOperator(mContext, new Runnable() {
                    @Override
                    public void run() {
                        String comments = mEditTextEditText.getText().toString().trim();
                        mHttpRequest.addComments(mPublicVoteId, mUserId, comments);
                        mEditTextEditText.setText("");
                        CommentEntity entity = new CommentEntity();
                        entity.userData = new UserData();
                        entity.userData.setId(UserSharedPreference.getUserId());
                        entity.userData.setImgUrl(UserSharedPreference.getUserHeading());
                        entity.userData.setNickName(UserSharedPreference.getNickName());
                        entity.time = DateFormatUtil.formatTime(new Date(), DateFormatUtil.FORMAT_TIME);
                        entity.comments = comments;
                        mCommentsList.add(0,entity);
                        mAdapter.notifyDataSetChanged();
                        scrollToStart();
                    }
                });
            }
        });
    }

    private void showFollowWindow(String userId, String nickName, String imgUrl){
        // 显示关注窗口
        PopWindowUtils.followPopUpWindow(mContext, userId, nickName, imgUrl, 1);
    }

    private class CommentsAdapter extends ListBaseAdapter<CommentEntity>{

        public CommentsAdapter(List<CommentEntity> list) {
            super(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.video_comment_popwindow_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final CommentEntity commentEntity = getItem(position);
            ImageDisplayTools.displayHeadImage(commentEntity.userData.getImgUrl(), viewHolder.headerImage);
            viewHolder.nickName.setText(commentEntity.userData.getNickName());
            viewHolder.comments.setText(commentEntity.comments);
            viewHolder.time.setText(commentEntity.time);
            viewHolder.headerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserData userData = commentEntity.userData;
                    showFollowWindow(userData.getId(), userData.getNickName(), userData.getImgUrl());
                }
            });
            return convertView;
        }

        private class ViewHolder{
            ImageView headerImage;
            TextView nickName;
            TextView comments;
            TextView time;
            public ViewHolder(View convertView){
                headerImage = (ImageView) convertView.findViewById(R.id.commentHeadImg);
                nickName = (TextView) convertView.findViewById(R.id.commentUserName);
                comments = (TextView) convertView.findViewById(R.id.commentContent);
                time = (TextView) convertView.findViewById(R.id.commentTime);
            }
        }
    }

    private class HttpRequest{

        private Context context;

        public HttpRequest(Context context){
            this.context = context;
        }
        /**
         * 获取评论列表的网络请求
         * @param publicVoteId
         */
        public void getCommentList(int publicVoteId){
            String url = Constants.Root_Url + "/publicVoteComment/CommentList.do";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("publicvoteid", publicVoteId);
            NetWorkUtil.postForm(context, url, new MResponseListener() {

                @Override
                protected void onDataFine(JSONArray data) {
                    List<CommentEntity> commentEntities = new ArrayList<CommentEntity>();
                    for(int i = 0, length = data.length(); i < length; i++){
                        JSONObject jsonObject = JsonUtil.getJSONObject(data, i);
                        CommentEntity entity = new CommentEntity();
                        entity.id = JsonUtil.getJsonInt(jsonObject, "id");
                        entity.comments = JsonUtil.getJson(jsonObject, "context");
                        entity.time = JsonUtil.getJson(jsonObject, "time");
                        try {
                            JSONObject userData = (JSONObject) jsonObject.get("commentUser");
                            entity.userData = UserData.generateUserData(userData);
                            commentEntities.add(entity);
                        } catch (JSONException e) {
                            MLog.e(TAG, e.getMessage(), e);
                        }
                    }
//                    Collections.reverse(commentEntities);
                    onGetCommentList(commentEntities);
                }
            }, map);
        }

        /**  添加评论  */
        public void addComments(int publicVoteId, String userId, String comments){
            String url = Constants.Root_Url + "/orders/addComment.do";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("publicvoteid", publicVoteId);
            /** 这里不需要添加用户Id? */
//            map.put("")
            map.put("Context", comments);
            NetWorkUtil.postForm(context, url, new MResponseListener() {
                @Override
                protected void onDataFine(JSONObject data) {

                }
            }, map);
        }

        /** 网络回调区 */
        // 获取网络评价列表
        private void onGetCommentList(List<CommentEntity> list){
            if(mPullToRefreshListView != null){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshListView.onRefreshComplete();
                    }
                }, 2000);
            }
            mCommentsList.clear();
            mCommentsList.addAll(list);
            mAdapter.notifyDataSetChanged();
        }

        // success --> true 成功添加
        private void onPostComments(boolean success){

        }
    }

    private class CommentEntity{
        int id;
        String time;
        String comments;
        UserData userData;
    }

    /* 键盘弹出， 上滑动到评论顶部 */
    private Runnable mScrollToStart = new Runnable() {
        @Override
        public void run() {
            MLog.d(TAG, "mScrollToStart, and position 0");
            mCommentsListView.smoothScrollToPosition(0);
        }
    };
}
