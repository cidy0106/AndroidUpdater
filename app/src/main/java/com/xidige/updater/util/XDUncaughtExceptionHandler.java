package com.xidige.updater.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import com.xidige.updater.R;

import android.content.Context;
import android.widget.Toast;

public class XDUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private Context context;
	public XDUncaughtExceptionHandler(Context context){
		this.context=context;
	}
	@Override
	public void uncaughtException(Thread arg0, Throwable err) {
		// TODO Auto-generated method stub
		try{
			Toast.makeText(context, R.string.err_exit, Toast.LENGTH_LONG).show();
			StringWriter stringWriter=new StringWriter();
			err.printStackTrace(new PrintWriter(stringWriter));
			XiDiGeUtil.simpleEmailUseDefault(context, "cidy0106@gmail.com", context.getString(R.string.err_report), stringWriter.toString());
		}catch (Exception e) {
			// TODO: handle exception
		}
		try {			
			android.os.Process.killProcess(android.os.Process.myPid());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
