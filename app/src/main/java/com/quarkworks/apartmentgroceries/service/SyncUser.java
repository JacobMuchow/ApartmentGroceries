package com.quarkworks.apartmentgroceries.service;

import android.content.Context;
import android.content.SharedPreferences;
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
import io.realm.RealmResults;

import com.quarkworks.apartmentgroceries.service.models.RUser.JsonKeys;
import com.quarkworks.apartmentgroceries.service.models.RUser.RealmKeys;

/**
 * Created by zz on 10/15/15.
 */
public class SyncUser {
    private static final String TAG = SyncUser.class.getSimpleName();

    public static Task<Boolean> loginBolts(String username, String password) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.login(username, password);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Boolean> saveLoginCredential = new Continuation<JSONObject, Boolean>() {
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
                    editor.apply();
                    return true;

                } catch (JSONException e) {
                    Log.e(TAG, "login failure", e);
                }

                return false;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(saveLoginCredential);
    }

    public static Task<Boolean> logout() {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.logout();
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Boolean> checkLogout = new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                JSONObject logoutJsonObj = task.getResult();

                if (logoutJsonObj != null && logoutJsonObj.toString().equals("{}")) {
                    return true;
                } else {
                    Log.e(TAG, "Error logout");
                    return false;
                }
            }
        };

        return networkRequest.runNetworkRequest().continueWith(checkLogout);
    }

    public static Task<Boolean> signUp(String username, String password) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.signUp(username, password);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Boolean> saveCredential = new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    throw task.getError();
                } else {
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
                        editor.apply();
                        return true;
                    } catch (JSONException e) {
                        Log.e(TAG, "sign up failure", e);
                        return false;
                    }
                }
            }
        };

        return networkRequest.runNetworkRequest().continueWith(saveCredential);
    }

    public static Task getAll(){

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getAllUsers();
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Void> addUsersToRealm = new Continuation<JSONObject, Void>() {
            @Override
            public Void then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    throw task.getError();
                } else {
                    JSONObject jsonObject = task.getResult();
                    if (jsonObject == null) {
                        Log.e(TAG, "Error getting users from server");
                        return null;
                    }

                    Realm realm = Realm.getInstance(MyApplication.getContext());
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
                    } catch (JSONException e) {
                        Log.e(TAG, "Error get user object from server", e);
                        realm.cancelTransaction();
                    }
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(addUsersToRealm);
    }

    public static Task<Boolean> joinGroup(String userId, String groupId) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.joinGroup(userId, groupId);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, Boolean> checkJoiningGroup = new Continuation<JSONObject, Boolean>() {
            @Override
            public Boolean then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    throw task.getError();
                } else {
                    JSONObject jsonObject = task.getResult();
                    if (jsonObject == null) {
                        Log.e(TAG, "Error getting joining group response json");
                        return false;
                    }

                    try {
                        return !TextUtils.isEmpty(jsonObject.getString(JsonKeys.UPDATED_AT));
                    } catch (JSONException e) {
                        Log.e(TAG, "joining group failure", e);
                        return false;
                    }
                }
            }
        };

        return networkRequest.runNetworkRequest().continueWith(checkJoiningGroup);
    }

    public static Task<JSONObject> updateProfilePhoto(String photoName, byte[] data) {

        SharedPreferences sharedPreferences =
                MyApplication.getContext().getSharedPreferences(
                        MyApplication.getContext()
                                .getString(R.string.login_or_sign_up_session), 0);
        final String userId = sharedPreferences.getString(JsonKeys.USER_ID, null);

        final Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();

        Continuation<JSONObject, JSONObject> updateUserPhoto = new Continuation<JSONObject, JSONObject>() {
            @Override
            public JSONObject then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    throw task.getError();
                } else {
                    String photoName = task.getResult().optString("name");
                    Log.d(TAG, "get new photo name:" + photoName);
                    final UrlTemplate template = UrlTemplateCreator.updateProfilePhoto(userId, photoName);
                    new NetworkRequest(template, taskCompletionSource).runNetworkRequest();
                }

                return null;
            }
        };

        uploadPhoto(photoName, data).continueWith(updateUserPhoto);

        return taskCompletionSource.getTask();
    }

    public static Task<JSONObject> uploadPhoto(String photoName, byte[] data) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.uploadProfilePhoto(photoName, data);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, JSONObject> uploadingPhoto = new Continuation<JSONObject, JSONObject>() {
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
        };

        return networkRequest.runNetworkRequest().onSuccess(uploadingPhoto);

    }

    public static Task getById(String userId) {

        Task<JSONObject>.TaskCompletionSource taskCompletionSource = Task.create();
        UrlTemplate template = UrlTemplateCreator.getSingleUser(userId);
        NetworkRequest networkRequest = new NetworkRequest(template, taskCompletionSource);

        Continuation<JSONObject, RUser> addSingleUserToRealm = new Continuation<JSONObject, RUser>() {
            @Override
            public RUser then(Task<JSONObject> task) throws Exception {
                if (task.isFaulted()) {
                    throw task.getError();
                } else {
                    if (task.getResult() == null) {
                        Log.e(TAG, "Error getting user's information from server");
                        return null;
                    }

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

                        Realm realm = Realm.getInstance(MyApplication.getContext());
                        RealmResults<RUser> rUsers = realm.where(RUser.class)
                                .equalTo(RealmKeys.USER_ID, rUser.getUserId()).findAll();
                        if (rUsers != null) {
                            Realm realmDelete = Realm.getInstance(MyApplication.getContext());
                            realmDelete.beginTransaction();
                            for (int i = 0; i < rUsers.size(); i++) {
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
                }

                return null;
            }
        };

        return networkRequest.runNetworkRequest().continueWith(addSingleUserToRealm);
    }
}
