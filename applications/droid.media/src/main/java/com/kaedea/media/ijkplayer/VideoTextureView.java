/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.media.ijkplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import com.kaedea.media.BuildConfig;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * Created by Kaede on 16/7/18.
 */
public class VideoTextureView extends TextureView implements IVideoView, TextureView.SurfaceTextureListener {

    public static final String TAG = "VideoTextureView";
    private static final int MARGIN_DP = 0;

    private IMediaPlayer mMediaPlayer;
    private MeasureHelper mMeasureHelper;
    private Surface surface;
    private boolean mIsDestroyed;

    public VideoTextureView(Context context) {
        super(context);
        init(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        mMeasureHelper = new MeasureHelper(this);
    }

    @Override
    public View getView() {
        return this;
    }


    @Override
    public void setMediaPlayer(IMediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
        setSurfaceTextureListener(this);
    }

    @Override
    public void setVideoSampleAspect(int videoSarNum, int videoSarDen) {
        mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
    }

    @Override
    public void setVideoRotation(int videoRotation) {
        mMeasureHelper.setVideoRotation(videoRotation);
        setRotation(videoRotation);
    }

    @Override
    public void adjustSize(int surfaceViewWidth, int surfaceViewHeight, int videoWidth, int videoHeight) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "[kaede][adjustSize]surfaceViewWidth = " + surfaceViewWidth +
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
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    @Override
    public void stop() {

    }

    @Override
    public void resume(IMediaPlayer mediaPlayer) {

    }

    @Override
    public void release() {
        mMediaPlayer = null;
        this.mIsDestroyed = true;
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
        if (mIsDestroyed) {
            surface.release();
            surface = null;
        }
        return mIsDestroyed;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
