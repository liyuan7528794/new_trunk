package com.photoselector.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.GridView;

import com.photoselector.ui.PhotoItem.onPhotoItemCheckedListener;
import com.travel.bean.PhotoModel;
import com.travel.lib.utils.OSUtil;

import java.util.ArrayList;

/**
 * 图片选择的适配器
 *
 * @author WYP
 * @version 1.0
 * @created 2016/05/26
 */
public class PhotoSelectorAdapter extends MBaseAdapter<PhotoModel> {

    private onPhotoItemCheckedListener listener;
    private LayoutParams itemLayoutParams;
    private int itemWidth;

    private PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models) {
        super(context, models);
    }

    public PhotoSelectorAdapter(Context context, ArrayList<PhotoModel> models, onPhotoItemCheckedListener listener) {
        this(context, models);
        setItemWidth();
        this.listener = listener;
    }

    /**
     * 设置每一个Item的宽高
     */
    public void setItemWidth() {
        this.itemWidth = (OSUtil.getScreenWidth() - 5 * OSUtil.dp2px(context, 5)) / 3;
        this.itemLayoutParams = new LayoutParams(itemWidth, itemWidth);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoItem item = null;
        if (convertView == null || !(convertView instanceof PhotoItem)) {
            item = new PhotoItem(context, listener);
            item.setLayoutParams(itemLayoutParams);
            convertView = item;
        } else {
            item = (PhotoItem) convertView;
        }
        item.setImageDrawable(models.get(position));
        item.setSelected(models.get(position).isChecked());
        return convertView;
    }

    public void updateSingleRow(GridView gridView, String path) {

        if (gridView != null) {
            int start = gridView.getFirstVisiblePosition();
            for (int i = start, j = gridView.getLastVisiblePosition(); i <= j; i++)
                if (TextUtils.equals(path, ((PhotoModel) gridView.getItemAtPosition(i)).getOriginalPath())) {
                    View view = gridView.getChildAt(i - start);
                    getView(i, view, gridView);
                    break;
                }
        }
    }
}
