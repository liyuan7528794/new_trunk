package com.photoselector.ui;

/**
 * @author Aizaz AZ
 */

import android.os.Bundle;

import com.photoselector.domain.PhotoSelectorDomain;
import com.photoselector.ui.PhotoSelectorActivity.OnLocalReccentListener;
import com.travel.bean.PhotoModel;

import java.util.List;

public class PhotoPreviewActivity extends BasePhotoPreviewActivity implements OnLocalReccentListener {

    private PhotoSelectorDomain photoSelectorDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoSelectorDomain = new PhotoSelectorDomain(this);

        init(getIntent().getExtras());
    }

    @SuppressWarnings("unchecked")
    protected void init(Bundle extras) {
        if (extras == null)
            return;

        if (extras.containsKey("photos")) { // 预览图片
            photos = (List<PhotoModel>) extras.getSerializable("photos");
            current = extras.getInt("position", 0);
            tag = extras.getString("tag");
            updatePercent();
            bindData();
        } else if (extras.containsKey("album")) { // 点击图片查看
            String albumName = extras.getString("album"); // 相册
            this.current = extras.getInt("position");
            if (!isNull(albumName) && albumName.equals(PhotoSelectorActivity.RECCENT_PHOTO)) {
                photoSelectorDomain.getReccent(this);
            } else {
                photoSelectorDomain.getAlbum(albumName, this);
            }
        }
    }

    @Override
    public void onPhotoLoaded(List<PhotoModel> photos) {
        this.photos = photos;
        updatePercent();
        bindData();// 更新界面
    }

    private boolean isNull(CharSequence text) {
        if (text == null || "".equals(text.toString().trim()) || "null".equals(text))
            return true;
        return false;
    }

}
