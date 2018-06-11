package com.travel.localfile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.localfile.dao.LocalFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/1.
 */

public class LocalVideoCheckActivity extends TitleBarBaseActivity {
    private List<LocalFile> localFiles = new ArrayList<>();
    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private String activityId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video_check);
        localFiles = (List<LocalFile>) getIntent().getSerializableExtra("localFiles");

        if(getIntent().hasExtra("activityId")){
            activityId = getIntent().getStringExtra("activityId");
        }

        rightButton.setText("确认");
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalFile localFile = photoAdapter.getCheckLocalFile();
                if (localFile != null) {
                    Intent intent = new Intent(LocalVideoCheckActivity.this, PublishVideoActivity.class);
                    if(!TextUtils.isEmpty(activityId))
                        intent.putExtra("activityId", activityId);
                    intent.putExtra("isSaved", true);
                    intent.putExtra("localFile", localFile);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LocalVideoCheckActivity.this, "请选择视频", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photoAdapter = new PhotoAdapter();
        recyclerView.setAdapter(photoAdapter);
        photoAdapter.notifyDataSetChanged();
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        //        private CheckBox curCheckBox;
        private LocalFile checkLocalFile;
        private int checkedPos = -1;

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_evidence_select_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
            final LocalFile localFile = localFiles.get(position);
            holder.timeLong.setVisibility(View.VISIBLE);
            holder.timeLong.setText(localFile.getDurationFormat());
            String thumbnailPath = localFile.getLocalPath() + "_thumbnail";
            ImageDisplayTools.displayImage("file://" + thumbnailPath, holder.thumbnailImage);
            if (!OSUtil.isDayTheme())
                holder.thumbnailImage.setColorFilter(TravelUtil.getColorFilter(holder.itemView.getContext()));
            holder.pb_upload.setVisibility(View.GONE);

            if (position == checkedPos) {
                holder.radioButton.setChecked(true);
            } else
                holder.radioButton.setChecked(false);
            holder.radioButton.setTag(position);
            holder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkedPos = (Integer) holder.radioButton.getTag();
                    checkLocalFile = localFiles.get(checkedPos);
                    notifyDataSetChanged();
                }
            });
        }

        public LocalFile getCheckLocalFile() {
            return checkLocalFile;
        }

        @Override
        public int getItemCount() {
            if (localFiles == null || localFiles.size() == 0)
                return 0;
            return localFiles.size();
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        RadioButton radioButton;
        TextView timeLong;
        ProgressBar pb_upload;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            thumbnailImage = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            radioButton = (RadioButton) itemView.findViewById(R.id.rb_check);
            timeLong = (TextView) itemView.findViewById(R.id.tv_time_long);
            pb_upload = (ProgressBar) itemView.findViewById(R.id.pb_upload);
        }
    }
}
