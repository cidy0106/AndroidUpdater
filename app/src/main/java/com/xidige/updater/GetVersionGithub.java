package com.xidige.updater;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GetVersionGithub implements IGetVersion {
    private static Logger logger = LoggerFactory.getLogger(GetVersionGithub.class);

    /**
     * 这里的地址像https://cidy0106.github.io/cidy0106/apk.json
     * 从里面解析出自己对应的版本
     *
     * @param packageName 必须有，是app的完整包名
     */
    @Override
    public CheckVersionResult getVersion(String packageName) {
        String content = HtmlUtil.getHtml("https://cidy0106.github.io/cidy0106/apk.json");
        if (content != null) {
            Gson gson = new Gson();
            try {
                List<CheckVersionResult> results = gson.fromJson(content, new TypeToken<List<CheckVersionResult>>() {
                }.getType());
                if (results != null && !results.isEmpty()) {
                    for (int i = 0; i < results.size(); i++) {
                        CheckVersionResult result = results.get(i);
                        if (result.getPackagename().equalsIgnoreCase(packageName)) {
                            return result;
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("getVersion", e);
            }
        }

        return null;
    }
}
