package com.travel.lib.helper;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;

import com.travel.ShopConstant;
import com.travel.lib.TravelApp;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.volley.NormalPostRequest;
import com.volley.Request;
import com.volley.RequestQueue;
import com.volley.Response;
import com.volley.VolleyError;
import com.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * 选择封面的类
 * 包含了从相册选取与拍摄照片的处理，
 * 还包含了封面的上传返回Id的方法
 * Created by ldkxingzhe on 2016/11/10.
 */
public class SelectCoverHelper {
    private final static String TAG = "SelectCoverHelper";
    private Activity mActivity;
    private Fragment mFragment;

    public static final int REQUEST_IMAGE_PICK = 100;
    public static final int REQUEST_IMAGE_CROP = REQUEST_IMAGE_PICK + 1;
    public static final int REQUEST_CAMERA_CROP = REQUEST_IMAGE_CROP + 1;
    public static final int CAMERA_REQUEST_CODE = REQUEST_CAMERA_CROP + 1;

    private Uri imageUri;

    private ImageView mImageView;
    private String subFile;
    private final long TIMESTAMP = System.currentTimeMillis();
    public SelectCoverHelper(Activity activity, Fragment fragment, String subFile){
        mActivity = activity;
        mFragment = fragment;
        this.subFile = subFile;
        imageUri = Uri.fromFile(new File(TravelUtil.getFileAddress(subFile, TravelApp.appContext) + File.separator + TIMESTAMP +".png"));
    }

    public SelectCoverHelper setImageView(ImageView imageView){
        mImageView = imageView;
        return this;
    }

    public SelectCoverHelper setListener(Listener listener){
        mListener = listener;
        return this;
    }
    private Listener mListener;
    public interface Listener{
        /* noted: this called from a worker thread */
        void onImageUploadSuccess(String url, String coverId, Bitmap bitmap);
        /* noted: this called form a worker thread */
        void onUploadFileFailed();
    }

    public void pickFromGallery(){
        //从图库选择封面
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//打开图像库
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    public void pickFromCamera(){
        //HostWindowActivity.mUIHandler.sendEmptyMessage(HostWindowHandler.STOP_PREVIEW);
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (TravelUtil.GetSDState()) {
            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
    }

    private void startActivityForResult(Intent intent, int requestCoce){
        if(mFragment != null){
            mFragment.startActivityForResult(intent, requestCoce);
        }else{
            mActivity.startActivityForResult(intent, requestCoce);
        }
    }

    public void backFromCamera(){
        // 判断存储卡是否可以用，可用进行存储
        if (TravelUtil.GetSDState()) {
            startPhotoZoom(imageUri);
        } else {
            TravelUtil.showToast("未找到存储卡，无法存储照片！");
        }
    }

    /**
     *裁剪照片后获取图片
     */
    public void onCameraCropResult(int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK || data == null) return;
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(
                    mActivity.getContentResolver()
                            .openInputStream(imageUri));
            if(bitmap==null) return;
            if(mImageView != null){
                mImageView.setImageBitmap(bitmap);
            }
            //上传图片到服务器，并获取该图片的的网络地址
            new AsyncUpLoadedImage().execute(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onImagePickResult(int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && data!=null){
            // 当选择的图片不为空的话，在获取到图片的途径
            Uri uri = data.getData();
            Intent cropIntent = OSUtil.getPerformCrop(uri, 1, 1, imageUri, 500, 500);
            cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            startActivityForResult(cropIntent,
                    REQUEST_IMAGE_CROP);
        }
    }

    public void onImageCropResult(int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK || data == null) return;
        String headFilePath = TravelUtil.getFileAddress(subFile, TravelApp.appContext) + File.separator + TIMESTAMP +".png";
        uploadHeadFile(headFilePath);
    }

    public void uploadHeadFile(String headFilePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(headFilePath);
        if(mImageView != null){
            mImageView.setImageBitmap(bitmap);
        }
        //上传图片到服务器，并获取该图片的的网络地址
        new AsyncUpLoadedImage().execute(bitmap);
    }

    /**
     * 裁剪照片
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA_CROP);
    }

    /**
     * 上传图片异步操作
     */
    private class AsyncUpLoadedImage extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... params) {
            uploadImage(params[0]);
            return null;
        }
    }


    /**
     * 上传图片
     */
    private void uploadImage(final Bitmap bitmap) {
        String url = ShopConstant.UPLOAD_IMAGE;
        final String coverImage = Bitmap2StrByBase64(bitmap);
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("imgFile", coverImage);
        if(TextUtils.equals("headPhoto", subFile)){
            url = ShopConstant.HEAD_UPLOAD;
            paraMap.put("userId", UserSharedPreference.getUserId());
            paraMap.put("headImg", UserSharedPreference.getCoverId());
        }else
            paraMap.put("ownerId", UserSharedPreference.getUserId());

        RequestQueue mRequestQueue = Volley.newRequestQueue(mActivity);
        Request<JSONObject> request = new NormalPostRequest(mActivity,
                url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json_result) {
                try {
                    if (json_result.get("error")!=null && 0 == json_result.getInt("error")) {// 成功
                        String coverUrl = null, coverId = null;
                        if(json_result.getString("url")!=null){
                            coverUrl = JsonUtil.getJson(json_result, "url");
                            if(!"".equals(JsonUtil.getJson(json_result, "cover"))){
                                coverId = JsonUtil.getJson(json_result, "cover");
                            }
                        }
                        if(mListener != null) mListener.onImageUploadSuccess(coverUrl, coverId, bitmap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(mListener != null) mListener.onUploadFileFailed();
            }
        }, paraMap);
        mRequestQueue.add(request);
    }

    /**
     * 通过Base32将Bitmap转换成Base64字符串
     */
    public String Bitmap2StrByBase64(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
