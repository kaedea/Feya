package akatuki.kaede.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import akatuki.kaede.utils.KaedeUtil;
import akatuki.kaede.utils.KaedeUtil.StorageModel;
import akatuki.kaede.utils.LogUtil;
import akatuki.kaede.utils.sub.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author chris.xie
 * @github https://github.com/kidhaibara/Akatuki-Kaede-Utils-sub.git
 */
@SuppressLint("NewApi")
public class TestListActivity extends Activity implements OnItemClickListener {
	final public static String TAG = "AK_TestListActivity";

	ListView mListView;
	private String[] items = { "Function Test List", "系统文件夹路径", "全部扩展卡", "下载URL文件到本地", "拷贝并安装Asset里的APK文件", "GET HTTPS HTML", "Web Content", "Log Dialog", "动态加载jar", "动态加载apk（未安装）", "动态加载apk（已安装）" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_list);
		findView();
		init();
	}

	public void findView() {
		mListView = (ListView) this.findViewById(R.id.testList_listView);
	}

	public void init() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		mListView.setAdapter(adapter);
		// 为listView增加监听事件
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
		case 1:
			Log.e(TAG, "getSdPath: " + KaedeUtil.getSdPath());
			Log.e(TAG, "getDataDirectory: " + Environment.getDataDirectory().getPath());
			Log.e(TAG, "getDownloadCacheDirectory: " + Environment.getDownloadCacheDirectory().getPath());
			Log.e(TAG, "getRootDirectory: " + Environment.getRootDirectory().getPath());
			break;
		case 2:
			try {
				ArrayList<StorageModel> list_StorageModel = KaedeUtil.getSdPathMutil(this);
				for (StorageModel storageModel : list_StorageModel) {
					Log.e(TAG, storageModel.toString());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case 3:
			new NetTask_Image().execute();
			break;
		case 4:
			if (KaedeUtil.copyFileFromAssetsToSd(this, "akutils_sub.apk", Environment.getExternalStorageDirectory().getAbsolutePath() + "/akutils_sub.apk")) {
				String title = "没有安装APK,是否安装?";
				if (KaedeUtil.isApkInstalled(this, "akatuki.kaede.utils.sub")) {
					title = "已经安装APK,是否覆盖?";
				}
				Builder mBuilder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setMessage(title).setIcon(R.drawable.android).setPositiveButton("yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/akutils_sub.apk"), "application/vnd.android.package-archive");
						TestListActivity.this.startActivity(intent);
					}
				});
				mBuilder.show();
			}

			break;
		case 5:
			new NetTask_HTTPS().execute();
			break;
		case 6:
			startActivity(new Intent(TestListActivity.this, WebContentActivity.class));
			break;

		case 7:
			// LogUtil.LogDialog_addTag(TAG);
			// LogUtil.LogDialog_add(getResources().getString(R.string.dialog_log));
			// LogUtil.LogDialog_show(this);
			LogUtil.LogDialog_log(this, TAG, getResources().getString(R.string.dialog_log));

			break;
		case 8:
			if (KaedeUtil.copyFileFromAssetsToSd(this, "test_dexloader.jar", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_dexloader.jar")) {
				Log.e(TAG, "成功复制jar到SD卡");

				final File optimizedDexOutputPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_dexloader.jar");

				// 无法直接从SD卡加载.dex文件，需要指定APP缓存目录（.dex文件会被解压到此目录）
				File dexOutputDir = this.getDir("dex", 0);
				Log.e(TAG, "dexOutputDir:" + dexOutputDir.getAbsolutePath());
				DexClassLoader cl = new DexClassLoader(optimizedDexOutputPath.getAbsolutePath(), dexOutputDir.getAbsolutePath(), null, getClassLoader());
				Class libProviderClazz = null;

				try {
					libProviderClazz = cl.loadClass("me.kaede.dexclassloader.MyLoader");

					// 遍历类里所有方法
					Method[] methods = libProviderClazz.getDeclaredMethods();
					for (int i = 0; i < methods.length; i++) {
						Log.e(TAG, methods[i].toString());
					}

					Method start = libProviderClazz.getDeclaredMethod("func");// 获取方法
					start.setAccessible(true);// 未加这句之前报了一个错误：access to method
												// denied 加上之后可以了。
					String string = (String) start.invoke(libProviderClazz.newInstance());// 调用方法
					Log.e(TAG, string);

					Toast.makeText(this, string, Toast.LENGTH_LONG).show();

				} catch (Exception exception) {
					// Handle exception gracefully here.
					exception.printStackTrace();
				}

			}
			break;
		case 9:
			if (KaedeUtil.copyFileFromAssetsToSd(this, "test_DexClassLoader.apk", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_DexClassLoader.apk")) {
				Log.e(TAG, "成功复制apk到SD卡");
				String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_DexClassLoader.apk";
				try {

					// 4.1以后不能够将optimizedDirectory设置到sd卡目录， 否则抛出异常.
					File optimizedDirectoryFile = getDir("dex", 0);
					DexClassLoader classLoader = new DexClassLoader(path, optimizedDirectoryFile.getAbsolutePath(), null, getClassLoader());

					// 通过反射机制调用
					Class mLoadClass = classLoader.loadClass("me.kaede.dexclassloader.MainActivity");
					Constructor constructor = mLoadClass.getConstructor(new Class[] {});
					Object testActivity = constructor.newInstance(new Object[] {});

					// 遍历类里所有方法
					Method[] methods = mLoadClass.getDeclaredMethods();
					for (int i = 0; i < methods.length; i++) {
						Log.e(TAG, methods[i].toString());
					}

					// 获取sayHello方法
					Method method = mLoadClass.getMethod("func");
					method.setAccessible(true);
					Object content = method.invoke(testActivity);
					Toast.makeText(this, content.toString(), Toast.LENGTH_LONG).show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			break;

		case 10:
			if (KaedeUtil.copyFileFromAssetsToSd(this, "test_DexClassLoader.apk", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_DexClassLoader.apk")) {
				Log.e(TAG, "成功复制apk到SD卡");
				String title = "没有安装APK,是否安装?";
				if (!KaedeUtil.isApkInstalled(this, "me.kaede.dexclassloader")) {
					Builder mBuilder = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setMessage(title).setIcon(R.drawable.android).setPositiveButton("yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_DexClassLoader.apk"), "application/vnd.android.package-archive");
							TestListActivity.this.startActivity(intent);
						}
					});
					mBuilder.show();
				}

				try {
					String pkgName = "me.kaede.dexclassloader";
					Context context = createPackageContext(pkgName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);

					// 获取动态加载得到的资源
					Resources resources = context.getResources();
					// 过去该apk中的字符串资源"app_name"， 并且toast出来，apk换肤的实现就是这种原理
					String toast = resources.getString(resources.getIdentifier("app_name", "string", pkgName));
					Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

					Class cls = context.getClassLoader().loadClass(pkgName + ".MainActivity");
					// 跳转到该Activity
					startActivity(new Intent(context, cls));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			break;

		case 0:
			break;
		default:
			break;
		}

	}

	public class NetTask_Image extends AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			try {
				return KaedeUtil.downloadImageToSd("https://lh6.googleusercontent.com/-55osAWw3x0Q/URquUtcFr5I/AAAAAAAAAbs/rWlj1RUKrYI/s160-c/A%252520Photographer.jpg", KaedeUtil.getSdPath() + File.separator + "downloadImageToSd.jpg");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (result) {
				Log.e(TAG, "下载成功");
			} else
				Log.e(TAG, "下载失败");
			super.onPostExecute(result);
		}

	}

	public class NetTask_HTTPS extends AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			try {
				String html = KaedeUtil.getHtmlHttps("https://yande.re/post");
				Log.e(TAG, "GET HTTPS HTML");
				Log.e(TAG, html);
				FileOutputStream mFileOutputStream = new FileOutputStream(KaedeUtil.getSdPath() + File.separator + "yandere.html");
				mFileOutputStream.write(html.getBytes());
				mFileOutputStream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 加入横屏要处理的代码
			Log.e(TAG, "屏幕变化 横屏");
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// 加入竖屏要处理的代码
			Log.e(TAG, "屏幕变化 竖屏");
		}
	}

}
