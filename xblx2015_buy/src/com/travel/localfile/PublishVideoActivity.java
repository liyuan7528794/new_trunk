package com.travel.localfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.ShopConstant;
import com.travel.lib.TravelApp;
import com.travel.lib.helper.SelectCoverHelper;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.JsUploadHelper;
import com.travel.localfile.upload.IUploadFile;
import com.travel.localfile.upload.factory.UGCUploadFactory;
import com.travel.localfile.upload.inteface.UploadFileCallback;
import com.travel.map.AmapLocationActivity;
import com.travel.video.bean.XpaiCofig;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.travel.communication.entity.MessageEntity.mUserId;

/**
 * Created by Administrator on 2017/7/31.
 */

public class PublishVideoActivity extends TitleBarBaseActivity implements SelectCoverHelper.Listener {
    private final String TAG = "PublishVideoActivity";
    private ImageView iv_cover;
    private EditText et_title;
    private TextView tv_address, tv_publish, tv_save;
    private LinearLayout ll_address;

    private String mCoverId = "-1";
    private Bitmap bitmap;
    private LocalFile mLocalFile;
    private boolean isSaved = true;

    private SelectCoverHelper mSelectCoverHelper;
    private IUploadFile uploadFile;
    private LocalFileSQLiteHelper mLocalFileSQLiteHelper;
    private UploadPopWindow mUploadPopWindow;
    private HttpRequest mHttpRequest;

    private double latitude = XpaiCofig.latitude;
    private double longitude = XpaiCofig.longitude;
    private String place = "";

