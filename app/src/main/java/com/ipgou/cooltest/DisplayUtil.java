package com.ipgou.cooltest;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by lazen at 2019/10/30 17:26
 */
public class DisplayUtil {
    // No Instance
    private DisplayUtil() {
    }

    public static  DisplayMetrics getDiplayMetrics( ) {
        Context context = App.getApp().getContext();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            display.getRealMetrics(metrics);
        } else {
            display.getMetrics(metrics);
        }
        return metrics;
    }
    public static int getWidth() {
        DisplayMetrics metrics = getDiplayMetrics();
        return metrics.widthPixels;
    }

    public static int getHeight() {
        DisplayMetrics metrics = getDiplayMetrics();
        return metrics.heightPixels;
    }
}
