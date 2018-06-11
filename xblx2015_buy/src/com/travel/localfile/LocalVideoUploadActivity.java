package com.travel.localfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.lib.fragment_interface.Functions;
import com.travel.lib.helper.SelectCoverHelper;
import com.travel.lib.ui.BaseActivity;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.JsUploadHelper;
import com.travel.localfile.upload.IUploadFile;
import com.travel.localfile.upload.factory.UGCUploadFactory;
import com.travel.localfile.upload.inteface.UploadFileCallback;

import org.json.JSONObject;

import java.util.HashMap;

import static com.travel.communication.entity.MessageEntity.mUserId;

/**
 * 本地视频上传界面
 * Created by ldkxingzhe on 2016/11/10.
 */

public class LocalVideoUploadActivity extends BaseActivity implements SelectCoverHelper.Listener {
    @SuppressWarnings("unused")
    private static final String TAG = "LocalVideoUploadActivity";


    private Bitmap bitmap;
    private ImageView mCoverImageView;
    private TextView mPickFromCamera;
    private TextView mPickFromGallery;
    private EditText mTitleText;
    private ImageView mCancelText;
    private Button mPublishBtn;

    private UploadPopWindow mUploadPopWindow;

    private SelectCoverHelper mSelectCoverHelper;
    private HttpRequest mHttpRequest;
    private LocalFileSQLiteHelper mLocalFileSQLiteHelper;
//    private JsUploadHelper mJsUploadHelper;
    private IUploadFile uploadFile;
    private String mUrl = "-1", mUserId, mUserName;
    private String mVideoId;
    private LocalFile mLocalFile;
    private String mFilePath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_ready);
        mCoverImageView = findView(R.id.cover_imageView);
        mPickFromCamera = findView(R.id.go_camera);
        mPickFromGallery = findView(R.id.from_storage);
        mPublishBtn = findView(R.id.startLive);
        mCancelText = findView(R.id.cancle);
        mTitleText = findView(R.id.live_title_edit);

        findViewById(R.id.productLinearLayout).setVisibility(View.GONE);
        findViewById(R.id.tv_share_show).setVisibility(View.GONE);
        findViewById(R.id.ll_share).setVisibility(View.GONE);

        mLocalFile = (LocalFile) getIntent().getSerializableExtra("localFile");
        mFilePath = mLocalFile.getLocalPath();
        mSelectCoverHelper = new SelectCoverHelper(this, null, "evidence");
        mSelectCoverHelper.setImageView(mCoverImageView)
                .setListener(this);
        mHttpRequest = new HttpRequest(this);
//        mJsUploadHelper = new JsUploadHelper(this);
        uploadFile = UGCUploadFactory.getInstance().createUploadFile(this);
        mLocalFileSQLiteHelper = new LocalFileSQLiteHelper(this);
        mLocalFileSQLiteHelper.init();
        initListener();
        initView();
        showNotSupport();
    }

    private void showNotSupport() {
        if(JsUploadHelper.isOperationSupported() == -1){
            AlertDialogUtils.alertDialogOneButton(this, "该型号手机不支持上传功能", new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            });
        }
    }

    private void initView() {
        mUserId = UserSharedPreference.getUserId();
        mUserName = UserSharedPreference.getNickName();
        mPublishBtn.setText("发布");
        mTitleText.setText(mUserName + "的直播");
        View imageView = findViewById(R.id.imageRL);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = params.width = OSUtil.getScreenWidth()-OSUtil.dp2px(this,90);
        imageView.setLayoutParams(params);
        String thumbnailPath = mLocalFile.getLocalPath() + "_thumbnail";
        mSelectCoverHelper.uploadHeadFile(thumbnailPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) return;
        switch (requestCode){
            case SelectCoverHelper.CAMERA_REQUEST_CODE:
                mSelectCoverHelper.backFromCamera();
                break;
            case SelectCoverHelper.REQUEST_CAMERA_CROP:
                mSelectCoverHelper.onCameraCropResult(resultCode, data);
                break;
            case SelectCoverHelper.REQUEST_IMAGE_CROP:
                mSelectCoverHelper.onImageCropResult(resultCode, data);
                break;
            case SelectCoverHelper.REQUEST_IMAGE_PICK:
                mSelectCoverHelper.onImagePickResult(resultCode, data);
                break;
            default:
                MLog.d(TAG, "no this request code. %d", requestCode);
        }
    }

    private void initListener() {
        mPickFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectCoverHelper.pickFromCamera();
            }
        });

        mPickFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectCoverHelper.pickFromGallery();
            }
        });

        mPublishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mUrl)){
                    Toast.makeText(LocalVideoUploadActivity.this, "封面正在上传中， 稍后点击", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mUploadPopWindow == null){
                    mUploadPopWindow = new UploadPopWindow(LocalVideoUploadActivity.this);
                    mUploadPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            onBackPressed();
                        }
                    });
                }