    private String activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_video);

        mHttpRequest = new HttpRequest(this);
        uploadFile = UGCUploadFactory.getInstance().createUploadFile(this);
        mLocalFileSQLiteHelper = new LocalFileSQLiteHelper(this);
        mLocalFileSQLiteHelper.init();

        mSelectCoverHelper = new SelectCoverHelper(this, null, "evidence");

        initData();
        init();
        updateWithNewLocation(latitude, longitude);

        mSelectCoverHelper.setImageView(iv_cover).setListener(this);

        showNotSupport();
    }

    private void showNotSupport() {
        if (JsUploadHelper.isOperationSupported() == -1) {
            AlertDialogUtils.alertDialogOneButton(this, "该型号手机不支持上传功能", new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            });
        }
    }

    private void init() {
        iv_cover = (ImageView) findViewById(R.id.iv_cover);
        et_title = (EditText) findViewById(R.id.et_title);
        tv_address = (TextView) findViewById(R.id.tv_address);
        ll_address = (LinearLayout) findViewById(R.id.ll_address);
        tv_publish = (TextView) findViewById(R.id.tv_publish);
        tv_save = (TextView) findViewById(R.id.tv_save);

        iv_cover.setOnClickListener(coverClickListener);
        tv_save.setOnClickListener(saveClickListener);
        tv_publish.setOnClickListener(publishClickListener);
        ll_address.setOnClickListener(addressClickListener);

        tv_address.setText(UserSharedPreference.getAddress());
        ImageDisplayTools.displayImage("file://" + mLocalFile.getThumbnailPath(), iv_cover);
        if (!OSUtil.isDayTheme())
            iv_cover.setColorFilter(TravelUtil.getColorFilter(this));

        String thumbnailPath = mLocalFile.getLocalPath() + "_thumbnail";
        mSelectCoverHelper.uploadHeadFile(thumbnailPath);

    }

    private void initData() {
        mLocalFile = (LocalFile) getIntent().getSerializableExtra("localFile");
        isSaved = getIntent().getBooleanExtra("isSaved", true);
        mUserId = UserSharedPreference.getUserId();
        if (getIntent().hasExtra("activityId")) {
            activityId = getIntent().getStringExtra("activityId");
        }
    }

    View.OnClickListener addressClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PublishVideoActivity.this, AmapLocationActivity.class);
            startActivityForResult(intent, AmapLocationActivity.REQUEST_CODE);
        }
    };

    View.OnClickListener coverClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 选取封面
            mSelectCoverHelper.pickFromGallery();
        }
    };

    private boolean isFirst = true;
    View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 保存到本地
            if (isSaved)
                Toast.makeText(PublishVideoActivity.this, "视频已存在", Toast.LENGTH_SHORT).show();
            else {
                if (isFirst) {
                    mLocalFileSQLiteHelper.insert(mLocalFile);
                    isFirst = false;
                } else {
                    Toast.makeText(PublishVideoActivity.this, "视频已保存", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    View.OnClickListener publishClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OSUtil.hideKeyboard(PublishVideoActivity.this);
            // 发布视频
            if ("-1".equals(mCoverId)) {
                Toast.makeText(PublishVideoActivity.this, "封面正在上传中， 稍后点击", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("".equals(et_title.getText().toString().trim())) {
                Toast.makeText(PublishVideoActivity.this, "请输入标题", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mUploadPopWindow == null) {
                mUploadPopWindow = new UploadPopWindow(PublishVideoActivity.this);
                mUploadPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        onBackPressed();
                    }
                });
            }
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
                    Toast.makeText(PublishVideoActivity.this, "视频上传失败， 请联系管理员", Toast.LENGTH_SHORT).show();
                    showNotSupport();
                    mUploadPopWindow.dismiss();
                }

                @Override
                public void onUploadFileComplete(int index, String url) {
                    mHttpRequest.liveVideoUpload(mUserId, url, mCoverId, et_title.getText().toString().trim());
                }
            });
            mUploadPopWindow.showAtLocation(iv_cover, 0, 0, 0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case AmapLocationActivity.REQUEST_CODE:
                Bundle bundle = data.getExtras();
                place = bundle.getString("address");
                latitude = bundle.getDouble("latitude");
                longitude = bundle.getDouble("longitude");
                tv_address.setText(place);
                break;
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

    @Override
    public void onImageUploadSuccess(String url, String coverId, Bitmap bitmap) {
        mCoverId = url;
        this.bitmap = bitmap;
    }

    @Override
    public void onUploadFileFailed() {
        Toast.makeText(this, "上传封面失败，请稍后重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }


    private class HttpRequest {
        private Context mContext;

        HttpRequest(Context context) {
            mContext = context;
            updateWithNewLocation(XpaiCofig.latitude, XpaiCofig.longitude);
        }

//        public void liveVideoUpload(String userId, String videoUrl, String coverId, String title) {
//            String url = Constants.Root_Url + "/upload/liveVideoUpload.do";
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("title", title);
//            map.put("userId", userId);
//            map.put("imgUrl", coverId);
////            map.put("filedId", remoteFileId);
//            map.put("url", videoUrl);
//            map.put("longitude", longitude);
//            map.put("latitude", latitude);
//            if (TextUtils.equals(activityId, "-100")) {
//                map.put("type", 2);
//            } else {
//                map.put("type", 1);
//                if (!TextUtils.isEmpty(activityId))
//                    map.put("activityId", activityId);
//            }
//            map.put("place", place);
//            NetWorkUtil.postForm(mContext, url, new MResponseListener() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    super.onResponse(response);
//                    if (response.optInt("error") == 0) {
//                        MLog.v(TAG, "onDataFine");
//                        mUploadPopWindow.onComplete();
//                    /*if(mLocalFileSQLiteHelper != null){
//                        mLocalFileSQLiteHelper.delete(mLocalFile);
//                        mUploadPopWindow.onComplete();
//                    }*/
//                    }
//                }
//            }, map);
//        }

        private void liveVideoUpload(String userId, String videoUrl, String coverId, String title){
            Map<String, Object> map = new HashMap<>();
            map.put("content", title);
            map.put("userId", userId);
            map.put("imgUrl", coverId);
            map.put("videoUrl", videoUrl);
            map.put("location", place);
            NetWorkUtil.postForm(mContext, ShopConstant.TALK_ADD, new MResponseListener() {
                @Override
                protected void onNetComplete() {
                    super.onNetComplete();
                    LoadingDialog.getInstance(mContext).hideProcessDialog(0);
                }

                @Override
                protected void onDataFine(JSONObject data) {
                    super.onDataFine(data);
                    showToast("发布成功！");
                    sendBroadcast(new Intent("RefreshTalkList"));
                    ((Activity)mContext).finish();
                }

                @Override
                protected void onMsgWrong(String msg) {
                    super.onMsgWrong(msg);
                    showToast("发布失败！");
                }
            }, map);
        }
    }

    /**
     * 根据经纬度返回当前城市
     *
     * @param latitude
     * @param longitude
     * @return
     */
    private void updateWithNewLocation(final double latitude, final double longitude) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String addressStr = "";
                Geocoder geocoder = new Geocoder(TravelApp.appContext, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        addressStr = address.getLocality();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                place = addressStr;
            }
        }).start();
    }

}
