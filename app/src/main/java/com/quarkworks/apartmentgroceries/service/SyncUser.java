package com.quarkworks.apartmentgroceries.service;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;

import org.json.JSONObject;

/**
 * @author jacobamuchow@gmail.com (Jacob Muchow)
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static Promise login(String username, String password) {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                Log.d(TAG, "login jsonObject:" + jsonObject.toString());
                String sessionToken = jsonObject.optString("sessionToken");
                String username = jsonObject.optString("username");

                SharedPreferences sharedPreferences = MyApplication.getContext().getSharedPreferences("login", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("sessionToken", sessionToken);
                editor.putString("username", username);
                editor.commit();

                //TODO: update realm

                //TODO: update user preferences

                promise.onSuccess();

                //or promise.onFailure() depending
            }
        };

        UrlTemplate template = UrlTemplateCreator.login(username, password);
        new NetworkRequest(template, callback).execute();
        return promise;
    }

    public static Promise logout() {
        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                //todo: logout

                promise.onSuccess();
            }
        };

        UrlTemplate template = UrlTemplateCreator.logout();
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}
