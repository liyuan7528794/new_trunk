package com.travel.localfile.pk.fragment;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.UploadFileHelper;

/**
 * 图片类的证据上传
 * Created by ldkxingzhe on 2017/2/13.
 */
public class PhotoEvidenceSelector extends AbstractEvidenceSelect {
    @SuppressWarnings("unused")
    private static final String TAG = "PhotoEvidenceSelector";

    private PhotoAdapter mAdapter;

    public PhotoEvidenceSelector(Context context, String userId) {
        super(context, userId);
        mAdapter = new PhotoAdapter();
        init();
    }

    @Override
    void loadFile() {
        loadFileFromDB(CameraFragment.TYPE_PHOTO);
    }

    @Override
    RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(mContext, 3);
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder>{

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_evidence_select_photo_new, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
            final LocalFile localFile = mLocalFileList.get(position);
            if (localFile.getType() == CameraFragment.TYPE_PHOTO){
                ImageDisplayTools.displayImage("file://" + localFile.getLocalPath(), holder.thumbnailImage);
            }else if (localFile.getType() == CameraFragment.TYPE_VIDEO){
                holder.timeLong.setVisibility(View.VISIBLE);
                holder.timeLong.setText(localFile.getDurationFormat());
                String thumbnailPath = localFile.getLocalPath() + "_thumbnail";
                ImageDisplayTools.displayImage("file://" + thumbnailPath, holder.thumbnailImage);
            }else if(localFile.getType() == CameraFragment.TYPE_LIVE){
                ImageDisplayTools.displayImage(localFile.getThumbnailPath(), holder.thumbnailImage);
            }
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(mSelectedList.contains(localFile));
            if(localFile.getType() == CameraFragment.TYPE_VIDEO){
                holder.pb_upload.setVisibility(View.VISIBLE);
            }else{
                holder.pb_upload.setVisibility(View.GONE);
            }
            try{
                if(localFile.getIsUpLoaded()){
                    holder.pb_upload.setProgress(100);
                }else{
                    int progress = Integer.valueOf(localFile.getOthers());
                    holder.pb_upload.setProgress(progress);
                }
            }catch (Exception ignored){
                MLog.d("LocalFile", ignored.getMessage());}
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    LocalFile inLocalFile = mLocalFileList.get(holder.getAdapterPosition());
                    if (isChecked){
                        if (!select(inLocalFile)){
                            buttonView.setChecked(false);
                            return;
                        }
                        mUploadFileHelper.uploadFile(inLocalFile, new UploadFileHelper.ProgressCallback() {
                            @Override
                            public void onProgress(int progress) {
                                localFile.setOthers(String.valueOf(progress));
                                holder.pb_upload.setProgress(progress);
                            }

                            @Override
                            public void onFailed(String errorMessage) {
                                localFile.setOthers(String.valueOf(-1));
                                holder.pb_upload.setProgress(-1);
                            }

                            @Override
                            public void onComplete(LocalFile lf) {
                                for (int i = 0; i < mSelectedList.size(); i++){
                                    LocalFile localFile1 = mSelectedList.get(i);
                                    if(localFile1.getId() == lf.getId()){
                                        mSelectedList.set(i, lf);
                                    }
                                }
                                localFile.setOthers(String.valueOf(101));
                                holder.pb_upload.setProgress(101);
                            }
                        });
                    }else{
                        mSelectedList.remove(inLocalFile);
                        mUploadFileHelper.stopUploadFile(inLocalFile);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLocalFileList.size();
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder{
        ImageView thumbnailImage;
        CheckBox checkBox;
        TextView timeLong;
        ProgressBar pb_upload;
        public PhotoViewHolder(View itemView) {
            super(itemView);
            thumbnailImage = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_check);
            timeLong = (TextView) itemView.findViewById(R.id.tv_time_long);
            pb_upload = (ProgressBar) itemView.findViewById(R.id.pb_upload);
        }
    }
}
