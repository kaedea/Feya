/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.json;

import android.test.InstrumentationTestCase;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Kaede on 16/8/3.
 */
public class JsonObjectTest extends InstrumentationTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*{
        "enable": 0,
            "force": 0,
            "min_build": 420000,
            "size": 10109047,
            "url": "http://dl.hdslb.com/bili/android-plugintmedia_r2_v2.0_20160615_155037.so",
            "ver_code": 2,
            "ver_name": "插件化二期"
    }*/

    public void testListJsonObject() throws JSONException {
        JSONObject jsonObject  = new JSONObject("{\n" +
                "        \"enable\": 0,\n" +
                "        \"force\": 0,\n" +
                "        \"min_build\": 420000,\n" +
                "        \"size\": 10109047,\n" +
                "        \"url\": \"http://dl.hdslb.com/bili/android-plugintmedia_r2_v2.0_20160615_155037.so\",\n" +
                "        \"ver_code\": 2,\n" +
                "        \"ver_name\": \"插件化二期\"\n" +
                "      }");
        JSONObject jsonMap = jsonObject.optJSONObject("tt_2009_4");
        Iterator<String> iterator = jsonMap.keys();
        Map<String, Object> maps = new HashMap<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (TextUtils.isEmpty(key)) continue;
            Object value = jsonMap.opt(key);
            if (value == null) continue;
            maps.put(key, value);
        }
    }
}
