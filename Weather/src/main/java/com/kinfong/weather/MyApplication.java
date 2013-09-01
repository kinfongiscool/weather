package com.kinfong.weather;

import android.app.Application;
import android.content.Context;

/**
 * Created by Kin on 9/1/13.
 */
public class MyApplication extends Application{

    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
