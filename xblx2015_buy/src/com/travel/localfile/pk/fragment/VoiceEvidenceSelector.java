package com.travel.localfile.pk.fragment;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.DateFormatUtil;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.UploadFileHelper;

/**
 * 音频证据上传选择页
 * Created by ldkxingzhe on 2017/2/13.
 */
public class VoiceEvidenceSelector extends AbstractEvidenceSelect{
    @SuppressWarnings("unused")
    private static final String TAG = "VoiceEvidenceSelector";
    private VoiceAdapter mAdapter;

    public VoiceEvidenceSelector(Context context, String userId) {
        super(context, userId);
        mAdapter = new VoiceAdapter();
        init();
    }

    @Override
    void loadFile() {
        loadFileFromDB(CameraFragment.TYPE_AUDIO);
    }

    @Override
    RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(mContext);
    }

    private class VoiceAdapter extends RecyclerView.Adapter<VoiceViewHolder>{

        @Override
        public VoiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_evidence_select_voice, parent, false);
            return new VoiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final VoiceViewHolder holder, int position) {
            LocalFile localFile = mLocalFileList.get(position);
            holder.recordTime.setText("录音日期: " + localFile.getCreateTime());
            holder.timeLong.setText(localFile.getDurationFormat());
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(mSelectedList.contains(localFile));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int adapterPosition = holder.getAdapterPosition();
                    LocalFile inLocalFile = mLocalFileList.get(adapterPosition);
                    if (isChecked){
                        if (!select(inLocalFile)){
                            buttonView.setChecked(false);
                            return;
                        }
                        mUploadFileHelper.uploadFile(inLocalFile, new UploadFileHelper.ProgressCallback() {
                            @Override
                            public void onProgress(int progress) {

                            }

                            @Override
                            public void onFailed(String errorMessage) {

                            }

                            @Override
                            public void onComplete(LocalFile lf) {

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

    private static class VoiceViewHolder extends RecyclerView.ViewHolder{
        private TextView recordTime, timeLong;
        private CheckBox checkBox;
        public VoiceViewHolder(View itemView) {
            super(itemView);
            recordTime = (TextView) itemView.findViewById(R.id.tv_record_time);
            timeLong = (TextView) itemView.findViewById(R.id.tv_time_long);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_check);
        }
    }
}
