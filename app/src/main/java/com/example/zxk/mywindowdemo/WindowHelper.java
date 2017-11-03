package com.example.zxk.mywindowdemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ZXK on 2017/10/30.
 */

public enum WindowHelper {
    instance;

    //是否有权限
    private boolean hasPermission;
    //是否在前台
    private boolean isForeground;
    //是否正在运行
    private boolean isRunning;

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean foreground) {
        isForeground = foreground;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void startWindowService(Context context){
        if(isForeground && !isRunning && hasPermission){
            WindowService.startService(context);
        }
    }

    public void stopWindowService(Context context){
        WindowService.stopService(context);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * top的x、y坐标
     * @param context
     * @param x
     */

    public static void setCoordinateX(Context context,int x){
        SharedPreferences.Editor editor = context.getSharedPreferences("window_sp",Context.MODE_PRIVATE).edit();
        editor.putInt("windowX",x);
        editor.apply();
    }

    public static int getCoordinateX(Context context){
        return context.getSharedPreferences("window_sp",Context.MODE_PRIVATE).getInt("windowX",100);
    }

    public static void setCoordinateY(Context context,int y){
        SharedPreferences.Editor editor = context.getSharedPreferences("window_sp",Context.MODE_PRIVATE).edit();
        editor.putInt("windowY",y);
        editor.apply();
    }

    public static int getCoordinateY(Context context){
        return context.getSharedPreferences("window_sp",Context.MODE_PRIVATE).getInt("windowY",100);
    }
}
