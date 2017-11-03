package com.example.zxk.mywindowdemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class WindowService extends Service {

    private Context context;
    private WindowController controller;

    public static void startService(Context context){
        context.startService(new Intent(context,WindowService.class));
    }

    public static void stopService(Context context){
        context.stopService(new Intent(context,WindowService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            if(controller != null){
                controller.onDestroy();
                controller = null;
            }
            controller = WindowController.getInstance(context);
            controller.init();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(controller != null){
            controller.onDestroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
