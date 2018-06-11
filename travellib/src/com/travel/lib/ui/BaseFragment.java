package com.travel.lib.ui;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.travel.lib.fragment_interface.Functions;

import java.util.List;

/**
 *
 */
public class BaseFragment extends Fragment {
    protected Functions functions;
    public void setFunctions(Functions functions) {
        this.functions = functions;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof BaseActivity){
            BaseActivity baseActivity = (BaseActivity) context;
            baseActivity.bindFunction(this);
        }
    }

    public interface StopLoadListener<T> {
        void stopLoad(List<T> list);
    }

    protected StopLoadListener stopLoadListener;

    public void setStopLoadListener(StopLoadListener stopLoadListener) {
        this.stopLoadListener = stopLoadListener;
    }
}
