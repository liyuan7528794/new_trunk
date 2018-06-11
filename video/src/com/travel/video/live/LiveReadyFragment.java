package com.travel.video.live;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.tencent.qcloud.suixinbo.model.CurLiveInfo;
import com.tencent.qcloud.suixinbo.presenters.LiveHelper;
import com.travel.lib.TravelApp;
import com.travel.lib.helper.SelectCoverHelper;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.video.bean.XpaiCofig;
import com.travel.VideoConstant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class LiveReadyFragment extends Fragment implements SelectCoverHelper.Listener {
	// 请求码
	private static final int CHECK_PRODUCT = 5;

	private Activity context;
	private Bundle mBundle;
	private View rootView;
	private Button startLiveButton;
	private ImageView cancle;
	private ImageView coverImageView;
	private TextView fromStorage;
	private TextView goCamera;
	private EditText liveTitleEdit;
	private TextView checkProduct;

	private String coverUrl = "-1";
	private String coverId = "-1";
	private String userId = "-1";
	private String nickname = "";
	private String headImgUrl = "";
	private String scenicId = "-1";
	private String activityid = "-1";
	private String goodsId = "-1";
	private String share = "-1";
	private double longitude = -1;
	private double latitude = -1;
	private String place = "-1";

	private int mapType = 0;

	private ImageView wechatFavorite,wechat,qq,qZone;
	private String shareUrl = "";

	private SelectCoverHelper mSelectCoverHelper;
	private Bitmap bitmap;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = activity;
		mSelectCoverHelper = new SelectCoverHelper(activity, this, "liveCover");
		mSelectCoverHelper.setListener(this);
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.containsKey("mBundle")){
			mBundle = savedInstanceState.getBundle("mBundle");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("mBundle", mBundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(context instanceof HostWindowActivity && ((HostWindowActivity) context).liveType==HostWindowActivity.LIVE_TYPE_PACK){
			rootView = inflater.inflate(R.layout.activity_live_ready_land, null);
		}else{
			rootView = inflater.inflate(R.layout.activity_live_ready, null);
		}
		mBundle = getArguments();
		if(mBundle!=null){
			if(mBundle.containsKey("activity_id") && null != mBundle.getString("activity_id") && !"".equals(mBundle.getString("activity_id"))){
				activityid = mBundle.getString("activity_id");
			}
			if(mBundle.containsKey("scenic_id") && null != mBundle.getString("scenic_id") && !"".equals(mBundle.getString("scenic_id"))){
				scenicId = mBundle.getString("scenic_id");
			}

			if(mBundle.containsKey("map_type") && null != mBundle.getString("map_type") && !"".equals(mBundle.getString("map_type"))){
				mapType = mBundle.getInt("map_type");
			}
		}

		latitude = XpaiCofig.latitude;
		longitude = XpaiCofig.longitude;
		updateWithNewLocation(latitude,longitude);
		userId = UserSharedPreference.getUserId();
		headImgUrl = UserSharedPreference.getUserHeading();
		coverId = UserSharedPreference.getCoverId();
		nickname = UserSharedPreference.getNickName();
//		place = UserSharedPreference.getAddress();
		share = userId + "-" + getTime();
		CurLiveInfo.getInstance().setShare(share);
		shareUrl = VideoConstant.SHARE_VIDEO_URL + share;
		initView();
		coverUrl = UserSharedPreference.getUserHeading();
		ImageDisplayTools.displayImage(UserSharedPreference.getUserHeading(), coverImageView);

//		adapter = new GridViewAdapter(context, gridList);
//		gridView.setAdapter(adapter);
		return rootView;
	}

	private void initView() {
		liveTitleEdit = (EditText) rootView.findViewById(R.id.live_title_edit);
		cancle = (ImageView) rootView.findViewById(R.id.cancle);
		startLiveButton = (Button) rootView.findViewById(R.id.startLive);
		coverImageView = (ImageView) rootView.findViewById(R.id.cover_imageView);
		fromStorage = (TextView) rootView.findViewById(R.id.from_storage);
		goCamera = (TextView) rootView.findViewById(R.id.go_camera);
		checkProduct = (TextView) rootView.findViewById(R.id.checkProduct);
		checkProduct.setOnClickListener(checkProductListener);

		mSelectCoverHelper.setImageView(coverImageView);
		
		if("0".equals(UserSharedPreference.getUserId())){
			rootView.findViewById(R.id.productLinearLayout).setVisibility(View.GONE);
		}
		liveTitleEdit.setText(nickname+"的直播");
		coverImageView.setImageResource(R.drawable.live_pic_cover);

		cancle.setOnClickListener(liveReadyBackListener);
		startLiveButton.setOnClickListener(startLiveListener);
		fromStorage.setOnClickListener(fromStorageViewListener);
		goCamera.setOnClickListener(goCameraViewListener);

		wechatFavorite = (ImageView) rootView.findViewById(R.id.wechatFavorite);
		wechat = (ImageView) rootView.findViewById(R.id.wechat);
		qq = (ImageView) rootView.findViewById(R.id.qq);
		qZone = (ImageView) rootView.findViewById(R.id.qZone);
		wechatFavorite.setOnClickListener(new ShareClickListener());
		wechat.setOnClickListener(new ShareClickListener());
		qq.setOnClickListener(new ShareClickListener());
		qZone.setOnClickListener(new ShareClickListener());
	}

	@Override
	public void onImageUploadSuccess(String url, String coverId, Bitmap bitmap) {
		coverUrl = url;
		this.coverId = coverId;
		this.bitmap = bitmap;
	}

	@Override
	public void onUploadFileFailed() {
		Toast.makeText(getActivity(), "图片上传失败， 请检查网络连接", Toast.LENGTH_SHORT).show();
	}

	private class ShareClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			int id = v.getId();
			String platform = "";
			if(id == R.id.wechatFavorite)
				platform = WechatMoments.NAME;
			else if(id == R.id.wechat)
				platform = Wechat.NAME;
			else if(id == R.id.qq)
				platform = QQ.NAME;
			else if(id == R.id.qZone)
				platform = QZone.NAME;

			OSUtil.showShare(platform, liveTitleEdit.getText().toString(), liveTitleEdit.getText().toString(), headImgUrl, shareUrl, shareUrl, context);
//			goLive();
		}

	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
			LiveHelper.S_IN_CROP = false;
			return;
		}
		switch (requestCode) {
			case SelectCoverHelper.CAMERA_REQUEST_CODE:
				mSelectCoverHelper.backFromCamera();
				break;
			case SelectCoverHelper.REQUEST_CAMERA_CROP:
				LiveHelper.S_IN_CROP = false;
				mSelectCoverHelper.onCameraCropResult(resultCode, data);
				break;
			case SelectCoverHelper.REQUEST_IMAGE_PICK:
				// 图片拾取
				mSelectCoverHelper.onImagePickResult(resultCode, data);
				break;
			case SelectCoverHelper.REQUEST_IMAGE_CROP:
				LiveHelper.S_IN_CROP = false;
				mSelectCoverHelper.onImageCropResult(resultCode, data);
				break;
			case CHECK_PRODUCT:
				if (data == null || !data.hasExtra("goods_id"))
					return;
				System.out.println("goods:"+data.getStringExtra("goods_id"));
				goodsId = data.getStringExtra("goods_id");
				checkProduct.setText(data.getStringExtra("goods_name"));
				break;
			default:
				break;
		}
	}

	private void goLive(){
		CurLiveInfo.getInstance().setTitle(liveTitleEdit.getText().toString());
		CurLiveInfo.getInstance().setCoverurl(coverUrl);
		CurLiveInfo.getInstance().setCoverId(coverId);
//		if(mapType != 0){
		CurLiveInfo.getInstance().setLatitude(latitude);
		CurLiveInfo.getInstance().setLongitude(longitude);
		CurLiveInfo.getInstance().setAddress(place);
//		}
		CurLiveInfo.getInstance().setActivityId(activityid);
		CurLiveInfo.getInstance().setGoodsId(goodsId);
		if(getActivity() instanceof HostWindowActivity){
			((HostWindowActivity)getActivity()).mUIHandler.sendEmptyMessage(HostWindowHandler.START_LIVE);
		}
	}

	/**
	 * 开始直播监听器，若为普通用户直接直播，若是特殊用户则先从服务器获取直播地址在进直播室
	 */
	OnClickListener startLiveListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if("".equals(coverUrl) || "-1".equals(coverUrl)){
				Toast.makeText(context, "请上传封面后再开始直播！谢谢！", Toast.LENGTH_SHORT).show();
				return;
			}

			String livetitle = "";
			try {
				livetitle = URLEncoder.encode(liveTitleEdit.getText().toString(),"utf-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			goLive();
		}
	};

	OnClickListener checkProductListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent=new Intent(context,CheckProductActivity.class);
			startActivityForResult(intent, CHECK_PRODUCT);
		}
	};

	OnClickListener liveReadyBackListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			context.finish();
		}
	};

	OnClickListener fromStorageViewListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startCropPicture();
			mSelectCoverHelper.pickFromGallery();
		}
	};

	void startCropPicture() {
		if(context instanceof HostWindowActivity){
			((HostWindowActivity) context).mLiveHelper.pause();
			if(((HostWindowActivity) context).lvbLiveHelper != null)
				((HostWindowActivity) context).lvbLiveHelper.pause();
			LiveHelper.S_IN_CROP = true;
		}
	}

	OnClickListener goCameraViewListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startCropPicture();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mSelectCoverHelper.pickFromCamera();
				}
			}, 400);
		}
	};

	private String getTime(){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		return format.format(new Date());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
			bitmap = null;
			System.gc();
		}
	}

	/**
	 * 根据经纬度返回当前城市
	 * @param latitude
	 * @param longitude
     * @return
     */
	private void updateWithNewLocation(final double latitude, final double longitude) {
		 new Thread(new Runnable() {
			 @Override
			 public void run() {
				 String addressStr = "";
				 Geocoder geocoder = new Geocoder(TravelApp.appContext, Locale.getDefault());
				 try {
					 List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
					 if (addresses.size() > 0) {
						 Address address = addresses.get(0);
						 addressStr = address.getLocality();
					 }
				 } catch (IOException e) {
					 e.printStackTrace();
				 }

				 place = addressStr;
			 }
		 }).start();
	}
}
