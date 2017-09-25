/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.media.mediaplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import com.kaedea.media.R;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class MediaController extends FrameLayout implements VideoGestureListener {

    private static final String TAG = "MediaController";

    private static final int HANDLER_ANIMATE_OUT = 1;     // out animate
    private static final int HANDLER_UPDATE_PROGRESS = 2; // cycle update progress
    private static final long PROGRESS_SEEK = 500;

    private MediaPlayerControl mMediaPlayerControl;  // control media play
    private Context mContext;
    private ViewGroup mAnchorView;                    // anchor view
    private View mRootView;                           // root view of this
    private SeekBar mSeekBar;                         // seek bar for video
    private TextView mEndTime, mCurrentTime;
    private boolean mIsShowing;                       // controller view showing
    private boolean mIsDragging;                      // is dragging seekBar
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private GestureDetector mGestureDetector;         // gesture detector
    //top layout
    private View mTopLayout;
    private ImageButton mBackButton;
    private TextView mTitleText;
    //center layout
    private View mCenterLayout;
    private TextView mTvCenter;
    private ProgressBar mCenterProgress;
    private float mCurBrightness = -1;
    private int mCurVolume = -1;
    private AudioManager mAudioManager;
    private int mMaxVolume;
    //bottom layout
    private View mBottomLayout;
    private ImageButton mPauseButton;
    private ImageButton mFullscreenButton;

    private Handler mHandler = new ControllerHandler(this);

    public MediaController(Context context) {
        super(context);
    }


    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRootView = null;
        mContext = context;
    }

    public MediaController(Activity context) {
        super(context);
        mContext = context;
    }


    /**
     * Handler prevent leak memory.
     */
    private static class ControllerHandler extends Handler {
        private final WeakReference<MediaController> mView;

        ControllerHandler(MediaController view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaController mediaController = mView.get();
            if (mediaController == null || mediaController.mMediaPlayerControl == null) {
                return;
            }
            int pos;
            switch (msg.what) {
                case HANDLER_ANIMATE_OUT:
                    mediaController.hide();
                    break;
                case HANDLER_UPDATE_PROGRESS: // cycle update seek bar progress
                    pos = mediaController.updateSeekProgress();
                    if (!mediaController.mIsDragging && mediaController.mIsShowing
                            && mediaController.mMediaPlayerControl.isPlaying()) { // just in case
                        // cycle update progress, using WeakReference to avoid leak
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    /**
     * Inflate view from exit xml layout
     *
     * @return the root view of {@link MediaController}
     */
    private View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.layout_media_controller, null);
        initControllerView();
        return mRootView;
    }

    /**
     * find all views inside {@link MediaController}
     * and init params
     */
    private void initControllerView() {
        //top layout
        mTopLayout = mRootView.findViewById(R.id.layout_top);
        mBackButton = (ImageButton) mRootView.findViewById(R.id.top_back);
        if (mBackButton != null) {
            mBackButton.requestFocus();
            mBackButton.setOnClickListener(mBackListener);
        }

        mTitleText = (TextView) mRootView.findViewById(R.id.top_title);

        //center layout
        mCenterLayout = mRootView.findViewById(R.id.layout_center);
        mCenterLayout.setVisibility(GONE);
        mTvCenter = (TextView) mRootView.findViewById(R.id.image_center_bg);
        mCenterProgress = (ProgressBar) mRootView.findViewById(R.id.progress_center);

        //bottom layout
        mBottomLayout = mRootView.findViewById(R.id.layout_bottom);
        mPauseButton = (ImageButton) mRootView.findViewById(R.id.bottom_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (ImageButton) mRootView.findViewById(R.id.bottom_fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mSeekBar = (SeekBar) mRootView.findViewById(R.id.bottom_seekbar);
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(mSeekListener);
            mSeekBar.setMax(1000);
        }

        mEndTime = (TextView) mRootView.findViewById(R.id.bottom_time);
        mCurrentTime = (TextView) mRootView.findViewById(R.id.bottom_time_current);

        //init formatter
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    public void release() {
    }

    /**
     * toggle {@link MediaController} show or not
     * this can be called when {@link View#onTouchEvent(MotionEvent)} happened
     */
    public void toggleControllerView() {
        if (!isShowing()) {
            show();
        } else {
            hide();
        }
    }

    /**
     * if {@link MediaController} is visible
     *
     * @return showing or not
     */
    public boolean isShowing() {
        return mIsShowing;
    }


    /**
     * show controller view
     */
    private void show() {
        if (!isShowing() && mAnchorView != null) {
            this.setVisibility(VISIBLE);
        }
        notifyUiChanged();
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            if (!mMediaPlayerControl.canPause()) {
                mPauseButton.setEnabled(false);
            }
        }
        mIsShowing = true;
    }

    /**
     * hide controller view
     */
    private void hide() {
        if (isShowing() && mAnchorView != null) {
            this.setVisibility(GONE);
        }
        mIsShowing = false;
    }

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
    private String stringToTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * set {@link #mSeekBar} progress
     * and video play time {@link #mCurrentTime}
     *
     * @return current play position
     */
    private int updateSeekProgress() {
        if (mMediaPlayerControl == null || mIsDragging) {
            return 0;
        }

        int position = mMediaPlayerControl.getCurrentPosition();
        int duration = mMediaPlayerControl.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            //get buffer percentage
            int percent = mMediaPlayerControl.getBufferPercentage();
            //set buffer progress
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringToTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringToTime(position));

        mTitleText.setText(mMediaPlayerControl.getTopTitle());
        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mCurVolume = -1;
                mCurBrightness = -1;
                mCenterLayout.setVisibility(GONE);
//                break;// do need bread,should let gestureDetector to handle event
            default://gestureDetector handle other MotionEvent
                mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private void notifyUiChanged() {
        if (mRootView == null || mMediaPlayerControl == null) {
            return;
        }
        if (mMediaPlayerControl.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_fans_player_stop);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_fans_player_play);
        }
        if (mMediaPlayerControl.isFullScreen()) {
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
        } else {
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        }
        //update progress
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
    }

    public void doPauseResume() {
        if (mMediaPlayerControl == null) {
            return;
        }
        if (mMediaPlayerControl.isPlaying()) {
            mMediaPlayerControl.pause();
        } else {
            mMediaPlayerControl.start();
        }
        notifyUiChanged();
    }

    private void doToggleFullscreen() {
        if (mMediaPlayerControl == null) {
            return;
        }
        mMediaPlayerControl.toggleFullScreen();
        notifyUiChanged();
    }

    /**
     * Seek bar drag listener
     */
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show();
            mIsDragging = true;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mMediaPlayerControl == null) {
                return;
            }

            if (!fromuser) {
                return;
            }

            long duration = mMediaPlayerControl.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mMediaPlayerControl.seekTo((int) newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringToTime((int) newPosition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mIsDragging = false;
            show();
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }


    /**
     * set top back click listener
     */
    private View.OnClickListener mBackListener = new View.OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControl.exit();
        }
    };


    /**
     * set pause click listener
     */
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
        }
    };

    /**
     * set full screen click listener
     */
    private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
        }
    };

    /**
     * setMediaPlayerControlListener update play state
     *
     * @param mediaPlayerControl self
     */
    public void setMediaPlayerControl(MediaPlayerControl mediaPlayerControl) {
        mMediaPlayerControl = mediaPlayerControl;
        notifyUiChanged();
    }

    /**
     * set anchor view
     *
     * @param view view that hold controller view
     */
    public void setAnchorView(ViewGroup view) {
        mAnchorView = view;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        //remove all before add view
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
        //add controller view to bottom of the AnchorView
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mAnchorView.addView(this, tlp);
        this.setVisibility(GONE);
    }

    /**
     * set gesture listen to control media player
     * include screen brightness and volume of video
     * and seek video play
     */
    public void setGestureListener() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(mContext, new ViewGestureListener(mContext, this));
    }


    @Override
    public void onSingleTap() {
        toggleControllerView();
    }

    @Override
    public void onHorizontalScroll(boolean seekForward) {
        if (mMediaPlayerControl.canSeek()) {
            if (seekForward) {// seek forward
                seekForWard();
            } else {  //seek backward
                seekBackWard();
            }
        }
    }

    private void seekBackWard() {
        if (mMediaPlayerControl == null) {
            return;
        }

        int pos = mMediaPlayerControl.getCurrentPosition();
        pos -= PROGRESS_SEEK;
        mMediaPlayerControl.seekTo(pos);
        show();
    }

    private void seekForWard() {
        if (mMediaPlayerControl == null) {
            return;
        }
        int pos = mMediaPlayerControl.getCurrentPosition();
        pos += PROGRESS_SEEK;
        mMediaPlayerControl.seekTo(pos);
        show();
    }

    @Override
    public void onVerticalScroll(float percent, int direction) {
        if (direction == ViewGestureListener.SWIPE_LEFT) {
            mTvCenter.setText("Brightness");
            updateBrightness(percent);
        } else {
            mTvCenter.setText("Volume");
            updateVolume(percent);
        }
    }

    /**
     * update volume by seek percent
     *
     * @param percent seek percent
     */
    private void updateVolume(float percent) {

        mCenterLayout.setVisibility(VISIBLE);

        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }

        int volume = (int) (percent * mMaxVolume) + mCurVolume;
        if (volume > mMaxVolume) {
            volume = mMaxVolume;
        }

        if (volume < 0) {
            volume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        int progress = volume * 100 / mMaxVolume;
        mCenterProgress.setProgress(progress);
    }

    /**
     * update brightness by seek percent
     *
     * @param percent seek percent
     */
    private void updateBrightness(float percent) {

        if (!(mContext instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) mContext;
        if (mCurBrightness == -1) {
            mCurBrightness = activity.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness <= 0.01f) {
                mCurBrightness = 0.01f;
            }
        }
        mCenterLayout.setVisibility(VISIBLE);
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness >= 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness <= 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        activity.getWindow().setAttributes(attributes);
        float p = attributes.screenBrightness * 100;
        mCenterProgress.setProgress((int) p);

    }


    /**
     * Interface of Media Controller View Which can be callBack
     * when {@link android.media.MediaPlayer} or some other media
     * players work
     */
    public interface MediaPlayerControl {
        /**
         * start play video
         */
        void start();

        /**
         * pause video
         */
        void pause();

        /**
         * get video total time
         *
         * @return total time
         */
        int getDuration();

        /**
         * get video current position
         *
         * @return current position
         */
        int getCurrentPosition();

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        void seekTo(int position);

        /**
         * video is playing state
         *
         * @return is video playing
         */
        boolean isPlaying();

        /**
         * get buffer percent
         *
         * @return percent
         */
        int getBufferPercentage();

        /**
         * if the video can pause
         *
         * @return can pause video
         */
        boolean canPause();

        /**
         * can seek video progress
         *
         * @return can seek video progress
         */
        boolean canSeek();

        /**
         * video is full screen
         * in order to control image src...
         *
         * @return fullScreen
         */
        boolean isFullScreen();

        /**
         * toggle fullScreen
         */
        void toggleFullScreen();

        /**
         * exit media player
         */
        void exit();

        /**
         * get top title name
         */
        String getTopTitle();
    }
}