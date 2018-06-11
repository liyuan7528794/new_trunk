package com.travel.AdapterJoiner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ldkxingzhe on 2017/1/5.
 */
public class JoinableLayout implements AdapterJoiner.Joinable{

    private MAdapter mAdapter;
    private boolean mIsHiding = false;  // 是否正在隐藏
    public interface OnNeedLayoutCallback {
        View onNeedLayout(Context context);
    }

    private final OnNeedLayoutCallback mOnNeedLayoutCallback;

    public JoinableLayout(OnNeedLayoutCallback onNeedLayout){
        mOnNeedLayoutCallback = onNeedLayout;
        mAdapter = new MAdapter();
    }

    @Override
    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getAdapter() {
        return mAdapter;
    }

    @Override
    public int getTypeCount() {
        return 1;
    }

    @Override
    public int getTypeByIndex(int typeIndex) {
        return 0;
    }

    public void hide(){
        if(!mIsHiding){
            mIsHiding = true;
            mAdapter.notifyItemRemoved(0);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void show(){
        if (mIsHiding){
            mIsHiding = false;
            mAdapter.notifyItemInserted(0);
        }
    }

    private class MAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MViewHolder(mOnNeedLayoutCallback.onNeedLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // ignore
        }

        @Override
        public int getItemCount() {
            return mIsHiding ? 0 : 1;
        }

        private class MViewHolder extends RecyclerView.ViewHolder{

            public MViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
