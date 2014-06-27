package akatuki.kaede.activity;

import akatuki.kaede.utils.sub.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
	Boolean isOk=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sdksplash);
		new ThreadSplash().start();
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isOk=false;
		finish();
		super.onPause();
	}



	class ThreadSplash extends Thread
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isOk) {
				//SDKSplashActivity.this.startActivity(new Intent(SDKSplashActivity.this,MainActivity.class));
				finish();
			}
			super.run();
		}

	}



}
