package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.quarkworks.apartmentgroceries.MyApplication;
import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.service.models.RUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;

/**
 * Created by zz on 10/15/15.
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static final class JsonKeys {
        public static final String GROUP_ID = "groupId";
        private static final String OBJECT_ID = "objectId";
        private static final String RESULTS = "results";
        public static final String SESSION_TOKEN = "sessionToken";
        public static final String USERNAME = "username";
        public static final String USER_ID = "userId";
        public static final String UPDATED_AT = "updatedAt";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String PHOTO = "photo";
        public static final String URL = "url";
    }

    public static Task<Boolean> loginBolts(String username, String password) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.login(username, password);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                JSONObject loginJsonObj = task.getResult();

                if (loginJsonObj == null) {
                    Log.e(TAG, "Error login");
                    return null;
                }

                try {

                    String sessionToken = loginJsonObj.getString(JsonKeys.SESSION_TOKEN);
                    String username = loginJsonObj.getString(JsonKeys.USERNAME);
                    String userId = loginJsonObj.getString(JsonKeys.OBJECT_ID);

                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.SESSION_TOKEN, sessionToken);
                    editor.putString(JsonKeys.USERNAME, username);
                    editor.putString(JsonKeys.USER_ID, userId);

                    JSONObject groupIdObj = loginJsonObj.optJSONObject(JsonKeys.GROUP_ID);
                    if (groupIdObj != null) {
                        String groupId = groupIdObj.optString(JsonKeys.OBJECT_ID);
                        editor.putString(JsonKeys.GROUP_ID, groupId);
                    }
                    editor.commit();
                    return true;

                } catch (JSONException e) {
                    Log.e(TAG, "login failure", e);
                }

                return false;
            }
        });
    }

    public static Promise logout() {
        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                if (jsonObject == null) {
                    promise.onSuccess();
                } else {
                    Log.e(TAG, "Error login out");
                    promise.onFailure();
                }
            }
        };

        UrlTemplate template = UrlTemplateCreator.logout();
        new NetworkRequest(template, callback).execute();
        return promise;
    }

    public static Task<Boolean> signUpBolts(String username, String password) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.signUp(username, password);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                JSONObject signUpJsonObj = task.getResult();

                if (signUpJsonObj == null) {
                    Log.e(TAG, "Error sign up");
                    return null;
                }

                try {
                    String sessionToken = signUpJsonObj.getString(JsonKeys.SESSION_TOKEN);

                    Context context = MyApplication.getContext();
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.login_or_sign_up_session), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(JsonKeys.SESSION_TOKEN, sessionToken);
                    editor.commit();
                    return true;
                } catch (JSONException e) {
                    Log.e(TAG, "sign up failure", e);
                }

                return false;
            }
        });
    }

    public static Promise getAll() {

        final Promise promise = new Promise();

        NetworkRequest.Callback callback = new NetworkRequest.Callback() {
            @Override
            public void done(@Nullable JSONObject jsonObject) {
                if (jsonObject == null) {
                    Log.e(TAG, "Error getting users from server");
                    promise.onFailure();
                    return;
                }

                Realm realm = DataStore.getInstance().getRealm();
                realm.beginTransaction();
                realm.clear(RUser.class);

                try {
                    JSONArray userJsonArray = jsonObject.getJSONArray(JsonKeys.RESULTS);

                    for (int i = 0; i < userJsonArray.length(); i++) {
                        RUser rUser = realm.createObject(RUser.class);
                        try {
                            JSONObject userJsonObj = userJsonArray.getJSONObject(i);

                            rUser.setUserId(userJsonObj.getString(JsonKeys.OBJECT_ID));
                            rUser.setUsername(userJsonObj.getString(JsonKeys.USERNAME));
                            rUser.setEmail(userJsonObj.optString(JsonKeys.EMAIL));
                            rUser.setPhone(userJsonObj.optString(JsonKeys.PHONE));

                            JSONObject groupIdObj = userJsonObj.optJSONObject(JsonKeys.GROUP_ID);
                            if (groupIdObj != null) {
                                rUser.setGroupId(groupIdObj.getString(JsonKeys.OBJECT_ID));
                            }
                            rUser.setPhone(userJsonObj.optString(JsonKeys.PHONE));
                            JSONObject photoJsonObj = userJsonObj.optJSONObject(JsonKeys.PHOTO);
                            if (photoJsonObj != null) {
                                rUser.setUrl(photoJsonObj.getString(JsonKeys.URL));
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing user object", e);
                        }
                    }

                    realm.commitTransaction();
                    promise.onSuccess();
                } catch(JSONException e) {
                    Log.e(TAG, "Error get user object from server", e);
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
                if (jsonObject == null) {
                    Log.e(TAG, "Error getting joining group response json");
                    promise.onFailure();
                    return;
                }

                try {
                    // after joining group successfully, we will get something like
                    // {"updatedAt":"2015-10-20T05:49:21.524Z"}
                    Log.d(TAG, jsonObject.toString());
                    String groupId = jsonObject.getString(JsonKeys.UPDATED_AT);
                    // TODO: do something
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

    public static Task<JSONObject> updateProfilePhoto(String photoName, byte[] data) {
        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.uploadProfilePhoto(photoName, data);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<JSONObject> task) throws Exception {

                if (task.getResult() == null) {
                    Log.e(TAG, "Error getting uploading photo response json");
                    return null;
                }

                try {
                    String photoName = task.getResult().getString("name");
                    Log.d(TAG, "first:" + task.getResult().toString());
                    SharedPreferences sharedPreferences =
                            MyApplication.getContext().getSharedPreferences(
                                    MyApplication.getContext()
                                            .getString(R.string.login_or_sign_up_session), 0);
                    String userId = sharedPreferences.getString(SyncUser.JsonKeys.USER_ID, null);

                    Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
                    UrlTemplate template = UrlTemplateCreator.updateProfilePhoto(userId, photoName);
                    NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

                    return networkRequestBolts.runNetworkRequestBolts().onSuccess(new Continuation<JSONObject, Void>() {
                        public Void then(Task<JSONObject> task) throws Exception {
                            if (task.getResult() == null) {
                                Log.e(TAG, "Error getting updating photo response json");
                            }

                            try {
                                String updatedAt = task.getResult().getString(SyncUser.JsonKeys.UPDATED_AT);
                                if (!TextUtils.isEmpty(updatedAt)) {
                                    // on success
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing updating user photo response json", e);
                            }
                            return null;
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "uploading photo failure", e);
                }

                return null;
            }
        });
    }

    public static Task<RUser> getById(String userId) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getSingleUser(userId);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().continueWith(new Continuation<JSONObject, RUser>() {
            @Override
            public RUser then(Task task) throws Exception {

                Log.d(TAG, "jsonString:" + task.getResult().toString());

                JSONObject userJsonObj = new JSONObject(task.getResult().toString());
                String userId = userJsonObj.getString(JsonKeys.OBJECT_ID);
                String username = userJsonObj.getString(JsonKeys.USERNAME);
                String url = userJsonObj.getJSONObject(JsonKeys.PHOTO).getString(JsonKeys.URL);

                RUser rUser = new RUser();
                rUser.setUserId(userId);
                rUser.setUsername(username);
                rUser.setUrl(url);

                return rUser;
            }
        });
    }
}
