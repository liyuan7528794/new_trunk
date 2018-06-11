package com.travel.AdapterJoiner;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.travel.layout.HeadZoomRecyclerView;
import com.travel.AdapterJoiner.AdapterJoiner.Joinable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一个用于拼装多个Adapter的辅助类
 * <pre>
 *     AdapterJoiner joiner = new AdapterJoiner();
 *     JoinableLayout layout = new JoinableLayout(..);
 *     JoinableAdapter adapter = new JoinableAdapter(mRealAdapter);
 *     ....
 *     mRecyclerView.setAdapter(joiner.getAdapter());
 * </pre>
 *
 * 命名规则如下：
 * <ul>
 *     <li>JoinedPos: 代表在合成的Adapter中所占的AdapterPosition</li>
 *     <li>RealPos:   代表在各自相应的{@code Joinable}中对应的真实AdapterPosition</li>
 *     <li>Map的命名规则: AToB 代表以A为Key， B为Value</li>
 * </ul>
 * Created by ldkxingzhe on 2017/1/5.
 */
public class AdapterJoiners {
    private static final String TAG = "AdapterJoiner";

    @VisibleForTesting ShadowAdapter mShadowAdapter = new ShadowAdapter();
    @VisibleForTesting Map<AdapterJoiner.Joinable, DataObserver> mJoinableToDataObserver = new HashMap<>();
    @VisibleForTesting Map<DataObserver, Joinable> mDataObserverToJoinable = new HashMap<>();
    @VisibleForTesting List<Joinable> mJoinableList = new ArrayList<>();
    // update on structure change, type
    @VisibleForTesting SparseIntArray mJoinedTypeToRealType = new SparseIntArray();
    @VisibleForTesting SparseArray<Joinable> mJoinedTypeToJoinable = new SparseArray<>();
    @VisibleForTesting Map<Joinable, SparseIntArray> mJoinableToRealTypeToJoinedType = new HashMap<>();

    // update on every data change pos
    @VisibleForTesting List<Integer> mJoinedPosToJoinType = new ArrayList<>();
    @VisibleForTesting List<Integer> mJoinedPosToRealPos = new ArrayList<>();
    @VisibleForTesting List<Joinable> mJoinedPosToJoinable = new ArrayList<>();
    @VisibleForTesting Map<Joinable, int[]> mJoinableToRealPosition = new HashMap<>();
    @VisibleForTesting int mLastGenerateTypeId = 0;
    @VisibleForTesting int mCurrentItemCount = 0;
    public AdapterJoiners(){}


    public RecyclerView.Adapter getAdapter(){
        onDataSetChanged();
        return mShadowAdapter;
    }

    public void add(Joinable joinable){
        add(mJoinableList.size(), joinable);
    }

    public void add(int index, @NonNull Joinable joinable){
        if (mJoinableList.contains(joinable))
            throw new IllegalStateException("joinable has been add to this adapterJoiner");

        mJoinableList.add(index, joinable);
        onStructureChanged(joinable);
        if(mJoinableToDataObserver.get(joinable) == null){
            DataObserver dataObserver = new DataObserver();
            mJoinableToDataObserver.put(joinable, dataObserver);
            mDataObserverToJoinable.put(dataObserver,joinable);
            joinable.getAdapter().registerAdapterDataObserver(dataObserver);
        }
    }

    public void replace(int index, Joinable joinable){
        if(index > mJoinableList.size()){
            add(mJoinableList.size(), joinable);
            return;
        }
        if(mJoinableList.contains(joinable)){
            return;
        }
        mJoinableList.set(index, joinable);
        onStructureChanged(joinable);
        if(mJoinableToDataObserver.get(joinable) == null){
            DataObserver dataObserver = new DataObserver();
            mJoinableToDataObserver.put(joinable, dataObserver);
            mDataObserverToJoinable.put(dataObserver,joinable);
            joinable.getAdapter().registerAdapterDataObserver(dataObserver);
        }
    }

    /**
     * 是否已经存在拼接的Joinable
     * @param joinable
     * @return
     */
    public boolean isContains(Joinable joinable){
        return mJoinableList.contains(joinable);
    }

    public void remove(@NonNull Joinable joinable){
        if (!mJoinableList.contains(joinable))
            throw new IllegalStateException("joinableList not container joinable");

        int joinedPos = getJoinedPosition(joinable, 0);
        mJoinableList.remove(joinable);
        onStructureChanged(joinable);
        onDataSetChanged();
        mShadowAdapter.notifyItemRangeRemoved(joinedPos, joinable.getAdapter().getItemCount());
        DataObserver dataObserver = mJoinableToDataObserver.get(joinable);
        if (dataObserver != null){
            mJoinableToDataObserver.remove(joinable);
            mDataObserverToJoinable.remove(dataObserver);
            joinable.getAdapter().unregisterAdapterDataObserver(dataObserver);
        }
    }

