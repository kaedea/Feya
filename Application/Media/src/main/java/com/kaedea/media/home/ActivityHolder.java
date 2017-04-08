/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.media.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.kaedea.media.camera.CameraRecorderActivity;
import com.kaedea.media.mediaplayer.MediaPlayerActivity;
import com.kaedea.media.mediaplayer.VideoListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * list and navigate demo activities.
 * Created by kaede on 2015/10/13.
 */
class ActivityHolder {

    List<ActivityEntry> entries;

    ActivityHolder() {
        entries = new ArrayList<>();
    }

    public int getCount() {
        return entries.size();
    }

    void addActivity(@NonNull String name, String description, Class<? extends Activity> activity, Intent intent) {
        ActivityEntry activityHolder = new ActivityEntry();
        activityHolder.name = name;
        activityHolder.description = description;
        activityHolder.activity = activity;
        activityHolder.intent = intent;
        entries.add(activityHolder);
    }

    void addActivity(String name, String description, Class<? extends Activity> activity) {
        addActivity(name, description, activity, null);
    }

    public String getActivityName(int position) {
        return entries.get(position).name;
    }

    public String getActivityDesc(int position) {
        return entries.get(position).description;
    }

    public Class<? extends Activity> getActivity(int position) {
        return entries.get(position).activity;
    }

    public Intent getIntent(int position) {
        return entries.get(position).intent;
    }

    public void startActivity(Context context, int position) {
        Intent intent = getIntent(position);
        if (intent == null) {
            context.startActivity(new Intent(context, getActivity(position)));
            return;
        }
        intent.setClass(context, getActivity(position));
        context.startActivity(intent);
    }

    public static class ActivityEntry {
        String name;
        String description;
        Class<? extends Activity> activity;
        Intent intent;
    }
}