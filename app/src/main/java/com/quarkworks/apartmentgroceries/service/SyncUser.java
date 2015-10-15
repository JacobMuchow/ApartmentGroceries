package com.quarkworks.apartmentgroceries.service;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

/**
 * Created by zz on 10/15/15.
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    private static final class JsonKeys {
        private static final String RESULTS = "results";
        private static final String USERNAME = "username";
    }

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

    public static Promise getAllUser() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                Realm realm = DataStore.getDataStore().getRealm();
                realm.beginTransaction();
                realm.clear(RUser.class);

                Log.d(TAG, "user jsonObject:" + jsonObject);

                JSONArray userJsonArray = jsonObject.optJSONArray(JsonKeys.RESULTS);

                for (int i = 0; i < userJsonArray.length(); i++) {

                    try {
                        RUser user = realm.createObject(RUser.class);
                        user.setName(userJsonArray.getJSONObject(i).optString(JsonKeys.USERNAME));
                        Log.d(TAG, "user in parsing:" + user.getName());
                    } catch(JSONException e) {
                        Log.d(TAG, "Error parsing user object");
                    }

                }

                realm.commitTransaction();

                promise.onSuccess();

                //or promise.onFailure() depending
            }
        };

        UrlTemplate template = UrlTemplateCreator.getAllUser();
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}
