package com.travel.communication.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ctsmedia.hltravel.R;
import com.travel.communication.activity.ChatActivity;
import com.travel.communication.entity.DisplayRules;
import com.travel.communication.entity.Emojicon;
import com.travel.communication.fragment.ChatFunctionFragment;
import com.travel.communication.fragment.ChatFunctionFragment.ChatFunctionListener;
import com.travel.communication.fragment.EmojiconFragment;
import com.travel.communication.fragment.EmojiconFragment.OnEmojiconClickListener;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.OSUtil;

/**
 * 聊天界面的键盘输入框控制主界面
 */
public class ChatKeyboard extends RelativeLayout implements OnEmojiconClickListener, ChatActivity.OnGetVoicePermissionListener {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatKeyboard";

    private static final boolean IS_HAS_FEATURE_SOUND = true;

    // UI Elements
    // 表情, 更多按钮
    private CheckBox mFaceCB, mMoreCB, mVoiceCB;
    private Button mSendBtn;    // 发送按钮
    private EditText mEmojiconEditText;  // 支持表情的输逆天入编辑框
    private ViewPager mViewPager;
    private View mFaceLayout;  // 我的表情Fragmen的容器界面
    private View mEmojiconEditLayout; // 我的输入框与表情容器
    private Button mVoiceBtn; // 录音按钮

    private FragmentActivity mContext;
    private FragmentManager mFragmentManager; // 添加这个的目的是用来防止在Fragment里面调用此类, 出现的莫名问题
    private Fragment[] mFragments = new Fragment[2]; // 表情, 功能页

    private int mCurrentShowPosition = -1;
    private boolean mIsCheckedChangedByMe = false;

    private Handler mHandler;
    private int mTouchSlop;
    private Vibrator mVibrator;

    @Override
    public void onGetPermission() {

    }

    public interface ChatKeyboardListener extends ChatFunctionListener {
        /**
         * 点击了发送按钮
         *
         * @param message 此时message的值, 可以为空不可以为null
         */
        void onSendClick(String message);

        /**
         * 图片按钮被点击
         */
        void onPicClick();

        /**
         * 录音按键被点击
         */
        void onVoiceTouchStart();

        /**
         * 上滑手势取消发送
         */
        void onVoiceTouchCancel();

        /**
         * 录音按键松开
         */
        void onVoiceTouchEnd();

        /**
         * 录音超过60秒
         */
        void onVoice60(Button mVoiceBtn);

    }

    private ChatKeyboardListener mListener;

    public void setListener(ChatKeyboardListener listener) {
        mListener = listener;
    }

