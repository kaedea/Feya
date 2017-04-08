/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.home;

import android.support.v4.util.ArrayMap;

import me.kaede.feya.protobuff.ProtobuffActivity;
import me.kaede.feya.service.ServiceDemoActivity;
import me.kaede.feya.webview.WebActivity;

/**
 * Created by Kaede on 16/8/10.
 */
public class DemoProvider {
    public static ArrayMap<String, ActivityHolder> demos;

    static {
        demos = new ArrayMap<>();
        ActivityHolder camera = new ActivityHolder();

        // default demos
        camera.addActivity("Service", "Service api usage example", ServiceDemoActivity.class);
        camera.addActivity("WebView", "JS Bridge, performance monitor", WebActivity.class);
        camera.addActivity("Protocol Buff", "Protobuff, using square wire", ProtobuffActivity.class);
        demos.put("Default", camera);
    }
}
