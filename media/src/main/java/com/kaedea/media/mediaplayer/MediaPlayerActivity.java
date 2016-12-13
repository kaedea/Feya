/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package com.kaedea.media.mediaplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import com.kaedea.media.InternalUtils;
import com.kaedea.media.R;

import java.io.IOException;

public class MediaPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayerControl,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener {

    public final static String TAG = "MediaPlayerActivity";
    public static final String EXTRA_SURFACE_MODE = "EXTRA_SURFACE_MODE";
    public static final int SURFACE_SURFACEVIEW = 0;
    public static final int SURFACE_TEXTUREVIEW = SURFACE_SURFACEVIEW + 1;
    public static final String EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI";

    private IVideoView mVideoView;
    private MediaPlayer mMediaPlayer;
    private MediaController mController;
    private int mVideoWidth;
    private int mVideoHeight;
    private View mRootView;
    private View mLoadingView;
    private FrameLayout mVideoContainer;
    private int mSurfaceMode = SURFACE_SURFACEVIEW;
    private String mUri;
    private boolean shouldResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSurfaceMode = extras.getInt(EXTRA_SURFACE_MODE, SURFACE_SURFACEVIEW);
            mUri = extras.getString(EXTRA_VIDEO_URI);
        }
        mRootView = findViewById(R.id.root);
        mVideoContainer = (FrameLayout) findViewById(R.id.video_container);
        mLoadingView = findViewById(R.id.loading);
        createMediaPlayer();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        controller.show();
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mMediaPlayer.isPlaying() && shouldResume) {
            start();
            shouldResume = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer.isPlaying()) {
            pause();
            shouldResume = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetMediaPlayer();
    }

    private void createMediaPlayer() {
        mLoadingView.setVisibility(View.VISIBLE);
        // create video surface
        if (mSurfaceMode == SURFACE_SURFACEVIEW) {
            mVideoView = new VideoSurfaceView(this);
        } else {
            mVideoView = new VideoTextureView(this);
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mVideoView.getView().setLayoutParams(layoutParams);
        mVideoContainer.addView(mVideoView.getView(), 0);
        mMediaPlayer = new MediaPlayer();
        mVideoView.setMediaPlayer(mMediaPlayer);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mController = new MediaController(this);
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this, Uri.parse(mUri));
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        mVideoView.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.toggleControllerView();
                return false;
            }
        });
    }

    private void resetMediaPlayer() {
        if (mController != null) {
            mController.release();
            mController = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        Log.i(TAG, "mp.getVideoWidth() = " + mVideoWidth + ", mp.getVideoHeight() = " + mVideoHeight +
                ", width = " + width + ", height = " + height);
        if (mVideoHeight > 0 && mVideoWidth > 0)
            mVideoView.adjustSize(mRootView.getWidth(), mRootView.getHeight(), mVideoWidth,
                    mVideoHeight);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            mVideoView.adjustSize(InternalUtils.getScreenWidth(this), InternalUtils.getScreenHeight(this),
                    mVideoView.getView().getWidth(), mVideoView.getView().getHeight());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //setup video controller view
        mController.setMediaPlayerControl(this);
        mLoadingView.setVisibility(View.GONE);
        mVideoView.getView().setVisibility(View.VISIBLE);
        mController.setAnchorView(mVideoContainer);
        mController.setGestureListener();
        mMediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    /**
     * Implement VideoMediaController.MediaPlayerControl
     */
    @Override
    public boolean canPause() {
        return true;
    }


    @Override
    public boolean canSeek() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (null != mMediaPlayer)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public int getDuration() {
        if (null != mMediaPlayer)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    @Override
    public boolean isPlaying() {
        if (null != mMediaPlayer)
            return mMediaPlayer.isPlaying();
        else
            return false;
    }

    @Override
    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
        }

    }

    @Override
    public void seekTo(int i) {
        if (null != mMediaPlayer) {
            mMediaPlayer.seekTo(i);
        }
    }

    @Override
    public void start() {
        if (null != mMediaPlayer) {
            mMediaPlayer.start();
        }
    }

    @Override
    public boolean isFullScreen() {
        return getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    @Override
    public void toggleFullScreen() {
        if (isFullScreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public String getTopTitle() {
        return "buck bunny".toUpperCase();
    }
    // End VideoMediaController.MediaPlayerControl

}
