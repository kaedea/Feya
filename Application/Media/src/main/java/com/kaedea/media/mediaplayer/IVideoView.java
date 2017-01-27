/*
 * Copyright (c) 2016. Kaede <kidhaibra@gmail.com>
 */

package com.kaedea.media.mediaplayer;

import android.media.MediaPlayer;
import android.view.View;

/**
 * Created by Kaede on 16/7/18.
 */
public interface IVideoView {
    View getView();

    void setMediaPlayer(MediaPlayer mMediaPlayer);

    void adjustSize(int surfaceViewWidth, int surfaceViewHeight, int videoWidth, int videoHeight);

    void release();
}
