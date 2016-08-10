package me.kaede.feya.home;

import android.support.v4.util.ArrayMap;

import me.kaede.feya.service.ServiceDemoActivity;

/**
 * Created by Kaede on 16/8/10.
 */
public class DemoProvider {
    public static ArrayMap<String, ActivityHolder> demos;

    static {
        demos = new ArrayMap<>();
        ActivityHolder camera = new ActivityHolder();
        // media recorder
        camera.addActivity("Service",
                "Service api usage example",
                ServiceDemoActivity.class);
        demos.put("Default", camera);
    }
}
