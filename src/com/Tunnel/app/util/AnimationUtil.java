package com.Tunnel.app.util;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.Tunnel.app.R;
import com.Tunnel.app.TunnelApplication;

/**
 * @author yupeng.yyp
 * @create 14-10-12 22:10
 */
public class AnimationUtil {

    private static AnimationUtil instance = null;
    public static AnimationUtil getInstance()
    {
        if (instance == null)
        {
            instance = new AnimationUtil();
        }
        return instance;
    }

    private AnimationUtil() {
        Context context = TunnelApplication.getInstance();
        leftInAnimation = AnimationUtils.loadAnimation(context, R.anim.left_in);
        leftOutAnimation = AnimationUtils.loadAnimation(context, R.anim.left_out);
        rightInAnimation = AnimationUtils.loadAnimation(context, R.anim.right_in);
        rightOutAnimation = AnimationUtils.loadAnimation(context, R.anim.right_out);
    }

    public Animation leftInAnimation;
    public Animation leftOutAnimation;
    public Animation rightInAnimation;
    public Animation rightOutAnimation;
}
