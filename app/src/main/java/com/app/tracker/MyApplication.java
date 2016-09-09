package com.app.tracker;

import android.app.Application;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.app.utility.Singleton;

/**
 * Created by VIJAY on 02-09-2016.
 */
public class MyApplication extends MultiDexApplication {

    public static final String TAG = MyApplication.class
            .getSimpleName();

    private static MyApplication mAppInstance;
    private static boolean activityVisible;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        java.util.logging.Logger logger =
                java.util.logging.Logger.getLogger(
                        "sun.net.www.protocol.http.HttpURLConnection");
        logger.setLevel(java.util.logging.Level.FINE);
        mAppInstance = this;
        MultiDex.install(this);
        initSingleton();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }


    private void initSingleton() {
        Singleton.getInstance(getApplicationContext());
    }


    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static synchronized MyApplication getInstance() {
        return mAppInstance;
    }
}
