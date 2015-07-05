package com.Tunnel.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.Tunnel.app.TunnelApplication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Yang Yupeng on 2014/8/5.
 */
public class Orientation {

    public static float X = 0.0f;
    public static float Y = 0.0f;
    private static Map<Float, Float> posCorrectionMap = new HashMap<Float, Float>();
    private static Map<Float, Float> negCorrectionMap = new HashMap<Float, Float>();

    public static float adjustDegree(float degree)
    {
        if (X < 0) {
            degree -= Y;
        }
        else
        {
            degree += Y;
        }
        return degree;
    }

    public static void putCorrection(float correction) {
        if (X < 0) {
            negCorrectionMap.put(Y, correction);
            saveCorrection(negCorrectionMap);
        } else {
            posCorrectionMap.put(Y, correction);
            saveCorrection(posCorrectionMap);
        }
    }

    private static void saveCorrection(Map<Float, Float> correctionMap) {
        Set<String> set = new HashSet<String>();
        for (Map.Entry<Float, Float> entry:correctionMap.entrySet()) {
            set.add(String.format("%f;%f", entry.getKey(), entry.getValue()));
        }
        if (set.isEmpty()) {
            return;
        }
        SharedPreferences.Editor editor = TunnelApplication.getInstance().getSharedPreferences("Pref", Context.MODE_PRIVATE).edit();
        editor.putStringSet(correctionMap == posCorrectionMap ? "posCorrectionMap" : "negCorrectionMap",
                set);
        editor.apply();
    }

    public static void restoreCorrection() {
        SharedPreferences sp = TunnelApplication.getInstance().getSharedPreferences("Pref", Context.MODE_PRIVATE);
        Set<String> set = sp.getStringSet("negCorrectionMap", null);
        if (set != null) {
            for (String s : set) {
                String[] split = s.split(";");
                negCorrectionMap.put(Float.valueOf(split[0]), Float.valueOf(split[1]));
            }
        }

        set = sp.getStringSet("posCorrectionMap", null);
        if (set != null) {
            for (String s : set) {
                String [] split = s.split(";");
                posCorrectionMap.put(Float.valueOf(split[0]), Float.valueOf(split[1]));
            }
        }
    }

    public static void clearCorrection() {
        posCorrectionMap.clear();
        negCorrectionMap.clear();
        SharedPreferences sp = TunnelApplication.getInstance().getSharedPreferences("Pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("negCorrectionMap");
        editor.remove("posCorrectionMap");
        editor.apply();
    }

    public static float correctDegreeDiff(float degree)
    {
        Map<Float, Float> objMap = X < 0 ? negCorrectionMap : posCorrectionMap;
        float diff = Float.MAX_VALUE, correct = 0.0f;
        for (Map.Entry<Float, Float> entry:objMap.entrySet()) {
            float abs = Math.abs(entry.getKey() - Y);
            if (abs < diff) {
                diff = abs;
                correct = entry.getValue();
            }
        }
        return degree + correct;
    }

    public static void setOrientationExif(String imagePath)
    {
        ExifUtil.saveOrientation(imagePath);
    }

    public static void getOrientationExif(String imagePath)
    {
        ExifUtil.getOrientation(imagePath);
    }
}
