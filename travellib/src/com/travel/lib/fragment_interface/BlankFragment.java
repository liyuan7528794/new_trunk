package com.travel.lib.fragment_interface;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.lib.R;


/**
 */
public class BlankFragment extends Fragment {
    public static final String TAG = "BlankFragment";
    public static final String FUNC_PR = BlankFragment.class.getSimpleName() + "PUNC_PR";
    int num = 0;
    protected Functions functions;
    public void setFunctions(Functions functions) {
        this.functions = functions;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        TextView fragmentSend = (TextView) view.findViewById(R.id.fragmentSend);
        final TextView fragmentReceiver = (TextView) view.findViewById(R.id.fragmentReveiver);
        fragmentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final int result = functions.invokeFunc(FUNC_PR, Integer.class, num++);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragmentReceiver.setText("我收到了Activity的返回结果：" + result);
                        }
                    },500);
                } catch (NoFunctionException e) {
                    e.printStackTrace();
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ComunicationActivity){
            ComunicationActivity comunicationActivity = (ComunicationActivity) context;
            comunicationActivity.bindFunction(getTag());
        }
    }


}
