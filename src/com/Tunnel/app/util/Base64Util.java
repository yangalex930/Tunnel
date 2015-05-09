package com.Tunnel.app.util;
import android.util.Base64;



/**
 * @author yupeng.yyp
 * @create 14-10-4 14:45
 */
public class Base64Util {

    public static String encode(String s) {
        if (s == null) {
            return null;
        }
        return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP);
    }

    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        return new String(Base64.decode(s, Base64.NO_WRAP));
    }
}
