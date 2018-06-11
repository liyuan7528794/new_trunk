package com.travel.video.layout;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.travel.Constants;
import com.travel.VideoConstant;
import com.travel.layout.BaseBellowPopupWindow;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点评向上弹窗
 *
 * @author Administrator
 */
public class VideoCommentPopupWindow extends BaseBellowPopupWindow {
    private View rootView;
    private Activity activity;
    private String videoId = "";
    private int times = 1;
    private int commentNum = 0;

    private TextView commentNumText;
    private ImageButton hideCommentWindow;
    private EditText commentContentEdit;
    private Button commentSendButton;
    private TextView tv_no_comment;

    private ListView listView;
    private PullToRefreshListView pullRefreshListView;
    private List<Map<String, String>> cList;
    private VideoCommentListAdapter myAdapter;


    private boolean isTik;// 判断是否是从仿抖音页进入的
    public VideoCommentPopupWindow(final Activity activity, String videoId, int commentNum, boolean isTik) {
        super(activity);
        this.activity = activity;
        this.videoId = videoId;
        this.commentNum = commentNum;
        this.isTik = isTik;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.video_comment_popwindow, null);
        tv_no_comment = (TextView) rootView.findViewById(R.id.tv_no_comment);
        initListView();

        SetContentView(rootView);
        show();

