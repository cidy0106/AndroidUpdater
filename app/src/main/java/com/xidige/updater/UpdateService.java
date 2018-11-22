package com.xidige.updater;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * fileurl
 * @author lenovo
 *
 */
public class UpdateService extends Service {
	private static Logger logger=LoggerFactory.getLogger(UpdateService.class);

    public static final String ARG_DOWNLOAD_URL = "fileurl";

	//标题
	private String fileurl=null;//需要下载的文件路径
	private String titleContent=null;//提示内容
	
	//下载后的文件
	private File updateFile = null;
	 
	//通知栏
	private NotificationManager updateNotificationManager = null;
	private Notification updateNotification = null;
	
	
	private Intent updateIntent = null;
	private PendingIntent updatePendingIntent = null;
	
	public UpdateService(){}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private static final int HANDLER_MSG_DOWNLOADOVER=0x0106;//下完了
	private static final int HANDLER_MSG_DOWNLOADFAIL=0x0107;//下载失败
	private static final int HANDLER_MSG_UPDATEOVER=0x0108;//更新完成
	private static final int HANDLER_MSG_UPDATEFAIL=0x0109;//更新失败
	/**
	 * 响应线程中的事件
	 */
	private Handler handler=new Handler(){
		@Override
	    public void handleMessage(Message msg) {
	         switch (msg.what) {
			case HANDLER_MSG_DOWNLOADOVER:
				//下完了，开始更新吧
				Uri uri = Uri.fromFile(updateFile);
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                updatePendingIntent = PendingIntent.getActivity(UpdateService.this, 0, installIntent, 0);

                updateNotification=createNotification(UpdateService.this, titleContent, getString(R.string.soft_update_downloadover), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
				break;
			case HANDLER_MSG_DOWNLOADFAIL:
				//下失败了，提示吧
				//下载失败
                updateNotification=createNotification(UpdateService.this, titleContent, getString(R.string.soft_update_downloadfail), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
				break;
			case HANDLER_MSG_UPDATEOVER:
				//更新完了，提示吧
				updateNotification=createNotification(UpdateService.this, titleContent, getString(R.string.soft_update_updateover), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
                stopService(updateIntent);
				break;
			case HANDLER_MSG_UPDATEFAIL:
				//更新失败了，提示吧
				//下载失败
                updateNotification=createNotification(UpdateService.this, titleContent, getString(R.string.soft_update_updatefail), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
			default:
				break;
			}
	    }
	};
	
	private boolean stop = false;// 控制自己的下载线程的变量
	/**
	 * 下载线程
	 */
	private Runnable updateRunnable=new Runnable() {
		public void run() {
			InputStream is = null;
			FileOutputStream fos = null;
			HttpURLConnection conn=null;
			try {
				URL url = new URL(fileurl);
				// 创建连接
				conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				if (conn.getResponseCode() == 404) {
					handler.sendEmptyMessage(HANDLER_MSG_DOWNLOADFAIL);
					return ;
				}
				// // 获取文件大小
				int length = conn.getContentLength();
				// 创建输入流
				is = conn.getInputStream();
				// 创建临时文件
				File tempfile= creatTempFile(titleContent+ "update", ".apk");
				updateFile=tempfile;
				fos = new FileOutputStream(tempfile);
				int downloadCount=0;//记录次数
				int count = 0;
				// 缓存
				byte buf[] = new byte[1024];
				int numread = -1;// 读取多少字节了
				// 写入到文件中
				while (!stop &&(numread = is.read(buf)) != -1) {					
					fos.write(buf, 0, numread);
					count += numread;
					//为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
					if ((downloadCount == 0) || count * 100 / length - 10 > downloadCount) {
                        downloadCount += 10;
						updateNotification=createNotification(UpdateService.this, getString(R.string.soft_update_downloading), count * 100 / length + "%", updatePendingIntent);
                        updateNotificationManager.notify(0, updateNotification);
                    }   
				}
				is.close();
				fos.close();
				is=null;
				fos=null;
				//下载完了
				handler.sendEmptyMessage(HANDLER_MSG_DOWNLOADOVER);	
			} catch (Exception e) {
				// TODO: handle exception
				logger.debug("下载线程发生错误", e);
			}finally{
				if (is!=null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				}
				if (fos!=null) {
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				}
				if (conn!=null) {
					conn.disconnect();
				}
			}
		}
	};

	private Notification createNotification(Context context,String title,String content,PendingIntent pendingIntent){
        return new NotificationCompat.Builder(context,"default")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();
    }


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //获取传值
        this.fileurl = intent.getStringExtra(ARG_DOWNLOAD_URL);
	 
		this.titleContent=getString(R.string.app_name);
	    this.updateNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	 
	    //设置下载过程中，点击通知栏，回到主界面
	    updateIntent = new Intent(this, CheckVersion.RETURNOPENCLASS);
	    updatePendingIntent = PendingIntent.getActivity(this,0,updateIntent,0);
	    //设置通知栏显示内容
	    updateNotification=createNotification(this,titleContent,"0%",updatePendingIntent);
	    //发出通知
	    updateNotificationManager.notify(0,updateNotification);
	 
	    //开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
	    new Thread(updateRunnable).start();//这个是下载的重点，是下载的过程
	    
	    return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 创建临时文件
	 * @param filename 文件名
	 * @param ext 后缀，需要加.
	 * @return
	 */
	private File creatTempFile(String filename,String ext){
        File dir = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		} else {
            dir = getApplicationContext().getFilesDir();
        }

		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = null;

		try {
			file=new File(dir, filename + ext);
			if (!file.exists()) {				
				file.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("创建文件发生错误", e);
		}

		return file;
	}
	
}
