package com.Tunnel.app.activity;

import android.app.Activity;

/**
 * Created by Yang Yupeng on 2014/7/28.
 */
public class Module {
    public int titleRes;
    public int iconRes;
    public Class<? extends Activity> intendClass;
    public int resultCode = -1;
    public ModuleClickListener moduleClickListener;

    public interface ModuleClickListener
    {
        void onClickModule();
    }

    public static Module makeModule(int titleRes, int iconRes, Class<? extends Activity> intendClass)
    {
        return makeModule(titleRes, iconRes, intendClass, -1);
    }

    public static Module makeModule(int titleRes, int iconRes, Class<? extends Activity> intendClass, int resultCode)
    {
        Module module = new Module();
        module.titleRes = titleRes;
        module.iconRes = iconRes;
        module.intendClass = intendClass;
        module.resultCode = resultCode;
        return module;
    }

    public static Module makeModule(int titleRes, int iconRes, ModuleClickListener listener)
    {
        Module module = new Module();
        module.titleRes = titleRes;
        module.iconRes = iconRes;
        module.moduleClickListener = listener;
        return module;
    }
}
