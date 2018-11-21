package com.xidige.updater;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetVersionXidige implements IGetVersion {
	private static final String TAG="GetVersionXidige";
	/**
	 * 这里的地址像http://www.xidige.com/checkversion.php?package=com.xidige.qvmerger
	 */
	@Override
	public CheckVersionResult getVersion(String urlstr) {
		// TODO Auto-generated method stub
		HttpURLConnection connection=null;
		JsonReader jsonReader=null;
		try {
			URL url=new URL(urlstr);
			connection=(HttpURLConnection) url.openConnection();
			connection.getDoInput();
			jsonReader=new JsonReader(new InputStreamReader(connection.getInputStream()));
			jsonReader.beginObject();
			
			String name=null;
			CheckVersionResult checkVersionResult=new CheckVersionResult();
			while (jsonReader.hasNext()) {
				name=jsonReader.nextName();
				if ("versioncode".equalsIgnoreCase(name)) {
					checkVersionResult.setVersionCode(jsonReader.nextInt());
				}else if ("versionname".equalsIgnoreCase(name)) {
					checkVersionResult.setVersionName(jsonReader.nextString());
				}else if ("updatemsg".equalsIgnoreCase(name)) {
					checkVersionResult.setUpdatemsg(jsonReader.nextString());
				}else if ("downloadurl".equalsIgnoreCase(name)) {
					checkVersionResult.setDownloadPath(jsonReader.nextString());
				}else if ("packagename".equalsIgnoreCase(name)) {
					checkVersionResult.setPackagename(jsonReader.nextString());
				}else{
					jsonReader.skipValue();
				}
			}
			jsonReader.endObject();
			return checkVersionResult;
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}finally{
			if(jsonReader!=null){
				try {
					jsonReader.close();
				} catch (IOException e) {
				}
			}							
		}
		
		
		
		return null;
	}

}
