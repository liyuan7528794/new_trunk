package com.travel.localfile.pk.others;

import android.content.Context;

import com.travel.Constants;
import com.travel.VideoConstant;
import com.travel.bean.EvaluateInfoBean;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/9.
 */

public class VoteCommentsHelper {
    private final static String TAG = "VoteCommentsHelper";
    private Context context;
    private int voteId;
    private CommentsHttpListener listener;
    public interface CommentsHttpListener{
        void OnGetComments(List<EvaluateInfoBean> comments, boolean isSuc);
        void AddCommentsResult(boolean isSuc);
        void likeSuccess();
    }
    public VoteCommentsHelper(Context context, int voteId, CommentsHttpListener listener){
        this.context = context;
        this.voteId = voteId;
        this.listener = listener;
    }
    /**
     * 获取评论列表的网络请求
     */
    public void getCommentList(int times){
        String url = Constants.Root_Url + "/publicVoteComment/CommentList.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        map.put("pageNo", times);
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                List<EvaluateInfoBean> commentEntities = new ArrayList<EvaluateInfoBean>();
                for(int i = 0, length = data.length(); i < length; i++){
                    JSONObject jsonObject = JsonUtil.getJSONObject(data, i);
                    EvaluateInfoBean entity = new EvaluateInfoBean();
                    entity.setStoryCommentId(JsonUtil.getJson(jsonObject, "id"));
                    entity.setEvaluateUserId(JsonUtil.getJson(jsonObject, "userId"));
                    entity.setEvaluateContent(JsonUtil.getJson(jsonObject, "context"));
                    entity.setEvaluateTime(JsonUtil.getJson(jsonObject, "time"));
                    entity.setLikeCount(JsonUtil.getJsonInt(jsonObject, "praiseNum"));
                    try {
                        JSONObject userData = (JSONObject) jsonObject.get("commentUser");
                        // 头像
                        entity.setEvaluateUserPhoto(userData.optString("imgUrl"));
                        // 名字
                        entity.setEvaluateUserName(userData.optString("nickName"));
                    } catch (JSONException e) {
                        
                    }
                    commentEntities.add(entity);
                }
                listener.OnGetComments(commentEntities,true);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.OnGetComments(null,false);
            }
        }, map);
    }

    /**  添加评论  */
    public void addComments(String userId, String comments){
        String url = Constants.Root_Url + "/orders/addComment.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", voteId);
        /** 这里不需要添加用户Id? */
//            map.put("")
        map.put("Context", comments);
        NetWorkUtil.postForm(context, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.AddCommentsResult(true);
                }
            }
        }, map);
    }

    /**
     * 点赞
     */
    public void sendLike(String commentId) {
        Map map = new HashMap<>();
        map.put("id", commentId);
        NetWorkUtil.postForm(context, VideoConstant.VOTE_COMMENT_LIKE, new MResponseListener(context) {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0)
                    listener.likeSuccess();
            }
        }, map);
    }
}
