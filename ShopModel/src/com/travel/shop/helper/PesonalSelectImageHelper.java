package com.travel.shop.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;

import com.travel.ShopConstant;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2017/1/12.
 */

public class PesonalSelectImageHelper {
    private final static String TAG = "PesonalSelectImageHelper";
    private Activity mActivity;

    public static final int REQUEST_IMAGE_PICK = 100;
    public static final int REQUEST_IMAGE_CROP = REQUEST_IMAGE_PICK + 1;
    public static final int REQUEST_CAMERA_CROP = REQUEST_IMAGE_CROP + 1;
    public static final int CAMERA_REQUEST_CODE = REQUEST_CAMERA_CROP + 1;
    public static final String IMAGE_FILE_LOCATION = "file:///sdcard/temp.jpg";
    public Uri imageUri = Uri.parse(IMAGE_FILE_LOCATION);
    private String photoPath;
    public PesonalSelectImageHelper(Activity activity){
        mActivity = activity;
    }

    public PesonalSelectImageHelper setListener(Listener listener){
        mListener = listener;
        return this;
    }
    private Listener mListener;
    public interface Listener{
        void onUploadImageSuccess(String filePath, int id);
        void onImageResultBitmap(Bitmap bitmap);
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
     *裁剪照片后获取图片
     */
    public void onCameraCropResult(int resultCode, Intent data){
        try {
            if(resultCode != Activity.RESULT_OK || data == null) return ;

            Bitmap bitmap = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(imageUri));
            mListener.onImageResultBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onImageCropResult(int resultCode, Intent data, int coverId){
        if(resultCode != Activity.RESULT_OK || data == null) return;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        addPhotoData(bitmap, coverId);
        mListener.onImageResultBitmap(bitmap);

    }

    private String currentFilePath;
    public void onImagePickResult(int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && data!=null){
            Uri uri = data.getData();
            File tmpFile = new File(Environment.getExternalStorageDirectory() + "/tmp/");
            if (!tmpFile.exists())
                tmpFile.mkdirs();
            photoPath = Environment.getExternalStorageDirectory() + "/tmp/" + UUID.randomUUID() + ".png";
            tmpFile = new File(photoPath);
            currentFilePath = "file://" + tmpFile.getAbsolutePath();
            MLog.v(TAG, "ImagePick back, and imageResult is " + uri + ", currentFilePath is " + currentFilePath);
            Intent cropIntent = OSUtil.getPerformCrop(uri, 3, 2, Uri.fromFile(tmpFile), 1080, 782);
            startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
        }
    }

    public void pickFromGallery(){
        //从图库选择封面
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//打开图像库
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void startActivityForResult(Intent intent, int requestCoce){
        mActivity.startActivityForResult(intent, requestCoce);
    }

    /**
     * 修改背景图片
     */
    private void addPhotoData(Bitmap backImage, int coverId) {
        String imageStr = Bitmap2StrByBase64(backImage);
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("userId", UserSharedPreference.getUserId());
        map.put("imgFile", imageStr);
        map.put("imgId", coverId);
        NetWorkUtil.postForm(mActivity, ShopConstant.MY_PAGE_BACKGROUD, new MResponseListener(mActivity) {

            @Override
            public void onResponse(JSONObject response) {
                if (response.optInt("error") == 0) {
                    mListener.onUploadImageSuccess(currentFilePath, response.optInt("data"));
                    // mViewPager.setCurrentItem(mAdapter.getCount());
                }
            }

        }, map);

    }

    /**
     * 通过Base32将Bitmap转换成Base64字符串
     */
    private String Bitmap2StrByBase64(Bitmap bit){

        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        String imageStr = Base64.encodeToString(bytes, Base64.DEFAULT);
        return imageStr;
    }

    public void BitmapToStrByBase64(final Bitmap bitmap){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Bitmap2StrByBase64(bitmap);
            }
        });
    }
}
