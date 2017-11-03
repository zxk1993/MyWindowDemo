package com.example.zxk.mywindowdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by ZXK on 2017/10/30.
 */

public class MyApplication extends Application {

    private int activityCount;

    @Override
    public void onCreate() {
        super.onCreate();

        //是否是前台
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityCount++;
                if(activityCount == 1){
                    WindowHelper.instance.setForeground(true);
                    WindowHelper.instance.startWindowService(getApplicationContext());
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityCount --;
                if(activityCount == 0){
                    WindowHelper.instance.setForeground(false);
                    WindowHelper.instance.stopWindowService(getApplicationContext());
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }
}
