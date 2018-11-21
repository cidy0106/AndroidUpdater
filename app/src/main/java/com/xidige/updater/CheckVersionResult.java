package com.xidige.updater;

import java.io.Serializable;

public class CheckVersionResult implements Serializable {
	private int versionCode;// 版本号
	private String versionName;// 可见的版本号
	private String packagename;// 完整包名,比如com.xidige.qvmerger
	private String downloadPath;// 下载完整路径

	private String updatemsg;//提示信息
	
	
	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public String getUpdatemsg() {
		return updatemsg;
	}

	public void setUpdatemsg(String updatemsg) {
		this.updatemsg = updatemsg;
	}
}