        getComments();
    }

    private void initListView() {
        pullRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.commentWindowList);
        commentNumText = (TextView) rootView.findViewById(R.id.commentNumWindow);
        hideCommentWindow = (ImageButton) rootView.findViewById(R.id.hideCommentWindow);
        commentContentEdit = (EditText) rootView.findViewById(R.id.comment_content_edit);
        commentSendButton = (Button) rootView.findViewById(R.id.comment_send_button);

        commentNumText.setText("点评(" + commentNum + ")");

        hideCommentWindow.setOnClickListener(new OnClickListener() {
            //隐藏弹窗
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        commentSendButton.setOnClickListener(new commentSendListener());
        cList = new ArrayList<Map<String, String>>();
        pullRefreshListView.setMode(Mode.BOTH);
        // Set a listener to be invoked when the list should be refreshed.
        pullRefreshListView.setOnRefreshListener(new OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase refreshView) {
                String label = DateUtils.formatDateTime(
                        activity.getApplicationContext(),
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                times = 1;
                cList.clear();
                getComments();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                String label = DateUtils.formatDateTime(
                        activity.getApplicationContext(),
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                getComments();
            }
        });

        listView = pullRefreshListView.getRefreshableView();

        myAdapter = new VideoCommentListAdapter(activity, cList);
        listView.setAdapter(myAdapter);

        listView.setDivider(null);
    }

    private class commentSendListener implements OnClickListener {
        //发送点评
        @Override
        public void onClick(View v) {
            sendComment();
        }

    }

    /**
     * 发送评论
     */
    private void sendComment() {

        OSUtil.hideKeyboardPopWindow(activity);

        if (!UserSharedPreference.isLogin()) {
            Toast.makeText(activity, "请登录之后再操作", Toast.LENGTH_SHORT).show();
            return;
        }

        if (commentContentEdit.getText() == null || "".equals(commentContentEdit.getText().toString().trim())) {
            Toast.makeText(activity, "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }

        final Map<String, Object> paramap = new HashMap<String, Object>();
        String content = commentContentEdit.getText().toString();
        String url;
        paramap.put("content", content);
        paramap.put("userId", UserSharedPreference.getUserId());
        if (isTik) {
            paramap.put("cityVideoId", videoId);
            url = Constants.Root_Url + "/user/shortVideoComment.do";
        } else {
            paramap.put("videoId", videoId);
            paramap.put("nickName", UserSharedPreference.getNickName());
            paramap.put("imgUrl", UserSharedPreference.getUserHeading());
            url = Constants.Root_Url + "/live/addLiveVideoComment.do";
        }
        NetWorkUtil.postForm(activity, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                if (response.optInt("error") == 0) {
                    commentContentEdit.setText("");
                    commentNum = commentNum + 1;
                    commentNumText.setText("点评(" + commentNum + ")");

                    cList.clear();
                    getComments();
                    Toast.makeText(activity, "点评成功", Toast.LENGTH_SHORT).show();
                    if (updateCommentCountListener != null) {
                        updateCommentCountListener.updateCommentCount(commentNum);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
            }
        }, paramap);
    }

    private void getComments() {
        if (cList != null && cList.size() > 0 && cList.size() % Constants.ItemNum == 0) {
            times = times + 1;
            loadingData();
        } else if (cList != null && cList.size() > 0 && cList.size() % Constants.ItemNum != 0) {
            pullRefreshListView.onRefreshComplete();
        } else if (cList != null && cList.size() == 0) {
            times = 1;
            loadingData();
        }
    }

    /**
     * 加载评论
     */
    private void loadingData() {
        Map<String, Object> paramap = new HashMap<String, Object>();
        String url;
        if (isTik) {
            paramap.put("cityVideoId", videoId);
            paramap.put("pageNo", times);
            url = VideoConstant.TIK_VIDEO_COMMENT_DATA;
        } else {
            paramap.put("videoId", videoId);
            paramap.put("times", times);
            url = VideoConstant.VIDEO_COMMENT_DATA;
        }
        NetWorkUtil.postForm(activity, url, new MResponseListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && 0 == response.getInt("error") && "OK".equals(response.get("msg"))) {

                        JSONArray cJsonArray = response.getJSONArray("data");
                        if (cJsonArray != null && cJsonArray.length() > 0) {
                            for (int i = 0; i < cJsonArray.length(); i++) {
                                JSONObject cJson = cJsonArray.getJSONObject(i);
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("content", JsonUtil.getJson(cJson, "content"));
                                map.put("submitTime", JsonUtil.getJson(cJson, "submitTime"));
                                map.put("videoId", JsonUtil.getJson(cJson, isTik ? "cityVideoId" : "videoId"));

                                JSONObject userJson = cJson.getJSONObject("user");
                                map.put("userId", JsonUtil.getJson(userJson, "userId"));
                                map.put("nickName", JsonUtil.getJson(userJson, "nickName"));
                                map.put("imgUrl", JsonUtil.getJson(userJson, "imgUrl"));
                                cList.add(map);
                            }
                        }
                    }

                    if (cList.size() > 0) {
                        tv_no_comment.setVisibility(View.GONE);
                    } else {
                        tv_no_comment.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    pullRefreshListView.onRefreshComplete();
                    if (cList.size() > 0 && cList.size() % Constants.ItemNum == 0)
                        pullRefreshListView.setMode(Mode.BOTH);
                    else
                        pullRefreshListView.setMode(Mode.PULL_FROM_START);
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            protected void onNetComplete() {
                super.onNetComplete();
                pullRefreshListView.onRefreshComplete();
            }
        }, paramap);
    }

    private class VideoCommentListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<Map<String, String>> list;

        public VideoCommentListAdapter(Context context, List<Map<String, String>> list) {
            this.list = list;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.video_comment_popwindow_item, null);
                holder.commentHeadImg = (ImageView) convertView.findViewById(R.id.commentHeadImg);
                holder.commentUserName = (TextView) convertView.findViewById(R.id.commentUserName);
                holder.commentTime = (TextView) convertView.findViewById(R.id.commentTime);
                holder.commentContent = (TextView) convertView.findViewById(R.id.commentContent);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, String> map = list.get(position);
            holder.commentUserName.setText(map.get("nickName"));
            holder.commentTime.setText(map.get("submitTime"));
            holder.commentContent.setText(map.get("content"));
            ImageDisplayTools.displayCircleImage(map.get("imgUrl"), holder.commentHeadImg, OSUtil.dp2px(activity, 1));
            if (!OSUtil.isDayTheme())
                holder.commentHeadImg.setColorFilter(TravelUtil.getColorFilter(activity));
            return convertView;
        }

        private class ViewHolder {
            ImageView commentHeadImg;
            TextView commentUserName;
            TextView commentTime;
            TextView commentContent;
        }
    }
    public interface UpdateCommentCountListener {
        void updateCommentCount(int count);
    }
    private UpdateCommentCountListener updateCommentCountListener;

    public void setUpdateCommentCountListener(UpdateCommentCountListener updateCommentCountListener) {
        this.updateCommentCountListener = updateCommentCountListener;
    }
}
