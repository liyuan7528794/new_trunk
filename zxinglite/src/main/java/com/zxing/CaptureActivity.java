package com.zxing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.R;
import com.google.zxing.Result;
import com.google.zxing.client.android.AutoScannerView;
import com.google.zxing.client.android.BaseCaptureActivity;

/**
 * Created by Administrator on 2017/11/17.
 */

public class CaptureActivity extends BaseCaptureActivity {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private SurfaceView surfaceView;
    private AutoScannerView autoScannerView;
    private ImageView iv_scan_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wechat_capture);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        autoScannerView = (AutoScannerView) findViewById(R.id.autoscanner_view);
        iv_scan_back = (ImageView) this.findViewById(R.id.iv_scan_back);
        iv_scan_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScannerView.setCameraManager(cameraManager);
    }



    @Override
    public SurfaceView getSurfaceView() {
        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        Log.i(TAG, "dealDecode ~~~~~ " + rawResult.getText() + " " + barcode + " " + scaleFactor);
        playBeepSoundAndVibrate(true, false);
        Toast.makeText(this, rawResult.getText(), Toast.LENGTH_LONG).show();
        playBeepSoundAndVibrate();

        String resultString = rawResult.getText();
        // FIXME
        if (resultString.equals("")) {
            Toast.makeText(CaptureActivity.this, "Scan failed!",
                    Toast.LENGTH_SHORT).show();
        } else {
            // System.out.println("Result:"+resultString);
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
        }
        CaptureActivity.this.finish();
//        对此次扫描结果不满意可以调用
//        reScan();
    }
}