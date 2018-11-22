package com.xidige.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HtmlUtil {
	private static Logger logger=LoggerFactory.getLogger(HtmlUtil.class);

	private static OkHttpClient client = new OkHttpClient();

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
			logger.debug("读取流中数据出现错误", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.debug("关闭流时发生错误", e);
				}
			}
		}
		return buffer.toString();
	}


	public static String getHtml(String url) {
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            logger.debug("getHtml",e);
        }finally {
            if(response!=null){
                response.close();
            }
        }
        return null;
    }
}
