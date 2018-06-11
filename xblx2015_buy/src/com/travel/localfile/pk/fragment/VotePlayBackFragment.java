package com.travel.localfile.pk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ctsmedia.hltravel.R;
import com.travel.lib.utils.MLog;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.pk.others.PublicVoteCommentsHelper;
import com.travel.widget.ListViewOverrideTouch;

/**
 * 众投中的直播回放界面
 * 包含消息,与视频
 * Created by ldkxingzhe on 2016/7/18.
 */
public class VotePlayBackFragment extends Fragment{
    @SuppressWarnings("unused")
    private static final String TAG = "VotePlayBackFragment";

    private PublicVoteCommentsHelper mPublicVoteCommentsHelper;
    private ListViewOverrideTouch mListViewOverrideTouch;
    private EditText mEditText;
    private Button mSendBtn;

    public static final String VOTE_ID = "vote_id";

    private int mVoteId = 1; // 众投Id
    private String mUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args == null){
            MLog.e(TAG, "args should not be null");
        }else{
            mVoteId = args.getInt(VOTE_ID, mVoteId);
            MLog.v(TAG, "enter VotePlayBackFragment, and mVoteId is %d.", mVoteId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_vote_play_back, container, false);
        mListViewOverrideTouch = (ListViewOverrideTouch) rootView.findViewById(R.id.lv_comments);
        mEditText = (EditText) rootView.findViewById(R.id.et_comments);
        mSendBtn = (Button) rootView.findViewById(R.id.btn_send);
        mPublicVoteCommentsHelper =
                new PublicVoteCommentsHelper(getActivity(),
                        mListViewOverrideTouch, mSendBtn, mEditText);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUserId = UserSharedPreference.getUserId();
        mPublicVoteCommentsHelper.setInfo(mVoteId, mUserId);
    }
}
