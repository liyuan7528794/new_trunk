package com.travel.localfile;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ctsmedia.hltravel.R;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.localfile.dao.LocalFile;

import java.util.List;

public class LocalFileGridViewAdapter extends ListBaseAdapter<LocalFile> {

    public interface Listener{
        /** 是否显示选择框 */
        boolean hasChecked();
        /** 该位置是否已经选择了 */
        boolean isPositionChecked(int position);
        /** 该位置被选择 */
        void addPosition(int position);
        /** 该位置移除选择 */
        void removePosition(int position);
        /** 当某项选中状态改变后 */
        boolean onCheckedChanged(int position, boolean isChecked);
        /** 获取每项的宽度 */
        int getItemWidth();
    }

    private int mItemWidth;
    private Listener mListener;
    private boolean mHasFeatureSelected;
    public LocalFileGridViewAdapter(List<LocalFile> list, Listener listener) {
        super(list);
        mListener = listener;
    }

    public void setHasFeatureSelected(boolean hasFeatureSelected){
        mHasFeatureSelected = hasFeatureSelected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item_local_file, parent, false);
            viewHolder.frameLayout = (FrameLayout) convertView;
            viewHolder.bgImage = (ImageView) convertView.findViewById(R.id.iv_bg);
            viewHolder.typeImage = (ImageView) convertView.findViewById(R.id.iv_type);
            viewHolder.tv_make_time = (TextView) convertView.findViewById(R.id.tv_make_time);
            viewHolder.toggleButton = (ToggleButton) convertView.findViewById(R.id.cb_select);
            viewHolder.toggleLayout = (FrameLayout) convertView.findViewById(R.id.fl_toggle_layout);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.pb_upload);
            viewHolder.foreBg = convertView.findViewById(R.id.v_fore_ground);
            if(mHasFeatureSelected){
                viewHolder.toggleButton.setVisibility(View.VISIBLE);
                viewHolder.toggleLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            viewHolder.toggleButton.toggle();
                        }
                        return true;
                    }
                });
            }
            convertView.setTag(viewHolder);
            makeSureItemWidthExist();
            ViewGroup.LayoutParams params = convertView.getLayoutParams();
            params.height = mItemWidth;
            convertView.setLayoutParams(params);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.typeImage.setVisibility(View.INVISIBLE);
            viewHolder.tv_make_time.setVisibility(View.INVISIBLE);
            viewHolder.toggleButton.setOnCheckedChangeListener(null);
            viewHolder.toggleButton.setChecked(false);
        }

        LocalFile localFile = getItem(position);
        viewHolder.tv_make_time.setText(localFile.getDurationFormat());
        switch (localFile.getType()){
            case CameraFragment.TYPE_PHOTO:
                ImageDisplayTools.displayImage("file://" + localFile.getLocalPath(), viewHolder.bgImage);
                break;
            case CameraFragment.TYPE_AUDIO:
                viewHolder.bgImage.setBackgroundResource(R.drawable.bg_record_voice);
                viewHolder.typeImage.setImageResource(R.drawable.detail_icon_voice_white);
                viewHolder.typeImage.setVisibility(View.VISIBLE);
                viewHolder.tv_make_time.setVisibility(View.VISIBLE);
                break;
            case CameraFragment.TYPE_VIDEO:
                ImageDisplayTools.displayImage("file://" + localFile.getLocalPath() + "_thumbnail", viewHolder.bgImage);
                viewHolder.typeImage.setImageResource(R.drawable.detail_icon_play_white);
                viewHolder.typeImage.setVisibility(View.VISIBLE);
                viewHolder.tv_make_time.setVisibility(View.VISIBLE);
                break;
            case CameraFragment.TYPE_LIVE:
                viewHolder.typeImage.setImageResource(R.drawable.detail_icon_play_white);
                ImageDisplayTools.displayImage(localFile.getThumbnailPath(), viewHolder.bgImage);
                viewHolder.typeImage.setVisibility(View.VISIBLE);
                break;
        }
        if(mListener.hasChecked()){
            if(mListener.isPositionChecked(position)){
                viewHolder.toggleButton.setChecked(true);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.foreBg.setVisibility(View.VISIBLE);
            }else{
                viewHolder.foreBg.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
//                viewHolder.progressBar.setVisibility(
//                        localFile.getIsUpLoaded() ? View.VISIBLE : View.GONE);
            }

            viewHolder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        mListener.addPosition(position);
                        viewHolder.foreBg.setVisibility(View.VISIBLE);
                    }else {
                        mListener.removePosition(position);
                        viewHolder.foreBg.setVisibility(View.GONE);
                    }
                    mListener.onCheckedChanged(position, isChecked);
                }
            });
        }else{
            viewHolder.toggleButton.setVisibility(View.GONE);
        }
        try{
            if(localFile.getIsUpLoaded()){
                viewHolder.progressBar.setProgress(100);
            }else{
                int progress = Integer.valueOf(localFile.getOthers());
                viewHolder.progressBar.setProgress(progress);
            }
        }catch (Exception ignored){MLog.d("LocalFile", ignored.getMessage());}
        return convertView;
    }

    private void makeSureItemWidthExist(){
        if(mItemWidth > 0) return;
        mItemWidth = mListener.getItemWidth();
    }

    private class ViewHolder{
        FrameLayout frameLayout, toggleLayout;
        ImageView bgImage;
        View foreBg;
        ImageView typeImage;
        TextView tv_make_time;
        ToggleButton toggleButton;
        ProgressBar progressBar;
    }
}