package com.travel.localfile.pk.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.google.gson.JsonArray;
import com.travel.Constants;
import com.travel.activity.OneFragmentActivity;
import com.travel.communication.adapter.ListBaseAdapter;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.LocalFileGridFragment;
import com.travel.localfile.LocalFileLookFragment;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.pk.entity.LocalFileUtil;
import com.travel.localfile.pk.others.FileUploadService;
import com.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择证据界面
 * Created by ldkxingzhe on 2016/7/4.
 */
public class EvidenceSelectFragment extends Fragment
        implements AdapterView.OnItemClickListener, OneFragmentActivity.OneFragmentInterface {
    @SuppressWarnings("unused")
    private static final String TAG = "EvidenceSelectFragment";

    private static final int MAX_ITEM = 9; // 图片的最大张数

    private GridView mGridView;
    private TextView mCompleteText;
    private EditText mIntroductionTextView;

    private ProgressDialog mProgressDialog;

    // 模拟数据
    private String mUserId = "";
    private int mPublicVoteId;
    private boolean mIsSeller = false;

    private HttpRequest mHttpRequest;
    /**
     * 已经选择的文件列表
     */
    private ArrayList<LocalFile> mSelectedFileList = new ArrayList<LocalFile>();
    private MGridViewAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            mPublicVoteId = bundle.getInt(VoteFragment.BUNDLE_VOTE_ID, mPublicVoteId);
            mIsSeller = bundle.getBoolean("is_seller", false);
        }
        mHttpRequest = new HttpRequest(getActivity(), mPublicVoteId);
    }

    @Override
    public void onResume() {
        super.onResume();
        mUserId = UserSharedPreference.getUserId();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_evidence_select, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gv_grid_view);
        mCompleteText = (TextView) rootView.findViewById(R.id.tv_complete);
        mIntroductionTextView = (EditText) rootView.findViewById(R.id.tv_introduction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCompleteText.setTranslationZ(50);
        }

        mAdapter = new MGridViewAdapter(mSelectedFileList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mCompleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickComplete();
            }
        });
        return rootView;
    }

    private void onClickComplete() {
        if(TextUtils.isEmpty(getIntroduction()) && mSelectedFileList.isEmpty()){
            Toast.makeText(getActivity(), "证据项不能全部为空", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArray jsonArray = new JsonArray();
        for(int i = 0, length = mSelectedFileList.size(); i < length; i++){
            jsonArray.add(LocalFileUtil.toJsonObject(mSelectedFileList.get(i)));
        }
        mHttpRequest.addEvidencePacket(getIntroduction(), jsonArray.toString(),
                 mUserId, mNetResponseListener);
        showProgressDialog();
    }

    private void showProgressDialog(){
        mProgressDialog = ProgressDialog.show(getActivity(), null, "证据上传中");
    }

    private void hideProgressDialog(){
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private String getIntroduction(){
        return mIntroductionTextView.getText().toString().trim();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mAdapter.isPlusImage(position)){
            Intent intent = new Intent(getActivity(), OneFragmentActivity.class);
            intent.putExtra(OneFragmentActivity.TITLE, "");
            intent.putExtra(OneFragmentActivity.CLASS, LocalFileGridFragment.class.getName());
            Bundle bundle = new Bundle();
            bundle.putInt(LocalFileGridFragment.ALL_CAN_SELECTED_NUM, MAX_ITEM);
            bundle.putSerializable(LocalFileGridFragment.SELECTED_LOCAL_FILE_LIST, mSelectedFileList);
            bundle.putBoolean(LocalFileGridFragment.HAS_FEATURE_DELETE,false);
            intent.putExtra(OneFragmentActivity.BUNDLE, bundle);
            startActivityForResult(intent, 1);
        }else{
            Intent intent = new Intent(getActivity(), OneFragmentActivity.class);
            intent.putExtra(OneFragmentActivity.TITLE, "");
            intent.putExtra(OneFragmentActivity.CLASS, LocalFileLookFragment.class.getName());
            Bundle bundle = new Bundle();
            bundle.putBoolean(LocalFileLookFragment.HAS_FEATURE_DELETE, true);
            bundle.putSerializable(LocalFileLookFragment.LOCAL_FILE_LIST, mSelectedFileList);
            bundle.putInt(LocalFileLookFragment.CURRENT_POSITION, position);
            intent.putExtra(OneFragmentActivity.BUNDLE, bundle);
            startActivityForResult(intent, 2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) return;
        // 无论是删除还是选择都是这样的
        ArrayList<LocalFile> selectFile = null;
        if(data != null){
            if(requestCode == 1){
                // 选择多媒体
                selectFile = (ArrayList<LocalFile>) data.getSerializableExtra(LocalFileGridFragment.SELECTED_LOCAL_FILE_LIST);
            }else if(requestCode == 2){
                // 删除多媒体
                selectFile = (ArrayList<LocalFile>) data.getSerializableExtra(LocalFileLookFragment.LOCAL_FILE_LIST);
            }
        }
        if(selectFile != null){
            mSelectedFileList.clear();
            mSelectedFileList.addAll(selectFile);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onTouchDown() {
        OSUtil.hideKeyboard(getActivity());
    }

    private ResultReceiver mUploadFileReceiver = new ResultReceiver(new Handler()){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            MLog.v(TAG, "resultCode is %d.", resultCode);
            if(resultCode == FileUploadService.ALL_COMPLETE){
                // 文件上传完成
                ArrayList<LocalFile> localList = (ArrayList<LocalFile>) resultData.getSerializable(FileUploadService.UPLOAD_FILE_LIST);
                JsonArray jsonArray = new JsonArray();
                for(int i = 0, length = localList.size(); i < length; i++){
                    jsonArray.add(LocalFileUtil.toJsonObject(localList.get(i)));
                }
                mHttpRequest.addEvidencePacket(getIntroduction(), jsonArray.toString(),
                         mUserId, mNetResponseListener);
            }else if(resultCode == FileUploadService.SINGLE_COMPLETE){

            }else{
                MLog.e(TAG, "resultCode not found");
            }
        }
    };

    private class MGridViewAdapter extends ListBaseAdapter<LocalFile>{

        private int width;

        public MGridViewAdapter(List<LocalFile> list) {
            super(list);
        }

        @Override
        public int getCount() {
            int realCount = super.getCount();
            return realCount < MAX_ITEM ? realCount + 1 : realCount;
        }

        // 是否是加号
        public boolean isPlusImage(int position){
            if (super.getCount() >= MAX_ITEM) return false;
            return getCount() == position + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_local_file, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
                viewHolder.checkBox.setVisibility(View.GONE);
                makeSureWidthExist();
                ViewGroup.LayoutParams params = viewHolder.frameLayout.getLayoutParams();
                params.height = width;
                viewHolder.frameLayout.setLayoutParams(params);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if(isPlusImage(position)){
                viewHolder.bgImage.setImageResource(R.drawable.bg_dash_black26);
                viewHolder.typeImage.setImageResource(R.drawable.icon_item_add);
                viewHolder.typeImage.setVisibility(View.VISIBLE);
            }else{
                String bgUrl = null;
                boolean isTypeImageVisible = false;
                LocalFile localFile = getItem(position);
                switch (localFile.getType()){
                    case CameraFragment.TYPE_AUDIO:
                        bgUrl = "drawable://" + R.drawable.bg_record_voice;
                        viewHolder.typeImage.setImageResource(R.drawable.detail_icon_voice_white);
                        isTypeImageVisible = true;
                        break;
                    case CameraFragment.TYPE_PHOTO:
                        bgUrl = "file://" + localFile.getLocalPath();
                        isTypeImageVisible = false;
                        break;
                    case CameraFragment.TYPE_VIDEO:
                        viewHolder.typeImage.setImageResource(R.drawable.detail_icon_play_white);
                        bgUrl = "file://" + localFile.getLocalPath() + "_thumbnail";
                        isTypeImageVisible = true;
                        break;
                    case CameraFragment.TYPE_LIVE:
                        viewHolder.typeImage.setImageResource(R.drawable.detail_icon_play_white);
                        bgUrl = localFile.getThumbnailPath();
                        isTypeImageVisible = true;
                        break;
                }
                ImageDisplayTools.displayImage(bgUrl, viewHolder.bgImage);
                if(isTypeImageVisible){
                    viewHolder.typeImage.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.typeImage.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

        private void makeSureWidthExist(){
            if(width > 0) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                width = mGridView.getColumnWidth();
            }
            if(width <= 0){
                width = mGridView.getWidth() / 3;
            }
        }

        private class ViewHolder{
            ImageView bgImage, typeImage;
            View checkBox;
            View frameLayout;

            public ViewHolder(View convertView){
                frameLayout = convertView;
                bgImage = (ImageView) convertView.findViewById(R.id.iv_bg);
                typeImage = (ImageView) convertView.findViewById(R.id.iv_type);
                checkBox = convertView.findViewById(R.id.cb_select);
            }
        }
    }

    private MResponseListener mNetResponseListener = new MResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            super.onResponse(response);
            if (response.optInt("error") == 0) {
                hideProgressDialog();
                getActivity().finish();
            }
        }

        @Override
        protected void onNetComplete() {
            hideProgressDialog();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            super.onErrorResponse(error);
            hideProgressDialog();
        }
    };

    public static class HttpRequest{
        private final int publicVoteId;
        private final Context context;

        public HttpRequest(Context context, int publicVoteId) {
            this.publicVoteId = publicVoteId;
            this.context = context;
        }


        public void addEvidencePacket(String introduction, String include, String sellerOrBuyer, MResponseListener listener){
            String url = Constants.Root_Url + "/orders/addVoteData.do";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("publicVoteId", publicVoteId);
            map.put("reason", introduction);
            map.put("include", include);
            map.put("sellerorbuyer", sellerOrBuyer);
            NetWorkUtil.postForm(context, url, listener, map);
        }
    }
}