    /**
     * 由ViewHolder获取真实的Adapter位置
     */
    public int getAdapterPositionByViewHolder(@NonNull RecyclerView.ViewHolder viewHolder){
        Integer integer = mJoinedPosToRealPos.get(viewHolder.getAdapterPosition());
        return integer == null ? RecyclerView.NO_POSITION : integer;
    }

    /**
     * 根据Joinable对象与真实位置， 获取对应Joined位置
     */
    public int getJoinedPosition(@NonNull Joinable joinable, int realPosition){
        int[] realPositionToJoinedType = mJoinableToRealPosition.get(joinable);
        try{
            return realPositionToJoinedType[realPosition];
        }catch (ArrayIndexOutOfBoundsException e){
            return RecyclerView.NO_POSITION;
        }
    }

    public View getZoomView(){
        return mShadowAdapter.getZoomView();
    }

    public void setHeadZoomRecyler(HeadZoomRecyclerView headZoom){
        mShadowAdapter.setHeadZoomRecyler(headZoom);
    }

    @VisibleForTesting void onStructureChanged(Joinable diffJoinable){
        if(mJoinableList.contains(diffJoinable)){// 是添加操作
            SparseIntArray realPosToJoinedTyped = new SparseIntArray(diffJoinable.getTypeCount());
            for (int i = 0; i < diffJoinable.getTypeCount(); i++){
                int newTypeId = mLastGenerateTypeId++;
                mJoinedTypeToJoinable.put(newTypeId, diffJoinable);
                mJoinedTypeToRealType.put(newTypeId, diffJoinable.getTypeByIndex(i));
                realPosToJoinedTyped.put(diffJoinable.getTypeByIndex(i), newTypeId);
            }
            mJoinableToRealTypeToJoinedType.put(diffJoinable, realPosToJoinedTyped);
        }
    }

    private void notifyDataSetChange(){
        onDataSetChanged();
        mShadowAdapter.notifyDataSetChanged();
    }

    @VisibleForTesting void onDataSetChanged(){
        mCurrentItemCount = 0;
        mJoinedPosToJoinable.clear();
        mJoinedPosToRealPos.clear();
        mJoinedPosToJoinType.clear();
        mJoinableToRealPosition.clear();

        for (Joinable joinable: mJoinableList){
            int[] joinedArray = new int[joinable.getAdapter().getItemCount()];
            for(int i = 0; i < joinedArray.length; i++){
                joinedArray[i] = mCurrentItemCount;
                mCurrentItemCount++;
                int itemRealType = joinable.getAdapter().getItemViewType(i);
                int itemJoinedType = mJoinableToRealTypeToJoinedType.get(joinable).get(itemRealType);
                mJoinedPosToJoinType.add(itemJoinedType);
                mJoinedPosToRealPos.add(i);
                mJoinedPosToJoinable.add(joinable);
            }
            mJoinableToRealPosition.put(joinable, joinedArray);
        }
    }

    private class ShadowAdapter extends RecyclerView.Adapter{
        private ViewGroup zoomView;
        private HeadZoomRecyclerView headZoomRecyler;
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return mJoinedTypeToJoinable.get(viewType).getAdapter()
                    .onCreateViewHolder(parent,mJoinedTypeToRealType.get(viewType));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(position == 0) {
                zoomView = (ViewGroup) ((ViewGroup)holder.itemView.getRootView()).getChildAt(0);
            }
            mJoinedPosToJoinable.get(position).getAdapter()
                    .onBindViewHolder(holder, mJoinedPosToRealPos.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            return mJoinedPosToJoinType.get(position);
        }

        @Override
        public int getItemCount() {
            if(mCurrentItemCount <= 0){
                zoomView = null;
            }
            headZoomRecyler.setZoomContain(zoomView);
            return mCurrentItemCount;
        }

        public View getZoomView() {
            return zoomView;
        }

        public void setHeadZoomRecyler(HeadZoomRecyclerView headZoomRecyler) {
            this.headZoomRecyler = headZoomRecyler;
        }
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver{
        @Override
        public void onChanged() {
            notifyDataSetChange();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            onDataSetChanged();
            mShadowAdapter.notifyItemRangeChanged(getJoinedPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
//            onChanged();
            super.onItemRangeChanged(positionStart,itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            onDataSetChanged();
            mShadowAdapter.notifyItemRangeInserted(getJoinedPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            onDataSetChanged();
            mShadowAdapter.notifyItemRangeRemoved(getJoinedPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }

        private int getJoinedPosition(int realPos){
            return AdapterJoiners.this.getJoinedPosition(mDataObserverToJoinable.get(this), realPos);
        }
    }
}
