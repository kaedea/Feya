/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.webview;

import android.app.Activity;
import android.webkit.JavascriptInterface;

/**
 * @author kaede
 * @version date 16/8/22
 */
public class JavaScriptBridge {
    Activity mActivity;

    public JavaScriptBridge(Activity activity) {
        mActivity = activity;
    }

    @JavascriptInterface
    public void closeBrowser() {
        if (mActivity != null) mActivity.finish();
    }

    @JavascriptInterface
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void onActivityDestoryed() {
        mActivity = null;
    }
}
