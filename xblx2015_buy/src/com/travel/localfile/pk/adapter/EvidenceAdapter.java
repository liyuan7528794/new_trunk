package com.travel.localfile.pk.adapter;

import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.pk.entity.EvidencePacket;

import java.util.Date;
import java.util.List;

/**
 * 证据列表的Adapter
 * Created by ldkxingzhe on 2016/7/6.
 */
public class EvidenceAdapter extends ListBaseAdapter<EvidencePacket>{
    @SuppressWarnings("unused")
    private static final String TAG = "EvidencePacket";

    private long mLastClickTime;

    public interface Listener{
        void onEvidencePacketClick(EvidencePacket evidencePacket, int position);
    }

    private Listener mListener;
    public EvidenceAdapter(List list, Listener listener) {
        super(list);
        mListener = listener;
    }

    @Override
    public int getViewTypeCount() {
        /* 左右两边 * 三种类型 */
        return 2 * 3;
    }

    @Override
    public int getItemViewType(int position) {
        EvidencePacket item = getItem(position);
        return (item.isLeft() ? 0 : 1) * 3 + item.getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = null;
        switch (getItemViewType(position)){
            case 0: // 左边多媒体
                result = getViewTypeMedia(position, convertView, parent, true);
                break;
            case 1: // 左边...
                break;
            case 3:  // 右边多媒体
                result = getViewTypeMedia(position, convertView, parent, false);
                break;
        }
        return result;
    }

    private View getViewTypeMedia(int position, View convertView, ViewGroup parent, boolean isLeft){
        MediaViewHolder viewHolder = null;
        if(convertView == null){
            int layoutId =
                    isLeft ? R.layout.list_item_evidence_left : R.layout.list_item_evidence_right;
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            viewHolder = new MediaViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (MediaViewHolder) convertView.getTag();
        }
        final EvidencePacket item = getItem(position);
        if(item.getMultipleMediaList() != null && item.getMultipleMediaList().size() > 0){
            viewHolder.gridView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.gridView.setVisibility(View.GONE);
        }
        MediaGridViewAdapter adapter = new MediaGridViewAdapter(item.getMultipleMediaList(), viewHolder.gridView);
        viewHolder.gridView.setAdapter(adapter);
        viewHolder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long currentTime = new Date().getTime();
                if(mListener != null && currentTime - mLastClickTime > 300) mListener.onEvidencePacketClick(item, position);
                mLastClickTime = currentTime;
            }
        });
        viewHolder.introduction.setText(item.getIntroduction());
        viewHolder.introduction.setVisibility(
                TextUtils.isEmpty(item.getIntroduction()) ? View.GONE : View.VISIBLE);
        if(viewHolder.gridView.getVisibility() != View.VISIBLE
                || viewHolder.introduction.getVisibility() != View.VISIBLE){
            viewHolder.middleSpace.setVisibility(View.GONE);
        }else{
            viewHolder.middleSpace.setVisibility(View.VISIBLE);
        }
        viewHolder.timer.setText(item.getCreateTime());
        return convertView;
    }

    private class EvidenceViewHolder{
        TextView timer;
        public EvidenceViewHolder(View convertView){
            timer = (TextView) convertView.findViewById(R.id.tv_time);
        }
    }

    private class MediaViewHolder extends EvidenceViewHolder{
        GridView gridView;
        TextView introduction;
        View middleSpace;

        public MediaViewHolder(View convertView){
            super(convertView);
            gridView = (GridView) convertView.findViewById(R.id.gv_grid_view);
            introduction = (TextView) convertView.findViewById(R.id.tv_introduction);
            middleSpace = convertView.findViewById(R.id.middle_space);
        }
    }

    private class MediaGridViewAdapter extends ListBaseAdapter<LocalFile>{
        private int itemWidth;
        private GridView gridView;
        public MediaGridViewAdapter(List<LocalFile> list, GridView gridView) {
            super(list);
            this.gridView = gridView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.grid_item_local_file, parent, false);
                makeSureWidthExist();
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                params.height = itemWidth;
                convertView.setLayoutParams(params);
                viewHolder = new ViewHolder(convertView);
                ViewGroup.LayoutParams para = viewHolder.typeImageView.getLayoutParams();
                para.width = para.height = OSUtil.dp2px(parent.getContext(), 20);
                viewHolder.typeImageView.setLayoutParams(para);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            LocalFile localFile = getItem(position);
            String url = localFile.getThumbnailPath();
            boolean showType = localFile.getType() != CameraFragment.TYPE_PHOTO;
            if(localFile.getType() != CameraFragment.TYPE_AUDIO){
                ImageDisplayTools.displayImage(url, viewHolder.bgImageView);
            }else {
                ImageDisplayTools.displayImage("drawable://" + R.drawable.bg_record_voice, viewHolder.bgImageView);
            }
            viewHolder.typeImageView.setImageResource(
                    localFile.getType() != CameraFragment.TYPE_AUDIO
                            ? R.drawable.detail_icon_play_white : R.drawable.detail_icon_voice_white);
            viewHolder.typeImageView.setVisibility(showType ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }

        private void makeSureWidthExist(){
            if(itemWidth > 0) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                itemWidth = gridView.getColumnWidth();
            }else{
                itemWidth = gridView.getWidth() / gridView.getNumColumns();
            }
        }


        private class ViewHolder{
            private ImageView bgImageView;
            private ImageView typeImageView;

            public ViewHolder(View convertView){
                bgImageView = (ImageView) convertView.findViewById(R.id.iv_bg);
                typeImageView = (ImageView) convertView.findViewById(R.id.iv_type);
            }
        }
    }
}
