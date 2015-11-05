package com.quarkworks.apartmentgroceries;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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
        RealmConfiguration config = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);

        Parse.initialize(this, "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD", "mMIya93OgPvuxLyAADCOYzGxvI8xuhktbMNQlIQ5");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static Context getContext() {
        return context;
    }

}
