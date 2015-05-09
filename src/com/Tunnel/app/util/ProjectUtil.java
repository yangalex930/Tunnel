package com.Tunnel.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.Tunnel.app.R;
import com.Tunnel.app.TunnelApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yupeng.yyp
 * @create 14-8-24 16:40
 */
public class ProjectUtil {
    private static String defaultProject = null;
    private static List<String> projectList = new ArrayList<String>();
    private static Context context = TunnelApplication.getInstance();
    static {
        initProjectList();
        projectList.add(context.getString(R.string.create_new_project));
        getDefaultProjectPref();
        if (projectList.indexOf(defaultProject) == -1)
        {
            defaultProject = context.getString(R.string.default_project);
        }
    }

    public static List<String> getProjects()
    {
        return projectList;
    }

    public static void addProject(String s)
    {
        if (projectList.indexOf(s) == -1)
        {
            projectList.add(projectList.size() - 1, s);
        }

        defaultProject = s;
        saveDefaultProjectPref();
        TunnelUtil.CreateProjectFolder(defaultProject);
    }

    public static void deleteProject(String s)
    {
        if (projectList.indexOf(s) != -1 && !s.equals(context.getString(R.string.default_project)))
        {
            projectList.remove(s);
        }

        defaultProject = context.getString(R.string.default_project);
        saveDefaultProjectPref();
    }

    public static void selectProject(int index)
    {
        if (index == projectList.size() - 1 || projectList.get(index).equals(defaultProject))
        {
            return;
        }
        defaultProject = projectList.get(index);
        saveDefaultProjectPref();
    }

    public static String getDefaultProject()
    {
        return defaultProject;
    }

    public static int getDefaultIndex()
    {
        return projectList.indexOf(defaultProject);
    }

    public static String genDefaultName()
    {
        int i = 1;
        while (true)
        {
            String s = "工程" + i;
            if (projectList.indexOf(s) == -1)
            {
                return s;
            }
            i++;
        }
    }

    public static void renameProject(String oldProject, String newProject)
    {
        int pos = projectList.indexOf(oldProject);
        if (pos != -1)
        {
            projectList.set(pos, newProject);
        }

        if (defaultProject.equals(oldProject))
        {
            defaultProject = newProject;
            saveDefaultProjectPref();
        }

        if (oldProject.equals(context.getString(R.string.default_project)))
        {
            projectList.add(0, context.getString(R.string.default_project));
        }
    }

    private static void saveDefaultProjectPref()
    {
        SharedPreferences sp = context.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("DefaultProject", defaultProject);
        editor.commit();
    }

    private static void getDefaultProjectPref()
    {
        SharedPreferences sp = context.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        defaultProject = sp.getString("DefaultProject", context.getString(R.string.default_project));
    }

    private static void initProjectList()
    {
        File[] files = FileUtil.getFileList(TunnelUtil.getTunnelFolderPath());
        for (File file:files)
        {
            if (file.isDirectory())
            {
                projectList.add(file.getName());
            }
        }
    }
}
