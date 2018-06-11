package com.travel.localfile.pk.fragment;

import android.content.Context;

import com.travel.localfile.CameraFragment;

/**
 * Created by Administrator on 2017/7/7.
 */

public class LiveEvidenceSelector extends PhotoEvidenceSelector{
    @SuppressWarnings("unused")
    private static final String TAG = "VideoEvidenceSelector";

    public LiveEvidenceSelector(Context context, String userId) {
        super(context, userId);
    }

    @Override
    void loadFile() {
        loadFileFromDB(CameraFragment.TYPE_LIVE);
    }
}