package com.runtai.scancode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 图片保存工具类
 */
public abstract class ToolImage {

	public static File file;
	/** 文件夹名 */
	public static String dir = "image";
	/** 文件名 */
	public static String fileName = "";
	/** 文件格式(后缀名) */
	public static String suffix = ".png";

	/**
	 * 先保存到本地再广播到图库
	 * */
	public static void saveImageToGallery(Context context, Bitmap bmp) {
		// 首先保存图片
		File appDir = new File(context.getExternalCacheDir(), dir);
//		File appDir = new File(Environment.getExternalStorageDirectory(), dir);
		if (!appDir.exists()) {
			appDir.mkdir();//创建文件夹
		}
		fileName = "IMG" + getTime();
		file = new File(appDir, fileName + suffix);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 其次把文件插入到系统图库
		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
			// 最后通知图库更新
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 文件名(时间格式)
     */
	private static  String getTime(){
		long time = System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");2016-08-05 11:27:43
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(time);
		return format.format(date);
	}


}
