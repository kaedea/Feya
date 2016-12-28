/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package com.kaedea.media.home;

import android.content.Intent;
import android.support.v4.util.ArrayMap;

import com.kaedea.media.camera.CameraRecorderActivity;
import com.kaedea.media.mediaplayer.MediaPlayerActivity;
import com.kaedea.media.mediaplayer.VideoListActivity;

/**
 * Created by Kaede on 16/8/10.
 */
public class DemoProvider {
    public static ArrayMap<String, ActivityHolder> demos;
    static {
        demos = new ArrayMap<>();
        // 1. player
        ActivityHolder player = new ActivityHolder();
        // videoview
        player.addActivity("VideoView",
                "play video with VideoView & builtin MediaController",
                VideoListActivity.class,
                new Intent().putExtra(VideoListActivity.EXTRA_TARGET_ACTIVITY,
                        "com.kaedea.media.videoview.VideoViewActivity"));
        // media player
        player.addActivity("MediaPlayer 1",
                "play video with MediaPlayer(custom MediaController, SurfaceView)",
                VideoListActivity.class,
                new Intent().putExtra(MediaPlayerActivity.EXTRA_SURFACE_MODE, MediaPlayerActivity.SURFACE_SURFACEVIEW)
                        .putExtra(VideoListActivity.EXTRA_TARGET_ACTIVITY,
                                "com.kaedea.media.mediaplayer.MediaPlayerActivity"));
        player.addActivity("MediaPlayer 2",
                "play video with MediaPlayer(custom MediaController, TextureView)",
                VideoListActivity.class,
                new Intent().putExtra(MediaPlayerActivity.EXTRA_SURFACE_MODE, MediaPlayerActivity.SURFACE_TEXTUREVIEW)
                        .putExtra(VideoListActivity.EXTRA_TARGET_ACTIVITY,
                                "com.kaedea.media.mediaplayer.MediaPlayerActivity"));
        // ijk player
        player.addActivity("Ijk Player 1",
                "play video with ijk-player(custom MediaController, SurfaceView)",
                VideoListActivity.class,
                new Intent().putExtra(MediaPlayerActivity.EXTRA_SURFACE_MODE, MediaPlayerActivity.SURFACE_SURFACEVIEW)
                        .putExtra(VideoListActivity.EXTRA_TARGET_ACTIVITY,
                                "com.kaedea.media.ijkplayer.FansPlayerActivity"));
        player.addActivity("Ijk Player 2",
                "play video with ijk-player(custom MediaController, TextureView)",
                VideoListActivity.class,
                new Intent().putExtra(MediaPlayerActivity.EXTRA_SURFACE_MODE, MediaPlayerActivity.SURFACE_TEXTUREVIEW)
                        .putExtra(VideoListActivity.EXTRA_TARGET_ACTIVITY,
                                "com.kaedea.media.ijkplayer.FansPlayerActivity"));
        demos.put("Player", player);


        // 2. camera
        ActivityHolder camera = new ActivityHolder();
        // media recorder
        camera.addActivity("Camera Record",
                "record video from camera with MediaRecorder with custom config",
                CameraRecorderActivity.class);
        demos.put("Camera", camera);
    }
}
