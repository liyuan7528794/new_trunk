package com.travel.usercenter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.Constants;
import com.travel.layout.PhotoChooseDialog;
import com.travel.lib.helper.SelectCoverHelper;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.utils.HLLXLoginHelper;
import com.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PersonalDataActivity extends TitleBarBaseActivity implements SelectCoverHelper.Listener{
    private static final String TAG = "PersonalDataActivity";

    public final static String MODIFYNICKNAME = "nickname_modify";
    public final static String MODIFYCOUNTRY = "country_modify";
    public final static String MYINTRODUCTION = "my_introduction";
    // 请求吗
    private final static int REQUEST_NICK_NAME = 2;
    private final static int REQUEST_MYINTRODUCTION = 5;
    private final static int CHECK_DISTRICT = 3;// 选择地区

    private PhotoChooseDialog mPhotoChooseDialog;

    private ProgressDialog pd;
    private ImageView headImg;
    private EditText nicknameText;
    private LinearLayout ll_layout;
    private TextView myAcountText;
    private TextView myIntroductionText;

    private TableRow  passwordRow;

    private Button submit;
    private String oldName;

    // 头像选取相关
    private SelectCoverHelper mSelectCoverHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_personal_data);
        initView();
        initDate();
        initListener();
    }

    /** 切换屏幕处理 */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void initView() {
        titleText.setText("个人设置");
        OSUtil.setShareParam(rightButton, "save", this);
        headImg = (ImageView) findViewById(R.id.head_img);
        nicknameText = findView(R.id.my_nickname);
        ll_layout = findView(R.id.ll_layout);
        myAcountText = (TextView) findViewById(R.id.my_account);
        myIntroductionText = (TextView) findViewById(R.id.myIntroduction);
        passwordRow = (TableRow) findViewById(R.id.passwordRow);
        submit = (Button) findViewById(R.id.user_quit);
    }

    /**
     * 主布局获取焦点并隐藏键盘
     */
    private void layoutGetFocus() {
        OSUtil.hideKeyboard(PersonalDataActivity.this);
        ll_layout.setFocusable(true);
        ll_layout.setFocusableInTouchMode(true);
        ll_layout.requestFocus();
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
            case REQUEST_NICK_NAME:
                if (data == null)
                    return;
                String nickname = data.getStringExtra("modify_nickname");
                pd = ProgressDialog.show(PersonalDataActivity.this, null, "获取数据…");
                if (nickname != null)
                    modifyData("", nickname, "", "", "", null);
                break;
            case REQUEST_MYINTRODUCTION:
                if (data == null)
                    return;
                String myIntroduction = data.getStringExtra("modify_myIntroduction");
                pd = ProgressDialog.show(PersonalDataActivity.this, null, "获取数据…");
                if (myIntroduction != null)
                    modifyData("", "", "", "", "", myIntroduction);
                break;
            case CHECK_DISTRICT:
                if (data == null)
                    return;
                String address = data.getStringExtra("district_name");
                String addressId = data.getStringExtra("district_id");
                pd = ProgressDialog.show(PersonalDataActivity.this, null, "获取数据…");
                if (address != null)
                    modifyData("", "", "", "", address, null);
                System.out.println("address:" + address);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void initDate() {
        pd = ProgressDialog.show(PersonalDataActivity.this, null, "获取数据…");
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        String userId = preferences.getString("user_id", "");
        String userAccount = preferences.getString("user_mobile", "");
        String userNickname = preferences.getString("user_nickname", "");
        String userCountry = preferences.getString("user_address", "");
        String headImage = preferences.getString("user_headimg", "");
        String myIntroduction = preferences.getString("user_myIntroduction", "");
        nicknameText.setText(userNickname);
        myAcountText.setText(userAccount);
        oldName = nicknameText.getText().toString();

        if (!"".equals(myIntroduction)) {
            myIntroductionText.setTextColor(ContextCompat.getColor(this, R.color.black_3));
            myIntroductionText.setText(myIntroduction);
        } else {
            myIntroductionText.setTextColor(ContextCompat.getColor(this, R.color.gray_C0));
            myIntroductionText.setText("点击添加（最多255字）");
        }
        // 头像
        ImageDisplayTools.displayCircleImage(headImage, headImg, OSUtil.dp2px(PersonalDataActivity.this, 1), PersonalDataActivity.this.getResources().getColor(R.color.gray_D3D2D3));
        pd.dismiss();
        mSelectCoverHelper = new SelectCoverHelper(this, null, "headPhoto");
        mSelectCoverHelper.setListener(this);

    }

    private void initListener(){
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutGetFocus();

                if(nicknameText.getText() == null || TextUtils.isEmpty(nicknameText.getText().toString())){
                    Toast.makeText(PersonalDataActivity.this, "请填写昵称！", Toast.LENGTH_SHORT).show();
                }
//                if(!TextUtils.equals(oldName,nicknameText.getText().toString()))
                    modifyData("", nicknameText.getText().toString(), "", "", "", null);
            }
        });
        ll_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layoutGetFocus();
                return false;
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            // 退出按钮
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                new HLLXLoginHelper(PersonalDataActivity.this).visitorLogin();
                finish();
            }
        });
        headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoChooseDialog == null) {
                    // 实例化SelectPicPopupWindow
                    mPhotoChooseDialog = new PhotoChooseDialog(PersonalDataActivity.this,
                            "图库", "拍照");
                }

                // 显示窗口
                mPhotoChooseDialog.show();

                mPhotoChooseDialog.setTakePhotoClick(new PhotoChooseDialog.DialogTakePhotoListener() {

                    @Override
                    public void takePhotoClick(View view) {
                        // 判断摄像头权限
                        if (ContextCompat.checkSelfPermission(PersonalDataActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(PersonalDataActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                        }else{
                            mSelectCoverHelper.pickFromCamera();
                        }
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

        passwordRow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalDataActivity.this, ModifyPassword.class);
                PersonalDataActivity.this.startActivityForResult(intent, REQUEST_NICK_NAME);
            }
        });
    }

    void modifyData(final String headImg, final String nickname, final String birthday, final String sex,
                    final String address, final String myIntroduction) {
        String url = Constants.Root_Url + "/user/updateUserInfo.do";
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        final String _userId = preferences.getString("user_id", "");
        Map<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("id", _userId);
        if (!"".equals(headImg))
            paraMap.put("headImg", headImg);
        if (!"".equals(nickname))
            paraMap.put("nickName", nickname);
        if (!"".equals(address))
            paraMap.put("area", address);
        if (myIntroduction != null)
            paraMap.put("myIntroduction", myIntroduction);
        NetWorkUtil.postForm(getApplication(), url, new MResponseListener() {

            @Override
            public void onResponse(JSONObject response) {
                super.onResponse(response);
                try {
                    if (response.get("error") != null && 0 == response.getInt("error")) {// 成功
                        if ("OK".equals(response.getString("msg"))) {
                            // 保存信息
                            SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                            Editor editor = preferences.edit();
                            if (!"".equals(nickname)) {
                                UserSharedPreference.saveNickName(nickname);
                                oldName = nickname;
                                startActivity(new Intent("com.travel.activity.HomeActivity").putExtra("position", Constants.USERCENTER_POSITION));
                                showToast("保存成功！");
                            }
                            if (!"".equals(headImg)) {
                                editor.putString("user_headimg_coverId", headImg);
                            }
                            if (!"".equals(address))
                                editor.putString("user_address", address);

                            if (myIntroduction != null) {
                                editor.putString("user_myIntroduction", myIntroduction);
                                myIntroductionText.setText(myIntroduction);
                            }
                            editor.commit();
                        } else {
                            Toast.makeText(PersonalDataActivity.this, "更新数据失败！", Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pd.dismiss();
                hideProgressDialog();
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                pd.dismiss();
                hideProgressDialog();
            }
        }, paraMap);
    }

    /**
     * requestPermissions方法执行后的回调方法
     * @param requestCode 相当于一个标志，
     * @param permissions 需要传进的permission，不能为空
     * @param grantResults 用户进行操作之后，或同意或拒绝回调的传进的两个参数;
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != 1) return;
        if(permissions != null && permissions.length > 0
                && TextUtils.equals(Manifest.permission.CAMERA, permissions[0])
                && PackageManager.PERMISSION_DENIED == grantResults[0]){
            Toast.makeText(PersonalDataActivity.this, "请打开摄像头权限！", Toast.LENGTH_SHORT).show();
            return;
        }
        mSelectCoverHelper.pickFromCamera();
    }

    @Override
    public void onImageUploadSuccess(String url, String coverId, Bitmap bitmap) {
        ImageDisplayTools.displayCircleImage(url, headImg, OSUtil.dp2px(this, 1), ContextCompat.getColor(this,R.color.gray_D3D2D3));
        UserSharedPreference.saveCoverId(coverId);
        UserSharedPreference.saveHeadImg(url);
        pd.dismiss();
    }

    @Override
    public void onUploadFileFailed() {
        pd.dismiss();
    }
}
