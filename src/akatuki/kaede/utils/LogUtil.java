package akatuki.kaede.utils;

import akatuki.kaede.utils.sub.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LogUtil {
	public static String LOG = "";
	
	public static void LogDialog_log(Context context,String tag,String log)
	{
		LogDialog_addTag(tag);
		LogDialog_add(log);
		LogDialog_show(context);
	}

	public static void LogDialog_add(String log) {
		if (!LOG.equals("")) {
			LOG += "\n";
		}
		LOG += log;
	}
	public static void LogDialog_addTag(String tag) {
		if (!LOG.equals("")) {
			LOG += "\n";
		}
		tag="---------- "+  tag  +" ----------";
		LOG += tag;
	}
	

	@SuppressLint("NewApi") public static void LogDialog_show(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater factory = LayoutInflater.from(context);
		final View textEntryView = factory.inflate(R.layout.dialog_log, null);
		builder.setIcon(R.drawable.ic_launcher);
		// builder.setTitle("自定义输入框");
		builder.setView(textEntryView);
		TextView txtLog = (TextView) textEntryView.findViewById(R.id.dialog_txt_log);
		txtLog.setMovementMethod(ScrollingMovementMethod.getInstance());
		txtLog.setText(LOG);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				LOG = "";
			}
		});
		builder.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				LOG = "";				
			}
		});
		builder.create().show();
	}

}
