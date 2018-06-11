package com.travel.localfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.VideoConstant;
import com.travel.activity.OneFragmentActivity;
import com.travel.bean.VideoInfoBean;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.UploadFileHelper;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地文件的网格化呈现,
 * 两种状态, 可选, 不可选
 * Created by ldkxingzhe on 2016/6/30.
 */
public class LocalFileGridFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = "LocalFileGridFragment";

    public static final String LOCAL_FILE_LIST = "local_file_list";
    public static final String SELECTED_LOCAL_FILE_LIST = "selected_position";
    public static final String ALL_CAN_SELECTED_NUM = "all_can_selected_num";
    public static final String HAS_FEATURE_LIVE = "has_live_feature";
    public static final String HAS_FEATURE_DELETE = "has_feature_delete";
    public static final String HAS_FEATURE_SELECTED = "has_feature_selected";
    public static final String HAS_FEATURE_DELETE_FROM_LOCAL = "has_feature_delete_from_local";

    private GridView mLiveGridView, mVideoGridView, mPictureGridView, mAudioGridView;
    private View mLiveLayout, mVideoLayout, mPictureLayout, mAudioLayout;
    private ArrayList<LocalFile> mLocalFileList;
    private ArrayList<LocalFile> mSelectLocalFileList;
    private ArrayList<LocalFile> mPictureLocalFileList;
    private ArrayList<LocalFile> mVideoLocalFileList;
    private ArrayList<LocalFile> mAudioLocalFileList;
    private ArrayList<LocalFile> mLiveFileList;
    private LocalFileSQLiteHelper mSQLiteHelper;

    private HttpRequest mHttpRequest;

    private int mAllCanSelectedNum;
    private boolean mHasFeatureLive; //是否有直播特性
    private boolean mHasFeatureDelete; // 是否拥有删除的权限
    private boolean mHasFeatureSelected; // 是否有批量选择的权限
    private boolean mHasFeatureDeleteFromLocal; // 是否拥有从磁盘删除的权限

    private static final int REQUEST_CODE_LIVE = 1;
    private static final int REQUEST_CODE_VIDEO = 2;
    private static final int REQUEST_CODE_AUDIO = 3;
    private static final int REQUEST_CODE_PHOTO = 4;

    private UploadFileHelper mUploadFileHelper;
    private Handler mHandler;

    private ScrollView sl_record;
    private RelativeLayout rl_no_record;
    private boolean isData;//是否有数据

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(SELECTED_LOCAL_FILE_LIST)) {
            mSelectLocalFileList = (ArrayList<LocalFile>) args.getSerializable(SELECTED_LOCAL_FILE_LIST);
        } else {
            mSelectLocalFileList = new ArrayList<>();
        }
        mLocalFileList = new ArrayList<LocalFile>();
        mAllCanSelectedNum = args != null ? args.getInt(ALL_CAN_SELECTED_NUM, 0) : 0;
        mHasFeatureLive = args != null && args.getBoolean(HAS_FEATURE_LIVE, true);
        mHasFeatureDelete = args != null && args.getBoolean(HAS_FEATURE_DELETE, true);
        mHasFeatureSelected = args != null && args.getBoolean(HAS_FEATURE_SELECTED, true);
        mHasFeatureDeleteFromLocal = args != null && args.getBoolean(HAS_FEATURE_DELETE_FROM_LOCAL, false);
        mSQLiteHelper = new LocalFileSQLiteHelper(getActivity());
        mSQLiteHelper.init();
        initData();
        dealWithData();
        mHttpRequest = new HttpRequest(getActivity());
        mUploadFileHelper = new UploadFileHelper(getActivity());
        mHandler = new Handler();
    }

    private void dealWithData() {
        mVideoLocalFileList = new ArrayList<LocalFile>();
        mAudioLocalFileList = new ArrayList<LocalFile>();
        mPictureLocalFileList = new ArrayList<LocalFile>();
        mLiveFileList = new ArrayList<LocalFile>();

        for (LocalFile localFile : mLocalFileList) {
            File file = new File(localFile.getLocalPath());
            if (!file.exists()) {
                // 从数据库中删除此项纪录
                mSQLiteHelper.delete(localFile);
                continue;
            }
            switch (localFile.getType()) {
                case CameraFragment.TYPE_AUDIO:
                    mAudioLocalFileList.add(localFile);
                    break;
                case CameraFragment.TYPE_VIDEO:
                    mVideoLocalFileList.add(localFile);
                    break;
                case CameraFragment.TYPE_PHOTO:
                    mPictureLocalFileList.add(localFile);
                    break;
            }
        }
    }

    private void initData() {
        List<LocalFile> list =
                mSQLiteHelper.loadAllFile(
                        UserSharedPreference.isLogin() ? UserSharedPreference.getUserId() : "",
                        null);
        mLocalFileList = new ArrayList<LocalFile>();
        mLocalFileList.addAll(list);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle result = data.getExtras();
        if (result != null) {
            GridView gridView = null;
            List<LocalFile> localFileList = null;
            View view = null;
            switch (requestCode) {
                case REQUEST_CODE_PHOTO:
                    gridView = mPictureGridView;
                    localFileList = mPictureLocalFileList;
                    view = mPictureLayout;
                    break;
                case REQUEST_CODE_AUDIO:
                    gridView = mAudioGridView;
                    localFileList = mAudioLocalFileList;
                    view = mAudioLayout;
                    break;
                case REQUEST_CODE_LIVE:
                    gridView = mLiveGridView;
                    localFileList = mLiveFileList;
                    view = mLiveLayout;
                    break;
                case REQUEST_CODE_VIDEO:
                    gridView = mVideoGridView;
                    localFileList = mVideoLocalFileList;
                    view = mVideoLayout;
                    break;
            }
            Collection<? extends LocalFile> localFiles =
                    (Collection<? extends LocalFile>) result.getSerializable(LOCAL_FILE_LIST);
            if (localFiles != null) {
                localFileList.clear();
                localFileList.addAll(localFiles);
                if (localFileList.isEmpty()) {
                    view.setVisibility(View.GONE);
                }
            }
            mSelectLocalFileList.clear();
            Collection<? extends LocalFile> selectionList =
                    (Collection<? extends LocalFile>) result.getSerializable(SELECTED_LOCAL_FILE_LIST);
            mSelectLocalFileList.addAll(selectionList);
            if (result.getBoolean("finish_local_file_grid_fragment", false)) {
                //                done();
            } else {
                ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local_file_grid, container, false);
        mLiveLayout = rootView.findViewById(R.id.live_layout);
        mAudioLayout = rootView.findViewById(R.id.audio_layout);
        mVideoLayout = rootView.findViewById(R.id.video_layout);
        mPictureLayout = rootView.findViewById(R.id.picture_layout);
        sl_record = (ScrollView) rootView.findViewById(R.id.sl_record);
        rl_no_record = (RelativeLayout) rootView.findViewById(R.id.rl_no_record);
        initTitleBar(rootView.findViewById(R.id.include_title_bar));
        initLocalFileGridView(mAudioLayout);
        initLocalFileGridView(mVideoLayout);
        initLocalFileGridView(mPictureLayout);
        initLocalFileGridView(mLiveLayout);
        if (mHasFeatureLive) {
            mHttpRequest.getVideoList(UserSharedPreference.getUserId());
        }
        if(!isData){
            sl_record.setVisibility(View.GONE);
            rl_no_record.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void initLocalFileGridView(View localFileLayout) {
        GridView gridView = (GridView) localFileLayout.findViewById(R.id.gv_grid_view);
        final ArrayList<LocalFile> localFileList;
        View layout;
        String name = "";
        final int requestCode;
        if (localFileLayout == mPictureLayout) {
            mPictureGridView = gridView;
            localFileList = mPictureLocalFileList;
            layout = mPictureLayout;
            name = "照片";
            requestCode = REQUEST_CODE_PHOTO;
        } else if (localFileLayout == mAudioLayout) {
            mAudioGridView = gridView;
            localFileList = mAudioLocalFileList;
            layout = mAudioLayout;
            name = "录音";
            requestCode = REQUEST_CODE_AUDIO;
        } else if (localFileLayout == mVideoLayout) {
            mVideoGridView = gridView;
            localFileList = mVideoLocalFileList;
            layout = mVideoLayout;
            name = "录像";
            requestCode = REQUEST_CODE_VIDEO;
        } else {
            mLiveGridView = gridView;
            localFileList = mLiveFileList;
            layout = mLiveLayout;
            name = "直播";
            requestCode = REQUEST_CODE_LIVE;
        }
        ((TextView) layout.findViewById(R.id.tv_name)).setText(name);
        LocalFileGridViewAdapter localFileGridViewAdapter = getLocalFileGridViewAdapter(localFileList, gridView);
        localFileGridViewAdapter.setHasFeatureSelected(mHasFeatureSelected);
        gridView.setAdapter(localFileGridViewAdapter);
        if (localFileList == null || localFileList.size() == 0) {
            layout.setVisibility(View.GONE);
        } else {
            isData = true;
            layout.setVisibility(View.VISIBLE);
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(LocalFileLookFragment.SELECTED_LOCAL_FILE_LIST, mSelectLocalFileList);
                bundle.putSerializable(LocalFileLookFragment.LOCAL_FILE_LIST, localFileList);
                bundle.putInt(LocalFileLookFragment.CURRENT_POSITION, position);
                bundle.putBoolean(LocalFileLookFragment.HAS_FEATURE_SELECT, true);
                bundle.putInt(LocalFileGridFragment.ALL_CAN_SELECTED_NUM, mAllCanSelectedNum);
                bundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE, mHasFeatureDelete);
                bundle.putBoolean(LocalFileLookFragment.HAS_FEATURE_SELECT, false);
                bundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE_FROM_LOCAL, mHasFeatureDeleteFromLocal);
                Intent intent = new Intent(getActivity(), OneFragmentActivity.class);
                intent.putExtra(OneFragmentActivity.TITLE, "");
                intent.putExtra(OneFragmentActivity.BUNDLE, bundle);
                intent.putExtra(OneFragmentActivity.CLASS, LocalFileLookFragment.class.getName());
                startActivityForResult(intent, requestCode);
            }
        });
    }

    private LocalFileGridViewAdapter
    getLocalFileGridViewAdapter(final ArrayList<LocalFile> list, final GridView gridView) {
        final LocalFileGridViewAdapter adapter
                = new LocalFileGridViewAdapter(list, new LocalFileGridViewAdapter.Listener() {
            @Override
            public boolean hasChecked() {
                return true;
            }

            @Override
            public boolean isPositionChecked(int position) {
                return mSelectLocalFileList.contains(list.get(position));
            }

            @Override
            public void addPosition(int position) {
                mSelectLocalFileList.add(list.get(position));
            }

            @Override
            public void removePosition(int position) {
                mSelectLocalFileList.remove(list.get(position));
            }

            @Override
            public boolean onCheckedChanged(final int position, boolean isChecked) {
                mUploadFileHelper.stopUploadFile(list.get(position));
                if (!isChecked || mHasFeatureDelete)
                    return false;
                if (mSelectLocalFileList.size() > mAllCanSelectedNum) {
                    mSelectLocalFileList.remove(list.get(position));
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    Toast.makeText(getActivity(),
                            "最多只能选择" + mAllCanSelectedNum + "个文件", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (list.get(position).getIsUpLoaded()) {
                    // 已上传的可以直接上传
                    return false;
                }
                if (mUploadFileHelper.isUploading()) {
                    Toast.makeText(getActivity(), "正在上传一份文件， 请稍后再试", Toast.LENGTH_SHORT).show();
                    mSelectLocalFileList.remove(list.get(position));
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    return true;
                } else {
                    mUploadFileHelper.uploadFile(list.get(position), new UploadFileHelper.ProgressCallback() {
                        @Override
                        public void onProgress(final int progress) {
                            MLog.v(TAG, "onProgress, and progress is %d", progress);
                            runInMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    list.get(position).setOthers(String.valueOf(progress));
                                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onFailed(String errorMessage) {
                            MLog.v(TAG, "onFailed, and errorMessage is %s.", errorMessage);
                            runInMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    list.get(position).setOthers("-1");
                                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onComplete(LocalFile lf) {
                            MLog.v(TAG, "onComplete");
                            runInMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    list.get(position).setOthers("101");
                                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
                return false;
            }

            @Override
            public int getItemWidth() {
                int result;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    result = gridView.getColumnWidth();
                } else {
                    result = (OSUtil.getScreenWidth() - OSUtil.dp2px(getContext(), 52))/ gridView.getNumColumns();
                }
                return result;
            }
        });
        return adapter;
    }

    private void runInMainThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    private void initTitleBar(View view) {
        View backView = view.findViewById(R.id.iv_title_bar_left);
        TextView titleText = (TextView) view.findViewById(
                mHasFeatureDelete ? R.id.tv_middle_title : R.id.tv_title);
        ImageView doneView = (ImageView) view.findViewById(R.id.iv_title_bar_right);
        doneView.setImageResource(
                mHasFeatureDelete ? R.drawable.icon_trash : R.drawable.icon_done);
        titleText.setText(mHasFeatureDelete ? "我的记录" : "选择证据");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            view.setTranslationZ(10);
//        } else {
            view.findViewById(R.id.line_title_bar).setVisibility(View.VISIBLE);
//        }
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });
        doneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasFeatureDelete) {
                    MLog.v(TAG, "删除数据");
                } else {
                    if (mUploadFileHelper != null && mUploadFileHelper.isUploading()) {
                        // 有文件正在上传
                        AlertDialogUtils.alertDialogOneButton(getActivity(),
                                "有一个文件正在上传， 确定将终止其上传", null);
                    } else {
                        done();
                    }
                }
            }
        });
        if (!mHasFeatureSelected)
            doneView.setVisibility(View.GONE);
    }

    /**
     * 选择完成, 返回结果
     */
    private void done() {
        Intent intent = new Intent();
        mSelectLocalFileList.remove(mUploadFileHelper.getCurrentUploadingFile());
        intent.putExtra(SELECTED_LOCAL_FILE_LIST, mSelectLocalFileList);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private class HttpRequest {
        private Context mContext;

        public HttpRequest(Context context) {
            mContext = context;
        }

        public void getVideoList(String userId) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("statusShow", 2);
            map.put("userId", userId);
            map.put("type", 1);

            NetWorkUtil.postForm(mContext, VideoConstant.VIDEO_LIST, new MResponseListener() {

                @Override
                protected void onDataFine(JSONArray data) {
                    if (data.length() > 0) {// 直播列表列表个数
                        for (int i = 0; i < data.length(); i++) {

                            VideoInfoBean entity = VideoInfoBean.getVideoInfoBean(JsonUtil.getJSONObject(data, i));
                            LocalFile localFile = new LocalFile();
                            localFile.setType(CameraFragment.TYPE_LIVE);
                            localFile.setIsUpLoaded(true);
                            localFile.setRemotePath(entity.getUrl());
                            localFile.setThumbnailPath(entity.getVideoImg());
                            localFile.setTag(entity);
                            localFile.setCreateTime(JsonUtil.getJsonLong(JsonUtil.getJSONObject(data, i), "createAt"));
                            localFile.setDuration(JsonUtil.getJsonLong(JsonUtil.getJSONObject(data, i), "duration"));
                            mLiveFileList.add(localFile);
                        }

                        ((LocalFileGridViewAdapter) mLiveGridView.getAdapter()).notifyDataSetChanged();
                        if (mLiveFileList.size() > 0) {
                            mLiveLayout.setVisibility(View.VISIBLE);
                        }

                        ((LocalFileGridViewAdapter) mLiveGridView.getAdapter()).notifyDataSetChanged();
                        if (mLiveFileList.size() > 0) {
                            mLiveLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        MLog.e(TAG, "没有直播");
                    }
                }
            }, map);
        }
    }
}
