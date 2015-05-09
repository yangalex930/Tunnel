package com.Tunnel.app.util;

import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.Toast;
import com.Tunnel.app.TunnelApplication;
import com.Tunnel.app.activity.MeasureInfo;

/**
 * @author yupeng.yyp
 * @create 14-10-4 14:01
 */
public class ExifUtil {
    public static void saveOrientation(String imagePath)
    {
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            String s = String.format("%f;%f", Orientation.X, Orientation.Y);
            ei.setAttribute(ExifInterface.TAG_MODEL, Base64Util.encode(s));
            ei.saveAttributes();
        } catch (Exception e) {
            Log.e("saveOrientation", e.getMessage());
        }
    }

    public static void getOrientation(String imagePath)
    {
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            String s = Base64Util.decode(ei.getAttribute(ExifInterface.TAG_MODEL));
            String [] xy = s.split(";");
            Orientation.X = Float.parseFloat(xy[0]);
            Orientation.Y = Float.parseFloat(xy[1]);

        } catch (Exception e) {
            Toast.makeText(TunnelApplication.getInstance(), "获取图片拍摄角度失败", Toast.LENGTH_LONG);
            Log.e("getOrientation", e.getMessage());
        }
    }

    public static void saveMeasureInfo(String imagePath, Rect corpRect, Rect markRect, int slope)
    {
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            String s = String.format("%d;%d;%d;%d",
                    corpRect.left,
                    corpRect.right,
                    corpRect.top,
                    corpRect.bottom);
            s = Base64Util.encode(s);
            ei.setAttribute(ExifInterface.TAG_MAKE, s);
            s = String.format("%d;%d;%d;%d;%d",
                    markRect.left,
                    markRect.right,
                    markRect.top,
                    markRect.bottom,
                    slope);
            s = Base64Util.encode(s);
            ei.setAttribute(ExifInterface.TAG_MODEL, s);

            ei.saveAttributes();
        } catch (Exception e) {
            Log.e("saveOrientation", e.getMessage());
        }
    }

    public static MeasureInfo getMeasureInfo(String imagePath)
    {
        MeasureInfo mi = new MeasureInfo();
        try {
            ExifInterface ei = new ExifInterface(imagePath);
            String s = ei.getAttribute(ExifInterface.TAG_MAKE);
            s = Base64Util.decode(s);
            String [] info = s.split(";");
            mi.cropRect.left = Integer.parseInt(info[0]);
            mi.cropRect.right = Integer.parseInt(info[1]);
            mi.cropRect.top = Integer.parseInt(info[2]);
            mi.cropRect.bottom = Integer.parseInt(info[3]);
            s = ei.getAttribute(ExifInterface.TAG_MODEL);
            s = Base64Util.decode(s);
            info = s.split(";");
            mi.markRect.left = Integer.parseInt(info[0]);
            mi.markRect.right = Integer.parseInt(info[1]);
            mi.markRect.top = Integer.parseInt(info[2]);
            mi.markRect.bottom = Integer.parseInt(info[3]);
            mi.slope = Integer.parseInt(info[4]);

        } catch (Exception e) {
            Toast.makeText(TunnelApplication.getInstance(), "获取图片检测信息失败", Toast.LENGTH_LONG);
            Log.e("getOrientation", e.getMessage());
            mi = null;
        }
        return mi;
    }
}
