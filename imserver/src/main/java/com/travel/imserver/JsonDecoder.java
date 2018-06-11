/*
package com.travel.imserver;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

*/
/**
 * Json解析器
 * Created by ldkxingzhe on 2016/12/7.
 *//*

public class JsonDecoder<T> extends ByteToMessageDecoder {
    @SuppressWarnings("unused")
    private static final String TAG = "JsonDecoder";
    private Class<T> mClazz;
    private Gson mGson;

    public JsonDecoder(Class<T> clazz){
        mClazz = clazz;
        mGson = new Gson();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(in);
        JsonReader reader = new JsonReader(new PrintReader(byteBufInputStream, "UTF-8"));
//        reader.beginObject();
        out.add(mGson.fromJson(reader, mClazz));
//        reader.endObject();
        reader.close();
    }

    private static class PrintReader extends InputStreamReader{

        public PrintReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
            super(in, charsetName);
        }

        @Override
        public int read(char[] cbuf, int offset, int length) throws IOException {
            int result = super.read(cbuf, offset, length);
            if(result != -1){
                Log.v(TAG, "parser buffer: " + String.valueOf(cbuf, offset, result));
            }
            return result;
        }
    }
}
*/
