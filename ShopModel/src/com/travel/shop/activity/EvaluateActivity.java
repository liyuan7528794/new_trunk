package com.travel.shop.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.photoselector.ui.GdAdapter;
import com.photoselector.ui.PhotoSelectorActivity;
import com.travel.ShopConstant;
import com.travel.bean.PhotoModel;
import com.travel.layout.PhotoChooseDialog;
import com.travel.layout.PhotoChooseDialog.DialogCancleListener;
import com.travel.layout.PhotoChooseDialog.DialogMapStorageListener;
import com.travel.layout.PhotoChooseDialog.DialogTakePhotoListener;
import com.travel.lib.ui.LoadingDialog;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.TravelUtil;
import com.travel.shop.R;
import com.travel.shop.tools.ShopTool;
import com.travel.shop.tools.commitphoto.UploadFilePostRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 评价页
 *
 * @author WYP
 * @version 1.0
 * @created 2016/03/08
 */
public class EvaluateActivity extends TitleBarBaseActivity
        implements OnItemClickListener, OnRatingBarChangeListener {

    private Context mContext;
    private ScrollView sl_evaluate_focus;

    // 评价相关
    private EditText et_evaluate;
    private TextView et_evaluate_num;
    //    private ImageView iv_good, iv_middle, iv_bad;
    private TextView tv_evaluate;
    private RatingBar ratingBar;
    private int evaluate = 1;
    private int star_evaluate_description = 5;
    private int star_evaluate_attitude = 5;
    private int star_evaluate = 5;

    // 图片选择相关
    private GridView mGridView;
    private List<PhotoModel> selected;
    private GdAdapter adapter;
    private String str_choosed_img = "";
    private int pos;
    private PhotoReceiver pr;
    private String[] files;
    private File cameraFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);

        init();
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setText("发布");
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // 禁止双击
                v.setEnabled(false);
                v.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        v.setEnabled(true);
                    }
                }, 500);
                // 评价内容不可为空
                if (!"".equals(et_evaluate.getText().toString().trim())) {
                    LoadingDialog.getInstance(mContext).showProcessDialogNotStop();
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Looper.prepare();
                            reportData();
                            Looper.loop();
                        }
                    }).start();
                } else {
                    showToast(R.string.evaluate_cannot_empty);
                }
            }
        });

        mGridView.setOnItemClickListener(this);
        ratingBar.setOnRatingBarChangeListener(this);
        sl_evaluate_focus.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                OSUtil.hideKeyboard(EvaluateActivity.this);
                return false;
            }
        });
        mGridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                OSUtil.hideKeyboard(EvaluateActivity.this);
                return false;
            }
        });
        et_evaluate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Editable editable = et_evaluate.getText();
                int len = editable.length();

                if (len > 300) {
                    int selEndIndex = Selection.getSelectionEnd(editable);
                    String str = editable.toString();
                    //截取新字符串
                    String newStr = str.substring(0, 300);
                    et_evaluate.setText(newStr);
                    editable = et_evaluate.getText();

                    //新字符串的长度
                    int newLen = editable.length();
                    //旧光标位置超过字符串长度
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length();
                    }
                    //设置新光标所在的位置
                    Selection.setSelection(editable, selEndIndex);
                    len = editable.length();
                }
                et_evaluate_num.setText(len + "/300");
            }
        });


    }

    /**
     * 控件初始化
     */
    private void init() {
        mContext = this;

        sl_evaluate_focus = findView(R.id.sl_evaluate_focus);
        et_evaluate = findView(R.id.et_evaluate);
        et_evaluate_num = findView(R.id.et_evaluate_num);
        tv_evaluate = findView(R.id.tv_rating);

        ratingBar = findView(R.id.ratingBar);

        mGridView = findView(R.id.gv_photo);
        selected = new ArrayList<>();
        PhotoModel photoModel = new PhotoModel();
        photoModel.setOriginalPath("default");
        selected.add(photoModel);
        adapter = new GdAdapter(mContext, selected);
        adapter.setListener(new GdAdapter.GdListener() {
            @Override
            public void onDelete(int position, PhotoModel photoModel) {
                pos = position;
                if (!"default".equals(selected.get(position).getOriginalPath()))
                    ShopTool.deletePicture(mContext);
            }
        });
        mGridView.setAdapter(adapter);

        setTitle(getString(R.string.evaluate_report_evaluate));
        ImageDisplayTools.initImageLoader(mContext);

        // 注册广播
        pr = new PhotoReceiver();
        registerReceiver(pr, new IntentFilter("delete"));
    }

    /**
     * 收到广播后的操作
     */
    class PhotoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent != null) {
                selected.remove(pos);
                if (selected.size() < ShopConstant.PHOTO_MAX
                        && !"default".equals(selected.get(selected.size() - 1).getOriginalPath())) {
                    PhotoModel addModel = new PhotoModel();
                    addModel.setOriginalPath("default");
                    selected.add(addModel);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pr);
    }

    @SuppressLint("NewApi")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 如果是最后一个并且是添加的图片
        if (position == selected.size() - 1 && "default".equals(selected.get(selected.size() - 1).getOriginalPath())) {
            PhotoChooseDialog dialog = new PhotoChooseDialog(EvaluateActivity.this, "图库", "拍照");
            dialog.show();
            // 本地上传
            dialog.setMapStorageClick(new DialogMapStorageListener() {

                @Override
                public void mapStorageClick(View view) {
                    enterChoosePhoto();
                }
            });
            // 拍照上传
            dialog.setTakePhotoClick(new DialogTakePhotoListener() {

                @Override
                public void takePhotoClick(View view) {
                    if (!TravelUtil.GetSDState()) {
                        TravelUtil.showToast("SD卡不存在，不能拍照");
                        return;
                    }
                    cameraFile = new File(TravelUtil.getFileAddress("evaluate", mContext),
                            System.currentTimeMillis() + ".png");
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EvaluateActivity.this, new String[]{Manifest.permission.CAMERA}, 1003);
                    } else {
                        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)), 1002);
                    }
                }
            });
            dialog.setCancleClick(new DialogCancleListener() {

                @Override
                public void cancleClick(View view) {

                }
            });
        }
    }

    /**
     * 本地图片选择
     */
    private void enterChoosePhoto() {

        ArrayList<PhotoModel> choosed = new ArrayList<>();
        if (selected.size() > 0) {
            choosed.addAll(selected);
            choosed.remove(choosed.size() - 1);// 去除添加功能的图片
        }
        Intent intent = new Intent(mContext, PhotoSelectorActivity.class);
        intent.putExtra("selected", choosed);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1003:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)), 1002);
                }else{
                    showToast("您已拒绝拍照权限");
                }
                break;
            default:
        }
    }

    /**
     * 获取拍照或从相册选的图片
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 相册上传
                case 1001:
                    List<PhotoModel> photos = (List<PhotoModel>) data.getExtras().getSerializable("photos");
                    selected.clear();
                    adapter.notifyDataSetChanged();
                    selected.addAll(photos);
                    if (selected.size() < ShopConstant.PHOTO_MAX) {
                        PhotoModel addModel = new PhotoModel();
                        addModel.setOriginalPath("default");
                        selected.add(addModel);
                    }
                    adapter.notifyDataSetChanged();
                    break;
                // 拍照上传
                case 1002:
                    if (cameraFile != null && cameraFile.exists()) {
                        str_choosed_img = cameraFile.getAbsolutePath();
                        PhotoModel cameraPhotoModel = new PhotoModel();
                        cameraPhotoModel.setChecked(true);
                        cameraPhotoModel.setOriginalPath(str_choosed_img);
                        if (selected.size() > 0) {// 如果原来有图片
                            selected.remove(selected.size() - 1);
                        }
                        selected.add(cameraPhotoModel);
                        if (selected.size() < ShopConstant.PHOTO_MAX) {
                            PhotoModel addModel1 = new PhotoModel();
                            addModel1.setChecked(false);
                            addModel1.setOriginalPath("default");
                            selected.add(addModel1);
                        }
                        adapter.notifyDataSetChanged();
                        MediaScannerConnection.scanFile(mContext, new String[]{str_choosed_img}, null, null);
                    } else {
                        showToast(R.string.evaluate_photo_fail);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 评分控件的监听
     */
    @Override
    public void onRatingChanged(RatingBar bar, float rating, boolean fromUser) {
        if (bar.getId() == R.id.ratingBar) {
            star_evaluate = (int) rating;
            if (star_evaluate < 3) {
                tv_evaluate.setText("差");
            } else if (star_evaluate == 3 || star_evaluate == 4) {
                tv_evaluate.setText("中");
            } else if (star_evaluate == 5) {
                tv_evaluate.setText("好");
            }
        }
    }

    /**
     * 向服务器提交数据
     */
    private void reportData() {
        long ordersId = getIntent().getLongExtra("ordersId", 0);
        Map<String, Object> map = new HashMap<>();
        map.put("ordersId", ordersId);
        map.put("goodsId", getIntent().getStringExtra("goodsId"));
        map.put("evaluateType", star_evaluate);
        map.put("content", et_evaluate.getText().toString().trim());
        // 获取图片的路径
        List<String> list = new ArrayList<>();
        int size = selected.size();
        // 如果不满三张，则会有默认的图片，此时需要把默认的图片删除
        if ("default".equals(selected.get(size - 1).getOriginalPath())) {
            --size;
        }
        for (int i = 0; i < size; i++) {
            try {
                list.add(ShopTool.PicControl(selected.get(i).getOriginalPath(), "photo" + (i + 1)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (list != null && size != 0)
            files = list.toArray(new String[size]);
        else
            files = new String[0];

        String info = "";
        info = UploadFilePostRequest.uploadFile(ShopConstant.COMMIT_EVALUATE, "", "evaluate", ShopTool.mapToJson(map),
                files);
        // 解析服务器传回来的信息
        try {
            JSONObject jsonObject = new JSONObject(info);
            if (jsonObject.optString("error").equals("0")) {
                OSUtil.intentOrderInfo(mContext, ordersId);
            } else
                showToast("您的网络不好，请重新上传！");
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            LoadingDialog.getInstance(mContext).hideProcessDialog(0);
        }
    }
}
