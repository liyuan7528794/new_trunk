package com.travel.localfile.pk.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.travel.app.TravelApp;
import com.travel.lib.utils.AlertDialogUtils;
import com.travel.localfile.LocalFileSQLiteHelper;
import com.travel.localfile.dao.LocalFile;
import com.travel.localfile.module.UploadFileHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 证据选择的抽象类
 * Created by ldkxingzhe on 2017/2/13.
 */
public abstract class AbstractEvidenceSelect {

    protected final String mUserId;
    protected final Context mContext;
    protected List<LocalFile> mLocalFileList = new ArrayList<>();
    protected List<LocalFile> mSelectedList = new ArrayList<>();
    protected UploadFileHelper mUploadFileHelper;

    private RecyclerView mRecyclerView;
    private LocalFileSQLiteHelper mLocalFileSQLiteHelper;

    public AbstractEvidenceSelect(Context context, String userId){
        mContext = context;
        mUserId = userId;
        mRecyclerView = new RecyclerView(context);
        mLocalFileSQLiteHelper = new LocalFileSQLiteHelper(context);
        mLocalFileSQLiteHelper.setPhoneLocal(true);
        mLocalFileSQLiteHelper.init();
        mLocalFileSQLiteHelper.setPhotoListener(new LocalFileSQLiteHelper.GetMediaListener(){

            @Override
            public void GetLocalFile(List<LocalFile> localFiles) {
                mLocalFileList = localFiles;
                getAdapter().notifyDataSetChanged();
            }
        });
        mUploadFileHelper = new UploadFileHelper(context);
        mUploadFileHelper.setPhone(true);
    }

    protected void init(){
        loadFile();
        /*new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                loadFile();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getAdapter().notifyDataSetChanged();
            }
        }.execute();*/
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.setAdapter(getAdapter());
    }

    abstract void loadFile();
    abstract RecyclerView.Adapter getAdapter();
    abstract RecyclerView.LayoutManager getLayoutManager();
    public boolean canPublish(){
        for (LocalFile localFile: mSelectedList){
            if (!localFile.getIsUpLoaded()){
                Toast.makeText(TravelApp.appContext, "正在上传中，请稍后...", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
    public List<LocalFile> getSelectedList(){
        return mSelectedList;
    }

    public View createView(){
        return mRecyclerView;
    }

    protected void loadFileFromDB(int type){
//        mLocalFileList = mLocalFileSQLiteHelper.loadFilesByType(mUserId, type);
        mLocalFileSQLiteHelper.loadFilesByType(mUserId, type);
    }

    protected boolean select(LocalFile localFile){
        if (mSelectedList.size() >= 9){
            AlertDialogUtils.alertDialogOneButton(mContext, "每次最多上传9张", null);
            return false;
        }else{
            mSelectedList.add(localFile);
            return true;
        }
    }
}
