package com.travel.localfile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.travel.VideoConstant;
import com.travel.lib.utils.JsonUtil;
import com.travel.lib.utils.MResponseListener;
import com.travel.lib.utils.NetWorkUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.localfile.dao.DaoMaster;
import com.travel.localfile.dao.DaoSession;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.dao.LocalFileDao;
import com.travel.localfile.pk.fragment.LoacalPhotoCursorTask;
import com.travel.localfile.pk.fragment.LocalVideoCursorTask;
import com.travel.localfile.pk.fragment.OnLocalMediaCursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * 本地资料数据库操作的简单辅助类
 * Created by ldkxingzhe on 2016/6/29.
 */
public class LocalFileSQLiteHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "LocalFileSQLiteHelper";

    private boolean isPhoneLocal = false;
    private Context context;

    private static WeakReference<DaoMaster.DevOpenHelper> s_Helper;
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase mDB;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private LocalFileDao mLocalFileDao;

    private LoacalPhotoCursorTask photoCursorTask;
    private LocalVideoCursorTask videoCursorTask;
    private GetMediaListener photoListener;

    public interface GetMediaListener{
        void GetLocalFile(List<LocalFile> localFiles);
    }

    public void setPhotoListener(GetMediaListener photoListener){
        this.photoListener = photoListener;
    }

    public LocalFileSQLiteHelper(Context context) {
        this.context = context;
    }

    public void init(){
        if (isPhoneLocal) {
            photoCursorTask = new LoacalPhotoCursorTask(context);
            photoCursorTask.setOnLoadPhotoCursor(new OnLocalMediaCursor() {
                @Override
                public void onLoadPhotoSursorResult(List<LocalFile> localFiles) {
                    photoListener.GetLocalFile(localFiles);
                }
            });

            videoCursorTask = new LocalVideoCursorTask(context);
            videoCursorTask.setOnLoadVideoCursor(new OnLocalMediaCursor() {
                @Override
                public void onLoadPhotoSursorResult(List<LocalFile> localFiles) {
                    photoListener.GetLocalFile(localFiles);
                }
            });

        } else {
            mHelper = generateOpenHelper(context);
            mDB = mHelper.getWritableDatabase();
            mDaoMaster = new DaoMaster(mDB);
            mDaoSession = mDaoMaster.newSession();
            mLocalFileDao = mDaoSession.getLocalFileDao();
        }
    }

    /**
     * 插入数据
     */
    public void insert(LocalFile localFile) {
        if (isPhoneLocal) {

        } else {
            mLocalFileDao.insert(localFile);
        }
    }

    /**
     * 更新某条数据
     */
    public void update(LocalFile localFile) {
        if (isPhoneLocal) {

        } else {
            mLocalFileDao.update(localFile);
        }
    }

    /**
     * 从数据中删除此项纪录
     */
    public void delete(LocalFile localFile) {
        if (isPhoneLocal) {

        } else {
            mLocalFileDao.delete(localFile);
        }
    }

    public List<LocalFile> loadAllFile(String userId, String orderId) {
        if (isPhoneLocal) {

            return null;
        } else {
            QueryBuilder<LocalFile> queryBuilder = mLocalFileDao.queryBuilder();
            queryBuilder.whereOr(LocalFileDao.Properties.UserId.eq(userId), LocalFileDao.Properties.UserId.eq(""));
            List<LocalFile> result = queryBuilder.list();
            Collections.reverse(result);
            return result;
        }
    }

    /**
     * 加载所有的视频
     */
    public List<LocalFile> loadAllVideo(String userId) {
        return loadFilesByType(userId, CameraFragment.TYPE_VIDEO);
    }

    /**
     * 根据资料类型获取列表
     *
     * @param userId 当前用户的userId
     * @param type   {@link CameraFragment#TYPE_PHOTO}
     */
    public List<LocalFile> loadFilesByType(String userId, int type) {
        if (isPhoneLocal) {
            switch (type){
                case CameraFragment.TYPE_PHOTO:
                    photoCursorTask.execute();
                    break;
                case CameraFragment.TYPE_AUDIO:

                    break;
                case CameraFragment.TYPE_VIDEO:
                    videoCursorTask.execute();
                    break;
                case CameraFragment.TYPE_LIVE:
                    getLiveList();
                break;
            }
            return null;
        } else {
            QueryBuilder<LocalFile> builder = mLocalFileDao.queryBuilder();
            builder.where(LocalFileDao.Properties.Type.eq(type),
                    LocalFileDao.Properties.UserId.eq(userId))
                    .orderDesc(LocalFileDao.Properties.Id);
            List<LocalFile> result = builder.list();
            Collections.reverse(result);
            return result;
        }
    }

    /**
     * 获取直播列表
     */
    private void getLiveList() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("statusShow", 1);
        NetWorkUtil.postForm(context, VideoConstant.VIDEO_LIST, new MResponseListener() {
            @Override
            protected void onDataFine(JSONArray data) {
                List<LocalFile> localFileList = new ArrayList<>();
                if (data.length() > 0) {// 直播列表列表个数
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            System.out.println(data);
                            JSONObject live = data.getJSONObject(i);
                            LocalFile localFile = new LocalFile();
                            localFile.setId(live.getLong("id"));
                            localFile.setLocalPath(JsonUtil.getJson(live, "url"));
                            localFile.setRemotePath(JsonUtil.getJson(live, "url"));
                            localFile.setCreateTime(JsonUtil.getJsonLong(live,"createAt"));
                            localFile.setThumbnailPath(JsonUtil.getJson(live,"imgUrl"));
                            localFile.setUserId(UserSharedPreference.getUserId());
                            localFile.setType(CameraFragment.TYPE_LIVE);
                            localFileList.add(localFile);
                        }
                        photoListener.GetLocalFile(localFileList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, map);
    }

    public LocalFile loadLocalFileById(long id) {
        if (isPhoneLocal) {
            return null;
        } else {
            return mLocalFileDao.load(id);
        }
    }

    private DaoMaster.DevOpenHelper generateOpenHelper(Context context) {
        if (s_Helper == null || s_Helper.get() == null) {
            s_Helper = new WeakReference<DaoMaster.DevOpenHelper>(
                    new DaoMaster.DevOpenHelper(context.getApplicationContext(), "LocalFile", null));
        }
        return s_Helper.get();
    }

    public boolean isPhoneLocal() {
        return isPhoneLocal;
    }

    public void setPhoneLocal(boolean phoneLocal) {
        isPhoneLocal = phoneLocal;
    }
}
