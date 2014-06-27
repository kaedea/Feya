package akatuki.kaede.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import akatuki.kaede.utils.KaedeUtil;
import akatuki.kaede.utils.sub.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("JavascriptInterface") public class WebContentActivity extends Activity {
	WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_web_content);

		webView = (WebView) this.findViewById(R.id.webcontent_webview);
		webView.getSettings().setJavaScriptEnabled(true);// 设置支持Javascript
		webView.addJavascriptInterface(new Handler(), "handler");
		webView.requestFocus();// 触摸焦点起作用
		webView.loadUrl("http://www.acfun.com/a/ac1197767");
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				// 通过内部类定义的方法获取html页面加载的内容，这个需要添加在webview加载完成后的回调中
				Log.e("webcontent",  "onPageFinished");
				view.loadUrl("javascript:window.handler.show(document.body.innerHTML);");
				super.onPageFinished(view, url);
			}					
		}
		);
		

	}
	
	
	class Handler {
		public void show(String data) {  // 这里的data就webview加载的内容，即使页面跳转页都可以获取到，这样就可以做自己的处理了 
			Log.e("webcontent",  "call html");
        	FileOutputStream mFileOutputStream;
			try {
				mFileOutputStream = new FileOutputStream(KaedeUtil.getSdPath()+File.separator+"acfun.html");
				mFileOutputStream.write(data.getBytes());
				mFileOutputStream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	final class InJavaScriptLocalObj {
        public void showSource(String html) {
        	Log.e("webcontent",  "call html");
        	FileOutputStream mFileOutputStream;
			try {
				mFileOutputStream = new FileOutputStream(KaedeUtil.getSdPath()+File.separator+"acfun.html");
				mFileOutputStream.write(html.getBytes());
				mFileOutputStream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
        }
    }

}
