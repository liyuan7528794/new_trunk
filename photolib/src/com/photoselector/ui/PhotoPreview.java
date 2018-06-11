package com.photoselector.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.photo.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.polites.GestureImageView;

/**
 * @author Aizaz AZ
 */

public class PhotoPreview extends LinearLayout implements OnClickListener {

	private ProgressBar pbLoading;
	private GestureImageView ivContent;
	private OnClickListener l;
	private Context mContext;

	public PhotoPreview(Context context) {
		super(context);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.view_photopreview, this, true);

		pbLoading = (ProgressBar) findViewById(R.id.pb_loading_vpp);
		ivContent = (GestureImageView) findViewById(R.id.iv_content_vpp);
		ivContent.setOnClickListener(this);
	}

	public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
		this(context);
	}

	public PhotoPreview(Context context, AttributeSet attrs) {
		this(context);
	}

	public void loadImage(String path) {
		if (path.contains("http")) {
			Glide.with(mContext).load(path).into(ivContent);
		} else {
			ImageLoader.getInstance().loadImage(path, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					ivContent.setImageBitmap(loadedImage);
					pbLoading.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					ivContent.setImageDrawable(getResources().getDrawable(R.drawable.ic_loading));
					pbLoading.setVisibility(View.GONE);
				}
			});
		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.l = l;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.iv_content_vpp && l != null)
			l.onClick(ivContent);
	};

}
