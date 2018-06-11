package com.travel.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;

import com.travel.ShopConstant;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.UploadPictureHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/5.
 */

public class PublishTalkHelper {

    private Context mContext;
    private OnUploadListener listener;
    private String[] files;
    public interface OnUploadListener{
        void onResult(String url);
        void onError(String msg);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HashMap<String, String> maps = (HashMap<String, String>) msg.obj;
                    map.put(Integer.parseInt(maps.get("index")), maps.get("url"));
                    break;
                case 2:
                    HashMap<String, String> maps2 = (HashMap<String, String>) msg.obj;
                    map.put(Integer.parseInt(maps2.get("index")), "-1");
                    break;
                default:
                    break;
            }

            if(map.size() == files.length){
                StringBuilder stringBuilder = new StringBuilder();
                List<Integer> s = new ArrayList<>();
                for (int i = 0; i < map.size(); i++) {
                    if(!TextUtils.isEmpty(stringBuilder.toString()) && !TextUtils.equals("-1", map.get(i))){
                        stringBuilder.append(",");
                    }
                    if(TextUtils.equals("-1", map.get(i))){
                        s.add(i);
                    }else{
                        stringBuilder.append(map.get(i));
                    }
                }
                if(s.size() != 0){
                    listener.onError("图片上传失败");
                }else if(!stringBuilder.toString().isEmpty()){
                    listener.onResult(stringBuilder.toString());
                }

            }
        }
    };

    private UploadPictureHelper helper;
    public PublishTalkHelper(Context mContext, OnUploadListener listener) {
        this.mContext = mContext;
        this.listener = listener;
        helper = new UploadPictureHelper(mContext);
    }

    public void uploadHeadFile(String[] files) {
        if(files == null || files.length == 0) {
            listener.onResult("");
            return;
        }
        this.files = files;
        map.clear();
        //上传图片到服务器，并获取该图片的的网络地址
        new AsyncUpLoadedImage().execute(files);
    }

    /**
     * 上传图片异步操作
     */
    private class AsyncUpLoadedImage extends AsyncTask<String[], Void, Void> {

        @Override
        protected Void doInBackground(String[]... params) {
            String[] file = params[0];

            for (int i = 0; i < file.length; i++) {
//                Bitmap bitmap = BitmapFactory.decodeFile(file[i]);
//                uploadImage(bitmap, i);
                uploadImage(file[i], i);
            }

            return null;
        }
    }

    private void uploadImage(String localUrl, final int position){
        helper.uploadFile(localUrl, position+"", new UploadPictureHelper.ProgressCallback() {
            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onFailed(String errorMessage, String tag) {
                HashMap<String, String> smap = new HashMap<>();
                smap.put("index", tag);
                Message msg = new Message();
                msg.what = 2;
                msg.obj = smap;
                handler.sendMessage(msg);
            }

            @Override
            public void onComplete(String url, String tag) {
                HashMap<String, String> smap = new HashMap<>();
                smap.put("url", url);
                smap.put("index", tag);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = smap;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 上传图片
     */
    private void uploadImage(final Bitmap bitmap, final int position) {
        String url = ShopConstant.UPLOAD_IMAGE;
        final String coverImage = Bitmap2StrByBase64(bitmap);
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("imgFile", coverImage);
            paraMap.put("ownerId", UserSharedPreference.getUserId());

        NetWorkUtil.postForm(mContext, url, new MResponseListener() {
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
                        HashMap<String, String> smap = new HashMap<>();
                        smap.put("url", coverUrl);
                        smap.put("index", position+"");
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = smap;
                        handler.sendMessage(msg);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> smap = new HashMap<>();
                smap.put("index", position+"");
                Message msg = new Message();
                msg.what = 2;
                msg.obj = smap;
                handler.sendMessage(msg);
            }

            @Override
            protected void onMsgWrong(String msgs) {
                super.onMsgWrong(msgs);
                HashMap<String, String> smap = new HashMap<>();
                smap.put("index", position+"");
                Message msg = new Message();
                msg.what = 2;
                msg.obj = smap;
                handler.sendMessage(msg);
            }
        }, paraMap);

    }

    /**
     * 通过Base32将Bitmap转换成Base64字符串
     */
    public String Bitmap2StrByBase64(Bitmap bit){
        if(bit == null){
            return "";
        }
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 80, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private HashMap<Integer, String> map = new HashMap<>();

}
