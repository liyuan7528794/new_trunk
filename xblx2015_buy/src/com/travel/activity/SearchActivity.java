package com.travel.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ctsmedia.hltravel.R;
import com.travel.VideoConstant;
import com.travel.layout.SearchTipsGroupView;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.video.fragment.VideoListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/21.
 */

public class SearchActivity extends TitleBarBaseActivity{
    private EditText editText;
    private SearchTipsGroupView search_layout;
    List<String> list = new ArrayList<>();
    private int activityId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if(getIntent().hasExtra("activityId")){
            activityId = getIntent().getIntExtra("activityId", -1);
        }
        search_layout = (SearchTipsGroupView) findViewById(R.id.search_layout);
        editText = (EditText) findViewById(R.id.et_search);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId ==EditorInfo.IME_ACTION_SEARCH) {
                    // 先隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
                    search();
                    return true;
                }
                return false;
            }
        });

        getHotWord();
    }

    private void search() {

        String searchContext = editText.getText().toString().trim();
        if (TextUtils.isEmpty(searchContext)) {
            showToast("请输入要搜索的关键词");
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("keyword", searchContext);
            bundle.putString("videoType", VideoListFragment.INTENT_SEARCH);
            bundle.putInt("activityId", activityId);
            OneFragmentActivity.startNewActivity(this, "搜索结果", VideoListFragment.class, bundle);
        }
    }


    private void getHotWord(){
        Map<String, Object> map = new HashMap<>();
        NetWorkUtil.postForm(this, VideoConstant.GET_HOT_WORD, new MResponseListener() {

            @Override
            protected void onDataFine(JSONArray data) {
                super.onDataFine(data);
                if(data != null && data.length() > 0){
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.getJSONObject(i);
                            String hotWord = JsonUtil.getJson(object, "hotWord");
                            list.add(hotWord);
                        }
                        search_layout.initViews(list, new SearchTipsGroupView.OnItemClick() {
                            @Override
                            public void onClick(int position) {
                                Bundle bundle = new Bundle();
                                bundle.putString("keyword", list.get(position));
                                bundle.putString("videoType", VideoListFragment.INTENT_SEARCH);
                                bundle.putInt("activityId", activityId);
                                OneFragmentActivity.startNewActivity(SearchActivity.this, "搜索结果", VideoListFragment.class, bundle);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, map);
    }

}
