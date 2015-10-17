package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
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

    public static final class JsonKeys {
        public static final String GROUPID = "groupId";
        private static final String OBJECTID = "objectId";
        private static final String RESULTS = "results";
        public static final String SESSION_TOKEN = "sessionToken";
        public static final String USERNAME = "username";
        public static final String USERID = "userId";
        public static final String UPDATEAT = "updatedAt";
    }

    public static Promise login(String username, String password) {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                Log.d(TAG, "login jsonObject:" + jsonObject);
                try {
                    String sessionToken = jsonObject.getString(JsonKeys.SESSION_TOKEN);
                    String username = jsonObject.getString(JsonKeys.USERNAME);
                    String userId = jsonObject.getString(JsonKeys.OBJECTID);

                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.SESSION_TOKEN, sessionToken);
                    editor.putString(JsonKeys.USERNAME, username);
                    editor.putString(JsonKeys.USERID, userId);
                    editor.commit();

                    promise.onSuccess();

                } catch (JSONException e) {
                    Log.e(TAG, "login failure", e);
                    promise.onFailure();
                }
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
                promise.onSuccess();
            }
        };

        UrlTemplate template = UrlTemplateCreator.logout();
        new NetworkRequest(template, callback).execute();
        return promise;
    }

    public static Promise signUp(String username, String password) {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                try {
                    String sessionToken = jsonObject.getString(JsonKeys.SESSION_TOKEN);

                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.SESSION_TOKEN, sessionToken);
                    editor.commit();

                    promise.onSuccess();
                } catch (JSONException e) {
                    Log.e(TAG, "sign up failure", e);
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.signUp(username, password);
        new NetworkRequest(template, callback).execute();
        return promise;
    }

    public static Promise getAll() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {

                Realm realm = DataStore.getInstance().getRealm();
                realm.beginTransaction();
                realm.clear(RUser.class);

                try {
                    JSONArray userJsonArray = jsonObject.optJSONArray(JsonKeys.RESULTS);

                    for (int i = 0; i < userJsonArray.length(); i++) {
                        RUser user = realm.createObject(RUser.class);
                        user.setName(userJsonArray.getJSONObject(i).optString(JsonKeys.USERNAME));
                    }

                    realm.commitTransaction();
                    promise.onSuccess();
                } catch(JSONException e) {
                    Log.e(TAG, "Error parsing user object", e);
                    realm.cancelTransaction();
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.getAllUsers();
        new NetworkRequest(template, callback).execute();
        return promise;
    }

    public static Promise joinGroup(String userId, String groupId) {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                Log.d(TAG, "jsonObject:" + jsonObject.toString());
                try {
                    String groupId = jsonObject.getJSONObject(JsonKeys.GROUPID)
                            .getString(JsonKeys.OBJECTID);
                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.GROUPID, groupId);
                    editor.commit();

                    promise.onSuccess();
                } catch (JSONException e) {
                    Log.e(TAG, "joining group failure", e);
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.joinGroup(userId, groupId);
        new NetworkRequest(template, callback).execute();
        return promise;
    }
}
