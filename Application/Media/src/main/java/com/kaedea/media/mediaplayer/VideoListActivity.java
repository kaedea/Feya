/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaedea.media.R;
import com.kaedea.media.home.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_ACTIVITY = "EXTRA_TARGET_ACTIVITY";
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
        loadDatas();
    }

    private void loadDatas() {
        AsyncTaskCompat.executeParallel(new LoadTask());
    }

    class LoadTask extends AsyncTask<Void, Void, Void> {
        private List<VideoEntry> mDatas;

        @Override
        protected Void doInBackground(Void... voids) {
            mDatas = new ArrayList<>();
            VideoEntry videoEntry = new VideoEntry();
            videoEntry.name = "Buck Bunny";
            videoEntry.uri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
            mDatas.add(videoEntry);
            File baseDir = getApplicationContext().getExternalFilesDir("Movies");
            if (baseDir != null && baseDir.exists()) {
                File[] files = baseDir.listFiles();
                for (File item : files) {
                    if (item.getAbsolutePath().endsWith("mp4")) {
                        videoEntry = new VideoEntry();
                        videoEntry.name = item.getName();
                        videoEntry.uri = item.getAbsolutePath();
                        this.mDatas.add(videoEntry);
                    }
                }
            }
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
