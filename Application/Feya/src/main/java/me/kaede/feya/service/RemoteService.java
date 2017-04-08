/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import me.kaede.feya.InternalUtils;
import me.kaede.feya.MainActivity;
import me.kaede.feya.R;

/**
 * service run in remote process
 */
public class RemoteService extends Service {
    public static final String TAG = ServiceDemoActivity.TAG;

    private Handler mHandler;
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 10086;

    /**
     * using aidl for IRemoteService
     * see {@link IRemoteService}
     */
    private IBinder mBinder = new IRemoteService.Stub() {
        @Override
        public void toast(final String msg) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    RemoteService.this.toast(msg);
                }
            });
        }

        @Override
        public String talk(String msg) throws RemoteException {
            return "reply " + msg;
        }
    };

    @Override
    public void onCreate() {
        Log.i(TAG, "[onCreate]");
        toast("onCreate");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "[onStart]remote service start id = " + startId + " : " + intent);
        toast("onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "[onStartCommand]remote received flags " + flags + ", start id = " + startId + " : " + intent);
        toast("onStartCommand startId = " + startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[onDestroy]");
        toast("onDestroy");

        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[onBind]");
        toast("onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "[onUnbind]");
        toast("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "[onRebind]");
        toast("onRebind");
        super.onRebind(intent);
    }

    public void toast(String msg) {
        InternalUtils.toast(this, msg);
    }

    /**
     * Show a notification while this service is running.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Service started";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Remote Service")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}
