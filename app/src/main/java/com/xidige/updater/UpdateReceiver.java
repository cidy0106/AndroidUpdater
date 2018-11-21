package com.xidige.updater;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * 使用系统download服务时，用来接收下载完成的消息
 * 
 * @author lenovo
 * 
 */
public class UpdateReceiver extends BroadcastReceiver {
	private DownloadManager downloadManager;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			// 判断这个id与之前的id是否相等，如果相等说明是之前的那个要下载的文件
			SharedPreferences sharedPreferences =PreferenceManager.getDefaultSharedPreferences(context);
			long oid=sharedPreferences.getLong(CheckVersion.PREFRENCE_DOWNLOAD_KEY, 0); 
			if (id==oid) {
				Toast.makeText(context, context.getString(R.string.soft_update_downloadover), Toast.LENGTH_LONG).show();
				
				Query query = new Query();
				query.setFilterById(id);
				downloadManager = (DownloadManager) context
						.getSystemService(Context.DOWNLOAD_SERVICE);
				Cursor cursor = downloadManager.query(query);

				int columnCount = cursor.getColumnCount();
				String local_uri = null; // TODO 这里把所有的列都打印一下，有什么需求，就怎么处理,文件的本地路径就是path
				String local_filename=null;
				while (cursor.moveToNext()) {
					for (int j = 0; j < columnCount; j++) {
						String columnName = cursor.getColumnName(j);
						String string = cursor.getString(j);
						if (columnName.equals("local_uri")) {
							local_uri = string;
						}else if (columnName.equals("local_filename")) {
							local_filename=string;
						}						
					}
				}
				cursor.close();
				
				if (local_filename!=null && !"".equals(local_filename.trim())) {
					installApk("file://" + local_filename, context);
				}else if (local_uri!=null && !"".equals(local_uri.trim())) {
					installApk(local_uri, context);
				}
				
			}
		} 
	}
	
	/**
     * 安装apk
     */
    private void installApk(String apkpath,Context context)
    {
        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse(apkpath), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
