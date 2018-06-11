package com.travel.layout;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.travel.Constants;
import com.travel.http_helper.GetCountHttp;
import com.travel.layout.DialogTemplet.DialogLeftButtonListener;
import com.travel.layout.DialogTemplet.DialogRightButtonListener;
import com.travel.lib.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 关注弹框
 *
 * @author Administrator
 */
public class FollowPopupWindow extends BaseBellowPopupWindow {
    private Context mContext;
    private String userId;
    private String nickName;
    private String imgUrl;
    private TextView followNickName, followLiveNum, followNum, followAddress;
    private ImageView followHeadImg;
    private Button follow;
    private View rootView;

    private GetCountHttp getCountHelper;

    /**
     * 关注弹窗初始化
     *
     * @param context
     * @param userId  用户或群的Id
     * @param type    1表示关注人，2表示关注群
     */
    public FollowPopupWindow(Context context, String userId, String nickName, String imgUrl, int type) {
        super(context);
        this.mContext = context;
        this.userId = userId;
        this.nickName = nickName;
        this.imgUrl = imgUrl;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.follow_pop_window, null);
        initView();
        SetContentView(rootView);
        show();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                initNet();
            }
        });
    }

    private void initView() {
        followNickName = (TextView) rootView.findViewById(R.id.followNickName);
        followLiveNum = (TextView) rootView.findViewById(R.id.followLiveNum);
        followNum = (TextView) rootView.findViewById(R.id.followNum);
        followAddress = (TextView) rootView.findViewById(R.id.followAddress);
        followHeadImg = (ImageView) rootView.findViewById(R.id.followHeadImg);
        follow = (Button) rootView.findViewById(R.id.follow);

        getCountHelper = new GetCountHttp(countListener);
        if (userId.equals(UserSharedPreference.getUserId()))
            follow.setVisibility(View.GONE);

        ImageDisplayTools.displayHeadImage(imgUrl, followHeadImg);
        if (!OSUtil.isDayTheme())
            followHeadImg.setColorFilter(TravelUtil.getColorFilter(mContext));
        followNickName.setText(nickName);
        follow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollow) {
                    DialogTemplet interCutDialog = new DialogTemplet(mContext, false, "您确定要取消对他的关注吗？", "", "不取消", "是的");
                    interCutDialog.show();

                    interCutDialog.setLeftClick(new DialogLeftButtonListener() {
                        @Override
                        public void leftClick(View view) {

                        }
                    });

                    interCutDialog.setRightClick(new DialogRightButtonListener() {
                        @Override
                        public void rightClick(View view) {
                            getCountHelper.followNet(mContext,"2", userId);
                        }
                    });
                } else {
                    getCountHelper.followNet(mContext,"1", userId);
                }

            }
        });
    }

    private boolean isFollow = false;

    private void setFollowStatus() {
        if (isFollow) {
            follow.setText("已关注");
        } else {
            follow.setText("关注");
        }
    }

    private void initNet() {
        getCountHelper.getVideoCount(userId, mContext);
    }

    private GetCountHttp.CountListener countListener = new GetCountHttp.CountListener() {

        @Override
        public void OnGetVideoCount(boolean isResult, int videoCount) {
            getCountHelper.getFollowerCount(userId, mContext);
            followLiveNum.setText(videoCount + "");
        }

        @Override
        public void OnGetFollowCount(boolean isResult, int followCount) {
            getCountHelper.getPlace(userId, mContext);
        }

        @Override
        public void OnGetFollowerCount(boolean isResult, int followerCount) {
            getCountHelper.getFollowCount(userId, mContext);
            followNum.setText(followerCount + "");
        }

        @Override
        public void OnGetIsFollow(boolean isResult, boolean isFollowStatus) {
            isFollow = isFollowStatus;
            setFollowStatus();
        }

        @Override
        public void onGetPlace(boolean isResult, String place) {
            getCountHelper.getIsFollow(userId, mContext);
            followAddress.setText(place);
        }

        @Override
        public void onFollowControl(boolean isResult) {
            if (isResult) {
                isFollow = !isFollow;
                if (isFollow) {
                    Toast.makeText(mContext, "关注成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "取消关注成功！", Toast.LENGTH_SHORT).show();
                }
                setFollowStatus();
            }
        }
    };

//	private void followNet(String followStatus){
//		if(!UserSharedPreference.isLogin()){
//			Toast.makeText(mContext, "请先登录！", Toast.LENGTH_SHORT).show();
//			return;
//		}
//		if(UserSharedPreference.getUserId().equals(userId)){
//			Toast.makeText(mContext, "自己不能关注自己！", Toast.LENGTH_SHORT).show();
//			return;
//		}
//		if(OSUtil.isVisitor(userId)){
//			Toast.makeText(mContext, "不能关注游客！", Toast.LENGTH_SHORT).show();
//			return;
//		}
//
//		Map<String,Object> paramap = new HashMap<String,Object>();
//		paramap.put("myId", UserSharedPreference.getUserId());
//		paramap.put("toId", userId);
//		paramap.put("type", 1);
//		paramap.put("status", followStatus);
//		NetWorkUtil.postForm(mContext, Constants.Root_Url+ "/user/userFollow.do", new MResponseListener(mContext) {
//
//			@Override
//			public void onResponse(JSONObject response) {
//				if (response.optInt("error") == 0) {
//					isFollow = !isFollow;
//
//					if(isFollow){
//						Toast.makeText(mContext, "关注成功！", Toast.LENGTH_SHORT).show();
//					}else{
//						Toast.makeText(mContext, "取消关注成功！", Toast.LENGTH_SHORT).show();
//					}
//					setFollowStatus();
//				}
//			}
//
//		}, paramap);
//	}
}
