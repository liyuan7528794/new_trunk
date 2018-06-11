package com.travel.communication.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.ctsmedia.hltravel.R;
import com.travel.communication.helper.LoadAndDisplayImageTask;
import com.travel.communication.helper.LoaderFromDiskOrNet.LoadFileTaskListener;
import com.travel.communication.view.TouchImageView;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.MLog;

/**
 * 展示图片的activity
 * @author ldkxingzhe
 */
public class ImageDisplayActivity extends TitleBarBaseActivity implements LoadFileTaskListener {
	@SuppressWarnings("unused")
	private final String TAG = "ImageDisplayActivity";
	
	/** activity传值的字段 */
	public static final String URL_PATH = "url_path";
	private String mUrlPath;
	
	private TouchImageView mTouchImageView;
	private LoadAndDisplayImageTask mLoadAndDisplayImageTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_display);
		mTouchImageView = findView(R.id.iv_touch_image_view);
		mUrlPath = getIntent().getStringExtra(URL_PATH);
		if(mUrlPath == null){
			throw new IllegalArgumentException("This ImageDisplay acitity must have a image path url");
		}
		
		mLoadAndDisplayImageTask = new LoadAndDisplayImageTask(mUrlPath);
		mLoadAndDisplayImageTask.setListener(this);
//		new Thread(mLoadAndDisplayImageTask).start();
		onFileFine(null, null);
	}

	@Override
	public void onFileFine(String url, String fileName) {
		// TODO: 考虑超大图片问题
//		Bitmap bitmap = BitmapFactory.decodeFile(DirUtils.getImageCachePath() + "/" + fileName);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.welcomebackgroud01);
		mTouchImageView.setImageBitmap(bitmap);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MLog.v(TAG, "onDestroy");
		Drawable drawable = mTouchImageView.getDrawable();
		if(drawable instanceof BitmapDrawable){
			((BitmapDrawable)drawable).getBitmap().recycle();
		}
		mTouchImageView.setImageBitmap(null);
	}

	@Override
	public void onError() {
		MLog.e(TAG, "onError");
	}
}
