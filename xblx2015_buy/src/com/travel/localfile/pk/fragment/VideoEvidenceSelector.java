package com.travel.localfile.pk.fragment;

import android.content.Context;

import com.travel.localfile.CameraFragment;

/**
 * Created by ldkxingzhe on 2017/2/14.
 */

public class VideoEvidenceSelector extends PhotoEvidenceSelector{
    @SuppressWarnings("unused")
    private static final String TAG = "VideoEvidenceSelector";

    public VideoEvidenceSelector(Context context, String userId) {
        super(context, userId);
    }

    @Override
    void loadFile() {
        loadFileFromDB(CameraFragment.TYPE_VIDEO);
    }
}
