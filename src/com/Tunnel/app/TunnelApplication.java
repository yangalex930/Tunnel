package com.Tunnel.app;

import android.app.Application;
import android.content.SharedPreferences;
import com.Tunnel.app.util.ProjectUtil;

/**
 * @author yupeng.yyp
 * @create 14-8-30 13:07
 */
public class TunnelApplication extends Application {

    private static TunnelApplication instance = null;

    public static TunnelApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
