package com.travel.helper;

import android.content.Context;

import com.travel.ShopConstant;
import com.travel.VideoConstant;
import com.travel.app.TravelApp;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.shop.bean.CommentBean;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/7.
 */

public class TalkCommentsHelper {
    private final static String TAG = "TalkCommentsHelper";
    public final static int TYPE_TALK = 1;
    public final static int TYPE_STORY = 2;
    public final static int TYPE_VIDEO = 3;
    private Context context;
    private CommentsHttpListener listener;
    public interface CommentsHttpListener{
        void OnGetComments(List<CommentBean> comments, boolean isSuc);
        void AddCommentsResult(boolean isSuc);
        void likeSuccess();
    }
    public TalkCommentsHelper(CommentsHttpListener listener){
        this.context = TravelApp.appContext;
        this.listener = listener;
    }
    /**
     * 获取评论列表的网络请求
     */
    public void getCommentsList(int pageNo, String businessId, int businessType, int pid){
        String url = ShopConstant.COMMMENTS_LIST;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("pageNo", pageNo);
        map.put("businessId", businessId);
        map.put("businessType", businessType);
        map.put("pid", pid);
        NetWorkUtil.postForm(context, url, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                List<CommentBean> list = new ArrayList<>();
                if(data != null){
                    list = JsonUtil.parseJsonArrayWithGson(data.toString(), CommentBean.class);
                }
                listener.OnGetComments(list,true);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                listener.OnGetComments(new ArrayList<CommentBean>(),false);
            }
        }, map);
    }

    /**  添加评论  */
    /**
     *
     * @param userId 自己的id
     * @param businessId 所属说说或故事的id
     * @param content 内容
     * @param businessType  类型 3种
     * @param toname 要评论的人的id（评论对象的）
     * @param pId 要评论的评论的id
     */
    public void addComments(String userId, String businessId,
                                      String content, int businessType,
                                      String toname, String pId){
        String url = ShopConstant.TALK_COMMMENTS_ADD;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("businessId", businessId);
        map.put("content", content);
        map.put("businessType", businessType);
        map.put("toname", toname);
        map.put("pid", pId);
        NetWorkUtil.postForm(context, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    listener.AddCommentsResult(true);
                }else{
                    listener.AddCommentsResult(false);
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
