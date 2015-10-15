package com.quarkworks.apartmentgroceries;

import android.app.Application;
import android.content.Context;

/**
 * Created by zz on 10/14/15.
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
