package com.xidige.updater;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class CheckVersion implements Runnable {
    private static final String TAG = "com.xidige.updater.CheckVersion";
    public static final String PREFRENCE_DOWNLOAD_KEY = "prefrence_download_key";
    //通知栏跳转Intent
    public static Class RETURNOPENCLASS = null;////点击通知栏要跳转的界面
    public static int NOTIFCATIONICON = android.R.drawable.stat_sys_download_done;//通知栏图标

    private static final boolean useSysDownloadFirst = false;//默认是否优先使用系统下载服务

    private static long checkedTime = 0;
    private static long checkInterval = 1000 * 60 * 60;//检查更新的间隔时间
    private boolean checking = false;//检查更新的标志

    private String packagename = null;// /当前包名
    private String version_url = null;// 获取版本的地址
    private SharedPreferences sharedPreferences = null;

    private Context context = null;
    private CheckVersionResult result = null;

    private CheckVersion() {
        //不使用
    }

    public CheckVersion(Context context, String packagename, String versionUrl, Class returnOpenClass, int notifcationIcon) {
        this.context = context;
        this.packagename = packagename;
        this.version_url = versionUrl;
        RETURNOPENCLASS = returnOpenClass;
        NOTIFCATIONICON = notifcationIcon;
    }

    private static final int HANDLER_MSG_HASNEWVERION = 0x0106;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MSG_HASNEWVERION:
                    showUpdateDialog(result.getDownloadPath());
                    break;
                default:
                    break;
            }
        }
    };

    public void run() {
        if (!checking) {
            checking = true;
            if (checkedTime + checkInterval < System.currentTimeMillis()) {
                if (isAutoUpdate()) {
                    result = checkLastVersion();
                    if (result != null) {
                        if (result.getVersionCode() > getVersionCode()) {
                            handler.sendEmptyMessage(HANDLER_MSG_HASNEWVERION);
                        }
                    }
                }
            }
            checkedTime = System.currentTimeMillis();
            checking = false;
        }
    }

    // 是否允许自动检查更新
    private boolean isAutoUpdate() {
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this.context);
        // 是否自动更新
        boolean autoupdate = sharedPreferences.getBoolean("autoupdate", true);// 默认自动更新
        return autoupdate;
    }

    // 检查更新版本
    // 记录版本信息，下载地址
    private CheckVersionResult checkLastVersion() {
//		IGetVersion getVersion=new GetVersion163dropbox();
        IGetVersion getVersion = new GetVersionXidige();
        return getVersion.getVersion(version_url);
    }

    // 获取本地应用版本号
    private int getVersionCode() {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(
                    packagename, 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.d(TAG, e.toString());
        }
        return versionCode;
    }

    /***
     * api 9,sdk 2.3之后才公开了download的
     * @return
     */
    private boolean canUseSysDownload() {
        return Build.VERSION_CODES.GINGERBREAD <= Integer.parseInt(Build.VERSION.SDK);
    }

    // 使用系统download服务
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void sysDownload(String fileurl, boolean visible) {
        DownloadManager downloadManager = (DownloadManager) context
                .getSystemService(Service.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(fileurl);
        Request request = new Request(uri);

        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
                | Request.NETWORK_WIFI);

        // 禁止发出通知，既后台下载，如果要使用这一句必须声明一个权限：android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        // request.setShowRunningNotification(false);

        // 不显示下载界面
        request.setVisibleInDownloadsUi(visible);
        /*
         * 设置下载后文件存放的位置,如果sdcard不可用，那么设置这个将报错，因此最好不设置
         * 如果sdcard可用，下载后的文件在/mnt/sdcard/Android/data/packageName/files目录下面，
         * 如果sdcard不可用,设置了下面这个将报错，不设置，下载后的文件在/cache这个 目录下面
         */
        // request.setDestinationInExternalFilesDir(this, null, "tar.apk");
        long id = downloadManager.enqueue(request);
        // TODO 把id保存好，在接收者里面要用，最好保存在Preferences里面
        Editor editor = sharedPreferences.edit();
        editor.putLong(PREFRENCE_DOWNLOAD_KEY, id);
        editor.commit();
    }

    /**
     * 显示下载提示对话框
     */
    public void showUpdateDialog(final String fileurl) {
        // 构造对话框
        Builder builder = new Builder(context);
        builder.setTitle(R.string.soft_update_title);
        if (result != null && !TextUtils.isEmpty(result.getUpdatemsg())) {
            builder.setMessage(result.getUpdatemsg());
        } else {
            builder.setMessage(R.string.soft_update_info);
        }

        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (useSysDownloadFirst
                        && canUseSysDownload()) {
                    sysDownload(fileurl, true);
                } else {
                    //开启更新服务UpdateService
                    //这里为了把update更好模块化，可以传一些updateService依赖的值
                    //如布局ID，资源ID，动态获取的标题,这里以app_name为例
                    Intent updateIntent = new Intent(context, UpdateService.class);
                    updateIntent.putExtra("fileurl", fileurl);
                    context.startService(updateIntent);
                }
            }
        }).setNegativeButton(R.string.soft_update_later, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /****
     * 保存版本信息
     *
     * @author lenovo
     *
     */
//	static class CheckVersionResult implements Serializable {
//		private int versionCode;// 版本号
//		private String versionName;// 可见的版本号
//		private String packagename;// 完整包名,比如com.xidige.qvmerger
//		private String downloadPath;// 下载完整路径
//
//		public int getVersionCode() {
//			return versionCode;
//		}
//
//		public void setVersionCode(int versionCode) {
//			this.versionCode = versionCode;
//		}
//
//		public String getVersionName() {
//			return versionName;
//		}
//
//		public void setVersionName(String versionName) {
//			this.versionName = versionName;
//		}
//
//		public String getPackagename() {
//			return packagename;
//		}
//
//		public void setPackagename(String packagename) {
//			this.packagename = packagename;
//		}
//
//		public String getDownloadPath() {
//			return downloadPath;
//		}
//
//		public void setDownloadPath(String downloadPath) {
//			this.downloadPath = downloadPath;
//		}
//
//	}
}
