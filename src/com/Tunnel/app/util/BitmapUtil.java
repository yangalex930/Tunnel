package com.Tunnel.app.util;

import android.graphics.*;
import android.media.ThumbnailUtils;
import com.Tunnel.app.TunnelApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author yupeng.yyp
 * @create 14-8-31 19:01
 */
public class BitmapUtil {

    public static String saveBitmapByName(Bitmap bitmap, String imageName)
    {
        StringBuilder path = new StringBuilder(TunnelUtil.CreateImageFolder(imageName));
        path.append("原图").append(".jpg");
        String imagePath = saveBitmap(bitmap, path.toString());
        Orientation.setOrientationExif(imagePath);
        return imagePath;
    }

    private static Paint edgePaint = new Paint();
    private static Paint areaPaint = new Paint();

    static {
        edgePaint.setColor(Color.WHITE);
        edgePaint.setStrokeWidth(2);
        edgePaint.setStyle(Paint.Style.STROKE);
        PathEffect effects = new DashPathEffect(new float[]{10, 10, 10, 10}, 1);
        edgePaint.setPathEffect(effects);

        areaPaint.setARGB(90, 8, 8, 8);
        areaPaint.setStyle(Paint.Style.FILL);
    }

    public static Bitmap drawCacheBitmap(Bitmap srcBitmap, Rect rect)
    {
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);

        canvas.drawRect(0, 0, rect.left, srcBitmap.getHeight(), areaPaint);
        canvas.drawRect(rect.left, 0, rect.right, rect.top, areaPaint);
        canvas.drawRect(rect.right, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), areaPaint);
        canvas.drawRect(rect.left, rect.bottom, rect.right, srcBitmap.getHeight(), areaPaint);
        canvas.drawRect(rect, edgePaint);
        canvas.save();
        return bitmap;
    }

    public static String getCacheImagePath() {
        return TunnelApplication.getInstance().getExternalCacheDir().getAbsoluteFile() + "/.temp";
    }

    public static String saveTempBitmapCache(Bitmap bitmap)
    {
        return BitmapUtil.saveBitmap(bitmap, getCacheImagePath());
    }

    private static String genImageNameWithCheckSuffix(String srcImagePath)
    {
        String s = srcImagePath.substring(0, srcImagePath.lastIndexOf("/"));
        int i = 1;
        while (true)
        {
            StringBuilder sb = new StringBuilder(s);
            sb.append("/检测").append(i).append(".jpg");
            File file = new File(sb.toString());
            if (file.exists())
            {
                i++;
            }
            else
            {
                return file.getPath();
            }
        }
    }

    public static String renameBitmapCache(String srcImagePath)
    {
        File file = new File(getCacheImagePath());
        if (!file.exists())
        {
            return null;
        }
        String newName = genImageNameWithCheckSuffix(srcImagePath);
        file.renameTo(new File(newName));
        return newName;
    }

    private static String saveBitmap(Bitmap bitmap, String imagePath)
    {
        File file = new File(imagePath);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imagePath;
    }

    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        setOption(options, width, height);
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static Bitmap getImageThumbnail(int Resid, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(TunnelApplication.getInstance().getResources(), Resid, options);

        setOption(options, width, height);
        bitmap = BitmapFactory.decodeResource(TunnelApplication.getInstance().getResources(), Resid, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    private static void setOption(BitmapFactory.Options options, int width, int height) {
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int wRate = w / width;
        int hRate = h / height;
        int rate = 1;
        if (wRate < hRate) {
            rate = wRate;
        } else {
            rate = hRate;
        }
        if (rate <= 0) {
            rate = 1;
        }
        options.inSampleSize = rate;
        options.inJustDecodeBounds = false;
    }
}
