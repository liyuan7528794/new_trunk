package com.travel.AdapterJoiner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Created by ldkxingzhe on 2017/1/5.
 */
public class JoinableAdapter implements AdapterJoiner.Joinable{
    private final RecyclerView.Adapter<? extends RecyclerView.ViewHolder> mAdapter;
    private final int mTypeCount;

    public JoinableAdapter(@NonNull RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter,
                           int typeCount){
        mAdapter = adapter;
        mTypeCount = typeCount;
    }

    public JoinableAdapter(@NonNull RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter){
        this(adapter, 1);
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getTypeCount() {
        return mTypeCount;
    }

    @Override
    public int getTypeByIndex(int typeIndex) {
        return typeIndex;
    }
}
