package com.xidige.updater;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HtmlUtil {
	private static final String TAG="com.xidige.updater.HtmlUtil";
	public static String readFromInputStream(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG,"读取流中数据出现错误", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG,"关闭流时发生错误", e);
				}
			}
		}
		return buffer.toString();
	}
}
