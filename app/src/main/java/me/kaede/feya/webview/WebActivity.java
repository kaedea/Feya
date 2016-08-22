package me.kaede.feya.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.kaede.feya.BaseActivity;
import me.kaede.feya.BuildConfig;
import me.kaede.feya.R;

public class WebActivity extends BaseActivity {
    static final String TAG = "WebActivity";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private Toolbar toolbar;
    private WebView webView;
    private JavaScriptBridge mJsbApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        webView = (WebView) findViewById(R.id.webview);
        prepareWebView();
        webView.loadUrl("http://www.bilibili.com/html/2233birthday-test-m.html");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            webView.loadUrl("");
        }
    }

    @Override
    protected void onDestroy() {
        if (mJsbApp != null) {
            mJsbApp.onActivityDestoryed();
            mJsbApp = null;
        }

        super.onDestroy();
        webView.destroy();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    protected void prepareWebView() {
        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);
        String originalUA = settings.getUserAgentString();
        settings.setUserAgentString(originalUA + " FeyaApp/" + BuildConfig.VERSION_CODE);
        if (DEBUG) settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (DEBUG) {
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                settings.setDatabasePath("/data/data/" + getPackageName() + "/databases/");
            }
            mJsbApp = createAppMainJavaScriptBridge();
            if (mJsbApp != null) {
                webView.removeJavascriptInterface("FeyaApp");
                webView.addJavascriptInterface(mJsbApp, "FeyaApp");
            }
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(DEBUG || BuildConfig.DEBUG);
        }
        webView.setWebChromeClient(createWebChromeClient());
        webView.setWebViewClient(createWebViewClient());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.removeJavascriptInterface("searchBoxJavaBridge_");
            webView.removeJavascriptInterface("accessibility");
            webView.removeJavascriptInterface("accessibilityTraversal");
        }
    }

    @Nullable
    protected JavaScriptBridge createAppMainJavaScriptBridge() {
        return new JavaScriptBridge(this);
    }

    static final int REQUEST_SELECT_FILE = 0xff;

    protected WebChromeClient createWebChromeClient() {
        return new WebChromeClient() {

            @Override
            protected boolean onShowFileChooser(Intent intent) {
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                    return true;
                } catch (ActivityNotFoundException e) {
                    return false;
                }
            }

            @NonNull
            @Override
            protected Context getContext() {
                return getApplicationContext();
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            }
        };
    }

    private WebViewClient createWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                Uri parsedUri = Uri.parse(url);
                if (url.startsWith("feya://")) {
                    intent = new Intent(Intent.ACTION_VIEW, parsedUri);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setPackage(getApplicationContext().getPackageName());
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                } else if (url.startsWith("mailto:")) {
                    intent.setData(parsedUri);
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("tel:")) {
                    return loadPhoneCallUrl(url);
                } else {
                    return loadHttpScheme(parsedUri) || onOverrideUrlLoading(view, parsedUri);
                }
            }

            private boolean loadPhoneCallUrl(String url) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {

                }
                return false;
            }

            private boolean loadHttpScheme(Uri parsedUri) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        };
    }

    protected boolean onOverrideUrlLoading(WebView view, Uri uri) {
        return false;
    }
}
