package com.travel.shop.tools;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.travel.imserver.BuildConfig;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by ldkxingzhe on 2016/12/20.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class ShopToolTest {

    private Gson mGson = new Gson();

    @Test
    public void mapToJson() throws Exception {
        ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("test", "ok");
        mapList.add(map);
        mapList.add(map);
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(ShopTool.mapToJson(mapList)).getAsJsonArray();
        Assert.assertEquals(2, jsonArray.size());
        Assert.assertEquals("ok", jsonArray.get(1).getAsJsonObject().get("test").getAsString());
    }

    @Test
    public void mapToJsonMap() throws Exception{
        HashMap<String, Object> map = new HashMap<>();
        map.put("test", 1);
        map.put("ok", "fine");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(ShopTool.mapToJson(map)).getAsJsonObject();
        assertEquals(1, jsonObject.get("test").getAsInt());
        assertEquals("fine", jsonObject.get("ok").getAsString());
    }

}