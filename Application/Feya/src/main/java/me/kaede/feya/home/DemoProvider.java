/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.home;

import android.support.v4.util.ArrayMap;

import me.kaede.feya.links.LinksActivity;
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
        ActivityHolder tab1 = new ActivityHolder();

        // default demos
        tab1.addActivity("Service", "Service api usage example", ServiceDemoActivity.class);
        tab1.addActivity("WebView", "JS Bridge, performance monitor", WebActivity.class);
        tab1.addActivity("Protocol Buff", "ProtoBuff, using square wire", ProtobuffActivity.class);
        tab1.addActivity("Links", "Interact with 3rd Apps.", LinksActivity.class);
        demos.put("Default", tab1);
    }
}
