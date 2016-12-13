/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package com.kaedea.media.ijkplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.kaedea.media.BuildConfig;
import com.kaedea.media.R;

import java.io.File;
import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;


public class IjkPlayerActivity extends AppCompatActivity implements View.OnClickListener,
        MediaController.MediaPlayerControl,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnInfoListener {

    public static final String TAG = "FansPlayerActivity";
    public static final String EXTRA_SURFACE_MODE = "EXTRA_SURFACE_MODE";
    public static final int SURFACE_SURFACEVIEW = 0;
    public static final int SURFACE_TEXTUREVIEW = SURFACE_SURFACEVIEW + 1;
    public static final String EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI";
    public static final String EXTRA_LAST_POSITION = "EXTRA_LAST_POSITION";
    private AnimationDrawable mAnimationDrawable;
    private int mVideoRotation;
    private int mVideoSarNum;
    private int mVideoSarDen;

    public static Intent createIntent(Context context, int mode, String uri) {
        Intent intent = new Intent(context, IjkPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_SURFACE_MODE, mode);
        bundle.putString(EXTRA_VIDEO_URI, uri);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent createIntent(Context context, String uri) {
        Intent intent = new Intent(context, IjkPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_SURFACE_MODE, SURFACE_SURFACEVIEW);
        bundle.putString(EXTRA_VIDEO_URI, uri);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private View mRootView;
    private FrameLayout mVideoContainer;           // 播放区域容器
    private View mPreLoadingView;                  // 小电视抖抖抖
    private View mLoadingView;                     // 缓冲
    private View mErrorView;                       // 错误
    private IVideoView mVideoView;
    private IMediaPlayer mMediaPlayer;
    private MediaController mController;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceMode = SURFACE_TEXTUREVIEW;
    private String mUri;
    private boolean shouldResume = true;
    private long mLastPosition;
    private boolean mIsResumeAfterHomePressed;
    private boolean mHasPrepared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bili_app_activity_fans_player);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getBundle(savedInstanceState);
        findView();
        setListener();
        init();
    }

    private void getBundle(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }
        if (bundle != null) {
            mLastPosition = bundle.getLong(EXTRA_LAST_POSITION);
            mSurfaceMode = bundle.getInt(EXTRA_SURFACE_MODE, SURFACE_SURFACEVIEW);
            mUri = bundle.getString(EXTRA_VIDEO_URI);
        }
    }

    private void findView() {
        mRootView = findViewById(R.id.root);
        mVideoContainer = (FrameLayout) findViewById(R.id.video_container);
        mLoadingView = findViewById(R.id.loading);
        mPreLoadingView = findViewById(R.id.pre_loading);
        mErrorView = findViewById(R.id.layout_error);
    }

    private void setListener() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.back_error).setOnClickListener(this);
        findViewById(R.id.layout_refresh).setOnClickListener(this);
    }

    private void init() {
        ImageView ivPreLoading = (ImageView) findViewById(R.id.iv_pre_loading);
        Drawable drawable = ivPreLoading.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            mAnimationDrawable = (AnimationDrawable) drawable;
        }
        if (TextUtils.isEmpty(mUri)) {
            mUri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
        }
        // create video controller
        mController = new MediaController(this);
        mController.setMediaPlayerControl(this);
        mController.setAnchorView(mVideoContainer);
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.toggleControllerView();
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsResumeAfterHomePressed) {
            showLoadingView();
        } else {
            showPreLoadingView();
        }
        createVideoSurface();
        createMediaPlayer();
        loadMedia();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer.isPlaying()) {
            pause();
            shouldResume = true;
        } else {
            shouldResume = false;
        }
        mLastPosition = getCurrentPosition();
        mIsResumeAfterHomePressed = true;
        releaseMedia();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(EXTRA_LAST_POSITION, mLastPosition);
        outState.putInt(EXTRA_SURFACE_MODE, mSurfaceMode);
        outState.putString(EXTRA_VIDEO_URI, mUri);
        super.onSaveInstanceState(outState);
    }

    private void createVideoSurface() {
        // create video surface
        if (mSurfaceMode == SURFACE_SURFACEVIEW) {
            mVideoView = new VideoSurfaceView(this);
        } else {
            mVideoView = new VideoTextureView(this);
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mVideoView.getView().setLayoutParams(layoutParams);
        mVideoContainer.addView(mVideoView.getView(), 0);
    }

    private void createMediaPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) mMediaPlayer;
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            /*ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);*/
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mVideoView.setMediaPlayer(mMediaPlayer);
    }

    private void loadMedia() {
        if (mMediaPlayer != null) {
            try {
                Uri uri = Uri.parse(mUri);
                String scheme = uri.getScheme();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                    IMediaDataSource dataSource = new FileMediaDataSource(new File(uri.toString()));
                    mMediaPlayer.setDataSource(dataSource);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mMediaPlayer.setDataSource(this, uri, null);
                } else {
                    mMediaPlayer.setDataSource(uri.toString());
                }
                mMediaPlayer.prepareAsync();
            } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetMedia() {
        mHasPrepared = false;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                pause();
            }
            mMediaPlayer.reset();
        }
    }

    private void releaseMedia() {
        if (mController != null) {
            mController.release();
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
        mVideoContainer.removeViewAt(0);
    }

    private void showPreLoadingView() {
        mPreLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        if (mAnimationDrawable != null) mAnimationDrawable.start();
    }

    private void showLoadingView() {
        mPreLoadingView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mVideoContainer.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        if (mAnimationDrawable != null) mAnimationDrawable.stop();
    }

    private void showErrorView() {
        mPreLoadingView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        if (mAnimationDrawable != null) mAnimationDrawable.stop();
    }

    private void showVideoView() {
        mPreLoadingView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        if (mAnimationDrawable != null) mAnimationDrawable.stop();
    }

/*    private void hidePreLoadingView() {
        mPreLoadingView.setVisibility(View.GONE);
    }

    private void hideErrorView() {
        mErrorView.setVisibility(View.GONE);
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: // 加载界面，退出
            case R.id.back_error: // 错误界面，退出
                exit();
                break;
            case R.id.layout_refresh: // 错误界面，重试
                showPreLoadingView();
                loadMedia();
                shouldResume = true;
                break;
        }
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoView != null && mVideoWidth > 0 && mVideoHeight > 0) {
            mVideoView.adjustSize(mRootView.getMeasuredHeight(), mRootView.getMeasuredWidth(),
                    mVideoView.getView().getWidth(), mVideoView.getView().getHeight());
            mVideoView.setVideoSampleAspect(mVideoSarNum, mVideoSarDen);
        }
    }*/

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        mVideoSarNum = mp.getVideoSarNum();
        mVideoSarDen = mp.getVideoSarDen();
        if (mVideoHeight != 0 && mVideoWidth != 0  && mVideoView != null) {
            mVideoView.adjustSize(mRootView.getWidth(), mRootView.getHeight(), mVideoWidth, mVideoHeight);
            mVideoView.setVideoSampleAspect(mVideoSarNum, mVideoSarDen);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mHasPrepared = true;
        showVideoView();
        if (mLastPosition > 0) {
            if (mLastPosition < getDuration() - 1000L)
                seekTo(mLastPosition);
            mLastPosition = 0;
        }
        if (shouldResume) {
            start();
            shouldResume = false;
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mController.notifyUiChanged();
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        mController.onBufferingUpdate(percent);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "MediaPlayer Error : " + what + " " + extra);
        }
        resetMedia();
        showErrorView();
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                showLoadingView();
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                showVideoView();
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                break;
            case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                break;
            case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                break;
            case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + extra);
                mVideoRotation = extra;
                if (mVideoView != null)
                    mVideoView.setVideoRotation(extra);
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                break;
        }
        return true;
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
    public long getCurrentPosition() {
        if (null != mMediaPlayer && mHasPrepared)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public long getDuration() {
        if (null != mMediaPlayer && mHasPrepared)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    @Override
    public boolean isPlaying() {
        return null != mMediaPlayer && mMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            mController.notifyUiChanged();
        }
    }

    @Override
    public void seekTo(long i) {
        if (null != mMediaPlayer && mHasPrepared && mMediaPlayer.getDuration() >= i && i >= 0) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "seekTo " + i);
            }
            mLoadingView.setVisibility(View.VISIBLE);
            mMediaPlayer.seekTo(i);
        }
    }

    @Override
    public void start() {
        if (null != mMediaPlayer && mHasPrepared) {
            mMediaPlayer.start();
            mController.notifyUiChanged();
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
        return "title";
    }
}
