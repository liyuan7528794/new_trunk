package com.travel.shop.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.travel.lib.ui.TitleBarBaseActivity;
import com.travel.lib.utils.ImageDisplayTools;
import com.travel.lib.utils.OSUtil;
import com.travel.lib.utils.UserSharedPreference;
import com.travel.shop.R;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/14.
 */

public class CreateBarCodeActivity extends TitleBarBaseActivity{
    private ImageView iv_head, iv_barCode;
    private TextView tv_name, tv_id;
    private String content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_barcode);
        setTitle("二维码");
        content = getIntent().getStringExtra("orderId");
        iv_head = findView(R.id.iv_head);
        iv_barCode = findView(R.id.iv_barCode);
        tv_name = findView(R.id.tv_name);
        tv_id = findView(R.id.tv_id);
        ImageDisplayTools.displayImage(UserSharedPreference.getUserHeading(), iv_head);
        tv_name.setText(UserSharedPreference.getNickName());
        tv_id.setText("ID:" + UserSharedPreference.getUserId());
        Bitmap bitmap = generateBitmap(content, OSUtil.dp2px(this, 216), OSUtil.dp2px(this, 216));
        iv_barCode.setImageBitmap(bitmap);
    }


    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, (Hashtable) hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
