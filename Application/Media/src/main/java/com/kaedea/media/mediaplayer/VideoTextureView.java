/*
 * Copyright (c) 2016. Kaede <kidhaibra@gmail.com>
 */

package com.kaedea.media.mediaplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Kaede on 16/7/18.
 */
public class VideoTextureView extends TextureView implements IVideoView, TextureView.SurfaceTextureListener{
    private static final int MARGIN_DP = 0;

    MediaPlayer mMediaPlayer;
    private Surface surface;
    boolean isDestroyed;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public View getView() {
        return this;
    }


    @Override
    public void setMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
        setSurfaceTextureListener(this);
    }

    @Override
    public void adjustSize(int surfaceViewWidth, int surfaceViewHeight, int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            int windowWidth = displayMetrics.widthPixels;
            int windowHeight = displayMetrics.heightPixels;
            int margin = (int) (getContext().getResources().getDisplayMetrics().density* MARGIN_DP);
            float videoRatio = 0;
            if (windowWidth < windowHeight) {
                videoRatio = ((float) (videoWidth)) / videoHeight;
            } else {
                videoRatio = ((float) (videoHeight)) / videoWidth;
            }
            if (windowWidth < windowHeight) {// portrait
                if (videoWidth > videoHeight) {
                    if (surfaceViewWidth / videoRatio > surfaceViewHeight) {
                        lp.height = surfaceViewHeight;
                        lp.width = (int) (surfaceViewHeight * videoRatio);
                    } else {
                        lp.height = (int) (surfaceViewWidth / videoRatio);
                        lp.width = surfaceViewWidth;
                    }
                } else if (videoWidth <= videoHeight) {
                    if (surfaceViewHeight * videoRatio > surfaceViewWidth) {
                        lp.height = (int) (surfaceViewWidth / videoRatio);
                        lp.width = surfaceViewWidth;
                    } else {
                        lp.height = surfaceViewHeight;
                        lp.width = (int) (surfaceViewHeight * videoRatio);
                    }
                }
            } else if (windowWidth > windowHeight) {// landscape
                if (videoWidth > videoHeight) {//video is landscape
                    if (windowWidth * videoRatio > videoHeight) {
                        lp.height = windowHeight - margin;
                        lp.width = (int) ((windowHeight - margin) / videoRatio);
                    } else {
                        lp.height = (int) (windowWidth * videoRatio);
                        lp.width = windowWidth;
                    }
                } else if (videoWidth < videoHeight) {//video is portrait
                    lp.width = (int) ((windowHeight - margin) / videoRatio);
                    lp.height = windowHeight - margin;
                } else {
                    lp.height = windowHeight- margin;
                    lp.width = lp.height;
                }
            }
            setLayoutParams(lp);
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void release() {
        mMediaPlayer = null;
        this.isDestroyed = true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        surface = new Surface(surfaceTexture);
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (isDestroyed) {
            surface.release();
            surface = null;
        }
        return isDestroyed;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
