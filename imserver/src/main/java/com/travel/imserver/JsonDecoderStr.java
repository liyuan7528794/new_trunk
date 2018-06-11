/*
package com.travel.imserver;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

*/
/**
 * Json解析器, 基于String类型
 * Created by ldkxingzhe on 2016/12/7.
 *//*

public class JsonDecoderStr<T extends Class> extends MessageToMessageDecoder<String> {
    @SuppressWarnings("unused")
    private static final String TAG = "JsonDecoder";
    private Gson mGson;
    private T mClazz;

    public JsonDecoderStr(T clazz){
        mGson = new Gson();
        mClazz = clazz;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        Log.v(TAG, "decode: " + msg);
        if(msg == null || TextUtils.isEmpty(msg.trim())){
            throw new IllegalStateException("消息为空");
        }
        out.add(mGson.fromJson(msg, mClazz));
    }
}
*/
