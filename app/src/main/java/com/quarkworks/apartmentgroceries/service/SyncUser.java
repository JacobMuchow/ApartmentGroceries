package com.quarkworks.apartmentgroceries.service;

import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * @author jacobamuchow@gmail.com (Jacob Muchow)
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static void login(String username, String password) {

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                //TODO: do stuff
            }
        };

        UrlTemplate template = UrlTemplateCreator.login(username, password);
        new NetworkRequest(template, callback).execute();
    }

    public static void logout() {

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                //todo: logout
            }
        };

        UrlTemplate template = UrlTemplateCreator.logout();
        new NetworkRequest(template, callback).execute();
    }
}
