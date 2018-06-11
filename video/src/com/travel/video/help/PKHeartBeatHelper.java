package com.travel.video.help;

import android.support.annotation.NonNull;

/**
 * Created by ldkxingzhe on 2016/11/4.
 */

public class PKHeartBeatHelper extends HeartBeatHelper{
    @SuppressWarnings("unused")
    private static final String TAG = "PKHeartBeatHelper";

    private String mUserType;
    public PKHeartBeatHelper() {
        super();
    }

    public PKHeartBeatHelper setUserType(@NonNull Object userType){
        mUserType = userType.toString();
        return this;
    }

    @NonNull
    @Override
    protected String generateHeartStr(boolean isClose) {
        String FTS = " ";
        return "PkHeartbeat" + FTS + mRoomNum + FTS + mUserId + FTS + mUserType;
    }
}
