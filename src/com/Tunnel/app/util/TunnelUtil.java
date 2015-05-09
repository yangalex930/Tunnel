package com.Tunnel.app.util;

import android.graphics.Bitmap;
import android.os.Environment;
import com.Tunnel.app.TunnelApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Yang Yupeng on 2014/7/30.
 */
public class TunnelUtil {
    private static String tunnelFolderPath = null;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static void prepareTunnelFolder()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            getTunnelFolderPath();
        }
    }

    public static String getTunnelFolderPath()
    {
        if (tunnelFolderPath == null)
        {
            tunnelFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/tunnel/";
            FileUtil.createFolder(tunnelFolderPath);
            CreateProjectFolder("缺省工程");
        }
        return tunnelFolderPath;
    }

    public static String CreateProjectFolder(String projectName)
    {
        String path = tunnelFolderPath + projectName;
        FileUtil.createFolder(path);
        return path;
    }

    public static String CreateImageFolder(String imageName)
    {
        StringBuilder sb = new StringBuilder(tunnelFolderPath);
        sb.append(ProjectUtil.getDefaultProject());
        FileUtil.createFolder(sb.toString());
        sb.append("/").append(imageName).append("/");
        FileUtil.createFolder(sb.toString());
        return sb.toString();
    }

    public static String getDefaultImageName()
    {
        return simpleDateFormat.format(new Date());
    }
}
