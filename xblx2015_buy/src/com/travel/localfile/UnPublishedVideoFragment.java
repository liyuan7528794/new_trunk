package com.travel.localfile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ctsmedia.hltravel.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.travel.activity.OneFragmentActivity;
import com.travel.layout.VideoViewFragment;
import com.travel.lib.helper.PullToRefreshHelper;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.LocalFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldkxingzhe on 2016/11/11.
 */
public class UnPublishedVideoFragment extends Fragment{
    @SuppressWarnings("unused")
    private static final String TAG = "UnPublishedVideoFragment";

    private LocalFileSQLiteHelper mLocalFileSQLiteHelper;
    private PullToRefreshGridView mPullToRefreshGridView;
    private GridView mGridView;

    private List<LocalFile> mLocalVideo = new ArrayList<>();

    private MAdapter mAdapter;

//    private VideoViewPopWindow mVideoViewPopWindow;
//    private IjkVideoView mVideoView;

    private View none_notify;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unpublished_video, container, false);
        mPullToRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.gv_grid_view);
        none_notify = view.findViewById(R.id.none_notify);
        mGridView = mPullToRefreshGridView.getRefreshableView();
        PullToRefreshHelper helper = new PullToRefreshHelper(mPullToRefreshGridView);
        helper.initPullDownToRefreshView(null);
        mAdapter = new MAdapter();
        mGridView.setAdapter(mAdapter);
        mPullToRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                new LoadFileTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {

            }
        });

        mLocalFileSQLiteHelper = new LocalFileSQLiteHelper(getActivity());
        mLocalFileSQLiteHelper.init();
        return view;
    }

    private void isShowNoneNotify(){
        if(mLocalVideo!=null && mLocalVideo.size()>0) {
            none_notify.setVisibility(View.GONE);
            mPullToRefreshGridView.setVisibility(View.VISIBLE);
        }else {
            none_notify.setVisibility(View.VISIBLE);
            mPullToRefreshGridView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadFileTask().execute();
    }

    private class LoadFileTask extends AsyncTask<Void, Void, List<LocalFile>>{
        @Override
        protected List<LocalFile> doInBackground(Void... params) {
            List<LocalFile> result = mLocalFileSQLiteHelper.loadAllVideo(UserSharedPreference.getUserId());
            for(int i = result.size() - 1; i >= 0; i--){
                LocalFile tmp = result.get(i);
                File file = new File(tmp.getLocalPath());
                if(!file.exists()){
                    mLocalFileSQLiteHelper.delete(tmp);
                    result.remove(tmp);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<LocalFile> localFiles) {
            mLocalVideo.clear();
            mLocalVideo.addAll(localFiles);

            isShowNoneNotify();
            mAdapter.notifyDataSetChanged();
            mPullToRefreshGridView.onRefreshComplete();
        }
    };

    private class MAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mLocalVideo.size();
        }

        @Override
        public LocalFile getItem(int position) {
            return mLocalVideo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_video_publish, parent, false);
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                params.width = params.height =
                        (OSUtil.getScreenWidth() - OSUtil.dp2px(parent.getContext(),16))/2;
                convertView.setLayoutParams(params);
                viewHolder = new ViewHolder(
                        convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.publishImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LocalVideoUploadActivity.class);
                    intent.putExtra("localFile", getItem(position));
                    startActivity(intent);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if(mVideoViewPopWindow == null){
//                        mVideoViewPopWindow = new VideoViewPopWindow();
//                        mVideoView = new IjkVideoView(getActivity());
//                    }
//
//                    mVideoView.setVideoPath(getItem(position).getLocalPath());
//                    mVideoView.start();
//                    mVideoViewPopWindow.show(getActivity(), mGridView, mVideoView);
                    Bundle bundle = new Bundle();
                    bundle.putString("path", getItem(position).getLocalPath());
                    OneFragmentActivity.startNewActivity(getContext(), "", VideoViewFragment.class, bundle);

                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialogUtils.alertDialog(v.getContext(), "是否确认删除录像?", new Runnable() {
                        @Override
                        public void run() {
                            LocalFile localFile = mLocalVideo.get(position);
                            mLocalFileSQLiteHelper.delete(localFile);
                            mLocalVideo.remove(localFile);
                            isShowNoneNotify();
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    return true;
                }
            });
            LocalFile localFile = getItem(position);
            String thumbNailImage = "file://" + localFile.getLocalPath() + "_thumbnail";
            ImageDisplayTools.displayImage(thumbNailImage, viewHolder.bgImage);
            return convertView;
        }

        private class ViewHolder{
            ImageView bgImage;
            ImageView publishImage;
            View root;

            public ViewHolder(View root){
                this.root = root;
                publishImage = (ImageView) root.findViewById(R.id.iv_upload);
                bgImage = (ImageView) root.findViewById(R.id.iv_bg);
            }
        }
    }
}
