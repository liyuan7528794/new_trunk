package com.travel.localfile.pk.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.ctsmedia.hltravel.R;
import com.google.gson.JsonArray;
import com.travel.Constants;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.CameraFragment;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.pk.entity.LocalFileUtil;
import com.travel.localfile.pk.fragment.AbstractEvidenceSelect;
import com.travel.localfile.pk.fragment.LiveEvidenceSelector;
import com.travel.localfile.pk.fragment.PhotoEvidenceSelector;
import com.travel.localfile.pk.fragment.VideoEvidenceSelector;
import com.travel.localfile.pk.fragment.VoiceEvidenceSelector;
import com.travel.localfile.pk.fragment.VoteFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 众投证据选择界面新版UI
 * Created by ldkxingzhe on 2017/2/13.
 */
public class EvidenceSelectActivity extends TitleBarBaseActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "EvidenceSelectActivity";


    private EditText mEditText;
    private FrameLayout mLocalFileLayout;
    private View mRootView;
    private RadioGroup mRadioGroup;

    private int mSoftKeyboardHeight = -1;
    private int mSoftKeyboardHeightTmp = -1;
    private String mUserId;
    private int mPublicVoteId;
    private boolean mIsSeller;
    private Handler mHandler;
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener;
    private List<LocalFile> mCurrentLocalFileList = new ArrayList<>();
    /**
     * @see com.travel.localfile.CameraFragment#TYPE_PHOTO
     */
    private int mCurrentType = -1;
    private AbstractEvidenceSelect mEvidenceSelect;

    public static void startActivityForResult(
            Fragment fragment,
            int requestCode,
            int publicVoteId, boolean isSeller) {
        Intent intent = new Intent(fragment.getActivity(), EvidenceSelectActivity.class);
        intent.putExtra(VoteFragment.BUNDLE_VOTE_ID, publicVoteId);
        intent.putExtra("is_seller", isSeller);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.activity_evidence_select);
        initTitleBar();
        mEditText = findView(R.id.et_input);
        mLocalFileLayout = findView(R.id.fl_content);
        mRootView = getWindow().getDecorView();
        mRadioGroup = findView(R.id.rg_tool_bar);
        getKeyboardHeight(mRootView);
        mUserId = UserSharedPreference.getUserId();
        mPublicVoteId = getIntent().getIntExtra(VoteFragment.BUNDLE_VOTE_ID, 86);
        mIsSeller = getIntent().getBooleanExtra("is_seller", false);
        initToolbarListener();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    rightButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_D));
                    rightButton.setEnabled(false);
                } else {
                    rightButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.yellow_F5A623));
                    rightButton.setEnabled(true);
                }
            }
        });
    }

    private void initToolbarListener() {
//        mRadioGroup.check(R.id.cb_voice);
//        checkVoice();
        mRadioGroup.check(R.id.cb_photo);
        checkPhoto();
        mRadioGroup.setOnCheckedChangeListener(new ToolbarCheckedChangedListener());
        View.OnTouchListener hideKeyboard = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    OSUtil.hideKeyboard(EvidenceSelectActivity.this);
                }
                return false;
            }
        };
        // 点击的时候隐藏软键盘
        int[] checkBoxArray = {
//                R.id.cb_voice, R.id.cb_photo, R.id.cb_video
                R.id.cb_photo, R.id.cb_video
        };
        for (int id : checkBoxArray) {
            findViewById(id).setOnTouchListener(hideKeyboard);
        }
    }

    private class ToolbarCheckedChangedListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, final int checkedId) {
            OSUtil.hideKeyboard(EvidenceSelectActivity.this);
            MLog.d(TAG, "onCheckedChanged");
            Runnable yesRunnable = new Runnable() {
                @Override
                public void run() {
                    mCurrentLocalFileList.clear();
                    mLocalFileLayout.removeAllViews();
                    switch (checkedId) {
                        case R.id.cb_voice:
                            checkVoice();
                            break;
                        case R.id.cb_photo:
                            mCurrentType = CameraFragment.TYPE_PHOTO;
                            checkPhoto();
                            break;
                        case R.id.cb_video:
                            mCurrentType = CameraFragment.TYPE_VIDEO;
                            checkVideo();
                            break;
                        case R.id.cb_live:
                            mCurrentType = CameraFragment.TYPE_LIVE;
                            checklive();
                            break;
                    }
                }
            };
            if (mCurrentType != -1
                    && mEvidenceSelect != null
                    && mEvidenceSelect.getSelectedList().size() > 0) {
                AlertDialogUtils.alertDialog(EvidenceSelectActivity.this,
                        "提示\n切换证据类型，当前选择会被清空",
                        yesRunnable,
                        new Runnable() {
                            @Override
                            public void run() {
                                mRadioGroup.setOnCheckedChangeListener(null);
                                switch (mCurrentType) {
                                    case CameraFragment.TYPE_PHOTO:
                                        mRadioGroup.check(R.id.cb_photo);
                                        break;
                                    case CameraFragment.TYPE_AUDIO:
                                        mRadioGroup.check(R.id.cb_voice);
                                        break;
                                    case CameraFragment.TYPE_VIDEO:
                                        mRadioGroup.check(R.id.cb_video);
                                        break;
                                    case CameraFragment.TYPE_LIVE:
                                        mRadioGroup.check(R.id.cb_live);
                                        break;
                                }
                                mRadioGroup.setOnCheckedChangeListener(ToolbarCheckedChangedListener.this);
                            }
                        });
            } else {
                yesRunnable.run();
            }
        }
    }

    private void checklive() {
        mCurrentType = CameraFragment.TYPE_LIVE;
        mEvidenceSelect = new LiveEvidenceSelector(this, mUserId);
        mLocalFileLayout.addView(mEvidenceSelect.createView(),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void checkVideo() {
        mCurrentType = CameraFragment.TYPE_VIDEO;
        mEvidenceSelect = new VideoEvidenceSelector(this, mUserId);
        mLocalFileLayout.addView(mEvidenceSelect.createView(),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void checkPhoto() {
        mCurrentType = CameraFragment.TYPE_PHOTO;
        mEvidenceSelect = new PhotoEvidenceSelector(this, mUserId);
        mLocalFileLayout.addView(mEvidenceSelect.createView(),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void checkVoice() {
        mCurrentType = CameraFragment.TYPE_AUDIO;
        mEvidenceSelect = new VoiceEvidenceSelector(this, mUserId);
        mLocalFileLayout.addView(mEvidenceSelect.createView(),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initTitleBar() {
        setTitle("上传证据");
        rightButton.setTextColor(ContextCompat.getColor(this, R.color.gray_D));
        rightButton.setText("发布");
        rightButton.setVisibility(View.VISIBLE);

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mEditText.getText().toString().trim()))
                    showToast("请输入有效字符");
                else if (mEvidenceSelect != null && mEvidenceSelect.canPublish()) {
                    List<LocalFile> list = mEvidenceSelect.getSelectedList();
                    JsonArray jsonArray = new JsonArray();
                    for (int i = 0, length = list.size(); i < length; i++) {
                        jsonArray.add(LocalFileUtil.toJsonObject(list.get(i)));
                    }
                    int type = -1;
                    if (list.size() > 0)
                        type = list.get(0).getType();
                    addEvidencePacket(type, mEditText.getText().toString(),
                            jsonArray.toString(), mUserId,
                            new MResponseListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    super.onResponse(response);
                                    if (response.optInt("error") == 0) {
                                        hideProgressDialog();
                                        finish();
                                    }
                                }

                                @Override
                                protected void onErrorNotZero(int error, String msg) {
                                    hideProgressDialog();
                                }
                            });
                    showProgressDialog("提示", "证据上传中...");
                }
            }
        });
    }

    private void getKeyboardHeight(final View rootView) {
        mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect visibleSize = new Rect();
                rootView.getWindowVisibleDisplayFrame(visibleSize);
                int screenHeight = rootView.getHeight();
                int heightDifference = screenHeight - visibleSize.bottom;
                MLog.v(TAG, "screenHeight is %d. heightDifference is %d.", screenHeight, heightDifference);
                if (mSoftKeyboardHeight == -1) {
                    if (mSoftKeyboardHeightTmp < heightDifference) {
                        mSoftKeyboardHeightTmp = heightDifference;
                        mHandler.removeCallbacks(mSoftKeyboardHeightCalculateRunnable);
                        mHandler.postDelayed(mSoftKeyboardHeightCalculateRunnable, 500);
                    }
                }
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);
    }

    private Runnable mSoftKeyboardHeightCalculateRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mSoftKeyboardHeightCalculateRunnable);
            mSoftKeyboardHeight = mSoftKeyboardHeightTmp;
            MLog.d(TAG, "计算键盘高度完成：%d.", mSoftKeyboardHeight);
            mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);

            ViewGroup.LayoutParams params = mLocalFileLayout.getLayoutParams();
            params.height = mSoftKeyboardHeight;
            mLocalFileLayout.setLayoutParams(params);
            mLocalFileLayout.setVisibility(View.VISIBLE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        }
    };

    public void addEvidencePacket(int type, String introduction, String include, String sellerOrBuyer, MResponseListener listener) {
        String url = Constants.Root_Url + "/orders/addVoteData.do";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("publicVoteId", mPublicVoteId);
        map.put("reason", introduction);
        map.put("include", include);
        map.put("sellerorbuyer", sellerOrBuyer);
        map.put("type", type);
        NetWorkUtil.postForm(this, url, listener, map);
    }
}
