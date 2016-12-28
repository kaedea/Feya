/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.webview;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

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
            inputStream = context.getAssets().open("h5_monitor.js");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            encoded = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        Log.v(TAG, "[getMonitorScript] js = " + String.valueOf(encoded));
        return encoded;
    }

    public static void injectJs(WebView webView) {
        new AysncInject(webView).start();
    }

    private static class AysncInject extends Thread {
        WeakReference<WebView> view;
        Context context;

        public AysncInject(WebView view) {
            this.view = new WeakReference<WebView>(view);
            this.context = view.getContext().getApplicationContext();
        }

        @Override
        public void run() {
            final String js = getMonitorScript(context);
            if (!TextUtils.isEmpty(js)) {
                final WebView webView = view.get();
                if (webView != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            injectJS(webView, js);
                        }
                    });
                }
            }
        }
    }
}
