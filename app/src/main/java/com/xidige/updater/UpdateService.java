package com.xidige.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
/**
 * fileurl
 * @author lenovo
 *
 */
public class UpdateService extends Service {
	private static final String TAG="com.xidige.updater.UpdateService";
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
                 
                updateNotification.defaults = Notification.DEFAULT_SOUND;//铃声提醒 
                updateNotification.setLatestEventInfo(UpdateService.this, titleContent, getString(R.string.soft_update_downloadover), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
				break;
			case HANDLER_MSG_DOWNLOADFAIL:
				//下失败了，提示吧
				//下载失败
                updateNotification.setLatestEventInfo(UpdateService.this, titleContent, getString(R.string.soft_update_downloadfail), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
				break;
			case HANDLER_MSG_UPDATEOVER:
				//更新完了，提示吧
				updateNotification.setLatestEventInfo(UpdateService.this, titleContent, getString(R.string.soft_update_updateover), updatePendingIntent);
                updateNotificationManager.notify(0, updateNotification);
                stopService(updateIntent);
				break;
			case HANDLER_MSG_UPDATEFAIL:
				//更新失败了，提示吧
				//下载失败
                updateNotification.setLatestEventInfo(UpdateService.this, titleContent, getString(R.string.soft_update_updatefail), updatePendingIntent);
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
                    if((downloadCount == 0)||(int) (count*100/length)-10>downloadCount){ 
                        downloadCount += 10;
                        updateNotification.setLatestEventInfo(UpdateService.this, getString(R.string.soft_update_downloading), (int)(count*100/length)+"%", updatePendingIntent);
                        updateNotificationManager.notify(0, updateNotification);
                    }   
				}
				is.close();
				fos.close();
				is=null;
				fos=null;
				//下载完了
				handler.sendEmptyMessage(HANDLER_MSG_DOWNLOADOVER);	
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"下载线程发生错误", e);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"下载线程发生错误", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"下载线程发生错误", e);
			} catch (Exception e) {
				// TODO: handle exception
				Log.d(TAG,"下载线程发生错误", e);
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
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //获取传值
		this.fileurl=intent.getStringExtra("fileurl");
	 
		this.titleContent=getString(R.string.app_name);
	    this.updateNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    this.updateNotification = new Notification();
	 
	    //设置下载过程中，点击通知栏，回到主界面
	    updateIntent = new Intent(this, CheckVersion.RETURNOPENCLASS);
	    updatePendingIntent = PendingIntent.getActivity(this,0,updateIntent,Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	    //设置通知栏显示内容
	    updateNotification.icon = CheckVersion.NOTIFCATIONICON;
	    updateNotification.tickerText = titleContent;
	    updateNotification.setLatestEventInfo(this,titleContent,"0%",updatePendingIntent);
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
		String tempDirStr = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			tempDirStr = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/download/";
		} else {
			tempDirStr = getApplicationContext().getFilesDir()
					.getAbsolutePath();
		}
		File dir = new File(tempDirStr);
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
			Log.d(TAG,"创建文件发生错误", e);
		}

		return file;
	}
	
}