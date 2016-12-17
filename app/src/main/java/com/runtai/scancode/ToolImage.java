package com.runtai.scancode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

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

    /**
     * 文件夹名
     */
    public static String dir;

    /**
     * 文件名
     */
    public static String fileName = "";

    /**
     * 文件格式(后缀名)
     */
    public static String suffix = ".png";

    /**
     * 先保存到本地再广播到图库
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        dir = context.getPackageName();
        //File appDir = new File(context.getExternalCacheDir(), dir);//不知道为什么用这个路径下保存的图片不能更新到图库
        File appDir = new File(Environment.getExternalStorageDirectory(), dir);
        if (!appDir.exists()) {
            appDir.mkdir();//创建文件夹
        }
        fileName = "IMG" + getTime() + suffix;
        file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 再通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
    }

    /**
     * 文件名(时间格式)
     */
    private static String getTime() {
        long time = System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");2016-08-05 11:27:43
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(time);
        return format.format(date);
    }

}