package com.kaedea.media.videoview;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import com.kaedea.media.R;

public class VideoViewActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    public static final String EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI";

    private String mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUri = extras.getString(EXTRA_VIDEO_URI);
        }
        VideoView videoView = (VideoView) this.findViewById(R.id.video_view);
        videoView.setMediaController(new MediaController(this));
        videoView.setOnPreparedListener(this);
        if (!TextUtils.isEmpty(mUri)) {
            videoView.setVideoURI(Uri.parse(mUri));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