//                mJsUploadHelper.uploadFile(mFilePath);
                uploadFile.UploadFile(mLocalFile, new UploadFileCallback() {
                    @Override
                    public void onCalculationSha1(int index, int progress) {
                        mUploadPopWindow.setProgress(progress);
                    }

                    @Override
                    public void onUploadFile(int index, int progress) {
                        mUploadPopWindow.setProgress(progress);
                    }

                    @Override
                    public void onError(int index) {
                        Toast.makeText(LocalVideoUploadActivity.this, "视频上传失败， 请联系管理员", Toast.LENGTH_SHORT).show();
                        showNotSupport();
                        mUploadPopWindow.dismiss();
                    }

                    @Override
                    public void onUploadFileComplete(int index, String url) {
                        mVideoId = url;
                        mHttpRequest.liveVideoUpload(mUserId, mVideoId, mUrl, mTitleText.getText().toString().trim());
                    }
                });

                mUploadPopWindow.showAtLocation(mCoverImageView, 0, 0, 0);
            }
        });

        mCancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*mJsUploadHelper.setListener(new JsUploadHelper.Listener() {
            @Override
            public void onCalculationSha1(int index, int progress) {
                mUploadPopWindow.setProgress(progress);
            }

            @Override
            public void onUploadFile(int index, int progress) {
                mUploadPopWindow.setProgress(progress);
            }

            @Override
            public void onError(int index) {
                Toast.makeText(LocalVideoUploadActivity.this, "视频上传失败， 请联系管理员", Toast.LENGTH_SHORT).show();
                showNotSupport();
            }

            @Override
            public void onUploadFileComplete(int index, String remoteFileId) {
                mVideoId = remoteFileId;
                mHttpRequest.liveVideoUpload(mUserId, mVideoId, mUrl, mTitleText.getText().toString().trim());
            }
        });*/
    }

    @Override
    public void onImageUploadSuccess(String url, String coverId, Bitmap bitmap) {
        mUrl = url;
        this.bitmap = bitmap;

    }

    @Override
    public void onUploadFileFailed() {
        Toast.makeText(this, "上传封面失败，请稍后重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void addFunction(Functions functions) {

    }

    private class HttpRequest{
        private Context mContext;
        HttpRequest(Context context){
            mContext = context;
        }

        public void liveVideoUpload(String userId, String remoteFileId, String coverUrl, String title){
            String url = Constants.Root_Url + "/upload/liveVideoUpload.do";
            HashMap<String,Object> map = new HashMap<>();
            map.put("title", title);
            map.put("userId", userId);
			map.put("imgUrl", coverUrl);
//            map.put("filedId", remoteFileId);
            map.put("url", remoteFileId);
            NetWorkUtil.postForm(mContext, url, new MResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    super.onResponse(response);
                    if (response.optInt("error") == 0) {
                        MLog.v(TAG, "onDataFine");
                        if (mLocalFileSQLiteHelper != null) {
                            mLocalFileSQLiteHelper.delete(mLocalFile);
                            mUploadPopWindow.onComplete();
                        }
                    }
                }
            }, map);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }
}
