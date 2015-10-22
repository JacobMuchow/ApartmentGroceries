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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by zz on 10/15/15.
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static final Executor DISK_EXECUTOR = Executors.newCachedThreadPool();

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

        SharedPreferences sharedPreferences =
                MyApplication.getContext().getSharedPreferences(
                        MyApplication.getContext()
                                .getString(R.string.login_or_sign_up_session), 0);
        final String userId = sharedPreferences.getString(SyncUser.JsonKeys.USER_ID, null);

        final Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();

        uploadPhoto(photoName, data).onSuccess(new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isCancelled()) {
                    return null;
                } else if (task.isFaulted()) {
                    throw task.getError();
                } else {
                    String photoName = task.getResult().optString("name");
                    Log.d(TAG, "get new photo name:" + photoName);
                    final UrlTemplate template = UrlTemplateCreator.updateProfilePhoto(userId, photoName);
                    new NetworkRequestBolts(template, taskCompletionSource).runNetworkRequestBolts();
                }

                return null;
            }
        });

        return taskCompletionSource.getTask();
    }

    public static Task<JSONObject> uploadPhoto(String photoName, byte[] data) {
        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.uploadProfilePhoto(photoName, data);
        NetworkRequestBolts networkRequestBolts = new NetworkRequestBolts(template, taskCompletionSource);

        return networkRequestBolts.runNetworkRequestBolts().onSuccess(new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {

                if (task.getResult() == null) {
                    Log.e(TAG, "Error getting uploading photo response json");
                    return null;
                }

                try {
                    String photoName = task.getResult().getString("name");
                    Log.d(TAG, "uploaded photo name:" + photoName);
                    return task.getResult();

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
            public RUser then(Task<JSONObject> task) throws Exception {

                try {
                    JSONObject userJsonObj = new JSONObject(task.getResult().toString());

                    RUser rUser = new RUser();
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

                    String userId = rUser.getUserId();
                    Realm realm = Realm.getInstance(MyApplication.getContext());
                    RealmResults<RUser> rUsers = realm.where(RUser.class)
                            .equalTo(SyncUser.JsonKeys.USER_ID, userId).findAll();
                    if (rUsers != null) {
                        Realm realmDelete = Realm.getInstance(MyApplication.getContext());
                        realmDelete.beginTransaction();
                        for(int i = 0; i < rUsers.size(); i++) {
                            rUsers.get(i).removeFromRealm();
                        }
                        rUsers.clear();
                        realmDelete.commitTransaction();
                    }

                    Realm realmInsert = Realm.getInstance(MyApplication.getContext());
                    realmInsert.beginTransaction();
                    realmInsert.copyToRealm(rUser);
                    realmInsert.commitTransaction();

                    return rUser;

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing user object", e);
                }

                return null;
            }
        },DISK_EXECUTOR);
    }
}
