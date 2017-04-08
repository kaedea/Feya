/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package me.kaede.feya.webview;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;

/**
 * copycat
 * Export some methods for opening file-chooser which are hidden in {@link android.webkit.WebChromeClient}
 *
 * @author Kaede
 * @since date 16/8/22
 */
public abstract class WebChromeClient extends android.webkit.WebChromeClient {
    private ValueCallback mFileUploadCallback = null;

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        resetFileCallback();
        mFileUploadCallback = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intentToFileChooser(Intent.createChooser(intent, "选择文件"));
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        resetFileCallback();
        if (acceptType == null || !acceptType.contains("image")) {
            uploadMsg.onReceiveValue(null);
            return;
        }
        mFileUploadCallback = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intentToFileChooser(Intent.createChooser(intent, "选择文件"));
    }

    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        resetFileCallback();
        if (acceptType == null || !acceptType.contains("image")) {
            uploadMsg.onReceiveValue(null);
            return;
        }
        mFileUploadCallback = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intentToFileChooser(Intent.createChooser(intent, "选择文件"));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        resetFileCallback();
        mFileUploadCallback = filePathCallback;
        Intent intent = fileChooserParams.createIntent();
        return intentToFileChooser(intent);
    }

    private boolean intentToFileChooser(Intent intent) {
        try {
            return onShowFileChooser(intent);
        } catch (Exception e) {
            Toast.makeText(getContext().getApplicationContext(), "选择文件出错", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * @param intent the intent to file chooser.
     * @return true if file chooser opening, false otherwise.
     */
    protected abstract boolean onShowFileChooser(Intent intent);


    private void resetFileCallback() {
        if (mFileUploadCallback != null) {
            mFileUploadCallback.onReceiveValue(null);
            mFileUploadCallback = null;
        }
    }

    public void onReceiveFile(int resultCode, Intent data) {
        if (mFileUploadCallback == null) return;
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null)
            mFileUploadCallback.onReceiveValue(null);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mFileUploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
        else
            mFileUploadCallback.onReceiveValue(getImageFileUri(data.getData()));
        mFileUploadCallback = null;
    }

    private Uri getImageFileUri(Uri data) {
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(data, columns, null, null, null);
        if (cursor != null) {
            String picPath;
            try {
                picPath = null;
                if (cursor.moveToFirst()) {
                    picPath = cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
            if (picPath != null // upload image only
                    && (picPath.endsWith(".png") || picPath.endsWith(".PNG")
                    || picPath.endsWith(".jpg") || picPath.endsWith(".JPG")
                    || picPath.endsWith(".gif") || picPath.endsWith(".GIF")
                    || picPath.endsWith(".jpeg") || picPath.endsWith(".JPEG"))) {
                File file = new File(picPath);
                if (file.exists())
                    return Uri.fromFile(file);
            }
        }
        return null;
    }

    @NonNull
    protected abstract Context getContext();
}