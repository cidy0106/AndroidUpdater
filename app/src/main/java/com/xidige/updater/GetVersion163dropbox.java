package com.xidige.updater;

import com.google.gson.stream.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 163博客上的文章要有一下内容
 * ###{packageName:com.xidige.filemanager,versionName:'2',versionCode
 * :'1.0.1',dropbox:'s/d66us5q4kr3or2d/FileManager_1.0.1.apk'}###
 * 
 * @author lenovo
 * 163博客做不下去了，关闭了；dropbox国内已不能访问，这里就没用了
 *
 */
@Deprecated
public class GetVersion163dropbox implements IGetVersion {
	private static Logger logger=LoggerFactory.getLogger(GetVersion163dropbox.class);

	/**
	 * 存放有应用版本信息的url，这里是163上的
	 */
	@Override
	public CheckVersionResult getVersion(String url) {
		// TODO Auto-generated method stub
		// 先获取到163上的内容
		String html = HtmlUtil.getHtml(url);
		if(html!=null){
			// 上面的地址里的内容有：###{packageName:com.xidige.filemanager,versionName:2,versionCode:1.0.1,dropbox::'s/d66us5q4kr3or2d/FileManager_1.0.1.apk'}###
			Pattern pattern = Pattern.compile("###\\{(.+?)\\}###");
			Matcher matcher = pattern.matcher(html);
			if (matcher.find()) {
				html = matcher.group(1);
				logger.debug( "匹配到的内容" + html);
				if (html != null) {
					html = "{" + html + "}";// 原来那个是没有括号的
					ByteArrayInputStream in = null;
					try {
						in = new ByteArrayInputStream(html.getBytes("utf-8"));
						// 解析出包的部分信息，后面还要解析正确地址
						JsonReader reader = new JsonReader(
								new InputStreamReader(in));

						reader.setLenient(true);

						reader.beginObject();

						CheckVersionResult versionInfo = new CheckVersionResult();
						String name = null;
						String dropboxGongkaiUrl = null;// 存放dropbox公开地址
						while (reader.hasNext()) {
							name = reader.nextName();
							if ("packageName".equalsIgnoreCase(name)) {
								versionInfo.setPackagename(reader.nextString());
							} else if ("versionCode".equalsIgnoreCase(name)) {
								versionInfo.setVersionCode(reader.nextInt());
							} else if ("versionName".equalsIgnoreCase(name)) {
								versionInfo.setVersionName(reader.nextString());
							} else if ("dropbox".equalsIgnoreCase(name)) {
								// 只是一个参数而已
								dropboxGongkaiUrl = reader.nextString();
							}
						}
						reader.endObject();
						// 解析dropbox的真正地址

						if (dropboxGongkaiUrl != null) {
							dropboxGongkaiUrl = "https://www.dropbox.com/"
									+ dropboxGongkaiUrl;
							// 解析公开地址上的内容，取出真是地址
							html = parsetDropboxReal(dropboxGongkaiUrl);

							if (html != null && html.startsWith("http")) {
								versionInfo.setDownloadPath(html);
								return versionInfo;
							}
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						logger.debug( "解析版本信息时错误", e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.debug( "解析版本信息时错误", e);
					}
				}
			}
		}
		
		return null;
	}
	/**
	 * 解析真正的文件地址，如果是文件地址，一般是：http开始的，记得检查这个
	 * @param gongkai
	 * @return
	 */
	private String parsetDropboxReal(String gongkai) {
		// 获取页面内容
		String html = HtmlUtil.getHtml(gongkai);
		if(html!=null){
			// 里面的下载地址格式是：
			// https://dl.dropboxusercontent.com/***">
			Pattern pattern = Pattern
					.compile("https://dl.dropboxusercontent.com/s/(.+?)\" id=\"default_content_download_button");
			Matcher matcher = pattern.matcher(html);
			if (matcher.find()) {
				html = matcher.group(1);
				if(html!=null){
					html = "https://dl.dropboxusercontent.com/s/" + html;
					return html;				
				}			
			}
		}
		return null;
	}
}
