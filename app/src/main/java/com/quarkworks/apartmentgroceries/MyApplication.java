package com.quarkworks.apartmentgroceries;

import android.app.Application;
import android.content.Context;

/**
 * Created by zz on 10/14/15.
 */
public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
