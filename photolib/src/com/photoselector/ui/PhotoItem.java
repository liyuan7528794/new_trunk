package com.photoselector.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.photo.R;
import com.travel.bean.PhotoModel;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;

/**
 * 图片选择中适配器的item的布局
 *
 * @author WYP
 * @version 1.0
 * @created 2016/05/26
 */
public class PhotoItem extends LinearLayout implements OnCheckedChangeListener {

    private ImageView ivPhoto;
    private CheckBox cbPhoto;
    private onPhotoItemCheckedListener listener;
    private PhotoModel photo;
    private boolean isCheckAll;
    private Context context;

    private PhotoItem(Context context) {
        super(context);
        this.context = context;
    }

    public PhotoItem(Context context, onPhotoItemCheckedListener listener) {
        this(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.layout_photoitem, this, true);
        this.listener = listener;
        ImageDisplayTools.initImageLoader(context);

        ivPhoto = (ImageView) findViewById(R.id.iv_photo_lpsi);
        cbPhoto = (CheckBox) findViewById(R.id.cb_photo_lpsi);

        cbPhoto.setOnCheckedChangeListener(this); // CheckBox选中状态改变监听器
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isCheckAll) {
            listener.onCheckedChanged(photo, buttonView, isChecked); // 调用主界面回调函数
        }
        // 让图片变暗或者变亮
        if (isChecked) {
            ivPhoto.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);//滤镜效果
        } else {
            if (!OSUtil.isDayTheme())
                ivPhoto.setColorFilter(TravelUtil.getColorFilter(context));
            else
                ivPhoto.clearColorFilter();
        }
        photo.setChecked(isChecked);
    }

    /**
     * 设置路径下的图片对应的缩略图
     */
    public void setImageDrawable(final PhotoModel photo) {
        this.photo = photo;

        ImageDisplayTools.displayImage("file://" + photo.getOriginalPath(), ivPhoto);
        if (!OSUtil.isDayTheme())
            ivPhoto.setColorFilter(TravelUtil.getColorFilter(context));
    }

    @Override
    public void setSelected(boolean selected) {
        if (photo == null) {
            return;
        }
        isCheckAll = true;
        cbPhoto.setChecked(selected);
        isCheckAll = false;
    }

    /**
     * 图片Item选中事件监听器
     */
    public static interface onPhotoItemCheckedListener {
        public void onCheckedChanged(PhotoModel photoModel, CompoundButton buttonView, boolean isChecked);
    }

}
