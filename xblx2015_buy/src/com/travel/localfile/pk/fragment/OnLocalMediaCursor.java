package com.travel.localfile.pk.fragment;

import com.travel.localfile.dao.LocalFile;

import java.util.List;

/**
 * Created by Administrator on 2017/6/16.
 */

public interface OnLocalMediaCursor {
    void onLoadPhotoSursorResult(List<LocalFile> localFiles);
}
