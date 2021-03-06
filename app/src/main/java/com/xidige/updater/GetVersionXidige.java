package com.xidige.updater;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 网站重新弄过，这部分功能已经不在了，全部迁到github
 */
@Deprecated
public class GetVersionXidige implements IGetVersion {
    private static final String TAG="GetVersionXidige";

    /**
     * 这里的地址像http://www.xidige.com/checkversion.php?package=com.xidige.qvmerger
     */
    @Override
    public CheckVersionResult getVersion(String urlstr) {
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
