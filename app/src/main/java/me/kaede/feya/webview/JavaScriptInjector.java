/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.webview;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kaede
 * @version date 16/8/23
 */
public class JavaScriptInjector {

    public static final String TAG = WebActivity.TAG;

    public static void injectJS(WebView webView, String js) {
        if (webView == null || TextUtils.isEmpty(js)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.v(TAG, "[onReceiveValue] value = " + String.valueOf(value));
                }
            });
        } else {
            webView.loadUrl("javascript:" + js);
        }
    }

    /**
     * load js form java code
     */
    public static String getMonitorScript() {
        StringBuilder builder = new StringBuilder();
        builder.append("window.addEventListener('DOMContentLoaded',");
        builder.append("function() {");
        builder.append("    prompt('domc:' + new Date().getTime());");
        builder.append("})");
        return builder.toString();
    }

    /**
     * load js form assets
     */
    public static String getMonitorScript(Context context) {
        InputStream inputStream = null;
        String encoded = null;
        try {
            inputStream = context.getAssets().open("monitor.js");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            encoded = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        Log.v(TAG, "[getMonitorScript] js = " + String.valueOf(encoded));
        return encoded;
    }
}
