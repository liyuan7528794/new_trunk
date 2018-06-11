package com.travel.localfile.pk.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.OSUtil;
import com.travel.localfile.pk.fragment.VoteCommentFragment;
import com.travel.localfile.pk.fragment.VoteInfoFragment;

/**
 * Created by Administrator on 2017/2/9.
 */

public class PublicVoteActivity extends TitleBarBaseActivity implements View.OnClickListener{
    private View titleView;
    private Button backButton, shareButton;

    private RelativeLayout tab_info, tab_comment;
    private TextView tv_info, tv_comment;
    private View infoLine, commentLine;
    private VoteInfoFragment infoFragment;
    private VoteCommentFragment commentFragment;
    private Fragment currentFragment;
    private int currentTag = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_vote);
        titleText.setText("辩论详情");
        titleView = getLayoutInflater().inflate(R.layout.vote_titlebar_layout, null);
//        addNewTitleLayout(titleView);
        titleView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, OSUtil.dp2px(this, 44)));

        infoFragment = new VoteInfoFragment();
        commentFragment = new VoteCommentFragment();
        infoFragment.setArguments(getIntent().getExtras());
        commentFragment.setArguments(getIntent().getExtras());

        initview();
//        setListener();
        currentFragment = infoFragment;
        addFragment(0);
    }

    private void initview() {
        backButton = findView(R.id.backButton);
        shareButton = findView(R.id.shareButton);
        frameLayout = (FrameLayout) findViewById(R.id.fragment_container);
        tab_info = findView(R.id.tab_info);
        tab_comment = findView(R.id.tab_comment);
        tv_info = findView(R.id.tv_info);
        tv_comment= findView(R.id.tv_comment);
        infoLine= findView(R.id.infoLine);
        commentLine= findView(R.id.commentLine);
    }

    private void setListener() {
        tab_comment.setOnClickListener(this);
        tab_info.setOnClickListener(this);
        backButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tab_info:
                addFragment(0);
                break;
            case R.id.tab_comment:
                addFragment(1);
                break;
            case R.id.backButton:
                finish();
                break;
            case R.id.shareButton:

                break;
        }
    }

    private void addFragment(int index) {
        if (currentTag != index) {
//            tv_info.setTextColor(ContextCompat.getColor(this, R.color.gray_9));
//            tv_comment.setTextColor(ContextCompat.getColor(this, R.color.gray_9));
//            infoLine.setVisibility(View.INVISIBLE);
//            commentLine.setVisibility(View.INVISIBLE);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (index == 0) {
                if (!infoFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fragment_container, infoFragment).show(infoFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(infoFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
//                tv_info.setTextColor(ContextCompat.getColor(this, R.color.black_3));
//                infoLine.setVisibility(View.VISIBLE);
                currentFragment = infoFragment;
            } else if(index == 1) {
                if (!commentFragment.isAdded()) { // 先判断是否被add过
                    transaction.hide(currentFragment).add(R.id.fragment_container, commentFragment).show(commentFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    transaction.hide(currentFragment).show(commentFragment).commit(); // 隐藏当前的fragment，显示下一个
                }
//                tv_comment.setTextColor(ContextCompat.getColor(this, R.color.black_3));
//                commentLine.setVisibility(View.VISIBLE);
                currentFragment = commentFragment;
            }
        }
        currentTag = index;
    }
}
