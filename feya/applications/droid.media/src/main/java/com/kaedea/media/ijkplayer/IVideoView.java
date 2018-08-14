/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.media.ijkplayer;

import android.view.View;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Kaede on 16/7/18.
 */
public interface IVideoView {

    int AR_ASPECT_FIT_PARENT = 0;          // without clip
    int AR_ASPECT_FILL_PARENT = 1;         // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_MATCH_PARENT = 3;
    int AR_16_9_FIT_PARENT = 4;
    int AR_4_3_FIT_PARENT = 5;

    View getView();

    void setMediaPlayer(IMediaPlayer mMediaPlayer);

    void setVideoSampleAspect(int videoSarNum, int videoSarDen);

    void setVideoRotation(int videoRotation);

    void adjustSize(int surfaceViewWidth, int surfaceViewHeight, int videoWidth, int videoHeight);

    void resume(IMediaPlayer mediaPlayer);

    void stop();

    void release();
}
