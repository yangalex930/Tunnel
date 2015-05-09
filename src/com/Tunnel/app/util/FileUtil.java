package com.Tunnel.app.util;

import java.io.File;

/**
 * @author yupeng.yyp
 * @create 14-8-31 19:11
 */
public class FileUtil {
    public static String createFolder(String folderPath)
    {
        File folder = new File(folderPath);
        if (!folder.exists())
        {
            folder.mkdir();
        }
        return folderPath;
    }

    public static File[] getFileList(String folder)
    {
        File fileDir = new File(folder);
        File[] files = fileDir.listFiles();
        return files;
    }

    public static void deleteFile(File file)
    {
        if (!file.exists())
        {
            return;
        }
        if (file.isFile()) {
            file.delete();
        }
        else
        {
            File files[] = file.listFiles();
            for(int i = 0;i < files.length;i++){
                deleteFile(files[i].getPath());
            }
            file.delete();
        }
    }

    public static void deleteFile(String filePath)
    {
        deleteFile(new File(filePath));
    }

    public static boolean moveFile(String srcFileName, String destDirName) {

        File srcFile = new File(srcFileName);
        if(!srcFile.exists() || !srcFile.isFile())
            return false;

        File destDir = new File(destDirName);
        if (!destDir.exists())
            destDir.mkdirs();

        return srcFile.renameTo(new File(destDirName + File.separator + srcFile.getName()));
    }

    public static boolean moveDirectory(String srcDirName, String destDirName) {

        if (srcDirName.equals(destDirName))
        {
            return false;
        }

        File srcDir = new File(srcDirName);
        if(!srcDir.exists() || !srcDir.isDirectory())
            return false;

        File destDir = new File(destDirName);
        if(!destDir.exists())
            destDir.mkdirs();

        /**
         * 如果是文件则移动，否则递归移动文件夹。删除最终的空源文件夹
         * 注意移动文件夹时保持文件夹的树状结构
         */
        File[] sourceFiles = srcDir.listFiles();
        for (File sourceFile : sourceFiles) {
            if (sourceFile.isFile())
                moveFile(sourceFile.getAbsolutePath(), destDir.getAbsolutePath());
            else if (sourceFile.isDirectory())
                moveDirectory(sourceFile.getAbsolutePath(),
                        destDir.getAbsolutePath() + File.separator + sourceFile.getName());
            else
                ;
        }
        return srcDir.delete();
    }
}
