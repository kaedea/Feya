/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.media.ijkplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kaedea.media.BuildConfig;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class VideoSurfaceView extends SurfaceView implements IVideoView, SurfaceHolder.Callback {
    public static final String TAG = "VideoSurfaceView";
    private static final int MARGIN_DP = 0;

    private MeasureHelper mMeasureHelper;
    private IMediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoSurfaceView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "surfaceview init");
        }
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setMediaPlayer(IMediaPlayer mediaPlayer) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "setMediaPlayer");
        }
        mMeasureHelper = new MeasureHelper(this);
        mMediaPlayer = mediaPlayer;
        getHolder().addCallback(this);
    }

    @Override
    public void setVideoSampleAspect(int videoSarNum, int videoSarDen) {
        mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
    }

    @Override
    public void setVideoRotation(int videoRotation) {
        mMeasureHelper.setVideoRotation(videoRotation);
    }

    /**
     * adjust SurfaceView area according to video width and height
     *
     * @param surfaceViewWidth  original
     * @param surfaceViewHeight
     * @param videoWidth
     * @param videoHeight
     */
    @Override
    public void adjustSize(int surfaceViewWidth, int surfaceViewHeight, int videoWidth, int videoHeight) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "[adjustSize]surfaceViewWidth = " + surfaceViewWidth +
                    ", surfaceViewHeight = " + surfaceViewHeight +
                    ", videoWidth = " + videoWidth +
                    ", videoHeight = " + videoHeight);
        }
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            setVisibility(View.VISIBLE);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = mMeasureHelper.getMeasuredWidth();
        int height = mMeasureHelper.getMeasuredHeight();
        getHolder().setFixedSize(width, height);
        setMeasuredDimension(width, height);
    }

    @Override
    public void stop() {

    }

    @Override
    public void resume(IMediaPlayer mediaPlayer) {
        setMediaPlayer(mediaPlayer);
    }

    @Override
    public void release() {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "surfaceHolder create");
        }
        mSurfaceHolder = surfaceHolder;
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mSurfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "surfaceHolder change");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "surfaceHolder destroy");
        }
    }
}