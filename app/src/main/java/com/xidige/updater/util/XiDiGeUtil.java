package com.xidige.updater.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class XiDiGeUtil {
	/**
	 * 获取应用版本名
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {    
	    String versionName = "";    
	    try {    
	        // ---get the package info---    
	        PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi.versionName != null) {
	        	versionName = pi.versionName;    
	        }    
	    } catch (Exception e) {
	    }    
	    return versionName;    
	}
	/**
	 * 发送邮件
	 * @param context
	 * @param emailaddr 邮件地址：cidy0106@hotmail.com
	 * @param title 标题
	 * @param content 内容
	 */
	public static void simpleEmailUseDefault(Context context, String emailaddr,String title,String content){
		try {
			Intent data=new Intent(Intent.ACTION_SENDTO);
			data.setData(Uri.parse("mailto:"+emailaddr)); 
			
//			Intent data=new Intent(Intent.ACTION_SEND);
			data.putExtra(Intent.EXTRA_EMAIL, emailaddr);
			data.putExtra(Intent.EXTRA_SUBJECT, title); 
			data.putExtra(Intent.EXTRA_TEXT, content);
			data.setType("message/rfc822");
			File cpuinfo=new File("/proc/cpuinfo");
			if(cpuinfo.exists()){
				data.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cpuinfo));
			}
			
			context.startActivity(data);
		} catch (Exception e) {
			// TODO: handle exception
		}		
	}
	/**
	 * 取某段范围内的随机数
	 * @param random
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randomInt(Random random,int min,int max){
		return random.nextInt(max)% (max-min+1) +min;
	}
	/**
	 * 处理文件大小的显示
	 * @param size 单位B
	 * @return
	 */
	public static String fileSizeStr(long size){
		String str = "B";
		if (size >= 1024) {
			str = "KB";
			size /= 1024;
			if (size >= 1024) {
				str = "MB";
				size /= 1024;
				if (size >= 1024) {
					str = "GB";
					size /= 1024;
				}
			}
		}
		DecimalFormat formatter = new DecimalFormat();
		formatter.setGroupingSize(3);
		return formatter.format(size)+" "+str;
	}
	/**
	 * 时间格式化，根据区域自动选择格式，比如有：（yyyy-mm-dd hh:mm:ss）
	 * @param timestamp
	 * @return
	 */
	public static String timeFormat(long timestamp){
		SimpleDateFormat formate=(SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
//		SimpleDateFormat formate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formate.format(new Date(timestamp));		
	}
	/**
	 * 获取sd卡根目录
	 * @return
	 */
	public static String sdDir(){
		if (Environment.getExternalStorageState()   
                           .equals(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return null;
	}

	/**
	 * 将获取的int转为真正的ip地址,参考的网上的，修改了下
	 * @param i
	 * @return
	 */
	public static String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}
	/**
	 * 把路径上的后缀名部分取出来，比如：“.mp4”
	 * @param filepath
	 * @return
	 */
	public static String getFileExt(String filepath){
		if(filepath!=null){
			int index=filepath.lastIndexOf(".");
			if(index!=-1){
				return filepath.substring(index);
			}
		}
		return null;
	}
	/**
	 * 从给定的文件夹名上获取文件名部分，没有后缀名
	 * @return
	 */
	public static String getname(String filename){
		if(filename!=null){
			int index=filename.lastIndexOf(".");
			if(index!=-1){
				return filename.substring(0, index);						
			}
		}
		return filename;
	}
	/**
	 * 系统时间设置是否是24小时制
	 * @param ctx
	 * @return
	 */
	public static boolean isTime24(Context ctx){
		try {
			ContentResolver cv = ctx.getContentResolver();
			String strTimeFormat = android.provider.Settings.System.getString(
					cv, android.provider.Settings.System.TIME_12_24);
			if (strTimeFormat != null && strTimeFormat.equals("24")) {// strTimeFormat某些rom12小时制时会返回null
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}	    
		return false;
    }
}
