/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package com.kaedea.media.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.kaedea.media.R;
import com.kaedea.media.camera.CameraPreview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Actividad principal para el control de la camara
 *
 * @author anthorlop
 */
public class CameraRecorderActivity extends Activity {

    public static final String TAG = "CameraActivity";
    public static final int FOCUS_AREA_SIZE = 500;
    public static int MAX_DURATION_RECORD = 600000;
    public static int MAX_FILE_SIZE_RECORD = 50000000;
    private boolean cameraFront = false;
    private boolean flash = false;

    private Context context;
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private String outputFile;
    private long countUp;
    private int mCameraId = -1;
    private int quality = CamcorderProfile.QUALITY_480P;
    private int videoBitRate = 500 * 1000;
    boolean recording = false;

    @BindView(R.id.button_capture) Button btnCapture;
    @BindView(R.id.button_ChangeCamera) Button btnSwitch;
    @BindView(R.id.buttonFlash) Button btnFlash;
    @BindView(R.id.camera_preview) LinearLayout cameraPreview;
    @BindView(R.id.buttonQuality) Button btnQuality;
    @BindView(R.id.buttonBitrate) Button btnVideoBitRate;
    @BindView(R.id.listOfQualities) ListView listConfig;
    @BindView(R.id.chronoRecordingImage) View chronoRecordingImage;
    @BindView(R.id.textChrono) Chronometer chronometer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_recorder);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context = this;
        ButterKnife.bind(this);
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasCamera(context)) {
            Toast toast = Toast.makeText(context, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            releaseCamera();
            boolean frontal = cameraFront;
            // if the front facing camera does not exist
            mCameraId = findFrontFacingCamera();
            if (mCameraId < 0) {
                // desactivar el cambio de camara
                mSwitchCameraListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(CameraRecorderActivity.this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                    }
                };
                // seleccionar la camara trasera
                mCameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            } else if (!frontal) {
                // seleccionar la camara trasera sin desactivar la delantera
                mCameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
            mCamera = Camera.open(mCameraId);
            mPreview.refreshCamera(mCamera);
            reloadQualities(mCameraId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initialize() {
        mPreview = new CameraPreview(context, mCamera);
        cameraPreview.addView(mPreview);
        btnCapture.setOnClickListener(mCaptureListener);
        btnSwitch.setOnClickListener(mSwitchCameraListener);
        btnQuality.setOnClickListener(mQualityListener);
        btnVideoBitRate.setOnClickListener(mBitrateListener);
        btnFlash.setOnClickListener(mFlashListener);
        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        focusOnTouch(event);
                    } catch (Exception e) {
                        Log.i(TAG, "Fail when camera try autofocus");
                    }
                }
                return true;
            }
        });
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void switchCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
                reloadQualities(cameraId);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                // al poner la camara frontal se desactiva el flash
                if (flash) {
                    flash = false;
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
                reloadQualities(cameraId);
            }
        }
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void setFlashMode(String mode) {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)
                    && mCamera != null
                    && !cameraFront) {
                mPreview.setFlashMode(mode);
                mPreview.refreshCamera(mCamera);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception changing flashLight mode",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void reloadBitRates() {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);
        videoBitRate = prefs.getInt("BITRATE", 500 * 1000);
        changeBitRate(videoBitRate);
        final ArrayList<String> list = new ArrayList<String>();
        list.add("300000");
        list.add("500000");
        list.add("700000");
        list.add("1000000");
        list.add("2000000");
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listConfig.setAdapter(adapter);
        listConfig.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                listConfig.setVisibility(View.GONE);
                changeBitRate(Integer.valueOf(item));
            }

        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void reloadQualities(int cameraId) {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);
        quality = prefs.getInt("QUALITY", CamcorderProfile.QUALITY_480P);
        final ArrayList<String> list = new ArrayList<String>();
        int maxQualitySupported = CamcorderProfile.QUALITY_480P;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
                list.add("QVGA");
                maxQualitySupported = CamcorderProfile.QUALITY_QVGA;
            }
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
            list.add("480p");
            maxQualitySupported = CamcorderProfile.QUALITY_480P;
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
            list.add("720p");
            maxQualitySupported = CamcorderProfile.QUALITY_720P;
        }
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
            list.add("1080p");
            maxQualitySupported = CamcorderProfile.QUALITY_1080P;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) {
                list.add("2160p");
            }
        }
        if (!CamcorderProfile.hasProfile(cameraId, quality)) {
            quality = maxQualitySupported;
        }
        changeVideoQuality(quality);
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listConfig.setAdapter(adapter);
        listConfig.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                btnQuality.setText(item);
                if (item.equals("480p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_480P);
                } else if (item.equals("720p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_720P);
                } else if (item.equals("1080p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_1080P);
                } else if (item.equals("2160p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_2160P);
                } else if (item.equals("QVGA")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_QVGA);
                }
                listConfig.setVisibility(View.GONE);
            }

        });
    }

    private void changeRequestedOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }

    private void changeBitRate(int bitrate) {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("BITRATE", bitrate);
        editor.commit();
        this.videoBitRate = bitrate;
        btnVideoBitRate.setText(String.valueOf(bitrate));
    }

    private void changeVideoQuality(int quality) {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("QUALITY", quality);
        editor.commit();
        this.quality = quality;
        updateButtonText(quality);
    }

    private void updateButtonText(int quality) {
        if (quality == CamcorderProfile.QUALITY_480P)
            btnQuality.setText("480p");
        if (quality == CamcorderProfile.QUALITY_720P)
            btnQuality.setText("720p");
        if (quality == CamcorderProfile.QUALITY_1080P)
            btnQuality.setText("1080p");
        if (quality == CamcorderProfile.QUALITY_2160P)
            btnQuality.setText("2160p");
        if (quality == CamcorderProfile.QUALITY_QVGA)
            btnQuality.setText("QVGA");
    }

    private void startChronometer() {
        chronometer.setVisibility(View.VISIBLE);
        final long startTime = SystemClock.elapsedRealtime();
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer arg0) {
                countUp = (SystemClock.elapsedRealtime() - startTime) / 1000;
                if (countUp % 2 == 0) {
                    chronoRecordingImage.setVisibility(View.VISIBLE);
                } else {
                    chronoRecordingImage.setVisibility(View.INVISIBLE);
                }

                String asText = String.format("%02d", countUp / 60) + ":" + String.format("%02d", countUp % 60);
                chronometer.setText(asText);
            }
        });
        chronometer.start();
    }

    private void stopChronometer() {
        chronometer.stop();
        chronoRecordingImage.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);
    }

    public void reset() {
        flash = false;
        cameraFront = false;
    }


    private boolean prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (cameraFront) {
                mediaRecorder.setOrientationHint(270);
            } else {
                mediaRecorder.setOrientationHint(90);
            }
        }
        CamcorderProfile profile = CamcorderProfile.get(quality);
        /*try {
            Method method = MediaRecorder.class.getDeclaredMethod("setParameter", String.class);
            method.setAccessible(true);
            method.invoke(mediaRecorder, "video-param-encoder-profile=8");
            method.invoke(mediaRecorder, "video-param-encoder-level=512");
            method.invoke(mediaRecorder, "video-param-i-frames-interval=100000");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }*/
        profile.videoBitRate = videoBitRate;
        profile.videoFrameRate = 24;
        mediaRecorder.setProfile(profile);
        Date d = new Date();
        String timestamp = String.valueOf(d.getTime());
        String orient = "landscape";
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orient = "portrait";
        }
        outputFile = Environment.getExternalStorageDirectory().getPath() + "/Movies/preview_"
                + profile.videoFrameWidth + "x" + profile.videoFrameHeight
                + "_" + profile.videoFrameRate
                + "_" + profile.videoBitRate
                + "_" + orient + "_" + timestamp + ".mp4";
        new File(Environment.getExternalStorageDirectory().getPath() + "/Movies").mkdir();
        mediaRecorder.setOutputFile(outputFile);
        mediaRecorder.setMaxDuration(MAX_DURATION_RECORD); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(MAX_FILE_SIZE_RECORD); // Set max file size 50M
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (recording) {
            // stop recording and release camera
            mediaRecorder.stop(); // stop the recording
            if (chronometer != null && chronometer.isActivated())
                chronometer.stop();
            releaseMediaRecorder(); // release the MediaRecorder object
            // Toast.makeText(CameraRecorderActivity.this, "La grabación se ha detenido", Toast.LENGTH_LONG).show();
            recording = false;
            File mp4 = new File(outputFile);
            if (mp4.exists() && mp4.isFile()) {
                mp4.delete();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Log.i(TAG, "fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY());
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");
            }
        }
    };

    View.OnClickListener mQualityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                listConfig.setVisibility(View.VISIBLE);
                reloadQualities(mCameraId);
            }
        }
    };

    View.OnClickListener mBitrateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                listConfig.setVisibility(View.VISIBLE);
                reloadBitRates();
            }
        }
    };

    View.OnClickListener mFlashListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording && !cameraFront) {
                if (flash) {
                    flash = false;
                    btnFlash.setText("FLASH OFF");
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    flash = true;
                    btnFlash.setText("FLASH ON");
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
        }
    };

    View.OnClickListener mSwitchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the back and vice versa
                    releaseCamera();
                    switchCamera();
                } else {
                    Toast toast = Toast.makeText(context, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    View.OnClickListener mCaptureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (recording) {
                // stop recording and release camera
                mediaRecorder.stop(); // stop the recording
                stopChronometer();
                btnCapture.setText("RECORD");
                changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                releaseMediaRecorder(); // release the MediaRecorder object
                // Toast.makeText(CameraRecorderActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
                recording = false;
            } else {
                if (!prepareMediaRecorder()) {
                    // Toast.makeText(CameraRecorderActivity.this, "Fail in MediaRecorder#prepareMediaRecorder()!", Toast.LENGTH_LONG).show();
                    finish();
                }
                // work on UiThread for better performance
                runOnUiThread(new Runnable() {
                    public void run() {
                        // If there are stories, add them to the table
                        try {
                            mediaRecorder.start();
                            startChronometer();
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            } else {
                                changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            }
                            btnCapture.setText("STOP");
                        } catch (final Exception ex) {
                            // Log.i("---","Exception in thread");
                        }
                    }
                });
                recording = true;
            }
        }
    };

    public class StableArrayAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}