    private int timer = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            ++timer;
            if (timer == 61) {
                mListener.onVoice60(mVoiceBtn);
                timerHandler.removeCallbacks(timerRunnable);
                timer = 0;
            } else
                timerHandler.postDelayed(this, 1000);
        }
    };

    public ChatKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatKeyboard(Context context) {
        this(context, null);
    }

    public ChatKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View viewRoot = LayoutInflater.from(context).inflate(R.layout.chat_keyboard, this, false);
        addView(viewRoot, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mFaceCB = (CheckBox) viewRoot.findViewById(R.id.toolbox_cb_face);
        mMoreCB = (CheckBox) viewRoot.findViewById(R.id.toolbox_cb_more);
        mSendBtn = (Button) viewRoot.findViewById(R.id.toolbox_btn_send);
        mEmojiconEditText = (EditText) viewRoot.findViewById(R.id.toolbox_et_message);
        mViewPager = (ViewPager) viewRoot.findViewById(R.id.tootbox_view_pager);
        mFaceLayout = viewRoot.findViewById(R.id.toolbox_layout_face);
        mVoiceCB = (CheckBox) viewRoot.findViewById(R.id.toolbox_cb_sound);
        mVoiceBtn = (Button) viewRoot.findViewById(R.id.toolbox_btn_voice);
        mEmojiconEditLayout = viewRoot.findViewById(R.id.toolbox_et_layout);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        if (IS_HAS_FEATURE_SOUND) {
            setSendBtnVisibility(View.GONE);
            //			mSendBtn.setBackgroundResource(R.drawable.send_barrage_icon);
        }
        if (context instanceof FragmentActivity) {
            mContext = (FragmentActivity) context;
        } else {
            throw new IllegalStateException("I'm sorry, but This is view only support FragmentActivity");
        }
        mHandler = new Handler();

        initListener();
    }

    /**
     * 设置FragmentManager
     *
     * @param fm
     */
    public void setFragmentManager(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /**
     * 设置输入框中的文字
     *
     * @param message 文字
     */
    public void setText(String message) {
        mEmojiconEditText.setText(message);
    }

    public boolean isFaceLayoutShowing() {
        return mFaceLayout.getVisibility() != View.GONE;
    }

    private void initListener() {
        mFaceCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsCheckedChangedByMe)
                    return;
                mVoiceCB.setChecked(false);
                if (isChecked) {
                    showFragment(0);
                } else {
                    hideFragment();
                }
                //				setAnotherChecked(mMoreCB);
            }
        });

        mMoreCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsCheckedChangedByMe)
                    return;
                if (isChecked) {
                    showFragment(1);
                } else {
                    hideFragment();
                }
                setAnotherChecked(mFaceCB);
            }
        });

        mSendBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    String messageStr = mEmojiconEditText.getText().toString();
                    messageStr = messageStr.trim();
                    if (TextUtils.isEmpty(messageStr)) {
                        if (IS_HAS_FEATURE_SOUND) {
                            // 添加图片
                            mListener.onPicClick();
                        } else {
                            Toast.makeText(getContext(), "消息不能为空", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mListener.onSendClick(messageStr);
                        mEmojiconEditText.setText("");
                    }
                }
            }
        });

        mEmojiconEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    if (IS_HAS_FEATURE_SOUND) {
                        setSendBtnVisibility(View.GONE);
                        //					}else{
                        //					mSendBtn.setBackgroundResource(R.drawable.send_barrage_grey);
                    }
                } else {
                    if (IS_HAS_FEATURE_SOUND) {
                        setSendBtnVisibility(View.VISIBLE);
                        //					}else{
                        //					mSendBtn.setBackgroundResource(R.drawable.send_barrage_icon);
                    }
                }
            }
        });
        mEmojiconEditText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 输入框获取焦点, 所有按键归位reset
                hideFragment();
                setAnotherChecked(mFaceCB);
                //				setAnotherChecked(mMoreCB);
            }
        });

        mEmojiconEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSendBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        if (IS_HAS_FEATURE_SOUND) {
            mVoiceCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ((ChatActivity) mContext).setmOnGetVoicePermissionListener(listener);
                            mContext.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                            mVoiceCB.setChecked(false);
                        } else {
                            setVoiceBtnVisibility(View.VISIBLE);
                            hideFragment();
                            OSUtil.hideKeyboard(mContext);
                        }
                    } else {
                        setVoiceBtnVisibility(View.INVISIBLE);
                    }
                }
            });
        } else {
            mVoiceCB.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    MLog.v(TAG, "onImage click");
                    if (mListener != null) {
                        mListener.onPicClick();
                    }
                }
            });
        }

        mVoiceBtn.setOnTouchListener(new OnTouchListener() {
            private float initY;
            private boolean isConsumed;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mListener == null)
                    return false;
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        initY = event.getY();
                        isConsumed = false;
                        MLog.v(TAG, "mVoice click, and action down");
                        mListener.onVoiceTouchStart();
                        mVoiceBtn.setText("松开 结束");
                        mVoiceBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_6));
                        OSUtil.vibrate(getContext(), 100);
                        timerHandler.postDelayed(timerRunnable, 1000);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() - initY < -3 * mTouchSlop && !isConsumed) {
                            MLog.d(TAG, "mVoice move, and cancel");
                            isConsumed = true;
                            mListener.onVoiceTouchCancel();
                            timerHandler.removeCallbacks(timerRunnable);
                            timer = 0;
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        MLog.v(TAG, "mVoice clicked, and action up. %b", isConsumed);
                        mVoiceBtn.setText("按住 说话");
                        if (OSUtil.isDayTheme())
                            mVoiceBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_3));
                        else
                            mVoiceBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                        if (!isConsumed) {
                            mListener.onVoiceTouchEnd();
                            timerHandler.removeCallbacks(timerRunnable);
                            timer = 0;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }

    private ChatActivity.OnGetVoicePermissionListener listener = new ChatActivity.OnGetVoicePermissionListener() {
        @Override
        public void onGetPermission() {
            mVoiceCB.setChecked(true);
            setVoiceBtnVisibility(View.VISIBLE);
            hideFragment();
            OSUtil.hideKeyboard(mContext);
        }
    };

    private void setVoiceBtnVisibility(int visibility) {
        mVoiceBtn.setVisibility(visibility);
        mEmojiconEditLayout.setVisibility(nonVisiblity(visibility));
    }

    // 获得相反的visibility
    // View.VISIBLE --> View.INVISIBLE
    // ... -->> View.VISIBLIE
    private int nonVisiblity(int visibility) {
        if (visibility == View.VISIBLE) {
            return View.GONE;
        }
        return View.VISIBLE;
    }

    private void setSendBtnVisibility(int visibility) {
        mSendBtn.setVisibility(visibility);
        mMoreCB.setVisibility(nonVisiblity(visibility));
    }

    private void setAnotherChecked(CheckBox checkBox) {
        mIsCheckedChangedByMe = true;
        checkBox.setChecked(false);
        mIsCheckedChangedByMe = false;
    }

    private void showFragment(int whichFragment) {
        OSUtil.hideKeyboard((Activity) getContext());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFaceLayout.setVisibility(View.VISIBLE);
            }
        }, 100);
        if (mCurrentShowPosition == whichFragment)
            return;
        if (mFragmentManager == null) {
            mFragmentManager = mContext.getSupportFragmentManager();
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragments[whichFragment];
        if (fragment == null) {
            if (whichFragment == 0) {
                fragment = new EmojiconFragment();
                ((EmojiconFragment) fragment).setOnEmojiconClickListener(this);
            } else {
                fragment = new ChatFunctionFragment();
                ((ChatFunctionFragment) fragment).setListener(mListener);
            }
            mFragments[whichFragment] = fragment;
            transaction.add(R.id.fl_chat_keyboard_content, fragment);
        }

        if (fragment.isHidden()) {
            transaction.show(fragment);
        }
        if (mCurrentShowPosition >= 0) {
            Fragment currentFragment = mFragments[mCurrentShowPosition];
            if (currentFragment != null && !currentFragment.isHidden()) {
                transaction.hide(mFragments[mCurrentShowPosition]);
            }
        }
        transaction.commit();
        mCurrentShowPosition = whichFragment;
        requestLayout();
    }

    @Override
    public void onEmojiconClick(Emojicon emojicon, boolean isBackSpace) {
        if (isBackSpace) {
            DisplayRules.backspace(mEmojiconEditText);
        } else {
            mEmojiconEditText.append(emojicon.getValue());
        }
    }

    public void hideFragment() {
        mFaceLayout.setVisibility(View.GONE);
    }


}
