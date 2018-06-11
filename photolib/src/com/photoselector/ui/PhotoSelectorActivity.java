package com.photoselector.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.photo.R;
import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.model.AlbumModel;
import com.photoselector.ui.PhotoItem.onPhotoItemCheckedListener;
import com.photoselector.util.AnimationUtil;
import com.travel.ShopConstant;
import com.travel.bean.PhotoModel;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择图片
 * 
 * @author WYP
 * @version 1.0
 * @created 2016/05/26
 *
 */
public class PhotoSelectorActivity extends TitleBarBaseActivity
		implements onPhotoItemCheckedListener, OnItemClickListener, OnClickListener {

	private Context mContext;
	public static final int SINGLE_IMAGE = 1;
	public static final int REQUEST_PHOTO = 0;
	public static String RECCENT_PHOTO = null;

	private GridView gvPhotos;
	private ListView lvAblum;
	private TextView tvAlbum, tvPreview;
	private PhotoSelectorDomain photoSelectorDomain;
	private ArrayList<PhotoModel> selected;
	private PhotoSelectorAdapter photoAdapter;
	private AlbumAdapter albumAdapter;
	private RelativeLayout layoutAlbum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photoselector);

		init();

		rightButton.setOnClickListener(this);
		tvAlbum.setOnClickListener(this);
		tvPreview.setOnClickListener(this);

		lvAblum.setOnItemClickListener(this);

		photoSelectorDomain.getReccent(reccentListener); // 更新最近照片
		photoSelectorDomain.updateAlbum(albumListener); // 更新相册信息
	}

	/**
	 * 控件初始化
	 */
	@SuppressWarnings("unchecked")
	private void init() {
		mContext = this;

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rightButton.getLayoutParams();
		params.rightMargin = OSUtil.dp2px(mContext, 10);
		rightButton.setLayoutParams(params);
		rightButton.setVisibility(View.VISIBLE);

		gvPhotos = (GridView) findViewById(R.id.gv_photos_ar);
		lvAblum = (ListView) findViewById(R.id.lv_ablum_ar);
		tvAlbum = (TextView) findViewById(R.id.tv_album_ar);
		tvPreview = (TextView) findViewById(R.id.tv_preview_ar);
		layoutAlbum = (RelativeLayout) findViewById(R.id.layout_album_ar);

		setTitle(getString(R.string.photo_choose_photo));
		RECCENT_PHOTO = getString(R.string.recent_photos);

		selected = new ArrayList<PhotoModel>();
		if (getIntent().getExtras() != null) {
			selected.addAll((ArrayList<PhotoModel>) getIntent().getSerializableExtra("selected"));
		}
		if (selected.size() > 0) {
			tvPreview.setEnabled(true);
		} else {
			tvPreview.setEnabled(false);
		}
		rightButton.setText(getString(R.string.sure) + "(" + selected.size() + ")");
		photoAdapter = new PhotoSelectorAdapter(mContext, new ArrayList<PhotoModel>(), this);
		gvPhotos.setAdapter(photoAdapter);

		albumAdapter = new AlbumAdapter(mContext, new ArrayList<AlbumModel>());
		lvAblum.setAdapter(albumAdapter);

		photoSelectorDomain = new PhotoSelectorDomain(mContext);
	}

	@Override
	public void onClick(View v) {
		// 选完图片
		if (v == rightButton)
			ok();
		// 选择其他相册的图片
		else if (v.getId() == R.id.tv_album_ar)
			album();
		// 预览
		else if (v.getId() == R.id.tv_preview_ar)
			priview();
	}

	/** 完成 */
	private void ok() {
		if (selected.size() > ShopConstant.PHOTO_MAX) {
			showToast(String.format(getString(R.string.max_img_limit_reached), ShopConstant.PHOTO_MAX));
			return;
		}
		Intent data = new Intent();
		data.putExtra("photos", selected);
		setResult(RESULT_OK, data);
		finish();
	}

	/** 预览照片 */
	private void priview() {
		Bundle bundle = new Bundle();
		bundle.putSerializable("photos", selected);
		TravelUtil.launchActivity(this, PhotoPreviewActivity.class, bundle);
	}

	private void album() {
		if (layoutAlbum.getVisibility() == View.GONE) {
			popAlbum();
		} else {
			hideAlbum();
		}
	}

	/** 弹出相册列表 */
	private void popAlbum() {
		layoutAlbum.setVisibility(View.VISIBLE);
		new AnimationUtil(getApplicationContext(), R.anim.translate_up_current).setLinearInterpolator()
				.startAnimation(layoutAlbum);
	}

	/** 隐藏相册列表 */
	private void hideAlbum() {
		new AnimationUtil(getApplicationContext(), R.anim.translate_down).setLinearInterpolator()
				.startAnimation(layoutAlbum);
		layoutAlbum.setVisibility(View.GONE);
	}

	/**
	 * 照片选中状态改变之后
	 */
	@Override
	public void onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			if (!selected.contains(photoModel))
				selected.add(photoModel);
			if (selected.size() > ShopConstant.PHOTO_MAX) {
				selected.get(selected.size() - 1).setChecked(false);
				photoAdapter.updateSingleRow(gvPhotos,selected.get(selected.size() - 1).getOriginalPath());
				selected.remove(selected.size() - 1);
				showToast(String.format(getString(R.string.max_img_limit_reached), ShopConstant.PHOTO_MAX));
				return;
			}
			tvPreview.setEnabled(true);
		} else {
			for (int i = 0; i < selected.size(); i++) {
				if (photoModel.getOriginalPath().equals(selected.get(i).getOriginalPath()))
					selected.remove(i);
			}
		}
		rightButton.setText(getString(R.string.sure) + "(" + selected.size() + ")");

		if (selected.isEmpty()) {
			tvPreview.setEnabled(false);
			tvPreview.setText(getString(R.string.preview));
		}
	}

	/**
	 * 相册列表点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AlbumModel current = (AlbumModel) parent.getItemAtPosition(position);
		for (int i = 0; i < parent.getCount(); i++) {
			AlbumModel album = (AlbumModel) parent.getItemAtPosition(i);
			if (i == position)
				album.setCheck(true);
			else
				album.setCheck(false);
		}
		albumAdapter.notifyDataSetChanged();
		hideAlbum();
		tvAlbum.setText(current.getName());

		// 更新照片列表
		if (current.getName().equals(RECCENT_PHOTO))
			photoSelectorDomain.getReccent(reccentListener);
		else
			photoSelectorDomain.getAlbum(current.getName(), reccentListener); // 获取选中相册的照片
	}

	/** 获取本地图库照片回调 */
	public interface OnLocalReccentListener {
		public void onPhotoLoaded(List<PhotoModel> photos);
	}

	/** 获取本地相册信息回调 */
	public interface OnLocalAlbumListener {
		public void onAlbumLoaded(List<AlbumModel> albums);
	}

	private OnLocalAlbumListener albumListener = new OnLocalAlbumListener() {
		@Override
		public void onAlbumLoaded(List<AlbumModel> albums) {
			albumAdapter.update(albums);
		}
	};

	private OnLocalReccentListener reccentListener = new OnLocalReccentListener() {
		@Override
		public void onPhotoLoaded(List<PhotoModel> photos) {
			for (PhotoModel model : photos) {
				for (int i = 0; i < selected.size(); i++) {
					if (selected.get(i).getOriginalPath().equals(model.getOriginalPath())) {
						model.setChecked(true);
					}
				}
			}
			photoAdapter.update(photos);
			gvPhotos.smoothScrollToPosition(0); // 滚动到顶端
		}
	};

	@Override
	public void onBackPressed() {
		if (layoutAlbum.getVisibility() == View.VISIBLE) {
			hideAlbum();
		} else
			super.onBackPressed();
	}
}
