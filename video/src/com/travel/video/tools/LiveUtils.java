package com.travel.video.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tencent.TIMCallBack;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.travel.activity.ChangeHeadImageActivity;
import com.travel.layout.DialogTemplet;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.CheckNetStatus;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.help.LiveHttpRequest;
import com.travel.video.live.HostWindowActivity;

/**
 * 直播控件使用工具
 * @author Administrator
 *
 */
public class LiveUtils {
	
	/**
	 * 直播按钮，跳转功能
	 * @param activity
	 */
	public static void GoLiveClick(final Activity activity,final Bundle bundle){
		CurLiveInfo.getInstance().setAddress(UserSharedPreference.getAddress());
		String netType = CheckNetStatus.checkNetworkConnection();
		if (CheckNetStatus.wifiNetwork.equals(netType)) {
			goLive(activity,bundle);
			return;
		}

		if (CheckNetStatus.unNetwork.equals(netType)) {// 没网
			Toast.makeText(activity, "当前无网络，请检查网络！", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!CheckNetStatus.unNetwork.equals(netType) && !CheckNetStatus.wifiNetwork.equals(netType)) {
			if ("UNKNOWN".equals(netType)) {
				Toast.makeText(activity, "当前无网络，请检查网络！", Toast.LENGTH_SHORT).show();
				return;
			}
			//弹框提醒
			final DialogTemplet dialog = AlertDialogUtils.getNetStatusDialog(netType, activity);
			dialog.setLeftClick(new DialogTemplet.DialogLeftButtonListener() {

		            @Override
		            public void leftClick(View view) {
		                dialog.dismiss();
		            }
		        });
			dialog.setRightClick(new DialogTemplet.DialogRightButtonListener() {

		        @Override
				public void rightClick(View view) {
					dialog.dismiss();
					goLive(activity,bundle);
				}
			});
		}
	}
	
	private static void goLive(final Activity activity, final Bundle bundle){
		if (!UserSharedPreference.isLogin()) {
			Intent intent = new Intent();
			intent.setAction("com.travel.login");
			activity.startActivity(intent);
		} else if(UserSharedPreference.isChangeHead()){
			Intent intent = new Intent(activity, ChangeHeadImageActivity.class);
			activity.startActivity(intent);
		}else{
			new LiveHttpRequest(activity, null)
					.applyFlowControlOperationPermission(null, UserSharedPreference.getUserId(), 1, new TIMCallBack() {
						@Override
						public void onError(int i, String s) {
							MLog.d("LiveUtil", "Error code is %d, and reason is %s", i, s);
							Toast.makeText(activity, "当前直播人数已达到上限，请稍后再试", Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onSuccess() {
							Intent intent = new Intent(activity, HostWindowActivity.class);
							intent.putExtra("activity_bundle", bundle);
							intent.putExtra(HostWindowActivity.LIVE_IS_HOST, true);
							activity.startActivity(intent);
						}
					});
		}
	}

}
