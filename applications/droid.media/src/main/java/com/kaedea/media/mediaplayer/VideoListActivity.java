/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.media.mediaplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.config.BoxingConfig.Mode;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing_impl.ui.BoxingActivity;
import com.kaedea.media.R;
import com.kaedea.media.home.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_ACTIVITY = "EXTRA_TARGET_ACTIVITY";
    public static final int REQ_MEDIA_VIDEO = 2233;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List<VideoEntry> mDatas;
    int mSurfaceMode = MediaPlayerActivity.SURFACE_SURFACEVIEW;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Video List");
        mRecyclerView = (RecyclerView) this.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mDatas = new ArrayList<>();
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSurfaceMode = extras.getInt(MediaPlayerActivity.EXTRA_SURFACE_MODE);
            className = extras.getString(EXTRA_TARGET_ACTIVITY);
        }

        // Load datas.
        loadDatas();

    }

    private void loadDatas() {
        AsyncTaskCompat.executeParallel(new LoadTask());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_pick:
                Boxing.of(new BoxingConfig(Mode.VIDEO))
                        .withIntent(this, BoxingActivity.class)
                        .start(this, REQ_MEDIA_VIDEO);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_MEDIA_VIDEO) {
            List<BaseMedia> medias = Boxing.getResult(data);
            // avoid null
            if (medias != null) {
                for (BaseMedia item : medias) {
                    VideoEntry videoEntry = new VideoEntry();
                    videoEntry.name = new File(item.getPath()).getName();
                    videoEntry.uri = item.getPath();
                    this.mDatas.add(videoEntry);
                }
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    class LoadTask extends AsyncTask<Void, Void, Void> {

        private List<VideoEntry> mDatas;

        @Override
        protected Void doInBackground(Void... voids) {
            this.mDatas = new ArrayList<>();
            VideoEntry videoEntry = new VideoEntry();
            videoEntry.name = "Buck Bunny (YouTube)";
            videoEntry.uri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
            this.mDatas.add(videoEntry);
            videoEntry = new VideoEntry();
            videoEntry.name = "Yannc";
            videoEntry.uri = "http://7u2jfl.com1.z0.glb.clouddn.com/705a394902d7d7acf67af917087e42bb_SD_0.mp4";
            this.mDatas.add(videoEntry);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            VideoListActivity.this.mDatas = this.mDatas;
            mAdapter.notifyDataSetChanged();
        }

    }

    public class VideoEntry {
        public String name;
        public String uri;
    }

    class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_two_line, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public int position;
        public TextView tvTitle;
        public TextView tvSubTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvSubTitle = (TextView) itemView.findViewById(R.id.tv_subtitle);
        }

        public void bind(int position) {
            this.position = position;
            VideoEntry item = mDatas.get(position);
            tvTitle.setText(item.name == null ? "" : item.name);
            tvSubTitle.setText(item.uri == null ? "" : item.uri);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (TextUtils.isEmpty(className)) return;
            Intent intent = new Intent();
            intent.setClassName(itemView.getContext(), className);
            intent.putExtra(MediaPlayerActivity.EXTRA_SURFACE_MODE, mSurfaceMode);
            intent.putExtra(MediaPlayerActivity.EXTRA_VIDEO_URI, mDatas.get(position).uri);
            itemView.getContext().startActivity(intent);
        }
    }
}
