package com.travel.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.travel.layout.PhotoChooseDialog;
import com.travel.lib.R;
import com.travel.lib.helper.SelectCoverHelper;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.lib.utils.UserSharedPreference;

/**
 * Created by Administrator on 2016/10/28.
 */

public class ChangeHeadImageActivity extends Activity implements SelectCoverHelper.Listener{

    private ImageView headImage;
    private Button commitButton;
    private PhotoChooseDialog mPhotoChooseDialog;

    private SelectCoverHelper mSelectCoverHelper;
    private boolean isSuccess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_head_image);
        headImage = (ImageView) findViewById(R.id.headImg);
        commitButton = (Button) findViewById(R.id.commitButton);
        mSelectCoverHelper = new SelectCoverHelper(this, null, "headPhoto");
        mSelectCoverHelper.setListener(this);
        headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoChooseDialog == null) {
                    // 实例化SelectPicPopupWindow
                    mPhotoChooseDialog = new PhotoChooseDialog(ChangeHeadImageActivity.this,
                            "图库", "拍照");
                }

                // 显示窗口
                mPhotoChooseDialog.show();

                mPhotoChooseDialog.setTakePhotoClick(new PhotoChooseDialog.DialogTakePhotoListener() {

                    @Override
                    public void takePhotoClick(View view) {
                        mSelectCoverHelper.pickFromCamera();
                    }
                });
                mPhotoChooseDialog.setMapStorageClick(new PhotoChooseDialog.DialogMapStorageListener() {

                    @Override
                    public void mapStorageClick(View view) {
                        mSelectCoverHelper.pickFromGallery();
                    }
                });

                mPhotoChooseDialog.setCancleClick(new PhotoChooseDialog.DialogCancleListener() {
                    @Override
                    public void cancleClick(View view) {

                    }
                });
            }
        });

        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSuccess)
                    finish();
                else
                    TravelUtil.showToast("头像设置有误，请重新设置！");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SelectCoverHelper.CAMERA_REQUEST_CODE:
                mSelectCoverHelper.backFromCamera();
                break;
            case SelectCoverHelper.REQUEST_CAMERA_CROP:
                mSelectCoverHelper.onCameraCropResult(resultCode, data);
                break;
            case SelectCoverHelper.REQUEST_IMAGE_PICK:
                // 图片拾取
                mSelectCoverHelper.onImagePickResult(resultCode, data);
                break;
            case SelectCoverHelper.REQUEST_IMAGE_CROP:
                mSelectCoverHelper.onImageCropResult(resultCode, data);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageUploadSuccess(String url, String coverId, Bitmap bitmap) {
        ImageDisplayTools.displayCircleImage(url, headImage, 0);
        if (!OSUtil.isDayTheme())
            headImage.setColorFilter(TravelUtil.getColorFilter(ChangeHeadImageActivity.this));
        UserSharedPreference.saveCoverId(coverId);
        UserSharedPreference.saveHeadImg(url);
        isSuccess = true;
    }

    @Override
    public void onUploadFileFailed() {
        isSuccess = false;
    }
}
