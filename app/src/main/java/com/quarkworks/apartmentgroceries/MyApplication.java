package com.quarkworks.apartmentgroceries;

import android.app.Application;
import android.content.Context;

/**
 * Created by zz on 10/14/15.
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static Context context;
    private String sessionToken;
    private String userId;
    private String groupId;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
