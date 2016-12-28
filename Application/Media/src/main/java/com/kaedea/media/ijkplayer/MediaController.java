/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package com.kaedea.media.ijkplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.kaedea.media.R;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class MediaController extends FrameLayout {

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
    // top layout
    // private View mTopLayout;
    private View mBackButton;
    // private TextView mTitleText;
    // bottom layout
    // private View mBottomLayout;
    private View mPauseButton;
    private ImageView mPlayImageView;
    private ImageView mStatusView;
    private Handler mHandler = new ControllerHandler(this);
    private long mMediaDuration;
    // center layout
    private View mHintView;
    private TextView mHintTv1;
    private TextView mHintTv2;

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
            long pos;
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
        mRootView = inflate.inflate(R.layout.bili_app_layout_fans_player_controller, null);
        initControllerView();
        return mRootView;
    }

    /**
     * find all views inside {@link MediaController}
     * and init params
     */
    private void initControllerView() {
        // top layout
        // mTopLayout = mRootView.findViewById(R.id.layout_top);
        mBackButton = mRootView.findViewById(R.id.btn_back);
        if (mBackButton != null) {
            mBackButton.requestFocus();
            mBackButton.setOnClickListener(mBackListener);
        }
        // mTitleText = (TextView) mRootView.findViewById(R.id.top_title);

        // center layout
        mHintView = mRootView.findViewById(R.id.layout_hint);
        mHintTv1 = (TextView) mRootView.findViewById(R.id.tv_hint_1);
        mHintTv2 = (TextView) mRootView.findViewById(R.id.tv_hint_2);

        // bottom layout
        // mBottomLayout = mRootView.findViewById(R.id.layout_bottom);
        mPauseButton = mRootView.findViewById(R.id.bottom_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mPlayImageView = (ImageView) mRootView.findViewById(R.id.iv_play);
        mStatusView = (ImageView) mRootView.findViewById(R.id.iv_status);
        /*mFullscreenButton = (ImageButton) mRootView.findViewById(R.id.bottom_fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }*/
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

    public void resume() {
        notifyUiChanged();

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

    private String milisToTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
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
    private long updateSeekProgress() {
        if (mMediaPlayerControl == null || mIsDragging) {
            return 0;
        }
        long position = mMediaPlayerControl.getCurrentPosition();
        long duration = getDuration();
        // seekbar
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            /*//get buffer percentage
            int percent = mMediaPlayerControl.getBufferPercentage();
            //set buffer progress
            mSeekBar.setSecondaryProgress(percent * 10);*/
        }
        // play time
        if (mCurrentTime != null)
            mCurrentTime.setText(milisToTime(position));
        if (mEndTime != null)
            mEndTime.setText(milisToTime(duration));
        return position;
    }

    public void notifyUiChanged() {
        if (mRootView == null || mMediaPlayerControl == null) {
            return;
        }
        if (mMediaPlayerControl.isPlaying()) {
            mPlayImageView.setImageResource(R.drawable.ic_fans_player_stop);
            mStatusView.setImageLevel(1);
        } else {
            mPlayImageView.setImageResource(R.drawable.ic_fans_player_play);
            mStatusView.setImageLevel(0);
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
            mHintView.setVisibility(VISIBLE);
            mIsDragging = true;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mMediaPlayerControl == null) {
                return;
            }
            if (!fromuser) {
                return;
            }
            long duration = getDuration();
            long currentPosition = mMediaPlayerControl.getCurrentPosition();
            long newPosition = (duration * progress) / 1000L;
            if (mCurrentTime != null)
                mCurrentTime.setText(milisToTime((int) newPosition));
            if (mEndTime != null)
                mEndTime.setText(milisToTime(mMediaDuration));
            if (mHintTv1 != null) {
                mHintTv1.setText(milisToTime((int) newPosition) + "/" + milisToTime(mMediaDuration));
            }
            if (mHintTv2 != null) {
                int offset = (int) ((newPosition - currentPosition) / 1000);
                String text = String.valueOf(offset) + "秒";
                if (offset > 0) text = "+" + text;
                mHintTv2.setText(text);
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mIsDragging = false;
            show();
            mHintView.setVisibility(GONE);
            long duration = mMediaPlayerControl.getDuration();
            long newPosition = (duration * bar.getProgress()) / 1000L;
            mMediaPlayerControl.seekTo((int) newPosition);
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

    public void onBufferingUpdate(int percent) {
        if (mSeekBar != null) {
            mSeekBar.setSecondaryProgress((int) ((percent / 100F) * 1000L));
        }
    }

    private long getDuration() {
        mMediaDuration = mMediaPlayerControl.getDuration() > 0 ?
                mMediaPlayerControl.getDuration() : mMediaDuration;
        return mMediaDuration;
    }


    /**
     * set top back click listener
     */
    private OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControl.exit();
        }
    };


    /**
     * set pause click listener
     */
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
        }
    };

    /**
     * set full screen click listener
     */
    private OnClickListener mFullscreenListener = new OnClickListener() {
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
        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        //remove all before add view
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
        //add controller view to bottom of the AnchorView
        LayoutParams tlp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mAnchorView.addView(this, tlp);
        this.setVisibility(GONE);
    }

    private void seekBackWard() {
        if (mMediaPlayerControl == null) {
            return;
        }

        long pos = mMediaPlayerControl.getCurrentPosition();
        pos -= PROGRESS_SEEK;
        mMediaPlayerControl.seekTo(pos);
        show();
    }

    private void seekForWard() {
        if (mMediaPlayerControl == null) {
            return;
        }
        long pos = mMediaPlayerControl.getCurrentPosition();
        pos += PROGRESS_SEEK;
        mMediaPlayerControl.seekTo(pos);
        show();
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
        long getDuration();

        /**
         * get video current position
         *
         * @return current position
         */
        long getCurrentPosition();

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        void seekTo(long position);

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
//        int getBufferPercentage();

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